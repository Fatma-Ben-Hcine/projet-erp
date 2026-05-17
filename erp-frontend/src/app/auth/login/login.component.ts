import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { AuthService, LoginResponse } from '../auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {
  loginForm: FormGroup = new FormGroup({});
  isLoading = false;
  errorMessage = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      motDePasse: ['', [Validators.required, Validators.minLength(6)]]
    });

    // Ne pas rediriger si déjà sur la page de login
    // pour éviter les boucles de redirection
  }

  onSubmit(): void {
    if (this.loginForm.invalid) {
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    const { email, motDePasse } = this.loginForm.value;

    this.authService.login(email, motDePasse).subscribe({
      next: (response: LoginResponse) => {
        const normalizedRole = this.normalizeRole(response.role);
        localStorage.setItem('token', response.token);
        localStorage.setItem('role', normalizedRole);
        localStorage.setItem('email', response.email);
        localStorage.setItem('userId', response.id.toString());

        this.redirectBasedOnRole(normalizedRole);
      },
      error: (error) => {
        this.isLoading = false;

        if (error.status === 401) {
          this.errorMessage = 'Email ou mot de passe incorrect';
        } else if (error.status === 403) {
          this.errorMessage = 'Employé désactivé pour le moment';
        } else {
          this.errorMessage = 'Erreur de connexion, réessayez plus tard';
        }
      }
    });
    this.isLoading = false;
  }

  private redirectBasedOnRole(role?: string): void {
    const currentRole = role || this.normalizeRole(localStorage.getItem('role') || '');

    if (currentRole === 'ROLE_ADMIN') {
      this.router.navigate(['/admin/dashboard']);
    } else if (currentRole === 'ROLE_EMPLOYE') {
      this.router.navigate(['/employe/dashboard']);
    } else {
      this.router.navigate(['/login']);
    }
  }

  private normalizeRole(role?: string): string {
    if (!role) {
      return '';
    }

    const normalized = role.trim().toUpperCase();
    if (normalized === 'ADMIN') {
      return 'ROLE_ADMIN';
    }
    if (normalized === 'EMPLOYE' || normalized === 'EMPLOYEE') {
      return 'ROLE_EMPLOYE';
    }
    return normalized;
  }
}