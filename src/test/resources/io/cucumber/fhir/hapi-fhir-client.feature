Feature: HAPI-FHIR Client

# Create a new patient
Scenario: Create a new patient
When new patient is created
Then no errors returned
And status code is 201
