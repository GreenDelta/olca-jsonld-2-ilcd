package org.openlca.convert.jsonld.ilcd;

import org.openlca.ilcd.contacts.AdminInfo;
import org.openlca.ilcd.contacts.Contact;
import org.openlca.ilcd.contacts.ContactInfo;
import org.openlca.ilcd.contacts.DataSetInfo;

import com.google.gson.JsonObject;

class ActorConverter implements Converter<Contact> {

	private final Util util;

	ActorConverter(Util util) {
		this.util = util;
	}

	@Override
	public Contact run(JsonObject obj) {
		if (obj == null)
			return null;
		Contact contact = new Contact();
		contact.version = "1.1";
		contact.adminInfo = createAdminInfo(obj);
		contact.contactInfo = new ContactInfo();
		contact.contactInfo.dataSetInfo = createDataSetInfo(obj);
		return contact;
	}

	private DataSetInfo createDataSetInfo(JsonObject obj) {
		DataSetInfo info = new DataSetInfo();
		info.uuid = In.getString(obj, "@id");
		util.setLangString(info.name, In.getString(obj, "name"));
		util.setLangString(info.description, In.getString(obj, "description"));
		info.classifications.add(util.createClassification(obj));
		info.email = In.getString(obj, "email");
		info.telefax = In.getString(obj, "telefax");
		info.telephone = In.getString(obj, "telephone");
		info.wwwAddress = In.getString(obj, "website");
		util.setLangString(info.contactAddress, convertAddress(obj));
		return info;
	}

	private String convertAddress(JsonObject obj) {
		String address = In.getString(obj, "address");
		if (address == null || address.isEmpty())
			return null;
		String zipCode = In.getString(obj, "zipCode");
		if (zipCode != null && !zipCode.isEmpty()) {
			address += ", " + zipCode;
		}
		String city = In.getString(obj, "city");
		if (city != null && !city.isEmpty()) {
			address += " " + city;
		}
		return address;
	}

	private AdminInfo createAdminInfo(JsonObject obj) {
		AdminInfo info = new AdminInfo();
		info.publication = util.createPublication(obj);
		info.dataEntry = util.createDataEntry(obj);
		return info;
	}

}
