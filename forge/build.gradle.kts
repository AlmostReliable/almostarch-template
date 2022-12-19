val minecraftVersion: String by project
val forgeVersion: String by project
val modId: String by project


plugins {
    id("com.github.johnrengelman.shadow") version ("7.1.2")
}

architectury {
    platformSetupLoomIde()
    forge()
}

loom {
    if (project.findProperty("enableAccessWidener") == "true") { // Optional property for `gradle.properties` to enable access wideners.
        accessWidenerPath.set(project(":common").loom.accessWidenerPath)
        forge {
            convertAccessWideners.set(true)
            extraAccessWideners.add(loom.accessWidenerPath.get().asFile.name)
        }
        println("Access widener enabled for project ${project.name}. Access widener path: ${loom.accessWidenerPath.get()}")
    }

    forge {
        mixinConfigs("$modId-common.mixins.json", "$modId-forge.mixins.json")
    }
}

val common by configurations
val shadowCommon by configurations
dependencies {
    forge("net.minecraftforge:forge:$minecraftVersion-$forgeVersion")

    common(project(":common", "namedElements")) { isTransitive = false }
    shadowCommon(project(":common", "transformProductionForge")) { isTransitive = false }
}
