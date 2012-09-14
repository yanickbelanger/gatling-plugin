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
import hudson.model.AbstractProject;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.excilys.ebi.gatling.jenkins.chart.Graph;

public class GatlingProjectAction implements Action {

	private final AbstractProject<?, ?> project;

	public GatlingProjectAction(AbstractProject<?, ?> project) {
		this.project = project;
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

	public AbstractProject<?, ?> getProject() {
		return project;
	}

	public boolean isVisible() {
		for (AbstractBuild<?, ?> build : getProject().getBuilds()) {
			GatlingBuildAction gatlingBuildAction = build.getAction(GatlingBuildAction.class);
			if (gatlingBuildAction != null && gatlingBuildAction.getRequestsReports() != null && gatlingBuildAction.getRequestsReports().size() > 0) {
				return true;
			}
		}
		return false;
	}

	public Graph<Long> getDashboardGraph() {
		return new Graph<Long>(project, MAX_BUILDS_TO_DISPLAY_DASHBOARD) {
			@Override
			public Long getValue(RequestReport requestReport) {
				return requestReport.getMeanResponseTime().getTotal();
			}
		};
	}

	public Graph<Long> getMeanResponseTimeGraph() {
		return new Graph<Long>(project, MAX_BUILDS_TO_DISPLAY) {
			@Override
			public Long getValue(RequestReport requestReport) {
				return requestReport.getMeanResponseTime().getTotal();
			}
		};
	}

	public Graph<Long> getPercentileResponseTimeGraph() {
		return new Graph<Long>(project, MAX_BUILDS_TO_DISPLAY) {
			@Override
			public Long getValue(RequestReport requestReport) {
				return requestReport.getPercentiles95().getTotal();
			}
		};
	}

	public Graph<Long> getRequestKOPercentageGraph() {
		return new Graph<Long>(project, MAX_BUILDS_TO_DISPLAY) {
			@Override
			public Long getValue(RequestReport requestReport) {
				return Math.round(requestReport.getNumberOfRequests().getKO() * 100.0 / requestReport.getNumberOfRequests().getTotal());
			}
		};
	}

	public Map<AbstractBuild<?, ?>, List<String>> getReports() {
		Map<AbstractBuild<?, ?>, List<String>> reports = new LinkedHashMap<AbstractBuild<?, ?>, List<String>>();

		for (AbstractBuild<?, ?> build : project.getBuilds()) {
			List<String> simulations = GatlingBuildAction.getReports(build);

			if (!simulations.isEmpty()) {
				reports.put(build, simulations);
			}
		}

		return reports;
	}

	public String getReportURL(int build, String simulation) {
		return new StringBuilder().append(build).append("/").append(GatlingReportAction.getURL(simulation)).toString();
	}
}
