import {
  ChangeDetectionStrategy,
  Component,
  OnInit,
  signal,
  inject,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormBuilder,
  FormGroup,
  Validators,
  ReactiveFormsModule,
  FormControl,
} from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Category } from '../../interfaces/category.interface';
import { CategoryService } from '../../services/category.service';
import { ConfirmDialogService } from '../../services/confirm-dialog.service';
import { FeedbackService } from '../../services/feedback.service';

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
    MatProgressSpinnerModule,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CategoryComponent implements OnInit {
  private categoryService = inject(CategoryService);
  private fb = inject(FormBuilder);

  readonly categories = signal<Category[]>([]);
  readonly isLoading = signal(false);
  categoryForm: FormGroup<{ name: FormControl<string> }> =
    this.fb.nonNullable.group({
      name: this.fb.nonNullable.control('', {
        validators: [Validators.required, Validators.minLength(2)],
      }),
    });
  readonly editingCategoryId = signal<number | null>(null);

  private confirmDialog = inject(ConfirmDialogService);
  private feedback = inject(FeedbackService);

  ngOnInit(): void {
    this.loadCategories();
  }

  loadCategories(): void {
    this.isLoading.set(true);
    this.categoryService.getAllCategories().subscribe({
      next: (categories: Category[]) => {
        this.categories.set(categories);
        this.isLoading.set(false);
      },
      error: () => {
        this.feedback.error('Error loading categories');
        this.isLoading.set(false);
      },
    });
  }

  onSubmit(): void {
    if (this.categoryForm.invalid) return;

    const { name } = this.categoryForm.getRawValue();
    const categoryData: Category = { name };

    if (this.editingCategoryId() !== null) {
      categoryData.id = this.editingCategoryId()!;
    }

    const operation =
      this.editingCategoryId() !== null
        ? this.categoryService.updateCategory(categoryData)
        : this.categoryService.addCategory(categoryData);

    operation.subscribe({
      next: () => {
        this.feedback.success(
          this.editingCategoryId() !== null
            ? 'Category updated successfully'
            : 'Category created successfully',
        );
        this.resetForm();
        this.loadCategories();
      },
      error: () => {
        this.feedback.error('Error saving category');
      },
    });
  }

  startEdit(category: Category): void {
    this.editingCategoryId.set(category.id ?? null);
    this.categoryForm.patchValue({
      name: category.name,
    });
  }

  deleteCategory(id: number): void {
    this.confirmDialog
      .open({
        title: 'Delete Category',
        message: 'Are you sure you want to delete this category?',
        confirmText: 'Delete',
      })
      .subscribe((confirmed) => {
        if (!confirmed) return;

        this.categoryService.deleteCategory(id).subscribe({
          next: () => {
            this.feedback.success('Category deleted successfully');
            this.loadCategories();
          },
          error: () => {
            this.feedback.error('Error deleting category');
          },
        });
      });
  }

  resetForm(): void {
    this.categoryForm.reset();
    this.editingCategoryId.set(null);
  }
}
