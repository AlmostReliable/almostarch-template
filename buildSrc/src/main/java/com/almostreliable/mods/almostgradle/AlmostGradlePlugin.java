package com.almostreliable.mods.almostgradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class AlmostGradlePlugin implements Plugin<Project> {

	public static final String TASK_GROUP = "AlmostGradle";

	@Override
	public void apply(Project project) {
		project.getTasks().create("updateMixinPackage", UpdateMixinPackageTask.class);
	}
}
