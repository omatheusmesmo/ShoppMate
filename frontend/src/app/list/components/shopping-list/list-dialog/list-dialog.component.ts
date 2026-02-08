import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { ShoppingListRequestDTO } from '../../../../shared/interfaces/shopping-list.interface';

@Component({
  selector: 'app-list-dialog',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule
  ],
  template: `
    <h2 mat-dialog-title>{{ data.list ? 'Editar' : 'Nova' }} Lista</h2>
    <mat-dialog-content>
      <mat-form-field appearance="outline" class="w-100">
        <mat-label>Nome</mat-label>
        <input matInput [(ngModel)]="list.name" required>
      </mat-form-field>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="onCancel()">Cancelar</button>
      <button mat-raised-button color="primary" (click)="onSave()" [disabled]="!list.name">
        Salvar
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    .w-100 {
      width: 100%;
      margin-bottom: 1rem;
    }
  `]
})
export class ListDialogComponent {
  list: ShoppingListRequestDTO = {
    name: '',
    idUser: 0
  };

  constructor(
    public dialogRef: MatDialogRef<ListDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { list?: ShoppingListRequestDTO }
  ) {
    if (data.list) {
      this.list = { ...data.list };
    }
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  onSave(): void {
    this.dialogRef.close(this.list);
  }
}
