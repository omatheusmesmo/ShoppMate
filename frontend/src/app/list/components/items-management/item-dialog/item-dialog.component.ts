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
import { MatSnackBar } from '@angular/material/snack-bar';

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
  private readonly snackBar = inject(MatSnackBar);
  readonly dialogRef = inject(MatDialogRef<ItemDialogComponent>);
  readonly data = inject(MAT_DIALOG_DATA) as { item?: ItemRequestDTO };

  readonly itemForm = this.fb.group({
    name: ['', [Validators.required]],
    idCategory: [null as number | null, [Validators.required]],
    idUnit: [null as number | null, [Validators.required]],
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
        this.snackBar.open('Erro ao carregar itens para validação', 'Fechar', {
          duration: 3000,
        });
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
      this.dialogRef.close(this.itemForm.value);
    }
  }
}
