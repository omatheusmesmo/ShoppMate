import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { ShoppingListService } from '../../../shared/services/shopping-list.service';
import {
  ShoppingListResponseDTO,
  ShoppingListRequestDTO,
} from '../../../shared/interfaces/shopping-list.interface';
import { AuthService } from '../../../shared/services/auth.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatDialogActions } from '@angular/material/dialog';
import { MatDialogContent } from '@angular/material/dialog';

@Component({
  selector: 'app-shopping-list-dialog',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    ReactiveFormsModule,
    MatDialogActions,
    MatDialogContent,
  ],
  templateUrl: './shopping-list-dialog.component.html',
  styleUrls: ['./shopping-list-dialog.component.scss'],
})
export class ShoppingListDialogComponent {
  private dialogRef = inject(MatDialogRef<ShoppingListDialogComponent>);
  private fb = inject(FormBuilder);
  private shoppingListService = inject(ShoppingListService);
  private snackBar = inject(MatSnackBar);
  private authService = inject(AuthService);
  private data = inject(MAT_DIALOG_DATA) as {
    list?: ShoppingListResponseDTO;
    isEdit: boolean;
  };

  listForm: FormGroup;
  isEdit: boolean;

  constructor() {
    this.isEdit = this.data.isEdit;
    this.listForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(3)]],
    });

    if (this.isEdit && this.data.list) {
      this.listForm.patchValue({
        name: this.data.list.listName,
      });
    }
  }

  onSubmit(): void {
    if (this.listForm.valid) {
      const listData: ShoppingListRequestDTO = {
        name: this.listForm.value.name,
        idUser: this.authService.getCurrentUserId(), // Get from auth service
      };

      if (this.isEdit && this.data.list) {
        this.shoppingListService
          .updateShoppingList(this.data.list.idList, listData)
          .subscribe({
            next: () => {
              this.snackBar.open('Lista atualizada com sucesso', 'Fechar', {
                duration: 3000,
              });
              this.dialogRef.close(true);
            },
            error: () => {
              this.snackBar.open('Erro ao atualizar lista', 'Fechar', {
                duration: 3000,
              });
            },
          });
      } else {
        this.shoppingListService.createShoppingList(listData).subscribe({
          next: () => {
            this.snackBar.open('Lista criada com sucesso', 'Fechar', {
              duration: 3000,
            });
            this.dialogRef.close(true);
          },
          error: () => {
            this.snackBar.open('Erro ao criar lista', 'Fechar', {
              duration: 3000,
            });
          },
        });
      }
    }
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}
