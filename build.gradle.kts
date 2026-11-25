// build.gradle.kts: how to compile, test, benchmark and run vector-lab.
// Useful commands:
//   ./gradlew check                     compile everything + run unit tests (what CI runs)
//   ./gradlew test --tests '*Flat*'     run some tests
//   ./gradlew run --args="--data data/siftsmall --index flat --k 10"
//   ./gradlew jmh                       run all JMH micro-benchmarks
//   ./gradlew jmh -PjmhArgs="DotProduct -f 1 -wi 3 -i 5"
//   ./gradlew tasks                     list every task

plugins {
    java         // compileJava, test, jar, the 'main' and 'test' source sets
    application  // the 'run' task for the Bench CLI
}

group = "io.github.fanagnou"
version = "0.1.0-SNAPSHOT"

java {
    // Toolchain: compile, test and run with JDK 21, whatever JDK runs Gradle itself.
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

// The Panama Vector API lives in an incubator module, which is not on the module path
// by default. Every compile/test/run/benchmark JVM needs this flag (slice 2 uses it).
val vectorModule = listOf("--add-modules", "jdk.incubator.vector")

// --- JMH: a third source set (src/jmh/java) for micro-benchmarks --------------------
// Done by hand instead of with a plugin: fewer moving parts, and it shows what a source set is.
val jmh: SourceSet by sourceSets.creating {
    compileClasspath += sourceSets.main.get().output
    runtimeClasspath += sourceSets.main.get().output
}

val jmhVersion = "1.37"

dependencies {
    // Main code: no dependencies on purpose (JDK only, like Lucene core).

    testImplementation(platform("org.junit:junit-bom:6.0.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    "jmhImplementation"("org.openjdk.jmh:jmh-core:$jmhVersion")
    // JMH generates benchmark harness classes at compile time from @Benchmark annotations.
    "jmhAnnotationProcessor"("org.openjdk.jmh:jmh-generator-annprocess:$jmhVersion")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(vectorModule)
    // -Xlint:all shows useful warnings ("-processing" mutes JMH's annotation-processor noise).
    // Expect one harmless line on every compile: "warning: using incubating module(s)".
    // It can't be switched off; it goes away when the Vector API leaves incubation.
    options.compilerArgs.add("-Xlint:all,-processing")
}

tasks.test {
    useJUnitPlatform()
    jvmArgs(vectorModule)
    maxHeapSize = "2g"
    // Replay a failing random test: ./gradlew test -Dtests.seed=<seed from the failure message>
    providers.systemProperty("tests.seed").orNull?.let { systemProperty("tests.seed", it) }
    testLogging {
        events("failed", "skipped")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

application {
    mainClass = "io.github.fanagnou.vectorlab.bench.Bench"
    // SIFT1M floats alone are ~0.5 GB; leave room for graphs and quantized copies.
    applicationDefaultJvmArgs = vectorModule + listOf("-Xmx8g")
}

// `./gradlew jmh` runs org.openjdk.jmh.Main on the jmh source set.
// JMH forks fresh JVMs for each benchmark; the benchmarks pass the module flag
// to those forks themselves via @Fork(jvmArgsAppend = ...).
tasks.register<JavaExec>("jmh") {
    group = "benchmark"
    description = "Runs JMH micro-benchmarks. Pass JMH options with -PjmhArgs=\"...\"."
    classpath = jmh.runtimeClasspath
    mainClass = "org.openjdk.jmh.Main"
    jvmArgs(vectorModule)
    val extra = providers.gradleProperty("jmhArgs").orNull
    if (extra != null) args(extra.split(" ").filter { it.isNotBlank() })
}

// CI compiles the benchmarks too, so they never rot.
tasks.check {
    dependsOn(jmh.classesTaskName)
}
