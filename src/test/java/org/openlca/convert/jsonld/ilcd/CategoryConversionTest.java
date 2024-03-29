package org.openlca.convert.jsonld.ilcd;

import org.junit.Test;
import org.openlca.ilcd.commons.Classification;

import com.google.gson.JsonObject;

public class CategoryConversionTest {

	@Test
	public void minimal() {
		Classification classification = convert("minimal", "65741ebf-e72b-37db-8b07-4f111ca4cf42");
		TestUtils.assertClassification(classification,
				"Main", "65741ebf-e72b-37db-8b07-4f111ca4cf42");
	}

	@Test
	public void complete() {
		Classification classification = convert("complete", "133ccead-8cd0-3718-b0c6-2450cbfcb040");
		TestUtils.assertClassification(classification,
				"Main", "65741ebf-e72b-37db-8b07-4f111ca4cf42",
				"Sub", "133ccead-8cd0-3718-b0c6-2450cbfcb040");
	}

	private Classification convert(String testType, String id) {
		Util util = TestUtils.createUtil(testType);
		CategoryConverter converter = new CategoryConverter(util);
		JsonObject category = In.parse(util.config.store.get("Category", id));
		return converter.run(category);
	}

}
