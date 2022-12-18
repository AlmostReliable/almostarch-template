package com.almostreliable.mods.almostgradle;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import javax.annotation.Nullable;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public class UpdateMixinPackageTask extends DefaultTask {

	private String baseResourcePath = "src/main/resources";
	@Nullable private List<String> mixinFileNames = null;
	@Nullable private String mixinPackage = null;
	private boolean includeProjectNameAsSubpackage = false;

	public UpdateMixinPackageTask() {
		setGroup(AlmostGradlePlugin.TASK_GROUP);
		setDescription("Updates the package name in all mixin json files");
	}

	public void setBaseResourcePath(String baseResourcePath) {
		this.baseResourcePath = Objects.requireNonNull(baseResourcePath);
	}

	public void mixinFileNames(List<String> mixinFileNames) {
		this.mixinFileNames = Objects.requireNonNull(mixinFileNames);
	}

	public void mixinFileNames(String... mixinFileNames) {
		this.mixinFileNames = List.of(mixinFileNames);
	}

	public void mixinPackage(String mixinPackage) {
		this.mixinPackage = Objects.requireNonNull(mixinPackage);
	}

	public void projectAsSubpackage(boolean flag) {
		this.includeProjectNameAsSubpackage = flag;
	}

	@TaskAction
	public void invoke() throws IOException {
		for (String mixinFileName : getMixinFileNames()) {
			var filePath = Path.of(getProject().getProjectDir().getAbsolutePath(), baseResourcePath, mixinFileName);
			invokeEachFile(filePath);
		}
	}

	private void invokeEachFile(Path filePath) throws IOException {
		var file = filePath.toFile();
		if (!file.exists()) {
			throw new IllegalStateException("Mixin file " + file.getAbsolutePath() + " does not exist");
		}

		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		var json = gson.fromJson(new FileReader(filePath.toString(), StandardCharsets.UTF_8), JsonObject.class);
		if (!json.has("package")) {
			return;
		}
		json.addProperty("package", getMixinPackage());
		Files.writeString(filePath, gson.toJson(json) + "\n"); // add a newline at the end
	}

	private List<String> getMixinFileNames() {
		if (mixinFileNames == null) {
			throw new IllegalStateException("Mixin file names not set. use `setMixinFileNames`");
		}

		return mixinFileNames;
	}

	private String getMixinPackage() {
		if (mixinPackage == null) {
			throw new IllegalStateException("Mixin package not set. use `setMixinPackage`");
		}

		String subpackage = "";
		if (includeProjectNameAsSubpackage) {
			subpackage = "." + getProject().getName().toLowerCase();
		}
		return mixinPackage + subpackage + ".mixin";
	}
}
