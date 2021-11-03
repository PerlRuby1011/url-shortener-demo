package com.cloudcompaign.validator;

import org.apache.commons.validator.routines.UrlValidator;

/**
 * Validator class to do basic validation on the incoming url, sanitize them and check expiration.
 * 
 * @author Muthu
 */
public class URLValidator {

	enum AllowedPrototypes {
		http,
		https;
	}

	private static UrlValidator httpValidator = new UrlValidator(
			new String[] { AllowedPrototypes.http.toString(), AllowedPrototypes.https.toString()});
	
	/**
	 * Validate the incoming url using the apache commons validator
	 * 
	 * @param url
	 * @return true/false
	 */
    public static boolean validate(String url) {
        return httpValidator.isValid(url);
    }
    
    //TODO: yet to be implemented
    public static boolean isURLExpired() {
    	// if the created or updated date/time is greater than expiration date, return TRUE
    	return false;
    }
    
    //TODO: yet to be implemented
    public static boolean isAuthenticated() {
    	return true;
    }
    
    //TODO: yet to be implemented
    public static boolean isAuthorized() {
    	return true;
    }

}
