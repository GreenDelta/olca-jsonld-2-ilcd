package org.openlca.convert.jsonld.ilcd;

import java.util.List;

import com.google.gson.JsonObject;

public interface JsonStore {

	JsonObject get(String type, String refId);

	List<JsonObject> getAll(String type);

}
