package org.rbfcu.projectview.bean;

import java.io.Serializable;

public class Ticket implements Serializable {

	private static final long serialVersionUID = 7784796271843695206L;
	private String ticketId;
	private String summary;
	private String requestStatus;
	private String status;
	private String standUpTime;
	private String requestDept;
	private String priority;
	private String order;
	private String lead;
	private String ba;
	private String issueType;
	private String assignee;
	private String irPriority;
	private String resolvedDate;
	private String heat;
	private String testUrl;
	private String expectedUatDate;

	public String getTicketId() {
		return ticketId;
	}

	public void setTicketId(String ticketId) {
		this.ticketId = ticketId;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getRequestStatus() {
		return requestStatus;
	}

	public void setRequestStatus(String requestStatus) {
		this.requestStatus = requestStatus;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getStandUpTime() {
		return standUpTime;
	}

	public void setStandUpTime(String standUpTime) {
		this.standUpTime = standUpTime;
	}

	public String getRequestDept() {
		return requestDept;
	}

	public void setRequestDept(String requestDept) {
		this.requestDept = requestDept;
	}

	public String getPriority() {
		return priority;
	}

	public void setPriority(String priority) {
		this.priority = priority;
	}

	public String getOrder() {
		return order;
	}

	public void setOrder(String order) {
		this.order = order;
	}

	public String getLead() {
		return lead;
	}

	public void setLead(String lead) {
		this.lead = lead;
	}

	public String getBa() {
		return ba;
	}

	public void setBa(String ba) {
		this.ba = ba;
	}

	public String getIssueType() {
		return issueType;
	}

	public void setIssueType(String issueType) {
		this.issueType = issueType;
	}

	public String getAssignee() {
		return assignee;
	}

	public void setAssignee(String assignee) {
		this.assignee = assignee;
	}

	public String getIrPriority() {
		return irPriority;
	}

	public void setIrPriority(String irPriority) {
		this.irPriority = irPriority;
	}

	public String getResolvedDate() {
		return resolvedDate;
	}

	public void setResolvedDate(String resolvedDate) {
		this.resolvedDate = resolvedDate;
	}

	public String getHeat() {
		return heat;
	}

	public void setHeat(String heat) {
		this.heat = heat;
	}

	public String getTestUrl() {
		return testUrl;
	}

	public void setTestUrl(String testUrl) {
		this.testUrl = testUrl;
	}

	public String getExpectedUatDate() {
		return expectedUatDate;
	}

	public void setExpectedUatDate(String expectedUatDate) {
		this.expectedUatDate = expectedUatDate;
	}

}
