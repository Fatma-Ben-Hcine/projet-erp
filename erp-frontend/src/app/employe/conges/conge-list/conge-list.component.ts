import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { CongeService } from '../../../core/services/conge.service';
import { Conge, StatutConge, TypeConge } from '../../../core/models/conge.model';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { EmployeSidebarComponent } from '../../shared/sidebar/sidebar.component';

@Component({
  selector: 'app-conge-list',
  standalone: true,
  imports: [CommonModule, ConfirmDialogComponent, LoadingSpinnerComponent, EmployeSidebarComponent],
  templateUrl: './conge-list.component.html',
  styleUrls: ['./conge-list.component.css']
})
export class CongeListComponent implements OnInit {
  conges: Conge[] = [];
  isLoading = false;
  showConfirmDialog = false;
  confirmDialogData = {
    title: '',
    message: '',
    confirmText: '',
    cancelText: ''
  };
  congeToDelete: Conge | null = null;
  errorMessage = '';
  successMessage = '';

  constructor(
    private congeService: CongeService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadConges();
  }

  loadConges(): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.congeService.getMesConges().subscribe({
      next: (conges: any[]) => {
        this.conges = conges;
        this.isLoading = false;
      },
      error: (error: any) => {
        console.error('Error loading conges:', error);
        this.errorMessage = 'Erreur lors du chargement des congés';
        this.isLoading = false;
      }
    });
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

  canEdit(conge: Conge): boolean {
    return conge.statut === StatutConge.EN_ATTENTE;
  }

  canDelete(conge: Conge): boolean {
    return conge.statut === StatutConge.EN_ATTENTE;
  }

  editConge(conge: Conge): void {
    this.router.navigate(['/employe/conges/modifier', conge.id]);
  }

  deleteConge(conge: Conge): void {
    this.congeToDelete = conge;
    this.confirmDialogData = {
      title: 'Supprimer le congé',
      message: `Êtes-vous sûr de vouloir supprimer votre congé du ${conge.dateDebut} au ${conge.dateFin} ?`,
      confirmText: 'Supprimer',
      cancelText: 'Annuler'
    };
    this.showConfirmDialog = true;
  }

  onConfirmDelete(): void {
    if (this.congeToDelete && this.congeToDelete.id) {
      this.isLoading = true;
      this.showConfirmDialog = false;
      this.congeService.supprimerConge(this.congeToDelete.id).subscribe({
        next: () => {
          this.successMessage = 'Congé supprimé avec succès';
          this.congeToDelete = null;
          this.loadConges();
          setTimeout(() => {
            this.successMessage = '';
          }, 3000);
        },
        error: (error) => {
          console.error('Error deleting conge:', error);
          this.errorMessage = 'Erreur lors de la suppression du congé';
          this.isLoading = false;
          this.congeToDelete = null;
        }
      });
    }
  }

  onCancelDelete(): void {
    this.showConfirmDialog = false;
    this.congeToDelete = null;
  }

  newConge(): void {
    this.router.navigate(['/employe/conges/nouveau']);
  }

  clearMessages(): void {
    this.errorMessage = '';
    this.successMessage = '';
  }
}
