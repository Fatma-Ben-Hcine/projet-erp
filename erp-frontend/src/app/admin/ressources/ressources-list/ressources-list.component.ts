import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AdminRessourceService } from '../../../core/services/admin-ressource.service';
import { Ressource } from '../../../core/models/ressource.model';

@Component({
  selector: 'app-ressources-list',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './ressources-list.component.html',
  styleUrls: ['./ressources-list.component.scss']
})
export class RessourcesListComponent implements OnInit {
  ressources: Ressource[] = [];
  filteredRessources: Ressource[] = [];
  loading = false;
  error: string | null = null;
  showModal = false;
  ressourceEnEdition: any = null;
  ressourceForm = {
    nom: '',
    description: '',
    prix: null,
    dateDebutAbonnement: '',
    dateFinAbonnement: ''
  };
  currentFilter = 'all';
  searchTerm = '';
  notification: { type: 'success' | 'error', message: string } | null = null;
  private notificationTimeout: any;

  constructor(private adminRessourceService: AdminRessourceService) {}

  ngOnInit(): void {
    this.loadRessources();
  }

  loadRessources(): void {
    this.loading = true;
    this.adminRessourceService.getAll().subscribe({
      next: (ressources: Ressource[]) => {
        this.ressources = ressources;
        // Mark expired abonnements for UI only (do not change backend statut here)
        this.markExpiredForDisplay(this.ressources);
        this.filteredRessources = ressources;
        this.loading = false;
      },
      error: (err: any) => {
        this.error = 'Erreur lors du chargement des ressources';
        this.loading = false;
        console.error(err);
      }
    });
  }

  filterRessources(searchTerm: string): void {
    this.searchTerm = searchTerm.toLowerCase();
    this.applyFilters();
  }

  setFilter(filter: string): void {
    this.currentFilter = filter;
    this.applyFilters();
  }

  applyFilters(): void {
    let filtered = [...this.ressources];

    // Appliquer le filtre de recherche
    if (this.searchTerm) {
      filtered = filtered.filter(r => 
        r.nom.toLowerCase().includes(this.searchTerm) ||
        (r.description && r.description.toLowerCase().includes(this.searchTerm))
      );
    }

    // Appliquer le filtre de statut/situation
    switch (this.currentFilter) {
      case 'actives':
        filtered = filtered.filter(r => r.statut === 'ACTIVE');
        break;
      case 'non-actifs':
        filtered = filtered.filter(r => r.statut === 'NON_ACTIVE');
        break;
      // 'all' ne filtre rien
    }

    this.filteredRessources = filtered;
  }

  toggleStatut(id: number, event: any): void {
    const newStatut = event.target.checked ? 'ACTIVE' : 'NON_ACTIVE';
    this.changerStatut(id, newStatut);
  }

  getStatutClass(statut: string): string {
    const s = this.getStatutValue({ statut });
    return s === 'ACTIVE' ? 'badge-success' : 'badge-danger';
  }

  /**
   * Normalize statut value returned from backend which may be a string or an object { name: 'ACTIVE' }
   */
  getStatutValue(ressource: any): string {
    if (!ressource) return 'NON_ACTIVE';
    const st = ressource.statut;
    if (!st) return 'NON_ACTIVE';
    if (typeof st === 'string') return st;
    if (typeof st === 'object' && st.name) return st.name;
    return String(st);
  }

  
  ouvrirModalCreation(): void {
    this.ressourceEnEdition = null;
    this.ressourceForm = {
      nom: '',
      description: '',
      prix: null,
      dateDebutAbonnement: '',
      dateFinAbonnement: ''
    };
    this.showModal = true;
  }

  ouvrirModification(ressource: any): void {
    this.ressourceEnEdition = ressource;
    this.ressourceForm = {
      nom: ressource.nom,
      description: ressource.description || '',
      prix: ressource.prix || null,
      dateDebutAbonnement: ressource.dateDebutAbonnement || '',
      dateFinAbonnement: ressource.dateFinAbonnement || ''
    };
    this.showModal = true;
  }

  fermerModal(): void {
    this.showModal = false;
    this.ressourceEnEdition = null;
  }

  enregistrerRessource(): void {
    if (!this.ressourceForm.nom) {
      alert('Le nom est obligatoire');
      return;
    }

    const observable = this.ressourceEnEdition 
      ? this.adminRessourceService.update(this.ressourceEnEdition.id, this.ressourceForm)
      : this.adminRessourceService.create(this.ressourceForm);

    observable.subscribe({
      next: () => {
        // Après la sauvegarde, vérifier si la date de fin est future -> activer la ressource
        const dateFin = this.ressourceForm.dateFinAbonnement;
        const nom = this.ressourceForm.nom;
        if (dateFin) {
          this.ensureActivationIfDateValid(nom, dateFin);
        } else {
          this.showSuccess('Ressource ' + (this.ressourceEnEdition ? 'modifiée' : 'créée') + ' avec succès');
          this.fermerModal();
          this.loadRessources();
        }
      },
      error: () => this.showError('Erreur lors de la sauvegarde')
    });
  }

  /**
   * Trouve la ressource créée/éditée (by name+dateFin) et l'active si la dateFin est dans le futur.
   * Rafraîchit ensuite la liste.
   */
  private ensureActivationIfDateValid(nom: string, dateFinStr: string): void {
    this.adminRessourceService.getAll().subscribe({
      next: (ressources: Ressource[]) => {
        // Chercher correspondance probable
        const match = ressources.find(r => r.nom === nom && (r.dateFinAbonnement || '') === dateFinStr);
        if (match && match.dateFinAbonnement) {
          const fin = new Date(match.dateFinAbonnement);
          const today = new Date();
          const todayOnly = new Date(today.getFullYear(), today.getMonth(), today.getDate());
          const finOnly = new Date(fin.getFullYear(), fin.getMonth(), fin.getDate());
          if (finOnly > todayOnly && match.statut !== 'ACTIVE') {
            this.adminRessourceService.changerStatut(match.id, 'ACTIVE').subscribe({
              next: () => {
                this.showSuccess('Ressource activée car date d\'abonnement valide');
                this.fermerModal();
                this.loadRessources();
              },
              error: (err) => {
                console.error('Erreur activation automatique', err);
                this.showError('La ressource a été sauvegardée mais impossible de l\'activer automatiquement');
                this.fermerModal();
                this.loadRessources();
              }
            });
            return;
          }
        }
        // Si pas de correspondance ou pas besoin d'activer
        this.showSuccess('Ressource ' + (this.ressourceEnEdition ? 'modifiée' : 'créée') + ' avec succès');
        this.fermerModal();
        this.loadRessources();
      },
      error: (err) => {
        console.error('Erreur récupération ressources après sauvegarde', err);
        this.showSuccess('Ressource sauvegardée');
        this.fermerModal();
        this.loadRessources();
      }
    });
  }

  supprimerRessource(id: number): void {
    let message = 'Êtes-vous sûr de vouloir supprimer cette ressource ?';
    
    if (confirm(message)) {
      this.adminRessourceService.delete(id).subscribe({
        next: () => {
          this.showSuccess('Ressource supprimée avec succès');
          this.loadRessources();
        },
        error: () => this.showError('Erreur lors de la suppression')
      });
    }
  }

  changerStatut(id: number, nouveauStatut: string): void {
    this.adminRessourceService.changerStatut(id, nouveauStatut).subscribe({
      next: () => {
        // MISE À JOUR LOCALE IMMÉDIATE (sans recharger toute la liste)
        const ressource = this.ressources.find(r => r.id === id);
        if (ressource) {
          ressource.statut = nouveauStatut as 'ACTIVE' | 'NON_ACTIVE';
        }
        
        // Mettre aussi à jour dans la liste filtrée
        const ressourceFiltree = this.filteredRessources.find(r => r.id === id);
        if (ressourceFiltree) {
          ressourceFiltree.statut = nouveauStatut as 'ACTIVE' | 'NON_ACTIVE';
        }
        
        this.showSuccess('Statut modifié');
      },
      error: () => this.showError('Erreur changement statut')
    });
  }

  
  formatDate(dateStr: string): string {
    if (!dateStr) return '-';
    return new Date(dateStr).toLocaleDateString('fr-FR');
  }

  // Calcul stats depuis la liste déjà chargée
  get totalRessources(): number {
    return this.ressources.length;
  }

  get nombreActives(): number {
    return this.ressources
      .filter(r => r.statut === 'ACTIVE').length;
  }

  
  private showSuccess(message: string): void {
    this.notification = { type: 'success', message };
    if (this.notificationTimeout) {
      clearTimeout(this.notificationTimeout);
    }
    this.notificationTimeout = setTimeout(() => {
      this.clearNotification();
    }, 3000);
  }

  private showError(message: string): void {
    this.notification = { type: 'error', message };
    if (this.notificationTimeout) {
      clearTimeout(this.notificationTimeout);
    }
    this.notificationTimeout = setTimeout(() => {
      this.clearNotification();
    }, 5000);
  }

  clearNotification(): void {
    this.notification = null;
    if (this.notificationTimeout) {
      clearTimeout(this.notificationTimeout);
    }
  }

  // Méthodes pour les nouvelles colonnes
  
  getAbonnementClass(ressource: any): string {
    if (!ressource.dateDebutAbonnement || !ressource.dateFinAbonnement) {
      return 'badge-gray'; // Non abonné
    }
    if (this.isAbonnementExpire(ressource)) {
      return 'badge-red'; // Expiré
    }
    return 'badge-green'; // Abonné actif
  }

  getAbonnementLabel(ressource: any): string {
    if (!ressource.dateDebutAbonnement || !ressource.dateFinAbonnement) {
      return 'Non abonné';
    }
    if (this.isAbonnementExpire(ressource)) {
      return 'Expiré';
    }
    return 'Abonné';
  }

  isAbonnementExpire(ressource: any): boolean {
    if (!ressource.dateFinAbonnement) return false;
    const dateFin = new Date(ressource.dateFinAbonnement);
    const today = new Date();
    // Expired when end date is earlier than or equal to today
    // Compare dates only (ignore time)
    const y1 = today.getFullYear(), m1 = today.getMonth(), d1 = today.getDate();
    const y2 = dateFin.getFullYear(), m2 = dateFin.getMonth(), d2 = dateFin.getDate();
    const todayOnly = new Date(y1, m1, d1);
    const dateFinOnly = new Date(y2, m2, d2);
    return todayOnly >= dateFinOnly;
  }

  private markExpiredForDisplay(ressources: Ressource[]): void {
    const today = new Date();
    ressources.forEach(r => {
      (r as any).isExpired = false;
      if (r.dateFinAbonnement) {
        const fin = new Date(r.dateFinAbonnement);
        const todayOnly = new Date(today.getFullYear(), today.getMonth(), today.getDate());
        const finOnly = new Date(fin.getFullYear(), fin.getMonth(), fin.getDate());
        if (todayOnly >= finOnly) {
          (r as any).isExpired = true;
        }
      }
    });
  }

  getStatutLabel(ressource: any): string {
    if (this.isAbonnementExpire(ressource)) {
      return 'Expiré';
    }
    return ressource.statut;
  }

  getSituationLabel(situation: string): string {
    switch (situation) {
      case 'NON_DEMANDE':
        return 'Non demandé';
      case 'DEMANDE':
        return 'Demandé';
      default:
        return situation;
    }
  }
}
