import { Component, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormBuilder, FormGroup, Validators, FormArray, ReactiveFormsModule, AbstractControl, ValidationErrors } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { EmployeSidebarComponent } from '../../shared/sidebar/sidebar.component';
import { EmployeProjetService } from '../../../core/services/employe-projet.service';
import { EmployeActiviteService } from '../../../core/services/employe-activite.service';
import { EmployeTacheService } from '../../../core/services/employe-tache.service';
import { AuthService } from '../../../auth/auth.service';
import { ProjetResponse } from '../../../core/models/projet.model';

@Component({
  selector: 'app-employe-projet-start',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule, EmployeSidebarComponent],
  templateUrl: './projet-start.component.html',
  styleUrls: ['./projet-start.component.css'],
  providers: [DatePipe]
})
export class EmployeProjetStartComponent implements OnInit {
  projet: ProjetResponse | null = null;
  form!: FormGroup;
  errorMessage: string = '';
  isLoading = true;
  isChefDeProjet = false;
  currentUserId: number | null = null;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private employeProjetService: EmployeProjetService,
    private employeActiviteService: EmployeActiviteService,
    private employeTacheService: EmployeTacheService,
    private authService: AuthService,
    private datePipe: DatePipe
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

    this.employeProjetService.getById(id).subscribe({
      next: (data) => {
        this.projet = data;
        this.checkChefDeProjet();
        this.initForm();
        this.isLoading = false;
      },
      error: (err: any) => {
        this.errorMessage = 'Erreur lors du chargement du projet';
        this.isLoading = false;
      }
    });
  }

  checkChefDeProjet(): void {
    if (!this.projet || !this.currentUserId) {
      this.isChefDeProjet = false;
      this.router.navigate(['/employe/projets', this.projet?.id, 'details']);
      return;
    }
    this.employeProjetService.isChefDeProjet(this.projet.id).subscribe({
      next: (isChef) => {
        this.isChefDeProjet = isChef;
        if (!isChef) {
          this.router.navigate(['/employe/projets', this.projet!.id, 'details']);
        }
      },
      error: () => {
        this.isChefDeProjet = false;
        this.router.navigate(['/employe/projets', this.projet!.id, 'details']);
      }
    });
  }

  initForm(): void {
    this.form = this.fb.group({
      activites: this.fb.array([this.newActivite()])
    });
  }

  get activites(): FormArray {
    if (!this.form) {
      return this.fb.array([]);
    }
    return this.form.get('activites') as FormArray || this.fb.array([]);
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

    group.setValidators(this.activityDateRangeValidator(activityIndex));
    return group;
  }

  addActivite(): void {
    if (!this.form) {
      this.initForm();
    }
    this.activites.push(this.newActivite());
  }

  removeActivite(i: number): void {
    this.activites.removeAt(i);
  }

  getTaches(i: number): FormArray {
    if (!this.form || i >= this.activites.length || i < 0) {
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

    group.setValidators(this.taskDateRangeValidator(activityIndex, taskIndex));
    return group;
  }

  addTache(i: number): void {
    this.getTaches(i).push(this.newTache(i));
  }

  removeTache(i: number, j: number): void {
    this.getTaches(i).removeAt(j);
  }

  private activityDateRangeValidator(activityIndex: number) {
    return (control: AbstractControl): ValidationErrors | null => {
      if (!this.projet) return null;

      const activityGroup = control as FormGroup;
      const dateDebut = activityGroup.get('dateDebut')?.value;
      const dateFin = activityGroup.get('dateFin')?.value;
      const projetDateDebut = this.projet.dateDebut;
      const projetDateFin = this.projet.dateLimite;

      const errors: ValidationErrors = {};

      if (dateDebut && projetDateDebut && dateDebut < projetDateDebut) {
        errors['activityStartBeforeProject'] = {
          projectStartDate: projetDateDebut,
          activityStartDate: dateDebut
        };
      }

      if (dateFin && projetDateFin && dateFin > projetDateFin) {
        errors['activityEndAfterProject'] = {
          projectEndDate: projetDateFin,
          activityEndDate: dateFin
        };
      }

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

      const activityGroup = this.activites.at(activityIndex) as FormGroup;
      const activityDateDebut = activityGroup.get('dateDebut')?.value;
      const activityDateFin = activityGroup.get('dateFin')?.value;

      const errors: ValidationErrors = {};

      if (taskDateDebut && activityDateDebut && taskDateDebut < activityDateDebut) {
        errors['taskStartBeforeActivity'] = {
          activityStartDate: activityDateDebut,
          taskStartDate: taskDateDebut
        };
      }

      if (taskDateFin && activityDateFin && taskDateFin > activityDateFin) {
        errors['taskEndAfterActivity'] = {
          activityEndDate: activityDateFin,
          taskEndDate: taskDateFin
        };
      }

      if (taskDateDebut && taskDateFin && taskDateDebut >= taskDateFin) {
        errors['taskStartAfterEnd'] = true;
      }

      return Object.keys(errors).length > 0 ? errors : null;
    };
  }

  getActivityErrorMessage(activityIndex: number, errorType: string): string {
    if (!this.projet) return '';

    switch (errorType) {
      case 'activityStartBeforeProject':
        return `La date de début doit être supérieure ou égale à la date de début du projet (${this.formatDate(this.projet.dateDebut)})`;
      case 'activityEndAfterProject':
        return `La date de fin doit être inférieure ou égale à la date de fin du projet (${this.formatDate(this.projet.dateLimite)})`;
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

  private formatDate(dateString: string): string {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleDateString('fr-FR', { day: '2-digit', month: '2-digit', year: 'numeric' });
  }

  getActivityMinDate(): string {
    return this.projet?.dateDebut || '';
  }

  getActivityMaxDate(): string {
    return this.projet?.dateLimite || '';
  }

  getTaskMinDate(activityIndex: number): string {
    const activityGroup = this.activites.at(activityIndex) as FormGroup;
    return activityGroup.get('dateDebut')?.value || '';
  }

  getTaskMaxDate(activityIndex: number): string {
    const activityGroup = this.activites.at(activityIndex) as FormGroup;
    return activityGroup.get('dateFin')?.value || '';
  }

  get employesDisponibles(): any[] {
    return this.projet?.employes || [];
  }

  getEmployesDisponiblesPourTache(i: number): any[] {
    const activiteEmployes = ((this.activites.at(i) as FormGroup)
      .get('employes')?.value || []) as number[];

    return this.projet?.employes?.filter((emp: any) =>
      activiteEmployes.includes(emp.id)
    ) || [];
  }

  isChef(empId: number): boolean {
    return this.projet?.chefDeProjet?.id === empId;
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

  goBack(): void {
    this.router.navigate(['/employe/dashboard']);
  }

  async onSubmit(): Promise<void> {
    if (this.form.invalid) return;

    this.errorMessage = '';

    try {
      for (const act of this.activites.value) {
        const activiteCreee = await this.employeActiviteService.create({
          nom: act.nom,
          description: act.description,
          projetId: this.projet!.id,
          dateDebut: act.dateDebut || new Date().toISOString().split('T')[0],
          dateFin: act.dateFin || null,
          estDeposé: false,
          employeIds: []
        }).toPromise();

        if (!activiteCreee) continue;

        for (const empId of act.employes) {
          await this.employeActiviteService
            .assignEmploye(activiteCreee.id, empId, { statut: 'EN_COURS', progression: 0 })
            .toPromise();
        }

        for (const tache of act.taches) {
          const tacheCreee = await this.employeTacheService.create({
            nom: tache.nom,
            description: tache.description,
            activiteId: activiteCreee.id,
            dateDebut: tache.dateDebut || new Date().toISOString().split('T')[0],
            dateFin: tache.dateFin || null,
            estDeposé: false,
            employeIds: []
          }).toPromise();

          if (!tacheCreee) continue;

          for (const empId of tache.employes) {
            await this.employeTacheService
              .assignEmploye(tacheCreee.id, empId, { statut: 'EN_COURS' })
              .toPromise();
          }
        }
      }

      this.employeProjetService.updateStatut(this.projet!.id, 'EN_COURS').subscribe({
        next: () => {
          this.router.navigate(['/employe/dashboard']);
        },
        error: (err: any) => {
          this.errorMessage = 'Erreur lors du démarrage du projet';
        }
      });
    } catch (error: any) {
      if (error.status === 400 && error.error?.message) {
        this.errorMessage = error.error.message;
      } else if (typeof error.error === 'string' && error.error.length > 0) {
        this.errorMessage = error.error;
      } else {
        this.errorMessage = 'Erreur lors de la création des activités et tâches. Veuillez vérifier les dates et réessayer.';
      }
    }
  }
}
