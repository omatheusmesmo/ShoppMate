import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { AsyncPipe } from '@angular/common';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { combineLatest, finalize, map, Observable, switchMap, tap } from 'rxjs';

import { ListItemService } from '../../../shared/services/list-item.service';
import { ItemService } from '../../../shared/services/item.service';
import { ShoppingListService } from '../../../shared/services/shopping-list.service';
import { ListItemResponseDTO } from '../../../shared/interfaces/list-item.interface';
import { ItemResponseDTO } from '../../../shared/interfaces/item.interface';
import { ShoppingListResponseDTO } from '../../../shared/interfaces/shopping-list.interface';

@Component({
  selector: 'app-list-details',
  standalone: true,
  imports: [
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatTableModule,
    MatInputModule,
    MatFormFieldModule,
    MatSelectModule,
    MatCheckboxModule,
    MatProgressSpinnerModule,
    FormsModule,
    ReactiveFormsModule,
    AsyncPipe
  ],
  templateUrl: './list-details.component.html',
  styleUrls: ['./list-details.component.scss']
})
export class ListDetailsComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private listItemService = inject(ListItemService);
  private shoppingListService = inject(ShoppingListService);
  private itemService = inject(ItemService);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);
  private fb = inject(FormBuilder);

  listId!: number;
  loading = true;
  list$!: Observable<ShoppingListResponseDTO>;
  listItems$!: Observable<ListItemResponseDTO[]>;
  displayedColumns: string[] = ['item', 'quantity', 'status', 'actions'];

  ngOnInit(): void {
    this.listId = +this.route.snapshot.paramMap.get('id')!;
    this.loadData();
  }

  loadData(): void {
    this.loading = true;

    // Get the list details
    this.list$ = this.shoppingListService.getAllShoppingLists().pipe(
      map(lists => lists.find(list => list.idList === this.listId)!),
    );

    // Get list items
    this.listItems$ = this.listItemService.getAllListItemsByListId(this.listId).pipe(
      tap(() => this.loading = false),
      finalize(() => this.loading = false)
    );
  }

  togglePurchased(item: ListItemResponseDTO): void {
    const updatedItem = {
      listId: this.listId,
      itemId: item.item.id,
      quantity: item.quantity,
      purchased: !item.purchased
    };

    this.listItemService.updateListItem(this.listId, item.idListItem, updatedItem)
      .subscribe({
        next: () => this.loadData(),
        error: (error) => {
          console.error('Error updating item status:', error);
          this.snackBar.open('Erro ao atualizar status do item', 'Fechar', {
            duration: 3000
          });
        }
      });
  }

  openAddItemDialog(): void {
    // Implementar posteriormente com um dialog
    console.log('Add item dialog should open here');
  }

  editItem(item: ListItemResponseDTO): void {
    // Implementar posteriormente
    console.log('Edit item:', item);
  }

  removeItem(item: ListItemResponseDTO): void {
    if (confirm(`Tem certeza que deseja remover ${item.item.name} da lista?`)) {
      this.listItemService.deleteListItem(this.listId, item.idListItem)
        .subscribe({
          next: () => {
            this.loadData();
            this.snackBar.open('Item removido com sucesso', 'Fechar', {
              duration: 3000
            });
          },
          error: (error) => {
            console.error('Error removing item:', error);
            this.snackBar.open('Erro ao remover item', 'Fechar', {
              duration: 3000
            });
          }
        });
    }
  }
}
