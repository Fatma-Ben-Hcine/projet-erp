import { Routes } from '@angular/router';
import { LoginComponent } from './auth/login/login.component';
import { ForgotPasswordComponent } from './auth/forgot-password/forgot-password.component';
import { ResetPasswordComponent } from './auth/reset-password/reset-password.component';
import { AuthGuard } from './auth/auth.guard';
import { AdminGuard } from './auth/admin.guard';
import { EmployeGuard } from './auth/employe.guard';
import { AdminDashboardComponent } from './admin/dashboard/dashboard.component';
import { EmployeDashboardComponent } from './employe/dashboard/dashboard.component';
import { ProfileComponent } from './shared/profile/profile.component';
import { UserManagementComponent } from './admin/rh/user-management/user-management.component';

import { ClientManagementComponent } from './admin/clients/client-management/client-management.component';
import { ContratManagementComponent } from './admin/contrats/contrat-management/contrat-management.component';
import { ProjetsBoardComponent } from './admin/projets/board/board.component';
import { ProjetDetailComponent } from './admin/projets/projet-detail/projet-detail.component';
import { ProjetDepotComponent } from './admin/projets/projet-depot/projet-depot.component';

// Conge module imports
import { CongeListComponent } from './employe/conges/conge-list/conge-list.component';
import { CongeFormComponent } from './employe/conges/conge-form/conge-form.component';
import { AdminCongeListComponent } from './admin/conges/admin-conge-list/admin-conge-list.component';

// Heures Supplementaires module imports
import { HeuresSupplementairesListComponent } from './admin/heures-supplementaires/heures-supplementaires-list/heures-supplementaires-list.component';
import { HeuresSupplementaireFormComponent } from './admin/heures-supplementaires/heures-supplementaire-form/heures-supplementaire-form.component';

export const routes: Routes = [
  {
    path: '',
    component: LoginComponent,
    title: 'Connexion'
  },
  {
    path: 'login',
    redirectTo: '',
    pathMatch: 'full'
  },
  {
    path: 'forgot-password',
    component: ForgotPasswordComponent,
    title: 'Mot de passe oublié'
  },
  {
    path: 'reset-password',
    component: ResetPasswordComponent,
    title: 'Réinitialiser le mot de passe'
  },
  {
    path: 'admin/dashboard',
    component: AdminDashboardComponent,
    canActivate: [AuthGuard, AdminGuard],
    title: 'Dashboard Admin'
  },
  {
    path: 'admin/profile',
    component: ProfileComponent,
    canActivate: [AuthGuard, AdminGuard],
    title: 'Profil Admin'
  },
  {
    path: 'admin/rh/utilisateurs',
    component: UserManagementComponent,
    canActivate: [AuthGuard, AdminGuard],
    title: 'Gestion Utilisateurs'
  },
  {
    path: 'admin/clients/gestion',
    component: ClientManagementComponent,
    canActivate: [AuthGuard, AdminGuard],
    title: 'Gestion Clients'
  },
  {
    path: 'admin/clients/contrats',
    component: ContratManagementComponent,
    canActivate: [AuthGuard, AdminGuard],
    title: 'Gestion Contrats'
  },
  {
    path: 'admin/projets/board',
    component: ProjetsBoardComponent,
    canActivate: [AuthGuard, AdminGuard],
    title: 'Board Projets'
  },
  {
    path: 'admin/projets/:id',
    component: ProjetDetailComponent,
    canActivate: [AuthGuard, AdminGuard],
    title: 'Détail Projet'
  },
  {
    path: 'admin/projets/:id/depot',
    component: ProjetDepotComponent,
    canActivate: [AuthGuard, AdminGuard],
    title: 'Dépôt Projet'
  },
  {
    path: 'employe/dashboard',
    component: EmployeDashboardComponent,
    canActivate: [AuthGuard, EmployeGuard],
    title: 'Dashboard Employé'
  },
  {
    path: 'employe/profile',
    component: ProfileComponent,
    canActivate: [AuthGuard, EmployeGuard],
    title: 'Profil Employé'
  },
  {
    path: 'employe/conges',
    component: CongeListComponent,
    canActivate: [AuthGuard, EmployeGuard],
    title: 'Mes Congés'
  },
  {
    path: 'employe/conges/nouveau',
    component: CongeFormComponent,
    canActivate: [AuthGuard, EmployeGuard],
    title: 'Nouvelle Demande de Congé'
  },
  {
    path: 'employe/conges/modifier/:id',
    component: CongeFormComponent,
    canActivate: [AuthGuard, EmployeGuard],
    title: 'Modifier Demande de Congé'
  },
  {
    path: 'admin/conges',
    component: AdminCongeListComponent,
    canActivate: [AuthGuard, AdminGuard],
    title: 'Gestion des Congés'
  },
  {
    path: 'admin/heures-supplementaires',
    component: HeuresSupplementairesListComponent,
    canActivate: [AuthGuard, AdminGuard],
    title: 'Gestion des Heures Supplémentaires'
  },
  {
    path: 'admin/heures-supplementaires/new',
    component: HeuresSupplementaireFormComponent,
    canActivate: [AuthGuard, AdminGuard],
    title: 'Ajouter une Heure Supplémentaire'
  },
  {
    path: 'admin/heures-supplementaires/edit/:id',
    component: HeuresSupplementaireFormComponent,
    canActivate: [AuthGuard, AdminGuard],
    title: 'Modifier une Heure Supplémentaire'
  },
  {
    path: '**',
    redirectTo: ''
  }
];
