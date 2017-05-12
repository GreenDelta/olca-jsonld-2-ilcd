package org.openlca.convert.jsonld.ilcd;

import java.util.List;

import org.openlca.ilcd.commons.Category;
import org.openlca.ilcd.commons.Classification;
import org.openlca.ilcd.commons.FlowType;
import org.openlca.ilcd.flows.AdminInfo;
import org.openlca.ilcd.flows.Compartment;
import org.openlca.ilcd.flows.CompartmentList;
import org.openlca.ilcd.flows.DataEntry;
import org.openlca.ilcd.flows.DataSetInfo;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.flows.FlowCategoryInfo;
import org.openlca.ilcd.flows.FlowInfo;
import org.openlca.ilcd.flows.FlowName;
import org.openlca.ilcd.flows.FlowPropertyRef;
import org.openlca.ilcd.flows.Geography;
import org.openlca.ilcd.flows.LCIMethod;
import org.openlca.ilcd.flows.Modelling;
import org.openlca.ilcd.util.Flows;
import org.openlca.ilcd.util.Refs;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class FlowConverter implements Converter<Flow> {

	private final Util util;

	FlowConverter(Util util) {
		this.util = util;
	}

	@Override
	public Flow run(JsonObject obj) {
		if (obj == null)
			return null;
		Flow flow = new Flow();
		flow.version = "1.1";
		flow.adminInfo = createAdminInfo(obj);
		flow.flowInfo = new FlowInfo();
		flow.flowInfo.dataSetInfo = createDataSetInfo(obj);
		flow.flowInfo.geography = createGeography(obj);
		flow.modelling = createModelling(obj);
		addFlowProperties(flow, obj);
		return flow;
	}

	private DataSetInfo createDataSetInfo(JsonObject obj) {
		DataSetInfo info = new DataSetInfo();
		info.uuid = In.getString(obj, "@id");
		info.name = new FlowName();
		util.setLangString(info.name.baseName, In.getString(obj, "name"));
		util.setLangString(info.generalComment, In.getString(obj, "description"));
		info.casNumber = In.getString(obj, "cas");
		info.sumFormula = In.getString(obj, "formula");
		util.setLangString(info.synonyms, In.getString(obj, "synonyms"));
		info.classificationInformation = createCategoryInfo(obj);
		return info;
	}

	private FlowCategoryInfo createCategoryInfo(JsonObject obj) {
		FlowCategoryInfo info = new FlowCategoryInfo();
		String flowType = In.getString(obj, "flowType");
		Classification classification = util.createClassification(obj);
		if ("ELEMENTARY_FLOW".equals(flowType)) {
			CompartmentList compartments = new CompartmentList();
			for (Category category : classification.categories) {
				compartments.compartments.add(createCompartment(category));
			}
			info.compartmentLists.add(compartments);
		} else {
			info.classifications.add(classification);
		}
		return info;
	}

	private Compartment createCompartment(Category category) {
		Compartment compartment = new Compartment();
		compartment.catId = category.classId;
		compartment.level = category.level;
		compartment.value = category.value;
		return compartment;
	}

	private AdminInfo createAdminInfo(JsonObject obj) {
		AdminInfo info = new AdminInfo();
		info.publication = util.createPublication(obj);
		info.dataEntry = createDataEntry(obj);
		return info;
	}

	private DataEntry createDataEntry(JsonObject obj) {
		DataEntry entry = new DataEntry();
		entry.timeStamp = In.getTimestamp(obj, "lastChange");
		entry.formats.add(Refs.ilcd());
		return entry;
	}

	private void addFlowProperties(Flow flow, JsonObject obj) {
		List<FlowPropertyRef> properties = Flows.flowProperties(flow);
		JsonArray unitArray = In.getArray(obj, "flowProperties");
		if (unitArray == null)
			return;
		int pos = 1;
		for (JsonElement element : unitArray) {
			if (!element.isJsonObject())
				continue;
			FlowPropertyRef property = createFlowPropertyRef(element.getAsJsonObject(), pos);
			if (property.dataSetInternalID != 0) {
				pos++;
			}
			properties.add(property);
		}
	}

	private FlowPropertyRef createFlowPropertyRef(JsonObject factor, int pos) {
		JsonObject property = In.getRef(factor, "flowProperty", util.store);
		FlowPropertyRef propRef = new FlowPropertyRef();
		propRef.flowProperty = util.createRef(property);
		if (In.getBool(factor, "referenceFlowProperty"))
			propRef.dataSetInternalID = 0;
		else
			propRef.dataSetInternalID = pos;
		propRef.meanValue = In.getDouble(factor, "conversionFactor", 1);
		return propRef;
	}

	private Geography createGeography(JsonObject obj) {
		JsonObject location = In.getRef(obj, "location", util.store);
		if (location == null)
			return null;
		Geography geography = new Geography();
		util.setLangString(geography.location, In.getString(location, "code"));
		return geography;
	}

	private Modelling createModelling(JsonObject obj) {
		Modelling mav = new Modelling();
		LCIMethod method = new LCIMethod();
		mav.lciMethod = method;
		method.flowType = getFlowType(obj);
		return mav;
	}

	private FlowType getFlowType(JsonObject obj) {
		String flowType = In.getString(obj, "flowType");
		if (flowType == null)
			return FlowType.OTHER_FLOW;
		switch (flowType) {
		case "ELEMENTARY_FLOW":
			return FlowType.ELEMENTARY_FLOW;
		case "PRODUCT_FLOW":
			return FlowType.PRODUCT_FLOW;
		case "WASTE_FLOW":
			return FlowType.WASTE_FLOW;
		default:
			return FlowType.OTHER_FLOW;
		}
	}
}
