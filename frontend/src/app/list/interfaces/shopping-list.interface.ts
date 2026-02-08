export interface UserResponseDTO {
  id: number;
  fullName: string;
  email: string;
}

export interface ShoppingListRequestDTO {
  name: string;
  idUser: number;
}

export interface ShoppingListResponseDTO {
  idList: number;
  listName: string;
  owner: UserResponseDTO;
}
