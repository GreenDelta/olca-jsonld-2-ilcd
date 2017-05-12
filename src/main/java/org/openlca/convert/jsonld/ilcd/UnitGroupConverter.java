package org.openlca.convert.jsonld.ilcd;

import java.util.List;

import org.openlca.ilcd.units.AdminInfo;
import org.openlca.ilcd.units.DataSetInfo;
import org.openlca.ilcd.units.QuantitativeReference;
import org.openlca.ilcd.units.Unit;
import org.openlca.ilcd.units.UnitGroup;
import org.openlca.ilcd.units.UnitGroupInfo;
import org.openlca.ilcd.util.UnitExtension;
import org.openlca.ilcd.util.UnitGroups;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

class UnitGroupConverter implements Converter<UnitGroup> {

	private Util util;

	UnitGroupConverter(Util util) {
		this.util = util;
	}

	public UnitGroup run(JsonObject obj) {
		if (obj == null)
			return null;
		UnitGroup group = new UnitGroup();
		group.version = "1.1";
		group.adminInfo = createAdminInfo(obj);
		group.unitGroupInfo = new UnitGroupInfo();
		group.unitGroupInfo.dataSetInfo = createDataSetInfo(obj);
		group.unitGroupInfo.quantitativeReference = new QuantitativeReference();
		addUnits(group, obj);
		return group;
	}

	private DataSetInfo createDataSetInfo(JsonObject obj) {
		DataSetInfo dataSetInfo = new DataSetInfo();
		dataSetInfo.uuid = In.getString(obj, "@id");
		util.setLangString(dataSetInfo.name, In.getString(obj, "name"));
		util.setLangString(dataSetInfo.generalComment, In.getString(obj, "description"));
		dataSetInfo.classifications.add(util.createClassification(obj));
		return dataSetInfo;
	}

	private void addUnits(UnitGroup unitGroup, JsonObject obj) {
		List<Unit> units = UnitGroups.units(unitGroup);
		JsonArray unitArray = In.getArray(obj, "units");
		if (unitArray == null)
			return;
		int pos = 1;
		for (JsonElement element : unitArray) {
			if (!element.isJsonObject())
				continue;
			Unit unit = createUnit(element.getAsJsonObject(), pos++);
			units.add(unit);
		}
	}

	private Unit createUnit(JsonObject obj, int pos) {
		Unit unit = new Unit();
		unit.name = In.getString(obj, "name");
		unit.factor = In.getDouble(obj, "conversionFactor", 1);
		boolean isRef = In.getBool(obj, "referenceUnit");
		unit.id = isRef ? 0 : pos;
		util.setLangString(unit.comment, In.getString(obj, "description"));
		UnitExtension unitExtension = new UnitExtension(unit);
		unitExtension.setUnitId(In.getString(obj, "@id"));
		return unit;
	}

	private AdminInfo createAdminInfo(JsonObject obj) {
		AdminInfo info = new AdminInfo();
		info.publication = util.createPublication(obj);
		info.dataEntry = util.createDataEntry(obj);
		return info;
	}

}
