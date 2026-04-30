import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AdminRessourceService } from '../../../core/services/admin-ressource.service';
import { Ressource } from '../../../core/models/ressource.model';
import { formatDate } from '@angular/common';

@Component({
  selector: 'app-ressources-list',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './ressources-list.component.html',
  styleUrls: ['./ressources-list.component.scss']
})
export class RessourcesListComponent implements OnInit {
  ressources: Ressource[] = [];
  loading = false;
  error: string | null = null;

  constructor(private adminRessourceService: AdminRessourceService) {}

  ngOnInit(): void {
    this.loadRessources();
  }

  loadRessources(): void {
    this.loading = true;
    this.adminRessourceService.getAllRessources().subscribe({
      next: (ressources: Ressource[]) => {
        this.ressources = ressources;
        this.loading = false;
      },
      error: (err: any) => {
        this.error = 'Erreur lors du chargement des ressources';
        this.loading = false;
        console.error(err);
      }
    });
  }

  deleteRessource(id: number): void {
    if (confirm('Êtes-vous sûr de vouloir supprimer cette ressource ?')) {
      this.adminRessourceService.deleteRessource(id).subscribe({
        next: () => {
          this.loadRessources();
        },
        error: (err: any) => {
          console.error('Erreur lors de la suppression:', err);
        }
      });
    }
  }

  getSituationClass(situation: string): string {
    return situation === 'DEMANDE' ? 'badge-warning' : 'badge-info';
  }

  getStatutClass(statut: string): string {
    return statut === 'ACTIVE' ? 'badge-success' : 'badge-danger';
  }

  libererRessource(id: number): void {
    if (confirm('Êtes-vous sûr de vouloir libérer cette ressource ?')) {
      this.adminRessourceService.libererRessource(id).subscribe({
        next: () => {
          this.loadRessources();
        },
        error: (err: any) => {
          console.error('Erreur lors de la libération:', err);
        }
      });
    }
  }

  changerStatut(id: number, statut: 'ACTIVE' | 'NON_ACTIVE'): void {
    this.adminRessourceService.changerStatut(id, statut).subscribe({
      next: () => {
        this.loadRessources();
      },
      error: (err: any) => {
        console.error('Erreur lors du changement de statut:', err);
      }
    });
  }

  formatStatut(statut: string): string {
    return statut === 'ACTIVE' ? 'active' : 'non active';
  }

  formatSituation(situation: string): string {
    return situation === 'DEMANDE' ? 'demandé' : 'disponible';
  }

  formatDate(date: string | null | undefined): string {
    if (!date) return '-';
    return formatDate(date, 'dd/MM/yyyy', 'fr');
  }
}
