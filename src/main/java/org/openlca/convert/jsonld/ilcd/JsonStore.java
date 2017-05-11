package org.openlca.convert.jsonld.ilcd;

import com.google.gson.JsonObject;

public interface JsonStore {

	JsonObject get(String type, String refId);
	
}
