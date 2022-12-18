import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.fabricmc.loom.task.RemapJarTask

val license: String by project
val fabricLoaderVersion: String by project
val fabricApiVersion: String by project
val forgeVersion: String by project
val minecraftVersion: String by project
val modVersion: String by project
val modId: String by project
val modName: String by project
val modDescription: String by project
val modAuthor: String by project
val githubRepo: String by project
val githubUser: String by project

plugins {
	java
	`maven-publish`
	id("architectury-plugin") version ("3.4-SNAPSHOT")
	id("io.github.juuxel.loom-quiltflower") version "1.8.0" apply false
	id("dev.architectury.loom") version ("0.12.0-SNAPSHOT") apply false
	id("com.github.johnrengelman.shadow") version "7.1.2" apply false
}

architectury {
	minecraft = minecraftVersion
}

val extraModsPrefix = "extra-mods"

allprojects {
	apply(plugin = "java")
	apply(plugin = "architectury-plugin")
	apply(plugin = "maven-publish")

	repositories {
		mavenLocal()
		mavenCentral()
		flatDir {
			name = extraModsPrefix
			dir(file("$extraModsPrefix-$minecraftVersion"))
		}
	}

	tasks {
		withType<JavaCompile> {
			options.encoding = "UTF-8"
			options.release.set(17)
		}
	}

	extensions.configure<JavaPluginExtension> {
		toolchain.languageVersion.set(JavaLanguageVersion.of(17))
		withSourcesJar()
	}
}

subprojects {
	apply(plugin = "java")
	apply(plugin = "dev.architectury.loom")
	apply(plugin = "maven-publish")
	apply(plugin = "io.github.juuxel.loom-quiltflower")

	base.archivesName.set("$modId-${project.name.toLowerCase()}")
	version = "$minecraftVersion-$modVersion"

	val loom = project.extensions.getByName<net.fabricmc.loom.api.LoomGradleExtensionAPI>("loom")
	loom.silentMojangMappingsLicense()

	/**
	 * General dependencies we want to use for all subprojects. E.g. mappings or the minecraft version.
	 */
	dependencies {
		/**
		 * Kotlin accessor methods are not generated in this gradle, but we can access them through quoted names.
		 */
		"minecraft"("com.mojang:minecraft:${minecraftVersion}")
		"mappings"(loom.officialMojangMappings())

		/**
		 * Helps to load mods in development through an extra directory. Sadly this does not support transitive dependencies. :-(
		 */
		fileTree("$extraModsPrefix-$minecraftVersion") { include("**/*.jar") }
			.forEach { f ->
				val sepIndex = f.nameWithoutExtension.lastIndexOf('-');
				if (sepIndex == -1) {
					throw IllegalArgumentException("Invalid mod name: '${f.nameWithoutExtension}'. Expected format: 'modName-version.jar'")
				}
				val mod = f.nameWithoutExtension.substring(0, sepIndex);
				val version = f.nameWithoutExtension.substring(sepIndex + 1);
				println("Extra mod ${f.nameWithoutExtension} detected.")
				"modLocalRuntime"("extra-mods:$mod:$version")
			}

		/**
		 * Non Minecraft dependencies
		 */
		compileOnly("com.google.auto.service:auto-service:1.0.1")
		annotationProcessor("com.google.auto.service:auto-service:1.0.1")
	}

	/**
	 * Maven publishing
	 */
	publishing {
		publications {
			val mpm = project.properties["maven-publish-method"] as String;
			println("[Publish Task] Publishing method for project '${project.name}: $mpm")
			register(mpm, MavenPublication::class) {
				artifactId = base.archivesName.get()
				from(components["java"])
			}
		}

		// See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
		repositories {
			// Add repositories to publish to here.
		}
	}

	/**
	 * Disabling the runtime transformer from Architectury
	 * When runtime transformer should be enabled again, remove this block. And add the following to the corresponding subproject:
	 *
	 * configurations {
	 *      "developmentFabric" { extendsFrom(configurations["common"]) } // or "developmentForge" for Forge
	 * }
	 */
	architectury {
		compileOnly()
	}

	/**
	 * Resource processing for defined targets. This will replace `${key}` with the given values from the map below.
	 */
	tasks.processResources {
		val resourceTargets = listOf("META-INF/mods.toml", "pack.mcmeta", "fabric.mod.json")

		val replaceProperties = mapOf(
			"version" to project.version as String,
			"license" to license,
			"modId" to modId,
			"modName" to modName,
			"minecraftVersion" to minecraftVersion,
			"modAuthor" to modAuthor,
			"modDescription" to modDescription,
			"fabricApiVersion" to fabricApiVersion,
			"forgeVersion" to forgeVersion,
			"forgeFMLVersion" to forgeVersion.substringBefore("."), // Only use major version as FML error message sucks and the error message for wrong forge version is way better.
			"githubUser" to githubUser,
			"githubRepo" to githubRepo
		)

		println("[Process Resources] Replacing properties in resources: ")
		replaceProperties.forEach { (key, value) -> println("\t -> $key = $value") }

		inputs.properties(replaceProperties)
		filesMatching(resourceTargets) {
			expand(replaceProperties)
		}
	}
}

/**
 * Subproject configurations and tasks we only want to apply to subprojects which are not the common project. E.g. fabric or forge.
 */
subprojects {
	if (project.path != ":common") {
		apply(plugin = "com.github.johnrengelman.shadow")

		val common by configurations.creating
		val shadowCommon by configurations.creating // Don't use shadow from the shadow plugin because we don't want IDEA to index this.
		configurations {
			"compileClasspath" { extendsFrom(common) }
			"runtimeClasspath" { extendsFrom(common) }
		}

		with(components["java"] as AdhocComponentWithVariants) {
			withVariantsFromConfiguration(configurations["shadowRuntimeElements"]) { skip() }
		}

		tasks {
			named<ShadowJar>("shadowJar") {
				exclude("architectury.common.json")
				configurations = listOf(shadowCommon)
				archiveClassifier.set("dev-shadow")
			}

			named<RemapJarTask>("remapJar") {
				inputFile.set(named<ShadowJar>("shadowJar").get().archiveFile)
				dependsOn("shadowJar")
				classifier = null
			}

			named<Jar>("jar") {
				archiveClassifier.set("dev")
			}

			named<Jar>("sourcesJar") {
				val commonSources = project(":common").tasks.named<Jar>("sourcesJar")
				dependsOn(commonSources)
				from(commonSources.get().archiveFile.map { zipTree(it) })
				archiveClassifier.set("sources")
			}
		}
	}
}
