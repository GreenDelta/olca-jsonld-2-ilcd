package org.openlca.convert.jsonld.ilcd;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.function.Function;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

final class In {

	private In() {
	}

	static String getString(JsonObject obj, String property) {
		JsonPrimitive elem = getPrimitive(obj, property);
		if (elem == null)
			return null;
		String value = elem.getAsString();
		return value == null || value.isEmpty() ? null : value;
	}

	static Integer getInt(JsonObject obj, String property) {
		JsonPrimitive elem = getPrimitive(obj, property);
		return elem == null ? null : elem.getAsInt();
	}

	static Long getLong(JsonObject obj, String property) {
		JsonPrimitive elem = getPrimitive(obj, property);
		return elem == null ? null : elem.getAsLong();
	}

	static Double getDouble(JsonObject obj, String property) {
		JsonPrimitive elem = getPrimitive(obj, property);
		return elem == null ? null : elem.getAsDouble();
	}

	static double getDouble(JsonObject obj, String property, double defaultValue) {
		JsonPrimitive elem = getPrimitive(obj, property);
		return elem == null ? defaultValue : elem.getAsDouble();
	}

	static boolean getBool(JsonObject obj, String property) {
		JsonPrimitive elem = getPrimitive(obj, property);
		return elem == null ? false : elem.getAsBoolean();
	}

	static Date getDate(JsonObject obj, String property) {
		String date = getString(obj, property);
		return parseDate(date);
	}
	
	private static Date parseDate(String str) {
		if (str == null || str.isEmpty())
			return null;
		try {
			if (str.length() < 18) {
				// try to parse date strings like "2015-05-23"
				if (str.length() > 10) {
					// in older versions may have a zone offset
					// like "2015-05-23+02:00"
					str = str.substring(0, 10);
				}
				LocalDate date = LocalDate.parse(str);
				long seconds = date.toEpochSecond(LocalTime.MIN, ZoneOffset.UTC);
				return Date.from(Instant.ofEpochSecond(seconds));
			}
			if (str.endsWith("Z")) {
				// assume UTC time input
				Instant instant = Instant.parse(str);
				return Date.from(instant);
			}
			// assume offset time
			OffsetDateTime offset = OffsetDateTime.parse(str);
			return Date.from(offset.toInstant());
		} catch (Exception e) {
			LoggerFactory.getLogger(In.class).error("failed to parse date / time: " + str, e);
			return null;
		}
	}

	static JsonObject getRef(JsonObject obj, String property, JsonStore store) {
		JsonObject ref = getObject(obj, property);
		if (ref == null)
			return null;
		String type = getString(ref, "@type");
		String id = getString(ref, "@id");
		return store.get(type, id);
	}

	static XMLGregorianCalendar getTimestamp(JsonObject obj, String property) {
		if (obj == null)
			return null;
		String value = getString(obj, property);
		if (value == null)
			return null;
		try {
			return DatatypeFactory.newInstance().newXMLGregorianCalendar(value);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(In.class);
			log.error("failed to create XML calendar for time " + value, e);
			return null;
		}
	}

	static JsonObject getObject(JsonObject obj, String property) {
		JsonElement elem = getElement(obj, property, (e) -> e.isJsonObject());
		return elem == null ? null : elem.getAsJsonObject();
	}

	static JsonArray getArray(JsonObject obj, String property) {
		JsonElement elem = getElement(obj, property, (e) -> e.isJsonArray());
		return elem == null ? null : elem.getAsJsonArray();
	}

	static JsonPrimitive getPrimitive(JsonObject obj, String property) {
		JsonElement elem = getElement(obj, property, (e) -> e.isJsonPrimitive());
		return elem == null ? null : elem.getAsJsonPrimitive();
	}

	static JsonElement getElement(JsonObject obj, String property, Function<JsonElement, Boolean> check) {
		if (obj == null || property == null)
			return null;
		JsonElement elem = obj.get(property);
		if (elem == null || !check.apply(elem))
			return null;
		return elem;
	}

}
