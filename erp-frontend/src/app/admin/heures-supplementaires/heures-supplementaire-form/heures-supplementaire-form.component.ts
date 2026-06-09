import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { HeureSupplementaireService } from '../../../core/services/heure-supplementaire.service';
import { HeureSupplementaire, HeureSupplementaireRequest, StatutHeureSupplementaire } from '../../../core/models/heure-supplementaire.model';
import { UtilisateurService } from '../../../core/services/utilisateur.service';
import { UtilisateurResponse } from '../../../core/models/utilisateur.model';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { AdminSidebarComponent } from '../../shared/sidebar/sidebar.component';

@Component({
  selector: 'app-heures-supplementaire-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, LoadingSpinnerComponent, AdminSidebarComponent],
  templateUrl: './heures-supplementaire-form.component.html',
  styleUrls: ['./heures-supplementaire-form.component.css']
})
export class HeuresSupplementaireFormComponent implements OnInit {
  heureSupplementaireForm: FormGroup;
  isLoading = false;
  isEditMode = false;
  heureSupplementaireId: number | null = null;
  errorMessage = '';
  successMessage = '';

  statutOptions = [
    { value: StatutHeureSupplementaire.EN_ATTENTE, label: 'En Attente' },
    { value: StatutHeureSupplementaire.APPROUVEE, label: 'Approuvée' },
    { value: StatutHeureSupplementaire.REFUSEE, label: 'Refusée' }
  ];

  // Liste des employés chargée dynamiquement depuis la base de données
  employes: UtilisateurResponse[] = [];
  isLoadingEmployees = false;

  constructor(
    private fb: FormBuilder,
    private heureSupplementaireService: HeureSupplementaireService,
    private utilisateurService: UtilisateurService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.heureSupplementaireForm = this.createForm();
  }

  ngOnInit(): void {
    this.loadEmployees();
    
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode = true;
      this.heureSupplementaireId = parseInt(id, 10);
      this.loadHeureSupplementaire();
    }
  }

  loadEmployees(): void {
    this.isLoadingEmployees = true;
    // Désactiver le contrôle employeId pendant le chargement
    this.employeId?.disable();
    
    // Charger tous les employés
    this.utilisateurService.getAll().subscribe({
      next: (employes) => {
        this.employes = employes.filter(e => e.actif && e.role === 'ROLE_EMPLOYE');
        this.isLoadingEmployees = false;
        // Réactiver le contrôle employeId après le chargement
        this.employeId?.enable();
      },
      error: (error) => {
        console.error('Error loading employees:', error);
        this.errorMessage = 'Erreur lors du chargement des employés';
        this.isLoadingEmployees = false;
        // Réactiver le contrôle employeId en cas d'erreur
        this.employeId?.enable();
      }
    });
  }

  private createForm(): FormGroup {
    return this.fb.group({
      date: ['', [Validators.required]],
      nombreHeures: ['', [Validators.required, Validators.min(0.1), Validators.max(24)]],
      mission: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
      statut: [StatutHeureSupplementaire.EN_ATTENTE],
      tarifHeuresSupp: ['', [Validators.required, Validators.min(0)]],
      employeId: ['', [Validators.required]]
    });
  }

  loadHeureSupplementaire(): void {
    if (!this.heureSupplementaireId) return;

    this.isLoading = true;
    this.errorMessage = '';

    this.heureSupplementaireService.getById(this.heureSupplementaireId).subscribe({
      next: (heureSupplementaire) => {
        this.heureSupplementaireForm.patchValue({
          date: heureSupplementaire.date,
          nombreHeures: heureSupplementaire.nombreHeures,
          mission: heureSupplementaire.mission,
          statut: heureSupplementaire.statut,
          tarifHeuresSupp: heureSupplementaire.tarifHeuresSupp,
          employeId: heureSupplementaire.employeId
        });
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading heure supplementaire:', error);
        this.errorMessage = 'Erreur lors du chargement de l\'heure supplémentaire';
        this.isLoading = false;
      }
    });
  }

  getEmployeName(employeId: number): string {
    const employe = this.employes.find(e => e.id === employeId);
    return employe ? `${employe.prenom} ${employe.nom}` : 'Non spécifié';
  }

  getStatutLabel(statut: StatutHeureSupplementaire): string {
    return this.heureSupplementaireService.getStatutLabel(statut);
  }

  onSubmit(): void {
    if (this.heureSupplementaireForm.invalid) {
      this.markFormAsTouched();
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';

    const formValue = this.heureSupplementaireForm.value;
    
    // Log pour déboguer le payload
    console.log('Form value:', formValue);
    
    // Créer le payload correct pour le backend
    const heureSupplementaireRequest: HeureSupplementaireRequest = {
      date: formValue.date,
      nombreHeures: formValue.nombreHeures,
      mission: formValue.mission,
      statut: formValue.statut,
      tarifHeuresSupp: formValue.tarifHeuresSupp,
      employeId: formValue.employeId
    };
    
    // Log du payload final
    console.log('Payload to send:', heureSupplementaireRequest);

    if (this.isEditMode && this.heureSupplementaireId) {
      // Update existing heure supplementaire
      this.heureSupplementaireService.update(this.heureSupplementaireId, heureSupplementaireRequest).subscribe({
        next: () => {
          this.successMessage = 'Heure supplémentaire modifiée avec succès';
          setTimeout(() => {
            this.router.navigate(['/admin/heures-supplementaires']);
          }, 1500);
        },
        error: (error) => {
          console.error('Error updating heure supplementaire:', error);
          this.errorMessage = 'Erreur lors de la modification de l\'heure supplémentaire';
          this.isLoading = false;
        }
      });
    } else {
      // Create new heure supplementaire
      this.heureSupplementaireService.create(heureSupplementaireRequest).subscribe({
        next: () => {
          this.successMessage = 'Heure supplémentaire créée avec succès';
          setTimeout(() => {
            this.router.navigate(['/admin/heures-supplementaires']);
          }, 1500);
        },
        error: (error) => {
          console.error('Error creating heure supplementaire:', error);
          this.errorMessage = 'Erreur lors de la création de l\'heure supplémentaire';
          this.isLoading = false;
        }
      });
    }
  }

  onCancel(): void {
    this.router.navigate(['/admin/heures-supplementaires']);
  }

  private markFormAsTouched(): void {
    Object.keys(this.heureSupplementaireForm.controls).forEach(key => {
      const control = this.heureSupplementaireForm.get(key);
      control?.markAsTouched();
    });
  }

  // Getters for form validation
  get date() { return this.heureSupplementaireForm.get('date'); }
  get nombreHeures() { return this.heureSupplementaireForm.get('nombreHeures'); }
  get mission() { return this.heureSupplementaireForm.get('mission'); }
  get statut() { return this.heureSupplementaireForm.get('statut'); }
  get tarifHeuresSupp() { return this.heureSupplementaireForm.get('tarifHeuresSupp'); }
  get employeId() { return this.heureSupplementaireForm.get('employeId'); }

  // Helper methods for validation messages
  getErrorMessage(field: any): string {
    if (field?.errors?.required) {
      return 'Ce champ est obligatoire';
    }
    
    if (field?.errors?.min) {
      return `La valeur minimale est ${field.errors.min.min}`;
    }
    if (field?.errors?.max) {
      return `La valeur maximale est ${field.errors.max.max}`;
    }
    if (field?.errors?.minlength) {
      return `Minimum ${field.errors.minlength.requiredLength} caractères`;
    }
    if (field?.errors?.maxlength) {
      return `Maximum ${field.errors.maxlength.requiredLength} caractères`;
    }
    return '';
  }

  calculateTotalAmount(): number {
    const nombreHeures = this.heureSupplementaireForm.get('nombreHeures')?.value || 0;
    const tarif = this.heureSupplementaireForm.get('tarifHeuresSupp')?.value || 0;
    return nombreHeures * tarif;
  }
}
