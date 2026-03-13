import {
  ChangeDetectionStrategy,
  Component,
  inject,
  signal,
} from '@angular/core';
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
import { MatDialogActions } from '@angular/material/dialog';
import { MatDialogContent } from '@angular/material/dialog';
import { FeedbackService } from '../../../shared/services/feedback.service';

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
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ShoppingListDialogComponent {
  private dialogRef = inject(MatDialogRef<ShoppingListDialogComponent>);
  private fb = inject(FormBuilder);
  private shoppingListService = inject(ShoppingListService);
  private feedback = inject(FeedbackService);
  private authService = inject(AuthService);
  private data = inject(MAT_DIALOG_DATA) as {
    list?: ShoppingListResponseDTO;
    isEdit: boolean;
  };

  listForm: FormGroup;
  readonly isEdit = signal(false);

  constructor() {
    this.isEdit.set(this.data.isEdit);
    this.listForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(3)]],
    });

    if (this.isEdit() && this.data.list) {
      this.listForm.patchValue({
        name: this.data.list.listName,
      });
    }
  }

  onSubmit(): void {
    if (this.listForm.valid) {
      const userId = this.authService.getCurrentUserId();

      if (!userId) {
        this.feedback.error(
          'Usuario nao identificado. Por favor, faca login novamente.',
        );
        return;
      }

      const listData: ShoppingListRequestDTO = {
        name: this.listForm.value.name,
        idUser: userId,
      };

      if (this.isEdit() && this.data.list) {
        this.shoppingListService
          .updateShoppingList(this.data.list.idList, listData)
          .subscribe({
            next: () => {
              this.feedback.success('Lista atualizada com sucesso');
              this.dialogRef.close(true);
            },
            error: () => {
              this.feedback.error('Erro ao atualizar lista');
            },
          });
      } else {
        this.shoppingListService.createShoppingList(listData).subscribe({
          next: () => {
            this.feedback.success('Lista criada com sucesso');
            this.dialogRef.close(true);
          },
          error: () => {
            this.feedback.error('Erro ao criar lista');
          },
        });
      }
    }
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}
