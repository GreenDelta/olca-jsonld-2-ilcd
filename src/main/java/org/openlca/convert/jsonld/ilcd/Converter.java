package org.openlca.convert.jsonld.ilcd;

import com.google.gson.JsonObject;

interface Converter<T> {

	T run(JsonObject obj);
	
}
