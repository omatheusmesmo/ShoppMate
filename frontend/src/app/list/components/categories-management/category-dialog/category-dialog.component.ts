import {
  ChangeDetectionStrategy,
  Component,
  Inject,
  signal,
} from '@angular/core';
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
import { Category } from '../../../../shared/interfaces/category.interface';

@Component({
  selector: 'app-category-dialog',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
  ],
  templateUrl: './category-dialog.component.html',
  styleUrls: ['./category-dialog.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CategoryDialogComponent {
  readonly categoryName = signal('');

  constructor(
    public dialogRef: MatDialogRef<CategoryDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { category?: Category },
  ) {
    if (data.category) {
      this.categoryName.set(data.category.name);
    }
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  onSave(): void {
    this.dialogRef.close({
      ...this.data.category,
      name: this.categoryName(),
    } satisfies Category);
  }
}
