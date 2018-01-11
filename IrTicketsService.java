package org.rbfcu.projectview.service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.rbfcu.common.date.DateUtility;
import org.rbfcu.projectview.bean.ProjectViewForm;
import org.rbfcu.projectview.bean.Ticket;
import org.rbfcu.projectview.controller.ProjectViewController;
import org.springframework.stereotype.Service;

@Service
public class IrTicketsService {
	final static Logger logger = Logger.getLogger(ProjectViewController.class);
	private static final String STATUS_CLOSED = "Closed";
	private static final int PAST_DAYS_LIMIT = 30;
	DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

	public ProjectViewForm loadIrTickets(List<Ticket> sourceTickets, boolean showOlderTicket) {
		ProjectViewForm projectViewForm = new ProjectViewForm();
		Set<String> issueType = new TreeSet<String>();
		Set<String> assigneeList = new TreeSet<String>();
		Set<String> irPriorityList = new TreeSet<String>();
		List<Ticket> filteredTickets = new ArrayList<Ticket>();
		Set<String> irStatusSet = new TreeSet<String>();
		Set<String> requestDeptSet = new TreeSet<String>();
		if (!sourceTickets.isEmpty()) {
			filteredTickets = excludeOlderClosedIrTickets(sourceTickets);
			for (Ticket ticket : filteredTickets) {
				if (ticket.getRequestDept() != null)

					if (ticket.getIssueType() != null) {
						issueType.add(ticket.getIssueType());
					}
				if (ticket.getAssignee() != null) {
					assigneeList.add(ticket.getAssignee());
				}
				if (ticket.getIrPriority() != null) {
					irPriorityList.add(ticket.getIrPriority());
				}
				if (ticket.getStatus() != null) {
					irStatusSet.add(ticket.getStatus());
				}
				if (ticket.getRequestDept() != null) {
					requestDeptSet.add(ticket.getRequestDept());
				}

			}
			logger.debug("Total Ticket Count ::: " + sourceTickets.size());
		}
		if (showOlderTicket == true) {
			projectViewForm.setTickets(sourceTickets);
		} else {
			projectViewForm.setTickets(filteredTickets);
		}
		projectViewForm.setIssueType(issueType);
		projectViewForm.setAssigneeList(assigneeList);
		projectViewForm.setIrPriorityList(irPriorityList);
		projectViewForm.setIrStatusSet(irStatusSet);
		projectViewForm.setRequestDeptSet(requestDeptSet);
		return projectViewForm;
	}

	private List<Ticket> excludeOlderClosedIrTickets(List<Ticket> sourceTickets) {
		List<Ticket> filteredTickets = new ArrayList<Ticket>();
		if (!sourceTickets.isEmpty()) {
			for (Ticket eachTicket : sourceTickets) {
				if (!isClosedAndMonthOld(eachTicket)) {
					filteredTickets.add(eachTicket);
				}
			}
		}
		return filteredTickets;
	}

	private boolean isClosedAndMonthOld(Ticket eachTicket) {
		if (eachTicket.getStatus().equals(STATUS_CLOSED) && null != eachTicket.getResolvedDate()) {
			try {
				Date resolveDate = df.parse(eachTicket.getResolvedDate());
				return isMonthPast(resolveDate);
			} catch (Exception e) {
				return false;
			}
		}
		return false;
	}

	private boolean isMonthPast(Date resolveDate) {
		if (resolveDate != null) {
			Date currentDate = DateUtility.getZeroedTimeDate();
			if (DateUtility.compareDates(resolveDate, currentDate) == DateUtility.LT) {
				if (DateUtility.getDaysBetweenDates(resolveDate, currentDate) > PAST_DAYS_LIMIT) {
					return true;
				}
			}
		}
		return false;
	}

	public List<Ticket> filterIrTicketsByStatusAndPriorityAndDepartment(List<Ticket> sourceTickets, String prioritySet, String status,
			String department) {
		List<Ticket> targetTickets = new ArrayList<Ticket>();
		for (Ticket ticket : sourceTickets) {
			if ((StringUtils.isBlank(prioritySet) || StringUtils.equalsIgnoreCase(prioritySet, ticket.getIrPriority()))
					&& (StringUtils.isBlank(status) || StringUtils.equalsIgnoreCase(status, ticket.getStatus()))
					&& (StringUtils.isBlank(department) || StringUtils.equalsIgnoreCase(department, ticket.getRequestDept()))) {
				targetTickets.add(ticket);
			}
		}
		logger.debug("Total Ticket Count ::: " + targetTickets.size());
		return targetTickets;
	}

}
