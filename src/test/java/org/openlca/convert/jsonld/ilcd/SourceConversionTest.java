package org.openlca.convert.jsonld.ilcd;

import junit.framework.Assert;

import org.junit.Test;
import org.openlca.ilcd.sources.DataSetInfo;
import org.openlca.ilcd.sources.Source;

import com.google.gson.JsonObject;

public class SourceConversionTest {

	@Test
	public void minimal() {
		Source source = convert("minimal", "71df7790-40c1-47ed-8b8d-90a0b67bd8c0");
		Assert.assertEquals("71df7790-40c1-47ed-8b8d-90a0b67bd8c0", source.getUUID());
		Assert.assertEquals("Source", source.getName().get(0).value);
		Assert.assertEquals("00.00.000", source.adminInfo.publication.version);
		Assert.assertEquals("2017-05-11T15:15:14.846+02:00", source.adminInfo.dataEntry.timeStamp.toString());
		DataSetInfo info = source.sourceInfo.dataSetInfo;
		Utils.assertNull(info.description, info.citation);
	}

	@Test
	public void complete() {
		Source source = convert("complete", "c2f54cb9-d152-4137-8ad7-871598478578");
		Assert.assertEquals("c2f54cb9-d152-4137-8ad7-871598478578", source.getUUID());
		Assert.assertEquals("Source", source.getName().get(0).value);
		Assert.assertEquals("00.00.002", source.adminInfo.publication.version);
		Assert.assertEquals("2017-05-11T15:11:23.334+02:00", source.adminInfo.dataEntry.timeStamp.toString());
		DataSetInfo info = source.sourceInfo.dataSetInfo;
		Utils.assertLangString(info.description, "This is a complete source");
		Assert.assertEquals("A text reference 2017", info.citation);
		Assert.assertEquals(1, info.classifications.size());
		Utils.assertClassification(info.classifications.get(0),
				"Main", "da30666b-ff51-3844-9ab0-28dd830a4400",
				"Sub", "7b1d65cd-67a7-38ef-ad1a-d88a7d1bc1a9");
	}

	private Source convert(String testType, String id) {
		Util config = Utils.createConfig(testType);
		SourceConverter converter = new SourceConverter(config);
		JsonObject source = config.store.get("Source", id);
		return converter.run(source);
	}

}
