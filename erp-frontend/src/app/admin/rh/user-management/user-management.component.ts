import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { UtilisateurService } from '../../../core/services/utilisateur.service';
import { AuthService } from '../../../auth/auth.service';
import { StringToArrayPipe } from '../../../shared/pipes/string-to-array.pipe';
import { AdminSidebarComponent } from '../../shared/sidebar/sidebar.component';
import { UtilisateurResponse, CreateUtilisateurRequest, UpdateUtilisateurRequest } from '../../../core/models/utilisateur.model';

@Component({
  selector: 'app-user-management',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule, StringToArrayPipe, AdminSidebarComponent],
  templateUrl: './user-management.component.html',
  styleUrl: './user-management.component.scss'
})
export class UserManagementComponent implements OnInit {

  utilisateurs: UtilisateurResponse[] = [];
  filteredUtilisateurs: UtilisateurResponse[] = [];
  filter: 'all' | 'ROLE_ADMIN' | 'ROLE_EMPLOYE' = 'all';
  isLoading = false;
  errorMessage = '';
  showModal = false;
  isEditing = false;
  selectedUser: UtilisateurResponse | null = null;
  showDeleteModal = false;
  utilisateurASupprimer: UtilisateurResponse | null = null;
  createForm!: FormGroup;
  editForm!: FormGroup;

  // Photo upload variables
  createPhotoUrl: string = '';
  editPhotoUrl: string = '';
  isUploadingPhoto: boolean = false;
  photoErrorMessage: string = '';

  constructor(
    private utilisateurService: UtilisateurService,
    private fb: FormBuilder,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    console.log('Token:', this.authService.getToken());
    console.log('Role:', this.authService.getRole());
    console.log('IsAdmin:', this.authService.isAdmin());
    this.initForms();
    this.loadUtilisateurs();
  }

  initForms(): void {
    this.createForm = this.fb.group({
      nom: ['', Validators.required],
      prenom: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      motDePasse: ['', [Validators.required, Validators.minLength(6)]],
      CIN: ['', Validators.required],
      numeroTel: [''],
      poste: [''],
      competences: [''],
      role: ['ROLE_EMPLOYE', Validators.required],
      typeUtilisateur: ['PERMANENT', Validators.required]
    });

    this.editForm = this.fb.group({
      nom: ['', Validators.required],
      prenom: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      CIN: ['', Validators.required],
      numeroTel: [''],
      poste: [''],
      competences: [''],
      typeUtilisateur: ['PERMANENT', Validators.required]
    });
  }

  loadUtilisateurs(): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.utilisateurService.getAll().subscribe({
      next: (data: UtilisateurResponse[]) => {
        this.utilisateurs = data;
        this.applyFilter();
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Erreur lors du chargement des utilisateurs';
        this.isLoading = false;
      }
    });
  }

  applyFilter(): void {
    this.filteredUtilisateurs = this.filter === 'all'
      ? this.utilisateurs
      : this.utilisateurs.filter(u => u.role === this.filter);
  }

  setFilter(f: 'all' | 'ROLE_ADMIN' | 'ROLE_EMPLOYE'): void {
    this.filter = f;
    this.applyFilter();
  }

  openCreateModal(): void {
    this.isEditing = false;
    this.selectedUser = null;
    this.errorMessage = '';
    this.createPhotoUrl = '';
    this.photoErrorMessage = '';
    this.createForm.reset({
      role: 'ROLE_EMPLOYE',
      typeUtilisateur: 'PERMANENT'
    });
    this.showModal = true;
  }

  openEditModal(user: UtilisateurResponse): void {
    this.isEditing = true;
    this.selectedUser = user;
    this.errorMessage = '';
    this.editPhotoUrl = user.photo || '';
    this.photoErrorMessage = '';
    this.editForm.patchValue({
      nom: user.nom,
      prenom: user.prenom,
      email: user.email,
      CIN: user.CIN || '',
      numeroTel: user.numeroTel,
      poste: user.poste,
      competences: user.competences,
      typeUtilisateur: user.typeUtilisateur
    });
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
    this.errorMessage = '';
  }

  saveCreate(): void {
    if (this.createForm.invalid) return;
    this.errorMessage = '';

    const raw = this.createForm.value;
    const payload: CreateUtilisateurRequest = {
      ...raw,
      competences: raw.competences || '',
      photo: this.createPhotoUrl || null
    };

    console.log('Payload envoyé:', payload);

    this.utilisateurService.create(payload).subscribe({
      next: () => {
        this.loadUtilisateurs();
        this.closeModal();
      },
      error: (err) => {
        console.error('Erreur backend:', err);
        this.errorMessage = err.error?.message || err.error || err.message || 'Erreur lors de la création';
      }
    });
  }

  saveEdit(): void {
    if (this.editForm.invalid || !this.selectedUser) return;
    this.errorMessage = '';

    const raw = this.editForm.value;
    const payload: UpdateUtilisateurRequest = {
      ...raw,
      competences: raw.competences || '',
      photo: this.editPhotoUrl || null
    };

    this.utilisateurService.update(this.selectedUser.id, payload).subscribe({
      next: () => {
        this.loadUtilisateurs();
        this.closeModal();
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Erreur lors de la mise à jour';
      }
    });
  }

  // Ouvre la boîte de confirmation
  confirmDelete(utilisateur: UtilisateurResponse): void {
    this.utilisateurASupprimer = utilisateur;
    this.showDeleteModal = true;
  }

  // Exécute la suppression après confirmation
  deleteUtilisateur(): void {
    if (!this.utilisateurASupprimer) return;
    this.errorMessage = '';

    this.utilisateurService.delete(this.utilisateurASupprimer.id).subscribe({
      next: () => {
        this.loadUtilisateurs();
        this.showDeleteModal = false;
        this.utilisateurASupprimer = null;
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Erreur lors de la suppression';
        this.showDeleteModal = false;
      }
    });
  }

  // Annule la suppression
  cancelDelete(): void {
    this.showDeleteModal = false;
    this.utilisateurASupprimer = null;
  }

  toggleActivation(id: number): void {
    this.utilisateurService.toggleActivation(id).subscribe({
      next: () => {
        this.loadUtilisateurs();
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Erreur lors du changement de statut';
      }
    });
  }

  get adminCount(): number {
    return this.utilisateurs.filter(u => u.role === 'ROLE_ADMIN').length;
  }

  get employeCount(): number {
    return this.utilisateurs.filter(u => u.role === 'ROLE_EMPLOYE').length;
  }

  // Photo upload methods
  onPhotoSelected(event: Event, mode: 'create' | 'edit'): void {
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) return;

    const file = input.files[0];

    const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/webp'];
    if (!allowedTypes.includes(file.type)) {
      this.photoErrorMessage = 'Format non accepté. Utilisez jpg, png ou webp.';
      return;
    }
    if (file.size > 2 * 1024 * 1024) {
      this.photoErrorMessage = 'La photo ne doit pas dépasser 2MB.';
      return;
    }

    this.isUploadingPhoto = true;
    this.photoErrorMessage = '';

    const formData = new FormData();
    formData.append('photo', file);

    this.utilisateurService.uploadPhoto(formData).subscribe({
      next: (response) => {
        if (mode === 'create') {
          this.createPhotoUrl = response.photoUrl;
        } else {
          this.editPhotoUrl = response.photoUrl;
        }
        this.isUploadingPhoto = false;
      },
      error: () => {
        this.photoErrorMessage = 'Erreur lors du téléchargement de la photo.';
        this.isUploadingPhoto = false;
      }
    });
  }

  getPhotoFullUrl(photoUrl: string): string {
    return photoUrl ? `http://localhost:8080${photoUrl}` : '';
  }

  getInitials(prenom: string, nom: string): string {
    return ((prenom?.[0] || '') + (nom?.[0] || '')).toUpperCase();
  }
}
