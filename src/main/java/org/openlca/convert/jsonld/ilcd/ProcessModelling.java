package org.openlca.convert.jsonld.ilcd;

import org.openlca.ilcd.commons.ModellingApproach;
import org.openlca.ilcd.commons.ModellingPrinciple;
import org.openlca.ilcd.commons.ProcessType;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.commons.ReviewType;
import org.openlca.ilcd.processes.Method;
import org.openlca.ilcd.processes.Modelling;
import org.openlca.ilcd.processes.Representativeness;
import org.openlca.ilcd.processes.Review;
import org.openlca.ilcd.processes.Validation;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

class ProcessModelling {

	private final Util util;

	ProcessModelling(Util util) {
		this.util = util;
	}

	Modelling create(JsonObject obj) {
		Modelling modelling = new Modelling();
		JsonObject doc = In.getObject(obj, "processDocumentation");
		modelling.method = createLciMethod(obj, doc);
		modelling.representativeness = createRepresentativeness(doc);
		modelling.validation = createValidation(doc);
		return modelling;
	}

	private Method createLciMethod(JsonObject obj, JsonObject doc) {
		Method method = new Method();
		method.processType = mapProcessType(obj);
		method.principle = ModellingPrinciple.OTHER;
		if (doc != null) {
			util.setLangString(method.principleComment, In.getString(doc, "inventoryMethodDescription"));
			util.setLangString(method.constants, In.getString(doc, "modelingConstantsDescription"));
		}
		ModellingApproach allocation = mapAllocationMethod(obj);
		if (allocation != null) {
			method.approaches.add(allocation);
		}
		return method;
	}

	private ProcessType mapProcessType(JsonObject obj) {
		String type = In.getString(obj, "processType");
		if (type == null)
			return ProcessType.UNIT_PROCESS_BLACK_BOX;
		switch (type) {
		case "LCI_RESULT":
			return ProcessType.LCI_RESULT;
		case "UNIT_PROCESS":
		default:
			return ProcessType.UNIT_PROCESS_BLACK_BOX;
		}
	}

	private ModellingApproach mapAllocationMethod(JsonObject obj) {
		String method = In.getString(obj, "defaultAllocationMethod");
		if (method == null)
			return null;
		switch (method) {
		case "CAUSAL_ALLOCATION":
			return ModellingApproach.ALLOCATION_OTHER_EXPLICIT_ASSIGNMENT;
		case "ECONOMIC_ALLOCATION":
			return ModellingApproach.ALLOCATION_MARKET_VALUE;
		case "PHYSICAL_ALLOCATION":
			return ModellingApproach.ALLOCATION_PHYSICAL_CAUSALITY;
		default:
			return null;
		}
	}

	private Representativeness createRepresentativeness(JsonObject doc) {
		if (doc == null)
			return null;
		Representativeness represent = new Representativeness();
		util.setLangString(represent.completeness, In.getString(doc, "completenessDescription"));
		util.setLangString(represent.completenessComment, "None.");
		util.setLangString(represent.dataSelection, In.getString(doc, "dataSelectionDescription"));
		util.setLangString(represent.dataSelectionComment, "None.");
		util.setLangString(represent.dataTreatment, In.getString(doc, "dataTreatmentDescription"));
		util.setLangString(represent.samplingProcedure, In.getString(doc, "samplingDescription"));
		util.setLangString(represent.dataCollectionPeriod, In.getString(doc, "dataCollectionDescription"));
		JsonArray sources = In.getArray(doc, "sources");
		if (sources == null)
			return represent;
		for (JsonElement source : sources) {
			Ref ref = util.createRef(source.getAsJsonObject());
			represent.sources.add(ref);
		}
		return represent;
	}

	private Validation createValidation(JsonObject doc) {
		if (doc == null)
			return null;
		JsonObject reviewer = In.getObject(doc, "reviewer");
		String reviewDetails = In.getString(doc, "reviewDetails");
		if (reviewer == null && reviewDetails == null)
			return null;
		Review review = new Review();
		review.type = ReviewType.NOT_REVIEWED;
		if (reviewer != null) {
			Ref ref = util.createRef(reviewer);
			review.reviewers.add(ref);
		}
		util.setLangString(review.details, reviewDetails);
		Validation validation = new Validation();
		validation.reviews.add(review);
		return validation;
	}

}
