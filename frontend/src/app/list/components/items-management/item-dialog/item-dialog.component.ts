import {
  ChangeDetectionStrategy,
  Component,
  Inject,
  OnInit,
  inject,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormBuilder,
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
  styles: [
    `
      .w-100 {
        width: 100%;
        margin-bottom: 1rem;
      }
    `,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ItemDialogComponent implements OnInit {
  private fb = inject(FormBuilder);
  private itemService = inject(ItemService);
  private snackBar = inject(MatSnackBar);

  itemForm: FormGroup;
  categories: Category[] = [];
  units: Unit[] = [];
  existingItems: ItemResponseDTO[] = [];

  constructor(
    public dialogRef: MatDialogRef<ItemDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { item?: ItemRequestDTO },
    private categoryService: CategoryService,
    private unitService: UnitService,
  ) {
    this.itemForm = this.fb.group({
      name: ['', [Validators.required]],
      idCategory: [null, [Validators.required]],
      idUnit: [null, [Validators.required]],
    });

    if (data.item) {
      this.itemForm.patchValue(data.item);
    }
  }

  ngOnInit(): void {
    this.loadCategories();
    this.loadUnits();
    this.loadItems();
  }

  loadItems(): void {
    this.itemService.getAllItems().subscribe({
      next: (items) => {
        this.existingItems = items;
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
    const names = this.existingItems.map((i) => i.name);
    const originalName = this.data.item ? this.data.item.name : undefined;

    this.itemForm
      .get('name')
      ?.addValidators(duplicateNameValidator(names, originalName));
    this.itemForm.get('name')?.updateValueAndValidity();
  }

  loadCategories(): void {
    this.categoryService.getAllCategories().subscribe((categories) => {
      this.categories = categories;
    });
  }

  loadUnits(): void {
    this.unitService.getAllUnits().subscribe((units) => {
      this.units = units;
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
