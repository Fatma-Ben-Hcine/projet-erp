import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { ContratService } from '../../../core/services/contrat.service';
import { ClientService } from '../../../core/services/client.service';
import { AdminSidebarComponent } from '../../shared/sidebar/sidebar.component';
import { ContratRequest, ContratResponse, STATUTS_CONTRAT } from '../../../core/models/contrat.model';
import { ClientResponse } from '../../../core/models/client.model';

@Component({
  selector: 'app-contrat-management',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule, AdminSidebarComponent],
  templateUrl: './contrat-management.component.html',
  styleUrl: './contrat-management.component.css'
})
export class ContratManagementComponent implements OnInit {

  contrats: ContratResponse[] = [];
  filteredContrats: ContratResponse[] = [];
  clients: ClientResponse[] = [];
  isLoading = false;
  errorMessage = '';
  showModal = false;
  isEditing = false;
  selectedContrat: ContratResponse | null = null;
  createForm!: FormGroup;
  editForm!: FormGroup;

  activeFilter: string = 'all';
  statuts = STATUTS_CONTRAT;

  isDarkMode = false;
  private observer: MutationObserver | null = null;

  constructor(
    private contratService: ContratService,
    private clientService: ClientService,
    private fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.initForms();
    this.loadContrats();
    this.loadClients();
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

  initForms(): void {
    this.createForm = this.fb.group({
      dateDebut: ['', Validators.required],
      dateFin: ['', Validators.required],
      montant: ['', [Validators.required, Validators.min(0)]],
      statut: ['EN_COURS', Validators.required],
      clientId: ['', Validators.required]
    });

    this.editForm = this.fb.group({
      dateDebut: ['', Validators.required],
      dateFin: ['', Validators.required],
      montant: ['', [Validators.required, Validators.min(0)]],
      statut: ['', Validators.required],
      clientId: ['', Validators.required]
    });
  }

  loadContrats(): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.contratService.getAll().subscribe({
      next: (data: ContratResponse[]) => {
        this.contrats = data;
        this.applyFilter();
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Erreur lors du chargement des contrats';
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
        console.error('Erreur chargement clients:', err);
      }
    });
  }

  applyFilter(): void {
    if (this.activeFilter === 'all') {
      this.filteredContrats = [...this.contrats];
    } else {
      this.filteredContrats = this.contrats.filter(c => c.statut === this.activeFilter);
    }
  }

  setFilter(statut: string): void {
    this.activeFilter = statut;
    this.applyFilter();
  }

  getStatutLabel(statut: string): string {
    const labels: { [key: string]: string } = {
      'EN_COURS': 'En Cours',
      'TERMINE': 'Terminé',
      'SUSPENDU': 'Suspendu',
      'EN_ATTENTE': 'En Attente'
    };
    return labels[statut] || statut;
  }

  getStatutBadgeClass(statut: string): string {
    const classes: { [key: string]: string } = {
      'EN_COURS': 'badge-success',
      'TERMINE': 'badge-info',
      'SUSPENDU': 'badge-warning',
      'EN_ATTENTE': 'badge-default'
    };
    return classes[statut] || 'badge-default';
  }

  getClientName(clientId: number): string {
    const client = this.clients.find(c => c.id === clientId);
    return client ? `${client.nom} ${client.prenom}` : 'Client #' + clientId;
  }

  openCreateModal(): void {
    this.isEditing = false;
    this.selectedContrat = null;
    this.errorMessage = '';
    this.createForm.reset({
      statut: 'EN_COURS'
    });
    this.showModal = true;
  }

  openEditModal(contrat: ContratResponse): void {
    this.isEditing = true;
    this.selectedContrat = contrat;
    this.errorMessage = '';
    this.editForm.patchValue({
      dateDebut: contrat.dateDebut,
      dateFin: contrat.dateFin,
      montant: contrat.montant,
      statut: contrat.statut,
      clientId: contrat.clientId
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

    const payload: ContratRequest = {
      ...this.createForm.value,
      clientId: Number(this.createForm.value.clientId)
    };

    this.contratService.create(payload).subscribe({
      next: () => {
        this.loadContrats();
        this.closeModal();
      },
      error: (err) => {
        this.errorMessage = err.error?.message || err.error || 'Erreur lors de la création';
      }
    });
  }

  saveEdit(): void {
    if (this.editForm.invalid || !this.selectedContrat) return;
    this.errorMessage = '';

    const payload: ContratRequest = {
      ...this.editForm.value,
      clientId: Number(this.editForm.value.clientId)
    };

    this.contratService.update(this.selectedContrat.id, payload).subscribe({
      next: () => {
        this.loadContrats();
        this.closeModal();
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Erreur lors de la mise à jour';
      }
    });
  }

  deleteContrat(id: number): void {
    if (!confirm('Confirmer la suppression de ce contrat ?')) return;
    this.errorMessage = '';

    this.contratService.delete(id).subscribe({
      next: () => {
        this.loadContrats();
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Erreur lors de la suppression';
      }
    });
  }

  formatMontant(montant: number): string {
    return new Intl.NumberFormat('fr-FR', {
      style: 'currency',
      currency: 'TND'
    }).format(montant);
  }
}
