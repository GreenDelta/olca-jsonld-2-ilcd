package org.openlca.convert.jsonld.ilcd;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.openlca.ilcd.units.DataSetInfo;
import org.openlca.ilcd.units.Unit;
import org.openlca.ilcd.units.UnitGroup;

import com.google.gson.JsonObject;

public class UnitGroupConversionTest {

	@Test
	public void minimal() {
		UnitGroup group = convert("minimal", "fcc2440b-80c2-4fff-b2e1-bff41c551ecb");
		Assert.assertEquals("fcc2440b-80c2-4fff-b2e1-bff41c551ecb", group.getUUID());
		Assert.assertEquals("Unit group", group.getName().get(0).value);
		Assert.assertEquals("00.00.000", group.adminInfo.publication.version);
		Assert.assertEquals("2017-05-12T09:58:26.295+02:00", group.adminInfo.dataEntry.timeStamp.toString());
		List<Unit> units = group.unitList.units;
		Assert.assertEquals(1, units.size());
		Assert.assertEquals(0, group.unitGroupInfo.quantitativeReference.referenceUnit);
		assertUnit(units, 0, 0, "unit", 1, null);
		Utils.assertNull(group.unitGroupInfo.dataSetInfo.classifications.get(0).categories);
	}

	@Test
	public void complete() {
		UnitGroup group = convert("complete", "77242196-8b88-443f-9b22-4030ff6c21f3");
		Assert.assertEquals("77242196-8b88-443f-9b22-4030ff6c21f3", group.getUUID());
		Assert.assertEquals("Unit group", group.getName().get(0).value);
		Assert.assertEquals("00.00.002", group.adminInfo.publication.version);
		Assert.assertEquals("2017-05-12T10:02:39.987+02:00", group.adminInfo.dataEntry.timeStamp.toString());
		DataSetInfo info = group.unitGroupInfo.dataSetInfo;
		Utils.assertLangString(info.generalComment, "This is a complete unit group");
		List<Unit> units = group.unitList.units;
		Assert.assertEquals(3, units.size());
		Assert.assertEquals(0, group.unitGroupInfo.quantitativeReference.referenceUnit);
		assertUnit(units, 0, 1, "unit1", 2, "a first unit");
		assertUnit(units, 1, 2, "unit2", 3, "a second unit");
		assertUnit(units, 2, 0, "refUnit", 1, "the reference unit");
		Assert.assertEquals(1, info.classifications.size());
		Utils.assertClassification(info.classifications.get(0),
				"Main", "7c77b6b5-796f-3a0a-b781-85f664984e38",
				"Sub", "fbf7df2f-5b76-3edc-b615-837c50e45583");
	}

	private void assertUnit(List<Unit> units, int index, int id, String name, double factor, String description) {
		Unit unit = units.get(index);
		Assert.assertEquals(id, unit.id);
		Assert.assertEquals(name, unit.name);
		Assert.assertEquals(factor, unit.factor);
		if (description != null) {
			Utils.assertLangString(unit.comment, description);
		} else {
			Utils.assertNull(unit.comment);
		}
	}

	private UnitGroup convert(String testType, String id) {
		Util config = Utils.createConfig(testType);
		UnitGroupConverter converter = new UnitGroupConverter(config);
		JsonObject group = config.store.get("UnitGroup", id);
		return converter.run(group);
	}

}
