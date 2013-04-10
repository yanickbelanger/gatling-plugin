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
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class GatlingPublisher extends Recorder {

	private final Boolean enabled;
	private String simulation;
	private long lastReportTimestamp;
	private AbstractProject<?, ?> project;
	private PrintStream logger;


	@DataBoundConstructor
	public GatlingPublisher(Boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
		logger = listener.getLogger();
		if (enabled == null) {
			logger.println("Cannot check Gatling simulation tracking status, reports won't be archived.");
			logger.println("Please make sure simulation tracking is enabled in your build configuration !");
			return true;
		}
		if (!enabled) {
			logger.println("Simulation tracking disabled, reports were not archived.");
			return true;
		}

		// Find the timestamp of the last archived simulation, not persisted upon restart
		if (lastReportTimestamp == 0) {
			lastReportTimestamp = findLastReportTimeStamp();
		}

		logger.println("Archiving Gatling reports...");
		FilePath reportDirectory = saveFullReport(build.getWorkspace(), build.getRootDir());
		if (reportDirectory == null) {
			logger.println("No newer Gatling reports to archive.");
			return true;
		}

		SimulationReport report = new SimulationReport(reportDirectory, simulation);
		report.readStatsFile();

		GatlingBuildAction action = new GatlingBuildAction(build, simulation, report.getGlobalReport(), reportDirectory);

		build.addAction(action);
		return true;
	}

	public String getSimulation() {
		return simulation;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.BUILD;
	}

	@Override
	public Action getProjectAction(AbstractProject<?, ?> project) {
		this.project = project;
		return new GatlingProjectAction(project);
	}

	private FilePath saveFullReport(FilePath workspace, File rootDir) throws IOException, InterruptedException {
		FilePath[] files = workspace.list("**/global_stats.json");
		List<FilePath> reportFolders = new ArrayList<FilePath>();

		if (files.length == 0) {
			throw new IllegalArgumentException("Could not find a Gatling report in results folder.");
		}

		// Get reports folders for all "global_stats.json" found
		for (FilePath file : files) {
			reportFolders.add(file.getParent().getParent());
		}

		FilePath reportToArchive = selectReport(reportFolders);

		// If the most recent report has already been archived, there's nothing else to do
		if (reportToArchive == null) {
			return null;
		}

		String name = reportToArchive.getName();
		File allSimulationsDirectory = new File(rootDir, "simulations");
		if (!allSimulationsDirectory.exists())
			allSimulationsDirectory.mkdir();
		int dashIndex = name.lastIndexOf('-');
		simulation = name.substring(0, dashIndex);
		File simulationDirectory = new File(allSimulationsDirectory, name);
		simulationDirectory.mkdir();

		FilePath reportDirectory = new FilePath(simulationDirectory);

		reportToArchive.copyRecursiveTo(reportDirectory);

		// Report was successfully archived, update the timestamp
		lastReportTimestamp = getTimestampFromFolderName(name);

		return reportDirectory;
	}

	private FilePath selectReport(List<FilePath> reportFolders) {
		FilePath lastReport = reportFolders.get(0);
		long mostRecentTimestamp = getTimestampFromFolderName(lastReport.getName());
		for (FilePath reportFolder : reportFolders) {
			long reportTimestamp = getTimestampFromFolderName(reportFolder.getName());
			if (reportTimestamp > mostRecentTimestamp) {
				lastReport = reportFolder;
				mostRecentTimestamp = reportTimestamp;
			}
		}
		return mostRecentTimestamp > lastReportTimestamp ? lastReport : null;
	}

	private long findLastReportTimeStamp() {
		long lastTimestamp = 0L;
		for (AbstractBuild<?, ?> build : project.getBuilds()) {
			File allSimulationsDirectory = new File(build.getRootDir(), "simulations");
			// Is there a archived report for this build ?
			if (allSimulationsDirectory.exists()) {
				// There should be only the report folder
				String reportFolderName = (allSimulationsDirectory.listFiles()[0]).getName();
				long currentTimestamp = getTimestampFromFolderName(reportFolderName);
				lastTimestamp = Math.max(lastTimestamp, currentTimestamp);
			}
		}
		return lastTimestamp;
	}

	private long getTimestampFromFolderName(String folderName) {
		int dashIndex = folderName.lastIndexOf('-');
		return Long.parseLong(folderName.substring(dashIndex + 1, folderName.length()));
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


}
