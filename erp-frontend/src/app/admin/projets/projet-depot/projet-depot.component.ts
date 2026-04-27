import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { ProjetService } from '../../../core/services/projet.service';
import { ActiviteService } from '../../../core/services/activite.service';
import { TacheService } from '../../../core/services/tache.service';
import { ProjetResponse } from '../../../core/models/projet.model';
import { ActiviteResponse, TacheResponse } from '../../../core/models/activite.model';
import { AdminSidebarComponent } from '../../shared/sidebar/sidebar.component';

@Component({
  selector: 'app-projet-depot',
  standalone: true,
  imports: [CommonModule, RouterModule, AdminSidebarComponent, FormsModule],
  templateUrl: './projet-depot.component.html',
  styleUrls: ['./projet-depot.component.css']
})
export class ProjetDepotComponent implements OnInit {
  projetId: number = 0;
  projet: ProjetResponse | null = null;
  activites: ActiviteResponse[] = [];
  isLoading = false;
  errorMessage = '';
  successMessage = '';
  alertType: 'error' | 'success' = 'error';

  // Panneaux de dépôt inline - états séparés par type pour éviter les conflits d'ID
  selectedTacheId: number | null = null;
  selectedActiviteId: number | null = null;
  selectedProjetDepot: boolean = false;
  
  selectedTab: 'lien' | 'fichier' = 'lien';
  depotLien: string = '';
  selectedFile: File | null = null;
  depotErrorMessage: string = '';

  // Stockage des dépôts par entité
  depotParTache: Map<number, any> = new Map();
  depotParActivite: Map<number, any> = new Map();
  depotProjet: any = null;
  
  // Progression du projet
  progressionProjet: number = 0;
  
  // Mode modification
  modeModification: boolean = false;

  @ViewChild('fileInput') fileInput!: ElementRef;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private projetService: ProjetService,
    private activiteService: ActiviteService,
    private tacheService: TacheService
  ) {}

  ngOnInit(): void {
    this.projetId = +this.route.snapshot.paramMap.get('id')!;
    this.loadData();
  }

  loadData(): void {
    this.isLoading = true;
    this.errorMessage = '';
    
    this.projetService.getById(this.projetId).subscribe({
      next: (projet) => {
        this.projet = projet;
        
        // Charger le dépôt du projet s'il existe
        if (projet.depots && projet.depots.length > 0) {
          this.depotProjet = projet.depots[0];
          console.log('>>> Dépôt projet chargé:', this.depotProjet);
        }
        
        // Charger la progression depuis le backend
        if (projet.progression !== undefined) {
          this.progressionProjet = projet.progression;
          console.log('>>> Progression projet chargée:', this.progressionProjet);
        }
        
        this.loadActivites();
      },
      error: (err) => {
        this.errorMessage = 'Erreur lors du chargement du projet';
        this.isLoading = false;
      }
    });
  }

  loadActivites(): void {
    console.log('>>> loadActivites() - rechargement des activités pour projet:', this.projetId);
    this.activiteService.getByProjet(this.projetId).subscribe({
      next: (activites) => {
        console.log('>>> Activités rechargées:', activites);
        this.activites = activites;
        
        // Réinitialiser les Maps pour forcer le rechargement depuis la base
        this.depotParTache = new Map<number, any>();
        this.depotParActivite = new Map<number, any>();
        
        // DEBUG : Log complet des données reçues
        console.log('=== DEBUG loadActivites ===');
        console.log('Nombre d\'activités reçues:', activites.length);
        
        // Remplir les Maps avec les dépôts reçus du backend
        activites.forEach((activite: any) => {
          console.log('=== ACTIVITE:', activite.id, activite.nom);
          console.log('    depots activite:', activite.depots);
          
          // Dépôts des TÂCHES - cherche dans chaque tâche
          activite.taches?.forEach((tache: any) => {
            console.log('    TACHE:', tache.id, tache.nom);
            console.log('    depots tache:', tache.depots);
            console.log('    estDepose:', tache.estDepose);
            
            // Log de chaque dépôt pour voir les champs exacts
            tache.depots?.forEach((d: any) => {
              console.log('      depot champs:', JSON.stringify(d));
            });
            
            if (tache.depots && tache.depots.length > 0) {
              // Chercher un dépôt appartenant à cette tâche
              // Compatible avec tous les formats backend : tacheId, tache_id, tache.id
              const depotTache = tache.depots.find((d: any) => {
                const idTache = d.tacheId ?? d.tache_id ?? d.tache?.id;
                // Si le champ tacheId existe et correspond → OK
                // Si le champ n'existe pas mais le depot est dans tache.depots → c'est forcément le bon
                return idTache === tache.id || idTache === undefined;
              });
              
              if (depotTache) {
                this.depotParTache.set(tache.id, depotTache);
                console.log('>>> Dépôt tâche mappé:', tache.id, depotTache);
              }
            }
          });
          
          // Dépôt activité = dans activite.depots directement et tacheId est null/undefined
          const depotActivite = activite.depots?.find((d: any) => {
            const idTache = d.tacheId ?? d.tache_id ?? d.tache?.id;
            return idTache === null || idTache === undefined;
          });
          
          if (depotActivite) {
            this.depotParActivite.set(activite.id, depotActivite);
            console.log('>>> Dépôt activité mappé:', activite.id, depotActivite);
          }
        });
        console.log('=== FIN DEBUG ===');
        
        console.log('>>> Dépôts chargés - tâches:', this.depotParTache.size, 'activités:', this.depotParActivite.size);
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = 'Erreur lors du chargement des activités';
        this.isLoading = false;
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/admin/projets/board']);
  }

  // Méthodes pour les tâches
  deposerTache(tache: TacheResponse): void {
    console.log('>>> deposerTache appelé avec:', { tacheId: tache.id, tacheNom: tache.nom, projetId: this.projetId });
    
    // Fermer les autres panneaux et ouvrir celui de la tâche
    this.selectedActiviteId = null;
    this.selectedProjetDepot = false;
    this.selectedTacheId = tache.id;
    this.modeModification = false;
    this.selectedTab = 'lien';
    this.depotLien = '';
    this.selectedFile = null;
    this.depotErrorMessage = '';
  }

  // Méthode pour modifier un dépôt de tâche
  modifierDepotTache(tache: TacheResponse): void {
    console.log('>>> modifierDepotTache appelé avec:', { tacheId: tache.id });
    
    const depot = this.depotParTache.get(tache.id);
    if (!depot) {
      console.error('>>> Aucun dépôt trouvé pour la tâche', tache.id);
      return;
    }
    
    // Fermer les autres panneaux et ouvrir celui de la tâche en mode modification
    this.selectedActiviteId = null;
    this.selectedProjetDepot = false;
    this.selectedTacheId = tache.id;
    this.modeModification = true;
    
    // Pré-remplir le formulaire avec les données existantes
    this.selectedTab = depot.type === 'fichier' ? 'fichier' : 'lien';
    this.depotLien = depot.lien || '';
    this.selectedFile = null; // Le fichier ne peut pas être pré-rempli
    this.depotErrorMessage = '';
    
    console.log('>>> Formulaire pré-rempli:', { type: this.selectedTab, lien: this.depotLien });
  }

  // Méthodes pour les activités — basées sur depotParTache
  getNombreTachesDeposees(activite: ActiviteResponse): number {
    const deposees = activite.taches?.filter((t: any) => 
      this.depotParTache.has(t.id)
    ).length || 0;
    console.log('>>> getNombreTachesDeposees:', activite.nom, deposees);
    return deposees;
  }

  getNombreTachesTotal(activite: ActiviteResponse): number {
    return activite.taches?.length || 0;
  }

  toutesLesTachesDeposees(activite: ActiviteResponse): boolean {
    const total = this.getNombreTachesTotal(activite);
    const deposees = this.getNombreTachesDeposees(activite);
    const result = total > 0 && deposees === total;
    console.log('>>> toutesLesTachesDeposees:', activite.nom, result, deposees, '/', total);
    return result;
  }

  canDeposerActivite(activite: ActiviteResponse): boolean {
    return this.toutesLesTachesDeposees(activite);
  }

  deposerActivite(activite: ActiviteResponse): void {
    if (!this.toutesLesTachesDeposees(activite)) return;

    // Fermer les autres panneaux et ouvrir celui de l'activité
    this.selectedTacheId = null;
    this.selectedProjetDepot = false;
    this.selectedActiviteId = activite.id;
    this.modeModification = false;
    this.selectedTab = 'lien';
    this.depotLien = '';
    this.selectedFile = null;
    this.depotErrorMessage = '';
  }

  // Méthode pour modifier un dépôt d'activité
  modifierDepotActivite(activite: ActiviteResponse): void {
    console.log('>>> modifierDepotActivite appelé avec:', { activiteId: activite.id });
    
    const depot = this.depotParActivite.get(activite.id);
    if (!depot) {
      console.error('>>> Aucun dépôt trouvé pour l\'activité', activite.id);
      return;
    }
    
    // Fermer les autres panneaux et ouvrir celui de l'activité en mode modification
    this.selectedTacheId = null;
    this.selectedProjetDepot = false;
    this.selectedActiviteId = activite.id;
    this.modeModification = true;
    
    // Pré-remplir le formulaire avec les données existantes
    this.selectedTab = depot.type === 'fichier' ? 'fichier' : 'lien';
    this.depotLien = depot.lien || '';
    this.selectedFile = null;
    this.depotErrorMessage = '';
    
    console.log('>>> Formulaire activité pré-rempli:', { type: this.selectedTab, lien: this.depotLien });
  }

  // Méthodes pour le projet — basées sur depotParActivite
  getNombreActivitesDeposees(): number {
    const deposees = this.activites?.filter((a: any) => 
      this.depotParActivite.has(a.id)
    ).length || 0;
    console.log('>>> getNombreActivitesDeposees:', deposees);
    return deposees;
  }

  getNombreActivitesTotal(): number {
    return this.activites?.length || 0;
  }

  toutesLesActivitesDeposees(): boolean {
    const total = this.getNombreActivitesTotal();
    const deposees = this.getNombreActivitesDeposees();
    const result = total > 0 && deposees === total;
    console.log('>>> toutesLesActivitesDeposees:', result, deposees, '/', total);
    return result;
  }

  canDeposerProjet(): boolean {
    return this.toutesLesActivitesDeposees();
  }

  deposerProjet(): void {
    if (!this.toutesLesActivitesDeposees() || !this.projet) return;

    // Fermer les autres panneaux et ouvrir celui du projet
    this.selectedTacheId = null;
    this.selectedActiviteId = null;
    this.selectedProjetDepot = true;
    this.modeModification = false;
    this.selectedTab = 'lien';
    this.depotLien = '';
    this.selectedFile = null;
    this.depotErrorMessage = '';
  }

  // Méthode pour modifier un dépôt de projet
  modifierDepotProjet(): void {
    console.log('>>> modifierDepotProjet appelé');
    
    if (!this.depotProjet) {
      console.error('>>> Aucun dépôt trouvé pour le projet');
      return;
    }
    
    // Ouvrir le panneau du projet en mode modification
    this.selectedTacheId = null;
    this.selectedActiviteId = null;
    this.selectedProjetDepot = true;
    this.modeModification = true;
    
    // Pré-remplir le formulaire
    this.selectedTab = this.depotProjet.type === 'fichier' ? 'fichier' : 'lien';
    this.depotLien = this.depotProjet.lien || '';
    this.selectedFile = null;
    this.depotErrorMessage = '';
    
    console.log('>>> Formulaire projet pré-rempli:', { type: this.selectedTab, lien: this.depotLien });
  }

  // Alias pour compatibilité avec le template existant
  getTachesDeposeesCount(activite: ActiviteResponse): number {
    return this.getNombreTachesDeposees(activite);
  }

  getTachesTotalCount(activite: ActiviteResponse): number {
    return this.getNombreTachesTotal(activite);
  }

  getActivitesDeposeesCount(): number {
    return this.getNombreActivitesDeposees();
  }

  getActivitesTotalCount(): number {
    return this.getNombreActivitesTotal();
  }

  // Méthodes d'affichage
  formatDate(dateStr: string): string {
    if (!dateStr) return '';
    const date = new Date(dateStr);
    return date.toLocaleDateString('fr-FR', { day: '2-digit', month: '2-digit', year: 'numeric' });
  }

  showError(message: string): void {
    this.errorMessage = message;
    this.successMessage = '';
    this.alertType = 'error';
    setTimeout(() => {
      this.errorMessage = '';
    }, 5000);
  }

  showSuccess(message: string): void {
    this.successMessage = message;
    this.errorMessage = '';
    this.alertType = 'success';
    setTimeout(() => {
      this.successMessage = '';
    }, 3000);
  }

  // Panneau de dépôt inline
  closeDepotPanel(): void {
    this.selectedTacheId = null;
    this.selectedActiviteId = null;
    this.selectedProjetDepot = false;
    this.modeModification = false;
    this.selectedTab = 'lien';
    this.depotLien = '';
    this.selectedFile = null;
    this.depotErrorMessage = '';
  }

  // Helper pour obtenir l'ID actuellement sélectionné selon le type
  getSelectedTargetId(): number | null {
    if (this.selectedTacheId) return this.selectedTacheId;
    if (this.selectedActiviteId) return this.selectedActiviteId;
    if (this.selectedProjetDepot && this.projet) return this.projet.id;
    return null;
  }

  // Helper pour déterminer le type de cible actuel
  getSelectedTargetType(): 'tache' | 'activite' | 'projet' | null {
    if (this.selectedTacheId) return 'tache';
    if (this.selectedActiviteId) return 'activite';
    if (this.selectedProjetDepot) return 'projet';
    return null;
  }

  onTabChange(tab: 'lien' | 'fichier'): void {
    this.selectedTab = tab;
    this.depotErrorMessage = '';
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files[0]) {
      this.selectedFile = input.files[0];
    }
  }

  triggerFileInput(): void {
    if (this.fileInput) {
      this.fileInput.nativeElement.click();
    }
  }

  removeFile(): void {
    this.selectedFile = null;
  }

  submitDepot(): void {
    const targetId = this.getSelectedTargetId();
    const targetType = this.getSelectedTargetType();
    
    console.log('>>> submitDepot:', { targetId, targetType, selectedTacheId: this.selectedTacheId, selectedActiviteId: this.selectedActiviteId, selectedProjetDepot: this.selectedProjetDepot });
    
    if (!targetId || !targetType) return;

    this.depotErrorMessage = '';

    if (this.selectedTab === 'lien' && !this.depotLien) {
      this.depotErrorMessage = 'Veuillez entrer un lien';
      return;
    }

    if (this.selectedTab === 'fichier' && !this.selectedFile) {
      this.depotErrorMessage = 'Veuillez sélectionner un fichier';
      return;
    }

    const depotData = {
      type: this.selectedTab,
      value: this.selectedTab === 'lien' ? this.depotLien : this.selectedFile!
    };

    console.log('>>> Appel service avec:', { targetType, targetId, depotData });
    
    switch (targetType) {
      case 'tache':
        console.log('>>> APPEL TACHE SERVICE - ID:', targetId);
        this.tacheService.deposerTache(targetId, depotData).subscribe({
          next: (response: any) => {
            console.log('>>> Dépôt tâche SUCCESS - réponse:', response);
            // Extraire le dépôt de la réponse (plusieurs formats possibles)
            let depot = null;
            if (response.depots && response.depots.length > 0) {
              // Format 1: réponse = tâche avec tableau depots
              depot = response.depots[response.depots.length - 1];
            } else if (response.id && response.type) {
              // Format 2: réponse = dépôt direct
              depot = response;
            }
            if (depot) {
              this.depotParTache.set(targetId, depot);
              console.log('>>> Carte dépôt affichée pour tâche', targetId, ':', depot);
            }
            this.showSuccess('Tâche déposée avec succès');
            this.closeDepotPanel();
            // Recharger en arrière-plan pour synchroniser
            this.loadActivites();
          },
          error: (err) => {
            this.depotErrorMessage = err.error || 'Erreur lors du dépôt de la tâche';
          }
        });
        break;
      case 'activite':
        console.log('>>> APPEL ACTIVITE SERVICE - ID:', targetId);
        this.activiteService.deposerActivite(targetId, depotData).subscribe({
          next: (response: any) => {
            console.log('>>> Dépôt activité SUCCESS - réponse:', response);
            // Extraire le dépôt de la réponse
            let depot = null;
            if (response.depots && response.depots.length > 0) {
              depot = response.depots[response.depots.length - 1];
            } else if (response.id && response.type) {
              depot = response;
            }
            if (depot) {
              this.depotParActivite.set(targetId, depot);
              console.log('>>> Carte dépôt affichée pour activité', targetId, ':', depot);
            }
            this.showSuccess('Activité déposée avec succès');
            this.closeDepotPanel();
            this.loadActivites();
            // Mettre à jour la progression depuis le backend
            this.updateProgressionFromBackend();
          },
          error: (err) => {
            this.depotErrorMessage = err.error || 'Erreur lors du dépôt de l\'activité';
          }
        });
        break;
      case 'projet':
        console.log('>>> APPEL PROJET SERVICE - ID:', targetId);
        this.projetService.deposerProjet(targetId, depotData).subscribe({
          next: (response: any) => {
            console.log('>>> Dépôt projet SUCCESS - réponse:', response);
            // Stocker le dépôt
            if (response.depots && response.depots.length > 0) {
              this.depotProjet = response.depots[0];
              console.log('>>> Dépôt stocké pour projet:', response.depots[0]);
            }
            this.showSuccess('Projet déposé avec succès');
            this.closeDepotPanel();
            // Mettre à jour la progression depuis le backend
            this.updateProgressionFromBackend();
            // Optionnel : rediriger après succès
            // setTimeout(() => this.router.navigate(['/admin/projets/board']), 2000);
          },
          error: (err) => {
            this.depotErrorMessage = err.error || 'Erreur lors du dépôt du projet';
          }
        });
        break;
    }
  }

  // Getters pour les employés
  getChefDeProjet(): any {
    return this.projet?.chefDeProjet;
  }

  getEmployesNonChef(): any[] {
    const chefId = this.projet?.chefDeProjet?.id;
    return this.projet?.employes?.filter(e => e.id !== chefId) || [];
  }

  // Méthodes pour télécharger et ouvrir les dépôts
  telechargerDepot(depot: any): void {
    if (!depot?.id) {
      console.error('>>> Aucun ID de dépôt fourni');
      return;
    }
    
    const url = `http://localhost:8080/api/admin/depots/${depot.id}/telecharger`;
    const token = localStorage.getItem('token') || '';
    
    console.log('>>> Téléchargement du dépôt:', depot.id, depot.nomFichier);
    
    fetch(url, {
      method: 'GET',
      headers: { 
        'Authorization': `Bearer ${token}`,
        'Accept': '*/*'
      }
    })
    .then(response => {
      if (!response.ok) {
        throw new Error(`Erreur HTTP: ${response.status}`);
      }
      return response.blob();
    })
    .then(blob => {
      const link = document.createElement('a');
      link.href = URL.createObjectURL(blob);
      link.download = depot.nomFichier || 'fichier';
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      URL.revokeObjectURL(link.href);
      console.log('>>> Téléchargement réussi:', depot.nomFichier);
    })
    .catch(err => {
      console.error('>>> Erreur téléchargement:', err);
      this.showError('Erreur lors du téléchargement du fichier');
    });
  }

  ouvrirLien(lien: string): void {
    if (!lien) {
      console.error('>>> Aucun lien fourni');
      return;
    }
    
    console.log('>>> Ouverture du lien:', lien);
    
    // Vérifier que le lien commence par http:// ou https://
    let url = lien;
    if (!lien.startsWith('http://') && !lien.startsWith('https://')) {
      url = 'https://' + lien;
    }
    
    window.open(url, '_blank', 'noopener,noreferrer');
  }

  // Méthode pour gérer le clic sur une carte de dépôt
  onDepotCardClick(depot: any, event?: MouseEvent): void {
    // Ne pas déclencher si on a cliqué sur le bouton Modifier
    if (event) {
      const target = event.target as HTMLElement;
      if (target.closest('.btn-modifier')) {
        return;
      }
    }
    
    if (!depot) return;
    
    if (depot.type === 'fichier') {
      this.telechargerDepot(depot);
    } else if (depot.type === 'lien') {
      this.ouvrirLien(depot.lien);
    }
  }

  // ============================
  // MÉTHODES DE PROGRESSION
  // ============================

  /**
   * Calculer la progression d'une activité (0-100)
   * Basée sur : nb tâches déposées / nb total tâches × 100
   */
  getProgressionActivite(activite: any): number {
    const taches = activite.taches || [];
    if (taches.length === 0) return 0;
    
    const deposees = taches.filter((t: any) =>
      this.depotParTache.has(t.id) || t.estDepose
    ).length;
    
    return Math.round((deposees / taches.length) * 100);
  }

  /**
   * Calculer la progression globale du projet (0-100)
   * Basée sur : nb activités déposées / nb total activités × 100
   */
  getProgressionProjet(): number {
    const total = this.activites?.length || 0;
    if (total === 0) return this.progressionProjet;
    
    const deposees = this.activites.filter((a: any) =>
      this.depotParActivite.has(a.id) || a.estDepose
    ).length;
    
    return Math.round((deposees / total) * 100);
  }

  /**
   * Mettre à jour la progression du projet depuis le backend
   * À appeler après chaque dépôt
   */
  updateProgressionFromBackend(): void {
    this.projetService.getById(this.projetId).subscribe({
      next: (projet) => {
        if (projet.progression !== undefined) {
          this.progressionProjet = projet.progression;
          console.log('>>> Progression mise à jour:', this.progressionProjet);
        }
      },
      error: (err) => {
        console.error('>>> Erreur récupération progression:', err);
      }
    });
  }
}
