import {
  Component,
  OnInit,
  inject,
  signal,
  ChangeDetectionStrategy,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormBuilder,
  ReactiveFormsModule,
  Validators,
  AbstractControl,
  ValidationErrors,
  ValidatorFn,
} from '@angular/forms';
import {
  MatDialogModule,
  MatDialogRef,
  MAT_DIALOG_DATA,
} from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { Unit } from '../../../../shared/interfaces/unit.interface';
import { UnitService } from '../../../../shared/services/unit.service';
import { MatSnackBar } from '@angular/material/snack-bar';

export function duplicateNameValidator(
  existingNames: string[],
  originalName?: string,
): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    if (!control.value) return null;
    const name = control.value.trim().toLowerCase();
    if (originalName && name === originalName.trim().toLowerCase()) return null;
    return existingNames.some(
      (existingName) => existingName.trim().toLowerCase() === name,
    )
      ? { duplicateName: true }
      : null;
  };
}

@Component({
  selector: 'app-unit-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
  ],
  templateUrl: './unit-dialog.component.html',
  styleUrls: ['./unit-dialog.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UnitDialogComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly unitService = inject(UnitService);
  private readonly snackBar = inject(MatSnackBar);
  readonly dialogRef = inject(MatDialogRef<UnitDialogComponent>);
  readonly data = inject(MAT_DIALOG_DATA) as { unit?: Unit };

  readonly unitForm = this.fb.group({
    name: ['', [Validators.required]],
    symbol: ['', [Validators.required]],
  });

  readonly existingUnits = signal<Unit[]>([]);

  ngOnInit(): void {
    if (this.data.unit) {
      this.unitForm.patchValue(this.data.unit);
    }
    this.loadUnits();
  }

  loadUnits(): void {
    this.unitService.getAllUnits().subscribe({
      next: (units) => {
        this.existingUnits.set(units);
        this.updateNameValidator();
      },
      error: () => {
        this.snackBar.open(
          'Erro ao carregar unidades para validação',
          'Fechar',
          {
            duration: 3000,
          },
        );
      },
    });
  }

  updateNameValidator(): void {
    const names = this.existingUnits().map((u) => u.name);
    const originalName = this.data.unit ? this.data.unit.name : undefined;

    this.unitForm
      .get('name')
      ?.addValidators(duplicateNameValidator(names, originalName));
    this.unitForm.get('name')?.updateValueAndValidity();
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  onSave(): void {
    if (this.unitForm.valid) {
      this.dialogRef.close({
        ...this.data.unit,
        ...this.unitForm.value,
      });
    }
  }
}
