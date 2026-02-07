import { environment } from '../../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ShoppingListResponseDTO, ShoppingListRequestDTO, ListItemResponseDTO, ListItemRequestDTO } from '../interfaces/shopping-list.interface';
import { catchError } from 'rxjs/operators';
import { BaseService } from './base.service';

@Injectable({
  providedIn: 'root'
})
export class ShoppingListService extends BaseService {
  private apiUrl = `${environment.apiUrl}/lists`;

  constructor(override http: HttpClient) {
    super();
  }

  getAllShoppingLists(): Observable<ShoppingListResponseDTO[]> {
    return this.http.get<ShoppingListResponseDTO[]>(this.apiUrl)
      .pipe(catchError(this.handleError));
  }

  getShoppingListById(id: number): Observable<ShoppingListResponseDTO> {
    return this.http.get<ShoppingListResponseDTO>(`${this.apiUrl}/${id}`)
      .pipe(catchError(this.handleError));
  }

  createShoppingList(list: ShoppingListRequestDTO): Observable<ShoppingListResponseDTO> {
    return this.http.post<ShoppingListResponseDTO>(this.apiUrl, list)
      .pipe(catchError(this.handleError));
  }

  updateShoppingList(list: ShoppingListRequestDTO): Observable<ShoppingListResponseDTO> {
    return this.http.put<ShoppingListResponseDTO>(this.apiUrl, list)
      .pipe(catchError(this.handleError));
  }

  deleteShoppingList(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`)
      .pipe(catchError(this.handleError));
  }

  getAllListItems(listId: number): Observable<ListItemResponseDTO[]> {
    return this.http.get<ListItemResponseDTO[]>(`${this.apiUrl}/${listId}/items`)
      .pipe(catchError(this.handleError));
  }

  addListItem(listItem: ListItemRequestDTO): Observable<ListItemResponseDTO> {
    return this.http.post<ListItemResponseDTO>(`${this.apiUrl}/${listItem.listId}/items`, listItem)
      .pipe(catchError(this.handleError));
  }

  updateListItem(listId: number, itemId: number, listItem: ListItemRequestDTO): Observable<ListItemResponseDTO> {
    return this.http.put<ListItemResponseDTO>(`${this.apiUrl}/${listId}/items/${itemId}`, listItem)
      .pipe(catchError(this.handleError));
  }

  deleteListItem(listId: number, itemId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${listId}/items/${itemId}`)
      .pipe(catchError(this.handleError));
  }
}
