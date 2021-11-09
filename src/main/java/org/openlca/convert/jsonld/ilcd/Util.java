package org.openlca.convert.jsonld.ilcd;

import java.io.File;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.openlca.ilcd.commons.Classification;
import org.openlca.ilcd.commons.DataEntry;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Publication;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.sources.FileRef;
import org.openlca.ilcd.util.Refs;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

class Util {

	final Config config;

	Util(Config config) {
		this.config = config;
	}

	Publication createPublication(JsonObject obj) {
		Publication pub = new Publication();
		pub.version = In.getString(obj, "version");
		String uriPart = getUriPart(getType(obj));
		pub.uri = config.baseUri + uriPart + "/" + In.getString(obj, "@id");
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
		JsonObject category = In.getRef(obj, "category", config.store);
		return converter.run(category);
	}

	FileRef createFileRef(File extFile) {
		FileRef fileRef = new FileRef();
		fileRef.uri = "../external_docs/" + extFile.getName();
		return fileRef;
	}

	Ref createRef(JsonObject obj) {
		if (obj == null)
			return null;
		Ref ref = new Ref();
		ref.version = "01.00.000";
		ref.uuid = In.getString(obj, "@id");
		ref.type = getType(obj);
		ref.uri = "../" + getUriPart(ref.type) + "/" + In.getString(obj, "@id") + ".xml";
		setLangString(ref.name, In.getString(obj, "name"));
		if (config.refCallback != null) {
			config.refCallback.throwRef(In.getString(obj, "@type"), ref.uuid);
		}
		return ref;
	}

	DataSetType getType(JsonObject obj) {
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

	String getUriPart(DataSetType type) {
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
		LangString.set(list, value, config.lang);
	}

	void putAttribute(String name, Map<QName, String> map, String value) {
		if (name == null || value == null || map == null)
			return;
		QName qName = new QName("http://openlca.org/ilcd-extensions", name);
		map.put(qName, value);
	}

	double getPropertyFactor(JsonObject obj) {
		JsonObject flow = In.getObject(obj, "flow");
		JsonObject property = In.getObject(obj, "flowProperty");
		return getPropertyFactor(flow, property);
	}

	double getPropertyFactor(JsonObject flow, JsonObject property) {
		if (flow == null || property == null)
			return 1;
		String propertyId = In.getString(property, "@id");
		String flowId = In.getString(flow, "@id");
		flow = config.store.get("Flow", flowId);
		JsonArray factors = In.getArray(flow, "flowProperties");
		if (factors == null)
			return 1;
		JsonObject correctFactor = null;
		for (JsonElement elem : factors) {
			JsonObject factor = elem.getAsJsonObject();
			String propId = In.getString(In.getObject(factor, "flowProperty"), "@id");
			if (!propId.equals(propertyId))
				continue;
			correctFactor = factor;
			break;
		}
		return In.getDouble(correctFactor, "conversionFactor", 1);
	}

	double getUnitFactor(JsonObject obj) {
		JsonObject elem = In.getObject(obj, "unit");
		return In.getDouble(elem, "conversionFactor", 1);
	}

}
