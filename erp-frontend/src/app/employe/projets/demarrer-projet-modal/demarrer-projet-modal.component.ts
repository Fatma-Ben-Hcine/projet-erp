import { Component, Input, Output, EventEmitter, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, FormArray, ReactiveFormsModule, AbstractControl, ValidationErrors } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { EmployeActiviteService } from '../../../core/services/employe-activite.service';
import { EmployeTacheService } from '../../../core/services/employe-tache.service';
import { EmployeProjetService } from '../../../core/services/employe-projet.service';

@Component({
  selector: 'app-demarrer-projet-modal',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './demarrer-projet-modal.component.html',
  styleUrls: ['./demarrer-projet-modal.component.scss'],
  providers: [DatePipe]
})
export class DemarrerProjetModalComponent implements OnChanges {
  @Input() projet: any;
  @Input() isVisible: boolean = false;
  @Output() closed = new EventEmitter();
  @Output() projectStarted = new EventEmitter();

  form!: FormGroup;
  errorMessage: string = '';

  constructor(
    private fb: FormBuilder,
    private activiteService: EmployeActiviteService,
    private tacheService: EmployeTacheService,
    private projetService: EmployeProjetService
  ) {}

  // Validateurs personnalisés
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

  ngOnChanges(changes: SimpleChanges): void {
    console.log('DemarrerProjetModal ngOnChanges:', changes);
    console.log('isVisible:', this.isVisible);
    console.log('projet:', this.projet);

    if (changes['projet'] && this.projet) {
      console.log('Initializing form for projet:', this.projet);
      this.initForm();
    }
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

  async onSubmit(): Promise<void> {
    if (this.form.invalid) return;

    this.errorMessage = '';

    try {
      for (const act of this.activites.value) {
        const activiteCreee = await this.activiteService.create({
          nom: act.nom,
          description: act.description,
          projetId: this.projet.id,
          dateDebut: act.dateDebut || new Date().toISOString().split('T')[0],
          dateFin: act.dateFin || null,
          estDeposé: false
        }).toPromise();

        if (!activiteCreee) continue;

        for (const empId of act.employes) {
          await this.activiteService
            .assignEmploye(activiteCreee.id, empId)
            .toPromise();
        }

        for (const tache of act.taches) {
          const tacheCreee = await this.tacheService.create({
            nom: tache.nom,
            description: tache.description,
            activiteId: activiteCreee.id,
            dateDebut: tache.dateDebut || new Date().toISOString().split('T')[0],
            dateFin: tache.dateFin || null,
            estDeposé: false
          }).toPromise();

          if (!tacheCreee) continue;

          for (const empId of tache.employes) {
            await this.tacheService
              .assignEmploye(tacheCreee.id, empId, { statut: 'EN_COURS' })
              .toPromise();
          }
        }
      }

      // Changer le statut du projet en EN_COURS
      await this.projetService.updateStatut(this.projet.id, 'EN_COURS').toPromise();

      this.projectStarted.emit();
      this.closed.emit();
    } catch (error: any) {
      if (error.status === 400 && error.error?.message) {
        this.errorMessage = error.error.message;
      } else if (typeof error.error === 'string' && error.error.length > 0) {
        this.errorMessage = error.error;
      } else {
        this.errorMessage = 'Erreur lors de la création des activités et tâches. Veuillez vérifier les dates et réessayer.';
      }
      console.error('Erreur lors de la soumission:', error);
    }
  }

  close(): void {
    this.closed.emit();
  }
}
