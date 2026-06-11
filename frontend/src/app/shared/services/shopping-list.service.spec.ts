import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ShoppingListService } from './shopping-list.service';
import { environment } from '../../../environments/environment';
import { ShoppingListResponseDTO } from '../interfaces/shopping-list.interface';
import { ListItemResponseDTO, ListItemRequestDTO } from '../interfaces/list-item.interface';

describe('ShoppingListService', () => {
  let service: ShoppingListService;
  let httpMock: HttpTestingController;
  const apiUrl = `${environment.apiUrl}/lists`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ShoppingListService],
    });
    service = TestBed.inject(ShoppingListService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  it('should fetch all shopping lists', () => {
    const mockLists: ShoppingListResponseDTO[] = [
      {
        idList: 1,
        listName: 'Groceries',
        owner: { id: 1, fullName: 'User', email: 'test@test.com' },
        totalValue: 10.0,
      },
    ];

    service.getAllShoppingLists().subscribe((lists) => {
      expect(lists).toEqual(mockLists);
    });

    const req = httpMock.expectOne(apiUrl);
    expect(req.request.method).toBe('GET');
    req.flush(mockLists);
  });

  it('should add a list item', () => {
    const mockItemRequest: ListItemRequestDTO = { listId: 1, itemId: 5, quantity: 2 };

    const mockItemResponse = {
      idListItem: 10,
      quantity: 2,
      purchased: false,
      unitPrice: 5.0,
      totalPrice: 10.0,
    } as ListItemResponseDTO;

    service.addListItem(mockItemRequest).subscribe((res) => {
      expect(res.idListItem).toEqual(10);
    });

    const req = httpMock.expectOne(`${apiUrl}/1/items`);
    expect(req.request.method).toBe('POST');
    req.flush(mockItemResponse);
  });

  afterEach(() => httpMock.verify());
});
