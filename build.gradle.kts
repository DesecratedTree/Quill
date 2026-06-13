import proguard.gradle.ProGuardTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.File
import org.gradle.jvm.toolchain.JavaLanguageVersion

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("com.guardsquare:proguard-gradle:7.7.0")
    }
}

plugins {
    id("java")
    kotlin("jvm")
}

group = "com.desecratedtree"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val lwjglVersion = "3.3.6"
val lwjglNatives = when {
    org.gradle.internal.os.OperatingSystem.current().isWindows -> "natives-windows"
    org.gradle.internal.os.OperatingSystem.current().isMacOsX -> "natives-macos"
    else -> "natives-linux"
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("com.displee:rs-cache-library:8.1.0")
    implementation("com.displee:disio:2.2")
    implementation("com.formdev:flatlaf:3.5.1")
    implementation("io.netty:netty-buffer:4.1.118.Final")
    implementation("org.apache.logging.log4j:log4j-api:2.24.3")
    implementation("org.apache.commons:commons-lang3:3.10")
    implementation("com.moandjiezana.toml:toml4j:0.7.2")
    implementation(kotlin("stdlib-jdk8"))
    implementation(platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))
    implementation("org.lwjgl:lwjgl")
    implementation("org.lwjgl:lwjgl-jawt")
    implementation("org.lwjgl:lwjgl-opengl")
    implementation("org.lwjglx:lwjgl3-awt:0.2.4") {
        exclude(group = "org.lwjgl")
    }
    runtimeOnly("org.lwjgl:lwjgl::$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-opengl::$lwjglNatives")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

sourceSets.named("main") {
    java {
        srcDir("src/main/java")
        srcDir("_reference_cs2_editor/src/main/java")
        include("com/**")
        include("net/**")
        include("io/**")
        include("dawn/cs2/**")
        exclude("com/displee/**")
        exclude("**/*.kt")
    }
    resources {
        srcDir("src/main/resources")
        srcDir("_reference_cs2_editor/src/main/resources")
        include("**")
    }
}

tasks.withType<JavaCompile>().configureEach {
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
    options.encoding = "UTF-8"
}

val lintClassesDir = layout.buildDirectory.dir("tmp/lint-classes")

tasks.test {
    useJUnitPlatform()
}

tasks.processResources {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(layout.projectDirectory.file("changelog.md"))
}

tasks.register<JavaCompile>("lintJava") {
    group = "verification"
    description = "Runs javac with -Xlint across the main source set."
    source = sourceSets["main"].allJava
    classpath = sourceSets["main"].compileClasspath
    destinationDirectory.set(lintClassesDir)
    options.compilerArgs.addAll(listOf("-Xlint:all", "-Xlint:-options"))
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
}

tasks.register("lint") {
    group = "verification"
    description = "Runs the project's static compilation lint checks."
    dependsOn("lintJava")
}

tasks.register<JavaExec>("dumpItemSprites") {
    group = "application"
    description = "Dumps inventory sprite PNGs for every item ID."
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.desecratedtree.quill.tools.ItemSpriteDumpMain")
    args(
        (project.findProperty("cachePath") as String?) ?: "data/cache",
        (project.findProperty("outputDir") as String?) ?: "dump/itemsprites"
    )
}

tasks.jar {
    archiveFileName.set("Quill-main.jar")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    exclude("module-info.class", "META-INF/versions/**")
    manifest {
        attributes["Main-Class"] = "com.desecratedtree.quill.Main"
    }
    from({
        configurations.runtimeClasspath.get()
                .filter { it.exists() }
                .map { if (it.isDirectory) it else zipTree(it) }
    })
}

tasks.register<ProGuardTask>("proguardJar") {
    group = "build"
    description = "Builds a processed application jar in out/Quill-main.jar."
    dependsOn(tasks.jar)
    outputs.upToDateWhen { false }
    injars(tasks.jar.flatMap { it.archiveFile })
    outjars(layout.projectDirectory.file("out/Quill-main.jar"))
    val java8Home = javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(8))
    }.get().metadata.installationPath.asFile
    val jmodsDir = File(java8Home, "jmods")
    if (jmodsDir.isDirectory) {
        jmodsDir.listFiles { file -> file.extension == "jmod" }
            ?.sortedBy { it.name }
            ?.forEach {
                libraryjars(
                    mapOf("jmodfilter" to "!**.jar,!module-info.class"),
                    it
                )
            }
    } else {
        val rtJar = listOf(
            File(java8Home, "jre/lib/rt.jar"),
            File(java8Home, "lib/rt.jar")
        ).firstOrNull { it.isFile }
        if (rtJar != null) {
            libraryjars(rtJar)
        }
    }
    configuration("proguard.pro")
    doFirst {
        layout.projectDirectory.file("out/Quill-main.jar").asFile.delete()
    }
}

tasks.assemble {
    dependsOn("proguardJar")
}

kotlin {
    jvmToolchain(8)
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_1_8)
    }
    sourceSets.named("main") {
        kotlin.srcDir("src/main/java")
        kotlin.exclude("_reference_cs2_editor/**")
    }
    sourceSets.named("test") {
        kotlin.srcDir("src/test/java")
    }
}
