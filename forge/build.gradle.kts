import com.iamkaf.multiloader.support.MultiloaderProjectContext
import org.gradle.api.GradleException
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension

plugins {
    id("com.iamkaf.multiloader.forge")
}

val multiloader = MultiloaderProjectContext.of(project)
val catalog = mcCatalog()
val withLiteminer = providers.systemProperty("bonded.withLiteminer")
        .orElse(providers.gradleProperty("bonded.withLiteminer"))
        .map { it.toBoolean() }
        .orElse(false)
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
    fun addAvailableFixture(alias: String) {
        if (multiloader.versionOrNull(catalog, alias) != null) {
            runtimeOnly(catalog.findLibrary(alias).get())
        }
    }

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

    if ("basic-weapons" in compatFixtures) {
        addAvailableFixture("basic-weapons-forge")
        addAvailableFixture("khazodacore-forge")
    }
    if ("advanced-netherite" in compatFixtures) {
        addAvailableFixture("advanced-netherite-forge")
    }
    if ("immersive-armors" in compatFixtures) {
        addAvailableFixture("immersive-armors-forge")
    }
    if ("betterend" in compatFixtures) {
        addAvailableFixture("betterend-forge")
    }
    if ("betternether" in compatFixtures) {
        addAvailableFixture("betternether-forge")
    }
}
