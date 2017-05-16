package org.openlca.convert.jsonld.ilcd;

import org.openlca.ilcd.commons.CommissionerAndGoal;
import org.openlca.ilcd.processes.AdminInfo;
import org.openlca.ilcd.processes.DataEntry;
import org.openlca.ilcd.processes.DataGenerator;
import org.openlca.ilcd.processes.Publication;
import org.openlca.ilcd.util.Refs;

import com.google.gson.JsonObject;

class ProcessAdminInfo {

	private final Util util;

	ProcessAdminInfo(Util util) {
		this.util = util;
	}

	AdminInfo create(JsonObject obj) {
		AdminInfo info = new AdminInfo();
		JsonObject doc = In.getObject(obj, "processDocumentation");
		info.publication = createPublication(obj, doc);
		info.dataEntry = createDataEntry(obj, doc);
		info.dataGenerator = createDataGenerator(doc);
		info.commissionerAndGoal = createCommissionerAndGoal(doc);
		return info;
	}

	private Publication createPublication(JsonObject obj, JsonObject doc) {
		Publication pub = new Publication();
		pub.version = In.getString(obj, "version");
		String uriPart = util.getUriPart(util.getType(obj));
		pub.uri = util.config.baseUri + uriPart + "/" + In.getString(obj, "@id");
		pub.lastRevision = In.getTimestamp(obj, "lastChange");
		if (doc == null)
			return pub;
		pub.copyright = In.getBool(doc, "copyright");
		JsonObject dsOwner = In.getObject(doc, "dataSetOwner");
		pub.owner = util.createRef(dsOwner);
		util.setLangString(pub.accessRestrictions, In.getString(doc, "restrictionsDescription"));
		JsonObject source = In.getObject(doc, "publication");
		pub.republication = util.createRef(source);
		return pub;
	}

	private DataGenerator createDataGenerator(JsonObject doc) {
		if (doc == null)
			return null;
		JsonObject dataGenerator = In.getObject(doc, "dataGenerator");
		if (dataGenerator == null)
			return null;
		DataGenerator generator = new DataGenerator();
		generator.contacts.add(util.createRef(dataGenerator));
		return generator;
	}

	private CommissionerAndGoal createCommissionerAndGoal(JsonObject doc) {
		String application = In.getString(doc, "intendedApplication");
		String project = In.getString(doc, "projectDescription");
		if (application == null || project == null)
			return null;
		CommissionerAndGoal comAndGoal = new CommissionerAndGoal();
		util.setLangString(comAndGoal.intendedApplications, application);
		util.setLangString(comAndGoal.project, project);
		return comAndGoal;
	}

	private DataEntry createDataEntry(JsonObject obj, JsonObject doc) {
		DataEntry entry = new DataEntry();
		entry.timeStamp = In.getTimestamp(obj, "lastChange");
		entry.formats.add(Refs.ilcd());
		if (doc == null)
			return entry;
		JsonObject documentor = In.getObject(doc, "dataDocumentor");
		entry.documentor = util.createRef(documentor);
		return entry;
	}

}
