import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../../shared/services/auth.service';
import { map, take } from 'rxjs';

export const loggedInAuthGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  return authService.isLoggedIn$.pipe(
    take(1),
    map(isLoggedIn => {
      if (isLoggedIn) {
        router.navigate(['/lists']); // Redirect to lists if already logged in
        return false;
      }
      return true; // Allow access to login/signup if not logged in
    })
  );
};
