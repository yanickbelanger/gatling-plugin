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

import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Simulation implements Describable<Simulation> {
	private final String name;
	private final List<Condition> failConditions;
	private final List<Condition> unstableConditions;

	@DataBoundConstructor
	public Simulation(String name, List<Condition> failConditions, List<Condition> unstableConditions) {
		this.name = name;

		if (failConditions == null)
			this.failConditions = new ArrayList<Condition>();
		else
			this.failConditions = failConditions;

		if (unstableConditions == null)
			this.unstableConditions = new ArrayList<Condition>();
		else
			this.unstableConditions = unstableConditions;
	}

	public String getName() {
		return name;
	}

	public List<Condition> getFailConditions() {
		return failConditions;
	}

	public List<Condition> getUnstableConditions() {
		return unstableConditions;
	}

	public Descriptor<Simulation> getDescriptor() {
		return Jenkins.getInstance().getDescriptor(Simulation.class);
	}

	@Extension
	public static class DescriptorImpl extends Descriptor<Simulation> {
		@Override
		public String getDisplayName() {
			return "My wonderful simulation";
		}

		public FormValidation doCheckName(@QueryParameter String val) throws IOException, ServletException {
			if (val != null && val.trim() != "")
				return FormValidation.ok();
			else
				return FormValidation.error(Messages.EMPTY_FIELDS_WARNING());
		}
	}
}
