import { User } from './user.interface';

export interface ShoppingList {
  id?: number;
  name: string;
  createdAt?: string;
  updatedAt?: string;
  deleted?: boolean;
  owner?: User;
}

export interface ShoppingListRequestDTO {
  name: string;
  idUser: number;
}

export interface UserResponseDTO {
  id: number;
  fullName: string;
  email: string;
}

export interface ShoppingListResponseDTO {
  idList: number;
  listName: string;
  owner: UserResponseDTO;
}

export interface ListItemResponseDTO {
  shoppingList: ShoppingListResponseDTO;
  item: ItemResponseDTO;
  idListItem: number;
  quantity: number;
  purchased: boolean;
}

export interface ListItemRequestDTO {
  listId: number;
  itemId: number;
  quantity?: number;
}

export interface CategoryResponseDTO {
  id: number;
  name: string;
}

export interface UnitResponseDTO {
  id: number;
  symbol: string;
}

export interface ItemResponseDTO {
  id: number;
  name: string;
  category: CategoryResponseDTO;
  unit: UnitResponseDTO;
}

export interface ItemRequestDTO {
  name: string;
  idCategory: number;
  idUnit: number;
}

export interface ListPermissionResponseDTO {
  id: number;
  shoppingListResponseDTO: ShoppingListResponseDTO;
  userResponseDTO: UserResponseDTO;
  permission: 'READ' | 'WRITE';
}

export interface ListPermissionRequestDTO {
  idList: number;
  idUser: number;
  permission: 'READ' | 'WRITE';
}

export interface LoginRequest {
  email: string;
  password: string;
}
