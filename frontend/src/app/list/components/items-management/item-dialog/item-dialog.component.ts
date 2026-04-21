import {
  Component,
  OnInit,
  inject,
  signal,
  ChangeDetectionStrategy,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormBuilder,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
  AbstractControl,
  ValidationErrors,
  ValidatorFn,
} from '@angular/forms';
import {
  MatDialogModule,
  MatDialogRef,
  MAT_DIALOG_DATA,
} from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import {
  ItemRequestDTO,
  ItemResponseDTO,
} from '../../../../shared/interfaces/item.interface';
import { CategoryService } from '../../../../shared/services/category.service';
import { UnitService } from '../../../../shared/services/unit.service';
import { ItemService } from '../../../../shared/services/item.service';
import { Category } from '../../../../shared/interfaces/category.interface';
import { Unit } from '../../../../shared/interfaces/unit.interface';
import { FeedbackService } from '../../../../shared/services/feedback.service';

export function duplicateNameValidator(
  existingNames: string[],
  originalName?: string,
): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    if (!control.value) return null;
    const name = control.value.trim().toLowerCase();
    if (originalName && name === originalName.trim().toLowerCase()) return null;
    return existingNames.some(
      (existingName) => existingName.trim().toLowerCase() === name,
    )
      ? { duplicateName: true }
      : null;
  };
}

@Component({
  selector: 'app-item-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule,
  ],
  templateUrl: './item-dialog.component.html',
  styleUrls: ['./item-dialog.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ItemDialogComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly itemService = inject(ItemService);
  private readonly categoryService = inject(CategoryService);
  private readonly unitService = inject(UnitService);
  private readonly feedback = inject(FeedbackService);
  readonly dialogRef = inject(MatDialogRef<ItemDialogComponent>);
  readonly data = inject(MAT_DIALOG_DATA) as { item?: ItemRequestDTO };

  readonly itemForm: FormGroup<{
    name: FormControl<string>;
    idCategory: FormControl<number | null>;
    idUnit: FormControl<number | null>;
  }> = this.fb.group({
    name: this.fb.nonNullable.control('', [Validators.required]),
    idCategory: this.fb.control<number | null>(null, [Validators.required]),
    idUnit: this.fb.control<number | null>(null, [Validators.required]),
  });

  readonly categories = signal<Category[]>([]);
  readonly units = signal<Unit[]>([]);
  readonly existingItems = signal<ItemResponseDTO[]>([]);

  ngOnInit(): void {
    if (this.data.item) {
      this.itemForm.patchValue(this.data.item);
    }
    this.loadCategories();
    this.loadUnits();
    this.loadItems();
  }

  loadItems(): void {
    this.itemService.getAllItems().subscribe({
      next: (items) => {
        this.existingItems.set(items);
        this.updateNameValidator();
      },
      error: () => {
        this.feedback.error('Error loading items for validation');
      },
    });
  }

  updateNameValidator(): void {
    const names = this.existingItems().map((i) => i.name);
    const originalName = this.data.item ? this.data.item.name : undefined;

    this.itemForm
      .get('name')
      ?.addValidators(duplicateNameValidator(names, originalName));
    this.itemForm.get('name')?.updateValueAndValidity();
  }

  loadCategories(): void {
    this.categoryService.getAllCategories().subscribe((categories) => {
      this.categories.set(categories);
    });
  }

  loadUnits(): void {
    this.unitService.getAllUnits().subscribe((units) => {
      this.units.set(units);
    });
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  onSave(): void {
    if (this.itemForm.valid) {
      const { name, idCategory, idUnit } = this.itemForm.getRawValue();
      if (idCategory === null || idUnit === null) return;

      this.dialogRef.close({
        name,
        idCategory,
        idUnit,
      });
    }
  }
}
