package org.rbfcu.projectview.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.naming.AuthenticationException;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.rbfcu.projectview.bean.Ticket;
import org.rbfcu.projectview.config.ProjectViewServiceConfig;
import org.rbfcu.projectview.controller.RestServiceUtility;
import org.springframework.stereotype.Service;

import com.sun.jersey.core.util.Base64;

@Service
public class JiraClientService {

	final static Logger logger = Logger.getLogger(JiraClientService.class);

	private static final String KEY2 = "key";
	private static String ENV = null;
	private static final List<String> IGNORE_STATUS_LIST = Arrays.asList("Done");

	public List<Ticket> getProjectRequestIssues(String key) throws AuthenticationException, JSONException {

		return getAllIssues(ProjectViewServiceConfig.getInstance().getValue("jira_jql_proj_req_list_" + key));
	}

	public List<Ticket> getAllIssues(String jql)

			throws AuthenticationException, JSONException {

		JSONObject jObject = getAssociatedJiraIssue(jql);
		JSONArray issues = jObject.getJSONArray("issues");

		logger.debug("NEW jql:--------------------------------------------------------------->" + jql);

		logger.debug("Length of the IN PROGRESS Tickets :--->" + issues.length());

		List<Ticket> ticketList = new ArrayList<Ticket>();
		Ticket ticket = null;

		for (int i = 0; i < issues.length(); i++) {
			JSONObject jsonObject = issues.getJSONObject(i);

			String sourceKey = (String) jsonObject.get(KEY2);
			logger.debug("SRC- Issue::-->" + sourceKey);

			JSONObject fields = (JSONObject) jsonObject.get("fields");
			String[] names = JSONObject.getNames(fields);

			ticket = IssueProcessHelper.copyAllFields(fields, names, ticket, sourceKey, issues.length(), ENV);
			if (ticket != null && !IGNORE_STATUS_LIST.contains(ticket.getStatus()))
				;
			ticketList.add(ticket);
		}
		return ticketList;
	}

	private JSONObject getAssociatedJiraIssue(String jql) throws AuthenticationException, JSONException {
		String tickets = getJiraDetailsByQuery(jql);
		JSONObject jObject = new JSONObject(tickets);
		return jObject;
	}

	private String getJiraDetailsByQuery(String jql) throws AuthenticationException {

		String password = ProjectViewServiceConfig.getInstance().getValue("jira_password");
		String username = ProjectViewServiceConfig.getInstance().getValue("jira_username");
		String baseURL = ProjectViewServiceConfig.getInstance().getValue("jira_url");

		String userCredientials = username + ":" + password;
		String auth = new String(Base64.encode(userCredientials));
		String issueType = RestServiceUtility.invokeGetMethod(auth, baseURL + jql);
		return issueType;
	}

}
