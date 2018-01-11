
package org.rbfcu.projectview.controller;

import javax.naming.AuthenticationException;

import org.apache.log4j.Logger;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class RestServiceUtility {

	final static Logger logger = Logger.getLogger(RestServiceUtility.class);

	public static String invokeGetMethod(String auth, String url) throws AuthenticationException, ClientHandlerException {

		logger.debug("auth:-->" + auth);
		logger.debug("url:-->" + url);

		Client client = Client.create();
		WebResource webResource = client.resource(url);
		ClientResponse response =
				webResource.header("Authorization", "Basic " + auth).type("application/json").accept("application/json").get(ClientResponse.class);
		int statusCode = response.getStatus();
		if (statusCode == 401) {
			throw new AuthenticationException("Invalid Username or Password");
		}
		logger.debug("statusCode:-->" + statusCode);
		return response.getEntity(String.class);
	}
}
