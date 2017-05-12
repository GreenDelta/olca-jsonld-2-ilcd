package org.openlca.convert.jsonld.ilcd;

import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.flowproperties.AdminInfo;
import org.openlca.ilcd.flowproperties.DataSetInfo;
import org.openlca.ilcd.flowproperties.FlowProperty;
import org.openlca.ilcd.flowproperties.FlowPropertyInfo;
import org.openlca.ilcd.flowproperties.QuantitativeReference;

import com.google.gson.JsonObject;

class FlowPropertyConverter implements Converter<FlowProperty> {

	private final Util util;

	FlowPropertyConverter(Util util) {
		this.util = util;
	}

	@Override
	public FlowProperty run(JsonObject obj) {
		if (obj == null)
			return null;
		FlowProperty property = new FlowProperty();
		property.version = "1.1";
		property.flowPropertyInfo = new FlowPropertyInfo();
		property.flowPropertyInfo.dataSetInfo = createDataSetInfo(obj);
		property.flowPropertyInfo.quantitativeReference = createUnitGroupRef(obj);
		property.adminInfo = createAdminInfo(obj);
		return property;
	}

	private DataSetInfo createDataSetInfo(JsonObject obj) {
		DataSetInfo info = new DataSetInfo();
		info.uuid = In.getString(obj, "@id");
		util.setLangString(info.name, In.getString(obj, "name"));
		util.setLangString(info.generalComment, In.getString(obj, "description"));
		info.classifications.add(util.createClassification(obj));
		return info;
	}

	private QuantitativeReference createUnitGroupRef(JsonObject obj) {
		QuantitativeReference qRef = new QuantitativeReference();
		JsonObject unitGroup = In.getRef(obj, "unitGroup", util.store);
		Ref ref = util.createRef(unitGroup);
		qRef.unitGroup = ref;
		return qRef;
	}

	private AdminInfo createAdminInfo(JsonObject obj) {
		AdminInfo info = new AdminInfo();
		info.publication = util.createPublication(obj);
		info.dataEntry = util.createDataEntry(obj);
		return info;
	}
}
