import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CategoryDialogComponent } from './category-dialog.component';
import { ReactiveFormsModule } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { CategoryService } from '../../../shared/services/category.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { of } from 'rxjs';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';

describe('CategoryDialogComponent - Duplicate Validation', () => {
  let component: CategoryDialogComponent;
  let fixture: ComponentFixture<CategoryDialogComponent>;
  let categoryServiceSpy: jasmine.SpyObj<CategoryService>;

  const mockCategories = [
    { id: 1, name: 'Fruits', createdAt: '', updatedAt: '', deleted: false },
    { id: 2, name: 'Dairy', createdAt: '', updatedAt: '', deleted: false },
  ];

  beforeEach(async () => {
    categoryServiceSpy = jasmine.createSpyObj('CategoryService', [
      'getAllCategories',
      'addCategory',
      'updateCategory',
    ]);
    categoryServiceSpy.getAllCategories.and.returnValue(of(mockCategories));

    await TestBed.configureTestingModule({
      imports: [
        CategoryDialogComponent,
        ReactiveFormsModule,
        NoopAnimationsModule,
      ],
      providers: [
        {
          provide: MatDialogRef,
          useValue: { close: jasmine.createSpy('close') },
        },
        { provide: MAT_DIALOG_DATA, useValue: { isEdit: false } },
        { provide: CategoryService, useValue: categoryServiceSpy },
        { provide: MatSnackBar, useValue: { open: jasmine.createSpy('open') } },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(CategoryDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges(); // Triggers ngOnInit and loadCategories
  });

  it('should be invalid if name is a duplicate (case-insensitive and whitespace)', () => {
    const nameControl = component.categoryForm.get('name');

    nameControl?.setValue('Fruits');
    expect(nameControl?.hasError('duplicateName')).toBeTrue();

    nameControl?.setValue('  fruits  ');
    expect(nameControl?.hasError('duplicateName')).toBeTrue();

    nameControl?.setValue('Meat');
    expect(nameControl?.hasError('duplicateName')).toBeFalse();
  });

  it('should disable the submit button when the form is invalid', () => {
    const submitBtn = fixture.nativeElement.querySelector(
      'button[color="primary"]',
    );
    const nameControl = component.categoryForm.get('name');

    // Case: Empty name
    nameControl?.setValue('');
    fixture.detectChanges();
    expect(submitBtn.disabled).toBeTrue();

    // Case: Duplicate name
    nameControl?.setValue('Fruits');
    fixture.detectChanges();
    expect(submitBtn.disabled).toBeTrue();

    // Case: Valid name
    nameControl?.setValue('Vegetables');
    fixture.detectChanges();
    expect(submitBtn.disabled).toBeFalse();
  });

  it('should be valid when editing and keeping the same name', async () => {
    // Reconfigure for Edit Mode
    TestBed.resetTestingModule();
    const originalCategory = mockCategories[0]; // "Fruits"

    await TestBed.configureTestingModule({
      imports: [
        CategoryDialogComponent,
        ReactiveFormsModule,
        NoopAnimationsModule,
      ],
      providers: [
        {
          provide: MatDialogRef,
          useValue: { close: jasmine.createSpy('close') },
        },
        {
          provide: MAT_DIALOG_DATA,
          useValue: { isEdit: true, category: originalCategory },
        },
        { provide: CategoryService, useValue: categoryServiceSpy },
        { provide: MatSnackBar, useValue: { open: jasmine.createSpy('open') } },
      ],
    }).compileComponents();

    const editFixture = TestBed.createComponent(CategoryDialogComponent);
    const editComponent = editFixture.componentInstance;
    editFixture.detectChanges();

    const nameControl = editComponent.categoryForm.get('name');

    // Should be valid with its own name
    nameControl?.setValue('Fruits');
    expect(nameControl?.hasError('duplicateName')).toBeFalse();

    // Should be invalid with ANOTHER existing name
    nameControl?.setValue('Dairy');
    expect(nameControl?.hasError('duplicateName')).toBeTrue();
  });
});
