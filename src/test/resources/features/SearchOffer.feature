Feature: Peregrine Travel agent Holiday booking feature
  client searches for offer based on the destination, date and no of nights of stay, then client will be
  provided with the list of offers

  Scenario: Client searches for offer based on valid destination, date and no of nights the client will be provided with
    a list of available offers
    Given Peregrine holiday booking system is ready and available
    When  client search for holiday in "London" on date "25/02/2016" for 4 nights
    Then  client will be presented with a list of offers