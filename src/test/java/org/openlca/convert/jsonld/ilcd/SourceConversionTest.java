package org.openlca.convert.jsonld.ilcd;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.ilcd.sources.DataSetInfo;
import org.openlca.ilcd.sources.Source;

import com.google.gson.JsonObject;

public class SourceConversionTest {

	@Test
	public void minimal() {
		Source source = convert("minimal", "71df7790-40c1-47ed-8b8d-90a0b67bd8c0");
		Assert.assertEquals("71df7790-40c1-47ed-8b8d-90a0b67bd8c0", source.getUUID());
		TestUtils.assertLangString(source.getName(), "Source");
		Assert.assertEquals("00.00.000", source.adminInfo.publication.version);
		Assert.assertEquals("2017-05-11T15:15:14.846+02:00", source.adminInfo.dataEntry.timeStamp.toString());
		DataSetInfo info = source.sourceInfo.dataSetInfo;
		TestUtils.assertNull(info.description, info.citation, info.classifications.get(0).categories);
	}

	@Test
	public void complete() {
		Source source = convert("complete", "c2f54cb9-d152-4137-8ad7-871598478578");
		Assert.assertEquals("c2f54cb9-d152-4137-8ad7-871598478578", source.getUUID());
		TestUtils.assertLangString(source.getName(), "Source");
		Assert.assertEquals("00.00.002", source.adminInfo.publication.version);
		Assert.assertEquals("2017-05-11T15:11:23.334+02:00", source.adminInfo.dataEntry.timeStamp.toString());
		DataSetInfo info = source.sourceInfo.dataSetInfo;
		TestUtils.assertLangString(info.description, "This is a complete source");
		Assert.assertEquals("A text reference 2017", info.citation);
		Assert.assertEquals(1, info.classifications.size());
		TestUtils.assertClassification(info.classifications.get(0),
				"Main", "da30666b-ff51-3844-9ab0-28dd830a4400",
				"Sub", "7b1d65cd-67a7-38ef-ad1a-d88a7d1bc1a9");
	}

	private Source convert(String testType, String id) {
		Util util = TestUtils.createUtil(testType);
		SourceConverter converter = new SourceConverter(util);
		JsonObject source = In.parse(util.config.store.get("Source", id));
		return converter.run(source);
	}

}
