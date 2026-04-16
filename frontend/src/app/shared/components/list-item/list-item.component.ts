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
import { MatCheckboxModule } from '@angular/material/checkbox';
import { ActivatedRoute } from '@angular/router';
import {
  ListItemRequestDTO,
  ListItemResponseDTO,
} from '../../interfaces/list-item.interface';
import { ItemResponseDTO } from '../../interfaces/item.interface';
import { ItemService } from '../../services/item.service';
import { ListItemService } from '../../services/list-item.service';
import { forkJoin } from 'rxjs';
import { ConfirmDialogService } from '../../services/confirm-dialog.service';
import { FeedbackService } from '../../services/feedback.service';

@Component({
  standalone: true,
  selector: 'app-list-item',
  templateUrl: './list-item.component.html',
  styleUrls: ['./list-item.component.scss'],
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
    MatCheckboxModule,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ListItemComponent implements OnInit {
  private listItemService = inject(ListItemService);
  private itemService = inject(ItemService);
  private fb = inject(FormBuilder);
  private route = inject(ActivatedRoute);
  private confirmDialog = inject(ConfirmDialogService);

  public feedback = inject(FeedbackService);

  readonly listItems = signal<ListItemResponseDTO[]>([]);
  readonly availableItems = signal<ItemResponseDTO[]>([]);
  readonly isLoading = signal(false);
  readonly listItemForm: FormGroup = this.fb.group({
    itemId: ['', Validators.required],
    quantity: [1, [Validators.required, Validators.min(1)]],
    unitPrice: [0, [Validators.required, Validators.min(0)]],
    purchased: [false],
  });
  readonly editingListItemId = signal<number | null>(null);
  readonly listId = Number(this.route.snapshot.paramMap.get('listId') ?? 0);

  ngOnInit(): void {
    this.loadInitialData();
  }

  loadInitialData(): void {
    this.isLoading.set(true);
    forkJoin({
      listItems: this.listItemService.getListItemsByListId(this.listId),
      items: this.itemService.getAllItems(),
    }).subscribe({
      next: (data) => {
        this.listItems.set(data.listItems);
        this.availableItems.set(data.items);
        this.isLoading.set(false);
      },
      error: () => {
        this.feedback.error('Error loading data');
        this.isLoading.set(false);
      },
    });
  }

  onSubmit(): void {
    if (this.listItemForm.invalid) return;

    const { itemId } = this.listItemForm.getRawValue();
    if (itemId === null) return;

    const listItemData: ListItemRequestDTO = {
      listId: this.listId,
      itemId: this.listItemForm.value.itemId,
      quantity: this.listItemForm.value.quantity,
      unitPrice: this.listItemForm.value.unitPrice,
    };

    const operation = this.editingListItemId()
      ? this.listItemService.updateListItem(
          this.listId,
          this.editingListItemId()!,
          listItemData,
        )
      : this.listItemService.addListItem(this.listId, listItemData);

    operation.subscribe({
      next: () => {
        this.feedback.success(
          this.editingListItemId()
            ? 'Item updated successfully'
            : 'Item created successfully',
        );
        this.resetForm();
        this.loadInitialData();
      },
      error: () => {
        this.feedback.error('Error saving list item');
      },
    });
  }

  startEdit(listItem: ListItemResponseDTO): void {
    this.editingListItemId.set(listItem.idListItem);
    this.listItemForm.patchValue({
      itemId: listItem.item.id,
      quantity: listItem.quantity,
      unitPrice: listItem.unitPrice,
      purchased: listItem.purchased,
    });
  }

  deleteListItem(id: number): void {
    this.confirmDialog
      .open({
        title: 'Delete Item',
        message: 'Are you sure you want to delete this item from the list?',
        confirmText: 'Delete',
      })
      .subscribe((confirmed) => {
        if (!confirmed) return;

        this.listItemService.deleteListItem(this.listId, id).subscribe({
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

  togglePurchased(listItem: ListItemResponseDTO): void {
    const updatedListItem: ListItemRequestDTO = {
      listId: this.listId,
      itemId: listItem.item.id,
      quantity: listItem.quantity,
      unitPrice: listItem.unitPrice,
    };

    this.listItemService
      .updateListItem(this.listId, listItem.idListItem, updatedListItem)
      .subscribe({
        next: () => {
          this.loadInitialData();
        },
        error: () => {
          this.feedback.error('Error updating item status');
        },
      });
  }

  resetForm(): void {
    this.listItemForm.reset({
      itemId: '',
      quantity: 1,
      unitPrice: 0,
      purchased: false,
    });
    this.editingListItemId.set(null);
  }

  getAvailableItems(): ItemResponseDTO[] {
    if (!this.editingListItemId()) {
      return this.availableItems().filter(
        (item) =>
          !this.listItems().some((listItem) => listItem.item.id === item.id),
      );
    }
    return this.availableItems();
  }
}
