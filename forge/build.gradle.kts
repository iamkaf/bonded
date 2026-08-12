import com.iamkaf.multiloader.support.MultiloaderProjectContext

plugins {
    id("com.iamkaf.multiloader.forge")
}

val multiloader = MultiloaderProjectContext.of(project)
val withLiteminer = providers.systemProperty("bonded.withLiteminer")
        .orElse(providers.gradleProperty("bonded.withLiteminer"))
        .map { it.toBoolean() }
        .orElse(false)

dependencies {
    compileOnly(
            "com.iamkaf.liteminer:liteminer-common:${multiloader.requiredProperty("dependencies.liteminer")}"
    ) {
        isTransitive = false
    }

    if (withLiteminer.get()) {
        runtimeOnly(
                "com.iamkaf.liteminer:liteminer-forge:${multiloader.requiredProperty("dependencies.liteminer")}"
        )
    }
}
