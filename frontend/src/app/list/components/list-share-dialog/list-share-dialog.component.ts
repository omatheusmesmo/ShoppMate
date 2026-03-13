import {
  ChangeDetectionStrategy,
  Component,
  Inject,
  OnInit,
  inject,
  signal,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import {
  MatDialogRef,
  MAT_DIALOG_DATA,
  MatDialogModule,
} from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ListPermissionService } from '../../../shared/services/list-permission.service';
import { UserService } from '../../../shared/services/user.service';
import {
  ListPermissionSummaryDTO,
  Permission,
} from '../../../shared/interfaces/list-permission.interface';
import { User } from '../../../shared/interfaces/user.interface';
import { ConfirmDialogService } from '../../../shared/services/confirm-dialog.service';
import { FeedbackService } from '../../../shared/services/feedback.service';

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
    MatDividerModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './list-share-dialog.component.html',
  styleUrls: ['./list-share-dialog.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ListShareDialogComponent implements OnInit {
  private fb = inject(FormBuilder);
  private listPermissionService = inject(ListPermissionService);
  private userService = inject(UserService);
  private confirmDialog = inject(ConfirmDialogService);
  private feedback = inject(FeedbackService);

  shareForm: FormGroup;
  readonly permissions = signal<ListPermissionSummaryDTO[]>([]);
  readonly users = signal<User[]>([]);
  displayedColumns: string[] = ['user', 'permission', 'actions'];
  permissionTypes = Object.values(Permission);
  readonly isLoading = signal(false);

  constructor(
    public dialogRef: MatDialogRef<ListShareDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { listId: number; listName: string },
  ) {
    this.shareForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      permission: [Permission.READ, Validators.required],
    });
  }

  ngOnInit(): void {
    this.loadPermissions();
    this.loadUsers();
  }

  loadPermissions(): void {
    this.isLoading.set(true);
    this.listPermissionService
      .getAllListPermissions(this.data.listId)
      .subscribe({
        next: (permissions: ListPermissionSummaryDTO[]) => {
          this.permissions.set(permissions);
          this.isLoading.set(false);
        },
        error: () => {
          this.feedback.error('Erro ao carregar permissoes');
          this.isLoading.set(false);
        },
      });
  }

  loadUsers(): void {
    this.userService.getAllUsers().subscribe({
      next: (users: User[]) => {
        this.users.set(users);
      },
      error: () => {
        this.feedback.error('Erro ao carregar usuarios');
      },
    });
  }

  onShare(): void {
    if (this.shareForm.valid) {
      const email = this.shareForm.value.email;
      const user = this.users().find((u) => u.email === email);

      if (!user || !user.id) {
        this.feedback.error('Usuario nao encontrado com este e-mail');
        return;
      }

      const request = {
        idList: this.data.listId,
        idUser: user.id,
        permission: this.shareForm.value.permission,
      };

      this.listPermissionService.addListPermission(request).subscribe({
        next: () => {
          this.feedback.success('Lista compartilhada com sucesso');
          this.shareForm.reset({ permission: Permission.READ });
          this.loadPermissions();
        },
        error: () => {
          this.feedback.error('Erro ao compartilhar lista');
        },
      });
    }
  }

  removePermission(permissionId: number): void {
    this.confirmDialog
      .open({
        title: 'Remover permissao',
        message: 'Tem certeza que deseja remover esta permissao?',
        confirmText: 'Remover',
      })
      .subscribe((confirmed) => {
        if (!confirmed) return;

        this.listPermissionService
          .deleteListPermission(this.data.listId, permissionId)
          .subscribe({
            next: () => {
              this.feedback.success('Permissao removida com sucesso');
              this.loadPermissions();
            },
            error: () => {
              this.feedback.error('Erro ao remover permissao');
            },
          });
      });
  }

  onClose(): void {
    this.dialogRef.close();
  }
}
