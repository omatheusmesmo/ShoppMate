import { Routes } from '@angular/router';
import { LoginComponent } from './auth/login/login.component';
import { SignupComponent } from './auth/signup/signup.component';
import { ShoppingListComponent } from './list/components/shopping-list/shopping-list.component';
import { ListDetailsComponent } from './list/components/list-details/list-details.component';
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
    component: LoginComponent,
    canActivate: [loggedInAuthGuard],
  },
  {
    path: 'signup',
    component: SignupComponent,
    canActivate: [loggedInAuthGuard],
  },
  {
    path: 'lists',
    component: ShoppingListComponent,
    canActivate: [authGuard],
  },
  {
    path: 'lists/:id',
    component: ListDetailsComponent,
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
