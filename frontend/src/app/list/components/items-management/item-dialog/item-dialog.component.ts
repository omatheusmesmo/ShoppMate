import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  MatDialogModule,
  MatDialogRef,
  MAT_DIALOG_DATA,
} from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { ItemRequestDTO } from '../../../../shared/interfaces/item.interface';
import { CategoryService } from '../../../../shared/services/category.service';
import { UnitService } from '../../../../shared/services/unit.service';
import { Category } from '../../../../shared/interfaces/category.interface';
import { Unit } from '../../../../shared/interfaces/unit.interface';

@Component({
  selector: 'app-item-dialog',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule,
  ],
  template: `
    <h2 mat-dialog-title>{{ data.item ? 'Editar' : 'Novo' }} Item</h2>
    <mat-dialog-content>
      <mat-form-field appearance="outline" class="w-100">
        <mat-label>Nome</mat-label>
        <input matInput [(ngModel)]="item.name" required />
      </mat-form-field>
      <mat-form-field appearance="outline" class="w-100">
        <mat-label>Categoria</mat-label>
        <mat-select [(ngModel)]="item.idCategory" required>
          <mat-option *ngFor="let category of categories" [value]="category.id">
            {{ category.name }}
          </mat-option>
        </mat-select>
      </mat-form-field>
      <mat-form-field appearance="outline" class="w-100">
        <mat-label>Unidade</mat-label>
        <mat-select [(ngModel)]="item.idUnit" required>
          <mat-option *ngFor="let unit of units" [value]="unit.id">
            {{ unit.name }} ({{ unit.symbol }})
          </mat-option>
        </mat-select>
      </mat-form-field>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="onCancel()">Cancelar</button>
      <button
        mat-raised-button
        color="primary"
        (click)="onSave()"
        [disabled]="!item.name || !item.idCategory || !item.idUnit"
      >
        Salvar
      </button>
    </mat-dialog-actions>
  `,
  styles: [
    `
      .w-100 {
        width: 100%;
        margin-bottom: 1rem;
      }
    `,
  ],
})
export class ItemDialogComponent implements OnInit {
  item: ItemRequestDTO = {
    name: '',
    idCategory: 0,
    idUnit: 0,
  };
  categories: Category[] = [];
  units: Unit[] = [];

  constructor(
    public dialogRef: MatDialogRef<ItemDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { item?: ItemRequestDTO },
    private categoryService: CategoryService,
    private unitService: UnitService,
  ) {
    if (data.item) {
      this.item = { ...data.item };
    }
  }

  ngOnInit(): void {
    this.loadCategories();
    this.loadUnits();
  }

  loadCategories(): void {
    this.categoryService.getAllCategories().subscribe((categories) => {
      this.categories = categories;
    });
  }

  loadUnits(): void {
    this.unitService.getAllUnits().subscribe((units) => {
      this.units = units;
    });
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  onSave(): void {
    this.dialogRef.close(this.item);
  }
}
