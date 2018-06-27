Feature: Incident


Scenario Outline: Retrieve an incident
Given the incident identifier "<Incident ID>"
When a client retrieves an incident using this identifier
Then the result must be a valid incident

Examples:
| Incident ID       |
| NSIR-FHIR-IVY-001 |
| NSIR-FHIR-IVY-002 |
