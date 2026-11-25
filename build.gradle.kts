plugins {
    base
}

val modules = listOf("JobFinder", "JobFinder-CLI")

val staticAnalysis by tasks.registering {
    group = "verification"
    description = "Проверка метрик, оформления и компиляции Java-модулей."
    modules.forEach { dependsOn(gradle.includedBuild(it).task(":staticAnalysis")) }
}

tasks.check {
    dependsOn(staticAnalysis)
    modules.forEach { dependsOn(gradle.includedBuild(it).task(":check")) }
}

tasks.assemble {
    modules.forEach { dependsOn(gradle.includedBuild(it).task(":assemble")) }
}

tasks.clean {
    modules.forEach { dependsOn(gradle.includedBuild(it).task(":clean")) }
}
