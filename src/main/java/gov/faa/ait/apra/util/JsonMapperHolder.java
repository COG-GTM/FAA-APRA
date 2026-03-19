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

import java.text.SimpleDateFormat;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Thread-safe singleton holder for a pre-configured {@link ObjectMapper}.
 * Note: {@link SimpleDateFormat} is not thread-safe but Jackson's
 * {@link ObjectMapper} clones the date format internally for each
 * serialization/deserialization call, so this is safe.
 */
public final class JsonMapperHolder {

	public static final ObjectMapper MAPPER = new ObjectMapper()
		.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
		.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));

	private JsonMapperHolder() { }
}
