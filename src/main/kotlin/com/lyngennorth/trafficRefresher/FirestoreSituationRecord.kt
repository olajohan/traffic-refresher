package com.lyngennorth.trafficRefresher

import com.google.cloud.firestore.GeoPoint

data class FirestoreTrafficSituationRecord(
    val county: Int = 0,
    val description: String = "NA",
    val geoPoint: GeoPoint = GeoPoint(0.0, 0.0),
    val header: String = "NA",
    val iconType: String = "NA",
    val overallEndTime: Long = 0L,
    val overallStartTime: Long = 0L,
    val validTimes: Map<String, List<Pair<String, String>>> = emptyMap()
)