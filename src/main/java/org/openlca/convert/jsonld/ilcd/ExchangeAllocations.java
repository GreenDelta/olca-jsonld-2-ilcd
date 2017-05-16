package org.openlca.convert.jsonld.ilcd;

import java.util.Map;

import org.openlca.ilcd.processes.AllocationFactor;
import org.openlca.ilcd.processes.Exchange;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

class ExchangeAllocations {

	static void map(JsonObject process, Map<String, Exchange> exchangeMap) {
		JsonArray factors = In.getArray(process, "allocationFactors");
		if (factors == null)
			return;
		for (JsonElement elem : factors) {
			JsonObject factor = elem.getAsJsonObject();
			JsonObject product = In.getObject(factor, "product");
			if (product == null)
				continue;
			JsonObject exchange = In.getObject(factor, "exchange");
			String type = In.getString(factor, "allocationType");
			if (exchange == null && !"PHYSICAL_ALLOCATION".equals(type))
				continue;
			if (exchange == null) {
				exchange = product;
			}
			double value = In.getDouble(factor, "value", 0);
			addFactor(value, exchange, product, exchangeMap);
		}
	}

	private static void addFactor(double value, JsonObject exchange, JsonObject product, Map<String, Exchange> map) {
		AllocationFactor factor = new AllocationFactor();
		String productId = In.getString(product, "@id");
		String exchangeId = In.getString(exchange, "@id");
		factor.fraction = value * 100;
		factor.productExchangeId = map.get(productId).id;
		map.get(exchangeId).add(factor);
	}

}
