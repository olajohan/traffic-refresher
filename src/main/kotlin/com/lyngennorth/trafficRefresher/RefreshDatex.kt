package com.lyngennorth.trafficRefresher

import com.google.cloud.firestore.GeoPoint
import com.google.firebase.cloud.FirestoreClient
import eu.datex2.schema._2._2_0.*
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class RefreshDatex {

    @Autowired
    lateinit var vegvesenService: VegvesenService
    private val firestore = FirestoreClient.getFirestore()

    private val TROMS_OG_FINNMARK = 54
    private val NORDLAND = 18

    private val countyNumbersToFollow = listOf(TROMS_OG_FINNMARK, NORDLAND)

    @Scheduled(fixedDelay = 1200_000L, initialDelay = 5000L) // 20 minutes = 1200_000 milli
    fun refreshDatex() {
        val d2LogicalModel = vegvesenService.getUpdate()
        if (d2LogicalModel != null) {

            runBlocking {
                firestore.collection("traffic").listDocuments().forEach { it.delete() }
            }

            (d2LogicalModel.payloadPublication as SituationPublication).situation.forEach { situation ->
                val situationReference = firestore.collection("traffic").document(situation.id)

                situation.situationRecord.forEach { situationRecord->
                    val countyNumber = (situationRecord.groupOfLocations as NetworkLocation).locationExtension.locationExtension.countyNumber.toInt()
                    if (countyNumbersToFollow.contains(countyNumber)) {
                        val overallStartTime = situationRecord.validity.validityTimeSpecification.overallStartTime.toGregorianCalendar().toInstant().epochSecond


                        val overallEndTime = if (situationRecord.validity.validityTimeSpecification.overallEndTime != null)
                            situationRecord.validity.validityTimeSpecification.overallEndTime.toGregorianCalendar().toInstant().epochSecond else 0L

                        // Find the correct icon based on which situation record we are dealing with
                        val iconType = when (situationRecord) {
                            // Only 3 main categories: TrafficElement, OperatorAction and Non-road event information
                            is OperatorAction -> "roadWork"
                            else -> "exclamation"
                        }

                        val iconLocation = (situationRecord.groupOfLocations as NetworkLocation).locationForDisplay.toGeoPoint()

                        var header = "NA"
                        var description = "NA"
                        situationRecord.generalPublicComment.forEach {
                            when (it.commentType) {
                                CommentTypeEnum.DESCRIPTION -> {
                                    description = it.comment.values.value.first().value
                                }
                                CommentTypeEnum.LOCATION_DESCRIPTOR -> {
                                    header = it.comment.values.value.first().value
                                }
                            }
                        }

                        val validTimes = mutableMapOf<String, MutableList<Pair<String, String>>>()
                        situationRecord.validity.validityTimeSpecification.validPeriod.forEach { validPeriod ->
                            validPeriod.recurringDayWeekMonthPeriod.forEach { dayWeekMonth ->
                                dayWeekMonth.applicableDay.forEach { day ->
                                    var startTime = "NA"
                                    var endTime = "NA"

                                    validPeriod.recurringTimePeriodOfDay.forEach {
                                        it as TimePeriodByHour
                                        startTime = "${it.startTimeOfPeriod.hour.toString().padStart(2, '0')}:${it.startTimeOfPeriod.minute.toString().padStart(2, '0')}"
                                        endTime = "${it.endTimeOfPeriod.hour.toString().padStart(2, '0')}:${it.endTimeOfPeriod.minute.toString().padStart(2, '0')}"
                                    }

                                    if (validTimes.containsKey(day.name)) {
                                        validTimes[day.name]?.add(Pair(startTime, endTime))
                                    } else {
                                        validTimes.putIfAbsent(day.name, mutableListOf(Pair(startTime, endTime)))
                                    }
                                }
                            }
                        }

                        val message = """
                            
                            ########################################################
                            Situation record id: ${situationRecord.id}
                            Location: $header
                            Icon type: $iconType
                            Overall start time: $overallStartTime
                            Overall end time  : $overallEndTime
                            Situation record class: ${situationRecord::class.java.simpleName}
                            Super class: ${situationRecord::class.java.superclass.simpleName}
                            Description: $description
                            County number: $countyNumber
                            Location: ${iconLocation.latitude}, ${iconLocation.longitude}
                            ########################################################
                            
                        """.trimIndent()

                        print(message)
                        situationReference.set(mapOf(
                            "overallSeverity" to situation.overallSeverity.name
                        ))
                        situationReference.collection("situationRecords").document(situationRecord.id).set(
                            mapOf(
                                "header" to header,
                                "overallStartTime" to overallStartTime,
                                "overallEndTime" to overallEndTime,
                                "iconType" to iconType,
                                "description" to description,
                                "county" to countyNumber.toString(),
                                "geoPoint" to iconLocation,
                                "validTimes" to validTimes
                            )
                        )
                    }
                }
            }
        }
    }
}

fun PointCoordinates.toGeoPoint(): GeoPoint {
    return GeoPoint(
        this.latitude.toDouble(),
        this.longitude.toDouble()
    )
}