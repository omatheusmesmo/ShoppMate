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
<<<<<<< HEAD
  public dialogRef = inject(MatDialogRef<ListItemDialogComponent>);
  public data = inject(MAT_DIALOG_DATA) as {
    listItem?: ListItemResponseDTO;
    listId: number;
  };
  private itemService = inject(ItemService);

  readonly items = signal<ItemResponseDTO[]>([]);
  readonly selectedItemId = signal<number | null>(null);
  readonly quantity = signal(1);
=======
  items: ItemResponseDTO[] = [];
  readonly form: FormGroup<{
    itemId: FormControl<number | null>;
    quantity: FormControl<number>;
  }>;

  constructor(
    public dialogRef: MatDialogRef<ListItemDialogComponent>,
    @Inject(MAT_DIALOG_DATA)
    public data: { listItem?: ListItemResponseDTO; listId: number },
    private itemService: ItemService,
    private fb: FormBuilder,
  ) {
    const isEdit = !!data.listItem;
    this.form = this.fb.group({
      itemId: new FormControl<number | null>(
        { value: data.listItem?.item.id ?? null, disabled: isEdit },
        { validators: [Validators.required] },
      ),
      quantity: this.fb.nonNullable.control(data.listItem?.quantity ?? 1, {
        validators: [Validators.required, Validators.min(1)],
      }),
    });
  }
>>>>>>> fe9633e (fix: remove category dialog and ajustmends list-item-dialog refactor)

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
<<<<<<< HEAD
    if (!this.selectedItemId() || !this.quantity() || this.quantity() <= 0) {
=======
    if (this.form.invalid) {
      return;
    }

    const { itemId, quantity } = this.form.getRawValue();
    if (itemId === null) {
>>>>>>> fe9633e (fix: remove category dialog and ajustmends list-item-dialog refactor)
      return;
    }

    this.dialogRef.close({
<<<<<<< HEAD
      itemId: this.selectedItemId(),
      quantity: this.quantity(),
=======
      itemId,
      quantity,
>>>>>>> fe9633e (fix: remove category dialog and ajustmends list-item-dialog refactor)
    });
  }
}
