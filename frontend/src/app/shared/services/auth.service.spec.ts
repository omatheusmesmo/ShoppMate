import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { Router } from '@angular/router';
import { AuthService } from './auth.service';
import { MockRouter } from '../../shared/mocks/mock-services';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;
  let routerMock: MockRouter;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AuthService, { provide: Router, useClass: MockRouter }],
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
    routerMock = TestBed.inject(Router) as unknown as MockRouter;
    localStorage.clear();
  });

  it('should login and save token', () => {
    const mockToken = 'fake-jwt-token';
    service.login({ email: 'test@test.com', password: '123' }).subscribe((token) => {
      expect(token).toBe(mockToken);
      expect(localStorage.getItem('auth_token')).toBe(mockToken);
    });

    const req = httpMock.expectOne('/api/auth/login');
    expect(req.request.method).toBe('POST');
    req.flush(mockToken);
  });

  it('should handle 403 forbidden error', () => {
    service.login({ email: 'wrong@test.com', password: '123' }).subscribe({
      error: (err) => expect(err.message).toBe('Credenciais inválidas'),
    });

    const req = httpMock.expectOne('/api/auth/login');
    req.flush('Forbidden', { status: 403, statusText: 'Forbidden' });
  });

  it('should logout, clear token, and navigate to /login', () => {
    localStorage.setItem('auth_token', 'active-token');
    service.logout();

    expect(localStorage.getItem('auth_token')).toBeNull();
    expect(routerMock.navigate).toHaveBeenCalledWith(['/login']);
  });

  afterEach(() => httpMock.verify());
});
