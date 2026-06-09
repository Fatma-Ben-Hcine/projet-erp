import { Component } from '@angular/core';
import { AuthService } from '../../auth/auth.service';

@Component({
  selector: 'app-employe-dashboard',
  standalone: true,
  template: `
    <div class="dashboard-container">
      <header class="dashboard-header">
        <h1>Tableau de Bord Employé</h1>
        <div class="user-info">
          <span>Bienvenue, {{ authService.getEmail() }}</span>
          <button (click)="logout()" class="btn-logout">Déconnexion</button>
        </div>
      </header>
      
      <main class="dashboard-content">
        <div class="stats-grid">
          <div class="stat-card">
            <h3>Mon Profil</h3>
            <p>Gérez vos informations personnelles et votre photo</p>
          </div>
          <div class="stat-card">
            <h3>Mes Tâches</h3>
            <p>Suivez vos tâches et projets en cours</p>
          </div>
          <div class="stat-card">
            <h3>Documents</h3>
            <p>Accédez aux documents partagés</p>
          </div>
        </div>
      </main>
    </div>
  `,
  styleUrls: ['./employe-dashboard.component.css']
})
export class EmployeDashboardComponent {
  constructor(public authService: AuthService) {}

  logout(): void {
    this.authService.logout();
  }
}
