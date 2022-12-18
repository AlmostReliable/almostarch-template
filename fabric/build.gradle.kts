val enabledPlatforms: String by project
val fabricLoaderVersion: String by project
val fabricApiVersion: String by project
val architecturyVersion: String by project

plugins {
	id("com.github.johnrengelman.shadow") version ("7.1.2")
}

architectury {
	platformSetupLoomIde()
	fabric()
}

loom {
	if (project.findProperty("enableAccessWidener") == "true") { // Optional property for `gradle.properties` to enable access wideners.
		accessWidenerPath.set(project(":common").loom.accessWidenerPath)
		println("Access widener enabled for project ${project.name}. Access widener path: ${loom.accessWidenerPath.get()}")
	}
}

val common by configurations
val shadowCommon by configurations
dependencies {
	modImplementation("net.fabricmc:fabric-loader:${fabricLoaderVersion}")
	modApi("net.fabricmc.fabric-api:fabric-api:${fabricApiVersion}")

	// Remove the next line if you don't want to depend on the API
	modApi("dev.architectury:architectury-fabric:${architecturyVersion}")

	common(project(":common", "namedElements")) { isTransitive = false }
	shadowCommon(project(":common", "transformProductionFabric")) { isTransitive = false }
}
