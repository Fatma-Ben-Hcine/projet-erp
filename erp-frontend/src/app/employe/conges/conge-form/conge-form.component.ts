import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { CongeService } from '../../../core/services/conge.service';
import { Conge, CongeRequest, TypeConge, StatutConge } from '../../../core/models/conge.model';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { EmployeSidebarComponent } from '../../shared/sidebar/sidebar.component';

@Component({
  selector: 'app-conge-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, LoadingSpinnerComponent, EmployeSidebarComponent],
  templateUrl: './conge-form.component.html',
  styleUrls: ['./conge-form.component.css']
})
export class CongeFormComponent implements OnInit {
  congeForm: FormGroup;
  isLoading = false;
  isEditMode = false;
  congeId: number | null = null;
  errorMessage = '';
  successMessage = '';

  typeCongeOptions = [
    { value: TypeConge.ANNUEL, label: 'Congé Annuel' },
    { value: TypeConge.MALADIE, label: 'Maladie' },
    { value: TypeConge.MATERNITE, label: 'Maternité' },
    { value: TypeConge.PATERNITE, label: 'Paternité' },
    { value: TypeConge.SANS_SOLDE, label: 'Sans Solde' },
    { value: TypeConge.FORMATION, label: 'Formation' },
    { value: TypeConge.DECES, label: 'Décès' },
    { value: TypeConge.MARIAGE, label: 'Mariage' }
  ];

  constructor(
    private fb: FormBuilder,
    private congeService: CongeService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.congeForm = this.createForm();
  }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode = true;
      this.congeId = parseInt(id, 10);
      this.loadConge();
    }
  }

  private createForm(): FormGroup {
    const today = new Date().toISOString().split('T')[0];
    return this.fb.group({
      typeConge: [TypeConge.ANNUEL, Validators.required],
      dateDebut: ['', [Validators.required, this.minDateValidator(today)]],
      dateFin: ['', [Validators.required, this.minDateValidator(today)]]
    }, { validators: this.dateRangeValidator });
  }

  private minDateValidator(minDate: string) {
    return (control: any) => {
      const value = control.value;
      if (!value) return null;
      return value >= minDate ? null : { minDate: true };
    };
  }

  private dateRangeValidator(group: FormGroup): { [key: string]: any } | null {
    const dateDebut = group.get('dateDebut')?.value;
    const dateFin = group.get('dateFin')?.value;

    if (!dateDebut || !dateFin) {
      return null;
    }

    return dateFin < dateDebut ? { dateRange: true } : null;
  }

  loadConge(): void {
    if (!this.congeId) return;

    this.isLoading = true;
    this.errorMessage = '';

    // For edit mode, we need to get the specific conge
    // Since we don't have a getCongeById endpoint, we'll use the employee list and filter
    this.congeService.getMesConges().subscribe({
      next: (conges: any[]) => {
        const conge = conges.find((c: any) => c.id === this.congeId);
        if (conge) {
          this.congeForm.patchValue({
            typeConge: conge.typeConge,
            dateDebut: conge.dateDebut,
            dateFin: conge.dateFin
          });
        } else {
          this.errorMessage = 'Congé non trouvé';
        }
        this.isLoading = false;
      },
      error: (error: any) => {
        console.error('Error loading conge:', error);
        this.errorMessage = 'Erreur lors du chargement du congé';
        this.isLoading = false;
      }
    });
  }

  getTypeCongeLabel(type: TypeConge): string {
    return this.congeService.getTypeCongeLabel(type);
  }

  onSubmit(): void {
    if (this.congeForm.invalid) {
      this.markFormAsTouched();
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';

    const congeRequest: CongeRequest = this.congeForm.value;

    if (this.isEditMode && this.congeId) {
      // Update existing conge
      this.congeService.modifierConge(this.congeId, congeRequest).subscribe({
        next: () => {
          this.successMessage = 'Congé modifié avec succès';
          setTimeout(() => {
            this.router.navigate(['/employe/conges']);
          }, 1500);
        },
        error: (error) => {
          console.error('Error updating conge:', error);
          this.errorMessage = 'Erreur lors de la modification du congé';
          this.isLoading = false;
        }
      });
    } else {
      // Create new conge
      this.congeService.demanderConge(congeRequest).subscribe({
        next: () => {
          this.successMessage = 'Demande de congé envoyée avec succès';
          setTimeout(() => {
            this.router.navigate(['/employe/conges']);
          }, 1500);
        },
        error: (error) => {
          console.error('Error creating conge:', error);
          this.errorMessage = 'Erreur lors de la création de la demande de congé';
          this.isLoading = false;
        }
      });
    }
  }

  onCancel(): void {
    this.router.navigate(['/employe/conges']);
  }

  clearMessages(): void {
    this.errorMessage = '';
    this.successMessage = '';
  }

  private markFormAsTouched(): void {
    Object.values(this.congeForm.controls).forEach(control => {
      control.markAsTouched();
    });
  }

  get formControls() {
    return this.congeForm.controls;
  }

  get dateRangeError(): boolean {
    return this.congeForm.hasError('dateRange');
  }

  get duration(): number {
    const dateDebut = this.congeForm.get('dateDebut')?.value;
    const dateFin = this.congeForm.get('dateFin')?.value;
    
    if (dateDebut && dateFin && !this.dateRangeError) {
      return this.congeService.calculateDuration(dateDebut, dateFin);
    }
    return 0;
  }
}
