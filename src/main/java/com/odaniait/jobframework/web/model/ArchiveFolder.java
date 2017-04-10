package com.odaniait.jobframework.web.model;

import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

@Getter
public class ArchiveFolder {
	private String name;
	private String path;
	private Set<String> files = new HashSet<>();

	public ArchiveFolder(String name, String path) {
		this.name = name;
		this.path = path;
	}
}
