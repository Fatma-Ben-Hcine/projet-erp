import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class AdminGuard implements CanActivate {
  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  canActivate(): boolean {
    const isLoggedIn = this.authService.isLoggedIn();
    const isAdmin = this.authService.isAdmin();

    console.log('AdminGuard Debug - LoggedIn:', isLoggedIn);
    console.log('AdminGuard Debug - IsAdmin:', isAdmin);

    if (!isLoggedIn) {
      console.log('AdminGuard - Utilisateur non authentifié, redirection vers login');
      this.router.navigate(['/login']);
      return false;
    }

    if (!isAdmin) {
      console.log('AdminGuard - Utilisateur authentifié mais pas admin, redirection vers tableau de bord');
      this.router.navigate(['/admin/dashboard']);
      return false;
    }

    console.log('AdminGuard - Accès autorisé');
    return true;
  }
}
