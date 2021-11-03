package com.cloudcompaign.util;

import java.util.Random;

import org.springframework.stereotype.Component;

/**
 * Class contains the logic to create a unique key which will be used to map to a long url.
 * 
 * @author Muthu
 *
 */
@Component
public  class URLShortenerHelper {
	private static Random randomNumGenerator = new Random();
	
	private static char chars[] = new char[62];
	
	static {
		for (int i = 0; i < 62; i++) {
			int j = 0;
			if (i < 10) {
				j = i + 48;
			} else if (i > 9 && i <= 35) {
				j = i + 55;
			} else {
				j = i + 61;
			}
			chars[i] = (char) j;
		}
	}

	public static String generateKey(int keyLength) {
		String key = "";
		for (int i = 0; i < keyLength; i++) {
			key += chars[randomNumGenerator.nextInt(62)];
		}
		return key;
	}
}
