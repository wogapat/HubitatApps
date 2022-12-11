/*
 *  WogaLWRF - L42
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
	definition (name: "WogaLWRF - L42", namespace: "wogapat", author: "Patrick Wogan") {
		capability "Sensor"
        capability "Contact Sensor"
        capability "Switch"
		capability "PowerMeter"
		capability "EnergyMeter"
		capability "ColorMode"
		capability "VoltageMeasurement"
        
        	
		command "open"
		command "close"
        command "ident"
        command "childLock"
        command "childUnlock"

		attribute "identify", "String"
		attribute "outletInUse", "bool"
        attribute "childProtection", "String"

    }
	preferences {
        input name: "reversed", type: "bool", title: "Reverse Action"
	}
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


def childLock() {
	parent?.setChildProtection(device.deviceNetworkId, 1)
}

def childUnlock() {
	parent?.setChildProtection(device.deviceNetworkId, 0)
}

def ident() {
    sendEvent(name: "identify", value: "on")
	parent?.setChildIdentify(device.deviceNetworkId, 1)
	myRunIn(90, "disableIdentify")
}

private def disableIdentify() {
    sendEvent(name: "identify", value: "off")
	parent?.setChildIdentify(device.deviceNetworkId, 0)
}

def poll() {
    parent?.childPoll(device.deviceNetworkId)
}

private def myRunIn(delay_s, func) {
    log.debug("myRunIn(${delay_s},${func})")

	if (delay_s > 0) {
        def tms = now() + (delay_s * 1000)
        def date = new Date(tms)
        runOnce(date, func)
        //LOGDEBUG("'${func}' scheduled to run at ${date}")
    }
}


def installed(){
	initialize()
}

def updated(){
	initialize()
}

def initialize(){
	sendEvent(name: "switch", value: "off")
	sendEvent(name: "contact", value: "closed")
}


