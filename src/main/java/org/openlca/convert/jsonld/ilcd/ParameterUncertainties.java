package org.openlca.convert.jsonld.ilcd;

import org.openlca.ilcd.commons.UncertaintyDistribution;
import org.openlca.ilcd.processes.Parameter;

import com.google.gson.JsonObject;

class ParameterUncertainties {

	static void map(JsonObject uncertainty, Parameter parameter) {
		if (uncertainty == null)
			return;
		String type = In.getString(uncertainty, "distributionType");
		if (type == null)
			return;
		switch (type) {
		case "LOG_NORMAL_DISTRIBUTION":
			mapLogNormal(uncertainty, parameter);
			break;
		case "NORMAL_DISTRIBUTION":
			mapNormal(uncertainty, parameter);
			break;
		case "TRIANGLE_DISTRIBUTION":
			mapTriangle(uncertainty, parameter);
			break;
		case "UNIFORM_DISTRIBUTION":
			mapUniform(uncertainty, parameter);
			break;
		default:
			break;
		}
	}

	private static void mapLogNormal(JsonObject uncertainty, Parameter parameter) {
		Double std = In.getDouble(uncertainty, "geomSd");
		if (std == null)
			return;
		parameter.dispersion = std;
		parameter.distribution = UncertaintyDistribution.LOG_NORMAL;
	}

	private static void mapNormal(JsonObject uncertainty, Parameter parameter) {
		Double std = In.getDouble(uncertainty, "sd");
		if (std == null)
			return;
		parameter.dispersion = std;
		parameter.distribution = UncertaintyDistribution.NORMAL;
	}

	private static void mapTriangle(JsonObject uncertainty, Parameter parameter) {
		Double min = In.getDouble(uncertainty, "minimum");
		Double max = In.getDouble(uncertainty, "maximum");
		if (min == null || max == null)
			return;
		parameter.min = min;
		parameter.max = max;
		parameter.distribution = UncertaintyDistribution.TRIANGULAR;
	}

	private static void mapUniform(JsonObject uncertainty, Parameter parameter) {
		Double min = In.getDouble(uncertainty, "minimum");
		Double max = In.getDouble(uncertainty, "maximum");
		if (min == null || max == null)
			return;
		parameter.min = min;
		parameter.max = max;
		parameter.distribution = UncertaintyDistribution.UNIFORM;
	}

}
