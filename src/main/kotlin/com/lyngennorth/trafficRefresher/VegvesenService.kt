package com.lyngennorth.trafficRefresher

import eu.datex.v220.D2LogicalModel
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class VegvesenService(private val restTemplateBuilder: RestTemplateBuilder) {

    private var template: RestTemplate = restTemplateBuilder
        .rootUri("https://vegvesen.no/")
        .basicAuthentication(System.getenv("DATEX_USERNAME"), System.getenv("DATEX_PASSWORD"))
        .build()

    fun getUpdate(): D2LogicalModel? {
        return this.template.getForObject("/ws/no/vegvesen/veg/trafikkpublikasjon/trafikk/2/GetSituation", D2LogicalModel::class.java)
    }
}