import { ComponentFixture, TestBed } from '@angular/core/testing';
import { LoginComponent } from './login.component';
import { AuthService } from '../../shared/services/auth.service';
import { MockAuthService } from '../../shared/mocks/mock-services';
import { FeedbackService } from '../../shared/services/feedback.service';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { provideRouter } from '@angular/router';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LoginComponent, NoopAnimationsModule],
      providers: [
        { provide: AuthService, useClass: MockAuthService },
        {
          provide: FeedbackService,
          useValue: { success: jasmine.createSpy('success'), error: jasmine.createSpy('error') },
        },
        provideRouter([]),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
