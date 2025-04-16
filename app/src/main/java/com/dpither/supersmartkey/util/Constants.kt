/*
    Copyright (C) 2025  Dylan Pither

    This file is part of Super Smart Key.

    Super Smart Key is free software: you can redistribute it and/or modify it under the terms of
    the GNU General Public License as published by the Free Software Foundation, either version 3
    of the License, or (at your option) any later version.

    Super Smart Key is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
    without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
    See the GNU General Public License for more details.

    You should have received a copy of the GNU General Public License along with Super Smart Key.
    If not, see <https://www.gnu.org/licenses/>.
 */

package com.dpither.supersmartkey.util

//Units: dBM
const val MIN_RSSI_THRESHOLD = -100
const val MAX_RSSI_THRESHOLD = -40
const val DEFAULT_RSSI_THRESHOLD = -60
const val MAX_RSSI = -130
//Units: Seconds
const val MIN_GRACE_PERIOD = 10
const val MAX_GRACE_PERIOD = 120
const val DEFAULT_GRACE_PERIOD = 30
//Units: Seconds
const val MIN_POLLING_RATE = 1
const val MAX_POLLING_RATE = 30
const val DEFAULT_POLLING_RATE = 1

const val DEFAULT_ANIMATION_DURATION = 500
const val BLE_HCI_CONNECTION_TIMEOUT = 8
const val FONT_SCALE_CAP = 1.5f