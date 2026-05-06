plugins {
    alias(libs.plugins.shadow)
    alias(libs.plugins.run.paper)
}

dependencies {
    implementation(project(":core"))
    
    // Paper specific
    compileOnly(libs.paper.api)
    
    // Command Framework (Lamp) & XSeries
    implementation(libs.lamp.common)
    implementation(libs.lamp.bukkit)
    implementation(libs.xseries)
    implementation(libs.adventure.minimessage)
    implementation(libs.adventure.api)
    implementation(libs.adventure.platform.bukkit)

    // Packet Handling (ProtocolLib & PacketEvents)
    compileOnly(libs.protocollib)
    implementation(libs.packetevents.spigot)

    testImplementation(libs.junit.api)
    testRuntimeOnly(libs.junit.engine)
}

tasks {
    shadowJar {
        archiveFileName.set("gnluckyblock.jar")
        archiveClassifier.set("")
        
        // Relocate dependencies to avoid conflicts
        relocate("com.zaxxer.hikari", "com.gn027c.luckyblock.libs.hikari")
        relocate("org.flywaydb", "com.gn027c.luckyblock.libs.flyway")
        relocate("com.cryptomorin.xseries", "com.gn027c.luckyblock.libs.xseries")
        relocate("revxrsal.commands", "com.gn027c.luckyblock.libs.lamp")
    }
    
    build {
        dependsOn(shadowJar)
    }
}
