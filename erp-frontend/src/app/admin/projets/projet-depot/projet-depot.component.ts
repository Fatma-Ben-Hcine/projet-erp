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

import { environment } from '../../../../environments/environment';

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
  
  // Propriétés calculées pour éviter les appels de fonctions dans le template
  activitesStats: Map<number, { deposees: number; total: number; toutesDeposees: boolean }> = new Map();
  projetStats: { activitesDeposees: number; activitesTotal: number; toutesDeposees: boolean } = { activitesDeposees: 0, activitesTotal: 0, toutesDeposees: false };
  progressionProjetCalculee: number = 0;
  
  // Mode modification
  modeModification: boolean = false;

  // Modals CRUD Activités et Tâches
  showActiviteModal: boolean = false;
  showTacheModal: boolean = false;
  activiteEnEdition: ActiviteResponse | null = null;
  tacheEnEdition: TacheResponse | null = null;
  activiteParenteId: number | null = null;
  
  // Formulaires
  activiteForm: any = {
    nom: '',
    description: '',
    dateDebut: '',
    dateFin: '',
    employeIds: []
  };
  
  tacheForm: any = {
    nom: '',
    description: '',
    dateDebut: '',
    dateFin: '',
    employeIds: []
  };

  // Employés disponibles pour une activité (filtrés)
  employesActivite: Array<{ id: number; nom: string; prenom: string; progression: number }> = [];

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
        }
        
        // Charger la progression depuis le backend
        if (projet.progression !== undefined) {
          this.progressionProjet = projet.progression;
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
    this.activiteService.getByProjet(this.projetId).subscribe({
      next: (activites) => {
        this.activites = activites;
        
        // Réinitialiser les Maps pour forcer le rechargement depuis la base
        this.depotParTache = new Map<number, any>();
        this.depotParActivite = new Map<number, any>();
        
        // Remplir les Maps avec les dépôts reçus du backend
        activites.forEach((activite: any) => {
          // Dépôts des TÂCHES
          activite.taches?.forEach((tache: any) => {
            if (tache.depots && tache.depots.length > 0) {
              const depotTache = tache.depots.find((d: any) => {
                const idTache = d.tacheId ?? d.tache_id ?? d.tache?.id;
                return idTache === tache.id || idTache === undefined;
              });
              
              if (depotTache) {
                this.depotParTache.set(tache.id, depotTache);
              }
            }
          });
          
          // Dépôt activité
          const depotActivite = activite.depots?.find((d: any) => {
            const idTache = d.tacheId ?? d.tache_id ?? d.tache?.id;
            return idTache === null || idTache === undefined;
          });
          
          if (depotActivite) {
            this.depotParActivite.set(activite.id, depotActivite);
          }
        });
        
        // Calculer les statistiques pour le template
        this.updateComputedStats();
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
    const depot = this.depotParTache.get(tache.id);
    if (!depot) return;
    
    // Fermer les autres panneaux et ouvrir celui de la tâche en mode modification
    this.selectedActiviteId = null;
    this.selectedProjetDepot = false;
    this.selectedTacheId = tache.id;
    this.modeModification = true;
    
    // Pré-remplir le formulaire avec les données existantes
    this.selectedTab = depot.type === 'fichier' ? 'fichier' : 'lien';
    this.depotLien = depot.lien || '';
    this.selectedFile = null;
    this.depotErrorMessage = '';
  }

  // ============================
  // MÉTHODES DE CALCUL DES STATS
  // ============================

  /**
   * Calculer et mettre à jour toutes les statistiques pour le template
   * Appelé uniquement après loadActivites() ou après une action CRUD
   */
  updateComputedStats(): void {
    const newStats = new Map<number, { deposees: number; total: number; toutesDeposees: boolean }>();
    
    this.activites.forEach((activite: any) => {
      const total = activite.taches?.length || 0;
      const deposees = activite.taches?.filter((t: any) => this.depotParTache.has(t.id)).length || 0;
      const toutesDeposees = total > 0 && deposees === total;
      newStats.set(activite.id, { deposees, total, toutesDeposees });
    });
    
    this.activitesStats = newStats;
    
    // Stats projet
    const activitesTotal = this.activites.length;
    const activitesDeposees = this.activites.filter((a: any) => this.depotParActivite.has(a.id)).length;
    const toutesDeposees = activitesTotal > 0 && activitesDeposees === activitesTotal;
    this.projetStats = { activitesDeposees, activitesTotal, toutesDeposees };
    
    // Progression projet
    if (activitesTotal === 0) {
      this.progressionProjetCalculee = this.progressionProjet;
    } else {
      const deposees = this.activites.filter((a: any) => this.depotParActivite.has(a.id) || a.estDepose).length;
      this.progressionProjetCalculee = Math.round((deposees / activitesTotal) * 100);
    }
  }

  /**
   * Obtenir les stats d'une activité (propriété pré-calculée)
   */
  getActiviteStats(activiteId: number): { deposees: number; total: number; toutesDeposees: boolean } {
    return this.activitesStats.get(activiteId) || { deposees: 0, total: 0, toutesDeposees: false };
  }

  canDeposerActivite(activite: ActiviteResponse): boolean {
    return this.getActiviteStats(activite.id).toutesDeposees;
  }

  deposerActivite(activite: ActiviteResponse): void {
    if (!this.getActiviteStats(activite.id).toutesDeposees) return;

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
    const depot = this.depotParActivite.get(activite.id);
    if (!depot) return;
    
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
  }

  // Méthodes pour le projet — basées sur les propriétés calculées
  canDeposerProjet(): boolean {
    return this.projetStats.toutesDeposees;
  }

  deposerProjet(): void {
    if (!this.projetStats.toutesDeposees || !this.projet) return;

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
    if (!this.depotProjet) return;
    
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
    
    switch (targetType) {
      case 'tache':
        this.tacheService.deposerTache(targetId, depotData).subscribe({
          next: (response: any) => {
            let depot = null;
            if (response.depots && response.depots.length > 0) {
              depot = response.depots[response.depots.length - 1];
            } else if (response.id && response.type) {
              depot = response;
            }
            if (depot) {
              this.depotParTache.set(targetId, depot);
            }
            this.showSuccess('Tâche déposée avec succès');
            this.closeDepotPanel();
            this.loadActivites();
          },
          error: (err) => {
            this.depotErrorMessage = err.error || 'Erreur lors du dépôt de la tâche';
          }
        });
        break;
      case 'activite':
        this.activiteService.deposerActivite(targetId, depotData).subscribe({
          next: (response: any) => {
            let depot = null;
            if (response.depots && response.depots.length > 0) {
              depot = response.depots[response.depots.length - 1];
            } else if (response.id && response.type) {
              depot = response;
            }
            if (depot) {
              this.depotParActivite.set(targetId, depot);
            }
            this.showSuccess('Activité déposée avec succès');
            this.closeDepotPanel();
            this.loadActivites();
            this.updateProgressionFromBackend();
          },
          error: (err) => {
            this.depotErrorMessage = err.error || 'Erreur lors du dépôt de l\'activité';
          }
        });
        break;
      case 'projet':
        this.projetService.deposerProjet(targetId, depotData).subscribe({
          next: (response: any) => {
            if (response.depots && response.depots.length > 0) {
              this.depotProjet = response.depots[0];
            }
            this.showSuccess('Projet déposé avec succès');
            this.closeDepotPanel();
            this.updateProgressionFromBackend();
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
    if (!depot?.id) return;
    
    const url = environment.apiUrl + `/admin/depots/${depot.id}/telecharger`;
    const token = localStorage.getItem('token') || '';
    
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
    })
    .catch(err => {
      this.showError('Erreur lors du téléchargement du fichier');
    });
  }

  ouvrirLien(lien: string): void {
    if (!lien) return;
    
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
   * Mettre à jour la progression du projet depuis le backend
   */
  updateProgressionFromBackend(): void {
    this.projetService.getById(this.projetId).subscribe({
      next: (projet) => {
        if (projet.progression !== undefined) {
          this.progressionProjet = projet.progression;
        }
      },
      error: () => {}
    });
  }

  // ============================
  // MÉTHODES CRUD ACTIVITÉS
  // ============================

  /**
   * Ouvrir le modal de création d'activité
   */
  ouvrirModalCreerActivite(): void {
    this.activiteEnEdition = null;
    this.activiteForm = {
      nom: '',
      description: '',
      dateDebut: '',
      dateFin: '',
      employeIds: []
    };
    this.showActiviteModal = true;
  }

  /**
   * Ouvrir le modal de modification d'activité
   */
  ouvrirModalModifierActivite(activite: ActiviteResponse): void {
    // Vérifier si l'activité est déjà déposée
    if (this.depotParActivite.has(activite.id)) {
      this.showError('Cette activité est déjà déposée et ne peut plus être modifiée');
      return;
    }

    this.activiteEnEdition = activite;
    this.activiteForm = {
      nom: activite.nom,
      description: activite.description,
      dateDebut: activite.dateDebut,
      dateFin: activite.dateFin,
      employeIds: activite.employeActivites?.map((ea: any) => ea.employeId) || []
    };
    this.showActiviteModal = true;
  }

  /**
   * Fermer le modal d'activité
   */
  fermerModalActivite(): void {
    this.showActiviteModal = false;
    this.activiteEnEdition = null;
    this.activiteForm = {
      nom: '',
      description: '',
      dateDebut: '',
      dateFin: '',
      employeIds: []
    };
  }

  /**
   * Sauvegarder l'activité (création ou modification)
   */
  sauvegarderActivite(): void {
    if (!this.activiteForm.nom || !this.activiteForm.dateDebut) {
      this.showError('Veuillez remplir les champs obligatoires (nom et date de début)');
      return;
    }

    const activiteData = {
      nom: this.activiteForm.nom,
      description: this.activiteForm.description || '',
      dateDebut: this.activiteForm.dateDebut,
      dateFin: this.activiteForm.dateFin || null,
      projetId: this.projetId,
      employeIds: this.activiteForm.employeIds
    };

    if (this.activiteEnEdition) {
      // Modification
      this.activiteService.update(this.activiteEnEdition.id, activiteData).subscribe({
        next: () => {
          this.showSuccess('Activité modifiée avec succès');
          this.fermerModalActivite();
          this.loadActivites();
        },
        error: (err) => {
          const msg = err.error?.message || err.error || 'Erreur lors de la modification de l\'activité';
          this.showError(typeof msg === 'string' ? msg : 'Erreur lors de la modification de l\'activité');
        }
      });
    } else {
      // Création
      this.activiteService.create(activiteData).subscribe({
        next: () => {
          this.showSuccess('Activité créée avec succès');
          this.fermerModalActivite();
          this.loadActivites();
        },
        error: (err) => {
          const msg = err.error?.message || err.error || 'Erreur lors de la création de l\'activité';
          this.showError(typeof msg === 'string' ? msg : 'Erreur lors de la création de l\'activité');
        }
      });
    }
  }

  /**
   * Supprimer une activité avec confirmation
   */
  supprimerActivite(activite: ActiviteResponse): void {
    // Vérifier si l'activité est déjà déposée
    if (this.depotParActivite.has(activite.id)) {
      this.showError('Cette activité est déjà déposée et ne peut plus être supprimée');
      return;
    }

    if (confirm(`Êtes-vous sûr de vouloir supprimer l'activité "${activite.nom}" ? Cette action supprimera également toutes les tâches associées.`)) {
      this.activiteService.delete(activite.id).subscribe({
        next: () => {
          this.showSuccess('Activité supprimée avec succès');
          this.loadActivites();
        },
        error: (err) => {
          this.showError('Erreur lors de la suppression de l\'activité');
        }
      });
    }
  }

  // ============================
  // MÉTHODES CRUD TÂCHES
  // ============================

  /**
   * Ouvrir le modal de création de tâche
   */
  ouvrirModalCreerTache(activiteId: number): void {
    this.tacheEnEdition = null;
    this.activiteParenteId = activiteId;
    this.tacheForm = {
      nom: '',
      description: '',
      dateDebut: '',
      dateFin: '',
      employeIds: []
    };
    
    // Charger les employés de l'activité
    this.activiteService.getEmployesByActiviteId(activiteId).subscribe({
      next: (employes) => {
        this.employesActivite = employes;
        this.showTacheModal = true;
      },
      error: () => {
        this.employesActivite = [];
        this.showTacheModal = true;
      }
    });
  }

  /**
   * Ouvrir le modal de modification de tâche
   */
  ouvrirModalModifierTache(tache: TacheResponse): void {
    // Vérifier si la tâche est déjà déposée
    if (this.depotParTache.has(tache.id)) {
      this.showError('Cette tâche est déjà déposée et ne peut plus être modifiée');
      return;
    }

    if (!tache.activite || !tache.activite.id) {
      this.showError('Erreur: information d\'activité manquante pour cette tâche');
      return;
    }

    this.tacheEnEdition = tache;
    this.activiteParenteId = tache.activite.id;
    this.tacheForm = {
      nom: tache.nom,
      description: tache.description,
      dateDebut: tache.dateDebut,
      dateFin: tache.dateFin,
      employeIds: tache.employeTaches?.map((et: any) => et.employeId) || []
    };
    
    // Charger les employés de l'activité
    this.activiteService.getEmployesByActiviteId(tache.activite.id).subscribe({
      next: (employes) => {
        this.employesActivite = employes;
        this.showTacheModal = true;
      },
      error: () => {
        this.employesActivite = [];
        this.showTacheModal = true;
      }
    });
  }

  /**
   * Fermer le modal de tâche
   */
  fermerModalTache(): void {
    this.showTacheModal = false;
    this.tacheEnEdition = null;
    this.activiteParenteId = null;
    this.tacheForm = {
      nom: '',
      description: '',
      dateDebut: '',
      dateFin: '',
      employeIds: []
    };
    this.employesActivite = [];
  }

  /**
   * Récupérer la date de début de l'activité parente
   */
  getActiviteParenteDateDebut(): string {
    if (!this.activiteParenteId) return '';
    const activite = this.activites.find(a => a.id === this.activiteParenteId);
    return activite?.dateDebut || '';
  }

  /**
   * Récupérer la date de fin de l'activité parente
   */
  getActiviteParenteDateFin(): string {
    if (!this.activiteParenteId) return '';
    const activite = this.activites.find(a => a.id === this.activiteParenteId);
    return activite?.dateFin || '';
  }

  /**
   * Récupérer la date de début du projet
   */
  getProjetDateDebut(): string {
    return this.projet?.dateDebut || '';
  }

  /**
   * Récupérer la date limite du projet
   */
  getProjetDateLimite(): string {
    return this.projet?.dateLimite || '';
  }

  /**
   * Sauvegarder la tâche (création ou modification)
   */
  sauvegarderTache(): void {
    if (!this.tacheForm.nom || !this.tacheForm.dateDebut || !this.activiteParenteId) {
      this.showError('Veuillez remplir les champs obligatoires (nom et date de début)');
      return;
    }

    // Convertir les dates vides en null (le backend attend LocalDate ou null, pas "")
    const dateFin = this.tacheForm.dateFin ? this.tacheForm.dateFin : null;

    const tacheData = {
      nom: this.tacheForm.nom,
      description: this.tacheForm.description || '',
      dateDebut: this.tacheForm.dateDebut,
      dateFin: dateFin,
      activiteId: this.activiteParenteId,
      employeIds: this.tacheForm.employeIds
    };

    if (this.tacheEnEdition) {
      // Modification
      this.tacheService.update(this.tacheEnEdition.id, tacheData).subscribe({
        next: () => {
          this.showSuccess('Tâche modifiée avec succès');
          this.fermerModalTache();
          this.loadActivites();
        },
        error: (err) => {
          const msg = err.error?.message || err.error || 'Erreur lors de la modification de la tâche';
          this.showError(typeof msg === 'string' ? msg : 'Erreur lors de la modification de la tâche');
        }
      });
    } else {
      // Création
      this.tacheService.create(tacheData).subscribe({
        next: () => {
          this.showSuccess('Tâche créée avec succès');
          this.fermerModalTache();
          this.loadActivites();
        },
        error: (err) => {
          const msg = err.error?.message || err.error || 'Erreur lors de la création de la tâche';
          this.showError(typeof msg === 'string' ? msg : 'Erreur lors de la création de la tâche');
        }
      });
    }
  }

  /**
   * Supprimer une tâche avec confirmation
   */
  supprimerTache(tache: TacheResponse): void {
    // Vérifier si la tâche est déjà déposée
    if (this.depotParTache.has(tache.id)) {
      this.showError('Cette tâche est déjà déposée et ne peut plus être supprimée');
      return;
    }

    if (confirm(`Êtes-vous sûr de vouloir supprimer la tâche "${tache.nom}" ?`)) {
      this.tacheService.delete(tache.id).subscribe({
        next: () => {
          this.showSuccess('Tâche supprimée avec succès');
          this.loadActivites();
        },
        error: (err) => {
          this.showError('Erreur lors de la suppression de la tâche');
        }
      });
    }
  }

  /**
   * Basculer la sélection d'un employé pour le formulaire d'activité
   */
  toggleEmployeActivite(employeId: number): void {
    const index = this.activiteForm.employeIds.indexOf(employeId);
    if (index > -1) {
      this.activiteForm.employeIds.splice(index, 1);
    } else {
      this.activiteForm.employeIds.push(employeId);
    }
  }

  /**
   * Basculer la sélection d'un employé pour le formulaire de tâche
   */
  toggleEmployeTache(employeId: number): void {
    const index = this.tacheForm.employeIds.indexOf(employeId);
    if (index > -1) {
      this.tacheForm.employeIds.splice(index, 1);
    } else {
      this.tacheForm.employeIds.push(employeId);
    }
  }

  /**
   * Vérifier si un employé est sélectionné pour l'activité
   */
  isEmployeSelectedForActivite(employeId: number): boolean {
    return this.activiteForm.employeIds.includes(employeId);
  }

  /**
   * Vérifier si un employé est sélectionné pour la tâche
   */
  isEmployeSelectedForTache(employeId: number): boolean {
    return this.tacheForm.employeIds.includes(employeId);
  }
}
