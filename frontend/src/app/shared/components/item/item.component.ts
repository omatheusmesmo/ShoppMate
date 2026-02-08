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
import { MatSelectModule } from '@angular/material/select';
import { ItemResponseDTO, ItemRequestDTO } from '../../interfaces/item.interface';
import { Category } from '../../interfaces/category.interface';
import { Unit } from '../../interfaces/unit.interface';
import { ItemService } from '../../services/item.service';
import { CategoryService } from '../../services/category.service';
import { UnitService } from '../../services/unit.service';
import { forkJoin } from 'rxjs';

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
    MatSelectModule
  ]
})
export class ItemComponent implements OnInit {
  items: ItemResponseDTO[] = [];
  categories: Category[] = [];
  units: Unit[] = [];
  isLoading = false;
  itemForm: FormGroup;
  editingItemId: number | null = null;

  constructor(
    private itemService: ItemService,
    private categoryService: CategoryService,
    private unitService: UnitService,
    private fb: FormBuilder,
    private snackBar: MatSnackBar
  ) {
    this.itemForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2)]],
      idCategory: ['', Validators.required],
      idUnit: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    this.loadInitialData();
  }

  loadInitialData(): void {
    this.isLoading = true;
    forkJoin({
      items: this.itemService.getAllItems(),
      categories: this.categoryService.getAllCategories(),
      units: this.unitService.getAllUnits()
    }).subscribe({
      next: (data) => {
        this.items = data.items;
        this.categories = data.categories;
        this.units = data.units;
        this.isLoading = false;
      },
      error: (error) => {
        this.snackBar.open('Error loading data', 'Close', { duration: 3000 });
        this.isLoading = false;
      }
    });
  }

  onSubmit(): void {
    if (this.itemForm.invalid) return;

    const itemData: ItemRequestDTO = {
      name: this.itemForm.value.name,
      idCategory: this.itemForm.value.idCategory,
      idUnit: this.itemForm.value.idUnit
    };

    const operation = this.editingItemId
      ? this.itemService.updateItem(this.editingItemId, itemData)
      : this.itemService.addItem(itemData);

    operation.subscribe({
      next: () => {
        this.snackBar.open(
          this.editingItemId ? 'Item updated successfully' : 'Item created successfully',
          'Close',
          { duration: 3000 }
        );
        this.resetForm();
        this.loadInitialData();
      },
      error: (error) => {
        this.snackBar.open('Error saving item', 'Close', { duration: 3000 });
      }
    });
  }

  startEdit(item: ItemResponseDTO): void {
    this.editingItemId = item.id;
    this.itemForm.patchValue({
      name: item.name,
      idCategory: item.category.id,
      idUnit: item.unit.id
    });
  }

  deleteItem(id: number): void {
    if (confirm('Are you sure you want to delete this item?')) {
      this.itemService.deleteItem(id).subscribe({
        next: () => {
          this.snackBar.open('Item deleted successfully', 'Close', { duration: 3000 });
          this.loadInitialData();
        },
        error: (error) => {
          this.snackBar.open('Error deleting item', 'Close', { duration: 3000 });
        }
      });
    }
  }

  resetForm(): void {
    this.itemForm.reset();
    this.editingItemId = null;
  }
}
