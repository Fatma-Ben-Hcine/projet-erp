import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { EmployeRessourceService } from '../../../core/services/employe-ressource.service';
import { DemandeRessourceService } from '../../../core/services/demande-ressource.service';
import { AuthService } from '../../../auth/auth.service';
import { Ressource, DemandeRessourceRequest } from '../../../core/models/ressource.model';

@Component({
  selector: 'app-demande-ressources',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './demande-ressources.component.html',
  styleUrls: ['./demande-ressources.component.scss']
})
export class DemandeRessourcesComponent implements OnInit {
  ressources: Ressource[] = [];
  selectedRessources: Set<number> = new Set();
  loading = false;
  submitting = false;
  error: string | null = null;
  success: string | null = null;

  constructor(
    private employeRessourceService: EmployeRessourceService,
    private demandeService: DemandeRessourceService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadRessourcesDisponibles();
  }

  loadRessourcesDisponibles(): void {
    this.loading = true;
    this.employeRessourceService.getRessourcesActives().subscribe({
      next: (ressources) => {
        this.ressources = ressources;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement des ressources disponibles';
        this.loading = false;
        console.error(err);
      }
    });
  }

  toggleSelection(ressourceId: number): void {
    if (this.selectedRessources.has(ressourceId)) {
      this.selectedRessources.delete(ressourceId);
    } else {
      this.selectedRessources.add(ressourceId);
    }
  }

  isSelected(ressourceId: number): boolean {
    return this.selectedRessources.has(ressourceId);
  }

  getSituationClass(situation: string): string {
    return situation === 'DISPONIBLE' ? 'badge-success' : 'badge-warning';
  }

  formatDate(dateStr: string | null | undefined): string {
    if (!dateStr) return '---';
    const date = new Date(dateStr);
    if (isNaN(date.getTime())) return '---';
    return date.toLocaleDateString('fr-FR', {
      day: '2-digit',
      month: 'long',
      year: 'numeric'
    });
  }

  // Helper pour vérifier si la ressource est demandée par moi
  isMaDemande(ressource: Ressource): boolean {
    return ressource.situation === 'DEMANDE' && !!ressource.employeDemandeur;
  }

  soumettreDemande(): void {
    const ressourcesDisponibles = Array.from(this.selectedRessources).filter(ressourceId => {
      const ressource = this.ressources.find(r => r.id === ressourceId);
      return ressource && ressource.situation === 'DISPONIBLE';
    });

    if (ressourcesDisponibles.length === 0) {
      this.error = 'Aucune nouvelle ressource à demander (toutes déjà demandées)';
      return;
    }

    this.submitting = true;
    this.error = null;
    this.success = null;

    let completedRequests = 0;
    const totalRequests = ressourcesDisponibles.length;

    ressourcesDisponibles.forEach((ressourceId: number) => {
      this.employeRessourceService.demanderRessource(ressourceId).subscribe({
        next: () => {
          completedRequests++;
          if (completedRequests === totalRequests) {
            this.success = 'Demande(s) soumise(s) avec succès';
            this.selectedRessources.clear();
            this.loadRessourcesDisponibles();
            this.submitting = false;
          }
        },
        error: (err) => {
          this.error = 'Erreur lors de la soumission de la demande';
          this.submitting = false;
          console.error(err);
        }
      });
    });
  }
}
