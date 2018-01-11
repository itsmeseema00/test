package org.rbfcu.projectview.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.naming.AuthenticationException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.simple.parser.ParseException;
import org.rbfcu.projectview.bean.ProjectViewForm;
import org.rbfcu.projectview.bean.Ticket;
import org.rbfcu.projectview.service.IrTicketsService;
import org.rbfcu.projectview.service.JiraClientService;
import org.rbfcu.projectview.util.TicketChainedComparator;
import org.rbfcu.projectview.util.TicketPrioritySetComparator;
import org.rbfcu.projectview.util.TicketRequestDeptSetComparator;
import org.rbfcu.projectview.util.TicketSourceTicketIdSetComparator;
import org.rbfcu.projectview.util.TicketStandUpTimeComparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ProjectViewController {

	final static Logger logger = Logger.getLogger(ProjectViewController.class);
	private final static String ERR_MSG = "An error has occurred.  Please try again and contact the help desk if the problem persists.";
	private static final String PROJECT_VIEW_FORM = "projectViewForm";

	@Autowired
	private JiraClientService jiraClientService;

	@Autowired
	IrTicketsService irTicketsService;

	@RequestMapping(value = "/webteam", method = RequestMethod.GET)
	public ModelAndView jiraHome(HttpServletRequest request) {
		ModelAndView mv = new ModelAndView("projectView");
		ProjectViewForm projectViewForm = null;
		try {
			projectViewForm = loadTicketDetails("PR");
			mv.addObject(PROJECT_VIEW_FORM, projectViewForm);
		} catch (AuthenticationException e) {
			logger.error("AuthenticationException @ show dash board :: ", e);
			mv = new ModelAndView("error");
			mv.addObject("errorMessage", ERR_MSG);
		} catch (Exception e) {
			logger.error("Exception Occured :: ", e);
			mv = new ModelAndView("error");
			mv.addObject("errorMessage", ERR_MSG);
		}
		return mv;
	}

	@RequestMapping(value = "/webteam", method = { RequestMethod.POST })
	public ModelAndView filtering(HttpServletRequest request, @RequestParam(required = false) String requestDept,
			@RequestParam(required = false) String requestStatus, @RequestParam(required = false) String status) throws ParseException {
		ModelAndView mv = new ModelAndView("projectView");
		ProjectViewForm projectViewForm = null;
		try {
			if (logger.isDebugEnabled()) {
				logger.debug(":: requestDept = " + requestDept + ":: requestStatus ::" + requestStatus + " :: status" + status);
			}
			projectViewForm = loadTicketDetails("PR");
			projectViewForm.setTickets(filterTicketsByInputValues(projectViewForm.getTickets(), requestDept, requestStatus, status));
			mv.addObject(PROJECT_VIEW_FORM, projectViewForm);
		} catch (AuthenticationException e) {
			logger.error("AuthenticationException @ show dash board :: ", e);
			mv = new ModelAndView("error");
			mv.addObject("errorMessage", ERR_MSG);
		} catch (Exception e) {
			logger.error("Exception Occured :: ", e);
			mv = new ModelAndView("error");
			mv.addObject("errorMessage", ERR_MSG);
		}
		return mv;
	}

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public ModelAndView jiraHomeTv(HttpServletRequest request) {
		ModelAndView mv = new ModelAndView("projectTvView");
		ProjectViewForm projectViewForm = null;
		try {
			projectViewForm = loadTicketDetails("PR");
			mv.addObject(PROJECT_VIEW_FORM, projectViewForm);
		} catch (AuthenticationException e) {
			logger.error("AuthenticationException @ show dash board :: ", e);
			mv = new ModelAndView("error");
			mv.addObject("errorMessage", ERR_MSG);
		} catch (Exception e) {
			logger.error("Exception Occured :: ", e);
			mv = new ModelAndView("error");
			mv.addObject("errorMessage", ERR_MSG);
		}
		return mv;
	}

	@RequestMapping(value = "/", method = { RequestMethod.POST })
	public ModelAndView filteringTvView(HttpServletRequest request, @RequestParam(required = false) String requestDept,
			@RequestParam(required = false) String prioritySet, @RequestParam(required = false) String sourceTicketId,
			@RequestParam(required = false) String status) throws ParseException {
		ModelAndView mv = new ModelAndView("projectTvView");
		ProjectViewForm projectViewForm = null;
		try {

			if (logger.isDebugEnabled()) {
				logger.debug(":: requestDept = " + requestDept + " :: prioritySet =" + prioritySet + "::sourceTicketID = " + sourceTicketId
						+ " :: status" + status);
			}
			projectViewForm = loadTicketDetails("PR");
			projectViewForm
					.setTickets(filterTicketsByInputValuesTvView(projectViewForm.getTickets(), requestDept, prioritySet, sourceTicketId, status));
			mv.addObject(PROJECT_VIEW_FORM, projectViewForm);
		} catch (AuthenticationException e) {
			logger.error("AuthenticationException @ show dash board :: ", e);
			mv = new ModelAndView("error");
			mv.addObject("errorMessage", ERR_MSG);
		} catch (Exception e) {
			logger.error("Exception Occured :: ", e);
			mv = new ModelAndView("error");
			mv.addObject("errorMessage", ERR_MSG);
		}
		return mv;
	}

	@RequestMapping(value = "/ir", method = RequestMethod.GET)
	public ModelAndView jiraHomeIr(HttpServletRequest request, @RequestParam(required = false) boolean excludeOlderTicket) {
		ModelAndView mv = new ModelAndView("irView");
		ProjectViewForm projectViewForm = null;
		try {
			projectViewForm = loadIrTicketDetails("IR", excludeOlderTicket);
			mv.addObject(PROJECT_VIEW_FORM, projectViewForm);
		} catch (AuthenticationException e) {
			logger.error("AuthenticationException @ show dash board :: ", e);
			mv = new ModelAndView("error");
			mv.addObject("errorMessage", ERR_MSG);
		} catch (Exception e) {
			logger.error("Exception Occured :: ", e);
			mv = new ModelAndView("error");
			mv.addObject("errorMessage", ERR_MSG);
		}
		return mv;
	}

	@RequestMapping(value = "/ir", method = { RequestMethod.POST })
	public ModelAndView filteringIr(HttpServletRequest request, @RequestParam(required = false) String prioritySet,
			@RequestParam(required = false) boolean showOlderTicket, @RequestParam(required = false) String status,
			@RequestParam(required = false) String department) throws ParseException {
		ModelAndView mv = new ModelAndView("irView");
		ProjectViewForm projectViewForm = null;
		try {
			if (logger.isDebugEnabled()) {
				logger.debug(":: prioritySet ::" + prioritySet + ":: status ::" + status + ":: department ::" + department);
			}
			projectViewForm = loadIrTicketDetails("IR", showOlderTicket);
			projectViewForm.setTickets(new IrTicketsService().filterIrTicketsByStatusAndPriorityAndDepartment(projectViewForm.getTickets(),
					prioritySet, status, department));
			mv.addObject(PROJECT_VIEW_FORM, projectViewForm);
		} catch (AuthenticationException e) {
			logger.error("AuthenticationException @ show dash board :: ", e);
			mv = new ModelAndView("error");
			mv.addObject("errorMessage", ERR_MSG);
		} catch (Exception e) {
			logger.error("Exception Occured :: ", e);
			mv = new ModelAndView("error");
			mv.addObject("errorMessage", ERR_MSG);
		}
		return mv;
	}

	private List<Ticket> filterTicketsByInputValues(List<Ticket> sourceTickets, String requestDept, String requestStatus, String status) {
		List<Ticket> targetTickets = new ArrayList<Ticket>();
		for (Ticket ticket : sourceTickets) {
			if (("".equals(requestDept) || requestDept.equalsIgnoreCase(ticket.getRequestDept()))
					&& ("".equals(requestStatus) || requestStatus.equalsIgnoreCase(ticket.getRequestStatus()))
					&& ("".equals(status) || status.equalsIgnoreCase(ticket.getStatus())))
				targetTickets.add(ticket);
		}
		logger.debug("Total Ticket Count ::: " + targetTickets.size());
		return targetTickets;
	}

	private List<Ticket> filterTicketsByInputValuesTvView(List<Ticket> sourceTickets, String requestDept, String prioritySet, String sourceTicketId,
			String status) {
		List<Ticket> targetTickets = new ArrayList<Ticket>();
		for (Ticket ticket : sourceTickets) {
			if ((StringUtils.isBlank(requestDept) || StringUtils.equalsIgnoreCase(requestDept, ticket.getRequestDept()))
					&& (StringUtils.isBlank(prioritySet) || StringUtils.equalsIgnoreCase(prioritySet, ticket.getPriority()))
					&& (StringUtils.isBlank(sourceTicketId) || StringUtils.equalsIgnoreCase(sourceTicketId, ticket.getTicketId()))) {
				targetTickets.add(ticket);
			}
		}
		logger.debug("Total Ticket Count ::: " + targetTickets.size());
		return targetTickets;
	}

	/**
	 * This methods sorts the tickets based on the ticket status and ticket key.
	 * The primary sort is on Status, and the secondary sort is on the numerical key suffix (descending).  
	 * Hence, This will show the newest open tickets first.
	 * @param tickets
	 */
	//TODO make two separate lists of tickets for returning to the UI. Then each list will have their own sorting
	private void sortTicketsByStatus(List<Ticket> tickets) {
		Collections.sort(tickets, new TicketChainedComparator(new TicketStandUpTimeComparator(), new TicketPrioritySetComparator(),
				new TicketRequestDeptSetComparator(), new TicketSourceTicketIdSetComparator()));
	}

	private ProjectViewForm loadTicketDetails(String key) throws AuthenticationException, JSONException {
		List<Ticket> sourceTickets = new ArrayList<Ticket>(jiraClientService.getProjectRequestIssues(key));
		ProjectViewForm projectViewForm = new ProjectViewForm();
		Set<String> requestDeptSet = new TreeSet<String>();
		Set<String> prioritySet = new TreeSet<String>();
		Set<String> sourceTicketIdSet = new HashSet<String>();
		Set<String> requestStatusSet = new TreeSet<String>();
		Set<String> issueType = new TreeSet<String>();
		Set<String> assigneeList = new TreeSet<String>();
		Set<String> irPriorityList = new TreeSet<String>();
		Set<String> irStatusSet = new TreeSet<String>();
		if (!sourceTickets.isEmpty()) {
			for (Ticket ticket : sourceTickets) {
				if (ticket.getRequestDept() != null)
					requestDeptSet.add(ticket.getRequestDept());
				sourceTicketIdSet.add(ticket.getTicketId());
				if (ticket.getRequestStatus() != null) {
					requestStatusSet.add(ticket.getRequestStatus());
				}
				if (StringUtils.isNotBlank(ticket.getPriority())) {
					prioritySet.add(ticket.getPriority());
				}
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
			}
			logger.debug("Total Ticket Count ::: " + sourceTickets.size());
		}
		sortTicketsByStatus(sourceTickets);
		projectViewForm.setTickets(sourceTickets);
		projectViewForm.setRequestDeptSet(requestDeptSet);
		projectViewForm.setSourceTicketIdSet(sourceTicketIdSet);
		projectViewForm.setPrioritySet(prioritySet);
		projectViewForm.setRequestStatusSet(requestStatusSet);
		projectViewForm.setIssueType(issueType);
		projectViewForm.setAssigneeList(assigneeList);
		projectViewForm.setIrPriorityList(irPriorityList);
		projectViewForm.setIrStatusSet(irStatusSet);
		return projectViewForm;
	}

	private ProjectViewForm loadIrTicketDetails(String key, boolean showOlderTicket) throws AuthenticationException, JSONException {
		List<Ticket> sourceTickets = new ArrayList<Ticket>(jiraClientService.getProjectRequestIssues(key));
		return irTicketsService.loadIrTickets(sourceTickets, showOlderTicket);
	}

}
