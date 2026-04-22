import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HeureSupplementaireService } from '../../../core/services/heure-supplementaire.service';
import { HeureSupplementaire, StatutHeureSupplementaire } from '../../../core/models/heure-supplementaire.model';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { AdminSidebarComponent } from '../../shared/sidebar/sidebar.component';
import { Router } from '@angular/router';

@Component({
  selector: 'app-heures-supplementaires-list',
  standalone: true,
  imports: [CommonModule, FormsModule, ConfirmDialogComponent, LoadingSpinnerComponent, AdminSidebarComponent],
  templateUrl: './heures-supplementaires-list.component.html',
  styleUrls: ['./heures-supplementaires-list.component.css']
})
export class HeuresSupplementairesListComponent implements OnInit {
  heuresSupplementaires: HeureSupplementaire[] = [];
  filteredHeuresSupplementaires: HeureSupplementaire[] = [];
  isLoading = false;
  selectedFilter = 'Tous';
  errorMessage = '';
  successMessage = '';
  showConfirmDialog = false;
  confirmDialogData = {
    title: '',
    message: '',
    confirmText: '',
    cancelText: ''
  };
  heureSupplementaireToDelete: HeureSupplementaire | null = null;
  heureSupplementaireToApprove: HeureSupplementaire | null = null;
  heureSupplementaireToRefuse: HeureSupplementaire | null = null;
  actionType: 'delete' | 'approve' | 'refuse' | null = null;

  statutFilters = [
    { value: 'Tous', label: 'Tous' },
    { value: StatutHeureSupplementaire.EN_ATTENTE, label: 'En Attente' },
    { value: StatutHeureSupplementaire.APPROUVEE, label: 'Approuvées' },
    { value: StatutHeureSupplementaire.REFUSEE, label: 'Refusées' }
  ];

  constructor(
    private heureSupplementaireService: HeureSupplementaireService,
    private router: Router
  ) {}

  navigateToNewHeureSupplementaire(): void {
    this.router.navigate(['/admin/heures-supplementaires/new']);
  }

  ngOnInit(): void {
    this.loadHeuresSupplementaires();
  }

  loadHeuresSupplementaires(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.heureSupplementaireService.getAll().subscribe({
      next: (heuresSupplementaires) => {
        this.heuresSupplementaires = heuresSupplementaires;
        this.filteredHeuresSupplementaires = heuresSupplementaires;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading heures supplementaires:', error);
        this.errorMessage = 'Erreur lors du chargement des heures supplémentaires';
        this.isLoading = false;
      }
    });
  }

  onFilterChange(): void {
    if (this.selectedFilter === 'Tous') {
      this.filteredHeuresSupplementaires = this.heuresSupplementaires;
    } else {
      this.filteredHeuresSupplementaires = this.heuresSupplementaires.filter(
        hs => hs.statut === this.selectedFilter
      );
    }
  }

  getEmployeeName(heureSupplementaire: HeureSupplementaire): string {
    return heureSupplementaire.employeNomComplet || 'Non spécifié';
  }

  getStatutLabel(statut: StatutHeureSupplementaire): string {
    return this.heureSupplementaireService.getStatutLabel(statut);
  }

  getStatutColor(statut: StatutHeureSupplementaire): string {
    return this.heureSupplementaireService.getStatutColor(statut);
  }

  calculateTotalAmount(nombreHeures: number, tarif: number): number {
    return nombreHeures * tarif;
  }

  canApprove(heureSupplementaire: HeureSupplementaire): boolean {
    return heureSupplementaire.statut === StatutHeureSupplementaire.EN_ATTENTE;
  }

  canRefuse(heureSupplementaire: HeureSupplementaire): boolean {
    return heureSupplementaire.statut === StatutHeureSupplementaire.EN_ATTENTE;
  }

  canEdit(heureSupplementaire: HeureSupplementaire): boolean {
    return heureSupplementaire.statut === StatutHeureSupplementaire.EN_ATTENTE;
  }

  canDelete(heureSupplementaire: HeureSupplementaire): boolean {
    return true; // Admin peut supprimer n'importe quelle heure supplémentaire
  }

  editHeureSupplementaire(heureSupplementaire: HeureSupplementaire): void {
    if (heureSupplementaire.id) {
      this.router.navigate(['/admin/heures-supplementaires/edit', heureSupplementaire.id]);
    }
  }

  deleteHeureSupplementaire(heureSupplementaire: HeureSupplementaire): void {
    this.heureSupplementaireToDelete = heureSupplementaire;
    this.actionType = 'delete';
    this.confirmDialogData = {
      title: 'Supprimer l\'heure supplémentaire',
      message: `Êtes-vous sûr de vouloir supprimer l'heure supplémentaire de ${this.getEmployeeName(heureSupplementaire)} du ${heureSupplementaire.date} ?`,
      confirmText: 'Supprimer',
      cancelText: 'Annuler'
    };
    this.showConfirmDialog = true;
  }

  approveHeureSupplementaire(heureSupplementaire: HeureSupplementaire): void {
    this.heureSupplementaireToApprove = heureSupplementaire;
    this.actionType = 'approve';
    this.confirmDialogData = {
      title: 'Approuver l\'heure supplémentaire',
      message: `Êtes-vous sûr de vouloir approuver l'heure supplémentaire de ${this.getEmployeeName(heureSupplementaire)} du ${heureSupplementaire.date} ?`,
      confirmText: 'Approuver',
      cancelText: 'Annuler'
    };
    this.showConfirmDialog = true;
  }

  refuseHeureSupplementaire(heureSupplementaire: HeureSupplementaire): void {
    this.heureSupplementaireToRefuse = heureSupplementaire;
    this.actionType = 'refuse';
    this.confirmDialogData = {
      title: 'Refuser l\'heure supplémentaire',
      message: `Êtes-vous sûr de vouloir refuser l'heure supplémentaire de ${this.getEmployeeName(heureSupplementaire)} du ${heureSupplementaire.date} ?`,
      confirmText: 'Refuser',
      cancelText: 'Annuler'
    };
    this.showConfirmDialog = true;
  }

  onConfirmAction(): void {
    if (this.actionType === 'delete' && this.heureSupplementaireToDelete) {
      this.performDelete();
    } else if (this.actionType === 'approve' && this.heureSupplementaireToApprove) {
      this.performApprove();
    } else if (this.actionType === 'refuse' && this.heureSupplementaireToRefuse) {
      this.performRefuse();
    }
    this.showConfirmDialog = false;
  }

  onCancelAction(): void {
    this.showConfirmDialog = false;
    this.heureSupplementaireToDelete = null;
    this.heureSupplementaireToApprove = null;
    this.heureSupplementaireToRefuse = null;
    this.actionType = null;
  }

  private performDelete(): void {
    if (this.heureSupplementaireToDelete?.id) {
      console.log('Deleting heure supplementaire with ID:', this.heureSupplementaireToDelete.id);
      this.heureSupplementaireService.delete(this.heureSupplementaireToDelete.id).subscribe({
        next: () => {
          this.successMessage = 'Heure supplémentaire supprimée avec succès';
          this.loadHeuresSupplementaires();
          this.clearMessages();
        },
        error: (error) => {
          console.error('Error deleting heure supplementaire:', error);
          this.errorMessage = 'Erreur lors de la suppression de l\'heure supplémentaire';
        }
      });
    }
  }

  private performApprove(): void {
    if (this.heureSupplementaireToApprove?.id) {
      this.heureSupplementaireService.approuver(this.heureSupplementaireToApprove.id).subscribe({
        next: () => {
          this.successMessage = 'Heure supplémentaire approuvée avec succès';
          this.loadHeuresSupplementaires();
          this.clearMessages();
        },
        error: (error) => {
          console.error('Error approving heure supplementaire:', error);
          this.errorMessage = 'Erreur lors de l\'approbation de l\'heure supplémentaire';
        }
      });
    }
  }

  private performRefuse(): void {
    if (this.heureSupplementaireToRefuse?.id) {
      this.heureSupplementaireService.refuser(this.heureSupplementaireToRefuse.id).subscribe({
        next: () => {
          this.successMessage = 'Heure supplémentaire refusée avec succès';
          this.loadHeuresSupplementaires();
          this.clearMessages();
        },
        error: (error) => {
          console.error('Error refusing heure supplementaire:', error);
          this.errorMessage = 'Erreur lors du refus de l\'heure supplémentaire';
        }
      });
    }
  }

  clearMessages(): void {
    setTimeout(() => {
      this.errorMessage = '';
      this.successMessage = '';
    }, 3000);
  }

  getStats() {
    const total = this.heuresSupplementaires.length;
    const enAttente = this.heuresSupplementaires.filter(hs => hs.statut === StatutHeureSupplementaire.EN_ATTENTE).length;
    const approuvees = this.heuresSupplementaires.filter(hs => hs.statut === StatutHeureSupplementaire.APPROUVEE).length;
    const refusees = this.heuresSupplementaires.filter(hs => hs.statut === StatutHeureSupplementaire.REFUSEE).length;

    return { total, enAttente, approuvees, refusees };
  }
}
