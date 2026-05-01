import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { RessourcesDisponiblesService } from '../../../core/services/ressources-disponibles.service';
import { DemandeRessourceService } from '../../../core/services/demande-ressource.service';
import { AuthService } from '../../../auth/auth.service';
import { EmployeSidebarComponent } from '../../shared/sidebar/sidebar.component';
import { RessourceDisponible, DemandeRessourceRequest, DemandeMultipleRequest } from '../../../core/models/ressource.model';

@Component({
  selector: 'app-demande-ressources',
  standalone: true,
  imports: [CommonModule, RouterModule, EmployeSidebarComponent],
  templateUrl: './demande-ressources.component.html',
  styleUrls: ['./demande-ressources.component.scss']
})
export class DemandeRessourcesComponent implements OnInit {
  ressources: any[] = [];
  selectedIds: number[] = [];
  isLoading = false;
  errorMessage = '';
  successMessage = '';
  error = '';
  success = '';

  constructor(
    private http: HttpClient,
    private ressourcesDisponiblesService: RessourcesDisponiblesService,
    private demandeService: DemandeRessourceService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadRessourcesDisponibles();
  }

  loadRessourcesDisponibles(): void {
    this.isLoading = true;
    this.ressourcesDisponiblesService.getRessourcesDisponibles().subscribe({
      next: (ressources: RessourceDisponible[]) => {
        this.ressources = ressources;
        this.isLoading = false;
        console.log('Ressources disponibles chargées:', ressources.length, 'éléments');
      },
      error: (err: any) => {
        this.isLoading = false;
        console.error('=== ERREUR DÉTAILLÉE ===');
        console.error('URL appelée:', err.url);
        console.error('Status:', err.status);
        console.error('Status Text:', err.statusText);
        console.error('Message:', err.message);
        console.error('Erreur complète:', err);
        
        if (err.status === 0) {
          this.error = 'Impossible de contacter le serveur backend. Vérifiez que le backend est démarré sur http://localhost:8080';
        } else if (err.status === 404) {
          this.error = `Endpoint non trouvé: ${err.url}. Vérifiez que l'URL est correcte dans le backend.`;
        } else if (err.status === 500) {
          this.error = `Erreur serveur (500): ${err.message}. Consultez les logs du backend pour plus de détails.`;
        } else {
          this.error = `Erreur (${err.status}): ${err.message || 'Erreur inconnue'}`;
        }
      }
    });
  }

  
  formatDate(dateStr: string): string {
    if (!dateStr) return '-';
    return new Date(dateStr).toLocaleDateString('fr-FR');
  }

  showSuccess(msg: string): void {
    this.successMessage = msg;
    this.success = msg;
    this.error = '';
    this.errorMessage = '';
    setTimeout(() => {
      this.successMessage = '';
      this.success = '';
    }, 3000);
  }

  isSelected(id: number): boolean {
    return this.selectedIds.includes(id);
  }

  toggleSelection(id: number): void {
    if (this.isSelected(id)) {
      this.selectedIds = this.selectedIds.filter(x => x !== id);
    } else {
      this.selectedIds.push(id);
    }
  }

  soumettreDemande(): void {
    if (this.selectedIds.length === 0) return;
    
    this.isLoading = true;
    this.demandeService.createDemandesMultiples({ ressourceIds: this.selectedIds })
      .subscribe({
        next: (response) => {
          this.successMessage = 
            `${this.selectedIds.length} ressource(s) demandée(s) avec succès`;
          this.selectedIds = [];
          
          // Recharger les données pour voir le compteur mis à jour
          this.loadRessourcesDisponibles();
          this.isLoading = false;
        },
        error: () => {
          this.errorMessage = 'Erreur lors de la soumission';
          this.isLoading = false;
        }
      });
  }

  demanderRessource(ressource: any): void {
  const apiUrl = 'http://localhost:8080/api';
  this.http.post(
    `${apiUrl}/employe/ressources/${ressource.id}/demander`,
    {}
  ).subscribe({
    next: (response: any) => {
      // Mettre à jour localement
      ressource.dejaDemandeParMoi = true;
      ressource.situation = 'DEMANDE';
      // Utiliser le nombre retourné par le backend
      if (response.nombreDemandes !== undefined) {
        ressource.nombreDemandes = response.nombreDemandes;
      } else {
        ressource.nombreDemandes += 1;
      }
      this.showSuccess('Ressource demandée avec succès !');
    },
    error: (err) => {
      this.showError(
        err.error?.erreur || 'Erreur lors de la demande');
    }
  });
}

annulerDemande(ressource: any): void {
  const apiUrl = 'http://localhost:8080/api';
  this.http.delete(
    `${apiUrl}/employe/ressources/${ressource.id}/annuler` 
  ).subscribe({
    next: (response: any) => {
      // Mettre à jour localement
      ressource.dejaDemandeParMoi = false;
      // Utiliser le nombre retourné par le backend
      if (response.nombreDemandes !== undefined) {
        ressource.nombreDemandes = response.nombreDemandes;
      } else {
        ressource.nombreDemandes = Math.max(
          0, ressource.nombreDemandes - 1);
      }
      // Si plus aucune demande → DISPONIBLE
      if (ressource.nombreDemandes === 0) {
        ressource.situation = 'DISPONIBLE';
      }
      this.showSuccess('Demande annulée avec succès');
    },
    error: (err) => {
      this.showError(
        err.error?.erreur || 'Erreur lors de l\'annulation');
    }
  });
}

showError(msg: string): void {
    this.errorMessage = msg;
    this.error = msg;
    this.success = '';
    this.successMessage = '';
    setTimeout(() => {
      this.errorMessage = '';
      this.error = '';
    }, 5000);
  }
}
