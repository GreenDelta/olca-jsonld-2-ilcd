package org.openlca.convert.jsonld.ilcd;

import junit.framework.Assert;

import org.junit.Test;
import org.openlca.ilcd.commons.ExchangeDirection;
import org.openlca.ilcd.commons.QuantitativeReferenceType;
import org.openlca.ilcd.processes.DataSetInfo;
import org.openlca.ilcd.processes.Exchange;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.processes.QuantitativeReference;
import org.openlca.ilcd.productmodel.Connector;
import org.openlca.ilcd.productmodel.ProcessNode;
import org.openlca.ilcd.productmodel.Product;
import org.openlca.ilcd.productmodel.ProductModel;

import com.google.gson.JsonObject;

public class ProductSystemConversionTest {

	@Test
	public void minimal() {
		Process process = convert("minimal", "20f7f9d5-09ee-4992-b312-322de19fa988");
		Assert.assertEquals("20f7f9d5-09ee-4992-b312-322de19fa988", process.getUUID());
		TestUtils.assertLangString(process.getName(), "System (product system)");
		DataSetInfo info = process.processInfo.dataSetInfo;
		TestUtils.assertNull(process.adminInfo, info.comment, info.classifications.get(0).categories);
		QuantitativeReference qRef = process.processInfo.quantitativeReference;
		Assert.assertEquals(1, qRef.referenceFlows.size());
		Assert.assertEquals(1, (int) qRef.referenceFlows.get(0));
		Assert.assertEquals(QuantitativeReferenceType.REFERENCE_FLOWS, qRef.type);
		Assert.assertEquals(1, process.exchanges.size());
		assertExchange(process.exchanges.get(0), "cd8992ed-cede-42f3-b0e3-17fe93faa6d0", "Product");
		Assert.assertEquals(1, process.processInfo.other.any.size());
		ProductModel model = (ProductModel) process.processInfo.other.any.get(0);
		Assert.assertEquals("System", model.getName());
		Assert.assertEquals(1, model.getNodes().size());
		assertProcess(model.getNodes().get(0), "14454cd2-fe8e-40c1-bcf0-97a0466641c5", "Process");
		Assert.assertEquals(0, model.getConnections().size());
	}

	@Test
	public void complete() {
		Process process = convert("complete", "6deb6d95-9108-4b1e-a79f-d248bc712b5b");
		Assert.assertEquals("6deb6d95-9108-4b1e-a79f-d248bc712b5b", process.getUUID());
		TestUtils.assertLangString(process.getName(), "System (product system)");
		DataSetInfo info = process.processInfo.dataSetInfo;
		TestUtils.assertNull(process.adminInfo);
		TestUtils.assertLangString(info.comment, "This is a complete product system");
		Assert.assertEquals(1, info.classifications.size());
		TestUtils.assertClassification(info.classifications.get(0),
				"Main", "cd19a26a-745c-35f6-8aa6-f890b4ab2219",
				"Sub", "b62a1d0a-c073-31dd-b6bf-b8e06557d4de");
		QuantitativeReference qRef = process.processInfo.quantitativeReference;
		Assert.assertEquals(1, qRef.referenceFlows.size());
		Assert.assertEquals(1, (int) qRef.referenceFlows.get(0));
		Assert.assertEquals(QuantitativeReferenceType.REFERENCE_FLOWS, qRef.type);
		Assert.assertEquals(1, process.exchanges.size());
		assertExchange(process.exchanges.get(0), "eca471d9-d26b-4357-ba80-f812a4b64901", "Product");
		Assert.assertEquals(1, process.processInfo.other.any.size());
		ProductModel model = (ProductModel) process.processInfo.other.any.get(0);
		Assert.assertEquals("System", model.getName());
		Assert.assertEquals(2, model.getNodes().size());
		assertProcess(model.getNodes().get(0), "c80bc041-48ec-4182-be17-fe5e292c1a93", "Process");
		assertProcess(model.getNodes().get(1), "d83f61ba-1866-4be2-9270-827b847008d5", "Process 2");
		Assert.assertEquals(1, model.getConnections().size());
		assertLink(model.getConnections().get(0), 1, "d83f61ba-1866-4be2-9270-827b847008d5",
				"e6ee47cf-037a-4ab4-92ab-656868a5be32", "c80bc041-48ec-4182-be17-fe5e292c1a93");
	}

	private void assertExchange(Exchange exchange, String flowRefId, String flowName) {
		Assert.assertEquals(1, exchange.id);
		Assert.assertEquals(ExchangeDirection.OUTPUT, exchange.direction);
		Assert.assertEquals(flowRefId, exchange.flow.uuid);
		TestUtils.assertLangString(exchange.flow.name, flowName);
		Assert.assertEquals(1d, exchange.meanAmount);
		Assert.assertEquals(exchange.meanAmount, exchange.resultingAmount);

	}

	private void assertProcess(ProcessNode node, String refId, String name) {
		Assert.assertEquals(refId, node.getId());
		Assert.assertEquals(node.getId(), node.getUuid());
		Assert.assertEquals(name, node.getName());
	}

	private void assertLink(Connector connector, int id, String providerId, String productId, String recipientId) {
		Assert.assertEquals(Integer.toString(id), connector.getId());
		Assert.assertEquals(providerId, connector.getOrigin());
		Assert.assertEquals(1, connector.getProducts().size());
		Product product = connector.getProducts().get(0);
		Assert.assertEquals(productId, product.getUuid());
		Assert.assertEquals(productId, product.getConsumedBy().getFlowUUID());
		Assert.assertEquals(recipientId, product.getConsumedBy().getProcessId());
	}

	private Process convert(String testType, String id) {
		Util util = TestUtils.createUtil(testType);
		ProductSystemConverter converter = new ProductSystemConverter(util);
		JsonObject system = util.config.store.get("ProductSystem", id);
		return converter.run(system);
	}

}
