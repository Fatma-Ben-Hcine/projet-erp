import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { Subscription } from 'rxjs';
import { AuthService } from '../../../auth/auth.service';
import { DarkModeService } from '../../../core/services/dark-mode.service';

@Component({
  selector: 'app-admin-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.css']
})
export class AdminSidebarComponent implements OnInit, OnDestroy {
  isDarkMode = false;
  userEmail = '';
  userNom = '';
  userPrenom = '';
  isOpen = false;
  private darkModeSubscription: Subscription | null = null;

  // Dropdown states - all open by default
  projectsOpen = true;
  rhOpen = true;
  clientsOpen = true;
  resourcesOpen: boolean = true;

  constructor(
    private authService: AuthService,
    private darkModeService: DarkModeService
  ) {}

  toggleSidebar(): void {
    this.isOpen = !this.isOpen;
  }

  closeSidebar(): void {
    this.isOpen = false;
  }

  ngOnInit(): void {
    this.userEmail = localStorage.getItem('email') ?? '';
    this.userNom = localStorage.getItem('nom') ?? 'Utilisateur';
    this.userPrenom = localStorage.getItem('prenom') ?? '';
    this.isDarkMode = this.darkModeService.isDarkMode;
    this.darkModeSubscription = this.darkModeService.isDarkMode$.subscribe(value => {
      this.isDarkMode = value;
    });
  }

  toggleDropdown(section: 'projects' | 'rh' | 'clients' | 'resources'): void {
    if (section === 'projects') {
      this.projectsOpen = !this.projectsOpen;
    } else if (section === 'rh') {
      this.rhOpen = !this.rhOpen;
    } else if (section === 'clients') {
      this.clientsOpen = !this.clientsOpen;
    } else if (section === 'resources') {
      this.resourcesOpen = !this.resourcesOpen;
    }
  }

  toggleDarkMode(): void {
    this.darkModeService.toggle();
  }

  ngOnDestroy(): void {
    this.darkModeSubscription?.unsubscribe();
  }

  logout(): void {
    this.authService.logout();
  }
}
