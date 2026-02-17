import { ComponentFixture, TestBed } from '@angular/core/testing';
import { UnitComponent } from './unit.component';
import { UnitService } from '../../services/unit.service';
import { MockUnitService, MockMatSnackBar } from '../../mocks/mock-services';
import { MatSnackBar } from '@angular/material/snack-bar';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';

describe('UnitComponent', () => {
  let component: UnitComponent;
  let fixture: ComponentFixture<UnitComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UnitComponent, NoopAnimationsModule],
      providers: [
        { provide: UnitService, useClass: MockUnitService },
        { provide: MatSnackBar, useClass: MockMatSnackBar },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(UnitComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
