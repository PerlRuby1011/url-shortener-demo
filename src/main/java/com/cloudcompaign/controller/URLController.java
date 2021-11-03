package com.cloudcompaign.controller;

import java.net.URI;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.cloudcompaign.repository.URLShortenerDao;
import com.cloudcompaign.dto.URLInfo;
import com.cloudcompaign.validator.URLValidator;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

/**
 * Controller hosting various Rest end points to create, update, fetch and
 * redirect user to long url via short id.
 * 
 * @author Muthu
 *
 */
@RestController
@RequestMapping(value = "/shorturl")
public class URLController {

	private static final Logger LOGGER = LoggerFactory.getLogger(URLController.class);

	private static final String SHORT_URL_NOT_FOUND = "Short URL not found";

	private static final String UNAUTHORIZED_MSG = "Unauthorized to execute this operation";

	private static final String SHORT_URL_CREATED = "Short url is created";

	private static final String SHORT_URL_EXPIRED = "URL Expired";

	private static final String LONG_URL_UPDATED = "URL updated";

	private static final String INVALID_INPUT_URL = "Invalid input url";

	private static final String REDIRECT_MESSAGE = "Redirected to the original url";

	// this map captures all new short urls created/updated and acts as a cache.
	// Replace this with Redis/Memcached implementation.
	private static HashMap<String, URLInfo> shortUrlCache = new HashMap<String, URLInfo>();

	@Autowired
	private URLShortenerDao urlShortnerDao;

	@Operation(summary = "Create a short url")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = SHORT_URL_CREATED, content = {
					@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = URLInfo.class)) }),
			@ApiResponse(responseCode = "406", description = INVALID_INPUT_URL, content = @Content) })
	@PostMapping(consumes = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<URLInfo> shortenUrl(@RequestBody URLInfo shortenRequest, HttpServletRequest request)
			throws Exception {

		String longUrl = shortenRequest.getUrl();
		LOGGER.info("Received url to shorten: {}", longUrl);

		if (!URLValidator.isAuthenticated() && !URLValidator.isAuthorized()) {
			LOGGER.error("Not authenticated to create short id");
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, UNAUTHORIZED_MSG);
		}

		if (!URLValidator.validate(longUrl)) {
			throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, INVALID_INPUT_URL);
		}

		URLInfo shortenedUrl = urlShortnerDao.createShortenedURL(longUrl);

		LOGGER.info("Shortened url to {}", shortenedUrl);

		updateCache(shortenedUrl);

		return ResponseEntity.status(HttpStatus.CREATED).body(shortenedUrl);
	}

	@Operation(summary = "Update short url with new long url")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = LONG_URL_UPDATED, content = {
					@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = URLInfo.class)) }),
			@ApiResponse(responseCode = "404", description = SHORT_URL_NOT_FOUND, content = @Content) })
	@PutMapping(path = "/{shortUrl}", consumes = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<URLInfo> updateUrl(@PathVariable("shortUrl") String shortUrl,
			@RequestBody URLInfo shortenRequest, HttpServletRequest request) throws Exception {

		LOGGER.info("Received url to update: {} for short url {}", shortenRequest.getUrl(), shortUrl);

		String longUrl = shortenRequest.getUrl();

		if (!URLValidator.isAuthenticated() && !URLValidator.isAuthorized()) {
			LOGGER.error("Not authenticated to fetch the url: {}", shortUrl);
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, UNAUTHORIZED_MSG);
		}
		if (!URLValidator.validate(longUrl)) {
			LOGGER.error("Long url not found for: {}", shortUrl);
			throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, INVALID_INPUT_URL);
		}

		if (!shortUrlCache.containsKey(shortUrl)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, SHORT_URL_NOT_FOUND);
		}

		URLInfo shortenedUrl = urlShortnerDao.updateShortenedURL(shortUrl, longUrl);
		LOGGER.info("URL updated for: {}", shortenedUrl.getShortUrl());
		updateCache(shortenedUrl);
		return ResponseEntity.ok(shortenedUrl);

	}

	@Operation(summary = "Get details for the short url provided")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Found the short url", content = {
					@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = URLInfo.class)) }),
			@ApiResponse(responseCode = "404", description = SHORT_URL_NOT_FOUND, content = @Content) })
	@GetMapping(path = "/{shortUrl}")
	public ResponseEntity<URLInfo> fetchShortURLDetails(@PathVariable("shortUrl") String shortUrl) {
		if (!shortUrlCache.containsKey(shortUrl)) {
			LOGGER.error("Long url not found for: {}", shortUrl);
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, SHORT_URL_NOT_FOUND);
		}
		if (!URLValidator.isAuthenticated() && !URLValidator.isAuthorized()) {
			LOGGER.error("Not authenticated to fetch the url: {}", shortUrl);
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, UNAUTHORIZED_MSG);
		}
		if (URLValidator.isURLExpired()) {
			LOGGER.error("URL Expired: {}", shortUrl);
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, SHORT_URL_EXPIRED);
		}
		LOGGER.info("Fetch URL Info for: {}", shortUrl);
		return ResponseEntity.ok(shortUrlCache.get(shortUrl));
	}

	@Operation(summary = "Redirect short url to the original url")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "404", description = SHORT_URL_NOT_FOUND, content = {
					@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = URLInfo.class)) }),
			@ApiResponse(responseCode = "301", description = REDIRECT_MESSAGE, content = @Content) })
	@PostMapping(path = "/{shortUrl}")
	public ResponseEntity<URLInfo> redirect(@PathVariable("shortUrl") String shortUrl) {
		if (!shortUrlCache.containsKey(shortUrl)) {
			LOGGER.error("Long url not found for: {}", shortUrl);
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, SHORT_URL_NOT_FOUND);
		}
		if (!URLValidator.isAuthenticated() && !URLValidator.isAuthorized()) {
			LOGGER.error("Not authenticated to fetch the url: {}", shortUrl);
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, UNAUTHORIZED_MSG);
		}
		if (URLValidator.isURLExpired()) {
			LOGGER.error("URL Expired: {}", shortUrl);
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, SHORT_URL_EXPIRED);
		}
		URLInfo urlInfo = shortUrlCache.get(shortUrl);
		HttpHeaders headers = new HttpHeaders();
		headers.setLocation(URI.create(urlInfo.getUrl()));
		LOGGER.info("Redirecting user to: {}", urlInfo.getUrl());
		return new ResponseEntity<>(headers, HttpStatus.MOVED_PERMANENTLY);
	}

	private void updateCache(URLInfo urlInfo) {
		shortUrlCache.put(urlInfo.getShortUrl(), urlInfo);
	}

}
