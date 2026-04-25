import { Routes } from '@angular/router';
import { LandingComponent } from './layout/landing/landing.component';
import { authGuard } from './auth/guards/auth.guard';
import { loggedInAuthGuard } from './auth/guards/logged-in-auth.guard';

export const AppRoutes: Routes = [
  {
    path: '',
    component: LandingComponent,
    pathMatch: 'full',
  },
  {
    path: 'login',
    loadComponent: () => import('./auth/login/login.component').then((m) => m.LoginComponent),
    canActivate: [loggedInAuthGuard],
  },
  {
    path: 'signup',
    loadComponent: () => import('./auth/signup/signup.component').then((m) => m.SignupComponent),
    canActivate: [loggedInAuthGuard],
  },
  {
    path: 'lists',
    loadComponent: () =>
      import('./list/components/shopping-list/shopping-list.component').then(
        (m) => m.ShoppingListComponent,
      ),
    canActivate: [authGuard],
  },
  {
    path: 'lists/:id',
    loadComponent: () =>
      import('./list/components/list-details/list-details.component').then(
        (m) => m.ListDetailsComponent,
      ),
    canActivate: [authGuard],
  },
  {
    path: 'items',
    loadComponent: () =>
      import('./list/components/items-management/items-management.component').then(
        (m) => m.ItemsManagementComponent,
      ),
    canActivate: [authGuard],
  },
  {
    path: 'categories',
    loadComponent: () =>
      import('./list/components/categories-management/categories-management.component').then(
        (m) => m.CategoriesManagementComponent,
      ),
    canActivate: [authGuard],
  },
  {
    path: 'units',
    loadComponent: () =>
      import('./list/components/units-management/units-management.component').then(
        (m) => m.UnitsManagementComponent,
      ),
    canActivate: [authGuard],
  },
  {
    path: '**',
    redirectTo: '',
  },
];

export default AppRoutes;
