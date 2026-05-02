import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { EmployeSidebarComponent } from '../../shared/sidebar/sidebar.component';
import { EmployeProjetService } from '../../../core/services/employe-projet.service';
import { EmployeActiviteService } from '../../../core/services/employe-activite.service';
import { AuthService } from '../../../auth/auth.service';
import { ProjetResponse } from '../../../core/models/projet.model';
import { ActiviteResponse } from '../../../core/models/activite.model';

@Component({
  selector: 'app-employe-projet-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, EmployeSidebarComponent],
  templateUrl: './projet-detail.component.html',
  styleUrls: ['./projet-detail.component.css']
})
export class EmployeProjetDetailComponent implements OnInit {
  projet: ProjetResponse | null = null;
  activites: ActiviteResponse[] = [];
  isLoading = true;
  errorMessage = '';
  currentUserId: number | null = null;
  isChefDeProjet = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private projetService: EmployeProjetService,
    private employeActiviteService: EmployeActiviteService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.currentUserId = parseInt(this.authService.getUserId() || '0', 10);
    const projetId = this.route.snapshot.paramMap.get('id');
    if (projetId) {
      this.loadProjet(parseInt(projetId, 10));
    } else {
      this.errorMessage = 'ID de projet manquant';
      this.isLoading = false;
    }
  }

  loadProjet(id: number): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.projetService.getById(id).subscribe({
      next: (data) => {
        this.projet = data;
        // Check chef de projet status via dedicated endpoint
        this.projetService.isChefDeProjet(id).subscribe({
          next: (isChef) => {
            this.isChefDeProjet = Boolean(isChef);
          },
          error: () => {
            this.isChefDeProjet = false;
          }
        });
        this.loadActivites(id);
      },
      error: (err) => {
        this.errorMessage = 'Erreur lors du chargement du projet';
        this.isLoading = false;
      }
    });
  }

  loadActivites(projetId: number): void {
    this.employeActiviteService.getByProjet(projetId).subscribe({
      next: (data) => {
        this.activites = data;
        this.isLoading = false;
      },
      error: (err: any) => {
        this.errorMessage = 'Erreur lors du chargement des activités';
        this.isLoading = false;
      }
    });
  }


  goBack(): void {
    this.router.navigate(['/employe/dashboard']);
  }

  formatDate(dateString: string | null | undefined): string {
    if (!dateString) return '—';
    const date = new Date(dateString);
    return date.toLocaleDateString('fr-FR');
  }

  getStatutLabel(statut: string | undefined): string {
    if (!statut) return 'Inconnu';
    const labels: Record<string, string> = {
      'NOUVEAU': 'Nouveau',
      'EN_COURS': 'En cours',
      'TERMINE': 'Terminé',
      'EN_RETARD': 'En retard'
    };
    return labels[statut] || statut;
  }

  getStatutClass(statut: string | undefined): string {
    if (!statut) return 'badge-default';
    const classes: Record<string, string> = {
      'NOUVEAU': 'badge-blue',
      'EN_COURS': 'badge-yellow',
      'TERMINE': 'badge-green',
      'EN_RETARD': 'badge-red'
    };
    return classes[statut] || 'badge-default';
  }
}
