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
    
    if (isLoggedIn && isAdmin) {
      console.log('AdminGuard - Accès autorisé');
      return true;
    } else {
      console.log('AdminGuard - Accès refusé, redirection vers login');
      this.router.navigate(['/login']);
      return false;
    }
  }
}
