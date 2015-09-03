package webService;

import java.util.ArrayList;

import model.Business;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.scribe.builder.ServiceBuilder;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;

/**
 * Code sample for accessing the Yelp API V2.
 * 
 * This program demonstrates the capability of the Yelp API version 2.0 by using
 * the Search API to query for businesses by a search term and location, and the
 * Business API to query additional information about the top result from the
 * search query.
 * 
 * <p>
 * See <a href="http://www.yelp.com/developers/documentation">Yelp
 * Documentation</a> for more info.
 * 
 */
public class YelpAPI {

	private static final String API_HOST = "api.yelp.com";
	private static final String DEFAULT_TERM = "dinner";
	private static final String DEFAULT_LOCATION = "San Francisco, CA";
	private static final int SEARCH_LIMIT = 20;
	private static final String SEARCH_PATH = "/v2/search";
	private static final String BUSINESS_PATH = "/v2/business";

	/*
	 * Update OAuth credentials below from the Yelp Developers API site:
	 * http://www.yelp.com/developers/getting_started/api_access
	 */
	private static final String CONSUMER_KEY = "gvPAgBe9ZT6SIPyHF-KzEw";
	private static final String CONSUMER_SECRET = "I_PKY9036ZcaKmsFqBCVlYUyS5A";
	private static final String TOKEN = "0ZQPHxCNndSgAloBk6NV0bKWa0ew3Ihd";
	private static final String TOKEN_SECRET = "lNq8blW8Le7NxQ3QBD55fMvn1xU";

	private ArrayList<Business> list = new ArrayList<Business>();

	OAuthService service;
	Token accessToken;

	/**
	 * Setup the Yelp API OAuth credentials.
	 * 
	 * @param consumerKey
	 *            Consumer key
	 * @param consumerSecret
	 *            Consumer secret
	 * @param token
	 *            Token
	 * @param tokenSecret
	 *            Token secret
	 */
	public YelpAPI(String consumerKey, String consumerSecret, String token,
			String tokenSecret) {
		this.service = new ServiceBuilder().provider(TwoStepOAuth.class)
				.apiKey(consumerKey).apiSecret(consumerSecret).build();
		this.accessToken = new Token(token, tokenSecret);
	}

	/**
	 * Creates and sends a request to the Search API by term and location.
	 * <p>
	 * See <a
	 * href="http://www.yelp.com/developers/documentation/v2/search_api">Yelp
	 * Search API V2</a> for more info.
	 * 
	 * @param term
	 *            <tt>String</tt> of the search term to be queried
	 * @param location
	 *            <tt>String</tt> of the location
	 * @return <tt>String</tt> JSON Response
	 */
	public String searchForBusinessesByLocation(String term, String location) {
		OAuthRequest request = createOAuthRequest(SEARCH_PATH);
		request.addQuerystringParameter("term", term);
		request.addQuerystringParameter("location", location);
		request.addQuerystringParameter("limit", String.valueOf(SEARCH_LIMIT));
		return sendRequestAndGetResponse(request);
	}

	/**
	 * Creates and sends a request to the Business API by business ID.
	 * <p>
	 * See <a
	 * href="http://www.yelp.com/developers/documentation/v2/business">Yelp
	 * Business API V2</a> for more info.
	 * 
	 * @param businessID
	 *            <tt>String</tt> business ID of the requested business
	 * @return <tt>String</tt> JSON Response
	 */
	public String searchByBusinessId(String businessID) {
		OAuthRequest request = createOAuthRequest(BUSINESS_PATH + "/"
				+ businessID);
		return sendRequestAndGetResponse(request);
	}

	/**
	 * Creates and returns an {@link OAuthRequest} based on the API endpoint
	 * specified.
	 * 
	 * @param path
	 *            API endpoint to be queried
	 * @return <tt>OAuthRequest</tt>
	 */
	private OAuthRequest createOAuthRequest(String path) {
		OAuthRequest request = new OAuthRequest(Verb.GET, "http://" + API_HOST
				+ path);
		return request;
	}

	/**
	 * Sends an {@link OAuthRequest} and returns the {@link Response} body.
	 * 
	 * @param request
	 *            {@link OAuthRequest} corresponding to the API request
	 * @return <tt>String</tt> body of API response
	 */
	private String sendRequestAndGetResponse(OAuthRequest request) {
		//System.out.println("Querying " + request.getCompleteUrl() + " ...");
		this.service.signRequest(this.accessToken, request);
		Response response = request.send();
		return response.getBody();
	}

	/**
	 * Queries the Search API based on the command line arguments and takes the
	 * first result to query the Business API.
	 * 
	 * @param yelpApi
	 *            <tt>YelpAPI</tt> service instance
	 * @param yelpApiCli
	 *            <tt>YelpAPICLI</tt> command line arguments
	 */
	public JSONArray queryAPI(YelpAPI yelpApi, String term, String location) {
		String searchResponseJSON = yelpApi.searchForBusinessesByLocation(term,
				location);

		JSONParser parser = new JSONParser();
		JSONObject response = null;
		try {
			response = (JSONObject) parser.parse(searchResponseJSON);
		} catch (ParseException pe) {
			System.out.println("Error: could not parse JSON response:");
			System.out.println(searchResponseJSON);
			System.exit(1);
		}
		// System.out.println("response:"+response);
		JSONArray arr = parseToList(response);
		return arr;
	}

	public JSONArray parseToList(JSONObject response) {
		int total = ((Long)response.get("total")).intValue();
		//System.out.println("total:"+total);
		int max_num = Math.min(total, SEARCH_LIMIT);
		JSONArray businesses = (JSONArray) response.get("businesses");
		for (int i = 0; i < max_num; i++) {
			JSONObject bus = (JSONObject) businesses.get(i);
			String name = new String();
			String url = new String();
			String snippet_text = new String();
			String snippet_url = new String();
			JSONObject location = null;
			String display_address = new String();

			if (bus.get("name") != null)
				name = bus.get("name").toString();
			if (bus.get("url") != null)
				url = bus.get("url").toString();
			if (bus.get("snippet_text") != null)
				snippet_text = bus.get("snippet_text").toString();
			if (bus.get("snippet_image_url") != null)
				snippet_url = bus.get("snippet_image_url").toString();
			if (bus.get("location") != null)
				location = (JSONObject) bus.get("location");
			if (location.get("display_address") != null)
				display_address = location.get("display_address").toString();
			Business inst = new Business(name, url, snippet_text, snippet_url,
					display_address);
			list.add(inst);
		}
		JSONObject jObject = new JSONObject();
		JSONArray jArray = null;
		try {
			jArray = new JSONArray();
			for (Business val : list) {
				JSONObject valJSON = new JSONObject();
				valJSON.put("name", val.getName());
				valJSON.put("url", val.getUrl());
				valJSON.put("snippet_text", val.getSnippet_text());
				valJSON.put("snippet_url", val.getSnippet_url());
				valJSON.put("display_address", val.getDisplay_address());
				jArray.add(valJSON);
			}
			jObject.put("BusinessList", jArray);
			//System.out.println("jArray");
			//System.out.println(jArray);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		//System.out.println("jobject");
		//System.out.println(jObject);
		return jArray;
		//showList();
	}

	/**
	 * Command-line interface for the sample Yelp API runner.
	 */
	private static class YelpAPICLI {
		@Parameter(names = { "-q", "--term" }, description = "Search Query Term")
		public String term = DEFAULT_TERM;

		@Parameter(names = { "-l", "--location" }, description = "Location to be Queried")
		public String location = DEFAULT_LOCATION;
	}

	private void showList() {
		System.out.println("size:" + list.size());
		for (Business val : list) {
			System.out.println("name:" + val.getName());
			System.out.println("url:" + val.getUrl());
		}
	}

	/**
	 * Main entry for sample Yelp API requests.
	 * <p>
	 * After entering your OAuth credentials, execute <tt><b>run.sh</b></tt> to
	 * run this example.
	 */
	public static void main(String[] args) {
		YelpAPICLI yelpApiCli = new YelpAPICLI();
		new JCommander(yelpApiCli, args);

		YelpAPI yelpApi = new YelpAPI(CONSUMER_KEY, CONSUMER_SECRET, TOKEN,
				TOKEN_SECRET);
		yelpApi.queryAPI(yelpApi, DEFAULT_TERM, DEFAULT_LOCATION);
	}
}
