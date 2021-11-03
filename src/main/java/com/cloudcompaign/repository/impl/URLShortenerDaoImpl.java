package com.cloudcompaign.repository.impl;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.cloudcompaign.repository.URLShortenerDao;
import com.cloudcompaign.dto.URLInfo;
import com.cloudcompaign.util.URLShortenerHelper;

/**
 * Implementation class for URLShortenerDao
 * 
 * @author Muthu
 *
 */
@Component
public class URLShortenerDaoImpl implements URLShortenerDao {

	private static final HashMap<String, URLInfo> shortUrlRepo = new HashMap<String, URLInfo>();

	@Value("${url.shorten.length}")
	private int keyLength;
	
	@Override
	public URLInfo createShortenedURL(String actualUrl) {
		URLInfo urlInfo = new URLInfo();
		String shortURL = URLShortenerHelper.generateKey(keyLength);
		if(!shortUrlRepo.containsKey(shortURL)) { //check if the short id has already been created (edge case) if so repeat the method call
			urlInfo.setCreateDate(java.time.LocalDateTime.now());
			urlInfo.setUpdatedDate(java.time.LocalDateTime.now());
			urlInfo.setUrl(actualUrl);
			urlInfo.setShortUrl(shortURL);
			shortUrlRepo.put(shortURL, urlInfo);
		} else {
			createShortenedURL(actualUrl);
		}
		return urlInfo;
	}

	@Override
	public URLInfo updateShortenedURL(String shortUrl, String newUrl) {
		if(shortUrlRepo.containsKey(shortUrl)) {
			URLInfo urlInfo = shortUrlRepo.get(shortUrl);
			urlInfo.setUrl(newUrl);
			urlInfo.setUpdatedDate(java.time.LocalDateTime.now());
			return urlInfo;
		}
		return null;
	}
	
}
