describe('Registration, login, and list creation flow', () => {
  it('registers, logs in, and creates a shopping list', () => {
    const timestamp = Date.now();
    const fullName = 'Cypress Test User';
    const email = `cypress-${timestamp}@example.com`;
    const password = 'Password123!';
    const listName = `Cypress Groceries ${timestamp}`;

    cy.visit('/signup');

    cy.get('[data-cy="signup-full-name"]').type(fullName);
    cy.get('[data-cy="signup-email"]').type(email);
    cy.get('[data-cy="signup-password"]').type(password);
    cy.get('[data-cy="signup-submit"]').click();

    cy.url().should('include', '/login');

    cy.get('[data-cy="login-email"]').type(email);
    cy.get('[data-cy="login-password"]').type(password);
    cy.get('[data-cy="login-submit"]').click();

    cy.url().should('include', '/lists');

    cy.window().then((win) => {
  const token = win.localStorage.getItem('auth_token');
  expect(token).to.not.be.null;

  const payload = JSON.parse(atob(token!.split('.')[1]));
  cy.log(JSON.stringify(payload));
});

cy.intercept('POST', '**/api/lists').as('createList');
    cy.get('[data-cy="create-list-button"]').first().click();
    cy.get('[data-cy="list-name-input"]').type(listName);
    cy.get('[data-cy="save-list-button"]').click();

    cy.wait('@createList')
      .its('response.statusCode')
      .should('be.oneOf', [200, 201]);

    cy.contains(listName).should('be.visible');
  });
});
