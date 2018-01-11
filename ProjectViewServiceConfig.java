package org.rbfcu.projectview.config;

import org.rbfcu.config.Config;
import org.rbfcu.config.EnvConfig;
import org.rbfcu.config.provider.PropertiesFileConfigProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ProjectViewServiceConfig extends Config {

	private static final long serialVersionUID = -8778943213648996921L;
	private static final Logger LOG = LoggerFactory.getLogger(ProjectViewServiceConfig.class);
	private static ProjectViewServiceConfig   projectViewServiceConfig = new ProjectViewServiceConfig();

	private ProjectViewServiceConfig() {
		try {
			new PropertiesFileConfigProvider("application.config").loadProperties(this);
		} catch (Throwable e) {
			LOG.error("error in loading Project View service configuration", e);
		}
	}

	public static Config getInstance() {
		return projectViewServiceConfig;
	}

	@Override
	protected Config getPropertyContainer() {
		return getInstance();
	}

	public static void setValue(String key, String value) {
		if (EnvConfig.isPrd()) {
			throw new RuntimeException("not allow to set config keys programmatically in prod: " + key);
		}
		projectViewServiceConfig.setProperty(key, value);
	}
}
