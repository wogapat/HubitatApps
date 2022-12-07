/*
 *  WogaLWRF - Lightwave Automation Device
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
	definition (name: "WogaLWRF - Lightwave Automation Device", namespace: "wogapat", author: "Patrick Wogan") {
		capability "Switch"
        
    }
}

def on() {
	parent?.setAutoDeviceOn(device.deviceNetworkId)
    sendEvent(name: "switch", value: "on")
    myRunIn(2, "off")
	}
	
def off() {
    sendEvent(name: "switch", value: "off")
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

def parse(String description) {
    description.each {
        if (it.name in ["switch","level"]) {
            if (txtEnable) log.info it.descriptionText
            sendEvent(it)
        }
    }
}

private def myRunIn(delay_s, func) {
    if (delay_s > 0) {
        def tms = now() + (delay_s * 1000)
        def date = new Date(tms)
        runOnce(date, func)
    }
}
