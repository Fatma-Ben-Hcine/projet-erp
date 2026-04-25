import { Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { ProjetService } from '../../../core/services/projet.service';
import { ActiviteService } from '../../../core/services/activite.service';
import { TacheService } from '../../../core/services/tache.service';
import { ProjetResponse } from '../../../core/models/projet.model';
import { ActiviteResponse } from '../../../core/models/activite.model';
import { TacheResponse } from '../../../core/models/activite.model';
import { AdminSidebarComponent } from '../../shared/sidebar/sidebar.component';
import { DepotModalComponent } from '../depot-modal/depot-modal.component';

@Component({
  selector: 'app-projet-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, AdminSidebarComponent, DepotModalComponent],
  templateUrl: './projet-detail.component.html',
  styleUrl: './projet-detail.component.css'
})
export class ProjetDetailComponent implements OnInit {
  projet: ProjetResponse | null = null;
  activites: ActiviteResponse[] = [];
  taches: TacheResponse[] = [];
  isLoading = false;
  errorMessage = '';

  // Modal de dépôt
  showDepotModal = false;
  depotTarget: { type: 'projet' | 'activite' | 'tache', id: number, name: string } | null = null;
  depotModalMode: 'create' | 'view' = 'create';
  depotModalDepots: any[] = [];
  @ViewChild(DepotModalComponent) depotModal!: DepotModalComponent;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private projetService: ProjetService,
    private activiteService: ActiviteService,
    private tacheService: TacheService
  ) {}

  ngOnInit(): void {
    const projetId = this.route.snapshot.paramMap.get('id');
    if (projetId) {
      this.loadProjetDetail(+projetId);
    } else {
      this.errorMessage = 'ID de projet non fourni';
    }
  }

  loadProjetDetail(projetId: number): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.projetService.getById(projetId).subscribe({
      next: (data) => {
        this.projet = data;
        this.loadActivites(projetId);
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Erreur lors du chargement du projet';
        this.isLoading = false;
      }
    });
  }

  loadActivites(projetId: number): void {
    this.activiteService.getByProjet(projetId).subscribe({
      next: (data) => {
        this.activites = data;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Erreur lors du chargement des activités:', err);
        this.isLoading = false;
      }
    });
  }

  loadTachesForActivite(activiteId: number): void {
    this.tacheService.getByActivite(activiteId).subscribe({
      next: (data) => {
        this.taches = data;
      },
      error: (err) => {
        console.error('Erreur lors du chargement des tâches:', err);
      }
    });
  }

  openDepotModalForProjet(): void {
    if (!this.projet) return;
    this.depotTarget = { type: 'projet', id: this.projet.id, name: this.projet.nom };
    this.depotModalMode = this.projet.statut === 'TERMINE' ? 'view' : 'create';
    this.depotModalDepots = this.projet.depots || [];
    this.showDepotModal = true;
  }

  openDepotModalForActivite(activite: ActiviteResponse): void {
    this.depotTarget = { type: 'activite', id: activite.id, name: activite.nom };
    this.depotModalMode = activite.estDepose ? 'view' : 'create';
    this.depotModalDepots = activite.depots || [];
    this.showDepotModal = true;
  }

  openDepotModalForTache(tache: TacheResponse): void {
    this.depotTarget = { type: 'tache', id: tache.id, name: tache.nom };
    this.depotModalMode = tache.estDepose ? 'view' : 'create';
    this.depotModalDepots = tache.depots || [];
    this.showDepotModal = true;
  }

  get employesNoms(): string {
    return this.projet?.employes
      ?.map(e => e.nom + ' ' + e.prenom)
      .join(', ') ?? '';
  }

  closeDepotModal(): void {
    this.showDepotModal = false;
    this.depotTarget = null;
  }

  onDepotSubmitted(): void {
    const depotData = this.depotModal?.depotData;
    if (!depotData || !this.depotTarget) return;

    if (this.depotTarget.type === 'projet' && this.projet) {
      this.projetService.deposerProjet(this.projet.id, depotData).subscribe({
        next: () => {
          this.closeDepotModal();
          this.loadProjetDetail(this.projet!.id);
        },
        error: (err) => {
          console.error('Erreur lors du dépôt du projet:', err);
          this.errorMessage = err.error?.message || 'Erreur lors du dépôt du projet';
        }
      });
    } else if (this.depotTarget.type === 'activite') {
      this.activiteService.deposerActivite(this.depotTarget.id, depotData).subscribe({
        next: () => {
          this.closeDepotModal();
          this.loadActivites(this.projet!.id);
        },
        error: (err) => {
          console.error('Erreur lors du dépôt de l\'activité:', err);
          this.errorMessage = err.error?.message || 'Erreur lors du dépôt de l\'activité';
        }
      });
    } else if (this.depotTarget.type === 'tache') {
      this.tacheService.deposerTache(this.depotTarget.id, depotData).subscribe({
        next: () => {
          this.closeDepotModal();
          this.loadActivites(this.projet!.id);
        },
        error: (err) => {
          console.error('Erreur lors du dépôt de la tâche:', err);
          this.errorMessage = err.error?.message || 'Erreur lors du dépôt de la tâche';
        }
      });
    }
  }

  goBack(): void {
    this.router.navigate(['/admin/projets/board']);
  }

  formatDate(dateStr: string): string {
    const date = new Date(dateStr);
    return date.toLocaleDateString('fr-FR', { day: '2-digit', month: '2-digit', year: 'numeric' });
  }
}
