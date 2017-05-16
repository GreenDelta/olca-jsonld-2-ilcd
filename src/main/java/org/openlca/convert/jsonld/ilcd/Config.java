package org.openlca.convert.jsonld.ilcd;

public class Config {

	final String baseUri;
	final String lang;
	final JsonStore store;

	public Config(JsonStore store) {
		this(null, null, store);
	}

	public Config(String baseUri, String lang, JsonStore store) {
		this.lang = lang != null ? lang : "en";
		if (baseUri == null) {
			baseUri = "http://openlca.org/ilcd/resource/";
		} else if (!baseUri.endsWith("/")) {
			baseUri += "/";
		}
		this.baseUri = baseUri;
		this.store = store;
	}

}
