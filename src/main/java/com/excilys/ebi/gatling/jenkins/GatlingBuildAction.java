/**
 * Copyright 2011-2012 eBusiness Information, Groupe Excilys (www.excilys.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.excilys.ebi.gatling.jenkins;

import static com.excilys.ebi.gatling.jenkins.PluginConstants.*;
import hudson.model.Action;
import hudson.model.AbstractBuild;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GatlingBuildAction implements Action {

	private final AbstractBuild<?, ?> build;
	private final Map<String, RequestReport> requestsReports = new HashMap<String, RequestReport>();

	public GatlingBuildAction(AbstractBuild<?, ?> build) {
		this.build = build;
	}

	public AbstractBuild<?, ?> getBuild() {
		return build;
	}

	public Map<String, RequestReport> getRequestsReports() {
		return requestsReports;
	}

	public List<String> getReports() {
		return getReports(build);
	}

	public String getIconFileName() {
		return ICON_URL;
	}

	public String getDisplayName() {
		return DISPLAY_NAME;
	}

	public String getUrlName() {
		return URL_NAME;
	}

	public static List<String> getReports(AbstractBuild<?, ?> build) {
		List<String> reports = new ArrayList<String>();

		for (GatlingReportAction action : build.getActions(GatlingReportAction.class))
			reports.add(action.getSimulationName());

		return reports;
	}

	public String getReportURL(String simulation) {
		return GatlingReportAction.getURL(simulation);
	}
}
