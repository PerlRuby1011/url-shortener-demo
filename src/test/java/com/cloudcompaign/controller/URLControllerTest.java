package com.cloudcompaign.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.cloudcompaign.dto.URLInfo;
import com.cloudcompaign.repository.URLShortenerDao;

import static org.hamcrest.Matchers.*;

@RunWith(MockitoJUnitRunner.class)
public class URLControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private URLController urlController;

    @Mock
    private URLShortenerDao urlShortenerDao;
    
    @Before
    public void setup() {
    	ReflectionTestUtils.setField(URLController.class, "shortUrlCache", getUrlInfoMap());
    	mockMvc = MockMvcBuilders.standaloneSetup(urlController).build();
    }

    @Test
    public void testCreateShortURL() throws Exception {
    	Mockito.when(urlShortenerDao.createShortenedURL("http://www.longUrl.com")).thenReturn(buildUrlInfo());
    	JSONObject json = new JSONObject();
    	json.put("url", "http://www.longUrl.com");
    	mockMvc.perform(MockMvcRequestBuilders
    			.post("/shorturl")
    			.contentType(MediaType.APPLICATION_JSON)
    			.accept(MediaType.APPLICATION_JSON)
    			.content(json.toString().getBytes("UTF-8")))
    			.andExpect(status().isCreated())
    			.andExpect(jsonPath("url", is("http://www.longUrl.com")))
    			.andExpect(jsonPath("shortUrl", is("shortId")));
    			
    } 
    
    @Test
    public void testFetchURLDetails() throws Exception {
    	mockMvc.perform(MockMvcRequestBuilders
    			.get("/shorturl/shortId")
    			.contentType(MediaType.APPLICATION_JSON))
    			.andExpect(status().isOk())
    			.andExpect(jsonPath("url", is("http://www.longUrl.com")));
    			
    } 
    
    @Test
    public void testFetchUrlNotFound() throws Exception {
    	mockMvc.perform(MockMvcRequestBuilders
    			.get("/shorturl/short_id_not_created")
    			.contentType(MediaType.APPLICATION_JSON))
    			.andExpect(status().isNotFound());
    }
    
    @Test
    public void testUpdateUrl() throws UnsupportedEncodingException, Exception {
    	URLInfo urlInfo = buildUrlInfo();
    	urlInfo.setUrl("http://www.UpdatedlongUrl.com");
    	Mockito.when(urlShortenerDao.updateShortenedURL("shortId", "http://www.UpdatedlongUrl.com")).thenReturn(urlInfo);
    	JSONObject json = new JSONObject();
    	json.put("url", "http://www.UpdatedlongUrl.com");
    	mockMvc.perform(MockMvcRequestBuilders
    			.put("/shorturl/shortId")
    			.contentType(MediaType.APPLICATION_JSON)
    			.accept(MediaType.APPLICATION_JSON)
    			.content(json.toString().getBytes("UTF-8")))
    			.andExpect(status().isOk())
    			.andExpect(jsonPath("url", is("http://www.UpdatedlongUrl.com")))
    			.andExpect(jsonPath("shortUrl", is("shortId")));    	
    }
    
    @Test
    public void testUpdateUrlForFailure() throws UnsupportedEncodingException, Exception {
    	JSONObject json = new JSONObject();
    	json.put("url", "http://www.UpdatedlongUrl.com");
    	mockMvc.perform(MockMvcRequestBuilders
    			.put("/shorturl/shortId_does_not_exist")
    			.contentType(MediaType.APPLICATION_JSON)
    			.accept(MediaType.APPLICATION_JSON)
    			.content(json.toString().getBytes("UTF-8")))
    			.andExpect(status().isNotFound());
    }
    
    private HashMap<String, URLInfo> getUrlInfoMap() {
    	HashMap<String, URLInfo> shortUrlCache = new HashMap<String, URLInfo>();
    	shortUrlCache.put("shortId", buildUrlInfo());
    	return shortUrlCache;
    }
    
    private URLInfo buildUrlInfo() {
    	URLInfo urlInfo = new URLInfo();
    	urlInfo.setShortUrl("shortId");
    	urlInfo.setUrl("http://www.longUrl.com");
    	return urlInfo;
    }
}
