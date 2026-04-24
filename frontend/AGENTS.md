# AGENTS.md - ShoppMate Frontend

Angular 19 frontend application for ShoppMate shopping list management system.

## Build/Lint/Test Commands

```bash
# Development
npm start                  # Start dev server (ng serve)
npm run build             # Production build
npm run build:staging     # Staging build
npm run watch             # Watch mode build

# Linting
npm run lint              # ESLint via Angular CLI
npm run lint:custom       # Custom signals/OnPush check
npm run prettier:check    # Check formatting
npm run prettier          # Auto-format code

# Testing
npm test                  # Run all tests (Karma/Jasmine, watch mode)
npm run test:ci           # CI tests (headless, no watch)

# Running a single test
npx ng test --include='src/app/shared/services/item.service.spec.ts'
npx ng test --include='**/auth.interceptor.spec.ts'
# Or use fdescribe/fit in the test file, then run npm test
```

## Project Structure

```
src/app/
├── auth/           # Authentication (guards, login, signup)
├── layout/         # Layout components
├── list/           # List feature module
└── shared/         # Shared (components, interceptors, interfaces, services)
```

## Code Style Guidelines

### Imports Order

1. Angular core/common imports
2. Angular Material imports
3. Third-party imports (rxjs)
4. Application imports (relative paths)

```typescript
// 1. Angular core
import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
// 2. Angular Material
import { MatButtonModule } from '@angular/material/button';
// 3. Third-party
import { catchError } from 'rxjs/operators';
// 4. Application
import { ItemService } from '../../services/item.service';
```

### Components

**Standalone components with OnPush are required:**

```typescript
@Component({
  selector: 'app-example',
  standalone: true,
  imports: [CommonModule, MatButtonModule],
  templateUrl: './example.component.html',
  styleUrls: ['./example.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush, // REQUIRED
})
export class ExampleComponent implements OnInit {}
```

**Selectors:** Components use `kebab-case` with `app` prefix (e.g., `app-shopping-list`)

### Signals - REQUIRED for UI State

**All UI state must use Angular signals** (enforced by custom lint):

```typescript
// ✅ CORRECT
readonly shoppingLists = signal<ShoppingListResponseDTO[]>([]);
readonly isLoading = signal(false);

// ❌ WRONG
shoppingLists: ShoppingListResponseDTO[] = [];
isLoading = false;
```

### Dependency Injection

Use `inject()` function, not constructor injection:

```typescript
// ✅ CORRECT
export class ItemComponent {
  private itemService = inject(ItemService);
  private fb = inject(FormBuilder);
}

// ❌ AVOID
constructor(private itemService: ItemService) {}
```

### Services

Extend `BaseService` for HTTP operations:

```typescript
@Injectable({ providedIn: 'root' })
export class ItemService extends BaseService {
  private apiUrl = `${environment.apiUrl}/item`;

  getAllItems(): Observable<ItemResponseDTO[]> {
    return this.http.get<ItemResponseDTO[]>(this.apiUrl).pipe(catchError(this.handleError));
  }
}
```

### Interfaces

Use `RequestDTO`/`ResponseDTO` suffix for API DTOs:

```typescript
export interface ItemRequestDTO {
  name: string;
  idCategory: number;
}
export interface ItemResponseDTO {
  id: number;
  name: string;
}
```

### Error Handling

Use `FeedbackService` for user messages:

```typescript
this.itemService.deleteItem(id).subscribe({
  next: () => {
    this.feedback.success('Item excluído com sucesso');
    this.loadInitialData();
  },
  error: () => this.feedback.error('Erro ao excluir item'),
});
```

### Forms

Use typed forms with `nonNullable`:

```typescript
loginForm = this.fb.nonNullable.group({
  email: ['', [Validators.required, Validators.email]],
  password: ['', Validators.required],
});
```

### Routing

Use lazy loading for feature routes:

```typescript
{
  path: 'items',
  loadComponent: () => import('./list/components/items-management/items-management.component')
    .then(m => m.ItemsManagementComponent),
  canActivate: [authGuard],
}
```

### Testing

Use Jasmine/Karma with TestBed. Mock services from `shared/mocks/mock-services.ts`:

```typescript
beforeEach(() => {
  authServiceSpy = jasmine.createSpyObj('AuthService', ['getToken', 'logout']);
  TestBed.configureTestingModule({
    providers: [
      provideHttpClient(withInterceptors([authInterceptor])),
      provideHttpClientTesting(),
      { provide: AuthService, useValue: authServiceSpy },
    ],
  });
});

afterEach(() => httpTestingController.verify());
```

## Formatting

- **Indent:** 2 spaces | **Quotes:** Single | **Semicolons:** Required
- Run `npm run prettier` before committing

## Key Patterns

1. **ChangeDetectionStrategy.OnPush** - Required on all components
2. **Signals** - Required for all UI state properties
3. **Standalone components** - No NgModules
4. **inject()** - Preferred over constructor injection
5. **RxJS operators** - Use `pipe()` with `catchError`, `finalize`
6. **Lazy loading** - Use `loadComponent` for feature routes
7. **Forms** - Use `nonNullable` for typed forms
8. **Interfaces** - Use `RequestDTO`/`ResponseDTO` suffix for API DTOs
