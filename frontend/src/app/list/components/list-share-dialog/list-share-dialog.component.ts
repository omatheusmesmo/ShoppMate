import {
  ChangeDetectionStrategy,
  Component,
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
  public readonly dialogRef = inject(MatDialogRef<ListShareDialogComponent>);
  public readonly data = inject(MAT_DIALOG_DATA) as {
    listId: number;
    listName: string;
  };

  shareForm: FormGroup = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    permission: [Permission.READ, Validators.required],
  });
  readonly permissions = signal<ListPermissionSummaryDTO[]>([]);
  readonly users = signal<User[]>([]);
  displayedColumns: string[] = ['user', 'permission', 'actions'];
  permissionTypes = Object.values(Permission);
  readonly isLoading = signal(false);

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
          this.feedback.error('Error loading permissions');
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
        this.feedback.error('Error loading users');
      },
    });
  }

  onShare(): void {
    if (this.shareForm.valid) {
      const { email, permission } = this.shareForm.getRawValue();
      const user = this.users().find((u) => u.email === email);

      if (!user || !user.id) {
        this.feedback.error('User not found with this email');
        return;
      }

      const request = {
        idList: this.data.listId,
        idUser: user.id,
        permission,
      };

      this.listPermissionService.addListPermission(request).subscribe({
        next: () => {
          this.feedback.success('List shared successfully');
          this.shareForm.reset({ permission: Permission.READ });
          this.loadPermissions();
        },
        error: () => {
          this.feedback.error('Error sharing list');
        },
      });
    }
  }

  removePermission(permissionId: number): void {
    this.confirmDialog
      .open({
        title: 'Remove Permission',
        message: 'Are you sure you want to remove this permission?',
        confirmText: 'Remove',
      })
      .subscribe((confirmed) => {
        if (!confirmed) return;

        this.listPermissionService
          .deleteListPermission(this.data.listId, permissionId)
          .subscribe({
            next: () => {
              this.feedback.success('Permission removed successfully');
              this.loadPermissions();
            },
            error: () => {
              this.feedback.error('Error removing permission');
            },
          });
      });
  }

  onClose(): void {
    this.dialogRef.close();
  }
}
