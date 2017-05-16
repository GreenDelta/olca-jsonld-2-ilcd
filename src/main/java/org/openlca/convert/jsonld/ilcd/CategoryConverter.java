package org.openlca.convert.jsonld.ilcd;

import org.openlca.ilcd.commons.Category;
import org.openlca.ilcd.commons.Classification;

import com.google.gson.JsonObject;

class CategoryConverter implements Converter<Classification> {

	private final Util util;

	CategoryConverter(Util util) {
		this.util = util;
	}

	@Override
	public Classification run(JsonObject obj) {
		if (obj == null)
			return new Classification();
		JsonObject parent = In.getRef(obj, "category", util.config.store);
		Classification classification = run(parent);
		Category c = createCategory(obj);
		c.level = classification.categories.size();
		classification.categories.add(c);
		return classification;
	}

	private Category createCategory(JsonObject obj) {
		Category c = new Category();
		c.classId = In.getString(obj, "@id");
		c.value = In.getString(obj, "name");
		return c;
	}

}
