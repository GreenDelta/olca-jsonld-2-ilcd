package org.openlca.convert.jsonld.ilcd;

import org.openlca.ilcd.commons.IDataSet;

import com.google.gson.JsonObject;

interface Converter<T extends IDataSet> {

	T run(JsonObject obj);

}
