import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ShoppingListService } from '../../../shared/services/shopping-list.service';
import { ShoppingListResponseDTO } from '../../../shared/interfaces/shopping-list.interface';
import { ShoppingListDialogComponent } from '../shopping-list-dialog/shopping-list-dialog.component';

@Component({
  selector: 'app-shopping-list',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './shopping-list.component.html',
  styleUrls: ['./shopping-list.component.scss']
})
export class ShoppingListComponent implements OnInit {
  shoppingLists: ShoppingListResponseDTO[] = [];
  isLoading = false;

  constructor(
    private shoppingListService: ShoppingListService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadLists();
  }

  loadLists(): void {
    this.isLoading = true;
    this.shoppingListService.getAllShoppingLists().subscribe({
      next: (lists) => {
        this.shoppingLists = lists;
        this.isLoading = false;
      },
      error: (error) => {
        this.snackBar.open('Erro ao carregar listas', 'Fechar', { duration: 3000 });
        this.isLoading = false;
      }
    });
  }

  openEditDialog(list: ShoppingListResponseDTO): void {
    const dialogRef = this.dialog.open(ShoppingListDialogComponent, {
      width: '400px',
      data: { list, isEdit: true }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadLists();
      }
    });
  }

  openNewListDialog(): void {
    const dialogRef = this.dialog.open(ShoppingListDialogComponent, {
      width: '400px',
      data: { isEdit: false }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadLists();
      }
    });
  }

  deleteList(id: number): void {
    if (confirm('Tem certeza que deseja excluir esta lista?')) {
      this.shoppingListService.deleteShoppingList(id).subscribe({
        next: () => {
          this.snackBar.open('Lista excluída com sucesso', 'Fechar', { duration: 3000 });
          this.loadLists();
        },
        error: (error) => {
          this.snackBar.open('Erro ao excluir lista', 'Fechar', { duration: 3000 });
        }
      });
    }
  }
}
