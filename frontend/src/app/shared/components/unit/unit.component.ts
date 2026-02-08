import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Unit } from '../../interfaces/unit.interface';
import { UnitService } from '../../services/unit.service';

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
    MatProgressSpinnerModule
  ]
})
export class UnitComponent implements OnInit {
  units: Unit[] = [];
  isLoading = false;
  unitForm: FormGroup;
  editingUnitId: number | null = null;

  constructor(
    private unitService: UnitService,
    private fb: FormBuilder,
    private snackBar: MatSnackBar
  ) {
    this.unitForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2)]],
      symbol: ['', [Validators.required]]
    });
  }

  ngOnInit(): void {
    this.loadUnits();
  }

  loadUnits(): void {
    this.isLoading = true;
    this.unitService.getAllUnits().subscribe({
      next: (units: Unit[]) => {
        this.units = units;
        this.isLoading = false;
      },
      error: (error: Error) => {
        this.snackBar.open('Error loading units', 'Close', { duration: 3000 });
        this.isLoading = false;
      }
    });
  }

  onSubmit(): void {
    if (this.unitForm.invalid) return;

    const unitData: Unit = {
      name: this.unitForm.value.name,
      symbol: this.unitForm.value.symbol
    };

    if (this.editingUnitId !== null) {
      unitData.id = this.editingUnitId;
    }

    const operation = this.editingUnitId !== null
      ? this.unitService.updateUnit(unitData)
      : this.unitService.addUnit(unitData);

    operation.subscribe({
      next: () => {
        this.snackBar.open(
          this.editingUnitId !== null ? 'Unit updated successfully' : 'Unit created successfully',
          'Close',
          { duration: 3000 }
        );
        this.resetForm();
        this.loadUnits();
      },
      error: (error: Error) => {
        this.snackBar.open('Error saving unit', 'Close', { duration: 3000 });
      }
    });
  }

  startEdit(unit: Unit): void {
    this.editingUnitId = unit.id ?? null;
    this.unitForm.patchValue({
      name: unit.name,
      symbol: unit.symbol
    });
  }

  deleteUnit(id: number): void {
    if (confirm('Are you sure you want to delete this unit?')) {
      this.unitService.deleteUnit(id).subscribe({
        next: () => {
          this.snackBar.open('Unit deleted successfully', 'Close', { duration: 3000 });
          this.loadUnits();
        },
        error: (error: Error) => {
          this.snackBar.open('Error deleting unit', 'Close', { duration: 3000 });
        }
      });
    }
  }

  resetForm(): void {
    this.unitForm.reset();
    this.editingUnitId = null;
  }
}
