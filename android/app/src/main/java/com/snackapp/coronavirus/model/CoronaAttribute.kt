package com.snackapp.coronavirus.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable


//{"attributes":{"OBJECTID":43,"Province_State":"Hubei",
// "Country_Region":"Mainland China",
// "Last_Update":1580599982000,
// "Lat":30.9756403482891,
// "Long_":112.270692167452,
// "Confirmed":9074,
// "Deaths":294,
// "Recovered":215}}

data class CoronaAttribute(
    val OBJECTID: Int,

    val Province_State: String?,

    var Country_Region: String?,

    @SerializedName("Lat")
    val latitude: Double,

    @SerializedName("Long_")
    val longitude: Double,

    var Confirmed: Int,

    @SerializedName("Deaths")
    val rips: Int,

    val Recovered: Int
) : Serializable