package org.openlca.convert.jsonld.ilcd;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.openlca.ilcd.commons.Category;
import org.openlca.ilcd.commons.Classification;

import junit.framework.Assert;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

class Utils {

	private static final Gson gson = new Gson();

	static Util createConfig(String testType) {
		return new Util(null, null, (type, refId) -> parse(testType + "/" + type + "/" + refId + ".json"));
	}

	private static JsonObject parse(String path) {
		InputStream stream = Utils.class.getClassLoader().getResourceAsStream(path);
		JsonElement element = gson.fromJson(new InputStreamReader(stream), JsonElement.class);
		return element != null && element.isJsonObject() ? element.getAsJsonObject() : null;
	}

	static void assertClassification(Classification c, String catName, String catId) {
		assertClassification(c, catName, catId, null, null);
	}

	static void assertClassification(Classification c, String cat1Name, String cat1Id, String cat2Name, String cat2Id) {
		int expectedSize = cat2Name == null ? 1 : 2;
		Assert.assertEquals(expectedSize, c.categories.size());
		Category cat1 = c.categories.get(0);
		Assert.assertEquals(cat1Name, cat1.value);
		Assert.assertEquals(cat1Id, cat1.classId);
		Assert.assertEquals(0, cat1.level);
		if (cat2Name == null)
			return;
		Category cat2 = c.categories.get(1);
		Assert.assertEquals(cat2Name, cat2.value);
		Assert.assertEquals(cat2Id, cat2.classId);
		Assert.assertEquals(1, cat2.level);
	}

	static void assertNull(Object... values) {
		if (values == null)
			return;
		for (Object value : values) {
			if (value instanceof List) {
				Assert.assertEquals(0, ((List<?>) value).size());
			} else {
				Assert.assertNull(value);
			}
		}
	}

}
