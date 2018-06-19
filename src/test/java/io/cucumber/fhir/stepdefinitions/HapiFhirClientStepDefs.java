package io.cucumber.fhir.stepdefinitions;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;

import org.hl7.fhir.dstu3.model.Patient;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IClientInterceptor;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.IHttpRequest;
import ca.uhn.fhir.rest.client.api.IHttpResponse;
import cucumber.api.java8.En;

public class HapiFhirClientStepDefs implements En {

	private final static FhirContext mContext = FhirContext.forDstu3();
	private IGenericClient mClient;
	private MethodOutcome mOutcome;
	private IHttpResponse mResponse;
	private IHttpRequest mRequest;

	public HapiFhirClientStepDefs() {
		Before(new String[] {}, 0, 1, () -> {
			initHapiClient();
		});

		When("new patient is created", () -> newPatient());

		Then("no errors returned", () -> noErrors());

		And("status code is {int}", (code) -> statusCode((int) code));

		After(new String[] {}, 0, 1, () -> {
			mClient = null;
			mOutcome = null;
			mResponse = null;
			mRequest = null;
		});
	}

	private void initHapiClient() {
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

	private void newPatient() {
		Patient patient = new Patient();
		mOutcome = mClient.create().resource(patient).execute();
	}

	private void noErrors() {
		assertThat(mOutcome, not(nullValue()));
		// TODO: implement check to see whether outcome contains any severe issues
	}

	private void statusCode(int code) {
		assertThat(mResponse, not(nullValue()));
		assertThat(mResponse.getStatus(), equalTo(code));
	}

}