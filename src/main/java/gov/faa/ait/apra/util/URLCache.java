/*
 * Federal Aviation Administration (FAA) public work 
 * 
 * As a work of the United States Government, this project is in the 
 * public domain within the United States. Additionally, we waive copyright 
 * and related rights in the work worldwide 
 * through the Creative Commons 0 (CC0) 1.0 Universal public domain dedication
 * 
 * APRA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 */
package gov.faa.ait.apra.util;

import java.time.Instant;
import java.util.GregorianCalendar;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class URLCache {
	private static final Logger logger = LoggerFactory.getLogger(URLCache.class);
	private static URLCache instance;
	private static final Set<String> cache = ConcurrentHashMap.newKeySet();
	private static final AtomicReference<Instant> lastFlush = new AtomicReference<>(Instant.now());
	
	private URLCache () {
		flush();
	}
	
	public static synchronized URLCache getInstance() {
		if(instance == null) {
			instance = new URLCache();
			URLCache.lastFlush.set(Instant.now());
		}
		
		if (instance.isUpdateRequired()) {
			URLCache.flush();
		}
		return instance;
	}
	
	public boolean contains (String url) {
		return cache.contains(url);
	}
	
	public static void addUrl (String url) {
		cache.add(url);
	}
	
	public static void flush () {		
		logger.info("URL cache is being flushed.");
		cache.clear();
		URLCache.lastFlush.set(Instant.now());
	}
	
	private boolean isUpdateRequired () {

		CycleDateUtil cdu = new CycleDateUtil();
		
		GregorianCalendar cycle = new GregorianCalendar(TimeZone.getDefault());
		GregorianCalendar lastRefresh = new GregorianCalendar(TimeZone.getDefault());
		
		lastRefresh.setTime(java.util.Date.from(URLCache.lastFlush.get()));
		cycle.setTime(cdu.getCurrentCycle());
		
		if (cycle.after(lastRefresh)) {
			logger.info("URL cache requires a refresh");
			return true;
		}
		
		return false;
	}
	
}
