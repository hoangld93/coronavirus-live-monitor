package com.snackapp.coronavirus.network

import com.snackapp.coronavirus.model.Result
import retrofit2.http.GET
import rx.Observable

interface ServerApi {

    @get:GET("/$GET_LIST_COUNTRY")
    val coronaAttributes: Observable<Result>

    @get:GET("/$GET_LIST_IN_CHINA")
    val coronaAttributesInChina: Observable<Result>

    @get:GET("/$GET_LIST_CORONA_CITY")
    val coronaAttributesCity: Observable<Result>

    companion object {
        const val GET_LIST_IN_CHINA = "0MSEUqKaxRlEPj5g/arcgis/rest/services/ncov_cases/FeatureServer/1/query?f=json&where=Recovered%3C%3E0&returnGeometry=false&spatialRel=esriSpatialRelIntersects&outFields=*&orderByFields=Recovered%20desc%2CCountry_Region%20asc%2CProvince_State%20asc&resultOffset=0&resultRecordCount=250&cacheHint=true"
        const val GET_LIST_COUNTRY = "0MSEUqKaxRlEPj5g/arcgis/rest/services/ncov_cases/FeatureServer/2/query?f=json&where=1%3D1&returnGeometry=false&spatialRel=esriSpatialRelIntersects&outFields=*&orderByFields=Confirmed%20desc&resultOffset=0&resultRecordCount=250&cacheHint=true"
        const val GET_LIST_CORONA_CITY = "0MSEUqKaxRlEPj5g/arcgis/rest/services/ncov_cases/FeatureServer/1/query?f=json&where=1%3D1&returnGeometry=false&spatialRel=esriSpatialRelIntersects&outFields=*&orderByFields=Confirmed%20desc%2CCountry_Region%20asc%2CProvince_State%20asc&resultOffset=0&resultRecordCount=250&cacheHint=true"
    }
}