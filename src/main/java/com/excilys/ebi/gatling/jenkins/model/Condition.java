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
package com.excilys.ebi.gatling.jenkins.model;

import com.excilys.ebi.gatling.jenkins.RequestReport;
import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.IOException;

public class Condition implements Describable<Condition> {

	private final Property property;
	private final long conditionThreshold;

	@DataBoundConstructor
	public Condition(Property property, int conditionThreshold) {
		this.property = property;
		this.conditionThreshold = conditionThreshold;
	}

	public Property getProperty() {
		return property;
	}

	public long getConditionThreshold() {
		return conditionThreshold;
	}

	public boolean isFulfilledBy(RequestReport report) {
		switch (property) {
			case ERROR_RATE:
				return (report.getNumberOfRequests().getKO() * 100. / report.getNumberOfRequests().getTotal()) < conditionThreshold;
			case MAX_RESPONSE_TIME:
				return report.getMaxResponseTime().getOK() < conditionThreshold;
			case MEAN_RESPONSE_TIME:
				return report.getMeanResponseTime().getOK() < conditionThreshold;
			case PERCENTILES_95:
				return report.getPercentiles95().getOK() < conditionThreshold;
			case PERCENTILES_99:
				return report.getPercentiles99().getOK() < conditionThreshold;
			case STANDARD_DEVIATION:
				return report.getStandardDeviation().getOK() < conditionThreshold;
			default:
				return false;
		}
	}

	public Descriptor<Condition> getDescriptor() {
		return Jenkins.getInstance().getDescriptor(Condition.class);
	}

	@Extension
	public static class DescriptorImpl extends Descriptor<Condition> {
		@Override
		public String getDisplayName() {
			return "A condition";
		}

		public FormValidation doCheckConditionThreshold(@QueryParameter String val) throws IOException, ServletException {
			try {
				long threshold = Long.parseLong(val);
				if (threshold >= 0)
					return FormValidation.ok();
				else
					return FormValidation.error(Messages.NEGATIVE_FIELDS_WARNING());
			} catch (NumberFormatException e) {
				return FormValidation.error(Messages.NEGATIVE_FIELDS_WARNING());
			}
		}
	}


}
