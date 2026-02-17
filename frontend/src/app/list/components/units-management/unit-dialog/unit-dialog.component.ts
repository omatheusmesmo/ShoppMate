import {
  ChangeDetectionStrategy,
  Component,
  Inject,
  OnInit,
  inject,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormBuilder,
  FormGroup,
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
  template: `
    <h2 mat-dialog-title>{{ data.unit ? 'Editar' : 'Nova' }} Unidade</h2>
    <mat-dialog-content>
      <form [formGroup]="unitForm">
        <mat-form-field appearance="outline" class="w-100">
          <mat-label>Nome</mat-label>
          <input
            matInput
            formControlName="name"
            placeholder="Ex: Quilograma"
            required
          />
          <mat-error *ngIf="unitForm.get('name')?.hasError('required')">
            Nome é obrigatório
          </mat-error>
          <mat-error *ngIf="unitForm.get('name')?.hasError('duplicateName')">
            Já existe uma unidade com este nome
          </mat-error>
        </mat-form-field>
        <mat-form-field appearance="outline" class="w-100">
          <mat-label>Símbolo</mat-label>
          <input
            matInput
            formControlName="symbol"
            placeholder="Ex: kg"
            required
          />
          <mat-error *ngIf="unitForm.get('symbol')?.hasError('required')">
            Símbolo é obrigatório
          </mat-error>
        </mat-form-field>
      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="onCancel()">Cancelar</button>
      <button
        mat-raised-button
        color="primary"
        (click)="onSave()"
        [disabled]="unitForm.invalid"
      >
        Salvar
      </button>
    </mat-dialog-actions>
  `,
  styles: [
    `
      .w-100 {
        width: 100%;
        margin-bottom: 1rem;
      }
    `,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UnitDialogComponent implements OnInit {
  private fb = inject(FormBuilder);
  private unitService = inject(UnitService);
  private snackBar = inject(MatSnackBar);

  unitForm: FormGroup;
  existingUnits: Unit[] = [];

  constructor(
    public dialogRef: MatDialogRef<UnitDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { unit?: Unit },
  ) {
    this.unitForm = this.fb.group({
      name: ['', [Validators.required]],
      symbol: ['', [Validators.required]],
    });

    if (data.unit) {
      this.unitForm.patchValue(data.unit);
    }
  }

  ngOnInit(): void {
    this.loadUnits();
  }

  loadUnits(): void {
    this.unitService.getAllUnits().subscribe({
      next: (units) => {
        this.existingUnits = units;
        this.updateNameValidator();
      },
      error: () => {
        this.snackBar.open('Erro ao carregar unidades para validação', 'Fechar', {
          duration: 3000,
        });
      },
    });
  }

  updateNameValidator(): void {
    const names = this.existingUnits.map((u) => u.name);
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
      this.dialogRef.close(this.unitForm.value);
    }
  }
}
