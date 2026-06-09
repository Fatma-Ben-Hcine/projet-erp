import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { EmployeSidebarComponent } from '../shared/sidebar/sidebar.component';
import { KanbanColumnComponent } from '../shared/kanban-column/kanban-column.component';
import { DemarrerProjetModalComponent } from '../projets/demarrer-projet-modal/demarrer-projet-modal.component';
import { Project, ProjectStatus } from '../../core/models/project.model';
import { ProjetResponse } from '../../core/models/projet.model';
import { EmployeProjetService } from '../../core/services/employe-projet.service';
import { AuthService } from '../../auth/auth.service';
import { mockEmployees } from '../../core/data/mock-data';

@Component({
  selector: 'app-employe-dashboard',
  standalone: true,
  imports: [CommonModule, EmployeSidebarComponent, KanbanColumnComponent, DemarrerProjetModalComponent],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class EmployeDashboardComponent implements OnInit {
  view: 'kanban' | 'calendar' = 'kanban';
  projects: Project[] = [];
  currentUserId = '';
  userName = '';

  selectedProject: Project | null = null;
  showDetailsModal = false;

  // Modal Démarrer Projet
  showDemarrerModal = false;
  projetPourDemarrer: ProjetResponse | null = null;

  // Store original projet responses for modal
  private projetResponses: Map<string, ProjetResponse> = new Map();

  constructor(
    private router: Router,
    private projetService: EmployeProjetService,
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ) {}

  get newProjects()        { return this.projects.filter(p => p.status === 'new'); }
  get inProgressProjects() { return this.projects.filter(p => p.status === 'in-progress'); }
  get completedProjects()  { return this.projects.filter(p => p.status === 'completed'); }
  get lateProjects()       { return this.projects.filter(p => p.status === 'late'); }

  ngOnInit(): void {
    this.currentUserId = this.authService.getUserId() || '0';
    this.userName = localStorage.getItem('prenom') || 'Jean Dupont';

    // Load projects from backend
    this.loadProjects();
  }

  loadProjects(): void {
    this.projetService.getMesProjets().subscribe({
      next: (projets) => {
        // Store original responses for modal
        this.projetResponses.clear();
        projets.forEach(p => this.projetResponses.set(p.id.toString(), p));

        this.projects = projets.map(p => this.mapProjetResponseToProject(p));

        // Vérifier is-chef pour chaque projet individuellement
        this.projects.forEach(project => {
          this.projetService.isChefDeProjet(parseInt(project.id)).subscribe({
            next: (result: boolean) => {
              project.isChef = result === true;
            },
            error: () => {
              project.isChef = false;
            }
          });
        });
      },
      error: (err) => {
        console.error('Erreur lors du chargement des projets:', err);
        this.projects = [];
      }
    });
  }

  private mapProjetResponseToProject(projet: ProjetResponse): Project {
    // Map backend status to frontend status
    const statusMap: Record<string, ProjectStatus> = {
      'NOUVEAU': 'new',
      'EN_COURS': 'in-progress',
      'TERMINE': 'completed',
      'EN_RETARD': 'late',
      'EN_PAUSE': 'in-progress',
      'ANNULE': 'completed'
    };

    // Check if project is late based on deadline
    let status = statusMap[projet.statut || 'NOUVEAU'] || 'new';
    const today = new Date();
    const deadline = new Date(projet.dateLimite);
    if (deadline < today && status !== 'completed') {
      status = 'late';
    }

    return {
      id: projet.id.toString(),
      name: projet.nom,
      description: projet.description,
      status: status,
      deadline: projet.dateLimite,
      progress: projet.progression || 0,
      budget: projet.budget,
      projectManager: projet.chefDeProjet?.id?.toString() || '',
      clientId: projet.client?.id?.toString() || '',
      assignedEmployees: (projet.employes || []).map(e => e.id.toString())
    };
  }

  getClientName(clientId: string): string {
    return 'Client ' + clientId;
  }

  getEmployeeName(empId: string): string {
    const emp = mockEmployees.find(e => e.id === empId);
    return emp ? emp.firstName + ' ' + emp.lastName : empId;
  }

  isProjectManager(project: Project): boolean {
    return project.isChef === true;
  }

  handleViewDetails(projectId: string): void {
    // Navigate to read-only project detail page
    this.router.navigate(['/employe/projets', projectId, 'details']);
  }

  handleDeposit(projectId: string): void {
    // Navigate to deposit page (for status management)
    this.router.navigate(['/employe/projets', projectId, 'depot']);
  }

  handleStart(projectId: string): void {
    console.log('handleStart called with projectId:', projectId);
    console.log('projetResponses Map:', this.projetResponses);
    console.log('Map keys:', Array.from(this.projetResponses.keys()));

    // Get the original projet response
    const projet = this.projetResponses.get(projectId);
    console.log('Found projet:', projet);

    if (projet) {
      this.projetPourDemarrer = projet;
      this.showDemarrerModal = true;
      this.cdr.detectChanges(); // Force change detection
      console.log('Modal opened, showDemarrerModal:', this.showDemarrerModal);
    } else {
      console.error('Project not found in projetResponses Map');
    }
  }

  onDemarrerModalClosed(): void {
    this.showDemarrerModal = false;
    this.projetPourDemarrer = null;
  }

  onProjectStarted(): void {
    // Refresh projects list after project is started
    this.loadProjects();
    this.showDemarrerModal = false;
    this.projetPourDemarrer = null;
  }

  closeModal(): void {
    this.showDetailsModal = false;
    this.selectedProject = null;
  }

  getStatusLabel(status: string): string {
    const labels: Record<string, string> = {
      'new': 'Nouveau',
      'in-progress': 'En cours',
      'completed': 'Terminé',
      'late': 'En retard'
    };
    return labels[status] || status;
  }

  getStatusClass(status: string): string {
    const classes: Record<string, string> = {
      'new': 'badge-default',
      'in-progress': 'badge-warning',
      'completed': 'badge-success',
      'late': 'badge-danger'
    };
    return classes[status] || 'badge-default';
  }
}
