import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.testing.Test

val metrics = tasks.register<Exec>("codeMetrics") {
    group = "verification"
    description = "Цикломатическая сложность и метрики Холстеда."
    workingDir(rootProject.projectDir.parentFile)
    commandLine("bash", "quality/metrics.sh", rootProject.projectDir.absolutePath)
}

val analysis = tasks.register("staticAnalysis") {
    group = "verification"
    description = "Метрики, оформление и компиляция Java-кода и тестов."
    dependsOn(metrics, "spotlessJavaCheck", "classes", "testClasses")
}

tasks.named("check") {
    dependsOn(analysis)
}

tasks.withType<Test>().configureEach {
    dependsOn(analysis)
}
