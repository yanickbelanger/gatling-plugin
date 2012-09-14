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

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.kohsuke.stapler.DataBoundConstructor;

import com.excilys.ebi.gatling.jenkins.model.Simulation;

public class GatlingPublisher extends Recorder {

	private static final Logger LOGGER = Logger.getLogger(GatlingPublisher.class.getName());

	private final Simulation simulation;

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
		GatlingBuildAction action = new GatlingBuildAction(build);

		SimulationReport report = new SimulationReport(build.getWorkspace(), simulation);
		report.readStatsFile();

		if (report.isBuildFailed()) {
			build.setResult(Result.FAILURE);
		}
		if (report.isBuildUnstable()) {
			build.setResult(Result.UNSTABLE);
		}

		action.getRequestsReports().put(simulation.getName(), report.getGlobalReport());

		FilePath reportDirectory = saveFullReport(build.getWorkspace(), build.getRootDir(), simulation.getName());

		build.addAction(new GatlingReportAction(build, simulation.getName(), reportDirectory));

		build.addAction(action);
		return true;
	}

	@DataBoundConstructor
	public GatlingPublisher(Simulation simulation) {
		if (simulation == null)
			this.simulation = null;
		else
			this.simulation = simulation;
	}

	public Simulation getSimulation() {
		return simulation;
	}

	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.BUILD;
	}

	@Override
	public Action getProjectAction(AbstractProject<?, ?> project) {
		return new GatlingProjectAction(project);
	}

	@Extension
	public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {

		@Override
		public boolean isApplicable(Class<? extends AbstractProject> aClass) {
			return true;
		}

		@Override
		public String getDisplayName() {
			return Messages.Title();
		}
	}

	private FilePath saveFullReport(FilePath workspace, File rootDir, String simulationName) throws IOException, InterruptedException {
		FilePath[] files = workspace.list("**/" + simulationName + "*/index.html");
		FilePath parent = files[0].getParent();

		File allSimulationsDirectory = new File(rootDir, "simulations");
		if (!allSimulationsDirectory.exists())
			allSimulationsDirectory.mkdir();

		File simulationDirectory = new File(allSimulationsDirectory, simulationName);
		simulationDirectory.mkdir();

		FilePath reportDirectory = new FilePath(simulationDirectory);

		parent.copyRecursiveTo(reportDirectory);

		return reportDirectory;
	}
}
