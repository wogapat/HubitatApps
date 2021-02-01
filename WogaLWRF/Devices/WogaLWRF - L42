/*
 *  WogaLWRF - L42
 *
 *  Copyright 2021 Patrick Wogan
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
metadata {
	definition (name: "WogaLWRF - L42", namespace: "wogapat", author: "Patrick Wogan") {
		capability "Switch"
        capability "Lock"
		capability "PowerMeter"
		capability "EnergyMeter"
		capability "ColorMode"
		capability "VoltageMeasurement"
        
        command "identifyOn"
        command "identifyOff"

		attribute "identify", "String"
		attribute "outletInUse", "bool"
    }
}

def poll() {
    parent?.childPoll(device.deviceNetworkId)
}

def on() {
	parent?.setChildSwitch(device.deviceNetworkId, 1)
}
	
def off() {
	parent?.setChildSwitch(device.deviceNetworkId, 0)
}
	
def lock() {
	parent?.setChildProtection(device.deviceNetworkId, 1)
}

def unlock() {
	parent?.setChildProtection(device.deviceNetworkId, 0)
}

def identifyOn() {
	parent?.setChildIdentify(device.deviceNetworkId, 1)
	runIn(180, disableIdentify)
}

def identifyOff() {
	parent?.setChildIdentify(device.deviceNetworkId, 0)
}

def updated() {
	log.debug "updated()"
	initialize()
}

def uninstalled() {
	log.debug "uninstalled()"
	unschedule()
}

def initialize() {
    log.debug "initialize()"
	unschedule()
		}

private def disableIdentify() {
	parent?.setChildIdentify(device.deviceNetworkId, 0)
    }

private def myRunIn(delay_s, func) {
    //LOGDEBUG("myRunIn(${delay_s},${func})")

	if (delay_s > 0) {
    def tms = now() + (delay_s * 1000)
    def date = new Date(tms)
    runOnce(date, func)
    //LOGDEBUG("'${func}' scheduled to run at ${date}")
  }
}
