import { of } from 'rxjs';
import { Unit } from '../interfaces/unit.interface';

export class MockAuthService {
  isLoggedIn$ = of(true);
  login = jasmine.createSpy('login').and.returnValue(of('mock-token'));
  register = jasmine.createSpy('register').and.returnValue(of({}));
  logout = jasmine.createSpy('logout');
  getToken = jasmine.createSpy('getToken').and.returnValue('mock-token');
  getCurrentUserId = jasmine.createSpy('getCurrentUserId').and.returnValue(1);
}

export class MockUnitService {
  getAllUnits = jasmine.createSpy('getAllUnits').and.returnValue(of([]));
  addUnit = jasmine
    .createSpy('addUnit')
    .and.returnValue(of({ id: 1, name: 'Test', symbol: 'T' } as Unit));
  updateUnit = jasmine
    .createSpy('updateUnit')
    .and.returnValue(of({ id: 1, name: 'Test Updated', symbol: 'TU' } as Unit));
  deleteUnit = jasmine.createSpy('deleteUnit').and.returnValue(of(undefined));
}

export class MockMatSnackBar {
  open = jasmine.createSpy('open');
}

export class MockRouter {
  navigate = jasmine.createSpy('navigate');
}
