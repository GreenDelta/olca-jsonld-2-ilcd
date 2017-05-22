package org.openlca.convert.jsonld.ilcd;

public class Config {

	final String baseUri;
	final String lang;
	final JsonStore store;
	final RefCallback refCallback;

	public Config(JsonStore store, RefCallback refCallback) {
		this(null, null, store, refCallback);
	}

	public Config(String baseUri, String lang, JsonStore store, RefCallback refCallback) {
		this.lang = lang != null ? lang : "en";
		if (baseUri == null) {
			baseUri = "http://openlca.org/ilcd/resource/";
		} else if (!baseUri.endsWith("/")) {
			baseUri += "/";
		}
		this.baseUri = baseUri;
		this.store = store;
		this.refCallback = refCallback;
	}

}
