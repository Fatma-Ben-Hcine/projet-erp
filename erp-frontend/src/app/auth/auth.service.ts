import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Router } from '@angular/router';

export interface LoginResponse {
  token: string;
  type: string;
  email: string;
  role: string;
  id: number;
  nom?: string;
  prenom?: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly apiUrl = 'http://localhost:8080/api/auth';

  constructor(
    private http: HttpClient,
    private router: Router
  ) {}

  login(email: string, motDePasse: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, {
      email,
      motDePasse
    });
  }

  forgotPassword(email: string): Observable<string> {
    return this.http.post(`${this.apiUrl}/forgot-password`, { email }, {
      responseType: 'text'
    });
  }

  resetPassword(token: string, newPassword: string): Observable<string> {
    return this.http.post(`${this.apiUrl}/reset-password`, {
      token,
      newPassword
    }, {
      responseType: 'text'
    });
  }

  logout(): void {
    // Appeler le backend pour invalider le token
    this.http.post(`${this.apiUrl}/logout`, {}).subscribe({
      next: () => {
        this.clearSession();
      },
      error: () => {
        // Même si le logout échoue, on nettoie la session locale
        this.clearSession();
      }
    });
  }

  private clearSession(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('role');
    localStorage.removeItem('email');
    localStorage.removeItem('userId');
    localStorage.removeItem('nom');
    localStorage.removeItem('prenom');
    this.router.navigate(['/']);
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  getRole(): string | null {
    const role = localStorage.getItem('role');
    if (!role) {
      return null;
    }

    const normalizedRole = role.trim().toUpperCase();
    if (normalizedRole === 'ADMIN') {
      return 'ROLE_ADMIN';
    }
    if (normalizedRole === 'EMPLOYE' || normalizedRole === 'EMPLOYEE') {
      return 'ROLE_EMPLOYE';
    }
    return normalizedRole;
  }

  getEmail(): string | null {
    return localStorage.getItem('email');
  }

  getUserId(): string | null {
    const token = this.getToken();
    if (!token) return null;

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.sub || payload.id || payload.userId || null;
    } catch (e) {
      return null;
    }
  }

  isLoggedIn(): boolean {
    const token = this.getToken();
    const role = this.getRole();
    console.log('Auth Debug - Token:', token ? 'Présent' : 'Absent');
    console.log('Auth Debug - Role:', role);

    if (!token) {
      return false;
    }

    const tokenParts = token.split('.');
    if (tokenParts.length === 3) {
      try {
        const payload = JSON.parse(atob(tokenParts[1]));
        const isExpired = payload.exp * 1000 < Date.now();
        console.log('Auth Debug - Token expiré:', isExpired);

        if (isExpired) {
          this.clearSession();
          return false;
        }
      } catch (e) {
        console.log('Auth Debug - Token invalid JWT payload, skipping expiry check', e);
      }
    } else {
      console.log('Auth Debug - Token non JWT, skipping expiry check');
    }

    return true;
  }

  isAdmin(): boolean {
    return this.getRole() === 'ROLE_ADMIN';
  }

  isEmploye(): boolean {
    return this.getRole() === 'ROLE_EMPLOYE';
  }

  getCurrentUser(): { id: number | null; email: string | null; role: string | null } {
    const userIdStr = localStorage.getItem('userId');
    const parsedId = userIdStr ? parseInt(userIdStr, 10) : null;
    return {
      id: isNaN(parsedId as number) ? null : parsedId,
      email: this.getEmail(),
      role: this.getRole()
    };
  }

  setUserId(id: number): void {
    localStorage.setItem('userId', id.toString());
  }

  getCurrentUserId(): number | null {
    const userIdStr = localStorage.getItem('userId');
    if (!userIdStr) return null;
    const parsedId = parseInt(userIdStr, 10);
    return isNaN(parsedId) ? null : parsedId;
  }

  clearUserId(): void {
    localStorage.removeItem('userId');
  }
}
