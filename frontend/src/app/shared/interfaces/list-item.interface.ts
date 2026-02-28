import { ItemResponseDTO } from './item.interface';
import { ShoppingListResponseDTO } from './shopping-list.interface';

export interface ListItemRequestDTO {
  listId: number;
  itemId: number;
  quantity?: number;
  unitPrice?: number;
}

export interface ListItemResponseDTO {
  shoppingList: ShoppingListResponseDTO;
  item: ItemResponseDTO;
  idListItem: number;
  quantity: number;
  purchased: boolean;
  unitPrice: number;
  totalPrice: number;
}
