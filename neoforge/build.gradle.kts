import com.iamkaf.multiloader.support.MultiloaderProjectContext
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension

plugins {
    id("com.iamkaf.multiloader.neoforge")
}

val multiloader = MultiloaderProjectContext.of(project)
val catalog = mcCatalog()
val patchouli = catalog.findLibrary("patchouli-neoforge")
val withLiteminer = providers.systemProperty("bonded.withLiteminer")
        .orElse(providers.gradleProperty("bonded.withLiteminer"))
        .map { it.toBoolean() }
        .orElse(false)
val withPatchouli = providers.systemProperty("bonded.withPatchouli")
        .orElse(providers.gradleProperty("bonded.withPatchouli"))
        .map { it.toBoolean() }
        .orElse(true)

fun mcCatalog(): VersionCatalog {
    val catalogs = extensions.getByType<VersionCatalogsExtension>()
    val name = "libsMc${project.name.replace(".", "").replace("-", "")}"
    return catalogs.named(name)
}

dependencies {
    compileOnly(
            "com.iamkaf.liteminer:liteminer-common:${multiloader.requiredProperty("dependencies.liteminer")}"
    ) {
        isTransitive = false
    }

    if (withLiteminer.get()) {
        runtimeOnly(
                "com.iamkaf.liteminer:liteminer-neoforge:${multiloader.requiredProperty("dependencies.liteminer")}"
        )
    }

    if (withPatchouli.get() && patchouli.isPresent) {
        runtimeOnly(patchouli.get())
    }
}
