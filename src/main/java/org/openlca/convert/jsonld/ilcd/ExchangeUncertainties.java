package org.openlca.convert.jsonld.ilcd;

import org.openlca.ilcd.commons.UncertaintyDistribution;
import org.openlca.ilcd.processes.Exchange;

import com.google.gson.JsonObject;

class ExchangeUncertainties {

	static void map(JsonObject uncertainty, Exchange exchange) {
		if (uncertainty == null)
			return;
		String type = In.getString(uncertainty, "distributionType");
		if (type == null)
			return;
		switch (type) {
		case "LOG_NORMAL_DISTRIBUTION":
			mapLogNormal(uncertainty, exchange);
			break;
		case "NORMAL_DISTRIBUTION":
			mapNormal(uncertainty, exchange);
			break;
		case "TRIANGLE_DISTRIBUTION":
			mapTriangle(uncertainty, exchange);
			break;
		case "UNIFORM_DISTRIBUTION":
			mapUniform(uncertainty, exchange);
			break;
		default:
			break;
		}
	}

	private static void mapLogNormal(JsonObject uncertainty, Exchange exchange) {
		Double std = In.getDouble(uncertainty, "geomSd");
		if (std == null)
			return;
		exchange.relativeStandardDeviation95In = std;
		exchange.uncertaintyDistribution = UncertaintyDistribution.LOG_NORMAL;
	}

	private static void mapNormal(JsonObject uncertainty, Exchange exchange) {
		Double std = In.getDouble(uncertainty, "sd");
		if (std == null)
			return;
		exchange.relativeStandardDeviation95In = std;
		exchange.uncertaintyDistribution = UncertaintyDistribution.NORMAL;
	}

	private static void mapTriangle(JsonObject uncertainty, Exchange exchange) {
		Double min = In.getDouble(uncertainty, "minimum");
		Double max = In.getDouble(uncertainty, "maximum");
		if (min == null || max == null)
			return;
		exchange.minimumAmount = min;
		exchange.maximumAmount = max;
		exchange.uncertaintyDistribution = UncertaintyDistribution.TRIANGULAR;
	}

	private static void mapUniform(JsonObject uncertainty, Exchange exchange) {
		Double min = In.getDouble(uncertainty, "minimum");
		Double max = In.getDouble(uncertainty, "maximum");
		if (min == null || max == null)
			return;
		exchange.minimumAmount = min;
		exchange.maximumAmount = max;
		exchange.uncertaintyDistribution = UncertaintyDistribution.UNIFORM;
	}

}
