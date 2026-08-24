import com.iamkaf.multiloader.support.MultiloaderProjectContext
import org.gradle.api.GradleException
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension

plugins {
    id("com.iamkaf.multiloader.fabric")
}

val multiloader = MultiloaderProjectContext.of(project)
val catalog = mcCatalog()
val patchouli = catalog.findLibrary("patchouli-fabric")
val withLiteminer = providers.systemProperty("bonded.withLiteminer")
        .orElse(providers.gradleProperty("bonded.withLiteminer"))
        .map { it.toBoolean() }
        .orElse(false)
val withPatchouli = providers.systemProperty("bonded.withPatchouli")
        .orElse(providers.gradleProperty("bonded.withPatchouli"))
        .map { it.toBoolean() }
        .orElse(true)
val compatFixtures = providers.systemProperty("bonded.compatFixtures")
        .orElse(providers.gradleProperty("bonded.compatFixtures"))
        .orElse("")
        .map { value -> value.split(',').map { it.trim() }.filter { it.isNotEmpty() }.toSet() }
        .get()
val knownCompatFixtures = setOf(
        "basic-weapons",
        "advanced-netherite",
        "immersive-armors",
        "betterend",
        "betternether",
)
val unknownCompatFixtures = compatFixtures - knownCompatFixtures
if (unknownCompatFixtures.isNotEmpty()) {
    throw GradleException(
            "Unknown Bonded compatibility fixtures: ${unknownCompatFixtures.sorted().joinToString(", ")}. " +
                    "Expected any of: ${knownCompatFixtures.sorted().joinToString(", ")}."
    )
}

fun mcCatalog(): VersionCatalog {
    val catalogs = extensions.getByType<VersionCatalogsExtension>()
    val name = "libsMc${project.name.replace(".", "").replace("-", "")}"
    return catalogs.named(name)
}

dependencies {
    val runtimeConfiguration = if (multiloader.useUnobfuscatedMinecraft()) "runtimeOnly" else "modLocalRuntime"

    fun addAvailableFixture(alias: String) {
        if (multiloader.versionOrNull(catalog, alias) != null) {
            add(runtimeConfiguration, catalog.findLibrary(alias).get())
        }
    }

    compileOnly(
            "com.iamkaf.liteminer:liteminer-common:${multiloader.requiredProperty("dependencies.liteminer")}"
    ) {
        isTransitive = false
    }

    if (withLiteminer.get()) {
        add(
                runtimeConfiguration,
                "com.iamkaf.liteminer:liteminer-fabric:${multiloader.requiredProperty("dependencies.liteminer")}"
        )
    }

    if (withPatchouli.get() && patchouli.isPresent) {
        runtimeOnly(patchouli.get())
    }

    if ("basic-weapons" in compatFixtures) {
        addAvailableFixture("basic-weapons-fabric")
        addAvailableFixture("khazodacore-fabric")
    }
    if ("advanced-netherite" in compatFixtures) {
        addAvailableFixture("advanced-netherite-fabric")
    }
    if ("immersive-armors" in compatFixtures) {
        addAvailableFixture("immersive-armors-fabric")
    }
    if ("betterend" in compatFixtures) {
        addAvailableFixture("betterend-fabric")
    }
    if ("betternether" in compatFixtures) {
        addAvailableFixture("betternether-fabric")
    }
    if ("betterend" in compatFixtures || "betternether" in compatFixtures) {
        addAvailableFixture("bclib-fabric")
        addAvailableFixture("worldweaver-fabric")
    }
}

sourceSets {
    named("main") {
        java.exclude("com/iamkaf/bonded/fabric/BondedDatagen.java")
        java.exclude("com/iamkaf/bonded/fabric/datagen/**")
    }
}
