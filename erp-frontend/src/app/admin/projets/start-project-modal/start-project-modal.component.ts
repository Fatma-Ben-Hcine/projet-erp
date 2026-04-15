import { Component, Input, Output, EventEmitter, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, FormArray, ReactiveFormsModule } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { ActiviteService } from '../../../core/services/activite.service';
import { TacheService } from '../../../core/services/tache.service';

@Component({
  selector: 'app-start-project-modal',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './start-project-modal.component.html',
  styleUrls: ['./start-project-modal.component.scss'],
  providers: [DatePipe]
})
export class StartProjectModalComponent implements OnChanges {
  @Input() projet: any;
  @Input() isVisible: boolean = false;
  @Output() closed = new EventEmitter();
  @Output() projectStarted = new EventEmitter();

  form!: FormGroup;

  constructor(private fb: FormBuilder, 
              private activiteService: ActiviteService,
              private tacheService: TacheService) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['projet'] && this.projet) {
      this.initForm();
    }
  }

  initForm(): void {
    this.form = this.fb.group({
      activites: this.fb.array([this.newActivite()])
    });
  }

  // ---- ACTIVITÉS ----
  get activites(): FormArray {
    return this.form.get('activites') as FormArray;
  }

  newActivite(): FormGroup {
    return this.fb.group({
      nom:          ['', Validators.required],
      description:  [''],
      dateDebut:    [''],
      dateFin:      [''],
      employes:     [[]],
      taches:       this.fb.array([])
    });
  }

  addActivite(): void {
    this.activites.push(this.newActivite());
  }

  removeActivite(i: number): void {
    this.activites.removeAt(i);
  }

  // ---- TÂCHES ----
  getTaches(i: number): FormArray {
    return (this.activites.at(i) as FormGroup)
      .get('taches') as FormArray;
  }

  newTache(): FormGroup {
    return this.fb.group({
      nom:         ['', Validators.required],
      description: [''],
      dateDebut:   [''],
      dateFin:     [''],
      employes:    [[]]
    });
  }

  addTache(i: number): void {
    this.getTaches(i).push(this.newTache());
  }

  removeTache(i: number, j: number): void {
    this.getTaches(i).removeAt(j);
  }

  // ---- EMPLOYÉS ----
  get employesDisponibles(): any[] {
    return this.projet?.employes || [];
  }

  // Récupérer les employés disponibles pour une tâche (filtrés par activité parente)
  getEmployesDisponiblesPourTache(i: number): any[] {
    const activiteEmployes = ((this.activites.at(i) as FormGroup)
      .get('employes')?.value || []) as number[];
    
    // Filtrer les employés du projet pour ne garder que ceux assignés à l'activité
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

  // ---- SOUMISSION ----
  async onSubmit(): Promise<void> {
    if (this.form.invalid) return;
    
    for (const act of this.activites.value) {
      const activiteCreee = await this.activiteService.create({
        nom: act.nom,
        description: act.description,
        projetId: this.projet.id,
        dateDebut: act.dateDebut || new Date().toISOString().split('T')[0],
        dateFin: act.dateFin || null
      }).toPromise();

      if (!activiteCreee) continue;

      for (const empId of act.employes) {
        await this.activiteService
          .assignEmploye(activiteCreee.id, empId, { statut: 'EN_COURS', progression: 0 })
          .toPromise();
      }

      for (const tache of act.taches) {
        const tacheCreee = await this.tacheService.create({
          nom: tache.nom,
          description: tache.description,
          activiteId: activiteCreee.id,
          dateDebut: tache.dateDebut || new Date().toISOString().split('T')[0],
          dateFin: tache.dateFin || null
        }).toPromise();

        if (!tacheCreee) continue;

        for (const empId of tache.employes) {
          await this.tacheService
            .assignEmploye(tacheCreee.id, empId, { statut: 'EN_COURS' })
            .toPromise();
        }
      }
    }

    this.projectStarted.emit();
    this.closed.emit();
  }

  close(): void {
    this.closed.emit();
  }
}
