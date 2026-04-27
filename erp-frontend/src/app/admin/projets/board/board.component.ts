import { Component, OnInit, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, FormsModule, FormArray, AbstractControl, ValidationErrors } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';
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

  // Modal de détails
  selectedProjet: ProjetResponse | null = null;

  // Modal de démarrage de projet
  showStartProjectModal = false;
  projetToStart: ProjetResponse | null = null;

  // Modal de dépôt
  depotModalMode: 'create' | 'view' = 'create';
  depotModalDepots: any[] = [];

  isDarkMode = false;
  private observer: MutationObserver | null = null;

  // Menu déroulant
  openMenuId: number | null = null;

  // Statut du projet en édition
  projetStatutEnEdition: string = '';

  constructor(
    private projetService: ProjetService,
    private clientService: ClientService,
    private utilisateurService: UtilisateurService,
    private fb: FormBuilder,
    private router: Router
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
      progression: [0],
      activites: this.fb.array([])
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

      // Vérification côté client : si date limite dépassée et statut != TERMINE, afficher en retard
      const estEnRetard = joursRestants < 0 && projet.statut !== StatutProjet.TERMINE;

      // Utiliser le statut du backend et calculer les projets en retard
      switch (projet.statut) {
        case StatutProjet.NOUVEAU:
          // Filet de sécurité : si en retard, afficher dans EN_RETARD
          if (estEnRetard) {
            this.projetsEnRetard.push(projet);
          } else {
            this.nouveauxProjets.push(projet);
          }
          break;
        case StatutProjet.EN_COURS:
          // Vérifier si le projet est en retard
          if (estEnRetard) {
            this.projetsEnRetard.push(projet);
          } else {
            this.projetsEnCours.push(projet);
          }
          break;
        case StatutProjet.TERMINE:
          this.projetsTermines.push(projet);
          break;
        case StatutProjet.EN_RETARD:
          this.projetsEnRetard.push(projet);
          break;
        default:
          // Cas par défaut pour les anciens projets sans statut
          if (projet.progression === 100) {
            projet.statut = StatutProjet.TERMINE;
            this.projetsTermines.push(projet);
          } else if (estEnRetard) {
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
    (this.createForm.get('activites') as FormArray).clear();
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

  // ---- ACTIVITÉS ----
  get activites(): FormArray {
    return this.createForm.get('activites') as FormArray || this.fb.array([]);
  }

  newActivite(): FormGroup {
    const activityIndex = this.activites.length;
    const group = this.fb.group({
      nom:          ['', Validators.required],
      description:  [''],
      dateDebut:    [''],
      dateFin:      [''],
      employes:     [[]],
      taches:       this.fb.array([])
    });

    // Ajouter le validateur personnalisé pour les dates
    group.setValidators(this.activityDateRangeValidator(activityIndex));
    return group;
  }

  addActivite(): void {
    this.activites.push(this.newActivite());
  }

  removeActivite(i: number): void {
    this.activites.removeAt(i);
  }

  // ---- TÂCHES ----
  getTaches(i: number): FormArray {
    if (i >= this.activites.length || i < 0) {
      return this.fb.array([]);
    }
    const activityGroup = this.activites.at(i) as FormGroup;
    if (!activityGroup) {
      return this.fb.array([]);
    }
    return activityGroup.get('taches') as FormArray || this.fb.array([]);
  }

  newTache(activityIndex: number): FormGroup {
    const taskIndex = this.getTaches(activityIndex).length;
    const group = this.fb.group({
      nom:         ['', Validators.required],
      description: [''],
      dateDebut:   [''],
      dateFin:     [''],
      employes:    [[]]
    });

    // Ajouter le validateur personnalisé pour les dates
    group.setValidators(this.taskDateRangeValidator(activityIndex, taskIndex));
    return group;
  }

  addTache(i: number): void {
    this.getTaches(i).push(this.newTache(i));
  }

  removeTache(i: number, j: number): void {
    this.getTaches(i).removeAt(j);
  }

  // ---- VALIDATEURS PERSONNALISÉS ----
  private activityDateRangeValidator(activityIndex: number) {
    return (control: AbstractControl): ValidationErrors | null => {
      const activityGroup = control as FormGroup;
      const dateDebut = activityGroup.get('dateDebut')?.value;
      const dateFin = activityGroup.get('dateFin')?.value;
      const projetDateDebut = this.createForm.get('dateDebut')?.value;
      const projetDateFin = this.createForm.get('dateLimite')?.value;

      const errors: ValidationErrors = {};

      // Validation: startDate >= project.startDate
      if (dateDebut && projetDateDebut && dateDebut < projetDateDebut) {
        errors['activityStartBeforeProject'] = {
          projectStartDate: projetDateDebut,
          activityStartDate: dateDebut
        };
      }

      // Validation: endDate <= project.endDate
      if (dateFin && projetDateFin && dateFin > projetDateFin) {
        errors['activityEndAfterProject'] = {
          projectEndDate: projetDateFin,
          activityEndDate: dateFin
        };
      }

      // Validation: startDate < endDate
      if (dateDebut && dateFin && dateDebut >= dateFin) {
        errors['activityStartAfterEnd'] = true;
      }

      return Object.keys(errors).length > 0 ? errors : null;
    };
  }

  private taskDateRangeValidator(activityIndex: number, taskIndex: number) {
    return (control: AbstractControl): ValidationErrors | null => {
      const taskGroup = control as FormGroup;
      const taskDateDebut = taskGroup.get('dateDebut')?.value;
      const taskDateFin = taskGroup.get('dateFin')?.value;

      // Get parent activity dates
      const activityGroup = this.activites.at(activityIndex) as FormGroup;
      const activityDateDebut = activityGroup.get('dateDebut')?.value;
      const activityDateFin = activityGroup.get('dateFin')?.value;

      const errors: ValidationErrors = {};

      // Validation: task.startDate >= activity.startDate
      if (taskDateDebut && activityDateDebut && taskDateDebut < activityDateDebut) {
        errors['taskStartBeforeActivity'] = {
          activityStartDate: activityDateDebut,
          taskStartDate: taskDateDebut
        };
      }

      // Validation: task.endDate <= activity.endDate
      if (taskDateFin && activityDateFin && taskDateFin > activityDateFin) {
        errors['taskEndAfterActivity'] = {
          activityEndDate: activityDateFin,
          taskEndDate: taskDateFin
        };
      }

      // Validation: startDate < endDate
      if (taskDateDebut && taskDateFin && taskDateDebut >= taskDateFin) {
        errors['taskStartAfterEnd'] = true;
      }

      return Object.keys(errors).length > 0 ? errors : null;
    };
  }

  // ---- MESSAGES D'ERREUR ----
  getActivityErrorMessage(activityIndex: number, errorType: string): string {
    const projetDateDebut = this.createForm.get('dateDebut')?.value;
    const projetDateFin = this.createForm.get('dateLimite')?.value;

    switch (errorType) {
      case 'activityStartBeforeProject':
        return `La date de début doit être supérieure ou égale à la date de début du projet (${this.formatDate(projetDateDebut)})`;
      case 'activityEndAfterProject':
        return `La date de fin doit être inférieure ou égale à la date de fin du projet (${this.formatDate(projetDateFin)})`;
      case 'activityStartAfterEnd':
        return 'La date de début doit être antérieure à la date de fin';
      default:
        return 'Date invalide';
    }
  }

  getTaskErrorMessage(activityIndex: number, taskIndex: number, errorType: string): string {
    const activityGroup = this.activites.at(activityIndex) as FormGroup;
    const activityDateDebut = activityGroup.get('dateDebut')?.value;
    const activityDateFin = activityGroup.get('dateFin')?.value;

    switch (errorType) {
      case 'taskStartBeforeActivity':
        return `La date de début doit être supérieure ou égale à la date de début de l'activité (${this.formatDate(activityDateDebut)})`;
      case 'taskEndAfterActivity':
        return `La date de fin doit être inférieure ou égale à la date de fin de l'activité (${this.formatDate(activityDateFin)})`;
      case 'taskStartAfterEnd':
        return 'La date de début doit être antérieure à la date de fin';
      default:
        return 'Date invalide';
    }
  }

  formatDate(dateString: string): string {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleDateString('fr-FR', { day: '2-digit', month: '2-digit', year: 'numeric' });
  }

  // ---- GETTERS MIN/MAX DATES ----
  getActivityMinDate(): string {
    return this.createForm.get('dateDebut')?.value || '';
  }

  getActivityMaxDate(): string {
    return this.createForm.get('dateLimite')?.value || '';
  }

  getTaskMinDate(activityIndex: number): string {
    const activityGroup = this.activites.at(activityIndex) as FormGroup;
    return activityGroup.get('dateDebut')?.value || '';
  }

  getTaskMaxDate(activityIndex: number): string {
    const activityGroup = this.activites.at(activityIndex) as FormGroup;
    return activityGroup.get('dateFin')?.value || '';
  }

  // ---- EMPLOYÉS ----
  getEmployesDisponiblesPourActivite(i: number): UtilisateurResponse[] {
    return this.employesSelectionnes;
  }

  getEmployesDisponiblesPourTache(i: number): UtilisateurResponse[] {
    const activiteEmployes = ((this.activites.at(i) as FormGroup)
      .get('employes')?.value || []) as number[];

    return this.employesSelectionnes.filter(emp =>
      activiteEmployes.includes(emp.id)
    );
  }

  isActiviteEmployeChecked(i: number, empId: number): boolean {
    return ((this.activites.at(i) as FormGroup)
      .get('employes')?.value || []).includes(empId);
  }

  onActiviteEmployeToggle(i: number, empId: number, e: any): void {
    const ctrl = (this.activites.at(i) as FormGroup).get('employes')!;
    let ids: number[] = ctrl.value || [];
    ids = e.target.checked
      ? [...ids, empId]
      : ids.filter(id => id !== empId);
    ctrl.setValue(ids);
  }

  isTacheEmployeChecked(i: number, j: number, empId: number): boolean {
    return ((this.getTaches(i).at(j) as FormGroup)
      .get('employes')?.value || []).includes(empId);
  }

  onTacheEmployeToggle(i: number, j: number, empId: number, e: any): void {
    const ctrl = (this.getTaches(i).at(j) as FormGroup).get('employes')!;
    let ids: number[] = ctrl.value || [];
    ids = e.target.checked
      ? [...ids, empId]
      : ids.filter(id => id !== empId);
    ctrl.setValue(ids);
  }

  // Fonction de conversion des dates en format yyyy-MM-dd pour le backend
  formatDateForBackend(date: any): string | null {
    if (!date) return null;
    if (typeof date === 'string') return date;
    const d = new Date(date);
    return d.toISOString().split('T')[0];
  }

  saveProjet(): void {
    if (this.createForm.invalid || this.selectedEmployes.length === 0) {
      this.errorMessage = 'Veuillez remplir tous les champs obligatoires';
      return;
    }

    this.errorMessage = '';

    // Convertir les dates du formulaire
    const formValue = this.createForm.value;
    
    // Déterminer si on doit inclure les activités dans le payload
    const shouldIncludeActivites = !this.isEditMode || this.projetStatutEnEdition !== 'NOUVEAU';
    
    const payload: ProjetRequest = {
      ...formValue,
      dateDebut: this.formatDateForBackend(formValue.dateDebut),
      dateLimite: this.formatDateForBackend(formValue.dateLimite),
      progression: formValue.progression || 0,
      employeIds: this.selectedEmployes,
      activites: shouldIncludeActivites && this.activites.length > 0 
        ? this.activites.value.map((act: any) => ({
            nom: act.nom,
            description: act.description,
            dateDebut: this.formatDateForBackend(act.dateDebut),
            dateFin: this.formatDateForBackend(act.dateFin),
            employeIds: act.employes || [],
            estDeposé: false,
            taches: (act.taches || []).map((t: any) => ({
              nom: t.nom,
              description: t.description,
              dateDebut: this.formatDateForBackend(t.dateDebut),
              dateFin: this.formatDateForBackend(t.dateFin),
              employeIds: t.employes || [],
              estDeposé: false
            }))
          }))
        : []
    };

    console.log('PAYLOAD ENVOYÉ:', JSON.stringify(payload, null, 2));

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
        next: () => {
          this.loadProjets();
          this.closeModal();
        },
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
        return { text: 'Dépôt', color: '#6b7280', action: 'DEPOT_VIEW' };
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
        
        // Stocker le statut du projet en édition
        this.projetStatutEnEdition = details.statut || '';

        // Vider le FormArray activités (dans tous les cas)
        (this.createForm.get('activites') as FormArray).clear();

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
    this.router.navigate(['/admin/projets', projet.id, 'depot']);
  }

  closeDepotModal(): void {
    this.showDepotModal = false;
    this.projetPourDepot = null;
  }

  onDepotSubmitted(depotData: { type: 'lien' | 'fichier', value: string | File }): void {
    if (!this.projetPourDepot) return;

    // Appeler l'API pour déposer le projet
    this.projetService.deposerProjet(this.projetPourDepot.id, depotData).subscribe({
      next: () => {
        console.log('Projet déposé avec succès');
        this.closeDepotModal();
        this.loadProjets();
      },
      error: (err: any) => {
        console.error('Erreur lors du dépôt du projet:', err);
        this.errorMessage = err.error?.message || 'Erreur lors du dépôt du projet';
      }
    });
  }

  // Menu déroulant
  toggleMenu(projetId: number, event: MouseEvent): void {
    event.stopPropagation();
    this.openMenuId = this.openMenuId === projetId ? null : projetId;
  }

  closeMenu(): void {
    this.openMenuId = null;
  }

  @HostListener('document:click')
  onDocumentClick(): void {
    this.openMenuId = null;
  }
}