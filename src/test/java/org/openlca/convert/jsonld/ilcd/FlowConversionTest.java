package org.openlca.convert.jsonld.ilcd;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.openlca.ilcd.flows.Compartment;
import org.openlca.ilcd.flows.DataSetInfo;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.flows.FlowCategoryInfo;
import org.openlca.ilcd.flows.FlowPropertyRef;

import com.google.gson.JsonObject;

public class FlowConversionTest {

	@Test
	public void minimal() {
		Flow flow = convert("minimal", "9e4bce95-3d1e-41ef-8b7f-8f8dd109eb91");
		Assert.assertEquals("9e4bce95-3d1e-41ef-8b7f-8f8dd109eb91", flow.getUUID());
		TestUtils.assertLangString(flow.getName(), "Flow");
		Assert.assertEquals("00.00.000", flow.adminInfo.publication.version);
		Assert.assertEquals("2017-05-12T12:10:11.142+02:00", flow.adminInfo.dataEntry.timeStamp.toString());
		DataSetInfo info = flow.flowInfo.dataSetInfo;
		FlowCategoryInfo classification = info.classificationInformation;
		TestUtils.assertNull(classification.classifications);
		TestUtils.assertNull(classification.compartmentLists.get(0).compartments);
		TestUtils.assertNull(info.casNumber, info.sumFormula, info.synonyms, flow.flowInfo.geography);
		List<FlowPropertyRef> properties = flow.flowPropertyList.flowProperties;
		Assert.assertEquals(1, properties.size());
		assertFlowProperty(properties, 0, 0, "f7709d96-c05c-42b4-b5e1-945daf582a20", "Flow property", 1);
	}

	@Test
	public void complete() {
		Flow flow = convert("complete", "78404ba2-f6e6-47a6-99de-e7c8464f7c54");
		Assert.assertEquals("78404ba2-f6e6-47a6-99de-e7c8464f7c54", flow.getUUID());
		TestUtils.assertLangString(flow.getName(), "Flow");
		Assert.assertEquals("00.00.009", flow.adminInfo.publication.version);
		Assert.assertEquals("2017-05-12T13:02:23.092+02:00", flow.adminInfo.dataEntry.timeStamp.toString());
		DataSetInfo info = flow.flowInfo.dataSetInfo;
		TestUtils.assertLangString(info.generalComment, "This is a complete elementary flow");
		Assert.assertEquals("12345-67-8", info.casNumber);
		Assert.assertEquals("O2", info.sumFormula);
		TestUtils.assertLangString(info.synonyms, "Synonym");
		TestUtils.assertLangString(flow.flowInfo.geography.location, "DE");
		List<FlowPropertyRef> properties = flow.flowPropertyList.flowProperties;
		Assert.assertEquals(3, properties.size());
		assertFlowProperty(properties, 0, 1, "d872fb63-46bc-486f-b279-d81db07ce7d7", "Flow property 2", 2);
		assertFlowProperty(properties, 1, 0, "4cb61669-4853-4556-9667-4a4a22d3f169", "Flow property", 1);
		assertFlowProperty(properties, 2, 2, "34e90c21-23ce-48e4-8184-2f6eb03081b1", "Flow property 3", 3);
		FlowCategoryInfo classification = info.classificationInformation;
		TestUtils.assertNull(classification.classifications);
		List<Compartment> compartments = classification.compartmentLists.get(0).compartments;
		Assert.assertEquals(2, compartments.size());
		assertCompartment(compartments, 0, "b333f073-ae6b-3222-af70-84e62e32a6fd", "Main");
		assertCompartment(compartments, 1, "4d029226-fd17-34ae-8bc8-f12267149e6b", "Sub");
	}

	private void assertFlowProperty(List<FlowPropertyRef> properties, int index, int id, String uuid, String name,
			double factor) {
		FlowPropertyRef property = properties.get(index);
		Assert.assertEquals(id, (int) property.dataSetInternalID);
		Assert.assertEquals(uuid, property.flowProperty.uuid);
		TestUtils.assertLangString(property.flowProperty.name, name);
		Assert.assertEquals(factor, property.meanValue);
	}

	private void assertCompartment(List<Compartment> compartments, int index, String uuid, String name) {
		Compartment compartment = compartments.get(index);
		Assert.assertEquals(name, compartment.value);
		Assert.assertEquals(index, compartment.level);
		Assert.assertEquals(uuid, compartment.catId);
	}

	private Flow convert(String testType, String id) {
		Util util = TestUtils.createUtil(testType);
		FlowConverter converter = new FlowConverter(util);
		JsonObject flow = util.config.store.get("Flow", id);
		return converter.run(flow);
	}

}
