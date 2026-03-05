import {
  ChangeDetectionStrategy,
  Component,
  OnInit,
  signal,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { CategoryService } from '../../../shared/services/category.service';
import { Category } from '../../../shared/interfaces/category.interface';
import { CategoryDialogComponent } from '../category-dialog/category-dialog.component';

@Component({
  selector: 'app-categories-management',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './categories-management.component.html',
  styleUrls: ['./categories-management.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CategoriesManagementComponent implements OnInit {
  readonly categories = signal<Category[]>([]);
  readonly isLoading = signal(false);

  constructor(
    private categoryService: CategoryService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
  ) {}

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
        this.snackBar.open('Erro ao carregar categorias', 'Fechar', {
          duration: 3000,
        });
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
    if (confirm('Tem certeza que deseja excluir esta categoria?')) {
      this.categoryService.deleteCategory(id).subscribe({
        next: () => {
          this.snackBar.open('Categoria excluída com sucesso', 'Fechar', {
            duration: 3000,
          });
          this.loadCategories();
        },
        error: () => {
          this.snackBar.open('Erro ao excluir categoria', 'Fechar', {
            duration: 3000,
          });
        },
      });
    }
  }
}
