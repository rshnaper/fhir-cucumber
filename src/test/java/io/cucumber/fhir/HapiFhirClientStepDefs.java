package io.cucumber.fhir;

import java.io.IOException;

import javax.management.OperationsException;

import cucumber.api.PendingException;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.And;
import cucumber.api.java8.En;
import ca.uhn.fhir.context.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.*;
import ca.uhn.fhir.util.OperationOutcomeUtil;

import org.hl7.fhir.dstu3.model.*;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;


public class HapiFhirClientStepDefs implements En {
    
    private final static FhirContext mContext = FhirContext.forDstu3();
    private final IGenericClient mClient;
    private MethodOutcome mOutcome;
    private IHttpResponse mResponse;
    private IHttpRequest mRequest;
    
 
    public HapiFhirClientStepDefs() {
         mClient = mContext.newRestfulGenericClient("http://hapi.fhir.org/baseDstu3");
         mClient.registerInterceptor(new IClientInterceptor() {
            
            @Override
            public void interceptResponse(IHttpResponse theResponse) throws IOException {
                mResponse = theResponse;
            }
            
            @Override
            public void interceptRequest(IHttpRequest theRequest) {
                mRequest = theRequest;
            }
        });
    }
    
    @When("new patient is created")
    public void newPatient() {
        Patient patient = new Patient();
        mOutcome = mClient.create().resource(patient).execute();
    }
    
    @Then("no errors returned")
    public void noErrors() {
        assertThat(mOutcome, not(nullValue()));
        //TODO: implement check to see whether outcome contains any severe issues
    }
    
    
    @And("status code is {int}")
    public void statusCode(int code) {
        assertThat(mResponse, not(nullValue()));
        assertThat(mResponse.getStatus(), equalTo(code));
    }
    
}