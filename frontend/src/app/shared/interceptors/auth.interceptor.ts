import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, tap } from 'rxjs';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const token = authService.getToken();

  console.log('Interceptor - Token:', token ? 'Present' : 'Missing');
  console.log('Interceptor - Request URL:', req.url);

  if (token) {
    const authReq = req.clone({
      headers: req.headers.set('Authorization', `Bearer ${token}`)
    });

    return next(authReq).pipe(
      tap(response => {
        console.log('Interceptor - Response received:', response);
      }),
      catchError((error: HttpErrorResponse) => {
        console.error('Interceptor - Error:', error);

        if (error.status === 401 || error.status === 403) {
          console.log('Interceptor - Unauthorized, redirecting to login');
          authService.logout();
          router.navigate(['/login']);
        }

        throw error;
      })
    );
  }

  return next(req);
};
