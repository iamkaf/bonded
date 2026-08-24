import com.iamkaf.multiloader.support.MultiloaderProjectContext

plugins {
    id("com.iamkaf.multiloader.common")
}

val multiloader = MultiloaderProjectContext.of(project)

dependencies {
    compileOnly(
            "com.iamkaf.liteminer:liteminer-common:${multiloader.requiredProperty("dependencies.liteminer")}"
    ) {
        isTransitive = false
    }

    testImplementation("org.junit.jupiter:junit-jupiter:6.1.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:6.1.0")
}

tasks.test {
    useJUnitPlatform()
}

multiloaderCommon {
    resourcesFrom("src/main/generated")
}
