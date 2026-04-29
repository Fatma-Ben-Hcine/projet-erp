import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Router } from '@angular/router';

export interface LoginResponse {
  token: string;
  type: string;
  email: string;
  role: string;
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
    this.router.navigate(['/']);
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  getRole(): string | null {
    return localStorage.getItem('role');
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
    
    // Vérifier si le token n'est pas expiré (format JWT simple)
    if (token) {
      try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        const isExpired = payload.exp * 1000 < Date.now();
        console.log('Auth Debug - Token expiré:', isExpired);
        
        if (isExpired) {
          this.clearSession();
          return false;
        }
      } catch (e) {
        console.log('Auth Debug - Token invalide:', e);
        this.clearSession();
        return false;
      }
    }
    
    return token !== null;
  }

  isAdmin(): boolean {
    return this.getRole() === 'ROLE_ADMIN';
  }

  isEmploye(): boolean {
    return this.getRole() === 'ROLE_EMPLOYE';
  }
}
