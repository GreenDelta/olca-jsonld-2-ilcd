package org.openlca.convert.jsonld.ilcd;

import org.openlca.ilcd.processes.Parameter;
import org.openlca.ilcd.util.ParameterExtension;

import com.google.gson.JsonObject;

class ParameterConverter {

	static final String SCOPE_GLOBAL = "global";
	static final String SCOPE_PROCESS = "process";
	static final String SCOPE_UNSPECIFIED = "unspecified";
	private final Util util;

	ParameterConverter(Util util) {
		this.util = util;
	}

	Parameter run(JsonObject obj, String scope) {
		if (obj == null)
			return null;
		Parameter param = new Parameter();
		param.name = In.getString(obj, "name");
		if (param.name == null)
			return null;
		param.formula = In.getString(obj, "formula");
		param.mean = In.getDouble(obj, "value");
		if (param.mean == null && param.formula == null)
			return null;
		util.setLangString(param.comment, In.getString(obj, "description"));
		if (scope != SCOPE_GLOBAL && scope != SCOPE_PROCESS) {
			scope = SCOPE_UNSPECIFIED;
		}
		new ParameterExtension(param).setScope(scope);
		ParameterUncertainties.map(In.getObject(obj, "uncertainty"), param);
		return param;
	}

}
