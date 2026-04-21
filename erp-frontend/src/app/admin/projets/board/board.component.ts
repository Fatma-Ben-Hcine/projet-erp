import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { ProjetService } from '../../../core/services/projet.service';
import { ClientService } from '../../../core/services/client.service';
import { UtilisateurService } from'../../../core/services/utilisateur.service';
import { ProjetRequest, ProjetResponse, StatutProjet, ClientResponse, EmployeResponse, ActiviteRequest } from '../../../core/models/projet.model';
import { UtilisateurResponse } from '../../../core/models/utilisateur.model';
import { AdminSidebarComponent } from '../../shared/sidebar/sidebar.component';
import { StartProjectModalComponent } from '../start-project-modal/start-project-modal.component';
import { DepotModalComponent } from '../depot-modal/depot-modal.component';

@Component({
  selector: 'app-projets-board',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, RouterModule, AdminSidebarComponent, StartProjectModalComponent, DepotModalComponent],
  templateUrl: './board.component.html',
  styleUrl: './board.component.css'
})
export class ProjetsBoardComponent implements OnInit {
  projets: ProjetResponse[] = [];
  filteredProjets: ProjetResponse[] = [];
  clients: ClientResponse[] = [];
  employes: UtilisateurResponse[] = [];
  isLoading = false;
  errorMessage = '';
  showModal = false;
  currentView: 'kanban' | 'calendar' = 'kanban';
  isEditMode = false;
  projetEnCoursId: number | null = null;
  showDeleteModal = false;
  projetASupprimer: ProjetResponse | null = null;
  showDepotModal = false;
  projetPourDepot: ProjetResponse | null = null;

  // Form groups
  createForm!: FormGroup;
  selectedEmployes: number[] = [];

  // Colonnes Kanban
  nouveauxProjets: ProjetResponse[] = [];
  projetsEnCours: ProjetResponse[] = [];
  projetsTermines: ProjetResponse[] = [];
  projetsEnRetard: ProjetResponse[] = [];

  // Activités
  activites: ActiviteRequest[] = [];

  // Modal de détails
  selectedProjet: ProjetResponse | null = null;

  // Modal de démarrage de projet
  showStartProjectModal = false;
  projetToStart: ProjetResponse | null = null;

  isDarkMode = false;
  private observer: MutationObserver | null = null;

  constructor(
    private projetService: ProjetService,
    private clientService: ClientService,
    private utilisateurService: UtilisateurService,
    private fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadProjets();
    this.loadClients();
    this.loadEmployes();
    this.setupDarkModeObserver();
  }

  ngOnDestroy(): void {
    if (this.observer) {
      this.observer.disconnect();
    }
  }

  setupDarkModeObserver(): void {
    const body = document.body;
    this.isDarkMode = body.classList.contains('dark');

    this.observer = new MutationObserver((mutations) => {
      mutations.forEach((mutation) => {
        if (mutation.attributeName === 'class') {
          this.isDarkMode = body.classList.contains('dark');
        }
      });
    });

    this.observer.observe(body, { attributes: true, attributeFilter: ['class'] });
  }

  initForm(): void {
    this.createForm = this.fb.group({
      nom: ['', Validators.required],
      description: [''],
      budget: [0, [Validators.required, Validators.min(0)]],
      dateDebut: ['', Validators.required],
      dateLimite: ['', Validators.required],
      clientId: [null, Validators.required],
      chefDeProjetId: [null, Validators.required],
      progression: [0]
    });
  }

  loadProjets(): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.projetService.getAll().subscribe({
      next: (data: ProjetResponse[]) => {
        this.projets = data;
        this.filteredProjets = data;
        this.classifyProjets();
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Erreur lors du chargement des projets';
        this.isLoading = false;
      }
    });
  }

  loadClients(): void {
    this.clientService.getAll().subscribe({
      next: (data: ClientResponse[]) => {
        this.clients = data;
      },
      error: (err) => {
        console.error('Erreur lors du chargement des clients:', err);
      }
    });
  }

  loadEmployes(): void {
    this.utilisateurService.getAll().subscribe({
      next: (data: UtilisateurResponse[]) => {
        // Filtrer uniquement les vrais employés (ROLE_EMPLOYE)
        this.employes = data.filter(u => u.role === 'ROLE_EMPLOYE');
        console.log('Employés filtrés:', this.employes);
      },
      error: (err) => {
        console.error('Erreur lors du chargement des employés:', err);
      }
    });
  }

  get employesSelectionnes(): UtilisateurResponse[] {
    return this.employes.filter(e =>
      this.selectedEmployes.includes(e.id)
    );
  }

  classifyProjets(): void {
    this.nouveauxProjets = [];
    this.projetsEnCours = [];
    this.projetsTermines = [];
    this.projetsEnRetard = [];

    const today = new Date();
    
    this.projets.forEach(projet => {
      const dateLimite = new Date(projet.dateLimite);
      const joursRestants = Math.ceil((dateLimite.getTime() - today.getTime()) / (1000 * 60 * 60 * 24));
      projet.joursRestants = Math.max(0, joursRestants);

      // Utiliser le statut du backend et calculer les projets en retard
      switch (projet.statut) {
        case StatutProjet.NOUVEAU:
          this.nouveauxProjets.push(projet);
          break;
        case StatutProjet.EN_COURS:
          // Vérifier si le projet est en retard
          if (joursRestants < 0) {
            this.projetsEnRetard.push(projet);
          } else {
            this.projetsEnCours.push(projet);
          }
          break;
        case StatutProjet.TERMINE:
          this.projetsTermines.push(projet);
          break;
        default:
          // Cas par défaut pour les anciens projets sans statut
          if (projet.progression === 100) {
            projet.statut = StatutProjet.TERMINE;
            this.projetsTermines.push(projet);
          } else if (joursRestants < 0) {
            projet.statut = StatutProjet.EN_RETARD;
            this.projetsEnRetard.push(projet);
          } else if (projet.progression > 0) {
            projet.statut = StatutProjet.EN_COURS;
            this.projetsEnCours.push(projet);
          } else {
            projet.statut = StatutProjet.NOUVEAU;
            this.nouveauxProjets.push(projet);
          }
      }
    });
  }

  openCreateModal(): void {
    this.isEditMode = false;
    this.projetEnCoursId = null;
    this.showModal = true;
    this.errorMessage = '';
    this.createForm.reset({ progression: 0 });
    this.selectedEmployes = [];
    this.activites = [];
  }

  closeModal(): void {
    this.showModal = false;
    this.errorMessage = '';
  }

  toggleView(view: 'kanban' | 'calendar'): void {
    this.currentView = view;
  }

  onEmployeToggle(employeId: number, event: any): void {
    if (event.target.checked) {
      this.selectedEmployes.push(employeId);
    } else {
      const index = this.selectedEmployes.indexOf(employeId);
      if (index > -1) {
        this.selectedEmployes.splice(index, 1);
      }
    }
    
    // Si le chef de projet n'est plus dans les employés sélectionnés, le réinitialiser
    const chefId = this.createForm.get('chefDeProjetId')?.value;
    if (!event.target.checked && chefId && +chefId === employeId) {
      this.createForm.patchValue({ chefDeProjetId: null });
    }
  }

  addActivite(): void {
    this.activites.push({
      nom: '',
      description: ''
    });
  }

  removeActivite(index: number): void {
    this.activites.splice(index, 1);
  }

  saveProjet(): void {
    if (this.createForm.invalid || this.selectedEmployes.length === 0) {
      this.errorMessage = 'Veuillez remplir tous les champs obligatoires';
      return;
    }
    this.errorMessage = '';
    const payload: ProjetRequest = {
      ...this.createForm.value,
      progression: this.createForm.value.progression || 0,
      employeIds: this.selectedEmployes,
      activites: this.activites.length > 0 ? this.activites : undefined
    };

    if (this.isEditMode && this.projetEnCoursId) {
      this.projetService.update(this.projetEnCoursId, payload).subscribe({
        next: () => { this.loadProjets(); this.closeModal(); },
        error: (err) => {
          if (err.status === 400 && err.error?.message) {
            this.errorMessage = err.error.message;
          } else if (typeof err.error === 'string' && err.error.length > 0) {
            this.errorMessage = err.error;
          } else {
            this.errorMessage = 'Erreur lors de la modification du projet. Veuillez réessayer.';
          }
        }
      });
    } else {
      this.projetService.create(payload).subscribe({
        next: () => { this.loadProjets(); this.closeModal(); },
        error: (err) => {
          if (err.status === 400 && err.error?.message) {
            this.errorMessage = err.error.message;
          } else if (typeof err.error === 'string' && err.error.length > 0) {
            this.errorMessage = err.error;
          } else {
            this.errorMessage = 'Erreur lors de la création du projet. Veuillez réessayer.';
          }
        }
      });
    }
  }

  updateStatut(projet: ProjetResponse, nouveauStatut: string): void {
    this.projetService.updateStatut(projet.id, nouveauStatut).subscribe({
      next: () => {
        this.loadProjets();
      },
      error: (err: any) => {
        console.error('Erreur lors de la mise à jour du statut:', err);
      }
    });
  }

  confirmDelete(projet: ProjetResponse): void {
    this.projetASupprimer = projet;
    this.showDeleteModal = true;
  }

  deleteProjet(): void {
    if (!this.projetASupprimer) return;
    this.projetService.delete(this.projetASupprimer.id).subscribe({
      next: () => {
        this.loadProjets();
        this.showDeleteModal = false;
        this.projetASupprimer = null;
      },
      error: (err: any) => {
        this.errorMessage = err.error?.message || 'Erreur lors de la suppression';
        this.showDeleteModal = false;
      }
    });
  }

  cancelDelete(): void {
    this.showDeleteModal = false;
    this.projetASupprimer = null;
  }

  formatDate(dateStr: string): string {
    const date = new Date(dateStr);
    return date.toLocaleDateString('fr-FR', { day: '2-digit', month: '2-digit', year: 'numeric' });
  }

  getStatutColor(statut: string): string {
    switch (statut) {
      case StatutProjet.NOUVEAU: return '#3b82f6';
      case StatutProjet.EN_COURS: return '#eab308';
      case StatutProjet.TERMINE: return '#22c55e';
      case StatutProjet.EN_RETARD: return '#ef4444';
      default: return '#6b7280';
    }
  }

  getStatutIcon(statut: string): string {
    switch (statut) {
      case StatutProjet.NOUVEAU: return 'new_releases';
      case StatutProjet.EN_COURS: return 'pending';
      case StatutProjet.TERMINE: return 'check_circle';
      case StatutProjet.EN_RETARD: return 'error';
      default: return 'help';
    }
  }

  getNextStatutButton(statut: string): { text: string; color: string; action: string } | null {
    switch (statut) {
      case StatutProjet.NOUVEAU:
        return { text: 'Start', color: '#3b82f6', action: StatutProjet.EN_COURS };
      case StatutProjet.EN_COURS:
        return { text: 'Dépôt', color: '#eab308', action: StatutProjet.TERMINE };
      case StatutProjet.TERMINE:
        return { text: 'Archiver', color: '#6b7280', action: 'ARCHIVE' };
      default:
        return null;
    }
  }

  viewProjet(projet: ProjetResponse): void {
    this.projetService.getById(projet.id).subscribe({
      next: (details) => {
        console.log('Détails complets reçus:', JSON.stringify(details, null, 2));
        this.selectedProjet = details;
      },
      error: (err: any) => {
        console.error('Erreur lors du chargement des détails:', err);
      }
    });
  }

  editProjet(projet: ProjetResponse): void {
    this.projetService.getById(projet.id).subscribe({
      next: (details) => {
        this.isEditMode = true;
        this.projetEnCoursId = details.id;
        this.showModal = true;
        this.errorMessage = '';
        this.activites = [];

        // Pré-remplir le formulaire
        this.createForm.patchValue({
          nom: details.nom,
          description: details.description,
          budget: details.budget,
          dateDebut: details.dateDebut,
          dateLimite: details.dateLimite,
          clientId: details.client?.id,
          chefDeProjetId: details.chefDeProjet?.id,
          progression: details.progression
        });

        // Pré-cocher les employés assignés
        this.selectedEmployes = details.employes?.map(e => e.id) || [];
      },
      error: (err: any) => console.error('Erreur chargement projet:', err)
    });
  }

  fermerDetails(): void {
    this.selectedProjet = null;
  }

  openStartProjectModal(projet: ProjetResponse): void {
    console.log('Ouverture modale pour le projet:', projet.id);
    // Faire un appel API pour récupérer les détails complets du projet avec les employés
    this.projetService.getById(projet.id).subscribe({
      next: (details) => {
        console.log('Détails complets du projet pour modale:', JSON.stringify(details, null, 2));
        this.projetToStart = details;
        this.showStartProjectModal = true;
      },
      error: (err: any) => {
        console.error('Erreur lors du chargement des détails du projet:', err);
        // En cas d'erreur, utiliser l'objet de base quand même
        this.projetToStart = projet;
        this.showStartProjectModal = true;
      }
    });
  }

  closeStartProjectModal(): void {
    this.showStartProjectModal = false;
    this.projetToStart = null;
  }

  onProjectStarted(): void {
    // Changer le statut du projet à EN_COURS après la segmentation
    if (this.projetToStart) {
      this.projetService.updateStatut(this.projetToStart.id, 'EN_COURS').subscribe({
        next: () => {
          console.log('Statut du projet mis à jour avec succès');
          // Recharger les projets après la mise à jour du statut
          this.loadProjets();
          this.closeStartProjectModal();
        },
        error: (err: any) => {
          console.error('Erreur lors de la mise à jour du statut du projet:', err);
          // Même en cas d'erreur, recharger les projets
          this.loadProjets();
          this.closeStartProjectModal();
        }
      });
    }
  }

  openDepotModal(projet: ProjetResponse): void {
    this.projetPourDepot = projet;
    this.showDepotModal = true;
  }

  closeDepotModal(): void {
    this.showDepotModal = false;
    this.projetPourDepot = null;
  }

  onDepotSubmitted(depotData: { type: 'lien' | 'fichier', value: string | File }): void {
    this.closeDepotModal();
    // Logique pour traiter le dépôt
    console.log('Dépôt soumis:', depotData);
    // Ici vous pouvez ajouter la logique pour envoyer les données au backend
    // Par exemple, appeler un service ou émettre un événement
    if (depotData.type === 'lien') {
      console.log('Lien de dépôt:', depotData.value);
      // TODO: Appeler le service pour sauvegarder le lien
    } else {
      console.log('Fichier de dépôt:', depotData.value);
      // TODO: Appeler le service pour uploader le fichier
    }
  }
}