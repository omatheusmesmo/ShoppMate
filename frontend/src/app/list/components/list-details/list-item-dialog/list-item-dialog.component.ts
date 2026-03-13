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
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import {
  MatDialogModule,
  MatDialogRef,
  MAT_DIALOG_DATA,
} from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { ItemResponseDTO } from '../../../../shared/interfaces/item.interface';
import { ListItemResponseDTO } from '../../../../shared/interfaces/list-item.interface';
import { ItemService } from '../../../../shared/services/item.service';

@Component({
  selector: 'app-list-item-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule,
  ],
  templateUrl: './list-item-dialog.component.html',
  styleUrls: ['./list-item-dialog.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ListItemDialogComponent implements OnInit {
  private dialogRef = inject(MatDialogRef<ListItemDialogComponent>);
  private itemService = inject(ItemService);
  private fb = inject(FormBuilder);

  readonly data = inject(MAT_DIALOG_DATA) as {
    listItem?: ListItemResponseDTO;
    listId: number;
  };

  readonly items = signal<ItemResponseDTO[]>([]);

  readonly form: FormGroup<{
    itemId: FormControl<number | null>;
    quantity: FormControl<number>;
  }> = this.fb.group({
    itemId: new FormControl<number | null>(
      {
        value: this.data.listItem?.item.id ?? null,
        disabled: !!this.data.listItem,
      },
      { validators: [Validators.required] },
    ),
    quantity: this.fb.nonNullable.control(this.data.listItem?.quantity ?? 1, {
      validators: [Validators.required, Validators.min(1)],
    }),
  });

  ngOnInit(): void {
    this.loadItems();
  }

  loadItems(): void {
    this.itemService.getAllItems().subscribe((items) => {
      this.items.set(items);
    });
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  onSave(): void {
    if (this.form.invalid) {
      return;
    }

    const { itemId, quantity } = this.form.getRawValue();
    if (itemId === null) {
      return;
    }

    this.dialogRef.close({
      itemId,
      quantity,
    });
  }
}
