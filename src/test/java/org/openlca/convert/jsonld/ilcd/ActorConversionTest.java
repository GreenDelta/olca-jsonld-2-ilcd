package org.openlca.convert.jsonld.ilcd;

import junit.framework.Assert;

import org.junit.Test;
import org.openlca.ilcd.contacts.Contact;
import org.openlca.ilcd.contacts.DataSetInfo;

import com.google.gson.JsonObject;

public class ActorConversionTest {

	@Test
	public void minimal() {
		Contact contact = convert("minimal", "1cd119a4-ad82-4cb4-9cad-9ccb0d70b14e");
		Assert.assertEquals("1cd119a4-ad82-4cb4-9cad-9ccb0d70b14e", contact.getUUID());
		Utils.assertLangString(contact.getName(), "Actor");
		Assert.assertEquals("00.00.000", contact.adminInfo.publication.version);
		Assert.assertEquals("2017-05-11T11:55:07.652+02:00", contact.adminInfo.dataEntry.timeStamp.toString());
		DataSetInfo info = contact.contactInfo.dataSetInfo;
		Utils.assertNull(info.email, info.telefax, info.telephone, info.wwwAddress, info.description,
				info.contactAddress, info.classifications.get(0).categories);
	}

	@Test
	public void complete() {
		Contact contact = convert("complete", "9fa11715-b484-436a-b01d-6d2af234c1c7");
		Assert.assertEquals("9fa11715-b484-436a-b01d-6d2af234c1c7", contact.getUUID());
		Utils.assertLangString(contact.getName(), "Actor");
		Assert.assertEquals("00.00.002", contact.adminInfo.publication.version);
		Assert.assertEquals("2017-05-11T11:50:33.055+02:00", contact.adminInfo.dataEntry.timeStamp.toString());
		DataSetInfo info = contact.contactInfo.dataSetInfo;
		Utils.assertLangString(info.description, "This is a complete actor");
		Assert.assertEquals("actor@greendelta.com", info.email);
		Assert.assertEquals("+4930123321", info.telefax);
		Assert.assertEquals("+4930321123", info.telephone);
		Assert.assertEquals("http://somewebsite.com", info.wwwAddress);
		Utils.assertLangString(info.contactAddress, "Müllerstraße 135, 11111 Berlin");
		Assert.assertEquals(1, info.classifications.size());
		Utils.assertClassification(info.classifications.get(0),
				"Main", "65741ebf-e72b-37db-8b07-4f111ca4cf42",
				"Sub", "133ccead-8cd0-3718-b0c6-2450cbfcb040");
	}

	private Contact convert(String testType, String id) {
		Util config = Utils.createConfig(testType);
		ActorConverter converter = new ActorConverter(config);
		JsonObject actor = config.store.get("Actor", id);
		return converter.run(actor);
	}

}
