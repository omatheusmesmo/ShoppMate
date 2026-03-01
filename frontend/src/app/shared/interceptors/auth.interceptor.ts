import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError } from 'rxjs';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const token = authService.getToken();

  console.log('Interceptor - Token:', token ? 'Present' : 'Missing');
  console.log('Interceptor - Request URL:', req.url);

  let nextRequest = req;
  if (token) {
    nextRequest = req.clone({
      headers: req.headers.set('Authorization', `Bearer ${token}`),
    });
  }

  return next(nextRequest).pipe(
    catchError((error: HttpErrorResponse) => {
      console.error('Interceptor - Error:', error);

      if (error.status === 401 || error.status === 403) {
        console.log('Interceptor - Unauthorized, redirecting to login');
        authService.logout();
        router.navigate(['/login']);
      }

      throw error;
    }),
  );
};
