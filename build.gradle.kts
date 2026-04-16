plugins {
    java
    application
}

repositories {
    mavenCentral()
}

dependencies {
}

application {
    mainClass.set("liza.Main")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "liza.Main"
    }
    from(sourceSets.main.get().output)
}
