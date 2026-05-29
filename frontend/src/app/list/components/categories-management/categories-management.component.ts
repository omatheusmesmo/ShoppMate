import { ChangeDetectionStrategy, Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog } from '@angular/material/dialog';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { CategoryService } from '../../../shared/services/category.service';
import { Category } from '../../../shared/interfaces/category.interface';
import { CategoryDialogComponent } from '../category-dialog/category-dialog.component';
import { ConfirmDialogService } from '../../../shared/services/confirm-dialog.service';
import { FeedbackService } from '../../../shared/services/feedback.service';

@Component({
  selector: 'app-categories-management',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatButtonModule, MatIconModule, MatProgressSpinnerModule],
  templateUrl: './categories-management.component.html',
  styleUrls: ['./categories-management.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CategoriesManagementComponent implements OnInit {
  private categoryService = inject(CategoryService);
  private dialog = inject(MatDialog);
  private confirmDialog = inject(ConfirmDialogService);
  private feedback = inject(FeedbackService);

  readonly categories = signal<Category[]>([]);
  readonly isLoading = signal(false);

  ngOnInit(): void {
    this.loadCategories();
  }

  loadCategories(): void {
    this.isLoading.set(true);
    this.categoryService.getAllCategories().subscribe({
      next: (categories) => {
        this.categories.set(categories);
        this.isLoading.set(false);
      },
      error: () => {
        this.feedback.error('Erro ao carregar categorias');
        this.isLoading.set(false);
      },
    });
  }

  openEditDialog(category: Category): void {
    const dialogRef = this.dialog.open(CategoryDialogComponent, {
      width: '400px',
      data: { category, isEdit: true },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.loadCategories();
      }
    });
  }

  openNewCategoryDialog(): void {
    const dialogRef = this.dialog.open(CategoryDialogComponent, {
      width: '400px',
      data: { isEdit: false },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.loadCategories();
      }
    });
  }

  deleteCategory(id: number): void {
    this.confirmDialog
      .open({
        title: 'Delete category',
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
}
