import { ShoppingList, ShoppingListResponseDTO } from './shopping-list.interface';
import { User, UserResponseDTO } from './user.interface';

export enum Permission {
  READ = 'READ',
  WRITE = 'WRITE'
}

export interface ListPermission {
  id?: number;
  createdAt?: string;
  updatedAt?: string;
  deleted?: boolean;
  shoppingList: ShoppingList;
  user: User;
  permission: Permission;
}

export interface ListPermissionRequestDTO {
  idList: number;
  idUser: number;
  permission: Permission;
}

export interface ListPermissionResponseDTO {
  id: number;
  shoppingListResponseDTO: ShoppingListResponseDTO;
  userResponseDTO: UserResponseDTO;
  permission: Permission;
}
