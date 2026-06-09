import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AdminRessourceService } from '../../../core/services/admin-ressource.service';
import { Ressource, RessourceRequest } from '../../../core/models/ressource.model';

@Component({
  selector: 'app-ressource-form',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './ressource-form.component.html',
  styleUrls: ['./ressource-form.component.scss']
})
export class RessourceFormComponent implements OnInit {
  isEditMode = false;
  ressourceId: number | null = null;
  loading = false;
  saving = false;
  error: string | null = null;

  // Form data - nouvelle structure selon la logique métier
  ressource: RessourceRequest = {
    nom: '',
    description: '',
    prix: 0,
    statut: 'ACTIVE',
    dateDebutAbonnement: null,
    dateFinAbonnement: null
  };

  constructor(
    private adminRessourceService: AdminRessourceService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.ressourceId = this.route.snapshot.params['id'];
    if (this.ressourceId) {
      this.isEditMode = true;
      this.loadRessource(this.ressourceId);
    }
  }

  loadRessource(id: number): void {
    this.loading = true;
    this.adminRessourceService.getAll().subscribe({
      next: (ressources: any[]) => {
        const ressource = ressources.find(r => r.id === id);
        if (ressource) {
          this.ressource = {
            nom: ressource.nom,
            description: ressource.description || '',
            prix: ressource.prix,
            statut: ressource.statut || 'ACTIVE',
            dateDebutAbonnement: ressource.dateDebutAbonnement || null,
            dateFinAbonnement: ressource.dateFinAbonnement || null
          };
        }
        this.loading = false;
      },
      error: (err: any) => {
        this.error = 'Erreur lors du chargement de la ressource';
        this.loading = false;
        console.error(err);
      }
    });
  }

  onSubmit(): void {
    if (!this.ressource.nom) {
      this.error = 'Veuillez remplir tous les champs obligatoires';
      return;
    }

    this.saving = true;
    this.error = null;

    // Payload final avec la nouvelle structure selon la logique métier
    const payload: RessourceRequest = {
      nom: this.ressource.nom,
      description: this.ressource.description,
      prix: this.ressource.prix,
      statut: this.ressource.statut,
      dateDebutAbonnement: this.ressource.dateDebutAbonnement,
      dateFinAbonnement: this.ressource.dateFinAbonnement
    };

    const observable = this.isEditMode && this.ressourceId
      ? this.adminRessourceService.update(this.ressourceId, payload)
      : this.adminRessourceService.create(payload);

    observable.subscribe({
      next: () => {
        this.router.navigate(['/admin/ressources']);
      },
      error: (err: any) => {
        this.error = 'Erreur lors de la sauvegarde de la ressource';
        this.saving = false;
        console.error(err);
      }
    });
  }

  // Méthodes pour la validation des dates d'abonnement
  onDateAbonnementChange(): void {
    // Logique pour valider les dates d'abonnement si nécessaire
    console.log('Dates d\'abonnement modifiées:', {
      debut: this.ressource.dateDebutAbonnement,
      fin: this.ressource.dateFinAbonnement
    });
  }

  isDateFinAbonnementInPast(): boolean {
    if (!this.ressource.dateFinAbonnement) {
      return false;
    }
    const dateFin = new Date(this.ressource.dateFinAbonnement);
    const today = new Date();
    today.setHours(0, 0, 0, 0); // Ignorer l'heure
    return dateFin < today;
  }
}
