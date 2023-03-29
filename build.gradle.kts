import kr.entree.spigradle.kotlin.spigot
import kr.entree.spigradle.kotlin.spigotmc
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	idea
	kotlin("jvm") version "1.7.20"
	id("com.github.johnrengelman.shadow") version "7.1.2"
	id("kr.entree.spigradle") version "2.4.2"
}

group = "nl.dantevg"
version = "1.4.0"
java.sourceCompatibility = JavaVersion.VERSION_1_8

repositories {
	mavenCentral()
	mavenLocal()
	spigotmc()
}

dependencies {
	implementation(kotlin("stdlib-jdk8"))
	shadow(kotlin("stdlib-jdk8"))
	
	compileOnly(spigot("1.13.2"))
}

tasks.withType<KotlinCompile> {
	kotlinOptions.jvmTarget = "1.8"
}

tasks.shadowJar {
	minimize()
	archiveClassifier.set("")
}

tasks.build {
	dependsOn("shadowJar")
}

idea.module {
	isDownloadJavadoc = true
	isDownloadSources = true
}

spigot {
	apiVersion = "1.13"
	description = "Automatically export dynmap tiles periodically"
	authors = listOf("RedPolygon")
	website = "dantevg.nl/mods-plugins/DynmapExport"
	softDepends = listOf("dynmap")
	commands {
		create("dynmapexport") {
			description = "DynmapExport command"
			usage = """
                Usage:
                /dynmapexport now
                /dynmapexport export <world> <map> <x> <z> <zoom>
                /dynmapexport reload
                /dynmapexport worldtomap <world> <map> <x> <y> <z> [zoom]
                /dynmapexport purge [all]
            """.trimIndent()
			permission = "dynmapexport.*"
		}
	}
	permissions {
		create("dynmapexport.*") {
			description = "Allows running /dynmapexport command"
		}
	}
}
