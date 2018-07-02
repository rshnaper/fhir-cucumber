package ca.cihi.nsir.fhir.incident.stepdefinitions;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidatorSettings.settings;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.fge.jsonschema.SchemaVersion;
import com.github.fge.jsonschema.cfg.ValidationConfiguration;
import com.github.fge.jsonschema.main.JsonSchemaFactory;

import ca.cihi.nsir.fhir.Constants;
import ca.cihi.nsir.fhir.TestSupport;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.module.jsv.JsonSchemaValidator;
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
	public void executeOnceBeforeEachTest() {
		if (!isInitialized) {
			try {
				RestAssured.baseURI = Constants.NSIR_WS_BASE_URI;
				RestAssured.basePath = Constants.NSIR_WS_BASE_PATH;
				
//				JsonSchemaValidator.settings = settings().with()
//						.jsonSchemaFactory(JsonSchemaFactory.newBuilder()
//								.setValidationConfiguration(
//										ValidationConfiguration.newBuilder().setDefaultVersion(SchemaVersion.DRAFTV4).freeze())
//								.freeze())
//						.and().with().checkedValidation(true);
				
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
	    	.header("Content-Type", Constants.NSID_WS_CONTENT_TYPE)
	    	.header("Accept", Constants.NSID_WS_CONTENT_TYPE)
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
			.assertThat()
			.statusCode(200);
	}

	@Then("the result must be a valid incident")
	public void theResultMustBeAValidIncident() {
	    String jsonString = response.body().asString();
		LOG.info("response: \n{}", TestSupport.toPrettyFormatJson(jsonString));
		
		// Validate using JSON schema
		response.then()
			.assertThat()
			.contentType(ContentType.JSON)
			.and()
			.body(JsonSchemaValidator.matchesJsonSchemaInClasspath("incident.json"));
		
		// Validate specific fields
		validatableResponse.and()
			.assertThat()
			.body("resourceType", equalTo("Bundle"))
			.and()
			.body("entry[2].resource.identifier.value", equalTo(identifier));
	}
	
}
