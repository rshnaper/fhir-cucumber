package ca.cihi.nsir.fhir.incident;

import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;
import cucumber.api.SnippetType;
import cucumber.api.junit.Cucumber;

@RunWith(Cucumber.class)
@CucumberOptions(
	features = "classpath:ca/cihi/nsir/fhir/incident/features",
	glue = "classpath:ca/cihi/nsir/fhir/incident/stepdefinitions", 
	strict = true, 
	monochrome = true, 
	snippets = SnippetType.CAMELCASE, 
	plugin = { "pretty", "html:target/cucumber", "json:target/cucumber/results.json", "junit:target/cucumber/results.xml" })
public class IncidentTest {
}
