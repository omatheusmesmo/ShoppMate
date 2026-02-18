import { Component, Inject, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatDividerModule } from '@angular/material/divider';
import { ListPermissionService } from '../../../shared/services/list-permission.service';
import { UserService } from '../../../shared/services/user.service';
import { ListPermission, ListPermissionSummaryDTO, Permission } from '../../../shared/interfaces/list-permission.interface';
import { User } from '../../../shared/interfaces/user.interface';

@Component({
  selector: 'app-list-share-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatTableModule,
    MatIconModule,
    MatDividerModule
  ],
  templateUrl: './list-share-dialog.component.html',
  styleUrls: ['./list-share-dialog.component.scss']
})
export class ListShareDialogComponent implements OnInit {
  private fb = inject(FormBuilder);
  private listPermissionService = inject(ListPermissionService);
  private userService = inject(UserService);
  private snackBar = inject(MatSnackBar);

  shareForm: FormGroup;
  permissions: ListPermissionSummaryDTO[] = [];
  users: User[] = [];
  displayedColumns: string[] = ['user', 'permission', 'actions'];
  permissionTypes = Object.values(Permission);
  isLoading = false;

  constructor(
    public dialogRef: MatDialogRef<ListShareDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { listId: number, listName: string }
  ) {
    this.shareForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      permission: [Permission.READ, Validators.required]
    });
  }

  ngOnInit(): void {
    this.loadPermissions();
    this.loadUsers();
  }

  loadPermissions(): void {
    this.isLoading = true;
    this.listPermissionService.getAllListPermissions(this.data.listId).subscribe({
      next: (permissions: ListPermissionSummaryDTO[]) => {
        this.permissions = permissions;
        this.isLoading = false;
      },
      error: () => {
        this.snackBar.open('Erro ao carregar permissões', 'Fechar', { duration: 3000 });
        this.isLoading = false;
      }
    });
  }

  loadUsers(): void {
    this.userService.getAllUsers().subscribe({
      next: (users: User[]) => {
        this.users = users;
      },
      error: () => {
        console.error('Error loading users');
      }
    });
  }

  onShare(): void {
    if (this.shareForm.valid) {
      const email = this.shareForm.value.email;
      const user = this.users.find(u => u.email === email);

      if (!user) {
        this.snackBar.open('Usuário não encontrado com este e-mail', 'Fechar', { duration: 3000 });
        return;
      }

      const request = {
        idList: this.data.listId,
        idUser: user.id!,
        permission: this.shareForm.value.permission
      };

      this.listPermissionService.addListPermission(request).subscribe({
        next: () => {
          this.snackBar.open('Lista compartilhada com sucesso', 'Fechar', { duration: 3000 });
          this.shareForm.reset({ permission: Permission.READ });
          this.loadPermissions();
        },
        error: () => {
          this.snackBar.open('Erro ao compartilhar lista', 'Fechar', { duration: 3000 });
        }
      });
    }
  }

  removePermission(permissionId: number): void {
    if (confirm('Tem certeza que deseja remover esta permissão?')) {
      this.listPermissionService.deleteListPermission(this.data.listId, permissionId).subscribe({
        next: () => {
          this.snackBar.open('Permissão removida', 'Fechar', { duration: 3000 });
          this.loadPermissions();
        },
        error: () => {
          this.snackBar.open('Erro ao remover permissão', 'Fechar', { duration: 3000 });
        }
      });
    }
  }

  onClose(): void {
    this.dialogRef.close();
  }
}
