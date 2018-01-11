package org.rbfcu.projectview.bean;

import java.util.List;
import java.util.Set;

public class ProjectViewForm {

	private String status;
	private List<Ticket> tickets;
	private Set<String> requestDeptSet;
	private Set<String> prioritySet;
	private Set<String> ticketIdSet;
	private Set<String> requestStatusSet;
	private Set<String> issueType;
	private Set<String> assigneeList;
	private Set<String> irPriorityList;
	private Set<String> irStatusSet;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public List<Ticket> getTickets() {
		return tickets;
	}

	public void setTickets(List<Ticket> tickets) {
		this.tickets = tickets;
	}

	public Set<String> getRequestDeptSet() {
		return requestDeptSet;
	}

	public void setRequestDeptSet(Set<String> requestDeptSet) {
		this.requestDeptSet = requestDeptSet;
	}

	public Set<String> getPrioritySet() {
		return prioritySet;
	}

	public void setPrioritySet(Set<String> prioritySet) {
		this.prioritySet = prioritySet;
	}

	public Set<String> getSourceTicketIdSet() {
		return ticketIdSet;
	}

	public void setSourceTicketIdSet(Set<String> sourceTicketIdSet) {
		this.ticketIdSet = sourceTicketIdSet;
	}

	public Set<String> getRequestStatusSet() {
		return requestStatusSet;
	}

	public void setRequestStatusSet(Set<String> requestStatusSet) {
		this.requestStatusSet = requestStatusSet;
	}

	public Set<String> getIssueType() {
		return issueType;
	}

	public void setIssueType(Set<String> issueType) {
		this.issueType = issueType;
	}

	public Set<String> getAssigneeList() {
		return assigneeList;
	}

	public void setAssigneeList(Set<String> assigneeList) {
		this.assigneeList = assigneeList;
	}

	public Set<String> getIrPriorityList() {
		return irPriorityList;
	}

	public void setIrPriorityList(Set<String> irPriorityList) {
		this.irPriorityList = irPriorityList;
	}

	public Set<String> getIrStatusSet() {
		return irStatusSet;
	}

	public void setIrStatusSet(Set<String> irStatusSet) {
		this.irStatusSet = irStatusSet;
	}

}
