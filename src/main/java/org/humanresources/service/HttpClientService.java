package org.humanresources.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.humanresources.model.Employee;
import org.humanresources.model.Office;
import org.humanresources.model.Role;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.humanresources.constants.URIConstants.*;

@Service
@AllArgsConstructor
public class HttpClientService {

    private static final String VAULT_USERNAME = "Vault_Username";
    private static final String VAULT_PASSWORD = "Vault_Password";

    private static final String ENCODING = "UTF-8";

    private static final String SESSION_ID = "sessionId";
    private static final String RETRIEVED_DATA = "data";
    private static final String RETRIEVED_PICKLIST_VALUES = "picklistValues";

    public final CloseableHttpClient httpClient;

    public String getSessionId() throws IOException, JSONException {
        try (CloseableHttpResponse response = httpClient.execute(createLoginRequest())) {
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK && response.getEntity() != null) {
                JSONObject jsonResponse = new JSONObject(EntityUtils.toString(response.getEntity()));
                return jsonResponse.getString(SESSION_ID);
            }
        }
        return null;
    }

    public void postDataWithCSVFile(String url, File csvFile, String sessionId) throws IOException {
        HttpPost postRequest = createPostRequest(url);
        setHttpHeaderWithCSVEntity(postRequest, csvFile, sessionId);
        executeRequest(postRequest);
    }


    public void putDataWithCSVFile(String url, File csvFile, String sessionId) throws IOException {
        HttpPut putRequest = createPutRequest(url);
        setHttpHeaderWithCSVEntity(putRequest, csvFile, sessionId);
        executeRequest(putRequest);
    }

    public void postDataWithTextEntity(String url, String sessionId, String requestBody) throws IOException {
        HttpPost postRequest = createPostRequest(url);
        postRequest.setHeader("Authorization", sessionId);
        postRequest.setHeader("Content-Type", "application/x-www-form-urlencoded");
        postRequest.setEntity(new StringEntity(requestBody));
        executeRequest(postRequest);
    }


    public Map<String, Employee> retrieveEmployees(String url, String sessionId) throws IOException, JSONException {
        return retrieveData(url, sessionId, RETRIEVED_DATA, new TypeReference<>() {}, Employee::getEmployeeId);
    }

    public Map<String, Office> retrieveOffices(String url, String sessionId) throws IOException, JSONException {
        return retrieveData(url, sessionId, RETRIEVED_DATA, new TypeReference<>() {}, Office::getName);
    }

    public Map<String, Role> retrieveRoles(String url, String sessionId) throws IOException, JSONException {
        return retrieveData(url, sessionId, RETRIEVED_PICKLIST_VALUES, new TypeReference<>() {}, Role::getLabel);
    }

    private HttpGet createGetRequest(String url, String sessionId) {
        HttpGet getRequest = new HttpGet(url);
        getRequest.setHeader(new BasicHeader("Authorization", sessionId));
        return getRequest;
    }

    private HttpPost createPostRequest(String url) {
        return new HttpPost(url);
    }

    private HttpPut createPutRequest(String url) {
        return new HttpPut(url);
    }

    private HttpPost createLoginRequest() throws UnsupportedEncodingException {
        HttpPost loginRequest = new HttpPost(VAULT_API_URL + AUTHORIZATION);
        loginRequest.setHeader(new BasicHeader("Content-Type", "application/x-www-form-urlencoded"));
        loginRequest.setHeader(new BasicHeader("Accept", "application/json"));
        loginRequest.setEntity(new StringEntity("username=" + URLEncoder.encode(System.getenv(VAULT_USERNAME), ENCODING)
                + "&password=" + URLEncoder.encode(System.getenv(VAULT_PASSWORD), ENCODING), ENCODING));
        return loginRequest;
    }

    private void setHttpHeaderWithCSVEntity(HttpEntityEnclosingRequestBase request, File csvFile, String sessionId){
        request.setHeader("Authorization", sessionId);
        request.setHeader("Content-Type", "text/csv");
        request.setHeader("Accept", "text/csv");
        request.setEntity(new FileEntity(csvFile, ContentType.TEXT_PLAIN));
    }

    private void executeRequest(HttpEntityEnclosingRequestBase request) throws IOException {
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK && response.getEntity() != null) {
                // this could be well-formated so it could be more user-friendly for logging purpose later
                System.out.println(EntityUtils.toString(response.getEntity()));
            } else {
                System.out.println("Request fail with response status: " + response.getStatusLine().getStatusCode());
            }
        }
    }

    private <T> Map<String, T> retrieveData(String url, String sessionId, String retrievedDataKey,
                                            TypeReference<List<T>> typeReference, Function<T, String> keyExtractor)
            throws IOException, JSONException {
        try (CloseableHttpResponse response = httpClient.execute(createGetRequest(url, sessionId))) {
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK && response.getEntity() != null) {
                JSONObject jsonResponse = new JSONObject(EntityUtils.toString(response.getEntity()));
                List<T> dataList = new ObjectMapper().readValue(jsonResponse.getString(retrievedDataKey), typeReference);
                return dataList.stream().collect(Collectors.toMap(keyExtractor, Function.identity()));
            }
            return null;
        }
    }

}
