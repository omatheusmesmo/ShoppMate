import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable, catchError, tap, throwError } from 'rxjs';
import { LoginRequest, User } from '../interfaces/user.interface';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);

  private readonly AUTH_TOKEN_KEY = 'auth_token';
  private isLoggedInSubject = new BehaviorSubject<boolean>(this.hasToken());
  private readonly API_BASE_URL = '/api';

  isLoggedIn$ = this.isLoggedInSubject.asObservable();

  login(loginRequest: LoginRequest): Observable<string> {
    return this.http
      .post(`${this.API_BASE_URL}/auth/login`, loginRequest, {
        responseType: 'text' as const,
      })
      .pipe(
        tap((token) => {
          localStorage.setItem(this.AUTH_TOKEN_KEY, token);
          this.isLoggedInSubject.next(true);
        }),
        catchError((error: HttpErrorResponse) => {
          if (error.status === 403) {
            return throwError(() => new Error('Credenciais inválidas'));
          }
          return throwError(() => new Error('Erro ao fazer login. Tente novamente mais tarde.'));
        }),
      );
  }

  register(user: User): Observable<User> {
    return this.http.post<User>(`${this.API_BASE_URL}/auth/sign`, user).pipe(
      catchError(() => {
        return throwError(
          () => new Error('Erro ao registrar usuário. Tente novamente mais tarde.'),
        );
      }),
    );
  }

  logout(): void {
    localStorage.removeItem(this.AUTH_TOKEN_KEY);
    this.isLoggedInSubject.next(false);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return localStorage.getItem(this.AUTH_TOKEN_KEY);
  }

  private hasToken(): boolean {
    return !!this.getToken();
  }
}
