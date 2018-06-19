package io.cucumber.fhir;

import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

@RunWith(Cucumber.class)
@CucumberOptions(
	features = "classpath:io/cucumber/fhir/features/patient/hapi-fhir-client.feature",
	glue = "classpath:io/cucumber/fhir/stepdefinitions", 
	strict = true, 
	monochrome = true, 
	plugin = { "pretty", "html:target/cucumber", "json:target/cucumber/results.json", "junit:target/cucumber/results.xml" })
public class HapiFhirClientTest {
}