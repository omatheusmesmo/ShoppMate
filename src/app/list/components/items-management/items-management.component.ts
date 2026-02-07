import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
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
import { MatSnackBar } from '@angular/material/snack-bar';
import { AsyncPipe } from '@angular/common';
import { finalize, Observable } from 'rxjs';
import { ItemDialogComponent } from './item-dialog/item-dialog.component';

import { ItemService } from '../../../shared/services/item.service';
import { CategoryService } from '../../../shared/services/category.service';
import { UnitService } from '../../../shared/services/unit.service';
import { ItemResponseDTO } from '../../../shared/interfaces/item.interface';
import { Category } from '../../../shared/interfaces/category.interface';
import { Unit } from '../../../shared/interfaces/unit.interface';

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
    AsyncPipe
  ],
  templateUrl: './items-management.component.html',
  styleUrls: ['./items-management.component.scss']
})
export class ItemsManagementComponent implements OnInit {
  private itemService = inject(ItemService);
  private categoryService = inject(CategoryService);
  private unitService = inject(UnitService);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);
  private fb = inject(FormBuilder);

  loading = true;
  items$!: Observable<ItemResponseDTO[]>;
  categories: Category[] = [];
  units: Unit[] = [];
  displayedColumns: string[] = ['name', 'category', 'unit', 'actions'];

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.loading = true;
    this.items$ = this.itemService.getAllItems().pipe(
      finalize(() => this.loading = false)
    );

    this.categoryService.getAllCategories().subscribe(categories => {
      this.categories = categories;
    });

    this.unitService.getAllUnits().subscribe(units => {
      this.units = units;
    });
  }

  openAddItemDialog(): void {
    // Implementar posteriormente com um dialog
    console.log('Add item dialog should open here');
  }

  editItem(item: ItemResponseDTO): void {
    const dialogRef = this.dialog.open(ItemDialogComponent, {
      width: '400px',
      data: { 
        item: {
          name: item.name,
          idCategory: item.category.id,
          idUnit: item.unit.id
        }
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.itemService.updateItem(item.id, result).subscribe({
          next: () => {
            this.loadData();
            this.snackBar.open('Item atualizado com sucesso', 'Fechar', { duration: 3000 });
          },
          error: () => {
            this.snackBar.open('Erro ao atualizar item', 'Fechar', { duration: 3000 });
          }
        });
      }
    });
  }

  deleteItem(item: ItemResponseDTO): void {
    if (confirm(`Tem certeza que deseja excluir o item "${item.name}"?`)) {
      this.itemService.deleteItem(item.id).subscribe({
        next: () => {
          this.loadData();
          this.snackBar.open('Item excluído com sucesso', 'Fechar', {
            duration: 3000
          });
        },
        error: (error) => {
          console.error('Error deleting item:', error);
          this.snackBar.open('Erro ao excluir item', 'Fechar', {
            duration: 3000
          });
        }
      });
    }
  }
}
