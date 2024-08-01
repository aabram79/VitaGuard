package com.example.vitaguard.utils

import android.telephony.PhoneNumberUtils
import java.util.*

object PhoneNumberUtils {

    fun isValidPhoneNumber(phoneNumber: String): Boolean {
        // Normalize the phone number by removing spaces, hyphens, and parentheses
        val normalizedPhoneNumber = PhoneNumberUtils.normalizeNumber(phoneNumber)

        // Use PhoneNumberUtils to check if the normalized phone number is a possible number
        return PhoneNumberUtils.isGlobalPhoneNumber(normalizedPhoneNumber)
    }
}
