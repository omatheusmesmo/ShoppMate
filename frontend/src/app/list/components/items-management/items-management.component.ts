import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDialog } from '@angular/material/dialog';
import { AsyncPipe } from '@angular/common';
import { finalize, Observable } from 'rxjs';
import { ItemDialogComponent } from './item-dialog/item-dialog.component';

import { ItemService } from '../../../shared/services/item.service';
import { CategoryService } from '../../../shared/services/category.service';
import { UnitService } from '../../../shared/services/unit.service';
import { ItemResponseDTO } from '../../../shared/interfaces/item.interface';
import { Category } from '../../../shared/interfaces/category.interface';
import { Unit } from '../../../shared/interfaces/unit.interface';
import { ConfirmDialogService } from '../../../shared/services/confirm-dialog.service';
import { FeedbackService } from '../../../shared/services/feedback.service';

@Component({
  selector: 'app-items-management',
  standalone: true,
  imports: [
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    MatSelectModule,
    MatTableModule,
    MatPaginatorModule,
    MatProgressSpinnerModule,
    ReactiveFormsModule,
    AsyncPipe,
  ],
  templateUrl: './items-management.component.html',
  styleUrls: ['./items-management.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ItemsManagementComponent implements OnInit {
  private itemService = inject(ItemService);
  private categoryService = inject(CategoryService);
  private unitService = inject(UnitService);
  private dialog = inject(MatDialog);
  private confirmDialog = inject(ConfirmDialogService);
  private feedback = inject(FeedbackService);
  private fb = inject(FormBuilder);

  readonly loading = signal(true);
  items$!: Observable<ItemResponseDTO[]>;
  readonly categories = signal<Category[]>([]);
  readonly units = signal<Unit[]>([]);
  displayedColumns: string[] = ['name', 'category', 'unit', 'actions'];

  ngOnInit(): void {
    this.loadReferenceData();
    this.loadItems();
  }

  loadItems(): void {
    this.loading.set(true);
    this.items$ = this.itemService.getAllItems().pipe(finalize(() => this.loading.set(false)));
  }

  loadReferenceData(): void {
    this.categoryService.getAllCategories().subscribe((categories) => {
      this.categories.set(categories);
    });

    this.unitService.getAllUnits().subscribe((units) => {
      this.units.set(units);
    });
  }

  openAddItemDialog(): void {
    const dialogRef = this.dialog.open(ItemDialogComponent, {
      width: '400px',
      data: { item: null },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.itemService.addItem(result).subscribe({
          next: () => {
            this.loadItems();
            this.feedback.success('Item added successfully');
          },
          error: () => {
            this.feedback.error('Error adding item');
          },
        });
      }
    });
  }

  editItem(item: ItemResponseDTO): void {
    const dialogRef = this.dialog.open(ItemDialogComponent, {
      width: '400px',
      data: {
        item: {
          name: item.name,
          idCategory: item.category.id,
          idUnit: item.unit.id,
        },
      },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.itemService.updateItem(item.id, result).subscribe({
          next: () => {
            this.loadItems();
            this.feedback.success('Item updated successfully');
          },
          error: () => {
            this.feedback.error('Error updating item');
          },
        });
      }
    });
  }

  deleteItem(item: ItemResponseDTO): void {
    this.confirmDialog
      .open({
        title: 'Delete Item',
        message: `Are you sure you want to delete the item "${item.name}"?`,
        confirmText: 'Delete',
      })
      .subscribe((confirmed) => {
        if (!confirmed) return;

        this.itemService.deleteItem(item.id).subscribe({
          next: () => {
            this.loadItems();
            this.feedback.success('Item deleted successfully');
          },
          error: () => {
            this.feedback.error('Error deleting item');
          },
        });
      });
  }
}
