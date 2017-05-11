package org.openlca.convert.jsonld.ilcd;

import java.io.File;
import java.util.List;

import org.openlca.ilcd.commons.Classification;
import org.openlca.ilcd.commons.DataEntry;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Publication;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.sources.FileRef;
import org.openlca.ilcd.util.Refs;

import com.google.gson.JsonObject;

public class Util {

	public final String baseUri;
	public final String lang;
	public final JsonStore store;

	public Util(String baseUri, String lang, JsonStore store) {
		this.lang = lang != null ? lang : "en";
		if (baseUri == null) {
			baseUri = "http://openlca.org/ilcd/resource/";
		} else if (!baseUri.endsWith("/")) {
			baseUri += "/";
		}
		this.baseUri = baseUri;
		this.store = store;
	}

	Publication createPublication(JsonObject obj) {
		Publication pub = new Publication();
		pub.version = In.getString(obj, "version");
		String uriPart = getUriPart(getType(obj));
		pub.uri = baseUri + uriPart + "/" + In.getString(obj, "@id");
		return pub;
	}

	DataEntry createDataEntry(JsonObject obj) {
		DataEntry entry = new DataEntry();
		entry.timeStamp = In.getTimestamp(obj, "lastChange");
		entry.formats.add(Refs.ilcd());
		return entry;
	}

	Classification createClassification(JsonObject obj) {
		CategoryConverter converter = new CategoryConverter(this);
		JsonObject category = In.getRef(obj, "category", store);
		return converter.run(category);
	}

	FileRef createFileRef(File extFile) {
		FileRef fileRef = new FileRef();
		fileRef.uri = "../external_docs/" + extFile.getName();
		return fileRef;
	}

	Ref createRef(JsonObject obj) {
		Ref ref = new Ref();
		if (obj == null)
			return ref;
		ref.version = "01.00.000";
		ref.uuid = In.getString(obj, "@id");
		ref.type = getType(obj);
		ref.uri = "../" + getUriPart(ref.type) + "/" + In.getString(obj, "@id");
		setLangString(ref.name, In.getString(obj, "name"));
		return ref;
	}

	private DataSetType getType(JsonObject obj) {
		String type = In.getString(obj, "@type");
		switch (type) {
		case "Actor":
			return DataSetType.CONTACT;
		case "Source":
			return DataSetType.SOURCE;
		case "UnitGroup":
			return DataSetType.UNIT_GROUP;
		case "FlowProperty":
			return DataSetType.FLOW_PROPERTY;
		case "Flow":
			return DataSetType.FLOW;
		case "Process":
			return DataSetType.PROCESS;
		default:
			throw new IllegalArgumentException("No uri part specified for " + type);
		}
	}

	private String getUriPart(DataSetType type) {
		switch (type) {
		case CONTACT:
			return "contacts";
		case SOURCE:
			return "sources";
		case UNIT_GROUP:
			return "unitgroups";
		case FLOW_PROPERTY:
			return "flowproperties";
		case FLOW:
			return "flows";
		case PROCESS:
			return "processes";
		default:
			throw new IllegalArgumentException("No uri part specified for " + type.name());
		}
	}

	void setLangString(List<LangString> list, String value) {
		if (value == null || value.isEmpty())
			return;
		LangString.set(list, value, lang);
	}

}
