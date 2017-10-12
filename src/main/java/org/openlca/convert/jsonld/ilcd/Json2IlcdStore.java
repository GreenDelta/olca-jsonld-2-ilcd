package org.openlca.convert.jsonld.ilcd;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.openlca.ilcd.io.DataStore;
import org.openlca.ilcd.io.DataStoreException;
import org.openlca.ilcd.methods.LCIAMethod;
import org.openlca.ilcd.sources.FileRef;
import org.openlca.ilcd.sources.Source;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

public class Json2IlcdStore {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final JsonStore jsonStore;
	private final DataStore ilcdStore;
	private final Json2Ilcd converter;
	private final File tmpDir;

	public Json2IlcdStore(JsonStore jsonStore, DataStore ilcdStore) throws IOException {
		this.jsonStore = jsonStore;
		this.ilcdStore = ilcdStore;
		this.converter = new Json2Ilcd(new Config(jsonStore, this::convertRef));
		tmpDir = Files.createTempDirectory("Json2ilcdStore").toFile();
	}

	public Json2IlcdStore(JsonStore jsonStore, DataStore ilcdStore, RefCallback convertRef) throws IOException {
		this.jsonStore = jsonStore;
		this.ilcdStore = ilcdStore;
		this.converter = new Json2Ilcd(new Config(jsonStore, convertRef));
		tmpDir = Files.createTempDirectory("Json2ilcdStore").toFile();
	}

	public void convertAndPut(JsonObject obj) {
		String type = In.getString(obj, "@type");
		if (type == null)
			throw new IllegalArgumentException("No type specified, can not convert");
		try {
			switch (type) {
			case "Actor":
				ilcdStore.put(converter.convertActor(obj));
				break;
			case "Source":
				Source source = converter.convertSource(obj);
				List<File> files = new ArrayList<>();
				for (FileRef ref : source.sourceInfo.dataSetInfo.files) {
					String filename = ref.uri.substring(ref.uri.lastIndexOf("/") + 1);
					File file = toFile(jsonStore.getExternalFile(source.getUUID(), filename), filename);
					files.add(file);
				}
				if (files.isEmpty()) {
					ilcdStore.put(source);
				} else {
					ilcdStore.put(source, files.toArray(new File[files.size()]));
				}
				break;
			case "UnitGroup":
				ilcdStore.put(converter.convertUnitGroup(obj));
				break;
			case "FlowProperty":
				ilcdStore.put(converter.convertFlowProperty(obj));
				break;
			case "Flow":
				ilcdStore.put(converter.convertFlow(obj));
				break;
			case "Process":
				ilcdStore.put(converter.convertProcess(obj));
				break;
			case "ImpactMethod":
				List<LCIAMethod> categories = converter.convertImpactMethod(obj);
				for (LCIAMethod category : categories) {
					ilcdStore.put(category);
				}
				break;
			default:
				log.warn("Unsupported type: " + type);
			}
		} catch (DataStoreException e) {
			log.error("Error converting JSON to ILCD", e);
		}
	}

	private File toFile(byte[] data, String filename) {
		try {
			File tmp = new File(tmpDir, filename);
			tmp.createNewFile();
			Files.write(tmp.toPath(), data);
			tmp.deleteOnExit();
			return tmp;
		} catch (IOException e) {
			log.error("Error creating tmp file", e);
			return null;
		}
	}

	private void convertRef(String type, String refId) {
		JsonObject obj = jsonStore.get(type, refId);
		convertAndPut(obj);
	}

}
