import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { EmployeSidebarComponent } from '../../shared/sidebar/sidebar.component';
import { EmployeProjetService } from '../../../core/services/employe-projet.service';
import { ProjetService } from '../../../core/services/projet.service';
import { EmployeActiviteService } from '../../../core/services/employe-activite.service';
import { EmployeTacheService } from '../../../core/services/employe-tache.service';
import { AuthService } from '../../../auth/auth.service';
import { ProjetResponse } from '../../../core/models/projet.model';
import { ActiviteResponse, ActiviteRequest, TacheRequest } from '../../../core/models/activite.model';

interface NewActivityForm {
  nom: string;
  description: string;
  dateDebut: string;
  dateFin: string;
  taches: NewTaskForm[];
}

interface NewTaskForm {
  nom: string;
  description: string;
  dateDebut: string;
  dateFin: string;
}

@Component({
  selector: 'app-employe-projet-start',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, EmployeSidebarComponent],
  templateUrl: './projet-start.component.html',
  styleUrls: ['./projet-start.component.css']
})
export class EmployeProjetStartComponent implements OnInit {
  projet: ProjetResponse | null = null;
  activites: ActiviteResponse[] = [];
  isLoading = true;
  errorMessage = '';
  currentUserId: number | null = null;
  isChefDeProjet = false;

  // Form for new activity
  showNewActivityForm = false;
  newActivity: NewActivityForm = {
    nom: '',
    description: '',
    dateDebut: '',
    dateFin: '',
    taches: []
  };

  // Form for new task within an activity
  newTaskForActivity: { [key: number]: { show: boolean; task: NewTaskForm } } = {};

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private employeProjetService: EmployeProjetService,
    private projetService: ProjetService,
    private employeActiviteService: EmployeActiviteService,
    private employeTacheService: EmployeTacheService,
    private authService: AuthService
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
        this.loadActivites(id);
      },
      error: (err: any) => {
        this.errorMessage = 'Erreur lors du chargement du projet';
        this.isLoading = false;
      }
    });
  }

  loadActivites(projetId: number): void {
    this.employeActiviteService.getByProjet(projetId).subscribe({
      next: (data) => {
        this.activites = data;
        // Initialize task forms for each activity
        data.forEach(activite => {
          this.newTaskForActivity[activite.id] = {
            show: false,
            task: { nom: '', description: '', dateDebut: '', dateFin: '' }
          };
        });
        this.isLoading = false;
      },
      error: (err: any) => {
        this.errorMessage = 'Erreur lors du chargement des activités';
        this.isLoading = false;
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/employe/dashboard']);
  }

  formatDate(dateString: string | null | undefined): string {
    if (!dateString) return '—';
    const date = new Date(dateString);
    return date.toLocaleDateString('fr-FR');
  }

  // Activity CRUD
  toggleNewActivityForm(): void {
    this.showNewActivityForm = !this.showNewActivityForm;
    if (!this.showNewActivityForm) {
      this.resetNewActivityForm();
    }
  }

  resetNewActivityForm(): void {
    this.newActivity = {
      nom: '',
      description: '',
      dateDebut: '',
      dateFin: '',
      taches: []
    };
  }

  addTaskToNewActivity(): void {
    this.newActivity.taches.push({
      nom: '',
      description: '',
      dateDebut: '',
      dateFin: ''
    });
  }

  removeTaskFromNewActivity(index: number): void {
    this.newActivity.taches.splice(index, 1);
  }

  createActivity(): void {
    if (!this.projet) return;

    const request: ActiviteRequest = {
      nom: this.newActivity.nom,
      description: this.newActivity.description,
      dateDebut: this.newActivity.dateDebut,
      dateFin: this.newActivity.dateFin,
      projetId: this.projet.id,
      employeIds: []
    };

    this.employeActiviteService.create(request).subscribe({
      next: (activite) => {
        // Create tasks after activity is created
        const taskRequests = this.newActivity.taches.map(t => ({
          nom: t.nom,
          description: t.description,
          dateDebut: t.dateDebut,
          dateFin: t.dateFin,
          activiteId: activite.id,
          employeIds: []
        }));
        
        // Execute all task creations in parallel
        Promise.all(taskRequests.map(req => 
          this.employeTacheService.create(req).toPromise()
        )).then(() => {
          this.showNewActivityForm = false;
          this.resetNewActivityForm();
          this.loadActivites(this.projet!.id);
        }).catch(() => {
          this.errorMessage = 'Erreur lors de la création des tâches';
        });
      },
      error: (err: any) => {
        this.errorMessage = 'Erreur lors de la création de l\'activité';
      }
    });
  }

  // Task CRUD within existing activity
  toggleNewTaskForm(activityId: number): void {
    if (this.newTaskForActivity[activityId]) {
      this.newTaskForActivity[activityId].show = !this.newTaskForActivity[activityId].show;
    }
  }

  createTask(activityId: number): void {
    const taskForm = this.newTaskForActivity[activityId]?.task;
    if (!taskForm || !this.projet) return;

    const request: TacheRequest = {
      nom: taskForm.nom,
      description: taskForm.description,
      dateDebut: taskForm.dateDebut,
      dateFin: taskForm.dateFin,
      activiteId: activityId,
      employeIds: []
    };

    this.employeTacheService.create(request).subscribe({
      next: () => {
        this.newTaskForActivity[activityId].show = false;
        this.newTaskForActivity[activityId].task = { nom: '', description: '', dateDebut: '', dateFin: '' };
        this.loadActivites(this.projet!.id);
      },
      error: (err: any) => {
        this.errorMessage = 'Erreur lors de la création de la tâche';
      }
    });
  }

  deleteActivity(activityId: number): void {
    if (!confirm('Êtes-vous sûr de vouloir supprimer cette activité ?')) return;
    
    this.employeActiviteService.delete(activityId).subscribe({
      next: () => {
        this.loadActivites(this.projet!.id);
      },
      error: (err: any) => {
        this.errorMessage = 'Erreur lors de la suppression de l\'activité';
      }
    });
  }

  deleteTask(taskId: number): void {
    if (!confirm('Êtes-vous sûr de vouloir supprimer cette tâche ?')) return;
    
    this.employeTacheService.delete(taskId).subscribe({
      next: () => {
        this.loadActivites(this.projet!.id);
      },
      error: (err: any) => {
        this.errorMessage = 'Erreur lors de la suppression de la tâche';
      }
    });
  }

  checkChefDeProjet(): void {
    if (!this.projet || !this.currentUserId) {
      this.isChefDeProjet = false;
      return;
    }
    this.employeProjetService.isChefDeProjet(this.projet.id).subscribe({
      next: (isChef) => {
        this.isChefDeProjet = isChef;
        // Redirect if not chef de projet
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

  // Change project status to EN_COURS when starting
  startProject(): void {
    if (!this.projet) return;
    
    if (this.activites.length === 0) {
      alert('Vous devez créer au moins une activité avant de démarrer le projet.');
      return;
    }

    this.projetService.updateStatut(this.projet.id, 'EN_COURS').subscribe({
      next: () => {
        this.router.navigate(['/employe/dashboard']);
      },
      error: (err: any) => {
        this.errorMessage = 'Erreur lors du démarrage du projet';
      }
    });
  }
}
