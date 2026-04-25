import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SignupComponent } from './signup.component';
import { AuthService } from '../../shared/services/auth.service';
import { MockAuthService } from '../../shared/mocks/mock-services';
import { FeedbackService } from '../../shared/services/feedback.service';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { provideRouter } from '@angular/router';

describe('SignupComponent', () => {
  let component: SignupComponent;
  let fixture: ComponentFixture<SignupComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SignupComponent, NoopAnimationsModule],
      providers: [
        { provide: AuthService, useClass: MockAuthService },
        {
          provide: FeedbackService,
          useValue: { success: jasmine.createSpy('success'), error: jasmine.createSpy('error') },
        },
        provideRouter([]),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(SignupComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
