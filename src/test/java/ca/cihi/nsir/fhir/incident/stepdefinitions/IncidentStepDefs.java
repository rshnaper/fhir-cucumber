package ca.cihi.nsir.fhir.incident.stepdefinitions;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.cihi.nsir.fhir.Constants;
import ca.cihi.nsir.fhir.TestSupport;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;

public class IncidentStepDefs {
	
	private static final Logger LOG = LoggerFactory.getLogger(IncidentStepDefs.class);
	
	private RequestSpecification requestSpecification;
	private ValidatableResponse validatableResponse;
	private Response response;
	private static String identifier = null;
	private static String accessToken = null;
	private static boolean isInitialized = false;
	
	@Before()
	public void executeOnceBeforeAllTests() {
		if (!isInitialized) {
			RestAssured.baseURI = Constants.NSIR_WS_BASE_URI;
			RestAssured.basePath = Constants.NSIR_WS_BASE_PATH;
			
			try {
				accessToken = TestSupport.getAccessToken();
				isInitialized = true;
			}
			catch (InvalidKeySpecException e) {
				LOG.error(ExceptionUtils.getStackTrace(e));
			}
			catch (NoSuchAlgorithmException e) {
				LOG.error(ExceptionUtils.getStackTrace(e));
			}
			catch (IOException e) {
				LOG.error(ExceptionUtils.getStackTrace(e));
			}
		}
	}
	
	@Given("the incident identifier {string}")
	public void theIncidentIdentifier(String incidentId) {
		assertThat(incidentId, is(notNullValue()));
		identifier = incidentId;
		// Setup the request
	    requestSpecification = given()
	    	.header("Authorization", "Bearer " + accessToken)
	    	.header("Content-Type", "application/fhir+json")
	    	.header("Accept", "application/fhir+json")
    		.param("identifier", incidentId)
    		.param("_profile", Constants.NSIR_FHIR_PROFILE_URI_RADIATION_ADVERSE_EVENT);
	}

	@When("a client retrieves an incident using this identifier")
	public void aClientRetrievesAnIncidentUsingThisIdentifier() {
		// Do a GET request
		response = requestSpecification.when()
	    	.get("/Bundle");
		
		// Check the response status code 
	    assertThat(response, is(notNullValue()));
		validatableResponse = response.then()
			.statusCode(200);
	}

	@SuppressWarnings("unchecked")
	@Then("the result must be a valid incident")
	public void theResultMustBeAValidIncident() {
	    String json = response.body().asString();
		LOG.info("response: \n{}", TestSupport.toPrettyFormatJson(json));
		
		validatableResponse.and()
			.body("resourceType", equalTo("Bundle"))
			.and()
			.body("entry[2].resource.identifier.value", equalTo(identifier));
		
//		// Do very basic validation on the response body
//		Map<String, Object> resultMap = TestSupport.convertJsonToMap(json);
//		assertThat(resultMap.get("resourceType"), is("Bundle"));
//		List<Object> entries = (List<Object>) resultMap.get("entry");
//		entries.stream().forEach(entry -> {
//			Map<String, Object> entryMap = (Map<String, Object>) ((Map<String, Object>) entry).get("resource");
//			assertThat(isValidEntry(entryMap), is(true));
//		});
	}
	
	private boolean isValidEntry(Map<String, Object> entryMap) {
		String resourceType = entryMap.get("resourceType").toString();
		LOG.info(resourceType);
		switch (resourceType) {
			case "Composition":
			case "Device":
			case "AdverseEvent":
			case "Patient":
			case "Basic":
				return true;
		}
		return false;
	}
	
}
