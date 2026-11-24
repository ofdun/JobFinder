import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.testing.Test

val testSourceSet = extensions.getByType<SourceSetContainer>().named("test")

tasks.register<Test>("e2eTest") {
    group = "verification"
    description = "Run the HTTP acceptance scenario against an isolated application and databases."
    testClassesDirs = testSourceSet.get().output.classesDirs
    classpath = testSourceSet.get().runtimeClasspath
    include("**/*E2E.class")
    shouldRunAfter(tasks.named("integrationTest"))
}

tasks.named("testAll") {
    description = "Run unit, integration and HTTP acceptance tests (requires Docker, curl, jq and the Ollama image)."
    dependsOn(tasks.named("e2eTest"))
}

tasks.register<Test>("bddTest") {
    group = "verification"
    description = "Run authentication BDD scenarios against an isolated application."
    testClassesDirs = testSourceSet.get().output.classesDirs
    classpath = testSourceSet.get().runtimeClasspath
    include("**/AuthenticationBddE2E.class")
    useJUnitPlatform {
        includeEngines("junit-platform-suite")
    }
}
