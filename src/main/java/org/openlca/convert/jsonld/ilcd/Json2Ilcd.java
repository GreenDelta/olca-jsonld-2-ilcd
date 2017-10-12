package org.openlca.convert.jsonld.ilcd;

import java.util.ArrayList;
import java.util.List;

import org.openlca.ilcd.contacts.Contact;
import org.openlca.ilcd.flowproperties.FlowProperty;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.methods.LCIAMethod;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.sources.Source;
import org.openlca.ilcd.units.UnitGroup;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Json2Ilcd {

	private final Util util;

	public Json2Ilcd(Config config) {
		this.util = new Util(config);
	}

	public Contact convertActor(JsonObject obj) {
		checkType(obj, "Actor");
		return new ActorConverter(util).run(obj);
	}

	public Source convertSource(JsonObject obj) {
		checkType(obj, "Source");
		return new SourceConverter(util).run(obj);
	}

	public UnitGroup convertUnitGroup(JsonObject obj) {
		checkType(obj, "UnitGroup");
		return new UnitGroupConverter(util).run(obj);
	}

	public FlowProperty convertFlowProperty(JsonObject obj) {
		checkType(obj, "FlowProperty");
		return new FlowPropertyConverter(util).run(obj);
	}

	public Flow convertFlow(JsonObject obj) {
		checkType(obj, "Flow");
		return new FlowConverter(util).run(obj);
	}

	public Process convertProcess(JsonObject obj) {
		checkType(obj, "Process");
		return new ProcessConverter(util).run(obj);
	}
	
	public List<LCIAMethod> convertImpactMethod(JsonObject obj) {
		checkType(obj, "ImpactMethod");
		List<LCIAMethod> categories = new ArrayList<>();
		String refId = In.getString(obj, "@id");
		String name = In.getString(obj, "@name");
		JsonArray array = In.getArray(obj, "impactCategories");
		for (JsonElement elem : array) {
			String categoryRefId = In.getString(elem.getAsJsonObject(), "@id");
			JsonObject category = util.config.store.get("ImpactCategory", categoryRefId);
			categories.add(convertImpactCategory(category, refId, name));
		}
		return categories;
	}

	public LCIAMethod convertImpactCategory(JsonObject obj, String methodRefId, String methodName) {
		checkType(obj, "ImpactCategory");
		return new ImpactCategoryConverter(util, methodRefId, methodName).run(obj);
	}

	private void checkType(JsonObject obj, String expectedType) {
		if (!expectedType.equals(In.getString(obj, "@type")))
			throw new IllegalArgumentException("Object is not of type " + expectedType);
	}

}
