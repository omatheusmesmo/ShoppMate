import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ItemRequestDTO, ItemResponseDTO } from '../interfaces/item.interface';
import { environment } from '../../../environments/environment';
import { catchError } from 'rxjs/operators';
import { BaseService } from './base.service';

@Injectable({
  providedIn: 'root',
})
export class ItemService extends BaseService {
  private apiUrl = `${environment.apiUrl}/item`;

  constructor(override http: HttpClient) {
    super();
  }

  getAllItems(): Observable<ItemResponseDTO[]> {
    return this.http
      .get<ItemResponseDTO[]>(this.apiUrl)
      .pipe(catchError(this.handleError));
  }

  getItemById(id: number): Observable<ItemResponseDTO> {
    return this.http
      .get<ItemResponseDTO>(`${this.apiUrl}/${id}`)
      .pipe(catchError(this.handleError));
  }

  addItem(item: ItemRequestDTO): Observable<ItemResponseDTO> {
    return this.http
      .post<ItemResponseDTO>(this.apiUrl, item)
      .pipe(catchError(this.handleError));
  }

  updateItem(id: number, item: ItemRequestDTO): Observable<ItemResponseDTO> {
    return this.http
      .put<ItemResponseDTO>(`${this.apiUrl}/${id}`, item)
      .pipe(catchError(this.handleError));
  }

  deleteItem(id: number): Observable<void> {
    return this.http
      .delete<void>(`${this.apiUrl}/${id}`)
      .pipe(catchError(this.handleError));
  }
}
