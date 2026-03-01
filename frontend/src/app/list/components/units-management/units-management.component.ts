import {
  Component,
  OnInit,
  inject,
  ChangeDetectorRef,
  ChangeDetectionStrategy,
} from '@angular/core';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatCardModule } from '@angular/material/card';
import { AsyncPipe } from '@angular/common';

import { UnitService } from '../../../shared/services/unit.service';
import { Unit } from '../../../shared/interfaces/unit.interface';
import { finalize, catchError, of, tap, BehaviorSubject } from 'rxjs';
import { UnitDialogComponent } from './unit-dialog/unit-dialog.component';

@Component({
  selector: 'app-units-management',
  standalone: true,
  imports: [
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatCardModule,
    AsyncPipe,
  ],
  templateUrl: './units-management.component.html',
  styleUrls: ['./units-management.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UnitsManagementComponent implements OnInit {
  private unitService = inject(UnitService);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);
  private cdr = inject(ChangeDetectorRef);

  loading = true;
  error = false;
  private unitsSubject = new BehaviorSubject<Unit[]>([]);
  units$ = this.unitsSubject.asObservable();
  displayedColumns: string[] = ['name', 'symbol', 'actions'];

  ngOnInit(): void {
    this.loadUnits();
  }

  loadUnits(): void {
    this.loading = true;
    this.error = false;

    this.unitService
      .getAllUnits()
      .pipe(
        tap((response) => {
          this.unitsSubject.next(response || []);
        }),
        catchError(() => {
          this.error = true;
          this.unitsSubject.next([]);
          this.snackBar.open(
            'Erro ao carregar unidades. Tente novamente mais tarde.',
            'Fechar',
            {
              duration: 5000,
            },
          );
          return of([]);
        }),
        finalize(() => {
          this.loading = false;
          this.cdr.detectChanges();
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
            this.snackBar.open('Unidade atualizada com sucesso', 'Fechar', {
              duration: 3000,
            });
          },
          error: () => {
            this.snackBar.open('Erro ao atualizar unidade', 'Fechar', {
              duration: 3000,
            });
          },
        });
      }
    });
  }

  deleteUnit(unit: Unit): void {
    if (confirm(`Tem certeza que deseja excluir a unidade "${unit.name}"?`)) {
      this.unitService.deleteUnit(unit.id!).subscribe({
        next: () => {
          this.loadUnits();
          this.snackBar.open('Unidade excluída com sucesso', 'Fechar', {
            duration: 3000,
          });
        },
        error: () => {
          this.snackBar.open('Erro ao excluir unidade', 'Fechar', {
            duration: 3000,
          });
        },
      });
    }
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
            this.snackBar.open('Unidade adicionada com sucesso', 'Fechar', {
              duration: 3000,
            });
          },
          error: () => {
            this.snackBar.open('Erro ao adicionar unidade', 'Fechar', {
              duration: 3000,
            });
          },
        });
      }
    });
  }
}
