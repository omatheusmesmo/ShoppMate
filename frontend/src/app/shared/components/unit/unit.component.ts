import {
  ChangeDetectionStrategy,
  Component,
  OnInit,
  signal,
  inject,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormBuilder,
  FormGroup,
  Validators,
  ReactiveFormsModule,
} from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Unit } from '../../interfaces/unit.interface';
import { UnitService } from '../../services/unit.service';
import { ConfirmDialogService } from '../../services/confirm-dialog.service';
import { FeedbackService } from '../../services/feedback.service';

@Component({
  standalone: true,
  selector: 'app-unit',
  templateUrl: './unit.component.html',
  styleUrls: ['./unit.component.scss'],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    MatProgressSpinnerModule,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UnitComponent implements OnInit {
  readonly units = signal<Unit[]>([]);
  readonly isLoading = signal(false);
  unitForm: FormGroup;
  readonly editingUnitId = signal<number | null>(null);

  private confirmDialog = inject(ConfirmDialogService);
  private feedback = inject(FeedbackService);

  constructor(
    private unitService: UnitService,
    private fb: FormBuilder,
  ) {
    this.unitForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2)]],
      symbol: ['', [Validators.required]],
    });
  }

  ngOnInit(): void {
    this.loadUnits();
  }

  loadUnits(): void {
    this.isLoading.set(true);
    this.unitService.getAllUnits().subscribe({
      next: (units: Unit[]) => {
        this.units.set(units);
        this.isLoading.set(false);
      },
      error: () => {
        this.feedback.error('Erro ao carregar unidades');
        this.isLoading.set(false);
      },
    });
  }

  onSubmit(): void {
    if (this.unitForm.invalid) return;

    const unitData: Unit = {
      name: this.unitForm.value.name,
      symbol: this.unitForm.value.symbol,
    };

    if (this.editingUnitId() !== null) {
      unitData.id = this.editingUnitId()!;
    }

    const operation =
      this.editingUnitId() !== null
        ? this.unitService.updateUnit(unitData)
        : this.unitService.addUnit(unitData);

    operation.subscribe({
      next: () => {
        this.feedback.success(
          this.editingUnitId() !== null
            ? 'Unidade atualizada com sucesso'
            : 'Unidade criada com sucesso',
        );
        this.resetForm();
        this.loadUnits();
      },
      error: () => {
        this.feedback.error('Erro ao salvar unidade');
      },
    });
  }

  startEdit(unit: Unit): void {
    this.editingUnitId.set(unit.id ?? null);
    this.unitForm.patchValue({
      name: unit.name,
      symbol: unit.symbol,
    });
  }

  deleteUnit(id: number): void {
    this.confirmDialog
      .open({
        title: 'Excluir unidade',
        message: 'Tem certeza que deseja excluir esta unidade?',
        confirmText: 'Excluir',
      })
      .subscribe((confirmed) => {
        if (!confirmed) return;

        this.unitService.deleteUnit(id).subscribe({
          next: () => {
            this.feedback.success('Unidade excluída com sucesso');
            this.loadUnits();
          },
          error: () => {
            this.feedback.error('Erro ao excluir unidade');
          },
        });
      });
  }

  resetForm(): void {
    this.unitForm.reset();
    this.editingUnitId.set(null);
  }
}
