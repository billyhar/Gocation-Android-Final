package com.gocation.gocation_android

import java.text.DateFormatSymbols

/**
 * Created by dylanlange on 11/05/17.
 */

fun String.toFirebaseKey(): String = this.replace('.', '_')
fun Int.asMonth(): String = if(this in 0..11) DateFormatSymbols().months[this] else ""

