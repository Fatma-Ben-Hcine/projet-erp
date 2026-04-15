import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../../auth/auth.service';

@Component({
  selector: 'app-admin-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.css']
})
export class AdminSidebarComponent implements OnInit {
  isDarkMode = false;
  userEmail = '';

  // Dropdown states - all open by default
  projectsOpen = true;
  rhOpen = true;
  clientsOpen = true;

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    this.userEmail = localStorage.getItem('email') || '';
    this.isDarkMode = localStorage.getItem('darkMode') === 'true';
    this.applyDarkMode();
  }

  toggleDropdown(section: 'projects' | 'rh' | 'clients'): void {
    if (section === 'projects') {
      this.projectsOpen = !this.projectsOpen;
    } else if (section === 'rh') {
      this.rhOpen = !this.rhOpen;
    } else if (section === 'clients') {
      this.clientsOpen = !this.clientsOpen;
    }
  }

  toggleDarkMode(): void {
    this.isDarkMode = !this.isDarkMode;
    localStorage.setItem('darkMode', String(this.isDarkMode));
    this.applyDarkMode();
  }

  private applyDarkMode(): void {
    if (this.isDarkMode) {
      document.body.classList.add('dark');
    } else {
      document.body.classList.remove('dark');
    }
  }

  logout(): void {
    this.authService.logout();
  }
}
