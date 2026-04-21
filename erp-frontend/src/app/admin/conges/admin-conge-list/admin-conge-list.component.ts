import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CongeService } from '../../../core/services/conge.service';
import { Conge, StatutConge, TypeConge } from '../../../core/models/conge.model';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { AdminSidebarComponent } from '../../shared/sidebar/sidebar.component';

@Component({
  selector: 'app-admin-conge-list',
  standalone: true,
  imports: [CommonModule, FormsModule, ConfirmDialogComponent, LoadingSpinnerComponent, AdminSidebarComponent],
  templateUrl: './admin-conge-list.component.html',
  styleUrls: ['./admin-conge-list.component.css']
})
export class AdminCongeListComponent implements OnInit {
  conges: Conge[] = [];
  filteredConges: Conge[] = [];
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
  congeToValidate: Conge | null = null;
  congeToRefuse: Conge | null = null;
  actionType: 'validate' | 'refuse' | null = null;

  statutFilters = [
    { value: 'Tous', label: 'Tous' },
    { value: StatutConge.EN_ATTENTE, label: 'En Attente' },
    { value: StatutConge.VALIDE, label: 'Validés' },
    { value: StatutConge.REFUSE, label: 'Refusés' }
  ];

  constructor(private congeService: CongeService) {}

  ngOnInit(): void {
    this.loadConges();
  }

  loadConges(): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.congeService.getAllConges().subscribe({
      next: (conges) => {
        this.conges = conges;
        this.applyFilter();
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading conges:', error);
        this.errorMessage = 'Erreur lors du chargement des congés';
        this.isLoading = false;
      }
    });
  }

  applyFilter(): void {
    if (this.selectedFilter === 'Tous') {
      this.filteredConges = this.conges;
    } else {
      this.filteredConges = this.conges.filter(conge => conge.statut === this.selectedFilter);
    }
  }

  onFilterChange(): void {
    this.applyFilter();
  }

  getTypeCongeLabel(type: TypeConge): string {
    return this.congeService.getTypeCongeLabel(type);
  }

  getStatutCongeLabel(statut: StatutConge): string {
    return this.congeService.getStatutCongeLabel(statut);
  }

  getStatutCongeColor(statut: StatutConge): string {
    return this.congeService.getStatutCongeColor(statut);
  }

  calculateDuration(dateDebut: string, dateFin: string): number {
    return this.congeService.calculateDuration(dateDebut, dateFin);
  }

  canValidate(conge: Conge): boolean {
    return conge.statut === StatutConge.EN_ATTENTE;
  }

  canRefuse(conge: Conge): boolean {
    return conge.statut === StatutConge.EN_ATTENTE;
  }

  getEmployeeName(conge: Conge): string {
    return conge.employeNomComplet || 'Non spécifié';
  }

  validateConge(conge: Conge): void {
    this.congeToValidate = conge;
    this.actionType = 'validate';
    this.confirmDialogData = {
      title: 'Valider le congé',
      message: `Êtes-vous sûr de vouloir valider le congé de ${this.getEmployeeName(conge)} du ${conge.dateDebut} au ${conge.dateFin} ?`,
      confirmText: 'Valider',
      cancelText: 'Annuler'
    };
    this.showConfirmDialog = true;
  }

  refuseConge(conge: Conge): void {
    this.congeToRefuse = conge;
    this.actionType = 'refuse';
    this.confirmDialogData = {
      title: 'Refuser le congé',
      message: `Êtes-vous sûr de vouloir refuser le congé de ${this.getEmployeeName(conge)} du ${conge.dateDebut} au ${conge.dateFin} ?`,
      confirmText: 'Refuser',
      cancelText: 'Annuler'
    };
    this.showConfirmDialog = true;
  }

  onConfirmAction(): void {
    if (this.actionType === 'validate' && this.congeToValidate && this.congeToValidate.id) {
      this.isLoading = true;
      this.showConfirmDialog = false;
      this.congeService.validerConge(this.congeToValidate.id).subscribe({
        next: () => {
          this.successMessage = 'Congé validé avec succès';
          this.congeToValidate = null;
          this.actionType = null;
          this.loadConges();
          setTimeout(() => {
            this.successMessage = '';
          }, 3000);
        },
        error: (error) => {
          console.error('Error validating conge:', error);
          this.errorMessage = 'Erreur lors de la validation du congé';
          this.isLoading = false;
          this.congeToValidate = null;
          this.actionType = null;
        }
      });
    } else if (this.actionType === 'refuse' && this.congeToRefuse && this.congeToRefuse.id) {
      this.isLoading = true;
      this.showConfirmDialog = false;
      this.congeService.refuserConge(this.congeToRefuse.id).subscribe({
        next: () => {
          this.successMessage = 'Congé refusé avec succès';
          this.congeToRefuse = null;
          this.actionType = null;
          this.loadConges();
          setTimeout(() => {
            this.successMessage = '';
          }, 3000);
        },
        error: (error) => {
          console.error('Error refusing conge:', error);
          this.errorMessage = 'Erreur lors du refus du congé';
          this.isLoading = false;
          this.congeToRefuse = null;
          this.actionType = null;
        }
      });
    }
  }

  onCancelAction(): void {
    this.showConfirmDialog = false;
    this.congeToValidate = null;
    this.congeToRefuse = null;
    this.actionType = null;
  }

  clearMessages(): void {
    this.errorMessage = '';
    this.successMessage = '';
  }

  getStats(): { total: number; enAttente: number; valides: number; refuses: number } {
    return {
      total: this.conges.length,
      enAttente: this.conges.filter(c => c.statut === StatutConge.EN_ATTENTE).length,
      valides: this.conges.filter(c => c.statut === StatutConge.VALIDE).length,
      refuses: this.conges.filter(c => c.statut === StatutConge.REFUSE).length
    };
  }
}
