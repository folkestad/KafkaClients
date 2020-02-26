plugins {
    kotlin("jvm") version "1.3.61"
}

group = "org.folkestad"
version = "1.0.0"

repositories {
    mavenCentral()
    mavenLocal()
    jcenter()
    maven { url = uri("https://packages.confluent.io/maven/") }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.apache.kafka:kafka-clients:2.4.0")
    implementation("org.apache.kafka:kafka-streams:2.4.0")
    implementation("io.confluent:kafka-avro-serializer:5.3.0")
    implementation("io.github.config4k:config4k:0.4.2")
    implementation("org.apache.avro:avro:1.9.1")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}