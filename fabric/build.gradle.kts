import com.iamkaf.multiloader.support.MultiloaderProjectContext

plugins {
    id("com.iamkaf.multiloader.fabric")
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
        val configuration = if (multiloader.useUnobfuscatedMinecraft()) "runtimeOnly" else "modLocalRuntime"
        add(
                configuration,
                "com.iamkaf.liteminer:liteminer-fabric:${multiloader.requiredProperty("dependencies.liteminer")}"
        )
    }
}

sourceSets {
    named("main") {
        java.exclude("com/iamkaf/bonded/fabric/BondedDatagen.java")
        java.exclude("com/iamkaf/bonded/fabric/datagen/**")
    }
}
