import {
  ChangeDetectionStrategy,
  Component,
  OnInit,
  inject,
  signal,
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
import { MatSelectModule } from '@angular/material/select';
import {
  ItemResponseDTO,
  ItemRequestDTO,
} from '../../interfaces/item.interface';
import { Category } from '../../interfaces/category.interface';
import { Unit } from '../../interfaces/unit.interface';
import { ItemService } from '../../services/item.service';
import { CategoryService } from '../../services/category.service';
import { UnitService } from '../../services/unit.service';
import { forkJoin } from 'rxjs';
import { ConfirmDialogService } from '../../services/confirm-dialog.service';
import { FeedbackService } from '../../services/feedback.service';

@Component({
  standalone: true,
  selector: 'app-item',
  templateUrl: './item.component.html',
  styleUrls: ['./item.component.scss'],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    MatProgressSpinnerModule,
    MatSelectModule,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ItemComponent implements OnInit {
  private itemService = inject(ItemService);
  private categoryService = inject(CategoryService);
  private unitService = inject(UnitService);
  private fb = inject(FormBuilder);

  itemForm: FormGroup = this.fb.group({
    name: ['', [Validators.required, Validators.minLength(2)]],
    idCategory: ['', Validators.required],
    idUnit: ['', Validators.required],
  });
  readonly items = signal<ItemResponseDTO[]>([]);
  readonly categories = signal<Category[]>([]);
  readonly units = signal<Unit[]>([]);
  readonly isLoading = signal(false);
  readonly editingItemId = signal<number | null>(null);

  private confirmDialog = inject(ConfirmDialogService);
  private feedback = inject(FeedbackService);

  ngOnInit(): void {
    this.loadInitialData();
  }

  loadInitialData(): void {
    this.isLoading.set(true);
    forkJoin({
      items: this.itemService.getAllItems(),
      categories: this.categoryService.getAllCategories(),
      units: this.unitService.getAllUnits(),
    }).subscribe({
      next: (data) => {
        this.items.set(data.items);
        this.categories.set(data.categories);
        this.units.set(data.units);
        this.isLoading.set(false);
      },
      error: () => {
        this.feedback.error('Error loading data');
        this.isLoading.set(false);
      },
    });
  }

  onSubmit(): void {
    if (this.itemForm.invalid) return;

    const { name, idCategory, idUnit } = this.itemForm.getRawValue();
    if (idCategory === null || idUnit === null) return;

    const itemData: ItemRequestDTO = { name, idCategory, idUnit };

    const operation = this.editingItemId()
      ? this.itemService.updateItem(this.editingItemId()!, itemData)
      : this.itemService.addItem(itemData);

    operation.subscribe({
      next: () => {
        this.feedback.success(
          this.editingItemId()
            ? 'Item updated successfully'
            : 'Item created successfully',
        );
        this.resetForm();
        this.loadInitialData();
      },
      error: () => {
        this.feedback.error('Error saving item');
      },
    });
  }

  startEdit(item: ItemResponseDTO): void {
    this.editingItemId.set(item.id);
    this.itemForm.patchValue({
      name: item.name,
      idCategory: item.category.id,
      idUnit: item.unit.id,
    });
  }

  deleteItem(id: number): void {
    this.confirmDialog
      .open({
        title: 'Delete Item',
        message: 'Are you sure you want to delete this item?',
        confirmText: 'Delete',
      })
      .subscribe((confirmed) => {
        if (!confirmed) return;

        this.itemService.deleteItem(id).subscribe({
          next: () => {
            this.feedback.success('Item deleted successfully');
            this.loadInitialData();
          },
          error: () => {
            this.feedback.error('Error deleting item');
          },
        });
      });
  }

  resetForm(): void {
    this.itemForm.reset();
    this.editingItemId.set(null);
  }
}
