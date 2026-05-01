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
    this.loadRessources(); 
  }

  loadRessources(): void {
    this.isLoading = true;
    this.http.get<any[]>(`http://localhost:8080/api/employe/ressources`).subscribe({
      next: (data) => { 
        this.ressources = data; 
        this.isLoading = false; 
      },
      error: () => { 
        this.errorMessage = 'Erreur chargement'; 
        this.isLoading = false; 
      }
    });
  }

  onCheckboxChange(ressource: any, event: any): void {
    if (event.target.checked) {
      this.demanderRessource(ressource);
    } else {
      this.annulerDemande(ressource);
    }
  }

  demanderRessource(ressource: any): void {
    this.http.post(`http://localhost:8080/api/employe/ressources/${ressource.id}/demander`, {})
      .subscribe({
        next: (res: any) => {
          ressource.dejaDemandeParMoi = true;
          ressource.nombreDemandes = res.nombreDemandes ?? ressource.nombreDemandes + 1;
          this.showSuccess('Ressource demandée avec succès !');
        },
        error: (err) => this.showError(err.error?.erreur || 'Erreur')
      });
  }

  annulerDemande(ressource: any): void {
    this.http.delete(`http://localhost:8080/api/employe/ressources/${ressource.id}/annuler`)
      .subscribe({
        next: (res: any) => {
          ressource.dejaDemandeParMoi = false;
          ressource.nombreDemandes = res.nombreDemandes ?? ressource.nombreDemandes - 1;
          this.showSuccess('Demande annulée');
        },
        error: (err) => this.showError(err.error?.erreur || 'Erreur annulation')
      });
  }

  formatDate(dateStr: string | null): string {
    if (!dateStr) return '';
    return new Date(dateStr).toLocaleDateString('fr-FR');
  }

  showSuccess(msg: string): void {
    this.successMessage = msg;
    setTimeout(() => this.successMessage = '', 3000);
  }

  showError(msg: string): void {
    this.errorMessage = msg;
    setTimeout(() => this.errorMessage = '', 5000);
  }
}
