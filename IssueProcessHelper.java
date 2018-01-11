package org.rbfcu.projectview.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.rbfcu.projectview.bean.Ticket;

public class IssueProcessHelper {
	final static Logger logger = Logger.getLogger(IssueProcessHelper.class);

	private static final String VALUE = "value";
	private static final String NAME = "name";
	private static final String DISPLAYNAME = "displayName";

	public static Ticket copyAllFields(JSONObject fields, String[] names, Ticket ticket, String sourceKey, int issueCount, String env)
			throws JSONException {

		logger.debug("IssueProcessHelper::copyAllFields: Begin");

		logger.debug("JSON Fields::" + fields);

		ticket = copyStandardFields(fields, names, ticket, sourceKey, issueCount);

		logger.debug("IssueProcessHelper::copyAllFields: End");

		return ticket;
	}

	private static Ticket copyStandardFields(JSONObject fields, String[] fieldNames, Ticket ticket, String sourceKey, int a) throws JSONException {
		logger.debug("IssueProcessHelper::copyStandardFields: Begin");

		ticket = new Ticket();
		ticket.setTicketId(sourceKey);
		logger.debug(fieldNames.length);

		for (int nameCount = 0; nameCount < fieldNames.length; nameCount++) {

			if ("summary".equalsIgnoreCase(fieldNames[nameCount])) {

				String summary = fields.getString(fieldNames[nameCount]);
				if (!summary.equalsIgnoreCase("null")) {
					ticket.setSummary(summary);

				}

			} else if ("resolutiondate".equalsIgnoreCase(fieldNames[nameCount])) {
				logger.debug("Resolved Date:------->" + fields.getString(fieldNames[nameCount]));
				String resolvedDate = fields.getString(fieldNames[nameCount]);
				if (!resolvedDate.equalsIgnoreCase("null")) {
					ticket.setResolvedDate(resolvedDate);

				}
			} else if ("status".equalsIgnoreCase(fieldNames[nameCount])) {

				logger.debug("Requesting status:------->" + fields.getString(fieldNames[nameCount]));
				ticket.setStatus(getFieldJsonObject(fields, fieldNames, nameCount, NAME));

			} else if ("customfield_10411".equalsIgnoreCase(fieldNames[nameCount])) {
				logger.debug("StandupTime:------->" + fields.getString(fieldNames[nameCount]));
				if (StringUtils.isNoneBlank(fields.getString(fieldNames[nameCount]))) {
					ticket.setStandUpTime(fields.getString(fieldNames[nameCount]));
				}
			} else if ("customfield_10112".equalsIgnoreCase(fieldNames[nameCount])) {

				logger.debug("Requesting Department:------->" + fields.getString(fieldNames[nameCount]));
				ticket.setRequestDept(getFieldJsonObject(fields, fieldNames, nameCount, VALUE));

			} else if ("customfield_10125".equalsIgnoreCase(fieldNames[nameCount])) {

				logger.debug("Order:------->" + fields.getString(fieldNames[nameCount]));
				ticket.setOrder(getFieldJsonObject(fields, fieldNames, nameCount, VALUE));

			} else if ("customfield_10120".equalsIgnoreCase(fieldNames[nameCount])) {

				logger.debug("Request Status:::---------->" + fields.getString(fieldNames[nameCount]));
				ticket.setRequestStatus(getFieldJsonObject(fields, fieldNames, nameCount, VALUE));

			} else if ("customfield_10127".equalsIgnoreCase(fieldNames[nameCount])) {

				logger.debug("BA:::---------->" + fields.getString(fieldNames[nameCount]));

				ticket.setBa(getFieldJsonObject(fields, fieldNames, nameCount, NAME));

			} else if ("customfield_10128".equalsIgnoreCase(fieldNames[nameCount])) {

				logger.debug("Lead:::---------->" + fields.getString(fieldNames[nameCount]));

				ticket.setLead(getFieldJsonObject(fields, fieldNames, nameCount, NAME));

			} else if ("customfield_10113".equalsIgnoreCase(fieldNames[nameCount])) {
				logger.debug("Requested Priority:::---------->" + fields.getString(fieldNames[nameCount]));
				ticket.setPriority(getFieldJsonObject(fields, fieldNames, nameCount, VALUE));

			} else if ("issuetype".equalsIgnoreCase(fieldNames[nameCount])) {

				logger.debug("Issue Type name:::---------->" + fields.getString(fieldNames[nameCount]));

				ticket.setIssueType(getFieldJsonObject(fields, fieldNames, nameCount, NAME));

			} else if ("assignee".equalsIgnoreCase(fieldNames[nameCount])) {

				logger.debug("Asignee Name:::---------->" + fields.getString(fieldNames[nameCount]));

				ticket.setAssignee(getFieldJsonObject(fields, fieldNames, nameCount, DISPLAYNAME));

			} else if ("priority".equalsIgnoreCase(fieldNames[nameCount])) {

				logger.debug("Ir Priority:::---------->" + fields.getString(fieldNames[nameCount]));

				ticket.setIrPriority(getFieldJsonObject(fields, fieldNames, nameCount, NAME));

			} else if ("customfield_10115".equalsIgnoreCase(fieldNames[nameCount])) {

				logger.debug("Heat#:------->" + fields.getString(fieldNames[nameCount]));
				String heat = fields.getString(fieldNames[nameCount]);
				if (!heat.equalsIgnoreCase("null")) {
					ticket.setHeat(heat);

				}
			} else if ("customfield_10710".equalsIgnoreCase(fieldNames[nameCount])) {

				logger.debug("Test Url#:------->" + fields.getString(fieldNames[nameCount]));
				String testUrl = fields.getString(fieldNames[nameCount]);
				if (!testUrl.equalsIgnoreCase("null")) {
					ticket.setTestUrl(testUrl);

				}
			} else if ("customfield_10713".equalsIgnoreCase(fieldNames[nameCount])) {

				logger.debug("Expected UAT Date#:------->" + fields.getString(fieldNames[nameCount]));
				String ExpectedUATDate = fields.getString(fieldNames[nameCount]);
				if (!ExpectedUATDate.equalsIgnoreCase("null")) {
					ticket.setExpectedUatDate(ExpectedUATDate);

				}
			}
		}
		logger.debug("IssueProcessHelper::copyStandardFields: End");
		return ticket;
	}

	private static String getFieldJsonObject(JSONObject fields, String[] fieldNames, int nameCount, String value) throws JSONException {

		String jObjectValue = null;
		if (!fields.getString(fieldNames[nameCount]).equalsIgnoreCase("null")) {
			JSONObject jObjects = new JSONObject(fields.getString(fieldNames[nameCount]));
			jObjectValue = jObjects.getString(value);

		}
		return jObjectValue;
	}

}
