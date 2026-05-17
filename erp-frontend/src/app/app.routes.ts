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
import { RhDashboardComponent } from './admin/rh/rh-dashboard/rh-dashboard.component';

import { ClientManagementComponent } from './admin/clients/client-management/client-management.component';
import { ContratManagementComponent } from './admin/contrats/contrat-management/contrat-management.component';
import { ProjetsBoardComponent } from './admin/projets/board/board.component';
import { ProjetDetailComponent } from './admin/projets/projet-detail/projet-detail.component';
import { ProjetDepotComponent } from './admin/projets/projet-depot/projet-depot.component';

// Employee project pages
import { EmployeProjetDetailComponent } from './employe/projets/projet-detail/projet-detail.component';
import { EmployeProjetStartComponent } from './employe/projets/projet-start/projet-start.component';
import { EmployeProjetDepotComponent } from './employe/projets/projet-depot/projet-depot.component';

// Conge module imports
import { CongeListComponent } from './employe/conges/conge-list/conge-list.component';
import { CongeFormComponent } from './employe/conges/conge-form/conge-form.component';
import { AdminCongeListComponent } from './admin/conges/admin-conge-list/admin-conge-list.component';

// Heures Supplementaires module imports
import { HeuresSupplementairesListComponent } from './admin/heures-supplementaires/heures-supplementaires-list/heures-supplementaires-list.component';
import { HeuresSupplementaireFormComponent } from './admin/heures-supplementaires/heures-supplementaire-form/heures-supplementaire-form.component';

// Ressources imports
import { AdminRessourcesComponent } from './admin/ressources/admin-ressources.component';
import { RessourceFormComponent } from './admin/ressources/ressource-form/ressource-form.component';
import { DemandeRessourcesComponent } from './employe/ressources/demande-ressources/demande-ressources.component';

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
    path: 'admin/rh/dashboard',
    component: RhDashboardComponent,
    canActivate: [AuthGuard, AdminGuard],
    title: 'Dashboard RH'
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
  // Employee project routes
  {
    path: 'employe/projets/:id/details',
    component: EmployeProjetDetailComponent,
    canActivate: [AuthGuard, EmployeGuard],
    title: 'Détail Projet'
  },
  {
    path: 'employe/projets/:id/start',
    component: EmployeProjetStartComponent,
    canActivate: [AuthGuard, EmployeGuard],
    title: 'Configurer Projet'
  },
  {
    path: 'employe/projets/:id/depot',
    component: EmployeProjetDepotComponent,
    canActivate: [AuthGuard, EmployeGuard],
    title: 'Gestion Projet'
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
  // Admin Ressources routes
  {
    path: 'admin/ressources',
    component: AdminRessourcesComponent,
    canActivate: [AuthGuard, AdminGuard],
    title: 'Gestion des Ressources'
  },
  {
    path: 'admin/ressources/nouveau',
    component: RessourceFormComponent,
    canActivate: [AuthGuard, AdminGuard],
    title: 'Nouvelle Ressource'
  },
  {
    path: 'admin/ressources/edit/:id',
    component: RessourceFormComponent,
    canActivate: [AuthGuard, AdminGuard],
    title: 'Modifier Ressource'
  },
  // Employee Ressources routes
  {
    path: 'employe/ressources/demande',
    component: DemandeRessourcesComponent,
    canActivate: [AuthGuard, EmployeGuard],
    title: 'Demande de Ressources'
  },
  {
    path: '**',
    redirectTo: ''
  }
];
