import {
  ChangeDetectionStrategy,
  Component,
  Inject,
  OnInit,
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
import { MatSelectModule } from '@angular/material/select';
import { ItemResponseDTO } from '../../../../shared/interfaces/item.interface';
import { ListItemResponseDTO } from '../../../../shared/interfaces/list-item.interface';
import { ItemService } from '../../../../shared/services/item.service';

@Component({
  selector: 'app-list-item-dialog',
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
  templateUrl: './list-item-dialog.component.html',
  styleUrls: ['./list-item-dialog.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ListItemDialogComponent implements OnInit {
  items: ItemResponseDTO[] = [];
  selectedItemId: number | null = null;
  quantity = 1;

  constructor(
    public dialogRef: MatDialogRef<ListItemDialogComponent>,
    @Inject(MAT_DIALOG_DATA)
    public data: { listItem?: ListItemResponseDTO; listId: number },
    private itemService: ItemService,
  ) {
    if (data.listItem) {
      this.selectedItemId = data.listItem.item.id;
      this.quantity = data.listItem.quantity;
    }
  }

  ngOnInit(): void {
    this.loadItems();
  }

  loadItems(): void {
    this.itemService.getAllItems().subscribe((items) => {
      this.items = items;
    });
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  onSave(): void {
    this.dialogRef.close({
      itemId: this.selectedItemId,
      quantity: this.quantity,
    });
  }
}
