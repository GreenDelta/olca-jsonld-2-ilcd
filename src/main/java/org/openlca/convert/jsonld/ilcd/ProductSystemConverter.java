package org.openlca.convert.jsonld.ilcd;

import java.util.ArrayList;
import java.util.List;

import org.openlca.ilcd.commons.ExchangeDirection;
import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.commons.QuantitativeReferenceType;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.processes.DataSetInfo;
import org.openlca.ilcd.processes.Exchange;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.processes.ProcessInfo;
import org.openlca.ilcd.processes.ProcessName;
import org.openlca.ilcd.processes.QuantitativeReference;
import org.openlca.ilcd.productmodel.Connector;
import org.openlca.ilcd.productmodel.ConsumedBy;
import org.openlca.ilcd.productmodel.ProcessNode;
import org.openlca.ilcd.productmodel.Product;
import org.openlca.ilcd.productmodel.ProductModel;
import org.openlca.ilcd.util.ProcessInfoExtension;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ProductSystemConverter implements Converter<Process> {

	private final Util util;

	ProductSystemConverter(Util util) {
		this.util = util;
	}

	@Override
	public Process run(JsonObject obj) {
		if (obj == null)
			return null;
		JsonObject refProcess = In.getObject(obj, "referenceProcess");
		if (refProcess == null)
			return null;
		if (In.getObject(obj, "referenceExchange") == null)
			return null;
		Process process = new Process();
		process.processInfo = new ProcessInfo();
		process.processInfo.dataSetInfo = createDataSetInfo(obj);
		process.processInfo.quantitativeReference = createQuantitativeReference(obj);
		process.processInfo.other = createProductModel(obj);
		process.exchanges.add(createExchange(obj));
		new ProcessInfoExtension(process.processInfo).setModelRefProcess(In.getString(refProcess, "@id"));
		return process;
	}

	private DataSetInfo createDataSetInfo(JsonObject obj) {
		DataSetInfo info = new DataSetInfo();
		info.uuid = In.getString(obj, "@id");
		info.name = new ProcessName();
		util.setLangString(info.name.name, In.getString(obj, "name") + " (product system)");
		util.setLangString(info.comment, In.getString(obj, "description"));
		info.classifications.add(util.createClassification(obj));
		return info;
	}

	private QuantitativeReference createQuantitativeReference(JsonObject obj) {
		QuantitativeReference qRef = new QuantitativeReference();
		qRef.type = QuantitativeReferenceType.REFERENCE_FLOWS;
		qRef.referenceFlows.add(1);
		return qRef;
	}

	private Exchange createExchange(JsonObject obj) {
		JsonObject refExchange = In.getObject(obj, "referenceExchange");
		JsonObject flow = In.getObject(refExchange, "flow");
		Exchange exchange = new Exchange();
		exchange.id = 1;
		exchange.direction = ExchangeDirection.OUTPUT;
		exchange.flow = util.createRef(flow);
		double refAmount = In.getDouble(obj, "targetAmount");
		JsonObject unit = In.getObject(obj, "targetUnit");
		double unitFactor = In.getDouble(unit, "conversionFactor", 1);
		JsonObject property = In.getObject(obj, "targetFlowProperty");
		double propertyFactor = util.getPropertyFactor(flow, property);
		refAmount = refAmount * unitFactor;
		refAmount = refAmount / propertyFactor;
		exchange.meanAmount = refAmount;
		exchange.resultingAmount = refAmount;
		return exchange;
	}

	private Other createProductModel(JsonObject obj) {
		ProductModel model = new ProductModel();
		model.setName(In.getString(obj, "name"));
		Other other = new Other();
		other.any.add(model);
		model.getNodes().addAll(createProcesses(obj));
		model.getConnections().addAll(createLinks(obj));
		return other;
	}

	private List<ProcessNode> createProcesses(JsonObject obj) {
		List<ProcessNode> nodes = new ArrayList<>();
		JsonArray processes = In.getArray(obj, "processes");
		if (processes == null)
			return nodes;
		for (JsonElement elem : processes) {
			JsonObject process = elem.getAsJsonObject();
			Ref ref = util.createRef(process);
			ProcessNode node = new ProcessNode();
			node.setId(ref.uuid);
			node.setName(ref.name.get(0).value);
			node.setUri(ref.uri);
			node.setUuid(node.getId());
			nodes.add(node);
		}
		return nodes;
	}

	private List<Connector> createLinks(JsonObject obj) {
		List<Connector> connections = new ArrayList<>();
		JsonArray links = In.getArray(obj, "processLinks");
		if (links == null)
			return connections;
		int c = 1;
		for (JsonElement elem : links) {
			JsonObject link = elem.getAsJsonObject();
			Connector connector = new Connector();
			connector.setId(Integer.toString(c++));
			JsonObject provider = In.getObject(link, "provider");
			if (provider == null)
				continue;
			connector.setOrigin(In.getString(provider, "@id"));
			JsonObject flow = In.getObject(link, "flow");
			if (flow == null)
				continue;
			Product product = new Product();
			product.setUuid(In.getString(flow, "@id"));
			product.setName(In.getString(flow, "name"));
			JsonObject recipient = In.getObject(link, "process");
			if (recipient == null)
				continue;
			ConsumedBy consumedBy = new ConsumedBy();
			consumedBy.setFlowUUID(product.getUuid());
			consumedBy.setProcessId(In.getString(recipient, "@id"));
			product.setConsumedBy(consumedBy);
			connector.getProducts().add(product);
			connections.add(connector);
		}
		return connections;
	}

}
