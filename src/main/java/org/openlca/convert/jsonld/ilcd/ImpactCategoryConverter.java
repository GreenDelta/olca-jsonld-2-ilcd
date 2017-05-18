package org.openlca.convert.jsonld.ilcd;

import org.openlca.ilcd.methods.DataSetInfo;
import org.openlca.ilcd.methods.Factor;
import org.openlca.ilcd.methods.FactorList;
import org.openlca.ilcd.methods.LCIAMethod;
import org.openlca.ilcd.methods.MethodInfo;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

class ImpactCategoryConverter implements Converter<LCIAMethod> {

	private final Util util;
	private final String methodRefId;
	private final String methodName;

	ImpactCategoryConverter(Util util, String methodRefId, String methodName) {
		this.util = util;
		this.methodRefId = methodRefId;
		this.methodName = methodName;
	}

	@Override
	public LCIAMethod run(JsonObject obj) {
		if (obj == null)
			return null;
		LCIAMethod method = new LCIAMethod();
		method.version = "1.1";
		method.methodInfo = new MethodInfo();
		method.methodInfo.dataSetInfo = createDataSetInfo(obj);
		method.characterisationFactors = createFactors(obj);
		util.putAttribute("olca_method_uuid", method.otherAttributes, methodRefId);
		return method;
	}

	private DataSetInfo createDataSetInfo(JsonObject obj) {
		DataSetInfo info = new DataSetInfo();
		info.uuid = In.getString(obj, "@id");
		info.methods.add(methodName);
		info.impactCategories.add(In.getString(obj, "name"));
		util.putAttribute("olca_category_unit", info.otherAttributes, In.getString(obj, "referenceUnitName"));
		util.setLangString(info.comment, In.getString(obj, "description"));
		return info;
	}

	private FactorList createFactors(JsonObject obj) {
		FactorList list = new FactorList();
		JsonArray factors = In.getArray(obj, "impactFactors");
		for (JsonElement elem : factors) {
			JsonObject fObj = elem.getAsJsonObject();
			Factor factor = new Factor();
			// TODO: uncertainty values + formulas
			factor.meanValue = getRefAmount(fObj);
			factor.flow = util.createRef(In.getObject(fObj, "flow"));
			list.factors.add(factor);
		}
		return list;
	}

	private double getRefAmount(JsonObject factor) {
		double value = In.getDouble(factor, "value", 0);
		double unitFactor = util.getUnitFactor(factor);
		double propertyFactor = util.getPropertyFactor(factor);
		return (value / unitFactor) * propertyFactor;
	}

}
