dependencies {
    api(project(":api"))
    
    // Database & Utils
    implementation(libs.hikari)
    implementation(libs.flyway.core)
    implementation(libs.flyway.mysql)
    
    // Commands (Core annotations) - removed
    // Text support
    implementation(libs.adventure.minimessage)

    testImplementation(libs.junit.api)
    testRuntimeOnly(libs.junit.engine)
}
