import {
  ChangeDetectionStrategy,
  Component,
  OnInit,
  inject,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  ValidationErrors,
  ValidatorFn,
  Validators,
} from '@angular/forms';
import { CategoryService } from '../../../shared/services/category.service';
import { Category } from '../../../shared/interfaces/category.interface';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatDialogModule } from '@angular/material/dialog';

export function duplicateNameValidator(existingNames: string[], originalName?: string): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    if (!control.value) return null;
    const name = control.value.trim().toLowerCase();
    if (originalName && name === originalName.trim().toLowerCase()) return null;
    return existingNames.some(existingName => existingName.trim().toLowerCase() === name)
      ? { duplicateName: true }
      : null;
  };
}

@Component({
  selector: 'app-category-dialog',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    ReactiveFormsModule,
    MatDialogModule,
  ],
  templateUrl: './category-dialog.component.html',
  styleUrls: ['./category-dialog.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CategoryDialogComponent implements OnInit {
  private dialogRef = inject(MatDialogRef<CategoryDialogComponent>);
  private fb = inject(FormBuilder);
  private categoryService = inject(CategoryService);
  private snackBar = inject(MatSnackBar);
  private data = inject(MAT_DIALOG_DATA) as {
    category?: Category;
    isEdit: boolean;
  };

  categoryForm: FormGroup;
  isEdit: boolean;
  existingCategories: Category[] = [];

  constructor() {
    this.isEdit = this.data.isEdit;
    this.categoryForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(3)]],
    });

    if (this.isEdit && this.data.category) {
      this.categoryForm.patchValue({
        name: this.data.category.name,
      });
    }
  }

  ngOnInit(): void {
    this.loadCategories();
  }

  loadCategories(): void {
    this.categoryService.getAllCategories().subscribe({
      next: (categories) => {
        this.existingCategories = categories;
        this.updateNameValidator();
      },
      error: () => {
        this.snackBar.open('Erro ao carregar categorias para validação', 'Fechar', { duration: 3000 });
      }
    });
  }

  updateNameValidator(): void {
    const names = this.existingCategories.map(c => c.name);
    const originalName = this.isEdit && this.data.category ? this.data.category.name : undefined;
    
    this.categoryForm.get('name')?.addValidators(duplicateNameValidator(names, originalName));
    this.categoryForm.get('name')?.updateValueAndValidity();
  }

  onSubmit(): void {
    if (this.categoryForm.valid) {
      const categoryData: Category = {
        ...this.categoryForm.value,
        id:
          this.isEdit && this.data.category ? this.data.category.id : undefined,
        createdAt:
          this.isEdit && this.data.category
            ? this.data.category.createdAt
            : new Date().toISOString(),
        updatedAt: new Date().toISOString(),
        deleted: false,
      };

      if (this.isEdit && this.data.category) {
        this.categoryService.updateCategory(categoryData).subscribe({
          next: () => {
            this.snackBar.open('Categoria atualizada com sucesso', 'Fechar', {
              duration: 3000,
            });
            this.dialogRef.close(true);
          },
          error: () => {
            this.snackBar.open('Erro ao atualizar categoria', 'Fechar', {
              duration: 3000,
            });
          },
        });
      } else {
        this.categoryService.addCategory(categoryData).subscribe({
          next: () => {
            this.snackBar.open('Categoria criada com sucesso', 'Fechar', {
              duration: 3000,
            });
            this.dialogRef.close(true);
          },
          error: () => {
            this.snackBar.open('Erro ao criar categoria', 'Fechar', {
              duration: 3000,
            });
          },
        });
      }
    }
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}
