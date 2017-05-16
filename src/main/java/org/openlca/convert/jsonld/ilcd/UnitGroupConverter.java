package org.openlca.convert.jsonld.ilcd;

import java.util.ArrayList;
import java.util.List;

import org.openlca.ilcd.units.AdminInfo;
import org.openlca.ilcd.units.DataSetInfo;
import org.openlca.ilcd.units.QuantitativeReference;
import org.openlca.ilcd.units.Unit;
import org.openlca.ilcd.units.UnitGroup;
import org.openlca.ilcd.units.UnitGroupInfo;
import org.openlca.ilcd.units.UnitList;
import org.openlca.ilcd.util.UnitExtension;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

class UnitGroupConverter implements Converter<UnitGroup> {

	private final Util util;

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
		group.unitList = new UnitList();
		group.unitList.units.addAll(createUnits(obj));
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

	private List<Unit> createUnits(JsonObject obj) {
		List<Unit> units = new ArrayList<>();
		JsonArray unitArray = In.getArray(obj, "units");
		if (unitArray == null)
			return units;
		int pos = 1;
		for (JsonElement element : unitArray) {
			Unit unit = createUnit(element.getAsJsonObject(), pos);
			if (unit.id != 0) {
				pos++;
			}
			units.add(unit);
		}
		return units;
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
