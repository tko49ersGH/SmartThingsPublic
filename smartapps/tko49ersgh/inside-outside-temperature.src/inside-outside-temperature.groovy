/**
 *  Close/Open the Windows
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
    name: "Inside/Outside Temperature",
    namespace: "tko49ersGH",
    author: "Tim Okazaki",
    description: "Sends a notification when the outside temperature is lower/higher than the inside temperature.",
    category: "My Apps",
    iconUrl: "https://s3-us-west-2.amazonaws.com/okazakiweb/SmartThings/Thermometer_256.png",
    iconX2Url: "https://s3-us-west-2.amazonaws.com/okazakiweb/SmartThings/Thermometer_512.png")


preferences {
	section("Icon...") {
        icon(title: "Icon")
    }
	section("Outdoor sensor...") {
		input "temperatureSensorOut", "capability.temperatureMeasurement"
	}
    section("Indoor sensor...") {
		input "temperatureSensorIn", "capability.temperatureMeasurement"
	}
    section("Switch that determines whether to notify...") {
		input "notifySwitch", "capability.switch"
	}
    section("Open/Close what?") {
		input "sensor", "capability.contactSensor", required: true
	}
    section("Temperature difference...") {
		input "tempDiff", "decimal", title: "Degrees", required: true
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

	subscribe(notifySwitch, "switch.on", switchOnHandler)
    subscribe(notifySwitch, "switch.off", switchOffHandler)

	if (notifySwitch.currentSwitch == "on") {
    	enableTemperatureEvents()
    }
}

def enableTemperatureEvents() {
	log.debug "Enabling temperature/window events..."
    log.debug "sensor: $sensor.currentContact"
    log.debug "switch: $notifySwitch.currentSwitch"

	state.lastNotifyState = sensor.currentContact
	checkTemp(temperatureSensorIn.currentTemperature, temperatureSensorOut.currentTemperature)

	subscribe(temperatureSensorIn, "temperature", temperatureInHandler)
	subscribe(temperatureSensorOut, "temperature", temperatureOutHandler)
	subscribe(location, "mode", modeChangeHandler)
}

def disableTemperatureEvents() {
	log.debug "Disabling temperature events..."
	unsubscribe()
    initialize()
}

def switchOnHandler(evt) {
	enableTemperatureEvents()
}

def switchOffHandler(evt) {
	disableTemperatureEvents()
}

def temperatureInHandler(evt) {
    checkTemp(evt.doubleValue, temperatureSensorOut.currentTemperature)
}

def temperatureOutHandler(evt) {
    checkTemp(temperatureSensorIn.currentTemperature, evt.doubleValue)
}

def modeChangeHandler(evt) {
	log.debug "Mode changed to $evt.value"
    
    state.lastNotifyState = sensor.currentContact

	checkTemp(temperatureSensorIn.currentTemperature, temperatureSensorOut.currentTemperature)
}

private checkTemp(inTemp, outTemp) {
	log.debug "in: $inTemp/out: $outTemp/Notify state: $state.lastNotifyState/window: $sensor.currentContact"
    if (inTemp >= outTemp + tempDiff) {
        if (state.lastNotifyState != "open") {
        	if (sensor.currentContact != "open") {
            	send("It's cooling off outside, open the windows! (in: $inTemp/out: $outTemp)")
            	log.debug "open"
            }
        	state.lastNotifyState ="open"
        }
    } else if (inTemp <= outTemp - tempDiff) {
    	if (state.lastNotifyState != "closed") {
        	if (sensor.currentContact != "closed") {
            	send("It's warming up outside, close the windows! (in: $inTemp/out: $outTemp)")
            	log.debug "closed"
            }
       		state.lastNotifyState ="closed"
    	}
  	}
}

private send(msg) {
	log.debug msg
	sendPush( msg )
}