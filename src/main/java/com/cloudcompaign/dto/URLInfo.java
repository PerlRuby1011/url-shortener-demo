package com.cloudcompaign.dto;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * DTO to pass URL information across different layers
 * @author Muthu
 *
 */
public class URLInfo {
	private String shortUrl;
	
	private String url;
	
	private LocalDateTime createDate;
	
	private LocalDateTime updatedDate;

	public String getShortUrl() {
		return shortUrl;
	}

	public void setShortUrl(String url) {
		this.shortUrl = url;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public LocalDateTime getUpdatedDate() {
		return updatedDate;
	}

	public void setUpdatedDate(LocalDateTime updatedDate) {
		this.updatedDate = updatedDate;
	}

	public LocalDateTime getCreateDate() {
		return createDate;
	}

	public void setCreateDate(LocalDateTime localDateTime) {
		this.createDate = localDateTime;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof URLInfo))
			return false;
		URLInfo urlInfo = (URLInfo) o;
		return Objects.equals(this.shortUrl, urlInfo.shortUrl);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.url, this.shortUrl, this.createDate, this.updatedDate);
	}

	@Override
	public String toString() {
		return "UrlInfo{" + "longUrl=" + this.url + ", shortUrl='" + this.shortUrl + '\'' + ", created='"
				+ this.createDate + '\'' + '}';
	}
}
