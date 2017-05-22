package org.openlca.convert.jsonld.ilcd;

import org.openlca.ilcd.sources.AdminInfo;
import org.openlca.ilcd.sources.DataSetInfo;
import org.openlca.ilcd.sources.FileRef;
import org.openlca.ilcd.sources.Source;
import org.openlca.ilcd.sources.SourceInfo;

import com.google.gson.JsonObject;

class SourceConverter implements Converter<Source> {

	private final Util util;

	SourceConverter(Util util) {
		this.util = util;
	}

	@Override
	public Source run(JsonObject obj) {
		if (obj == null)
			return null;
		Source source = new Source();
		source.version = "1.1";
		source.adminInfo = createAdminInfo(obj);
		source.sourceInfo = new SourceInfo();
		source.sourceInfo.dataSetInfo = createDateSetInfo(obj);
		return source;
	}

	private DataSetInfo createDateSetInfo(JsonObject obj) {
		DataSetInfo info = new DataSetInfo();
		info.uuid = In.getString(obj, "@id");
		util.setLangString(info.name, In.getString(obj, "name"));
		util.setLangString(info.description, In.getString(obj, "description"));
		info.classifications.add(util.createClassification(obj));
		info.citation = createCitation(obj);
		String externalFile = In.getString(obj, "externalFile");
		if (externalFile != null) {
			FileRef fileRef = new FileRef();
			fileRef.uri =  "../external_docs/" + externalFile;
			info.files.add(fileRef);
		}
		return info;
	}

	private String createCitation(JsonObject obj) {
		String cit = In.getString(obj, "textReference");
		if (cit == null)
			return null;
		Integer year = In.getInt(obj, "year");
		if (year != null) {
			cit += " " + year;
		}
		return cit;
	}

	private AdminInfo createAdminInfo(JsonObject obj) {
		AdminInfo info = new AdminInfo();
		info.publication = util.createPublication(obj);
		info.dataEntry = util.createDataEntry(obj);
		return info;
	}

}
