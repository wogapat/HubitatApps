/*
 *  WogaLWRF - LW400
 *
 *  Copyright 2021 Patrick Wogan
 *  
 *  Adapted From Virtual Contact Sensor with Switch by Stephan Hackett
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
	definition (name: "WogaLWRF - LW400", namespace: "wogapat", author: "Patrick Wogan") {
		capability "Sensor"
        capability "Contact Sensor"
        capability "Switch"
		capability "Switch Level"
        
        command "ident"
        command "childLock"
        command "childUnlock"

		attribute "identify", "String"
		attribute "dimLevel", "number"
        attribute "childProtection", "String"
    }
}


def poll() {
    parent?.childPoll(device.deviceNetworkId)
}

def open(){
	sendEvent(name: "contact", value: "open")
	if(reversed) switchVal = "off"
	else switchVal = "on"
	sendEvent(name: "switch", value: switchVal)
}

def close(){
	sendEvent(name: "contact", value: "closed")
	if(reversed) switchVal = "on"
	else switchVal = "off"
	sendEvent(name: "switch", value: switchVal)
}

def on(){
    sendEvent(name: "switch", value: "on")
    if(reversed==true) {
        contactVal = "closed"
        parent?.setChildSwitch(device.deviceNetworkId, 0)
    } else {
        contactVal = "open"
        parent?.setChildSwitch(device.deviceNetworkId, 1)
    }
	sendEvent(name: "contact", value: contactVal)
}

def off(){
    sendEvent(name: "switch", value: "off")
    if(reversed==true) {
        contactVal = "open"
        parent?.setChildSwitch(device.deviceNetworkId, 1)
    } else {
        contactVal = "closed"
        parent?.setChildSwitch(device.deviceNetworkId, 0)
    }
	sendEvent(name: "contact", value: contactVal)
}

def setLevel(nextLevel, duration=null) {
	parent?.setChildSetLevel(device.deviceNetworkId, nextLevel)
}
	
def childLock() {
    sendEvent(name: "childProtection", value: "on")
	parent?.setChildProtection(device.deviceNetworkId, 1)
}

def childUnlock() {
    sendEvent(name: "childProtection", value: "off")
	parent?.setChildProtection(device.deviceNetworkId, 0)
}

def ident() {
	parent?.setChildIdentify(device.deviceNetworkId, 1)
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
	unschedule()
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
