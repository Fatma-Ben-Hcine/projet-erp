import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { StatCardComponent } from '../../shared/components/stat-card/stat-card.component';
import { AdminSidebarComponent } from '../shared/sidebar/sidebar.component';
import { ProjetService } from '../../core/services/projet.service';
import { EmployeeService } from '../../admin/rh/services/employee.service';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, StatCardComponent, AdminSidebarComponent],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class AdminDashboardComponent implements OnInit, OnDestroy {
  stats: any = {};
  isLoadingStats = true;

  totalProjects = 0;
  completedProjects = 0;
  lateProjects = 0;

  totalActivities = 0;
  totalTasks = 0;

  newEmployees = 0;
  permanentEmployees = 0;
  temporaryEmployees = 0;
  admins = 0;

  isLoading = true;
  autoRefresh = true;
  refreshIntervalMs = 30000; // 30 secondes
  private refreshIntervalId: any;

  constructor(
    private http: HttpClient,
    private projetService: ProjetService,
    private employeeService: EmployeeService
  ) {}

  ngOnInit(): void {
    this.chargerStats();
    if (this.autoRefresh) {
      this.refreshIntervalId = setInterval(() => {
        this.chargerStats();
      }, this.refreshIntervalMs);
    }
  }

  chargerStats(): void {
    this.http.get<any>('http://localhost:8080/api/dashboard/stats').subscribe({
      next: (data) => {
        console.log('Stats from API:', data);
        this.stats = data;
        this.isLoadingStats = false;
      },
      error: (err) => {
        console.error('Error loading stats:', err);
        this.isLoadingStats = false;
      }
    });
  }

  ngOnDestroy(): void {
    if (this.refreshIntervalId) {
      clearInterval(this.refreshIntervalId);
    }
  }

  loadData(): void {
    this.isLoading = true;
    forkJoin({
      projects: this.projetService.getAll(),
      employees: this.employeeService.getEmployees()
    }).subscribe({
      next: ({ projects, employees }) => {
        console.log('📊 Projets reçus:', projects);
        console.log('👥 Employés reçus:', employees);

        // Projects
        this.totalProjects = (projects || []).length;
        this.completedProjects = (projects || []).filter((p: any) => p.status === 'completed' || p.statut === 'completed').length;
        this.lateProjects = (projects || []).filter((p: any) => p.status === 'late' || p.statut === 'late').length;

        // Activities & Tasks (calculated from projects)
        let activitiesCount = 0;
        let tasksCount = 0;
        (projects || []).forEach((p: any) => {
          const activities = p.activities || p.activites || p.Activity || p.Activites || [];
          if (Array.isArray(activities)) {
            activitiesCount += activities.length;
            activities.forEach((a: any) => {
              const tasks = a.tasks || a.taches || a.Task || a.Taches || [];
              if (Array.isArray(tasks)) {
                tasksCount += tasks.length;
              }
            });
          }
        });
        this.totalActivities = activitiesCount;
        this.totalTasks = tasksCount;

        console.log('📈 Stats calculées:', {
          totalProjects: this.totalProjects,
          completedProjects: this.completedProjects,
          lateProjects: this.lateProjects,
          totalActivities: this.totalActivities,
          totalTasks: this.totalTasks
        });

        // Employees
        const thirtyDaysAgo = new Date();
        thirtyDaysAgo.setDate(thirtyDaysAgo.getDate() - 30);

        this.newEmployees = (employees || []).filter(e => {
          const join = e && (e as any).joinDate ? new Date((e as any).joinDate) : null;
          return join ? join > thirtyDaysAgo : false;
        }).length;
        this.permanentEmployees = (employees || []).filter((e: any) => !e.isTemporary).length;
        this.temporaryEmployees = (employees || []).filter((e: any) => e.isTemporary).length;
        this.admins = (employees || []).filter((e: any) => e.role === 'admin' || e.role === 'ROLE_ADMIN').length;

        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error loading dashboard data', err);
        this.isLoading = false;
      }
    });
  }

  toggleAutoRefresh(): void {
    this.autoRefresh = !this.autoRefresh;
    if (this.autoRefresh) {
      this.refreshIntervalId = setInterval(() => this.loadData(), this.refreshIntervalMs);
    } else {
      if (this.refreshIntervalId) {
        clearInterval(this.refreshIntervalId);
      }
    }
  }

  manualRefresh(): void {
    this.loadData();
  }
}
