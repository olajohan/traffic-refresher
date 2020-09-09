package com.lyngennorth.trafficRefresher

import com.google.cloud.firestore.GeoPoint
import com.google.firebase.cloud.FirestoreClient
import eu.datex.v220.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class RefreshDatex {

    @Autowired
    lateinit var vegvesenService: VegvesenService
    private val firestore = FirestoreClient.getFirestore()

    @Scheduled(fixedDelay = 600_000L, initialDelay = 5000L) // 10 minutes = 600_000 milli
    fun refreshDatex() {
        val d2LogicalModel = vegvesenService.getUpdate()
        if (d2LogicalModel != null) {
            val writeBatch = firestore.batch()
            (d2LogicalModel.payloadPublication as SituationPublication).situation.forEach { situation ->

                val situationReference = firestore.collection("traffic").document(situation.id)

                situation.situationRecord.forEach { situationRecord->

                    if (situationRecord.groupOfLocations is Linear && (situationRecord.groupOfLocations as Linear).locationForDisplay != null) {
                        val location = (situationRecord.groupOfLocations as Linear).locationForDisplay
                        println(" Lat: ${location.latitude}, lng: ${location.longitude}")
                    }

                    val overallStartTime = situationRecord.validity.validityTimeSpecification.overallStartTime
                    val overallEndTime = situationRecord.validity.validityTimeSpecification.overallEndTime

                    val comments = situationRecord.generalPublicComment.forEach { generalPublicComment ->
                        generalPublicComment.comment.values.value.forEach { line ->
                            line.value
                        }
                    }

                    when (situationRecord) {

                        // An event which is not planned by the traffic operator, which is affecting, or
                        // has the potential to affect traffic flow (note this does includes events planned by external
                        // organisations e.g. exhibitions, sports events etc.).
                        // This class is abstract.
                        // There are 6 kinds of traffic elements:
                        //  • Obstruction,
                        //  • Abnormal traffic,
                        //  • Accident,
                        //  • Equipment or system fault,
                        //  • Activities,
                        //  • Conditions
                        is TrafficElement -> {
                            when (situationRecord) {

                                // Any stationary or moving obstacle of a physical nature (e.g. obstacles or vehicles from
                                // an earlier accident, shed loads on carriageway, rock fall, abnormal or dangerous loads, or animals
                                // etc.) which could disrupt or endanger traffic.
                                // This class is abstract.
                                // For each obstruction type, the number of obstructions can be given.
                                // For each obstruction type, details can be given of:
                                //  • Mobility type: mobile/stationary,
                                // There are 5 kinds of obstructions:
                                //  • Animal presence (alive or not, presence type)
                                //  • Environmental obstruction (depth, type),
                                //  • Infrastructure damage obstruction (type),
                                //  • General obstruction (type),
                                //  • Vehicle obstruction (type + individual vehicles characteristics)
                                is Obstruction -> {
                                    when (situationRecord) {
                                        is AnimalPresenceObstruction -> {}
                                        is EnvironmentalObstruction -> {}
                                        is InfrastructureDamageObstruction -> {}
                                        is GeneralObstruction -> {}
                                        is VehicleObstruction -> {}
                                    }

                                }

                                // A traffic condition which is not normal.
                                // Details can be given: type, number of vehicles waiting, queue length, relative traffic flow, traffic flow
                                // characteristics, traffic trend type.
                                is AbnormalTraffic -> {}

                                // Accidents are events in which one or more vehicles lose control and do not recover. They
                                // include collisions between vehicle(s) or other road user(s), between vehicle(s) and any obstacle(s),
                                // or they result from a vehicle running off the road.
                                // Details can be given:
                                //  • cause type,
                                //  • accident type,
                                //  • overview of people involved (number, injury status, involvement role, type),
                                //  • overview of vehicles involved per type (number, status, type, usage),
                                //  • details of involved vehicles (type + individual vehicle characteristics)
                                is Accident -> {}

                                // Equipment or system which is faulty, malfunctioning or not in a fully
                                // operational state that may be of interest or concern to road operators and road users.
                                // The type of equipment or system and the type of fault must be given.
                                is EquipmentOrSystemFault -> {}

                                // Deliberate human actions external to the traffic stream or roadway which could disrupt
                                // traffic.
                                // Details must be given in term of: types of authority operations / disturbance / public event, and
                                // possibly in term of mobility(mobile/stationary).
                                is Activity -> {}

                                // Any conditions which have the potential to degrade normal driving conditions.
                                // A general indicator can be given with 8 possible values “drivingConditionType”:
                                //  • impossible,
                                //  • very hazardous,
                                //  • hazardous,
                                //  • passable with care,
                                //  • winter conditions (driving conditions are consistent with those expected in winter),
                                //  • normal,
                                //  • other,
                                //  • unknown.
                                // There are 3 conditions categories:
                                //  • road surface conditions that are related to the weather which may affect the driving
                                //    conditions, such as ice, snow or water,
                                //  • road surface conditions that are not related to the weather but which may affect driving
                                //    conditions, such as mud, oil or loose chippings…,
                                //  • environment conditions which may be affecting the driving conditions without being directly
                                // linked to the road (precipitation, visibility, pollution, temperature, wind)
                                is Conditions -> {
                                    when (situationRecord.drivingConditionType) {
                                        DrivingConditionTypeEnum.IMPOSSIBLE -> {}
                                        DrivingConditionTypeEnum.HAZARDOUS -> {}
                                        DrivingConditionTypeEnum.NORMAL -> {}
                                        DrivingConditionTypeEnum.PASSABLE_WITH_CARE -> {}
                                        DrivingConditionTypeEnum.UNKNOWN -> {}
                                        DrivingConditionTypeEnum.VERY_HAZARDOUS -> {}
                                        DrivingConditionTypeEnum.WINTER_CONDITIONS -> {}
                                        DrivingConditionTypeEnum.OTHER -> {}
                                    }
                                }
                            }
                        }

                        // Actions that a traffic operator can decide to implement to prevent or help
                        // correct dangerous or poor driving conditions, including maintenance of the road
                        // infrastructure.
                        // Operator action
                        // For each operator action type, details can be given of:
                        //  • Origin: internal / external,
                        //  • Status: requested, approved, being implemented, implemented, rejected, termination
                        //
                        // requested, being terminated,
                        //  • Considered action plan
                        // There are 4 kinds of operator actions:
                        //  • Roadworks,
                        //  • Sign setting,
                        //  • Network management,
                        //  • Roadside assistance.
                        is OperatorAction -> {
                            when(situationRecord) {

                                // Highway maintenance, installation and construction activities that may potentially
                                // affect traffic operations.
                                // This class is abstract.
                                // The effect on road layout is mandatory.
                                // Details can be given of:
                                //  • duration,
                                //  • scale: major / medium / minor
                                //  • under traffic or not,
                                //  • urgent or not,
                                //  • mobility type: mobile / stationary,
                                //  • construction work type: blasting / construction / demolition, road improvement, road
                                //    widening,
                                //  • road maintenance type (e.g. grass cutting, resurfacing, repair, road marking, salting…,
                                //  • subject type of works (e.g. bridge, crash barrier, gantry, road tunnel,…,
                                //  • information on associated maintenance vehicles
                                is Roadworks -> {

                                }

                                // Provides information on variable message and the information currently displayed. It
                                // uses the class VmsUnit.
                                is SignSetting -> {}


                                // Changes to the configuration or usability of the road network whether by
                                // legal order or by operational decisions. It includes road and lane closures, weight and dimensional
                                // restrictions, speed limits, vehicle restrictions, contra-flows and rerouting operations.
                                // There are 6 types of network management:
                                //  • Rerouting management (type, itinerary description, sign posted or not,…),
                                //  • Speed management (type, speed value),
                                //  • Road, carriageway or lane management (type, specified lane or carriageway, minimum
                                // number of persons in a vehicle required for HOV/car pool lane),
                                //  • Winter driving management (type),
                                //  • General instructions to road users (type),
                                //  • General network management (type of management and type of person that is manually
                                //    directing traffic in case of manually directed traffic).
                                is NetworkManagement -> {
                                    when (situationRecord) {
                                        is ReroutingManagement -> {}
                                        is SpeedManagement -> {}
                                        is RoadOrCarriagewayOrLaneManagement -> {}
                                        is WinterDrivingManagement -> {}
                                        is GeneralInstructionOrMessageToRoadUsers -> {}
                                        is GeneralNetworkManagement -> {}
                                    }
                                }
                                is RoadsideAssistance -> {}
                            }
                        }

                        // Information about an event which is not on the road, but
                        // which may influence the behaviour of drivers and hence the characteristics of the traffic
                        // flow.
                        is NonRoadEventInformation -> {}
                    }
                }
            }
            writeBatch.commit()
        }
    }
}