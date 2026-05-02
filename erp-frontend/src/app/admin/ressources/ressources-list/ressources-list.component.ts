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
    return statut === 'ACTIVE' ? 'badge-success' : 'badge-danger';
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
        this.showSuccess('Ressource ' + (this.ressourceEnEdition ? 'modifiée' : 'créée') + ' avec succès');
        this.fermerModal();
        this.loadRessources();
      },
      error: () => this.showError('Erreur lors de la sauvegarde')
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
    return today > dateFin;
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
