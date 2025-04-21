import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Category } from '../../interfaces/category.interface';
import { CategoryService } from '../../services/category.service';

@Component({
  standalone: true,
  selector: 'app-category',
  templateUrl: './category.component.html',
  styleUrls: ['./category.component.scss'],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    MatProgressSpinnerModule
  ]
})
export class CategoryComponent implements OnInit {
  categories: Category[] = [];
  isLoading = false;
  categoryForm: FormGroup;
  editingCategoryId: number | null = null;

  constructor(
    private categoryService: CategoryService,
    private fb: FormBuilder,
    private snackBar: MatSnackBar
  ) {
    this.categoryForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2)]]
    });
  }

  ngOnInit(): void {
    this.loadCategories();
  }

  loadCategories(): void {
    this.isLoading = true;
    this.categoryService.getAllCategories().subscribe({
      next: (categories: Category[]) => {
        this.categories = categories;
        this.isLoading = false;
      },
      error: (error: Error) => {
        this.snackBar.open('Error loading categories', 'Close', { duration: 3000 });
        this.isLoading = false;
      }
    });
  }

  onSubmit(): void {
    if (this.categoryForm.invalid) return;

    const categoryData: Category = {
      name: this.categoryForm.value.name
    };

    if (this.editingCategoryId !== null) {
      categoryData.id = this.editingCategoryId;
    }

    const operation = this.editingCategoryId !== null
      ? this.categoryService.updateCategory(categoryData)
      : this.categoryService.addCategory(categoryData);

    operation.subscribe({
      next: () => {
        this.snackBar.open(
          this.editingCategoryId !== null ? 'Category updated successfully' : 'Category created successfully',
          'Close',
          { duration: 3000 }
        );
        this.resetForm();
        this.loadCategories();
      },
      error: (error: Error) => {
        this.snackBar.open('Error saving category', 'Close', { duration: 3000 });
      }
    });
  }

  startEdit(category: Category): void {
    this.editingCategoryId = category.id ?? null;
    this.categoryForm.patchValue({
      name: category.name
    });
  }

  deleteCategory(id: number): void {
    if (confirm('Are you sure you want to delete this category?')) {
      this.categoryService.deleteCategory(id).subscribe({
        next: () => {
          this.snackBar.open('Category deleted successfully', 'Close', { duration: 3000 });
          this.loadCategories();
        },
        error: (error: Error) => {
          this.snackBar.open('Error deleting category', 'Close', { duration: 3000 });
        }
      });
    }
  }

  resetForm(): void {
    this.categoryForm.reset();
    this.editingCategoryId = null;
  }
}
