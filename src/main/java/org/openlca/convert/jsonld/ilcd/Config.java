package org.openlca.convert.jsonld.ilcd;

import org.openlca.ilcd.commons.DataSetType;

public class Config {

	final String lang;
	final JsonStore store;
	final RefCallback refCallback;
	private final UrlCreator urlCreator;

	public Config(JsonStore store, RefCallback refCallback) {
		this(store, refCallback, null);
	}

	public Config(JsonStore store, RefCallback refCallback, UrlCreator urlCreator) {
		this.lang = "en";
		this.store = store;
		this.refCallback = refCallback;
		this.urlCreator = urlCreator;
	}

	String createPublicationLink(DataSetType type, String refId) {
		if (urlCreator != null)
			return urlCreator.createPublicationLink(type, refId);
		return "http://openlca.org/ilcd/resource/" + new Util(this).getUriPart(type) + "/" + refId;
	}

	public interface UrlCreator {

		String createPublicationLink(DataSetType type, String refId);

	}

}
