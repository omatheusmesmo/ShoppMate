import { HttpClient, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable, catchError, tap, throwError } from 'rxjs';
import { LoginRequest, User } from '../interfaces/user.interface';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);

  private readonly AUTH_TOKEN_KEY = 'auth_token';
  private isLoggedInSubject = new BehaviorSubject<boolean>(this.hasToken());
  private readonly API_BASE_URL = '/api';

  isLoggedIn$ = this.isLoggedInSubject.asObservable();

  private readonly httpOptions = {
    headers: new HttpHeaders({
      'Content-Type': 'application/json',
      'Accept': 'text/plain'
    }),
    responseType: 'text' as const
  };

  login(loginRequest: LoginRequest): Observable<string> {
    console.log('Attempting login with URL:', `${this.API_BASE_URL}/auth/login`);
    return this.http.post(`${this.API_BASE_URL}/auth/login`, loginRequest, this.httpOptions)
      .pipe(
        tap(token => {
          console.log('Login successful, token received');
          localStorage.setItem(this.AUTH_TOKEN_KEY, token);
          this.isLoggedInSubject.next(true);
        }),
        catchError((error: HttpErrorResponse) => {
          console.error('Login error:', error);
          if (error.status === 403) {
            return throwError(() => new Error('Credenciais inválidas'));
          }
          return throwError(() => new Error('Erro ao fazer login. Tente novamente mais tarde.'));
        })
      );
  }

  register(user: User): Observable<User> {
    console.log('Attempting registration with URL:', `${this.API_BASE_URL}/auth/sign`);
    return this.http.post<User>(`${this.API_BASE_URL}/auth/sign`, user, {
      headers: new HttpHeaders({
        'Content-Type': 'application/json',
        'Accept': 'application/json'
      })
    })
      .pipe(
        tap(() => console.log('Registration successful')),
        catchError((error: HttpErrorResponse) => {
          console.error('Registration error:', error);
          return throwError(() => new Error('Erro ao registrar usuário. Tente novamente mais tarde.'));
        })
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
