import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { Subscription } from 'rxjs';
import { AuthService } from '../../../auth/auth.service';
import { DarkModeService } from '../../../core/services/dark-mode.service';

@Component({
  selector: 'app-employe-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.css']
})
export class EmployeSidebarComponent implements OnInit, OnDestroy {
  isDarkMode = false;
  userName = '';
  userNom = '';
  userPrenom = '';
  isOpen = false;
  private darkModeSubscription: Subscription | null = null;

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
    this.userName = localStorage.getItem('email') || 'Employé';
    this.userNom = localStorage.getItem('nom') ?? 'Utilisateur';
    this.userPrenom = localStorage.getItem('prenom') ?? '';
    this.isDarkMode = this.darkModeService.isDarkMode;
    this.darkModeSubscription = this.darkModeService.isDarkMode$.subscribe(value => {
      this.isDarkMode = value;
    });
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
