package org.openlca.convert.jsonld.ilcd;

import java.util.List;

public interface JsonStore {

	String get(String type, String refId);

	byte[] getExternalFile(String sourceRefId, String filename);

	List<String> getGlobalParameters();

}
