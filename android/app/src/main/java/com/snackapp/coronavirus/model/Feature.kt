package com.snackapp.coronavirus.model

import java.io.Serializable


class Feature (
    val attributes : CoronaAttribute,
    var isSelected: Boolean
) : Serializable