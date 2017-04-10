package com.odaniait.jobframework.web;

import com.odaniait.jobframework.models.Build;
import com.odaniait.jobframework.models.Pipeline;
import com.odaniait.jobframework.pipeline.PipelineManager;
import com.odaniait.jobframework.web.model.ArchiveFolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

@Controller
@RequestMapping("/pipelines/{pipelineId}/builds")
public class Archive {
	@Autowired
	private PipelineManager pipelineManager;

	@RequestMapping("/latest/archive")
	public String showLatest(@PathVariable String pipelineId, Model model) {
		Pipeline pipeline = pipelineManager.getPipeline(pipelineId);
		return show(pipelineId, pipeline.getState().getLastBuild().getBuildNr(), model);
	}

	@RequestMapping("/{buildId}/archive")
	public String show(@PathVariable String pipelineId, @PathVariable Integer buildId, Model model) {
		Pipeline pipeline = pipelineManager.getPipeline(pipelineId);
		Build build = pipeline.getState().getBuilds().get(buildId);

		if (build == null) {
			return "redirect:/pipelines/" + pipelineId;
		}

		Set<ArchiveFolder> archivedFiles = new HashSet<>();
		File archiveFolder = new File(build.getBuildDir() + "/archive");
		model.addAttribute("archivedFiles", archivedFiles(archiveFolder.getAbsoluteFile().toString(), archiveFolder, "root", archivedFiles));
		model.addAttribute("pipeline", pipeline);
		model.addAttribute("build", build);

		return "archive/show";
	}

	@RequestMapping("/latest/archiveFile")
	@ResponseBody
	public FileSystemResource latestArchiveFile(@PathVariable String pipelineId, @RequestParam("file") String fileName) {
		Pipeline pipeline = pipelineManager.getPipeline(pipelineId);
		return archiveFile(pipelineId, pipeline.getState().getLastBuild().getBuildNr(), fileName);
	}

	@RequestMapping("/{buildId}/archiveFile")
	@ResponseBody
	public FileSystemResource archiveFile(@PathVariable String pipelineId, @PathVariable Integer buildId,
																				@RequestParam("file") String fileName) {
		Pipeline pipeline = pipelineManager.getPipeline(pipelineId);
		Build build = pipeline.getState().getBuilds().get(buildId);

		if (build == null) {
			return null;
		}

		File archiveFolder = new File(build.getBuildDir() + "/archive");
		File archiveFile = new File(archiveFolder + "/" + fileName);
		return new FileSystemResource(archiveFile);
	}

	private Set<ArchiveFolder> archivedFiles(String basePath, File folder, String name, Set<ArchiveFolder> result) {
		File[] listOfFiles = folder.listFiles();

		assert listOfFiles != null;
		ArchiveFolder archiveFolder = new ArchiveFolder(name, folder.getAbsoluteFile().toString().replace(basePath, ""));
		result.add(archiveFolder);
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				archiveFolder.getFiles().add(listOfFiles[i].getName());
			} else if (listOfFiles[i].isDirectory()) {
				archivedFiles(basePath, listOfFiles[i], listOfFiles[i].getName(), result);
			}
		}

		return result;
	}
}
