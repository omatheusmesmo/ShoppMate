import {
  Component,
  OnInit,
  inject,
  ChangeDetectionStrategy,
  signal,
} from '@angular/core';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog } from '@angular/material/dialog';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatCardModule } from '@angular/material/card';

import { UnitService } from '../../../shared/services/unit.service';
import { Unit } from '../../../shared/interfaces/unit.interface';
import { finalize, catchError, of, tap } from 'rxjs';
import { UnitDialogComponent } from './unit-dialog/unit-dialog.component';
import { ConfirmDialogService } from '../../../shared/services/confirm-dialog.service';
import { FeedbackService } from '../../../shared/services/feedback.service';

@Component({
  selector: 'app-units-management',
  standalone: true,
  imports: [
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatCardModule,
  ],
  templateUrl: './units-management.component.html',
  styleUrls: ['./units-management.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UnitsManagementComponent implements OnInit {
  private unitService = inject(UnitService);
  private dialog = inject(MatDialog);
  private confirmDialog = inject(ConfirmDialogService);
  private feedback = inject(FeedbackService);

  readonly loading = signal(true);
  readonly error = signal(false);
  readonly units = signal<Unit[]>([]);
  displayedColumns: string[] = ['name', 'symbol', 'actions'];

  ngOnInit(): void {
    this.loadUnits();
  }

  loadUnits(): void {
    this.loading.set(true);
    this.error.set(false);

    this.unitService
      .getAllUnits()
      .pipe(
        tap((response) => {
          this.units.set(response || []);
        }),
        catchError(() => {
          this.error.set(true);
          this.units.set([]);
          this.feedback.error('Error loading units. Please try again later.');
          return of([]);
        }),
        finalize(() => {
          this.loading.set(false);
        }),
      )
      .subscribe();
  }

  editUnit(unit: Unit): void {
    const dialogRef = this.dialog.open(UnitDialogComponent, {
      width: '400px',
      data: { unit },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.unitService.updateUnit(result).subscribe({
          next: () => {
            this.loadUnits();
            this.feedback.success('Unit updated successfully');
          },
          error: () => {
            this.feedback.error('Error updating unit');
          },
        });
      }
    });
  }

  deleteUnit(unit: Unit): void {
    this.confirmDialog
      .open({
        title: 'Delete Unit',
        message: `Are you sure you want to delete the unit "${unit.name}"?`,
        confirmText: 'Delete',
      })
      .subscribe((confirmed) => {
        if (!confirmed) return;

        this.unitService.deleteUnit(unit.id!).subscribe({
          next: () => {
            this.loadUnits();
            this.feedback.success('Unit deleted successfully');
          },
          error: () => {
            this.feedback.error('Error deleting unit');
          },
        });
      });
  }

  openNewUnitDialog(): void {
    const dialogRef = this.dialog.open(UnitDialogComponent, {
      width: '400px',
      data: { unit: null },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.unitService.addUnit(result).subscribe({
          next: () => {
            this.loadUnits();
            this.feedback.success('Unit added successfully');
          },
          error: () => {
            this.feedback.error('Error adding unit');
          },
        });
      }
    });
  }
}
