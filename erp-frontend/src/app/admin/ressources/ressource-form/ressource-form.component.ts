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

  // Form data - nouvelle structure simplifiée
  ressource: RessourceRequest = {
    nom: '',
    description: '',
    type: ''
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
    this.adminRessourceService.getAllRessources().subscribe({
      next: (ressources) => {
        const ressource = ressources.find(r => r.id === id);
        if (ressource) {
          this.ressource = {
            nom: ressource.nom,
            description: ressource.description || '',
            type: ressource.type || ''
          };
        }
        this.loading = false;
      },
      error: (err) => {
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

    // Payload final avec la nouvelle structure simplifiée
    const payload: RessourceRequest = {
      nom: this.ressource.nom,
      description: this.ressource.description,
      type: this.ressource.type
    };

    const observable = this.isEditMode && this.ressourceId
      ? this.adminRessourceService.updateRessource(this.ressourceId, payload)
      : this.adminRessourceService.createRessource(payload);

    observable.subscribe({
      next: () => {
        this.router.navigate(['/admin/ressources']);
      },
      error: (err) => {
        this.error = 'Erreur lors de la sauvegarde de la ressource';
        this.saving = false;
        console.error(err);
      }
    });
  }
}
