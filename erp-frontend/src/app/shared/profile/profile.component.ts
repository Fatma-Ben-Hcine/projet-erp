import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ProfileService, UserProfile } from '../services/profile.service';
import { AuthService } from '../../auth/auth.service';
import { AdminSidebarComponent } from '../../admin/shared/sidebar/sidebar.component';
import { EmployeSidebarComponent } from '../../employe/shared/sidebar/sidebar.component';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, RouterModule, AdminSidebarComponent, EmployeSidebarComponent],
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css']
})
export class ProfileComponent implements OnInit, OnDestroy {
  profile: UserProfile | null = null;
  isLoading = true;
  errorMessage = '';
  successMessage = '';
  isUploadingPhoto = false;
  photoPreview: string | null = null;
  isAdmin = false;
  isDarkMode = false;

  private darkModeObserver!: MutationObserver;

  constructor(
    private profileService: ProfileService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.isAdmin = this.authService.getRole() === 'ROLE_ADMIN';
    this.isDarkMode = document.body.classList.contains('dark');
    this.loadProfile();
    this.watchDarkMode();
  }

  ngOnDestroy(): void {
    if (this.darkModeObserver) {
      this.darkModeObserver.disconnect();
    }
  }

  private watchDarkMode(): void {
    this.darkModeObserver = new MutationObserver(() => {
      this.isDarkMode = document.body.classList.contains('dark');
    });

    this.darkModeObserver.observe(document.body, {
      attributes: true,
      attributeFilter: ['class']
    });
  }

  loadProfile(): void {
    this.isLoading = true;
    this.profileService.getProfile().subscribe({
      next: (data) => {
        this.profile = data;
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'Erreur lors du chargement du profil';
        this.isLoading = false;
      }
    });
  }

  getRoleLabel(role: string): string {
    const labels: Record<string, string> = {
      'ROLE_ADMIN': 'Administrateur',
      'ROLE_EMPLOYE': 'Employé'
    };
    return labels[role] || role;
  }

  getRoleBadgeClass(role: string): string {
    return role === 'ROLE_ADMIN' ? 'badge-admin' : 'badge-employe';
  }

  getInitials(): string {
    if (!this.profile) return '?';
    return (this.profile.prenom[0] + this.profile.nom[0]).toUpperCase();
  }

  getPhotoUrl(): string {
    if (this.profile?.photo) {
      return `http://localhost:8080${this.profile.photo}`;
    }
    return '';
  }

  hasPhoto(): boolean {
    return !!this.profile?.photo;
  }

  getBackLink(): string {
    return this.isAdmin ? '/admin/dashboard' : '/employe/dashboard';
  }
}
