describe('Student Walkthrough', () => {
    before(() => {
        cy.cleanItemCombinationQuestionsByName('Cypress Question Example');
      });
      after(() => {
        cy.cleanItemCombinationQuestionsByName('Cypress Question Example');
      });

    it('Student answers a quiz', function() {


        cy.demoStudentLogin();
        cy.server();
        cy.route('GET', '/student/*/available').as('getAvailable');
        cy.get('[data-cy="quizzesStudentMenuButton"]').click();
        cy.get('[data-cy="studentAvailableQuizButton"]').click();

        cy.contains(':')
            .parent()
            .click();

        cy.get('[data-cy="endQuizButton"]').click();
        cy.get('[data-cy="confirmationButton"]').click();
 
    });
});