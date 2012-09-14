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

import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.model.DirectoryBrowserSupport;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.IOException;

public class GatlingReportAction implements Action {
	private final AbstractBuild<?, ?> build;
	private final String simulationName;
	private final FilePath simulationDirectory;

	public GatlingReportAction(AbstractBuild<?, ?> build, String simulationName, FilePath simulationDirectory) {
		this.build = build;
		this.simulationName = simulationName;
		this.simulationDirectory = simulationDirectory;
	}

	public AbstractBuild<?, ?> getBuild() {
		return build;
	}

	public String getSimulationName() {
		return simulationName;
	}

	public FilePath getSimulationDirectory() {
		return simulationDirectory;
	}

	public String getIconFileName() {
		return null;
	}

	public String getDisplayName() {
		return null;
	}

	public String getUrlName() {
		return getURL(simulationName);
	}

	public void doSource(StaplerRequest request, StaplerResponse response) throws IOException, ServletException {
		DirectoryBrowserSupport dbs = new DirectoryBrowserSupport(this, simulationDirectory, simulationName, null, false);
		dbs.generateResponse(request, response, this);
	}

	public static String getURL(String simulationName) {
		return "gatlingReport" + simulationName;
	}
}
