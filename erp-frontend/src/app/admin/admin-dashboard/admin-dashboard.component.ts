import { Component } from '@angular/core';
import { AuthService } from '../../auth/auth.service';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  template: `
    <div class="dashboard-container">
      <header class="dashboard-header">
        <h1>Tableau de Bord Administrateur</h1>
        <div class="user-info">
          <span>Bienvenue, {{ authService.getEmail() }}</span>
          <button (click)="logout()" class="btn-logout">Déconnexion</button>
        </div>
      </header>
      
      <main class="dashboard-content">
        <div class="stats-grid">
          <div class="stat-card">
            <h3>Gestion des Utilisateurs</h3>
            <p>Gérez les comptes employés et administrateurs</p>
          </div>
          <div class="stat-card">
            <h3>Upload de Photos</h3>
            <p>Téléchargez des photos de profil pour les utilisateurs</p>
          </div>
          <div class="stat-card">
            <h3>Configuration Système</h3>
            <p>Paramètres et configuration de l'ERP</p>
          </div>
        </div>
      </main>
    </div>
  `,
  styleUrls: ['./admin-dashboard.component.css']
})
export class AdminDashboardComponent {
  constructor(public authService: AuthService) {}

  logout(): void {
    this.authService.logout();
  }
}
