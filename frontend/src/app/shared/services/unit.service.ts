import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Unit } from '../interfaces/unit.interface';
import { environment } from '../../../environments/environment';
import { catchError } from 'rxjs/operators';
import { BaseService } from './base.service';

@Injectable({
  providedIn: 'root'
})
export class UnitService extends BaseService {
  private apiUrl = `${environment.apiUrl}/unit`;

  constructor(override http: HttpClient) {
    super();
  }

  getAllUnits(): Observable<Unit[]> {
    return this.http.get<Unit[]>(this.apiUrl)
      .pipe(catchError(this.handleError));
  }

  addUnit(unit: Unit): Observable<Unit> {
    return this.http.post<Unit>(this.apiUrl, unit)
      .pipe(catchError(this.handleError));
  }

  updateUnit(unit: Unit): Observable<Unit> {
    const requestBody = {
      id: unit.id,
      name: unit.name,
      abbreviation: unit.symbol  // Map symbol to abbreviation for API
    };
    return this.http.put<Unit>(this.apiUrl, requestBody)
      .pipe(catchError(this.handleError));
  }

  deleteUnit(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`)
      .pipe(catchError(this.handleError));
  }
}
