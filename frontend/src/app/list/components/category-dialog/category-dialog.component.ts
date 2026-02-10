import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { CategoryService } from '../../../shared/services/category.service';
import { Category } from '../../../shared/interfaces/category.interface';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatDialogModule } from '@angular/material/dialog';

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
})
export class CategoryDialogComponent {
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
