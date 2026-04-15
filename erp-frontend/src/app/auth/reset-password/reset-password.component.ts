import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
import { AuthService } from '../auth.service';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule],
  templateUrl: './reset-password.component.html',
  styleUrls: ['./reset-password.component.css']
})
export class ResetPasswordComponent implements OnInit {
  resetForm: FormGroup = new FormGroup({});
  token: string = '';
  isLoading = false;
  errorMessage = '';
  successMessage = '';
  formDisabled = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    // Read token from URL query params
    this.route.queryParams.subscribe(params => {
      this.token = params['token'] || '';
      
      // If no token, redirect to forgot-password
      if (!this.token) {
        this.router.navigate(['/forgot-password']);
        return;
      }
    });

    // Initialize form with custom validator for password match
    this.resetForm = this.fb.group({
      newPassword: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', [Validators.required]]
    }, { validators: this.passwordsMatchValidator });
  }

  // Custom validator to check if passwords match
  passwordsMatchValidator(group: FormGroup): { [key: string]: any } | null {
    const newPassword = group.get('newPassword')?.value;
    const confirmPassword = group.get('confirmPassword')?.value;
    return newPassword === confirmPassword ? null : { passwordsMismatch: true };
  }

  onSubmit(): void {
    if (this.resetForm.invalid || this.formDisabled) {
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';

    const { newPassword } = this.resetForm.value;

    this.authService.resetPassword(this.token, newPassword).subscribe({
      next: (response: string) => {
        this.isLoading = false;
        this.successMessage = response;
        this.formDisabled = true;
        this.resetForm.disable();
        
        // Redirect to login after 3 seconds
        setTimeout(() => {
          this.router.navigate(['/']);
        }, 3000);
      },
      error: (error) => {
        this.isLoading = false;
        if (error.status === 400) {
          // Map backend error messages to user-friendly French messages
          const backendMessage = error.error || '';
          if (backendMessage.includes('Token invalide')) {
            this.errorMessage = 'Lien de réinitialisation invalide';
          } else if (backendMessage.includes('Token expiré')) {
            this.errorMessage = 'Lien de réinitialisation expiré, veuillez recommencer';
          } else if (backendMessage.includes('déjà utilisé') || backendMessage.includes('déjà été utilisé')) {
            this.errorMessage = 'Ce lien a déjà été utilisé';
          } else {
            this.errorMessage = backendMessage;
          }
        } else {
          this.errorMessage = 'Erreur lors de la réinitialisation, réessayez plus tard';
        }
      }
    });
  }

  // Helper method to check if passwords don't match
  get passwordsMismatch(): boolean {
    const hasError = this.resetForm.hasError('passwordsMismatch');
    const confirmTouched = this.resetForm.get('confirmPassword')?.touched ?? false;
    const newPassTouched = this.resetForm.get('newPassword')?.touched ?? false;
    return hasError && confirmTouched && newPassTouched;
  }

  goToForgotPassword(): void {
    this.router.navigate(['/forgot-password']);
  }
}
