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
  console.log('AuthInterceptor - Content-Type:', req.headers.get('Content-Type'));

  // Cloner la requête et ajouter le header Authorization
  let authReq = req;
  if (token) {
    let headers = req.headers.set('Authorization', `Bearer ${token}`);

    // Ne pas définir Content-Type pour GET et DELETE
    if (req.method !== 'GET' && req.method !== 'DELETE') {
      // Ne forcer Content-Type que s'il n'est pas déjà défini (ex: multipart/form-data)
      if (!req.headers.has('Content-Type')) {
        headers = headers.set('Content-Type', 'application/json');
      }
    }

    authReq = req.clone({ headers });
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
