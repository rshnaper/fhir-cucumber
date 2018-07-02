package ca.cihi.nsir.fhir;

import java.io.DataInputStream;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaSigner;
import org.springframework.security.jwt.crypto.sign.Signer;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class TestSupport {
	
	private static final Logger LOG = LoggerFactory.getLogger(TestSupport.class);
	
	private static ClientHttpRequestFactory getClientHttpRequestFactory() {
		int timeout = 5000;
		RequestConfig config = RequestConfig.custom()
				.setConnectTimeout(timeout)
				.setConnectionRequestTimeout(timeout)
				.setSocketTimeout(timeout)
				.build();
		
		CloseableHttpClient client = HttpClientBuilder.create()
				.setDefaultRequestConfig(config)
				.build();
		
		return new HttpComponentsClientHttpRequestFactory(client);
	}

	@SuppressWarnings("unchecked")
	public static String getAccessToken() throws InvalidKeySpecException, NoSuchAlgorithmException, IOException {
		ClientHttpRequestFactory requestFactory = getClientHttpRequestFactory();
		RestTemplate restTemplate = new RestTemplate(requestFactory);

		String tokenResourceUri = "https://apsit.cihi.ca/gateway/oauth/token";
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
		map.add("grant_type", "client_credentials");
		map.add("assertion", getRequestToken());

		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map, headers);
		ResponseEntity<String> response = restTemplate.postForEntity(tokenResourceUri, request, String.class);
		
		Map<String, String> responseBodyMap = new Gson().fromJson(response.getBody(), Map.class);
		
		return responseBodyMap.get("access_token");
	}

	public static String getRequestToken() throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
		String systemIdentifier = "nsir_fhir_rt";
		String scope = "NSIR_FHIR";
		String gatewayEndpoint = "https://apsit.cihi.ca/gateway/oauth/token";

		long issuingTime = Instant.now().toEpochMilli() / 1000;
		long expiryTime = issuingTime + 360000;

		Map<String, String> headers = new HashMap<>();
		headers.put("alg", "RS256");
		headers.put("typ", "JWT");

		StringBuffer claims = new StringBuffer();
		claims.append("{\"iss\":\"").append(systemIdentifier).append("\",");
		claims.append("\"sub\":\"").append("AccessRequest" + systemIdentifier).append("\",");
		claims.append("\"scope\":\"").append(scope).append("\",");
		claims.append("\"aud\":\"").append(gatewayEndpoint).append("\",");
		claims.append("\"iat\":\"").append(issuingTime).append("\",");
		claims.append("\"exp\":\"").append(expiryTime).append("\"}");

		Resource resource = new ClassPathResource("nsir_fhir_rt.key");

		byte[] privateKeyBytes = new byte[0];
		try (DataInputStream dis = new DataInputStream(resource.getInputStream())) {
			privateKeyBytes = new byte[(int) resource.contentLength()];
			dis.read(privateKeyBytes);
		}
		
		PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(privateKeyBytes);
		PrivateKey privateKey = KeyFactory.getInstance("RSA").generatePrivate(spec);
		Signer signer = new RsaSigner((RSAPrivateKey) privateKey);

		Jwt jwt = JwtHelper.encode(claims, signer, headers);
		String requestToken = jwt.getEncoded();

		return requestToken;
	}
	
	@SuppressWarnings("unchecked")
	public static Map<String, Object> convertJsonToMap(String json) {
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = new HashMap<>();
		try {
			map = objectMapper.readValue(json, HashMap.class);
		}
		catch (JsonParseException e) {
			LOG.error(ExceptionUtils.getStackTrace(e));
		}
		catch (JsonMappingException e) {
			LOG.error(ExceptionUtils.getStackTrace(e));
		}
		catch (IOException e) {
			LOG.error(ExceptionUtils.getStackTrace(e));
		}
		return map;
	}
	
	public static String toPrettyFormatJson(String jsonString) {
		JsonParser parser = new JsonParser();
		JsonObject json = parser.parse(jsonString).getAsJsonObject();

		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String prettyJson = gson.toJson(json);

		return prettyJson;
	}

}
