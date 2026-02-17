import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ItemDialogComponent } from './item-dialog.component';
import { ReactiveFormsModule } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { ItemService } from '../../../../shared/services/item.service';
import { CategoryService } from '../../../../shared/services/category.service';
import { UnitService } from '../../../../shared/services/unit.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { of } from 'rxjs';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';

describe('ItemDialogComponent - Duplicate Validation', () => {
  let component: ItemDialogComponent;
  let fixture: ComponentFixture<ItemDialogComponent>;
  let itemServiceSpy: jasmine.SpyObj<ItemService>;
  let categoryServiceSpy: jasmine.SpyObj<CategoryService>;
  let unitServiceSpy: jasmine.SpyObj<UnitService>;

  const mockItems = [
    { id: 1, name: 'Apple', category: { id: 1, name: 'Fruits' }, unit: { id: 1, name: 'kg', symbol: 'kg' } }
  ] as any;

  beforeEach(async () => {
    itemServiceSpy = jasmine.createSpyObj('ItemService', ['getAllItems']);
    categoryServiceSpy = jasmine.createSpyObj('CategoryService', ['getAllCategories']);
    unitServiceSpy = jasmine.createSpyObj('UnitService', ['getAllUnits']);

    itemServiceSpy.getAllItems.and.returnValue(of(mockItems));
    categoryServiceSpy.getAllCategories.and.returnValue(of([]));
    unitServiceSpy.getAllUnits.and.returnValue(of([]));

    await TestBed.configureTestingModule({
      imports: [
        ItemDialogComponent,
        ReactiveFormsModule,
        NoopAnimationsModule
      ],
      providers: [
        { provide: MatDialogRef, useValue: { close: jasmine.createSpy('close') } },
        { provide: MAT_DIALOG_DATA, useValue: { isEdit: false } },
        { provide: ItemService, useValue: itemServiceSpy },
        { provide: CategoryService, useValue: categoryServiceSpy },
        { provide: UnitService, useValue: unitServiceSpy },
        { provide: MatSnackBar, useValue: { open: jasmine.createSpy('open') } }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ItemDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be invalid if name is a duplicate (including whitespace)', () => {
    const nameControl = component.itemForm.get('name');
    nameControl?.setValue('Apple');
    expect(nameControl?.hasError('duplicateName')).toBeTrue();

    nameControl?.setValue('  apple  ');
    expect(nameControl?.hasError('duplicateName')).toBeTrue();

    nameControl?.setValue('Banana');
    expect(nameControl?.hasError('duplicateName')).toBeFalse();
  });

  it('should be valid when editing and keeping its own name', async () => {
    // Reconfigure for Edit Mode
    TestBed.resetTestingModule();
    const originalItem = { name: 'Apple', idCategory: 1, idUnit: 1 };
    
    await TestBed.configureTestingModule({
      imports: [ItemDialogComponent, ReactiveFormsModule, NoopAnimationsModule],
      providers: [
        { provide: MatDialogRef, useValue: { close: jasmine.createSpy('close') } },
        { provide: MAT_DIALOG_DATA, useValue: { item: originalItem } },
        { provide: ItemService, useValue: itemServiceSpy },
        { provide: CategoryService, useValue: categoryServiceSpy },
        { provide: UnitService, useValue: unitServiceSpy },
        { provide: MatSnackBar, useValue: { open: jasmine.createSpy('open') } }
      ]
    }).compileComponents();

    const editFixture = TestBed.createComponent(ItemDialogComponent);
    const editComponent = editFixture.componentInstance;
    editFixture.detectChanges();

    const nameControl = editComponent.itemForm.get('name');
    nameControl?.setValue('Apple');
    expect(nameControl?.hasError('duplicateName')).toBeFalse();
  });
});
