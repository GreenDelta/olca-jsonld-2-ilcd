package org.openlca.convert.jsonld.ilcd;

import org.openlca.ilcd.commons.ExchangeDirection;
import org.openlca.ilcd.processes.Exchange;
import org.openlca.ilcd.processes.Parameter;
import org.openlca.ilcd.processes.ParameterSection;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.util.ExchangeExtension;

import com.google.gson.JsonObject;

class ExchangeConverter {

	private final Util util;

	ExchangeConverter(Util util) {
		this.util = util;
	}

	Exchange run(JsonObject obj, Process process) {
		if (obj == null)
			return null;
		Exchange exchange = new Exchange();
		util.setLangString(exchange.comment, In.getString(obj, "description"));
		exchange.flow = util.createRef(In.getObject(obj, "flow"));
		exchange.direction = In.getBool(obj, "input") ? ExchangeDirection.INPUT : ExchangeDirection.OUTPUT;
		exchange.resultingAmount = calculateAmount(obj);
		mapExtensions(obj, exchange);
		mapFormula(obj, exchange, process);
		ExchangeUncertainties.map(In.getObject(obj, "uncertainty"), exchange);
		return exchange;
	}

	private double calculateAmount(JsonObject obj) {
		double propertyFactor = util.getPropertyFactor(obj);
		double unitFactor = util.getUnitFactor(obj);
		double amount = In.getDouble(obj, "amount", 0);
		return amount * propertyFactor * unitFactor;
	}

	private void mapExtensions(JsonObject obj, Exchange exchange) {
		ExchangeExtension ext = new ExchangeExtension(exchange);
		if (In.getBool(obj, "avoidedProduct")) {
			exchange.direction = ExchangeDirection.OUTPUT;
			ext.setAvoidedProduct(true);
		}
		ext.setAmount(In.getDouble(obj, "amount", 0));
		ext.setBaseUncertainty(In.getDouble(obj, "baseUncertainty", 0));
		ext.setPedigreeUncertainty(In.getString(obj, "dqEntry"));
		String formula = In.getString(obj, "amountFormula");
		if (formula != null) {
			ext.setFormula(formula);
		}
		JsonObject property = In.getObject(obj, "flowProperty");
		if (property != null) {
			ext.setPropertyId(In.getString(property, "@id"));
		}
		JsonObject unit = In.getObject(obj, "unit");
		if (unit != null) {
			ext.setUnitId(In.getString(unit, "@id"));
		}
		JsonObject defaultProvider = In.getObject(obj, "defaultProvider");
		if (defaultProvider != null) {
			ext.setDefaultProvider(In.getString(defaultProvider, "@id"));
		}
	}

	private void mapFormula(JsonObject obj, Exchange exchange, Process process) {
		String formula = In.getString(obj, "amountFormula");
		if (formula == null) {
			exchange.meanAmount = exchange.resultingAmount;
			return;
		}
		String paramName = "temp_olca_param" + getParamSize(process);
		exchange.variable = paramName;
		exchange.meanAmount = 1d;
		Parameter parameter = new Parameter();
		parameter.formula = In.getString(obj, "amountFormula");
		parameter.mean = exchange.resultingAmount;
		parameter.name = paramName;
		addParameter(parameter, process);
	}

	private int getParamSize(Process process) {
		ParameterSection section = process.processInfo.parameters;
		if (section == null)
			return 0;
		return section.parameters.size();
	}

	private void addParameter(Parameter parameter, Process process) {
		if (process.processInfo.parameters == null) {
			process.processInfo.parameters = new ParameterSection();
		}
		process.processInfo.parameters.parameters.add(parameter);
	}

}
