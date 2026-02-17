import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  ListPermission,
  ListPermissionRequestDTO,
  ListPermissionResponseDTO,
} from '../interfaces/list-permission.interface';
import { BaseService } from './base.service';
import { catchError } from 'rxjs/operators';

@Injectable({
  providedIn: 'root',
})
export class ListPermissionService extends BaseService {
  constructor(override http: HttpClient) {
    super();
  }

  getAllListPermissions(shoppingListId: number): Observable<ListPermission[]> {
    return this.http
      .get<
        ListPermission[]
      >(`${this.baseUrl}/lists/${shoppingListId}/permissions`)
      .pipe(catchError(this.handleError));
  }

  addListPermission(
    listPermission: ListPermissionRequestDTO,
  ): Observable<ListPermissionResponseDTO> {
    return this.http
      .post<ListPermissionResponseDTO>(
        `${this.baseUrl}/lists/${listPermission.idList}/permissions`,
        listPermission,
      )
      .pipe(catchError(this.handleError));
  }

  updateListPermission(
    listId: number,
    permissionId: number,
    listPermission: ListPermission,
  ): Observable<ListPermission> {
    return this.http
      .put<ListPermission>(
        `${this.baseUrl}/lists/${listId}/permissions/${permissionId}`,
        listPermission,
      )
      .pipe(catchError(this.handleError));
  }

  deleteListPermission(shoppingListId: number, id: number): Observable<void> {
    return this.http
      .delete<void>(`${this.baseUrl}/lists/${shoppingListId}/permissions/${id}`)
      .pipe(catchError(this.handleError));
  }
}
