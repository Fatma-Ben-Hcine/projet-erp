import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from './auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const token = authService.getToken();

  console.log('AuthInterceptor - URL:', req.url);
  console.log('AuthInterceptor - Token:', token ? 'présent' : 'absent');

  // Cloner la requête et ajouter le header Authorization
  let authReq = req;
  if (token) {
    authReq = req.clone({
      headers: req.headers.set('Authorization', `Bearer ${token}`)
    });
    console.log('AuthInterceptor - Header ajouté');
  }

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {
        // Token expiré ou invalide - silently logout and redirect
        authService.logout();
        router.navigate(['/login']);
      }
      return throwError(() => error);
    })
  );
};
