/**
 * Copyright 2011-2014 eBusiness Information, Groupe Excilys (www.excilys.com)
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
package io.gatling.jenkins;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import hudson.model.AbstractBuild;

/**
 * Created by yanick.belanger on 8/13/15.
 */
public class PerformanceDelta {

	private List<RequestPerformanceDelta> deltas;

	public PerformanceDelta(AbstractBuild<?, ?> currentBuild, String simName) {
		AbstractBuild<?, ?> previousBuild = currentBuild.getPreviousBuild();
		Map<String, RequestReport> currentReports = getDetailedReports(currentBuild, simName);
		Map<String, RequestReport> previousReports = getDetailedReports(previousBuild, simName);

		deltas = new ArrayList<RequestPerformanceDelta>();
		for (RequestReport currentReport : currentReports.values()) {
			String name = currentReport.getName();
			RequestReport previousReport = previousReports.get(name);
			deltas.add(new RequestPerformanceDelta(
					name,
					currentReport.getPercentiles1().getTotal(),
					previousReport == null ? null : previousReport.getPercentiles1().getTotal()));
		}

		Collections.sort(deltas);	
	}

	public List<RequestPerformanceDelta> getDeltas() {
		return deltas;
	}

	private static Map<String, RequestReport> getDetailedReports(AbstractBuild<?, ?> build, String simName) {
		if (build == null) {
			return Collections.emptyMap();
		}

		GatlingBuildAction action = build.getAction(GatlingBuildAction.class);
		if (action == null) {
			return Collections.emptyMap();
		}

		BuildSimulation simulation = action.getSimulation(simName);
		if (simulation == null) {
			return Collections.emptyMap();
		}
		
		Map<String, RequestReport> reports = simulation.getDetailedReports();
		if (reports == null) {
			return Collections.emptyMap();
		}
		
		return reports;
	}
	
	public static class RequestPerformanceDelta implements Comparable<RequestPerformanceDelta> {
		private String name;
		private Long currentValue;
		private Long previousValue;

		public RequestPerformanceDelta(String name, Long currentValue, Long previousValue) {
			this.name = name;
			this.currentValue = currentValue;
			this.previousValue = previousValue;
		}

		public String getName() {
			return name;
		}

		public Long getCurrentValue() {
			return currentValue;
		}
		
		public String getFormattedCurrentValue() {
			return formatValue(currentValue);
		}

		public Long getPreviousValue() {
			return previousValue;
		}

		public String getFormattedPreviousValue() {
			return formatValue(previousValue);
		}

		public Float getRelativeDelta() {
			if (currentValue == null || previousValue == null) {
				return null;
			}
			return ((float) currentValue / (float) previousValue) - 1f;
		}

		public String getFormattedRelativeDelta() {
			Float delta = getRelativeDelta();
			return delta == null ? "N/A" : (String.format("%.2f", delta * 100f) + "%");

		}

		public int compareTo(RequestPerformanceDelta other) {
			return Float.compare(other.getRelativeDeltaForComparison(), this.getRelativeDeltaForComparison());
		}

		private String formatValue(Long value) {
			return value == null ? "N/A" : (value + "ms");
		}

		private float getRelativeDeltaForComparison() {
			Float relativeDelta = getRelativeDelta();
			return relativeDelta == null ? Float.MIN_VALUE : relativeDelta;
		}
	}
}
