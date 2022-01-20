package org.openlca.convert.jsonld.ilcd;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openlca.ilcd.commons.QuantitativeReferenceType;
import org.openlca.ilcd.commons.Time;
import org.openlca.ilcd.processes.DataSetInfo;
import org.openlca.ilcd.processes.Exchange;
import org.openlca.ilcd.processes.Geography;
import org.openlca.ilcd.processes.Location;
import org.openlca.ilcd.processes.Parameter;
import org.openlca.ilcd.processes.ParameterSection;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.processes.ProcessInfo;
import org.openlca.ilcd.processes.ProcessName;
import org.openlca.ilcd.processes.QuantitativeReference;
import org.openlca.ilcd.processes.Technology;
import org.openlca.ilcd.util.TimeExtension;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

class ProcessConverter implements Converter<Process> {

	private final Util util;

	ProcessConverter(Util util) {
		this.util = util;
	}

	@Override
	public Process run(JsonObject obj) {
		if (obj == null)
			return null;
		Process process = new Process();
		process.version = "1.1";
		process.processInfo = new ProcessInfo();
		process.processInfo.dataSetInfo = createDataSetInfo(obj);
		JsonObject doc = In.getObject(obj, "processDocumentation");
		process.processInfo.geography = createGeography(obj, doc);
		process.processInfo.time = createTime(doc);
		process.processInfo.technology = createTechnology(doc);
		process.processInfo.parameters = createParameters(obj);
		process.processInfo.quantitativeReference = new QuantitativeReference();
		process.processInfo.quantitativeReference.type = QuantitativeReferenceType.REFERENCE_FLOWS;
		process.processInfo.quantitativeReference.referenceFlows.add(0);
		process.adminInfo = new ProcessAdminInfo(util).create(obj);
		process.modelling = new ProcessModelling(util).create(obj);
		// Map contains the flow id for product outputs, exchange id otherwise
		Map<String, Exchange> idMap = addExchanges(obj, process);
		ExchangeAllocations.map(obj, idMap);
		return process;
	}

	private DataSetInfo createDataSetInfo(JsonObject obj) {
		DataSetInfo info = new DataSetInfo();
		info.uuid = In.getString(obj, "@id");
		info.name = new ProcessName();
		util.setLangString(info.name.name, In.getString(obj, "name"));
		util.setLangString(info.comment, In.getString(obj, "description"));
		info.classifications.add(util.createClassification(obj));
		return info;
	}

	private Time createTime(JsonObject doc) {
		if (doc == null)
			return null;
		Time time = new Time();
		TimeExtension ext = new TimeExtension(time);
		Date validFrom = In.getDate(doc, "validFrom");
		if (validFrom != null) {
			time.referenceYear = getYear(validFrom);
			ext.setStartDate(validFrom);
		}
		Date validUntil = In.getDate(doc, "validUntil");
		if (validUntil != null) {
			time.validUntil = getYear(validUntil);
			ext.setEndDate(validUntil);
		}
		return time;
	}

	private Geography createGeography(JsonObject obj, JsonObject doc) {
		JsonObject location = In.getRef(obj, "location", util.config.store);
		String geoDesc = doc == null ? null : In.getString(doc, "geographyDescription");
		if (location == null && geoDesc == null)
			return null;
		Geography geography = new Geography();
		geography.location = new Location();
		util.setLangString(geography.location.description, geoDesc);
		if (location == null)
			return geography;
		double latitude = In.getDouble(location, "latitude", 0);
		double longitude = In.getDouble(location, "longitude", 0);
		geography.location.code = In.getString(location, "code");
		geography.location.latitudeAndLongitude = latitude + "," + longitude;
		return geography;
	}

	private Technology createTechnology(JsonObject doc) {
		if (doc == null)
			return null;
		String techDesc = In.getString(doc, "technologyDescription");
		if (techDesc == null)
			return null;
		Technology technology = new Technology();
		util.setLangString(technology.description, techDesc);
		return technology;
	}

	private ParameterSection createParameters(JsonObject obj) {
		JsonArray parameters = In.getArray(obj, "parameters");
		if (parameters == null || parameters.size() == 0)
			return null;
		ParameterSection section = new ParameterSection();
		ParameterConverter converter = new ParameterConverter(util);
		for (JsonElement elem : parameters) {
			Parameter param = converter.run(elem.getAsJsonObject(), ParameterConverter.SCOPE_PROCESS);
			section.parameters.add(param);
		}
		List<String> globalParameters = util.config.store.getGlobalParameters();
		for (String parameterJson : globalParameters) {
			JsonObject parameter = In.parse(parameterJson);
			Parameter param = converter.run(parameter, ParameterConverter.SCOPE_GLOBAL);
			section.parameters.add(param);
		}
		return section;
	}

	private Map<String, Exchange> addExchanges(JsonObject obj, Process process) {
		JsonArray exchangeArray = In.getArray(obj, "exchanges");
		ExchangeConverter converter = new ExchangeConverter(util);
		Map<String, Exchange> exchangeMap = new HashMap<>();
		int id = 1;
		for (JsonElement elem : exchangeArray) {
			JsonObject eObj = elem.getAsJsonObject();
			Exchange exchange = converter.run(eObj, process);
			if (!In.getBool(eObj, "quantitativeReference")) {
				exchange.id = id++;
			}
			process.exchanges.add(exchange);
			if (isProductOutput(eObj)) {
				JsonObject fObj = In.getObject(eObj, "flow");
				exchangeMap.put(In.getString(fObj, "@id"), exchange);
			} else {
				exchangeMap.put(In.getString(eObj, "@id"), exchange);
			}
		}
		return exchangeMap;
	}

	private boolean isProductOutput(JsonObject exchange) {
		if (In.getBool(exchange, "input"))
			return false;
		if (In.getBool(exchange, "avoidedProduct"))
			return false;
		JsonObject flow = In.getObject(exchange, "flow");
		String type = In.getString(flow, "flowType");
		return "PRODUCT_FLOW".equals(type);
	}

	private Integer getYear(Date date) {
		if (date == null)
			return null;
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(date);
		return cal.get(Calendar.YEAR);
	}

}
