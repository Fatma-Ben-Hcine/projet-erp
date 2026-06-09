import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { EmployeRessourceService } from '../../../core/services/employe-ressource.service';
import { Ressource } from '../../../core/models/ressource.model';

@Component({
  selector: 'app-liste-ressources',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './liste-ressources.component.html',
  styleUrls: ['./liste-ressources.component.scss']
})
export class ListeRessourcesComponent implements OnInit {
  ressourcesDisponibles: Ressource[] = [];
  mesDemandes: Ressource[] = [];
  loading = false;
  error: string | null = null;
  successMessage: string | null = null;

  constructor(private employeRessourceService: EmployeRessourceService) {}

  ngOnInit(): void {
    this.loadRessources();
    this.loadMesDemandes();
  }

  loadRessources(): void {
    this.loading = true;
    this.employeRessourceService.getRessourcesActives().subscribe({
      next: (ressources: Ressource[]) => {
        this.ressourcesDisponibles = ressources;
        this.loading = false;
      },
      error: (err: any) => {
        this.error = 'Erreur lors du chargement des ressources';
        this.loading = false;
        console.error(err);
      }
    });
  }

  loadMesDemandes(): void {
    this.employeRessourceService.getMesDemandes().subscribe({
      next: (demandes: Ressource[]) => {
        this.mesDemandes = demandes;
      },
      error: (err: any) => {
        console.error('Erreur lors du chargement des demandes:', err);
      }
    });
  }

  demanderRessource(ressource: Ressource): void {
    if (ressource.situation !== 'DISPONIBLE') {
      return;
    }

    this.employeRessourceService.demanderRessource(ressource.id).subscribe({
      next: (response) => {
        this.successMessage = response.message;
        this.loadRessources();
        this.loadMesDemandes();
        
        // Effacer le message après 3 secondes
        setTimeout(() => {
          this.successMessage = null;
        }, 3000);
      },
      error: (err: any) => {
        this.error = err.error?.erreur || 'Erreur lors de la demande';
        setTimeout(() => {
          this.error = null;
        }, 3000);
      }
    });
  }

  annulerDemande(ressource: Ressource): void {
    if (!confirm('Êtes-vous sûr de vouloir annuler cette demande ?')) {
      return;
    }

    this.employeRessourceService.annulerDemande(ressource.id).subscribe({
      next: (response) => {
        this.successMessage = response.message;
        this.loadRessources();
        this.loadMesDemandes();
        
        setTimeout(() => {
          this.successMessage = null;
        }, 3000);
      },
      error: (err: any) => {
        this.error = err.error?.erreur || 'Erreur lors de l\'annulation';
        setTimeout(() => {
          this.error = null;
        }, 3000);
      }
    });
  }

  isMaDemande(ressource: Ressource): boolean {
    return this.mesDemandes.some(demande => demande.id === ressource.id);
  }

  isDisponiblePourMoi(ressource: Ressource): boolean {
    return ressource.statut === 'ACTIVE' && 
           ressource.situation === 'DISPONIBLE' && 
           !this.isMaDemande(ressource);
  }

  formatDate(date: string | null | undefined): string {
    if (!date) return '-';
    const d = new Date(date);
    return d.toLocaleDateString('fr-FR', {
      day: '2-digit',
      month: 'long',
      year: 'numeric'
    });
  }
}
