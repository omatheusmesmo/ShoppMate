import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ListItemRequestDTO, ListItemResponseDTO } from '../interfaces/list-item.interface';
import { BaseService } from './base.service';
import { catchError } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class ListItemService extends BaseService {
  private readonly endpoint = `${this.baseUrl}/lists`;

  getAllListItemsByListId(listId: number): Observable<ListItemResponseDTO[]> {
    return this.http.get<ListItemResponseDTO[]>(`${this.endpoint}/${listId}/items`);
  }

  getListItemsByListId(listId: number): Observable<ListItemResponseDTO[]> {
    return this.http.get<ListItemResponseDTO[]>(`${this.endpoint}/${listId}/items`)
      .pipe(catchError(this.handleError));
  }

  getListItemById(listId: number, itemId: number): Observable<ListItemResponseDTO> {
    return this.http.get<ListItemResponseDTO>(`${this.endpoint}/${listId}/items/${itemId}`)
      .pipe(catchError(this.handleError));
  }

  addListItem(listId: number, item: Omit<ListItemRequestDTO, 'listId'>): Observable<ListItemResponseDTO> {
    const request: ListItemRequestDTO = {
      ...item,
      listId
    };
    return this.http.post<ListItemResponseDTO>(`${this.endpoint}/${listId}/items`, request)
      .pipe(catchError(this.handleError));
  }

  updateListItem(listId: number, itemId: number, item: ListItemRequestDTO): Observable<ListItemResponseDTO> {
    return this.http.put<ListItemResponseDTO>(`${this.endpoint}/${listId}/items/${itemId}`, item)
      .pipe(catchError(this.handleError));
  }

  deleteListItem(listId: number, itemId: number): Observable<void> {
    return this.http.delete<void>(`${this.endpoint}/${listId}/items/${itemId}`)
      .pipe(catchError(this.handleError));
  }
}
