import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { ClientService } from '../../../core/services/client.service';
import { DarkModeService } from '../../../core/services/dark-mode.service';
import { AdminSidebarComponent } from '../../shared/sidebar/sidebar.component';
import { ClientRequest, ClientResponse } from '../../../core/models/client.model';
import { Subject, Subscription } from 'rxjs';
import { debounceTime } from 'rxjs/operators';

@Component({
  selector: 'app-client-management',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule, AdminSidebarComponent],
  templateUrl: './client-management.component.html',
  styleUrl: './client-management.component.css'
})
export class ClientManagementComponent implements OnInit {

  clients: ClientResponse[] = [];
  filteredClients: ClientResponse[] = [];
  isLoading = false;
  errorMessage = '';
  showModal = false;
  isEditing = false;
  selectedClient: ClientResponse | null = null;
  createForm!: FormGroup;
  editForm!: FormGroup;

  searchKeyword = '';
  private searchSubject = new Subject<string>();
  private darkModeSubscription: Subscription | null = null;

  isDarkMode = false;

  constructor(
    private clientService: ClientService,
    private fb: FormBuilder,
    private darkModeService: DarkModeService
  ) {}

  ngOnInit(): void {
    this.initForms();
    this.loadClients();
    this.setupSearchDebounce();
    this.darkModeSubscription = this.darkModeService.isDarkMode$.subscribe(value => {
      this.isDarkMode = value;
    });
    this.isDarkMode = this.darkModeService.isDarkMode;
  }

  ngOnDestroy(): void {
    this.darkModeSubscription?.unsubscribe();
  }

  setupSearchDebounce(): void {
    this.searchSubject.pipe(debounceTime(300)).subscribe((keyword) => {
      if (keyword.trim()) {
        this.searchClients(keyword);
      } else {
        this.filteredClients = [...this.clients];
      }
    });
  }

  onSearchInput(event: Event): void {
    const value = (event.target as HTMLInputElement).value;
    this.searchKeyword = value;
    this.searchSubject.next(value);
  }

  initForms(): void {
    this.createForm = this.fb.group({
      nom: ['', Validators.required],
      prenom: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      numeroTelephone: [''],
      matriculeFiscale: ['']
    });

    this.editForm = this.fb.group({
      nom: ['', Validators.required],
      prenom: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      numeroTelephone: [''],
      matriculeFiscale: ['']
    });
  }

  loadClients(): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.clientService.getAll().subscribe({
      next: (data: ClientResponse[]) => {
        this.clients = data;
        this.filteredClients = data;
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Erreur lors du chargement des clients';
        this.isLoading = false;
      }
    });
  }

  searchClients(keyword: string): void {
    this.isLoading = true;
    this.clientService.search(keyword).subscribe({
      next: (data: ClientResponse[]) => {
        this.filteredClients = data;
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Erreur lors de la recherche';
        this.isLoading = false;
      }
    });
  }

  openCreateModal(): void {
    this.isEditing = false;
    this.selectedClient = null;
    this.errorMessage = '';
    this.createForm.reset();
    this.showModal = true;
  }

  openEditModal(client: ClientResponse): void {
    this.isEditing = true;
    this.selectedClient = client;
    this.errorMessage = '';
    this.editForm.patchValue({
      nom: client.nom,
      prenom: client.prenom,
      email: client.email,
      numeroTelephone: client.numeroTelephone,
      matriculeFiscale: client.matriculeFiscale
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

    const payload: ClientRequest = this.createForm.value;

    this.clientService.create(payload).subscribe({
      next: () => {
        this.loadClients();
        this.closeModal();
      },
      error: (err) => {
        this.errorMessage = err.error?.message || err.error || 'Erreur lors de la création';
      }
    });
  }

  saveEdit(): void {
    if (this.editForm.invalid || !this.selectedClient) return;
    this.errorMessage = '';

    const payload: ClientRequest = this.editForm.value;

    this.clientService.update(this.selectedClient.id, payload).subscribe({
      next: () => {
        this.loadClients();
        this.closeModal();
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Erreur lors de la mise à jour';
      }
    });
  }

  deleteClient(id: number): void {
    if (!confirm('Confirmer la suppression de ce client ?')) return;
    this.errorMessage = '';

    this.clientService.delete(id).subscribe({
      next: () => {
        this.loadClients();
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Erreur lors de la suppression';
      }
    });
  }
}
