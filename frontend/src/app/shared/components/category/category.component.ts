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
  readonly categories = signal<Category[]>([]);
  readonly isLoading = signal(false);
  categoryForm: FormGroup;
  readonly editingCategoryId = signal<number | null>(null);

  private confirmDialog = inject(ConfirmDialogService);
  private feedback = inject(FeedbackService);

  constructor(
    private categoryService: CategoryService,
    private fb: FormBuilder,
  ) {
    this.categoryForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2)]],
    });
  }

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
        this.feedback.error('Erro ao carregar categorias');
        this.isLoading.set(false);
      },
    });
  }

  onSubmit(): void {
    if (this.categoryForm.invalid) return;

    const categoryData: Category = {
      name: this.categoryForm.value.name,
    };

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
            ? 'Categoria atualizada com sucesso'
            : 'Categoria criada com sucesso',
        );
        this.resetForm();
        this.loadCategories();
      },
      error: () => {
        this.feedback.error('Erro ao salvar categoria');
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
        title: 'Excluir categoria',
        message: 'Tem certeza que deseja excluir esta categoria?',
        confirmText: 'Excluir',
      })
      .subscribe((confirmed) => {
        if (!confirmed) return;

        this.categoryService.deleteCategory(id).subscribe({
          next: () => {
            this.feedback.success('Categoria excluída com sucesso');
            this.loadCategories();
          },
          error: () => {
            this.feedback.error('Erro ao excluir categoria');
          },
        });
      });
  }

  resetForm(): void {
    this.categoryForm.reset();
    this.editingCategoryId.set(null);
  }
}
