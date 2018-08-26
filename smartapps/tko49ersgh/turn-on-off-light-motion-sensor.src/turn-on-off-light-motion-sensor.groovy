/**
 *  Turn off light
 *
 *  Copyright 2017 Tim Okazaki
 *    Modified 10/2017 tko
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
    name: "Turn on/off light/motion sensor",
    namespace: "tko49ersGH",
    author: "Tim Okazaki",
    description: "Turns on light with motion/off after motion stops + specified number of seconds",
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
    section("Light...") {
	input "light", "capability.switch"
    }
    section("Seconds after motion stops to turn off light...") {
	input "seconds", "number", title: "Seconds", required: true
    }
}

def installed() {
    initialize()
}

def updated() {
    unsubscribe()
    initialize()
}

def initialize() {
    log.debug "Initialize: ${settings}"

    subscribe(motionSensor, "motion.active", motionActiveHandler)
    subscribe(motionSensor, "motion.inactive", motionInactiveHandler)
}

def motionActiveHandler(evt) {
    unschedule();
    light.on();
}

def motionInactiveHandler(evt) {
    runIn(seconds, turnOffLight);
}

def turnOffLight(evt) {
    log.debug "light turned off after $seconds seconds"

    light.off();
}