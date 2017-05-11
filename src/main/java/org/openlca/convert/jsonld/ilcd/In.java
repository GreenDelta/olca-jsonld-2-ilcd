package org.openlca.convert.jsonld.ilcd;

import java.util.GregorianCalendar;
import java.util.function.Function;

import javax.xml.datatype.DatatypeConfigurationException;
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
		return elem == null ? null : elem.getAsString();
	}

	static Integer getInt(JsonObject obj, String property) {
		JsonPrimitive elem = getPrimitive(obj, property);
		return elem == null ? null : elem.getAsInt();
	}

	static double getDouble(JsonObject obj, String property, double defaultValue) {
		JsonPrimitive elem = getPrimitive(obj, property);
		return elem == null ? defaultValue : elem.getAsDouble();
	}

	static boolean getBool(JsonObject obj, String property) {
		JsonPrimitive elem = getPrimitive(obj, property);
		return elem == null ? false : elem.getAsBoolean();
	}

	static JsonObject getRef(JsonObject obj, String property, JsonStore store) {
		JsonObject ref = getObject(obj, property);
		if (ref == null)
			return null;
		String type = getString(ref, "@type");
		String id = getString(ref, "@id");
		return store.get(type, id);
	}

	public static void main(String[] args) throws DatatypeConfigurationException {
		GregorianCalendar cal = new GregorianCalendar();
		System.out.println(DatatypeFactory.newInstance().newXMLGregorianCalendar(cal));
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
