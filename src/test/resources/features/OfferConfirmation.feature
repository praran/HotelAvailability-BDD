Feature: Peregrine Travel agent Holiday booking feature
  clients will be provided with a feature to book holidays; by entering the location, date and number of nights
  the booking system will return a list of offers.
  The client can choose an suitable offer and submits the id of the offer along with the token, Peregrine booking
  system then confirms the booking for the client

  Background:
    Given Peregrine holiday booking system is ready and available
    When  client search for holiday in "London" on date "25/02/2016" for 4 nights
    Then  client will be presented with a list of offers

  Scenario: When client tries booking holiday with invalid offer id
    When  Client passes invalid UUID of the offer "c94cf1c8-4a4f-4ac4-a7d3-4ec8b3e9e550" and "authToken"
    Then  Peregrine holiday booking system will throw Nosuch element exception

  Scenario: Client booking holiday with expired offer id
    When Client passes UUID of expired offer and token "authToken"
    Then Peregrine holiday booking system will throw illegal state exception

  Scenario: Client booking holiday with valid offer and confirms within two minutes
    When Client passes UUID of valid offer and token "authToken"
    And  Client confirms booking within two minutes
    Then Peregrine holiday booking system will confirm booking with no booking charges

  Scenario: Client booking holiday with valid offer and confirms after two minutes but less
    than 10 minutes
    When Client passes UUID of valid offer and token "authToken"
    And  Client confirms booking greater than two minutes but less than 10 minutes
    Then Confirm booking with processing charge 5% of room rate or £10 which ever is less

  Scenario: Client booking holiday with valid offer and confirms offer between 11 and 20 minutes
    When Client passes UUID of valid offer and token "authToken"
    And  Client confirms booking between 11 and 20 minutes
    Then Confirm booking with processing charge 20£

  Scenario: Client booking holiday in advance of 3 months or more
    And  Client books a holiday three months in advance
    When Client passes UUID of valid offer and token "authToken"
    And  Client confirms booking between 11 and 20 minutes
    Then Confirm booking with 10% discount on total booking price