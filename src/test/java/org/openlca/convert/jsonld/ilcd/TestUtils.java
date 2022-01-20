package org.openlca.convert.jsonld.ilcd;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.openlca.ilcd.commons.Category;
import org.openlca.ilcd.commons.Classification;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Ref;

class TestUtils {

	static Util createUtil(String testType) {
		return createUtil(testType, new ArrayList<>());
	}

	static Util createUtil(String testType, List<String> globalParameterRefIds) {
		return new Util(new Config(new Store(testType, globalParameterRefIds), null));
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

	static void assertLangString(List<LangString> list, String... values) {
		Assert.assertEquals(values.length, list.size());
		for (int i = 0; i < values.length; i++) {
			LangString value = list.get(i);
			Assert.assertEquals(values[i], value.value);
		}
	}

	static void assertRef(Ref ref, String uuid, String name) {
		Assert.assertEquals(uuid, ref.uuid);
		assertLangString(ref.name, name);
	}

	static void assertDate(Date expectedDate, Date actualDate) {
		Calendar actual = Calendar.getInstance();
		actual.setTime(actualDate);
		Calendar expected = Calendar.getInstance();
		expected.setTime(expectedDate);
		Assert.assertEquals(expected.get(Calendar.DAY_OF_MONTH), actual.get(Calendar.DAY_OF_MONTH));
		Assert.assertEquals(expected.get(Calendar.MONTH), actual.get(Calendar.MONTH));
		Assert.assertEquals(expected.get(Calendar.YEAR), actual.get(Calendar.YEAR));
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

	static Date toDate(int day, int month, int year) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, day);
		cal.set(Calendar.MONTH, month - 1);
		cal.set(Calendar.YEAR, year);
		return cal.getTime();
	}

	private static class Store implements JsonStore {

		private final String testType;
		private final List<String> globalParameterRefIds;

		private Store(String testType, List<String> globalParameterRefIds) {
			this.testType = testType;
			this.globalParameterRefIds = globalParameterRefIds;
		}

		@Override
		public String get(String type, String refId) {
			String path = testType + "/" + type + "/" + refId + ".json";
			ClassLoader cl = TestUtils.class.getClassLoader();
			try (InputStream is = cl.getResourceAsStream(path);
					Reader reader = new InputStreamReader(is, "utf-8")) {
				return new BufferedReader(new InputStreamReader(is))
						.lines().collect(Collectors.joining("\n"));
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}

		@Override
		public List<String> getGlobalParameters() {
			List<String> list = new ArrayList<>();
			for (String refId : globalParameterRefIds) {
				list.add(get("Parameter", refId));
			}
			return list;
		}

		@Override
		public byte[] getExternalFile(String sourceRefId, String filename) {
			return null;
		}

	}

}
