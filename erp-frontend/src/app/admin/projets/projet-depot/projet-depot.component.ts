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

  // Panneau de dépôt inline
  selectedDepotTargetId: number | null = null;
  selectedDepotTargetType: 'tache' | 'activite' | 'projet' = 'tache';
  selectedTab: 'lien' | 'fichier' = 'lien';
  depotLien: string = '';
  selectedFile: File | null = null;
  depotErrorMessage: string = '';

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
    if (tache.estDepose) return;

    this.selectedDepotTargetType = 'tache';
    this.selectedDepotTargetId = tache.id;
    this.selectedTab = 'lien';
    this.depotLien = '';
    this.selectedFile = null;
    this.depotErrorMessage = '';
  }

  // Méthodes pour les activités
  canDeposerActivite(activite: ActiviteResponse): boolean {
    if (!activite.taches || activite.taches.length === 0) return false;
    return activite.taches.every(t => t.estDepose);
  }

  deposerActivite(activite: ActiviteResponse): void {
    if (!this.canDeposerActivite(activite) || activite.estDepose) return;

    this.selectedDepotTargetType = 'activite';
    this.selectedDepotTargetId = activite.id;
    this.selectedTab = 'lien';
    this.depotLien = '';
    this.selectedFile = null;
    this.depotErrorMessage = '';
  }

  // Méthodes pour le projet
  canDeposerProjet(): boolean {
    if (!this.activites || this.activites.length === 0) return false;
    return this.activites.every(a => a.estDepose);
  }

  deposerProjet(): void {
    if (!this.canDeposerProjet() || !this.projet || this.projet.estDepose) return;

    this.selectedDepotTargetType = 'projet';
    this.selectedDepotTargetId = this.projet.id;
    this.selectedTab = 'lien';
    this.depotLien = '';
    this.selectedFile = null;
    this.depotErrorMessage = '';
  }

  // Compteurs
  getTachesDeposeesCount(activite: ActiviteResponse): number {
    return activite.taches?.filter(t => t.estDepose).length || 0;
  }

  getTachesTotalCount(activite: ActiviteResponse): number {
    return activite.taches?.length || 0;
  }

  getActivitesDeposeesCount(): number {
    return this.activites.filter(a => a.estDepose).length;
  }

  getActivitesTotalCount(): number {
    return this.activites.length;
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
    this.selectedDepotTargetId = null;
    this.selectedTab = 'lien';
    this.depotLien = '';
    this.selectedFile = null;
    this.depotErrorMessage = '';
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
    if (!this.selectedDepotTargetId) return;

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

    switch (this.selectedDepotTargetType) {
      case 'tache':
        this.tacheService.deposerTache(this.selectedDepotTargetId, depotData).subscribe({
          next: () => {
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
        this.activiteService.deposerActivite(this.selectedDepotTargetId, depotData).subscribe({
          next: () => {
            this.showSuccess('Activité déposée avec succès');
            this.closeDepotPanel();
            this.loadActivites();
          },
          error: (err) => {
            this.depotErrorMessage = 'Erreur lors du dépôt de l\'activité';
          }
        });
        break;
      case 'projet':
        this.projetService.deposerProjet(this.selectedDepotTargetId, depotData).subscribe({
          next: () => {
            this.showSuccess('Projet déposé avec succès');
            this.closeDepotPanel();
            setTimeout(() => this.router.navigate(['/admin/projets/board']), 2000);
          },
          error: (err) => {
            this.depotErrorMessage = 'Erreur lors du dépôt du projet';
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
}
