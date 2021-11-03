package com.cloudcompaign.repository;

import com.cloudcompaign.dto.URLInfo;

/**
 * Class defines operations allowed on a short id
 * @author Muthu
 *
 */
public interface URLShortenerDao {
	public URLInfo createShortenedURL(String actualUrl);
	public URLInfo updateShortenedURL(String shortUrl, String newUrl);
}
