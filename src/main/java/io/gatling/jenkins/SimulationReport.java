/**
 * Copyright 2011-2014 eBusiness Information, Groupe Excilys (www.excilys.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gatling.jenkins;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import hudson.FilePath;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

public class SimulationReport {

  private final FilePath reportDirectory;

  private static final String GLOBAL_STATS_FILE_PATTERN = "**/global_stats.json";
  private static final String DETAILED_STATS_FILE_PATTERN = "**/stats.json";

  private RequestReport globalReport;
  private DetailedRequestReport detailedReports;

  private final String simulation;

  public SimulationReport(FilePath reportDirectory, String simulation) {
    this.reportDirectory = reportDirectory;
    this.simulation = simulation;
  }

  public void readGlobalStatsFile() throws IOException, InterruptedException {
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    File jsonFile = locateStatsFile(GLOBAL_STATS_FILE_PATTERN);
    globalReport = mapper.readValue(jsonFile, new TypeReference<RequestReport>() {
    });
  }

  public void readDetailedStatsFile() throws IOException, InterruptedException {
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
    File jsonFile = locateStatsFile(DETAILED_STATS_FILE_PATTERN);
    detailedReports = mapper.readValue(jsonFile, new TypeReference<DetailedRequestReport>() {
    });
  }

  private File locateStatsFile(String filePattern) throws IOException, InterruptedException {
    FilePath[] files = reportDirectory.list(filePattern);

    if (files.length == 0)
      throw new FileNotFoundException("Unable to locate the simulation results for " + simulation + ", pattern: " + filePattern);

    return new File(files[0].getRemote());
  }

  public String getSimulationPath() {
    return simulation;
  }

  public RequestReport getGlobalReport() {
    return globalReport;
  }

  public Map<String, RequestReport> getDetailedReports() {
    return detailedReports.getReports();
  }
}
