import { TestBed } from '@angular/core/testing';
import {
  HttpClient,
  provideHttpClient,
  withInterceptors,
} from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { Router } from '@angular/router';
import { authInterceptor } from './auth.interceptor';
import { AuthService } from '../services/auth.service';

describe('authInterceptor', () => {
  let httpClient: HttpClient;
  let httpTestingController: HttpTestingController;
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let routerSpy: jasmine.SpyObj<Router>;

  beforeEach(() => {
    authServiceSpy = jasmine.createSpyObj('AuthService', [
      'getToken',
      'logout',
    ]);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: authServiceSpy },
        { provide: Router, useValue: routerSpy },
      ],
    });

    httpClient = TestBed.inject(HttpClient);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('should add an Authorization header when token is present', () => {
    authServiceSpy.getToken.and.returnValue('fake-token');

    httpClient.get('/api/test').subscribe();

    const result = httpTestingController.expectOne('/api/test');
    expect(result.request.headers.has('Authorization')).toBeTrue();
    expect(result.request.headers.get('Authorization')).toBe(
      'Bearer fake-token',
    );
  });

  it('should NOT add an Authorization header when token is absent', () => {
    authServiceSpy.getToken.and.returnValue(null);

    httpClient.get('/api/test').subscribe();

    const result = httpTestingController.expectOne('/api/test');
    expect(result.request.headers.has('Authorization')).toBeFalse();
  });

  it('should logout and redirect to login on 401 Unauthorized', () => {
    authServiceSpy.getToken.and.returnValue('fake-token');

    httpClient.get('/api/test').subscribe({
      error: (error) => expect(error.status).toBe(401),
    });

    const result = httpTestingController.expectOne('/api/test');
    result.flush('Unauthorized', { status: 401, statusText: 'Unauthorized' });

    expect(authServiceSpy.logout).toHaveBeenCalled();
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/login']);
  });

  it('should logout and redirect to login on 403 Forbidden', () => {
    authServiceSpy.getToken.and.returnValue('fake-token');

    httpClient.get('/api/test').subscribe({
      error: (error) => expect(error.status).toBe(403),
    });

    const result = httpTestingController.expectOne('/api/test');
    result.flush('Forbidden', { status: 403, statusText: 'Forbidden' });

    expect(authServiceSpy.logout).toHaveBeenCalled();
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/login']);
  });

  it('should logout and redirect to login on 401 even without a token', () => {
    authServiceSpy.getToken.and.returnValue(null);

    httpClient.get('/api/test').subscribe({
      error: (error) => expect(error.status).toBe(401),
    });

    const result = httpTestingController.expectOne('/api/test');
    result.flush('Unauthorized', { status: 401, statusText: 'Unauthorized' });

    expect(authServiceSpy.logout).toHaveBeenCalled();
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/login']);
  });
});
