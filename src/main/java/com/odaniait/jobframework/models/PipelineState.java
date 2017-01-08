package com.odaniait.jobframework.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.odaniait.jobframework.exceptions.BuildException;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

@Data
@EqualsAndHashCode
public class PipelineState implements Serializable {
	@JsonIgnore private Logger logger = LoggerFactory.getLogger(PipelineState.class);
	@JsonIgnore private ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

	private int nextBuildNumber = 1;

	private Integer lastSuccessfulRun;
	private Integer lastFailedRun;
	private Long lastDuration;

	private CurrentState currentState = CurrentState.NOT_STARTED;
	private CurrentState lastState = CurrentState.NOT_STARTED;

	@JsonIgnore
	private File pipelineDirectory;

	@JsonIgnore
	private Map<Integer, Build> builds = new HashMap<>();

	public void save() throws IOException, BuildException {
		logger.info("Saving State");
		mapper.writeValue(new File(pipelineDirectory + "/state.yml"), this);
	}

	public void finish(Build build) throws IOException, BuildException {
		build.finish();

		if (build.getResultStatus().equals(ResultStatus.SUCCESS)) {
			lastSuccessfulRun = build.getBuildNr();
		} else {
			lastFailedRun = build.getBuildNr();
		}
		lastDuration = build.getDuration();
	}

	public void initialize(File pipelineDirectory, String pipelineWorkspacePath) throws IOException {
		this.pipelineDirectory = pipelineDirectory;

		// Read Builds
		File buildsPath = new File(pipelineDirectory + "/builds");
		if (buildsPath.isDirectory()) {
			File[] fList = buildsPath.listFiles();

			for (File file : fList) {
				File infoFile = new File(file, "/info.yml");
				if (infoFile.isFile()) {
					Build build = mapper.readValue(infoFile, Build.class);
					build.setBuildDir(file);
					build.setWorkspaceDir(new File(pipelineWorkspacePath + "/" + String.format("%08d", build.getBuildNr())));
					this.builds.put(build.getBuildNr(), build);
				}
			}

		}
	}

	public Build prepareBuild() throws IOException, BuildException {
		currentState = CurrentState.RUNNING;

		int currentBuildNumber = nextBuildNumber++;
		logger.info("Current Build Number " + currentBuildNumber + " (Next " + nextBuildNumber + ")");

		Build build = new Build();
		build.setBuildNr(currentBuildNumber);
		build.setBuildDir(new File(pipelineDirectory + "/builds/" + String.format("%08d", currentBuildNumber)));

		builds.put(currentBuildNumber, build);
		build.save();
		save();

		return build;
	}

	public void cleanupBuilds(Integer keepBuilds, File pipelineWorkspacePath) throws IOException {
		logger.info("Cleaning builds Current: " + builds.size() + " Max: " + keepBuilds);

		List<Integer> buildKeys = new ArrayList<>(builds.keySet());
		Collections.sort(buildKeys);
		Iterator<Integer> buildKeyIterator = buildKeys.iterator();

		while (builds.size() > keepBuilds) {
			Integer nextKey = buildKeyIterator.next();
			Build build = builds.get(nextKey);
			logger.info("Removing directory " + build.getBuildDir());
			FileUtils.deleteDirectory(build.getBuildDir());
			builds.remove(nextKey);

			if (nextKey.equals(lastSuccessfulRun)) {
				lastSuccessfulRun = null;
			}

			if (nextKey.equals(lastFailedRun)) {
				lastFailedRun = null;
			}
		}

		// Remove all workspaces that do not exist
		File[] fList = pipelineWorkspacePath.listFiles();

		if (fList != null) {
			for (File file : fList) {
				if (file.isDirectory()) {
					if (!builds.containsKey(Integer.parseInt(file.getName()))) {
						logger.info("Removing build directory " + file);
						FileUtils.deleteDirectory(file);
					}
				}
			}
		}
	}

	@JsonIgnore
	public Build getLastBuild() {
		List<Integer> buildKeys = new ArrayList<>(builds.keySet());
		Collections.sort(buildKeys);

		if (buildKeys.isEmpty()) {
			return null;
		}

		Integer lastIdx = buildKeys.get(buildKeys.size() - 1);

		return builds.get(lastIdx);
	}

	@JsonIgnore
	public List<Build> getLastBuilds() {
		List<Build> lastBuilds = new ArrayList<>();

		List<Integer> buildKeys = new ArrayList<>(builds.keySet());
		Collections.sort(buildKeys);

		if (buildKeys.isEmpty()) {
			return lastBuilds;
		}

		Integer lastIdx = buildKeys.get(buildKeys.size() - 1);
		Integer start = lastIdx - 10;

		for (int i = start ; i < lastIdx + 1 ; i++) {
			Build build = builds.get(i);

			if (build != null) {
				lastBuilds.add(build);
			}
		}


		return lastBuilds;
	}
}
