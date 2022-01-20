package org.openlca.convert.jsonld.ilcd;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.ExchangeDirection;
import org.openlca.ilcd.commons.ModellingApproach;
import org.openlca.ilcd.commons.ModellingPrinciple;
import org.openlca.ilcd.commons.ProcessType;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.commons.ReviewType;
import org.openlca.ilcd.commons.Time;
import org.openlca.ilcd.commons.UncertaintyDistribution;
import org.openlca.ilcd.processes.AdminInfo;
import org.openlca.ilcd.processes.AllocationFactor;
import org.openlca.ilcd.processes.DataSetInfo;
import org.openlca.ilcd.processes.Exchange;
import org.openlca.ilcd.processes.Geography;
import org.openlca.ilcd.processes.Method;
import org.openlca.ilcd.processes.Modelling;
import org.openlca.ilcd.processes.Parameter;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.processes.ProcessInfo;
import org.openlca.ilcd.processes.Representativeness;
import org.openlca.ilcd.processes.Review;
import org.openlca.ilcd.processes.Technology;
import org.openlca.ilcd.util.ExchangeExtension;
import org.openlca.ilcd.util.TimeExtension;

import com.google.gson.JsonObject;

public class ProcessConversionTest {

	@Test
	public void minimal() {
		Process process = convert("minimal", "14454cd2-fe8e-40c1-bcf0-97a0466641c5");
		Assert.assertEquals("14454cd2-fe8e-40c1-bcf0-97a0466641c5", process.getUUID());
		TestUtils.assertLangString(process.getName(), "Process");
		Assert.assertEquals("00.00.000", process.adminInfo.publication.version);
		Assert.assertEquals("2017-05-16T13:58:43.940+02:00", process.adminInfo.dataEntry.timeStamp.toString());
		Assert.assertEquals(1, process.exchanges.size());
		assertExchange(findExchange(process.exchanges, "Product"), false, 1, true, false);
		ProcessInfo pInfo = process.processInfo;
		DataSetInfo dsInfo = pInfo.dataSetInfo;
		AdminInfo adminInfo = process.adminInfo;
		Assert.assertEquals(false, (boolean) adminInfo.publication.copyright);
		TestUtils.assertNull(pInfo.time.referenceYear, pInfo.time.validUntil, pInfo.geography, pInfo.technology,
				pInfo.parameters, dsInfo.classifications.get(0).categories);
		TestUtils.assertNull(adminInfo.publication.owner, adminInfo.publication.republication,
				adminInfo.dataGenerator, adminInfo.commissionerAndGoal, adminInfo.dataEntry.documentor);
		Method method = process.modelling.method;
		Assert.assertEquals(ProcessType.LCI_RESULT, method.processType);
		Assert.assertEquals(ModellingPrinciple.OTHER, method.principle);
		TestUtils.assertNull(method.principleComment, method.constants, method.approaches);
		Representativeness represent = process.modelling.representativeness;
		TestUtils.assertNull(represent.completeness, represent.dataSelection, represent.dataTreatment,
				represent.samplingProcedure, represent.dataCollectionPeriod, represent.sources);
		TestUtils.assertLangString(represent.completenessComment, "None.");
		TestUtils.assertLangString(represent.dataSelectionComment, "None.");
		TestUtils.assertNull(process.modelling.validation);
	}

	@Test
	public void complete() {
		Process process = convert("complete", "c80bc041-48ec-4182-be17-fe5e292c1a93");
		Assert.assertEquals("c80bc041-48ec-4182-be17-fe5e292c1a93", process.getUUID());
		TestUtils.assertLangString(process.getName(), "Process");
		Assert.assertEquals("00.00.033", process.adminInfo.publication.version);
		Assert.assertEquals("2017-05-16T15:46:34.393+02:00", process.adminInfo.dataEntry.timeStamp.toString());
		ProcessInfo pInfo = process.processInfo;
		DataSetInfo dsInfo = pInfo.dataSetInfo;
		TestUtils.assertLangString(dsInfo.comment, "This is a complete process");
		Assert.assertEquals(1, dsInfo.classifications.size());
		TestUtils.assertClassification(dsInfo.classifications.get(0),
				"Main", "c15ba4cf-a94f-368f-ad08-74b8f5822f21",
				"Sub", "c5e7fdc0-9678-3e01-a649-486eec8186a9");
		assertTime(pInfo.time, TestUtils.toDate(1, 5, 2017), TestUtils.toDate(31, 5, 2017));
		assertGeography(pInfo.geography);
		assertTechnology(pInfo.technology);
		assertParameters(pInfo.parameters.parameters);
		assertAdminInfo(process.adminInfo);
		assertModelling(process.modelling);
		assertExchanges(process.exchanges);
	}

	private void assertTime(Time time, Date start, Date end) {
		TimeExtension ext = new TimeExtension(time);
		TestUtils.assertDate(start, ext.getStartDate());
		TestUtils.assertDate(end, ext.getEndDate());
		Calendar cal = Calendar.getInstance();
		cal.setTime(start);
		Assert.assertEquals(cal.get(Calendar.YEAR), (int) time.referenceYear);
		cal.setTime(end);
		Assert.assertEquals(cal.get(Calendar.YEAR), (int) time.validUntil);
	}

	private void assertGeography(Geography geography) {
		TestUtils.assertLangString(geography.location.description, "The Country Germany");
		Assert.assertEquals("DE", geography.location.code);
		Assert.assertEquals("51.11,9.85", geography.location.latitudeAndLongitude);
	}

	private void assertTechnology(Technology technology) {
		TestUtils.assertLangString(technology.description, "A technology was used");
	}

	private void assertParameters(List<Parameter> params) {
		Assert.assertEquals(7, params.size());
		Parameter p0 = findParameter(params, "p_0");
		assertParameter(p0, null, 1, "This is p_0");
		assertParameterUncertainty(params.get(0), null, null, null);
		Parameter p1 = findParameter(params, "p_1");
		assertParameter(p1, "2*p_0", 2, "This is p_1");
		assertParameterUncertainty(p1, null, null, null);
		Parameter p2 = findParameter(params, "p_2");
		assertParameter(p2, null, 1, null);
		assertParameterUncertainty(p2, UncertaintyDistribution.LOG_NORMAL, 2d, null);
		Parameter p3 = findParameter(params, "p_3");
		assertParameter(p3, null, 1, null);
		assertParameterUncertainty(p3, UncertaintyDistribution.NORMAL, 1d, null);
		Parameter p4 = findParameter(params, "p_4");
		assertParameter(p4, null, 1, null);
		assertParameterUncertainty(p4, UncertaintyDistribution.TRIANGULAR, 1d, 3d);
		Parameter p5 = findParameter(params, "p_5");
		assertParameter(p5, null, 1, null);
		assertParameterUncertainty(p5, UncertaintyDistribution.UNIFORM, 1d, 2d);
		Parameter p6 = findParameter(params, "temp_olca_param6");
		assertParameter(p6, "2*p_0*glob", 4, null);
		assertParameterUncertainty(p6, null, null, null);
	}

	private Parameter findParameter(List<Parameter> params, String name) {
		for (Parameter parameter : params)
			if (parameter.name.equals(name))
				return parameter;
		return null;
	}

	private void assertParameter(Parameter parameter, String formula, double value, String description) {
		Assert.assertEquals(formula, parameter.formula);
		Assert.assertEquals(value, parameter.mean, 0d);
		if (description == null) {
			TestUtils.assertNull(parameter.comment);
		} else {
			TestUtils.assertLangString(parameter.comment, description);
		}
	}

	private void assertParameterUncertainty(Parameter parameter, UncertaintyDistribution type, Double v1, Double v2) {
		Assert.assertEquals(type, parameter.distribution);
		if (type == null) {
			TestUtils.assertNull(parameter.dispersion, parameter.min, parameter.max);
			return;
		}
		switch (parameter.distribution) {
		case LOG_NORMAL:
		case NORMAL:
			Assert.assertEquals((double) v1, (double) parameter.dispersion, 0d);
			TestUtils.assertNull(parameter.min, parameter.max);
			break;
		case TRIANGULAR:
		case UNIFORM:
			Assert.assertEquals((double) v1, (double) parameter.min, 0d);
			Assert.assertEquals((double) v2, (double) parameter.max, 0d);
			TestUtils.assertNull(parameter.dispersion);
			break;
		default:
			TestUtils.assertNull(parameter.dispersion, parameter.min, parameter.max);
			break;
		}
	}

	private void assertAdminInfo(AdminInfo info) {
		Assert.assertEquals(true, (boolean) info.publication.copyright);
		assertRef(info.publication.owner, DataSetType.CONTACT, "9fa11715-b484-436a-b01d-6d2af234c1c7", "Actor");
		assertRef(info.publication.republication, DataSetType.SOURCE, "c2f54cb9-d152-4137-8ad7-871598478578", "Source");
		List<Ref> contacts = info.dataGenerator.contacts;
		Assert.assertEquals(1, contacts.size());
		assertRef(contacts.get(0), DataSetType.CONTACT, "9fa11715-b484-436a-b01d-6d2af234c1c7", "Actor");
		TestUtils.assertLangString(info.commissionerAndGoal.intendedApplications, "Test case");
		TestUtils.assertLangString(info.commissionerAndGoal.project, "Json 2 ILCD");
		assertRef(info.dataEntry.documentor, DataSetType.CONTACT, "9fa11715-b484-436a-b01d-6d2af234c1c7", "Actor");
	}

	private void assertModelling(Modelling modelling) {
		Method method = modelling.method;
		Assert.assertEquals(ProcessType.UNIT_PROCESS_BLACK_BOX, method.processType);
		Assert.assertEquals(ModellingPrinciple.OTHER, method.principle);
		TestUtils.assertLangString(method.principleComment, "Unknown");
		TestUtils.assertLangString(method.constants, "None");
		Assert.assertEquals(1, method.approaches.size());
		Assert.assertEquals(ModellingApproach.ALLOCATION_PHYSICAL_CAUSALITY, method.approaches.get(0));
		Representativeness represent = modelling.representativeness;
		TestUtils.assertLangString(represent.completeness, "Complete");
		TestUtils.assertLangString(represent.completenessComment, "None.");
		TestUtils.assertLangString(represent.dataSelection, "Manual");
		TestUtils.assertLangString(represent.dataSelectionComment, "None.");
		TestUtils.assertLangString(represent.dataTreatment, "Good");
		TestUtils.assertLangString(represent.samplingProcedure, "Random");
		TestUtils.assertLangString(represent.dataCollectionPeriod, "April 2017");
		Assert.assertEquals(2, represent.sources.size());
		assertRef(represent.sources.get(0), DataSetType.SOURCE, "d3331847-cff6-4dd1-87f0-b3759c492b3a", "Source 3");
		assertRef(represent.sources.get(1), DataSetType.SOURCE, "8d9f948a-ea35-4956-bb63-32aa37bdf670", "Source 2");
		Assert.assertEquals(1, modelling.validation.reviews.size());
		Review review = modelling.validation.reviews.get(0);
		Assert.assertEquals(ReviewType.NOT_REVIEWED, review.type);
		Assert.assertEquals(1, review.reviewers.size());
		assertRef(review.reviewers.get(0), DataSetType.CONTACT, "9fa11715-b484-436a-b01d-6d2af234c1c7", "Actor");
	}

	private void assertRef(Ref ref, DataSetType type, String uuid, String name) {
		Assert.assertEquals(type, ref.type);
		Assert.assertEquals(uuid, ref.uuid);
		TestUtils.assertLangString(ref.name, name);
	}

	private void assertExchanges(List<Exchange> exchanges) {
		Assert.assertEquals(8, exchanges.size());
		Exchange e1 = findExchange(exchanges, "Flow");
		assertExchange(e1, true, 1d, false, true);
		Assert.assertEquals("(5;4;3;2;1)", new ExchangeExtension(e1).getPedigreeUncertainty());
		assertExchangeUncertainty(e1, UncertaintyDistribution.LOG_NORMAL, 1.5d, null);
		assertAllocation(e1, 0.2, 0.8);
		Exchange e2 = findExchange(exchanges, "Flow 2");
		assertExchange(e2, true, 2d, false, true);
		TestUtils.assertLangString(e2.comment, "Flow 2");
		assertExchangeUncertainty(e2, UncertaintyDistribution.NORMAL, 1d, null);
		assertAllocation(e2, 0.3, 0.7);
		Exchange e3 = findExchange(exchanges, "Flow 3");
		assertExchange(e3, true, 3d, false, true);
		assertExchangeUncertainty(e3, UncertaintyDistribution.TRIANGULAR, 1d, 3d);
		assertAllocation(e3, 0.4, 0.6);
		Exchange e4 = findExchange(exchanges, "Flow 4");
		assertExchange(e4, true, 4d, false, true);
		assertExchangeUncertainty(e4, UncertaintyDistribution.UNIFORM, 2d, 4d);
		assertAllocation(e4, 0.5, 0.5);
		Exchange e5 = findExchange(exchanges, "Input");
		assertExchange(e5, true, 5d, false, true);
		Assert.assertEquals("d83f61ba-1866-4be2-9270-827b847008d5", new ExchangeExtension(e5).getDefaultProvider());
		assertExchangeUncertainty(e5, null, null, null);
		assertAllocation(e5, 0.6, 0.4);
		Exchange e6 = findExchange(exchanges, "Product");
		assertExchange(e6, false, 1d, true, true);
		assertExchangeUncertainty(e6, null, null, null);
		assertAllocation(e6, 0.7);
		Exchange e7 = findExchange(exchanges, "Product 2");
		assertExchange(e7, false, 1d, false, true);
		assertExchangeUncertainty(e7, null, null, null);
		assertAllocation(e7, 0.3);
		Exchange e8 = findExchange(exchanges, "Avoided");
		assertExchange(e8, false, 4d, false, true);
		ExchangeExtension ext = new ExchangeExtension(e8);
		Assert.assertEquals(true, ext.isAvoidedProduct());
		Assert.assertEquals("2*p_0*glob", ext.getFormula());
		assertExchangeUncertainty(e8, null, null, null);
		assertAllocation(e8, 0.1, 0.9);
	}

	private void assertExchange(Exchange exchange, boolean input, double amount, boolean ref, boolean checkIds) {
		Assert.assertEquals(input ? ExchangeDirection.INPUT : ExchangeDirection.OUTPUT, exchange.direction);
		Assert.assertEquals(amount, exchange.resultingAmount, 0d);
		ExchangeExtension ext = new ExchangeExtension(exchange);
		if (checkIds) {
			Assert.assertEquals("4cb61669-4853-4556-9667-4a4a22d3f169", ext.getPropertyId());
			Assert.assertEquals("887a4a80-3ae0-4627-b57a-8c552a3daa5b", ext.getUnitId());
		}
		if (ref) {
			Assert.assertEquals(0, exchange.id);
		} else {
			Assert.assertEquals(true, exchange.id != 0);
		}
	}

	private Exchange findExchange(List<Exchange> exchanges, String flowName) {
		for (Exchange exchange : exchanges)
			if (exchange.flow.name.get(0).value.equals(flowName))
				return exchange;
		return null;
	}

	private void assertExchangeUncertainty(Exchange exchange, UncertaintyDistribution type, Double v1, Double v2) {
		Assert.assertEquals(type, exchange.uncertaintyDistribution);
		Double sd = exchange.relativeStandardDeviation95In;
		if (type == null) {
			TestUtils.assertNull(sd, exchange.minimumAmount, exchange.maximumAmount);
			return;
		}
		switch (exchange.uncertaintyDistribution) {
		case LOG_NORMAL:
		case NORMAL:
			Assert.assertEquals((double) v1, (double) sd, 0d);
			TestUtils.assertNull(exchange.minimumAmount, exchange.maximumAmount);
			break;
		case TRIANGULAR:
		case UNIFORM:
			Assert.assertEquals((double) v1, (double) exchange.minimumAmount, 0d);
			Assert.assertEquals((double) v2, (double) exchange.maximumAmount, 0d);
			TestUtils.assertNull(sd);
			break;
		default:
			TestUtils.assertNull(sd, exchange.minimumAmount, exchange.maximumAmount);
			break;
		}
	}

	// values are different for one product -> if each is found, assume correct
	private void assertAllocation(Exchange exchange, double... values) {
		Assert.assertEquals(values.length, exchange.allocations.length);
		outer: for (double v : values) {
			for (AllocationFactor f : exchange.allocations) {
				if (f.fraction == v * 100)
					continue outer;
			}
			// not found, throw assertion error
			Assert.assertTrue(false);
		}
	}

	private Process convert(String testType, String id) {
		Util util = TestUtils.createUtil(testType);
		ProcessConverter converter = new ProcessConverter(util);
		JsonObject process = In.parse(util.config.store.get("Process", id));
		return converter.run(process);
	}

}
