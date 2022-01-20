package org.openlca.convert.jsonld.ilcd;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.flowproperties.DataSetInfo;
import org.openlca.ilcd.flowproperties.FlowProperty;

import com.google.gson.JsonObject;

public class FlowPropertyConversionTest {

	@Test
	public void minimal() {
		FlowProperty property = convert("minimal", "f7709d96-c05c-42b4-b5e1-945daf582a20");
		Assert.assertEquals("f7709d96-c05c-42b4-b5e1-945daf582a20", property.getUUID());
		TestUtils.assertLangString(property.getName(), "Flow property");
		Assert.assertEquals("00.00.000", property.adminInfo.publication.version);
		Assert.assertEquals("2017-05-12T09:58:53.034+02:00", property.adminInfo.dataEntry.timeStamp.toString());
		Ref unitGroupRef = property.flowPropertyInfo.quantitativeReference.unitGroup;
		TestUtils.assertRef(unitGroupRef, "fcc2440b-80c2-4fff-b2e1-bff41c551ecb", "Unit group");
		DataSetInfo info = property.flowPropertyInfo.dataSetInfo;
		TestUtils.assertNull(info.classifications.get(0).categories);
	}

	@Test
	public void complete() {
		FlowProperty property = convert("complete", "4cb61669-4853-4556-9667-4a4a22d3f169");
		Assert.assertEquals("4cb61669-4853-4556-9667-4a4a22d3f169", property.getUUID());
		TestUtils.assertLangString(property.getName(), "Flow property");
		Assert.assertEquals("00.00.000", property.adminInfo.publication.version);
		Assert.assertEquals("2017-05-12T10:02:34.516+02:00", property.adminInfo.dataEntry.timeStamp.toString());
		DataSetInfo info = property.flowPropertyInfo.dataSetInfo;
		TestUtils.assertLangString(info.generalComment, "This is a complete flow property");
		Ref unitGroupRef = property.flowPropertyInfo.quantitativeReference.unitGroup;
		TestUtils.assertRef(unitGroupRef, "77242196-8b88-443f-9b22-4030ff6c21f3", "Unit group");
		Assert.assertEquals(1, info.classifications.size());
		TestUtils.assertClassification(info.classifications.get(0),
				"Main", "700f4707-78e0-3582-8d9b-12d2cee29cbe",
				"Sub", "7bb0eb76-b92d-3600-9cf5-9494bc180ea6");
	}

	private FlowProperty convert(String testType, String id) {
		Util util = TestUtils.createUtil(testType);
		FlowPropertyConverter converter = new FlowPropertyConverter(util);
		JsonObject property = In.parse(util.config.store.get("FlowProperty", id));
		return converter.run(property);
	}

}
