package com.ibm.infosvr.restclient;

import javax.net.ssl.*;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.ibm.infosvr.restclient.model.*;
import org.springframework.util.Base64Utils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class IGCRestClient {

    private static final String TYPES = "/ibm/iis/igc-rest/v1/types";
    private static final String ASSET = "/ibm/iis/igc-rest/v1/assets";
    private static final String SEARCH = "/ibm/iis/igc-rest/v1/search";
    private static final String LOGOUT  = "/ibm/iis/igc-rest/v1/logout";

    private String authorization;
    private String baseURL;
    private Boolean workflowEnabled = false;
    private List<String> cookies = null;

    private ObjectMapper mapper;

    // TODO: pickup the URL and authorization information from a properties file, by default
    public IGCRestClient() {
        this(null, null);
    }

    /**
     * Default constructor used by the IGCRestClient.
     *
     * Creates a new session on the server and retains the cookies to re-use the same session for the life
     * of the client (or until the session times out); whichever occurs first
     */
    public IGCRestClient(String baseURL, String authorization) {

        this.baseURL = baseURL;
        this.authorization = authorization;
        this.mapper = new ObjectMapper();
        this.mapper.enableDefaultTyping();

        // Run a simple initial query to obtain a session and setup the cookies
        if (this.baseURL != null && this.authorization != null) {
            IGCSearch igcSearch = new IGCSearch("category");
            igcSearch.addType("term");
            igcSearch.addType("information_governance_policy");
            igcSearch.addType("information_governance_rule");
            igcSearch.setPageSize(1);
            igcSearch.setDevGlossary(true);
            JsonNode response = searchJson(igcSearch);
            this.workflowEnabled = response.path("paging").path("numTotal").asInt(0) > 0;
        }

        // Register the non-generated types
        this.registerPOJO(new NamedType(Paging.class, "paging"));
        this.registerPOJO(new NamedType(Label.class, "label"));

    }

    private HttpHeaders getHttpHeaders() {

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CACHE_CONTROL, "no-cache");
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json");

        // If we have cookies already, re-use these (to maintain the same session)
        if (cookies != null) {
            headers.addAll(HttpHeaders.COOKIE, cookies);
        } else { // otherwise re-authenticate by Basic authentication
            String auth = "Basic " + this.authorization;
            headers.add(HttpHeaders.AUTHORIZATION, auth);
        }

        return headers;

    }

    // TODO: would be good to find a way to identify when session times out and automatically re-authenticate
    private void setCookiesFromResponse(ResponseEntity<String> response) {
        if (response.getStatusCode() == HttpStatus.OK) {
            HttpHeaders headers = response.getHeaders();
            if (headers.get(HttpHeaders.SET_COOKIE) != null) {
                this.cookies = headers.get(HttpHeaders.SET_COOKIE);
            }
        }
    }

    /**
     * General pattern for making requests
     *
     * @param endpoint - the URL against which to make the request
     * @param method - HttpMethod (GET, POST, etc)
     * @param payload - if POSTing some content, the JSON structure providing what should be POSTed
     * @return JsonNode JSON structure of the response
     */
    protected JsonNode _makeRequest(String endpoint, HttpMethod method, JsonNode payload) {
        HttpEntity<String> toSend = new HttpEntity<>(getHttpHeaders());
        if (payload != null) {
            toSend = new HttpEntity<>(payload.toString(), getHttpHeaders());
        }
        ResponseEntity<String> response = new RestTemplate().exchange(
                endpoint,
                method,
                toSend,
                String.class);
        setCookiesFromResponse(response);
        JsonNode jsonNode = null;
        if (response.hasBody()) {
            try {
                jsonNode = mapper.readTree(response.getBody());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return jsonNode;
    }

    /**
     * Attempt to convert a JSON structure into a Java object, based on the registered POJOs
     *
     * @param jsonNode - the JSON structure to convert
     * @return Reference - an IGC object
     */
    protected Reference readJSONIntoPOJO(JsonNode jsonNode) {
        Reference reference = null;
        try {
            reference = this.mapper.readValue(jsonNode.toString(), Reference.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return reference;
    }

    /**
     * Utility function to easily encode a username and password to send through as authorization info
     *
     * @param username
     * @param password
     * @return String of appropriately-encoded credentials for authorization
     */
    public static String encodeBasicAuth(String username, String password) {
        return Base64Utils.encodeToString((username + ":" + password).getBytes(UTF_8));
    }

    /**
     * Retrieves the list of metadata types supported by IGC
     *
     * @return JsonNode the list of types supported by IGC, as a JSON structure
     */
    public JsonNode getTypes() {
        return _makeRequest(baseURL + TYPES, HttpMethod.GET, null);
    }

    /**
     * Retrieve all information about an asset from IGC.
     *
     * @param rid - the Repository ID of the asset
     * @return JsonNode - the JSON response of the retrieval
     */
    public JsonNode getJsonAssetById(String rid) {
        return _makeRequest(baseURL + ASSET + "/" + rid, HttpMethod.GET, null);
    }

    /**
     * Retrieve all information about an asset from IGC.
     *
     * @param rid - the Repository ID of the asset
     * @return Reference - the IGC object representing the asset
     */
    public Reference getAssetById(String rid) {
        return readJSONIntoPOJO(getJsonAssetById(rid));
    }

    /**
     * Retrieve all assets that match the provided search criteria from IGC.
     *
     * @param query - the JSON query by which to search
     * @return JsonNode - the first JSON page of results from the search
     */
    public JsonNode searchJson(JsonNode query) {
        return _makeRequest(baseURL + SEARCH, HttpMethod.POST, query);
    }

    /**
     * Retrieve all assets that match the provided search criteria from IGC.
     *
     * @param igcSearch - the IGCSearch object defining criteria by which to search
     * @return JsonNode - the first JSON page of results from the search
     */
    public JsonNode searchJson(IGCSearch igcSearch) { return searchJson(igcSearch.getQuery()); }

    /**
     * Retrieve all assets that match the provided search criteria from IGC.
     *
     * @param igcSearch
     * @return ReferenceList - the first page of results from the search
     */
    public ReferenceList search(IGCSearch igcSearch) {
        ReferenceList referenceList = null;
        JsonNode results = searchJson(igcSearch);
        try {
            referenceList = this.mapper.readValue(results.toString(), ReferenceList.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return referenceList;
    }

    /**
     * Update the asset specified by the provided RID with the value(s) provided.
     *
     * @param rid - the Repository ID of the asset to update
     * @param value - the JSON structure defining what value(s) of the asset to update (and mode)
     * @return JsonNode - the JSON structure indicating the updated asset's RID and updates made
     */
    public JsonNode updateJson(String rid, JsonNode value) {
        return _makeRequest(baseURL + ASSET, HttpMethod.PUT, value);
    }

    /**
     * Retrieve the next page of results from a set of paging details
     * ... or if there is no next page, return an empty JSON Items set
     *
     * @param paging - the "paging" portion of the JSON response from which to retrieve the next page
     * @return JsonNode - the JSON response of the next page of results
     */
    public JsonNode getNextPage(JsonNode paging) {
        JsonNode nextPage = null;
        try {
            nextPage = mapper.readTree("{\"items\": []}");
            JsonNode nextURL = paging.path("next");
            if (!nextURL.isMissingNode()) {
                String sNextURL = nextURL.asText();
                if (sNextURL != "null") {
                    if (this.workflowEnabled && !sNextURL.contains("workflowMode=draft")) {
                        sNextURL += "&workflowMode=draft";
                    }
                    nextPage = _makeRequest(sNextURL, HttpMethod.GET, null);
                    // If the page is part of an ASSET retrieval, we need to strip off the attribute
                    // name of the relationship for proper multi-page composition
                    if (sNextURL.contains(ASSET)) {
                        String remainder = sNextURL.substring((baseURL + ASSET).length() + 2);
                        String attributeName = remainder.substring(remainder.indexOf("/") + 1, remainder.indexOf("?"));
                        nextPage = nextPage.path(attributeName);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return nextPage;
    }

    /**
     * Retrieve all pages of results from a set of paging details and items
     * ... or if there is no next page, return the items provided
     *
     * @param items - the "items" array of the JSON response for which to retrieve all pages
     * @param paging - the "paging" portion of the JSON response for which to retrieve all pages
     * @return JsonNode - the JSON containing all pages of results as an "items" array
     */
    public ArrayNode getAllPages(ArrayNode items, JsonNode paging) {
        ArrayNode allPages = items;
        JsonNode results = getNextPage(paging);
        ArrayNode resultsItems = (ArrayNode) results.path("items");
        if (resultsItems.size() > 0) {
            allPages = getAllPages(items.addAll(resultsItems), results.path("paging"));
        }
        return allPages;
    }

    /**
     * Retrieve the next page of results from a set of Paging details
     * ... or if there is no next page, return an empty JSON Items set
     *
     * @param paging - the "Paging" object from which to retrieve the next page
     * @return ReferenceList - the ReferenceList containing the next page of results
     */
    public ReferenceList getNextPage(Paging paging) {
        JsonNode nextPage = getNextPage(mapper.convertValue(paging, JsonNode.class));
        ReferenceList rlNextPage = null;
        try {
            rlNextPage = this.mapper.readValue(nextPage.toString(), ReferenceList.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rlNextPage;
    }

    /**
     * Retrieve all pages of results from a set of Paging details and items
     * ... or if there is no next page, return the items provided
     *
     * @param items - the ArrayList of items for which to retrieve all pages
     * @param paging - the Paging object for which to retrieve all pages
     * @return ArrayList - an ArrayList containing all items from all pages of results
     */
    public ArrayList<Reference> getAllPages(ArrayList<Reference> items, Paging paging) {
        ArrayList<Reference> allPages = items;
        ReferenceList results = getNextPage(paging);
        ArrayList<Reference> resultsItems = results.items;
        if (resultsItems.size() > 0) {
            // NOTE: this ordering of addAll is important, to avoid side-effecting the original set of items
            resultsItems.addAll(allPages);
            allPages = getAllPages(resultsItems, results.paging);
        }
        return allPages;
    }

    /**
     * Disconnect from IGC REST API and invalidate the session
     */
    public void disconnect() {
        _makeRequest(baseURL + LOGOUT, HttpMethod.GET, null);
    }

    /**
     * Disables SSL verification, to allow self-signed certificates
     */
    public static void disableSslVerification() {
        try {
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() { return null; }
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                    }
            };
            // Install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = (hostname, session) -> true;
            // Install the all-trusting host verifier
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }
    }

    /**
     * Register a POJO as an object to handle serde of JSON objects.
     * Note that this MUST be done BEFORE any object mapping (translation) is done!
     *
     * In general, you'll want your POJO to extend at least the model.Reference
     * object in this package; more likely the model.MainObject (for your own OpenIGC object),
     * or if you are adding custom attributes to one of the native asset types, consider
     * directly extending that asset from model.generated.*
     *
     * @param namedType - the pairing of Java class name and JSON '_type' name
     */
    public void registerPOJO(NamedType namedType) {
        this.mapper.registerSubtypes(namedType);
    }

    /**
     * Returns true iff the workflow is enabled in the environment against which the REST connection is defined
     *
     * @return Boolean
     */
    public Boolean isWorkflowEnabled() {
        return this.workflowEnabled;
    }

}
