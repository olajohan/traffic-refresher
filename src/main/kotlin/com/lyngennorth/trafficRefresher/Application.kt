package com.lyngennorth.trafficRefresher

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.springframework.boot.Banner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling
import java.io.File


@EnableScheduling
@SpringBootApplication
open class Application

fun main(args: Array<String>) {

    val googleCreds = GoogleCredentials.fromStream(File("/secrets/gcloud-service-account").inputStream())
    val options = FirebaseOptions.builder()
        .setCredentials(googleCreds)
        .setProjectId("aurora-alarm")
        .build()
    FirebaseApp.initializeApp(options)

    runApplication<Application>(*args) {
        setBannerMode(Banner.Mode.OFF)
    }
}