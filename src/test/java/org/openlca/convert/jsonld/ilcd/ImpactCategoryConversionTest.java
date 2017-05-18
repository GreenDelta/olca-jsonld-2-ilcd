package org.openlca.convert.jsonld.ilcd;

import javax.xml.namespace.QName;

import junit.framework.Assert;

import org.junit.Test;
import org.openlca.ilcd.methods.DataSetInfo;
import org.openlca.ilcd.methods.Factor;
import org.openlca.ilcd.methods.LCIAMethod;

import com.google.gson.JsonObject;

public class ImpactCategoryConversionTest {

	private static final String METHOD_ID = "f32d809b-b136-44bd-bbe5-2552ae3556cc";
	private static final QName UUID_QNAME = new QName("http://openlca.org/ilcd-extensions", "olca_method_uuid");
	private static final QName UNIT_QNAME = new QName("http://openlca.org/ilcd-extensions", "olca_category_unit");

	@Test
	public void minimal() {
		LCIAMethod method = convert("minimal", "eb305268-9304-4dff-ac77-47afb39df560");
		Assert.assertEquals("eb305268-9304-4dff-ac77-47afb39df560", method.getUUID());
		DataSetInfo info = method.methodInfo.dataSetInfo;
		Assert.assertEquals(1, info.methods.size());
		Assert.assertEquals("Method", info.methods.get(0));
		Assert.assertEquals(1, info.impactCategories.size());
		Assert.assertEquals("Category", info.impactCategories.get(0));
		Assert.assertEquals(METHOD_ID, method.otherAttributes.get(UUID_QNAME));
		TestUtils.assertNull(info.comment, info.classifications, info.otherAttributes.get(UNIT_QNAME));
		Assert.assertEquals(1, method.characterisationFactors.factors.size());
		Factor factor = method.characterisationFactors.factors.get(0);
		Assert.assertEquals(1d, factor.meanValue);
		TestUtils.assertLangString(factor.flow.name, "Flow");
		Assert.assertEquals("9e4bce95-3d1e-41ef-8b7f-8f8dd109eb91", factor.flow.uuid);
	}

	@Test
	public void complete() {
		LCIAMethod method = convert("complete", "7267c31f-4678-46d6-85fd-68c7d726f5a7");
		Assert.assertEquals("7267c31f-4678-46d6-85fd-68c7d726f5a7", method.getUUID());
		DataSetInfo info = method.methodInfo.dataSetInfo;
		Assert.assertEquals(1, info.methods.size());
		Assert.assertEquals("Method", info.methods.get(0));
		Assert.assertEquals(1, info.impactCategories.size());
		Assert.assertEquals("Category", info.impactCategories.get(0));
		TestUtils.assertLangString(info.comment, "This is a complete impact category");
		Assert.assertEquals(METHOD_ID, method.otherAttributes.get(UUID_QNAME));
		Assert.assertEquals("refUnit", info.otherAttributes.get(UNIT_QNAME));
		TestUtils.assertNull(info.classifications);
		Assert.assertEquals(1, method.characterisationFactors.factors.size());
		Factor factor = method.characterisationFactors.factors.get(0);
		Assert.assertEquals(2d, factor.meanValue);
		TestUtils.assertLangString(factor.flow.name, "Flow");
		Assert.assertEquals("78404ba2-f6e6-47a6-99de-e7c8464f7c54", factor.flow.uuid);
	}

	private LCIAMethod convert(String testType, String id) {
		Util util = TestUtils.createUtil(testType);
		ImpactCategoryConverter converter = new ImpactCategoryConverter(util, METHOD_ID, "Method");
		JsonObject impactCategory = util.config.store.get("ImpactCategory", id);
		return converter.run(impactCategory);
	}
}
