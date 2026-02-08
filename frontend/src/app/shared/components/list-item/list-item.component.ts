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
import { MatCheckboxModule } from '@angular/material/checkbox';
import { ActivatedRoute } from '@angular/router';
import { ListItemRequestDTO, ListItemResponseDTO } from '../../interfaces/list-item.interface';
import { ItemResponseDTO } from '../../interfaces/item.interface';
import { ItemService } from '../../services/item.service';
import { ListItemService } from '../../services/list-item.service';
import { forkJoin } from 'rxjs';

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
    MatCheckboxModule
  ]
})
export class ListItemComponent implements OnInit {
  listItems: ListItemResponseDTO[] = [];
  availableItems: ItemResponseDTO[] = [];
  isLoading = false;
  listItemForm: FormGroup;
  editingListItemId: number | null = null;
  listId: number;

  constructor(
    private listItemService: ListItemService,
    private itemService: ItemService,
    private fb: FormBuilder,
    private snackBar: MatSnackBar,
    private route: ActivatedRoute
  ) {
    this.listItemForm = this.fb.group({
      itemId: ['', Validators.required],
      quantity: [1, [Validators.required, Validators.min(1)]],
      purchased: [false]
    });

    this.listId = Number(this.route.snapshot.paramMap.get('listId'));
  }

  ngOnInit(): void {
    this.loadInitialData();
  }

  loadInitialData(): void {
    this.isLoading = true;
    forkJoin({
      listItems: this.listItemService.getAllListItems(this.listId),
      items: this.itemService.getAllItems()
    }).subscribe({
      next: (data) => {
        this.listItems = data.listItems;
        this.availableItems = data.items;
        this.isLoading = false;
      },
      error: (error) => {
        this.snackBar.open('Error loading data', 'Close', { duration: 3000 });
        this.isLoading = false;
      }
    });
  }

  onSubmit(): void {
    if (this.listItemForm.invalid) return;

    const listItemData: ListItemRequestDTO = {
      listId: this.listId,
      itemId: this.listItemForm.value.itemId,
      quantity: this.listItemForm.value.quantity
    };

    const operation = this.editingListItemId
      ? this.listItemService.updateListItem(this.listId, this.editingListItemId, listItemData)
      : this.listItemService.addListItem(listItemData);

    operation.subscribe({
      next: () => {
        this.snackBar.open(
          this.editingListItemId ? 'Item updated successfully' : 'Item added successfully',
          'Close',
          { duration: 3000 }
        );
        this.resetForm();
        this.loadInitialData();
      },
      error: (error) => {
        this.snackBar.open('Error saving list item', 'Close', { duration: 3000 });
      }
    });
  }

  startEdit(listItem: ListItemResponseDTO): void {
    this.editingListItemId = listItem.idListItem;
    this.listItemForm.patchValue({
      itemId: listItem.item.id,
      quantity: listItem.quantity,
      purchased: listItem.purchased
    });
  }

  deleteListItem(id: number): void {
    if (confirm('Are you sure you want to remove this item from the list?')) {
      this.listItemService.deleteListItem(this.listId, id).subscribe({
        next: () => {
          this.snackBar.open('Item removed successfully', 'Close', { duration: 3000 });
          this.loadInitialData();
        },
        error: (error) => {
          this.snackBar.open('Error removing item', 'Close', { duration: 3000 });
        }
      });
    }
  }

  togglePurchased(listItem: ListItemResponseDTO): void {
    const updatedListItem: ListItemRequestDTO = {
      listId: this.listId,
      itemId: listItem.item.id,
      quantity: listItem.quantity
    };

    this.listItemService.updateListItem(this.listId, listItem.idListItem, updatedListItem)
      .subscribe({
        next: () => {
          this.loadInitialData();
        },
        error: (error) => {
          this.snackBar.open('Error updating item status', 'Close', { duration: 3000 });
        }
      });
  }

  resetForm(): void {
    this.listItemForm.reset({
      quantity: 1,
      purchased: false
    });
    this.editingListItemId = null;
  }

  getAvailableItems(): ItemResponseDTO[] {
    if (!this.editingListItemId) {
      return this.availableItems.filter(item =>
        !this.listItems.some(listItem => listItem.item.id === item.id)
      );
    }
    return this.availableItems;
  }
}
