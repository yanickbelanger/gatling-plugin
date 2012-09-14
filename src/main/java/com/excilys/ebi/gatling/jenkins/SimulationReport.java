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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import com.excilys.ebi.gatling.jenkins.model.Condition;
import com.excilys.ebi.gatling.jenkins.model.Simulation;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SimulationReport {

	private final FilePath workspace;

	private static final String STATS_FILE_PATTERN = "**/global_stats.json";

	private RequestReport globalReport;

	private final Simulation simulation;

	public SimulationReport(FilePath workspace, Simulation simulation) {
		this.workspace = workspace;
		this.simulation = simulation;
	}

	public void readStatsFile() throws IOException, InterruptedException {
		ObjectMapper mapper = new ObjectMapper();
		File jsonFile = locateStatsFile();
		globalReport = mapper.readValue(jsonFile, new TypeReference<RequestReport>() {
		});
	}

	private File locateStatsFile() throws IOException, InterruptedException {
		String pattern = new StringBuilder().append("**/").append(simulation.getName()).append("*/").append(STATS_FILE_PATTERN).toString();
		FilePath[] files = workspace.list(pattern);

		if (files.length == 0)
			throw new FileNotFoundException("Unable to locate the simulation results for " + simulation.getName());

		return new File(files[0].getRemote());
	}

	public boolean hasFailConditions() {
		return !simulation.getFailConditions().isEmpty();
	}

	public boolean hasUnstableConditions() {
		return !simulation.getUnstableConditions().isEmpty();
	}

	public boolean isBuildFailed() {
		for (Condition condition : simulation.getFailConditions()) {
			if (!condition.isFulfilledBy(globalReport)) {
				return true;
			}
		}
		return false;
	}

	public boolean isBuildUnstable() {
		for (Condition condition : simulation.getUnstableConditions()) {
			if (!condition.isFulfilledBy(globalReport)) {
				return true;
			}
		}
		return false;
	}

	public String getSimulationPath() {
		return simulation.getName();
	}

	public List<Condition> getFailConditions() {
		return simulation.getFailConditions();
	}

	public List<Condition> getUnstableConditions() {
		return simulation.getUnstableConditions();
	}

	public RequestReport getGlobalReport() {
		return globalReport;
	}
}
