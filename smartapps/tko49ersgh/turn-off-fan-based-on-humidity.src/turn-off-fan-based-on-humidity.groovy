/**
 *  Turn off light
 *
 *  Copyright 2017 Tim Okazaki
 *    Modified 11/2017 tko
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
    name: "Turn off fan based on humidity",
    namespace: "tko49ersGH",
    author: "Tim Okazaki",
    description: "Turns off fan when humidity stabilizes",
    category: "My Apps",
    iconUrl: "https://s3-us-west-2.amazonaws.com/okazakiweb/SmartThings/MotionSensor_256.png",
    iconX2Url: "https://s3-us-west-2.amazonaws.com/okazakiweb/SmartThings/MotionSensor_512.png")


preferences {
    section("Icon...") {
        icon(title: "Icon")
    }
    section("Motion sensor...") {
        input "motionSensor", "capability.motionSensor"
    }
    section("Humidity sensor...") {
	    input "humiditySensor", "capability.relativeHumidityMeasurement", title: "Humidity Sensor:", required: true
    }
    section("Fan...") {
	    input "fanSwitch", "capability.switch", title: "Fan Location:", required: true
    }
    section("Number of minutes to wait where humidity hasn't changed before turning off fan...") {
	    input "minutesToWait", "number", title: "Minutes", required: true, defaultValue: 15
    }
}

def installed() {
    initialize()
}

def updated() {
    unsubscribe()
    unschedule()
    initialize()
}

def initialize() {
    log.debug "Initialize: ${settings}"

    subscribe(fanSwitch, "switch.on", fanOnHandler)
    subscribe(fanSwitch, "switch.off", fanOffHandler)
}

def fanOnHandler(evt) {
    log.debug "Fan turned on"

    subscribe(motionSensor, "motion", motionHandler)
    subscribe(humiditySensor, "humidity", humidityHandler)
    state.lastHumidityValue = humiditySensor.currentHumidity
    scheduleFanOff()
}

def scheduleFanOff() {
    log.debug "Wait $minutesToWait minutes for humidity to stabilize. Current: $humiditySensor.currentHumidity"

    runIn(minutesToWait * 60, turnOffFan)
}

def motionHandler(evt) {
	log.debug "Motion $motionSensor.currentMotion"
    scheduleFanOff();
}

def humidityHandler(evt) {
	if (state.lastHumidityValue != humiditySensor.currentHumidity) {
	    log.debug "Humidity changed: $humiditySensor.currentHumidity"
        state.lastHumidityValue = humiditySensor.currentHumidity
        scheduleFanOff();
    }
}

def fanOffHandler(evt) {
	log.debug "Fan has been turned off"

    unsubscribe(humiditySensor)
    unsubscribe(motionHandler)
    unschedule()
}

def turnOffFan(evt) {
    log.debug "Humidity hasn't changed in $minutesToWait minutes. Turn off fan"

    fanSwitch.off();
}