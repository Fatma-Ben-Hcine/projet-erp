import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { EmployeSidebarComponent } from '../shared/sidebar/sidebar.component';
import { KanbanColumnComponent } from '../shared/kanban-column/kanban-column.component';
import { Project } from '../../core/models/project.model';
import { mockProjects, mockEmployees } from '../../core/data/mock-data';

@Component({
  selector: 'app-employe-dashboard',
  standalone: true,
  imports: [CommonModule, EmployeSidebarComponent, KanbanColumnComponent],
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

  get newProjects()        { return this.projects.filter(p => p.status === 'new'); }
  get inProgressProjects() { return this.projects.filter(p => p.status === 'in-progress'); }
  get completedProjects()  { return this.projects.filter(p => p.status === 'completed'); }
  get lateProjects()       { return this.projects.filter(p => p.status === 'late'); }

  ngOnInit(): void {
    this.currentUserId = localStorage.getItem('userId') || 'emp1';
    this.userName = localStorage.getItem('prenom') || 'Jean Dupont';

    // Load projects assigned to this employee
    this.projects = mockProjects.filter(p =>
      p.assignedEmployees.includes(this.currentUserId)
    );

    // Auto-mark late projects
    const today = new Date();
    this.projects = this.projects.map(p => {
      if (new Date(p.deadline) < today && p.status !== 'completed') {
        return { ...p, status: 'late' as const };
      }
      return p;
    });
  }

  getClientName(clientId: string): string {
    return 'Client ' + clientId;
  }

  getEmployeeName(empId: string): string {
    const emp = mockEmployees.find(e => e.id === empId);
    return emp ? emp.firstName + ' ' + emp.lastName : empId;
  }

  isProjectManager(project: Project): boolean {
    return project.projectManager === this.currentUserId;
  }

  handleViewDetails(projectId: string): void {
    this.selectedProject = this.projects.find(p => p.id === projectId) || null;
    this.showDetailsModal = true;
  }

  handleDeposit(projectId: string): void {
    console.log('Deposit for project:', projectId);
    // Navigation to deposit page will be implemented later
  }

  handleStart(projectId: string): void {
    this.projects = this.projects.map(p =>
      p.id === projectId ? { ...p, status: 'in-progress' as const } : p
    );
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
