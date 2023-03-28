package com.jeckonly.api.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Address(
    val detail: String,
    val neighborName: List<String>
): Parcelable
