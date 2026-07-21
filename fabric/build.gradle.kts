plugins {
    id("com.iamkaf.multiloader.fabric")
}

sourceSets {
    named("main") {
        java.exclude("com/iamkaf/bonded/fabric/BondedDatagen.java")
        java.exclude("com/iamkaf/bonded/fabric/datagen/**")
    }
}
