package org.rbfcu.projectview.util;

import org.rbfcu.common.database.config.IDatabaseConfig;
import org.rbfcu.config.servlet.BaseReloadConfigServlet;

@SuppressWarnings("serial")
public class ReloadConfigServlet extends BaseReloadConfigServlet {

	private static final String JIRA_CONFIG_FILE_NAME = "application.config";
	public ReloadConfigServlet() {
		// TODO Auto-generated constructor stub
	}
	
	protected IDatabaseConfig getDbConfig() {
		return null;
	}
	
	@Override
	protected String getAppConfigFileName() {

		return JIRA_CONFIG_FILE_NAME;
	}

}