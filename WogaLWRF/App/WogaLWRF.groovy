   /*
 *  WogaLWRF - Lightwave Integration
 *
 *  Copyright 2021 Patrick Wogan
 *  
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
 

 //********** !!!!! IMPORTANT - If your current version is less than 1.2.7 uninstall & re-install is recommended  
   
    import java.text.DecimalFormat
    import groovy.json.JsonSlurper
    import groovy.json.JsonOutput

    public static String version()      {  return "1.2.17"  }
	def getThisCopyright(){"&copy; 2020 P Wogan"}

    def displayVersionStatus(){
        state.versionStatus = "Current"
        section{paragraph "<BR>${state.ExternalName} - Version: $state.version <BR><font face='Lucida Handwriting'></font>"}
    }

    def setVersion(){
            //Cobra update code, modified by Rayzurbock
            state.version = version()
            state.InternalName = "wogalwrf"
            state.ExternalName = "WogaLWRF"
    }

    definition (
        name: "WogaLWRF Lightwave Integration",
        namespace: "wogapat",
        author: "Patrick Wogan",
        description: "Lightwave Link Plus Smart Series Integration",
        category: "Home Automation",
        importUrl: "https://github.com/wogapat/HubitatApps/main/WogaLWRF/App/WogaLWRF.groovy",
        documentationLink: "",
        iconUrl: "",
        iconX2Url: "",
        oauth: true,
        singleInstance: true
    )
    
    {
        appSetting "apiBasicToken"
        appSetting "apiRefreshToken"
        appSetting "debugmode"
        appSetting "installWebhook"
    }

   preferences {
        page(name: "mainPage")
        page(name: "pageIntegrationSettings")
        page(name: "pagePostAppState")
        page(name: "pagePostInstallConfigure")
        page(name: "pagePostTestApi")
        page(name: "pagePostAppPrefs")
        page(name: "pagePostManageWebhooks")
        page(name: "pageViewWebhooks")
        page(name: "pageAddAdditionalEndpoint")
        page(name: "pageCreateExternalEvent")
        page(name: "pageDeleteAdditionalEndpoint")
        page(name: "pagePostAppDebug")		
        page(name: "startUpTest")
        page(name: "pageCreateDevices")
        page(name: "pageCreateNewDevices")
        page(name: "pageDiscoverAstructure")
        page(name: "pageDiscoveryStructures")
        page(name: "pageAutoDeviceAdmin")
        page(name: "pageEnterAutomationDevice")
        page(name: "pageCreateAutomationDevice")
        page(name: "pageAutomationDeviceCreated")
        page(name: "pageAmendAutomationDevice")
		page(name: "pageDeleteAutomationDevice")
    }

    private getApiUrl()           { "https://publicapi.lightwaverf.com" }
    private getVendorName()            { "lightwaveRF" }
    private getVendorAuthPath()      { "https://auth.lightwaverf.com/token" }
    private getDiscoveryStructuresPath() { getApiUrl()+"/v1/structures/" }
    private getDiscoveryStructurePath() { getApiUrl()+"/v1/structure/" }
    private getFeatureReadPath() { getApiUrl()+"/v1/feature/"}
    private getFeatureBatchReadPath() { getApiUrl()+"/v1/features/read"}
    private getFeatureWritePath() { getApiUrl()+"/v1/feature/"}
    private getFeatureBatchWritePath() {getApiUrl()+"/v1/features/write"}
    private apiEventsPath() { getApiUrl()+"/v1/events/"}
    private apiEventIdPath() { getApiUrl()+"/v1/events/"}
    private apiCreateEventsPath() { getApiUrl()+"/v1/events"}
    private getWebhookUrl()		{ getServerUrl()+ "/webhook?access_token="+URLEncoder.encode("${state.accessToken}", "UTF-8") }
    private getServerUrl() 		{ return getFullApiServerUrl() }
    
    mappings {
        path("/webhook") {action: [GET: "webhook", POST: "webhook"]}
    }

    def getFormat(type, myText=""){            // Modified from @Stephack Code   
	    if(type == "line") return "<hr style='background-color:#1A77C9; height: 1px; border: 0;'>"
	    if(type == "title") return "<h2 style='color:#1A77C9;font-weight: bold'>${myText}</h2>"
    }


    def displayHeader() {
	    section (getFormat("title", "${state.ExternalName} - Lightwave Integration")) {
		    paragraph "<div style='color:#1A77C9;font-weight:small;font-size:11px;'>Developed by: Patrick Wogan<br/>Current Version: ${version()}</div>"
            if (app.getInstallationState() != "COMPLETE") paragraph "<div style=';font-weight:medium;font-size:18px;'>Installation</div>"
            if (app.getInstallationState() == "COMPLETE") paragraph "<div style=';font-weight:medium;font-size:18px;'>Home Page</div>"
		    paragraph "\n<hr style='background-color:#1A77C9; height: 1px; border: 0;'></hr>"
	    }
    }

    def displayMiniHeader(titleText) {
	    section (getFormat("title", "${state.ExternalName} - Lightwave Integration")) {
            paragraph "<div style=';font-weight:medium;font-size:18px;'>${titleText}</div>"
            paragraph (getFormat("line"))
	    }
    }

    def displayAbout(){
	    section() {
		    paragraph getFormat("line")
            def AboutApp = ""
            AboutApp += "Integrate Hubitat directly with Lightwave Link using ${state.ExternalName} and the Link Plus Smart Series API\n"
            AboutApp += "You will need an active Lightwave Link Plus Account, an API Bearer Token and a Refresh Token.\n\n" 
            AboutApp += "Go to the ‘settings’ page on either the Link Plus app or the web client <a target='_blank' href='https://my.lightwaverf.com'>https://my.lightwaverf.com</a>.\nIn your Lightwave Link Plus Account section, look for ‘API Integration’ and ‘Get Token'.\nAfter enterering your Link Plus account password you will be presented with the bearer and refresh tokens.\nCopy the bearer and refresh tokens to ${state.ExternalName} settings.\n\n"
		    AboutApp += "IMPORTANT\nRefresh token only useable once.\n ${state.ExternalName} maintains a session with Lighwave Link and updates refresh token automatically.\n In case of error you can create a new refresh token directly in the your Link account and update it in the  ${state.ExternalName} settings.\n"
            paragraph "<div style='text-align:left;font-weight:small;font-size:16px;'>${AboutApp}</div>"
        }
    }       

    def displayFooter(){
	    section() {
		    paragraph getFormat("line")
		    paragraph "<div style='color:#1A77C9;text-align:center;font-weight:small;font-size:11px;'>${state.ExternalName} - Lightwave Integration<br><a href='https://www.paypal.com/donate/?business=N8M3ZEA8CMEY6&no_recurring=0&currency_code=GBP' target='_blank'><img src='https://www.paypalobjects.com/webstatic/mktg/logo/pp_cc_mark_37x23.jpg' border='0' alt='PayPal Logo'></a><br><br>Please consider donating.</div>"
	    }       
    }

    def displayHomeButton() {
        section() {
            paragraph (getFormat("line"))
			input "btnHomePage", "button", title: "Home Page", width: 3

		}
    }

    def startUpTest() {
        LOGDEBUG("startUpTest()")
        getAccessToken()

        if (!state.installed == true && state.session == true) {
            dynPageProperties = [
                name:       "startUpTest",
                title:      "",
                nextPage:   "pageDiscoveryStructures",
                install:    false,
                uninstall:  false
            ]

        } else if (!state.session == true) {
            dynPageProperties = [
                name:       "startUpTest",
                title:      "",
                nextPage:   "pageIntegrationSettings",
                install:    false,
                uninstall:  false
            ]

        } else {
            dynPageProperties = [
                name:       "startUpTest",
                title:      "",
                install:    true,
                uninstall:  false
            ]
            StartupTest += "API tested successfully. Session established.\n\n"
        }
        return dynamicPage(dynPageProperties) {
            displayMiniHeader("API Testing")
            section() {
                def StartupTest = ""
                StartupTest += "Your API settings have been tested. The result is shown below.\n\n"
                StartupTest += "<B>Test Result.</B>\n\n"
                
                if (state.session == true) {
                    StartupTest += "API tested successfully. Session established.\n\n"
                    StartupTest += "If you are installing ${state.ExternalName} for the first time, you will be taken to the device discovery pages.\n\n"
                } else {
                    StartupTest += "A session could not be established. Error: ${state.apiLastError}\n\n"
                    StartupTest += "You'll be taken back to the integration settings."
                }
                paragraph(StartupTest)
            }
        }
    }

    def autoDeviceCreate() {
        LOGDEBUG("autoDeviceCreate()")
        if (state?.apiBatchFeatureWrite == null) state.apiBatchFeatureWrite = [:]
        LOGDEBUG"${settings.automationName}"
        state.apiBatchFeatureWrite["${settings.automationName}"] = [:]
        state.apiBatchFeatureWrite["${settings.automationName}"].features = []
        state["automation_${settings.automationName}"] = [:]
        state['automation_'+"${settings.automationName}"].devices = []
        state['automation_'+"${settings.automationName}"].features = []

        state.finalDeviceList.each { deviceId, name ->
            if (settings["${deviceId}"] == true) {
                if (state.deviceDetail["${deviceId}"].features.switch) {
                    state['automation_'+"${settings.automationName}"].devices.push(name)
                    
                }
            }
        }

        if (!state.configGroupDevices) state.configGroupDevices = []
        if (!state.amendAutoDevice) state.configGroupDevices.push(settings.automationName)

        state.availGroupFeatures.each { value ->
            settingValue = settings["${value}"]

            switch(settingValue) {
                case "9":
                    finValue = "do not include"
                    break
                default:
                    finValue = settingValue.toInteger()
            }

            if (finValue != "do not include") {
                //state["automation_${settings.automationName}"].features.push('{"featureId": "'+value+'", "value": '+finValue+'}')
                state.apiBatchFeatureWrite["${settings.automationName}"].features.push('{"featureId": "'+value+'", "value": '+finValue+'}')
                state["automation_${settings.automationName}"].features.push(value)
            }
        }
        if (!state.amendAutoDevice && settings.automationName) createAutomationDevice("${settings.automationName}")
    }


    def createExternalEvent() {
        result = false
        state["endpoint_${settings.endpointId}"] = [:]
        state['endpoint_'+"${settings.endpointId}"].devices = []
        state['endpoint_'+"${settings.endpointId}"].features = []
        

        state.finalDeviceList.each { deviceId, name ->
            if (settings["${deviceId}"] == true) {
                if (state.deviceDetail["${deviceId}"].features.switch) {
                    state['endpoint_'+"${settings.endpointId}"].devices.push(name)
                    state["endpoint_${settings.endpointId}"].features.push(state.deviceDetail["${deviceId}"].features.switch.featureId)
                    
                }
            }
        }
        state['endpoint_'+"${settings.endpointId}"].url = settings.endpointUrl
        if (!state.configExternalEvents) state.configExternalEvents = []

        if (!state.amendEndpoint) {
            state.configExternalEvents.push(settings.endpointId)

            if (addWebhookExternal(settings.endpointId) == true) {
                result = true
            } else {
                state.configExternalEvents.remove(settings.endpointId)
                endpointId = 'endpoint_'+"${settings.endpointId}"
                state.remove(endpointId)
            }
        }
        return result
    }



    def featureCheck() {
        LOGDEBUG("featureCheck()")
        featureCheck = false

        state.finalDeviceList.each { deviceId, name ->
            switch (settings["${deviceId}"]) {
                case true:
                    featureCheck = true
                    break
                
                default:
                    break 
            }
        }
        if (featureCheck == true) return true
        if (featureCheck != true) return false
    }

    def mainPage(){
        LOGDEBUG("mainPage()")
        state.namespace = "wogapat"
        setVersion()
        state.hubType = getHubType()
        LOGDEBUG("Hub Type: ${state.hubType}")

        if (checkConfig()) { 
            // Do nothing here, but run checkConfig() 
        }

        return dynamicPage(name: "mainPage", title: "", nextPage: "", install: (app.getInstallationState() == "COMPLETE"), uninstall: (app.getInstallationState() != "COMPLETE")) {
            displayHeader()

            section(){
                LOGDEBUG("install state=${app.getInstallationState()}.")

                if (!state.configOk == true) { 
                    href "pageIntegrationSettings", title:"Install ${state.ExternalName}", description:"Tap to install ${state.ExternalName}"

                } else {
                    settingsRemove()
                    state.remove("homePage")
                    href "pagePostAppState", title:"Status", description:"Tap to view the application status"
                    href "pagePostInstallConfigure", title: "App Configuration, Preferences & Testing", description: "Tap to configure app settings, preferences & debugging"
                    href "pageAutoDeviceAdmin", title: "Automation Devices", description: "Add, change or delete devices for routines"
                    href "pageCreateNewDevices", title:"Discover a new device", description:"Tap to discover new Lightwave devices"
                }
            }
            if (app.getInstallationState() != "COMPLETE") displayAbout()
            displayFooter()
        }
    }

    def pageIntegrationSettings() {
        LOGDEBUG("pageIntegrationSettings()")

        return dynamicPage(name: "pageIntegrationSettings", title: "", nextPage: "startUpTest", install: false, uninstall: false){
            displayMiniHeader("Configure settings")

            section("API Client Token"){
                input "apiBasicToken", "string", title: "Enter the API basic token", multiple: false, required: true, submitOnChange: true
            }

            section ("Refresh Token"){
                input "apiRefreshToken", "string", title: "Enter the API refresh token:", multiple: false, required: true, submitOnChange: true
            }

            section(){
                input "debugmode", "bool", title: "Enable debug logging", required: true, defaultValue: false, submitOnChange: true
                    if (debugmode) { 
                        if (state.debugMode == false || state?.debugMode == null){
                            state.debugMode = true; myRunIn(1800, disableDebug); LOGTRACE("Debug logging has been enabled.  Will auto-disable in 30 minutes.")
                        }

                    } else {
                        state.debugMode = false; unschedule("disableDebug");  LOGTRACE("Debug logging is not enabled.")
                    }
            }
            state.apiBasicToken = apiBasicToken
            state.apiRefreshToken = apiRefreshToken
        }
    }

    def pagePostAppState() {
        LOGDEBUG("pagePostAppState()")

        return dynamicPage(name: "pagePostAppState", title: "", nextPage: "mainPage", install: false, uninstall: false){
            displayMiniHeader("Application state")
            section() {
                def AboutState = ""
                AboutState += "<B>Lightwave session</B>\n\n"
                AboutState += "Refresh Token: ${state.apiRefreshToken}\n"
                AboutState += "API Access Token: ${state.apiAccessToken}\n"
                AboutState += "Active Session: ${state.session}\n\n"
                def AboutDetails = ""
                AboutDetails += "<B>Device details</B>\n\n"
                state.finalDeviceList.each { dev ->
                    devId = dev.getKey()
                    devName = dev.getValue()
                    AboutDetails += "<B>${devName}</B>\n"
                    state.deviceDetail[devId].each {
                        AboutDetails += "${it}\n"
                    }
                    AboutDetails += "\n"
                }
                def AboutGroup = ""
                AboutGroup += "<B>Group device details</B>\n\n"
                state.configGroupDevices.each { gDev ->
                    AboutGroup += "${gDev}\n"
                }
                paragraph(AboutState)
                paragraph(getFormat("line"))
                paragraph(AboutDetails)
                paragraph(getFormat("line"))
                paragraph(AboutGroup)
            }
            displayFooter()
        }
    }

    def pagePostInstallConfigure() {
        LOGDEBUG("pagePostInstallConfigure()")

        return dynamicPage(name: "pagePostInstallConfigure", title: "", nextPage: "mainPage", install: false, uninstall: false) {
            settingsRemove()
            displayMiniHeader("Configuration, Preferences & Testing")

            section(){
                href "pagePostTestApi", title:"API Configuration & Testing", description:"Tap to configure & test the Lightwave API connection settings"
                if (state.session == true) href "pagePostAppPrefs", title: "App Preferences", description: "Tap to configure preferences"
                href "pagePostAppDebug", title: "Debug & Feature Read", description: "Tap for debugging"
                href "pageCreateNewDevices", title:"Discover a new device", description:"Tap to discover new Lightwave devices"
            }
        }
    }

    def pagePostTestApi() {
        LOGDEBUG("pagePostTestApi()")
        getAccessToken()

        return dynamicPage(name: "pagePostTestApi", nextPage: "pagePostInstallConfigure", install: false, uninstall: false){
            displayMiniHeader("API Configuration & Testing")
            section(){
                paragraph "<B>Basic Token:</B>"
                input "apiBasicToken", "string", title: "Enter the API basic token", multiple: false, required: true, submitOnChange: true
            }

            section (){
                paragraph "<B>Refresh Token:</B>\n"
                input "apiRefreshToken", "string", title: "Enter the API refresh token:", multiple: false, required: true, submitOnChange: true
                input "apiTestButton", "button", title: "Test API", submitOnChange: false
            }

            section () {
                paragraph getFormat("line")
                paragraph "<B>API Status:</B>\n\n"
                def APIState = ""
                
                if (state.session == true) {
                    APIState += "Lightwave Linkplus connection established."

                } else {
                    APIState += "Lightwave Linkplus connection failed.\n"
                    APIState += "Error: ${state.apiLastError}"
                }
                paragraph(APIState)
            }
            displayFooter()
        } 
    }


    def pagePostAppPrefs() {
        LOGDEBUG("pagePostAppPrefs()")
        getEvents()

        if (installWebhook == true && state.webhookInstalled != true) {
            children = getChildDevices()

            LOGDEBUG("children: ${children}")
            children.each {addWebhook(it.deviceNetworkId, it.label)}
        }
        if (installWebhook != true && state.webhookInstalled == true) {
            deleteEvents(getEvents())
        }

        return dynamicPage(name: "pagePostAppPrefs", title: "", nextPage: "pagePostInstallConfigure", install: false, uninstall: false){
            displayMiniHeader("Application Preferences")
            section() {
                paragraph "<B>Preferences</B>\n\n"
        	    input "installWebhook", "bool", title: "Activate webhooks", description: "", submitOnChange: true, defaultValue: false
            }
            if (state?.webhookInstalled != null && state.webhookInstalled == true) {
                section() {
                    paragraph getFormat("line")
                    href "pagePostManageWebhooks", title: "Manage Webhook Events", description: "Tap to manage registered webhook events"
                }
            }
        }
    }


    def pagePostManageWebhooks() {
        settingsRemove()
        //state.remove("endpoint_testEvent")
        LOGDEBUG("pagePostManageWebhooks()")

        return dynamicPage(name: "pagePostManageWebhooks", title: "", nextPage: "pagePostAppPrefs", install: false, uninstall: false){
            displayMiniHeader("Manage Webooks")
            section () {
                href "pageViewWebhooks", title: "View Webhooks", description: "Tap to view registered webhook events"
                paragraph getFormat("line")
                href "pageAddAdditionalEndpoint", title: "Add Additional Webhook Endpoint", description: "Tap to add an external webhook endpoint event"
                if (state?.configExternalEvents != null && state.configExternalEvents) {
                    href "pageDeleteAdditionalEndpoint", title: "Delete Additional Webhook Endpoint", description: "Tap to remove an external webhook endpoint event"
                }
                //input "eventId", "string", title: "Enter the event ID:", multiple: false, required: false, submitOnChange: true
                //input "deleteEvents", "button", title: "Delete Events", submitOnChange: true
                //input "createEvents", "button", title: "Create Events", submitOnChange: true
                paragraph getFormat("line")
            }
        }
    }


    def pageViewWebhooks() {
        LOGDEBUG("pageViewWebhooks()")
        state.addExEventClear = true
        getEvents()
        internalEvent = false

        return dynamicPage(name: "pageViewWebhooks", title: "", nextPage: "pagePostManageWebhooks", install: false, uninstall: false){
            displayMiniHeader("Registered Webhook Events")
            def EventText = ""
            def WebhookText = ""

            if (state?.webhookEventInfo != null) {
                section() {
                    WebhookText += "<B>Event Info, ID: ${state.webhookEventInfo.id}</B>\n"
                    WebhookText += "Created at: ${state.webhookEventInfo.createdAt}\n"
                    WebhookText += "Client ID: ${state.webhookEventInfo.clientId}\n"
                    WebhookText += "URL: ${state.webhookEventInfo.meta.url}\n"
                    WebhookText += "<b>Events</b>\n"
                    state.webhookEventInfo.meta.events.each { eventInfo ->
                        WebhookText += "${eventInfo}\n"
                    }
                    WebhookText += "Type: ${state.webhookEventInfo.type}\n"
                    WebhookText += "Active: ${state.webhookEventInfo.active}\n"
                    WebhookText += "Version: ${state.webhookEventInfo.version}\n"
                    WebhookText += "User ID: ${state.webhookEventInfo.userId}\n"
                    WebhookText += "Updated at: ${state.webhookEventInfo.updatedAt}\n"
                    paragraph (WebhookText)
                    input "clearWebhookEventInfo", "button", title: "Clear Information", submitOnChange: true
                    paragraph getFormat("line")
                }
            }
            section() {
                input "getEvents", "button", title: "Get Registered Webhook Events", submitOnChange: true
                paragraph "<B>Registered Webhooks Events</B>\n\n"
                state.webhookEvents.each { webhookEvent ->
                    state.finalDeviceList.any { deviceId, deviceName ->
                        if (webhookEvent.id == deviceId[-5..-1]) {
                            EventText = "WogaLWRF Device Event: <b>${deviceName}</b>\nEvent ID: ${webhookEvent.id}\n"
                            internalEvent = true
                            return true
                    
                        } else {
                            EventText = "External Event ID: <b>${webhookEvent.id}</b>\n"
                            internalEvent = false
                            return
                        }
                    }

                    //EventText += ("${webhookEvent}\n")
                    LOGDEBUG("${webhookEvent.id}")
                    //eventDetail = getEventId(webhookEvent.id)
                    //EventText += ("${eventDetail}\n\n")
                    paragraph (EventText)
                    input "getEventsId@${webhookEvent.id}", "button", title: "Get Id: <b>${webhookEvent.id}</b> Information", submitOnChange: true
                }
            }
        }
    }


    def pageAddAdditionalEndpoint() {
        LOGDEBUG("pageAddAdditionalEndpoint()")
        state.addExEventClear = true

        LOGDEBUG("${featureCheck()}")
        existingEndpoint = state["endpoint_${endpointId}"]

        if (!settings.endpointId || !settings.endpointUrl || featureCheck() != true) {
            next = "pagePostManageWebhooks"
        } else  {
            next = "pageCreateExternalEvent"
        }

        dynPageProperties = [
            name:       "pageAddAdditionalEndpoint",
            title:      "",            
            nextPage:   next,
            install:    false,
            uninstall:  false
        ]
        if (!existingEndpoint || existingEndpoint && state.amendEndpoint) {
            return dynamicPage(dynPageProperties) {
                if (!state.amendEndpoint) displayMiniHeader("Register an External Webhook Endpoint Event")
                if (state.amendEndpoint) displayMiniHeader("Amending ${state.amendEndpoint}")
                section(){
                    paragraph "<b>Your External Event Details</b>\n"
                    if (!state.amendEndpoint) input "endpointId", "string", title: "Enter an ID for the event", multiple: false, required: (settings.endpointId), submitOnChange: true
                    if (state.amendEndpoint) paragraph "${state.amendEndpoint}"
                    input "endpointUrl", "string", title: "Enter the endpoint url", multiple: false, required: (settings.endpointId), submitOnChange: true
                }

                section (){
 
                    if (settings.endpointId) if (featureCheck() != true) paragraph "<div style='color:#FF0000;font-weight:bold;font-size:16px;'>Select a device to continue</div>"
                    paragraph "<b>Available Switches</b>\n\nOnly switch features are added to the external event."

                    state.finalDeviceList.each { deviceId, name ->

                            LOGDEBUG("deviceId: ${deviceId}")
                            LOGDEBUG("name: ${name}")

                            switchPresent = state.deviceDetail["${deviceId}"].features.switch
                            dimPresent = state.deviceDetail["${deviceId}"].features.dimLevel
                            
                            if (switchPresent)  {
                                if (!state.amendEndpoint) input "${deviceId}", "bool", title: "${name}", defaultValue: false, submitOnChange: true
                                if (state.amendEndpoint) input "${deviceId}", "bool", title: "${name}", defaultValue: (state["endpoint${state.amendEndpoint}"].events.contains(name)), submitOnChange: true
                            }
                        }
                }
            }
        } else {
            return dynamicPage(dynPageProperties) {
                displayMiniHeader("Change The Event Details")
                section() {
                    def ErrorMessage = ""
                    ErrorMessage += "<div style='color:#FF0000;font-weight:bold;font-size:16px;'>Error: Event ID exists - Delete or choose another ID</div>"
                    paragraph(ErrorMessage)
                }
                section(){
                    paragraph "<b>Your Event Details</b>\n"
                    input "endpointId", "string", title: "Change the endpoint ID", multiple: false, required: (settings.endpointId), submitOnChange: true
                }
            }
        }
    }

    def pageDeleteAdditionalEndpoint() {
        LOGDEBUG("pageDeleteAdditionalEndpoint()") 
        state.addExEventClear = true

        return dynamicPage(name: "pageDeleteAdditionalEndpoint", title: "", nextPage: "pagePostManageWebhooks", install: false, uninstall: false){
            displayMiniHeader("Delete External Webhook Endpoint Event")
            section() {
                if (state.endpointId && state.lastExtEvtRemove == true) {
                    def AboutState = ""
                    AboutState += "<div style='color:#FF0000;font-weight:bold;font-size:16px;'>${state.endpointId} deleted\n\n</div>"
                    paragraph(AboutState)
                } else if (state.endpointId && state.lastExtEvtRemove == false) {
                    def AboutState = ""
                    AboutState += "<div style='color:#FF0000;font-weight:bold;font-size:16px;'>${state.endpointId} deletion failed\n\n</div>"
                    paragraph(AboutState)
                }
                state.configExternalEvents.each { value ->
                    input "4D@${value}", "button", title: "Delete ${value}", submitOnChange: false
                }
            }
        }
    }


    def pageCreateExternalEvent() {
        LOGDEBUG("pageCreateExternalEvent()") 
        return dynamicPage(name: "pageCreateExternalEvent", nextPage: "pagePostManageWebhooks", title: "", install: false, uninstall: false){
            def EventCreate = ""

            if (createExternalEvent() == true) {
                resultText = "successfully"
            } else {
                resultText = "unsuccessfully"
            }

            if (!state.amendEndpoint) {
                displayMiniHeader("Creation result")
                EventCreate += "External event ID: ${settings.endpointId} ${resultText} created.\n\n"
            } else {
                displayMiniHeader("Amendment result")
                EventCreate += "External event ID: ${settings.endpointId} ${resultText} amended.\n\n"
            }
            section() {
                paragraph(EventCreate)
            }
            displayFooter()
        }
    }


    def pagePostAppDebug() {
        LOGDEBUG("pagePostAppDebug()")

        return dynamicPage(name: "pagePostAppDebug", title: "", nextPage: "pagePostInstallConfigure", install: false, uninstall: false){
            displayMiniHeader("Logging")

            section () {
                paragraph"<B>Debug</B>\n\n"
                input "debugmode", "bool", title: "Enable debug logging", defaultValue: false, submitOnChange: true
                input "apiBatchFeatureRead", "button", title: "Batch Feature Read", submitOnChange: false
            }
            displayFooter()

            if (debugmode) {
                if (state.debugMode == false || state?.debugMode == null){
                    state.debugMode = true; myRunIn(1800, disableDebug); LOGTRACE("Debug logging has been enabled.  Will auto-disable in 30 minutes.")
                }

            } else {
                state.debugMode = false; unschedule("disableDebug");  LOGTRACE("Debug logging is not enabled.")
            }
        }
    }

    def pageCreateDevices() {
        LOGDEBUG("pageCreateDevices()")
        createDevices()

        return dynamicPage(name: "pageCreateDevices", title: "", nextPage: "", install: true, uninstall: false){
            displayMiniHeader("Creating devices")
            section() {
                def CreateDevices = ""
                if (!state.configChildrenExist == true) {
                    CreateDevices += "Your devices could not be created. Check the application logs for information."

                } else if (state.configChildrenExist == true) {
                    CreateDevices += "Your devices were created succesfully. Device attributes are polled and populated during initialisation.\n\n I hope you enjoy ${state.ExternalName}.\n\n"
                    CreateDevices += "Please remember to make a donation if you would like to thank the developer.\n\n"
                }
                paragraph(CreateDevices)
            }
            displayFooter()                           
        }
    }

    def pageCreateNewDevices() {
        LOGDEBUG("pageCreateDevices()")
        getAstructure()
        createNewDevices()

        return dynamicPage(name: "pageCreateDevices", title: "", nextPage: "", install: true, uninstall: false){
            displayMiniHeader("Creating devices")
            section() {
                def CreateDevices = ""
                if (state.newDevices < 1) {
                    CreateDevices += "No new devices found."

                } else {
                    CreateDevices += "${state.newDevices} new devices were created succesfully. Device attributes are polled and populated during initialisation."
                }
                paragraph(CreateDevices)
            }
            displayFooter()                           
        }
    }

    def pageDiscoverAstructure() {
        LOGDEBUG("pageDiscoverAstructure()")
        getAstructure()

        if (state.configGetAstructure == true) {
        dynPageProperties = [
                name:       "pageDiscoverAstructure",
                title:      "",
                nextPage:   "pageCreateDevices",
                install:    false,
                uninstall:  false
            ]
        } else {
        dynPageProperties = [
                name:       "pageDiscoverAstructure",
                title:      "",
                install:    true,
                uninstall:  false
            ]
        }

        return dynamicPage(dynPageProperties) {
            displayMiniHeader("Discovering your devices")
            section() {
                def DiscoverAstructure = ""
                if (state.configGetAstructure == true) {
                DiscoverAstructure += "Lightwave devices were discovered. Your devices will be created in the Hubitat ecosystem.\n\n"
                } else {
                    DiscoveryAStructure += "There was an error when discovering your Lightwave devices:\n"
                    DiscoverAstructure += "${state.apiLastError}"
                }
                paragraph(DiscoverAstructure)
            }
            
            if (state.configGetAstructure == true) {
                section("Preferences") {
                    paragraph "Webhooks are needed to keep your devices in sync with Lightwave. You may choose not to active them.\n\n"
                    input "installWebhook", "bool", title: "Activate webhooks", description: "", required: true, defaultValue: true
                }
            }
        }
    }

    def pageDiscoveryStructures() {
        LOGDEBUG("pageDiscoveryStructures()")
        getStructure()

        dynPageProperties = [
            name:       "pageDiscoveryStructures",
            title:      "",
            nextPage:   "pageDiscoverAstructure",
            install:    false,
            uninstall:  false
        ]

        return dynamicPage(dynPageProperties) {
            displayMiniHeader("Discovering your structure")
            section() {
                def DiscoveryStructure = ""
                if (state.configGetStructure == true) {
                    DiscoveryStructure += "A Lightwave structure was found. Your devices will now be discovered.\n\n"
                    DiscoveryStructure += "Lightwave structure ID: ${state.structureId}"
                } else {
                    DiscoveryStructure += "There was an error when discovering your Lightwave ecosystem:\n\n"
                    DiscoveryStructure += "${state.apiLastError}"
                }
                paragraph(DiscoveryStructure)
            }
        }
    }

    def pageAutoDeviceAdmin() {
        LOGDEBUG("pageAutoDeviceAdmin()")
        settingsRemove()

        return dynamicPage(name: "pageAutoDeviceAdmin", title: "", nextPage: "mainPage", install: false, uninstall: false) {
            displayMiniHeader("Automation Device Maintenance")
            section(){
                LOGDEBUG("install state=${app.getInstallationState()}.")
                if (!state.configGroupDevices) { 
                    href "pageEnterAutomationDevice", title: "Create an Automation Device", description: "Group device features into a single device for routines"
                } else {
                    href "pageEnterAutomationDevice", title: "Create an Automation Device", description: "Group device into a single device for routines"
                    if (state.configGroupDevices) {
                        href "pageAmendAutomationDevice", title: "Amend an Automation Device", description: "Tap to amend an automation devices"
                        href "pageDeleteAutomationDevice", title: "Delete an Automation Device", description: "Tap to delete automation devices"
                    }
                }
            }
            displayFooter()       
        }
    }

    def pageEnterAutomationDevice() {
        LOGDEBUG("pageEnterAutomationDevice()")
        state.autoDevClear = true
        LOGDEBUG("${featureCheck()}")
        if (state.homePage) return mainPage()
        //if (state.amendAutoDevice) autoDevice = "automation_"+"${state.amendAutoDevice}"
        existingDevice = state["automation_${automationName}"]

        if (!automationName || featureCheck() != true) {
            next = "pageAutoDeviceAdmin"
        } else  {
            next = "pageCreateAutomationDevice"
        }

        dynPageProperties = [
            name:       "pageEnterAutomationDevice",
            title:      "",            
            nextPage:   next,
            install:    false,
            uninstall:  false
        ]
        if (!existingDevice || existingDevice && state.amendAutoDevice) {
            return dynamicPage(dynPageProperties) {
                if (!state.amendAutoDevice) displayMiniHeader("Automation Device Details")
                if (state.amendAutoDevice) displayMiniHeader("Amending ${state.amendAutoDevice}")
                section(){
                    paragraph "<b>Your Automation Device</b>\n"
                    if (!state.amendAutoDevice) input "automationName", "string", title: "Enter the automation device name", multiple: false, required: (automationName), submitOnChange: true
                    if (state.amendAutoDevice) paragraph "${state.amendAutoDevice}"
                }

                section (){
 
                    if (settings.automationName) if (featureCheck() != true) paragraph "<div style='color:#FF0000;font-weight:bold;font-size:16px;'>Select a device to continue</div>"
                    paragraph "<b>Available Switches</b>\n"
                    state.finalDeviceList.each { deviceId, name ->

                            LOGDEBUG("deviceId: ${deviceId}")
                            LOGDEBUG("name: ${name}")

                            switchPresent = state.deviceDetail["${deviceId}"].features.switch
                            dimPresent = state.deviceDetail["${deviceId}"].features.dimLevel
                            
                            if (switchPresent)  {
                                if (!state.amendAutoDevice) input "${deviceId}", "bool", title: "${name}", defaultValue: false, submitOnChange: true
                                if (state.amendAutoDevice) input "${deviceId}", "bool", title: "${name}", defaultValue: (state["automation_${state.amendAutoDevice}"].devices.contains(name)), submitOnChange: true
                            }
                        }
                }
            }
        } else {
            return dynamicPage(dynPageProperties) {
                displayMiniHeader("Change The Automation Device Details")
                section() {
                    def ErrorMessage = ""
                    ErrorMessage += "<div style='color:#FF0000;font-weight:bold;font-size:16px;'>Error: Automation device exists - Delete or choose another name</div>"
                    paragraph(ErrorMessage)
                }
                section(){
                    paragraph "<b>Your Automation Device</b>\n"
                    input "automationName", "string", title: "Change the automation device name", multiple: false, required: (automationName), submitOnChange: true
                }
            }
        }
    }

    def pageCreateAutomationDevice() {
        LOGDEBUG("pageCreateAutomationDevice()") 
        state.availGroupFeatures = []
        def switchOptions = [:]
            switchOptions << ["9": "do not include"]
            switchOptions << ["1": "on"]
            switchOptions << ["0": "off"]

        def dimOptions = [:]
            dimOptions << ["9": "do not include"]
            dimOptions << ["10": "10"]
            dimOptions << ["20": "20"]
            dimOptions << ["30": "30"]
            dimOptions << ["40": "40"]
            dimOptions << ["50": "50"]
            dimOptions << ["60": "60"]
            dimOptions << ["70": "70"]
            dimOptions << ["80": "80"]
            dimOptions << ["90": "90"]
            dimOptions << ["100": "100"]

        if (state.amendAutoDevice) {
           mapOrigFeatures = [:]
            LOGDEBUG(state.apiBatchFeatureWrite["${state.amendAutoDevice}"].features)

            for (int i = 0; i < state["automation_"+"${state.amendAutoDevice}"].features.size; i++) {
                identStr = state.apiBatchFeatureWrite["${state.amendAutoDevice}"].features[i].replace('{"featureId": "', "")
                newIdentStr = identStr.replace(' "value":', "")
                penIdentStr = newIdentStr.replace('"', "")
                ultSplit = penIdentStr.replace('}', "")
                finalIdentSplit = ultSplit.split(",", 2)
                origFeatureId = finalIdentSplit[0]
                String origValue = finalIdentSplit[1].trim()

                mapOrigFeatures["${origFeatureId}"] = "${origValue}"
            }
        LOGDEBUG("mapOrigFeatures ${mapOrigFeatures}")
        }

        return dynamicPage(name: "pageCreateAutomationDevice", title: "", nextPage: "pageAutomationDeviceCreated", install: false, uninstall: false){
            displayMiniHeader("${settings.automationName}")

            section() {
                paragraph "<b>Select the feature values</b>\n"
                state.finalDeviceList.each { key, value ->
                    deviceId = key
                    name = value

                    if (settings["${key}"] == true) {
                        if (state.deviceDetail["${key}"].features.switch) {
                            switchFeatureId = state.deviceDetail["${key}"].features.switch.featureId
                            state.availGroupFeatures.push(switchFeatureId)

                            input "${switchFeatureId}", "enum", title: "Select <b>${name}</b> switch value", options: switchOptions, required: true
                                           
                            if (state.amendAutoDevice) {
                                if (state["automation_"+"${state.amendAutoDevice}"].features.contains(switchFeatureId)) {
                                    String sVal = mapOrigFeatures["${switchFeatureId}"]
                                    LOGDEBUG("${switchFeatureId}  ${sVal}")
                                    app.updateSetting("${switchFeatureId}",[value: sVal, type: "string"])
    
                                } else {
                                    app.updateSetting("${switchFeatureId}",[value: "9", type: "string"])
                                }
                            }

                            if (state.deviceDetail["${key}"].features.dimLevel) {
                                dimLevelFeatureId = state.deviceDetail["${key}"].features.dimLevel.featureId
                                state.availGroupFeatures.push(dimLevelFeatureId)

                                input "${dimLevelFeatureId}", "enum", title: "Select <b>${name}</b> dim value", options: dimOptions, required: true
                            
                                if (state.amendAutoDevice) {
                                    if (state["automation_"+"${state.amendAutoDevice}"].features.contains(dimLevelFeatureId)) {
                                        String dVal = mapOrigFeatures["${dimLevelFeatureId}"].toString()
                                        app.updateSetting("${dimLevelFeatureId}",[value: "${dVal}", type: "string"])
                                    } else {
                                        app.updateSetting("${dimLevelFeatureId}",[value: "9", type: "string"])
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    def pageAutomationDeviceCreated() {
        LOGDEBUG("pageAutomationDeviceCreated()") 
        autoDeviceCreate()
        return dynamicPage(name: "pageAutomationDeviceCreated", nextPage: "pageAutoDeviceAdmin", title: "", install: false, uninstall: false){
            def DeviceCreate = ""
            if (!state.amendAutoDevice) {
                displayMiniHeader("Creation result")
                DeviceCreate += "Automation device ${settings.automationName} created.\n\n"
            } else {
                displayMiniHeader("Amendment result")
                DeviceCreate += "Automation device ${settings.automationName} amended.\n\n"
            }
            section() {
                paragraph(DeviceCreate)
            }
            displayFooter()
        }
    }

    def pageAmendAutomationDevice() {
        LOGDEBUG("pageAmendAutomationDevice()")
        state.autoDevClear =  true 
        if (state.amendAutoDevice) {
            return pageEnterAutomationDevice()
         } else {

            return dynamicPage(name: "pageAmendAutomationDevice", title: "", nextPage: "pageAutoDeviceAdmin", install: false, uninstall: false){
                displayMiniHeader("Amend Automation Device")
                section() {
                    paragraph "Choose the automation device to amend"
                    state.configGroupDevices.each { value ->
                        input "2A@${value}", "button", title: "Amend ${value}", submitOnChange: false
                    }
                }
            }
         }
    }


    def pageDeleteAutomationDevice() {
        LOGDEBUG("pageDeleteAutomationDevice()") 
        state.autoDevClear = true
        return dynamicPage(name: "pageDeleteAutomationDevice", title: "", nextPage: "pageAutoDeviceAdmin", install: false, uninstall: false){
            displayMiniHeader("Delete Automation Devices")
            section() {
                if (state.automationName) {
                    def AboutState = ""
                    AboutState += "<div style='color:#FF0000;font-weight:bold;font-size:16px;'>${state.automationName} device deleted\n\n</div>"
                    paragraph(AboutState)
                }
                state.configGroupDevices.each { value ->
                    input "2D@${value}", "button", title: "Delete ${value}", submitOnChange: false
                }
            }
        }
    }


    def removeExternalEvent(webhookId) {
        LOGDEBUG("removeExternalEvent()")
        state.lastExtEvtRemove = false

        state.webhookEvents.each { webhookEvent ->
            LOGDEBUG("${webhookId}  ${webhookEvent.id}")
            if (webhookId == webhookEvent.id) {
                state.endpointId = webhookId
                if (deleteEvent(webhookEvent) == true) {
                    state.configExternalEvents.remove(webhookId)
                    webhookDni = 'endpoint_'+"${webhookId}"
                    state.remove(webhookDni)
                    state.lastExtEvtRemove = true
                }
            }
        }
    }


    def appButtonHandler(btn) {
        LOGDEBUG("appButtonHandler()")   
        switch (btn) {
            case "clearWebhookEventInfo":
                state.remove("webhookEventInfo")
                break
            case "deleteEvents":
                deleteEvents(getEvents())
                break
            case "getEvents":
                state.getEvents = true
                getEvents()
                break
            case "createEvents":
                children = getChildDevices()
                LOGDEBUG("children: ${children}")
                children.each {addWebhook(it.deviceNetworkId, it.label)}
                break    
            case "btnHomePage":
			    state.homePage = true
			    break
            case "apiTestButton":
                getAccessToken()
                LOGTRACE ("API test button handled")
                break
            case "apiBatchFeatureRead":
                poll()
                LOGTRACE ("Batch read button handled")
                break

            default:
                arrOfBtn = btn.split("@", 2);
                possMaintBtn = arrOfBtn[0]
                dni = arrOfBtn[1]
                LOGDEBUG("appButtonHandler ${dni}")
                switch (possMaintBtn) {
                    case "getEventsId":
                        state.getEventId = true
                        getEventId(dni)
                        break
                    case "2A":
                        state.amendAutoDevice = dni
                        app.updateSetting("automationName", [value: "${state.amendAutoDevice}", type:"string"])
                        state.finalDeviceList.each { deviceId, name ->
                            app.updateSetting("${deviceId}", [value: state["automation_${state.amendAutoDevice}"].devices.contains(name), type: "bool"])
                        }
                        LOGDEBUG("automation name ${settings.automationName}")
                        LOGDEBUG("amendAutoDevice ${state.amendAutoDevice}")
                        break
                    case "2D":
                        state.configGroupDevices.remove("${dni}")
                        state.apiBatchFeatureWrite.remove("${dni}")        
                        removeAutomationDevice(dni)
                        break
                    case "4D":
                        removeExternalEvent(dni)
                        break
                    default:
                        break
                }
        }
    }

    def settingsRemove() {
        // state.remove("addEndpointClear")
        // state.remove("configExternalEvents")
        if (state?.lastExtEvtRemove != null) state.remove("lastExtEvtRemove")
        if (settings.eventId) app.removeSetting("eventId")
        if (state.endpointId) state.remove("endpointId")

        LOGDEBUG("settingsRemove()")
        if (state.webhookEventInfo) state.remove("webhookEventInfo")      
        if (state.homePage) state.remove("homePage")
        if (state.getEvents) state.remove("getEvents")
        if (state.getEventId) state.remove("getEventId")
        
        if (settings.getEventsId) app.removeSetting("getEventsId")
        if (settings.endpointUrl) app.removeSetting("endpointUrl")
        if (settings.endpointId) app.removeSetting("endpointId")

        //automation device state & settings clear
        if (state.autoDevClear || state.addExEventClear) {
            state.finalDeviceList.each { key, value ->
                app.removeSetting("${key}")
            }
            
            if (settings.automationName) app.removeSetting("automationName")
            if (state.availGroupFeatures) state.remove("availGroupFeatures")


            state.finalDeviceList.each { key, value ->
                if (settings["${automationName}_${key}"]) app.removeSetting("${automationName}_${key}")
                if (settings["${key}"]) app.removeSetting("${key}")
            
                switchPresent = state.deviceDetail["${key}"].features.switch
                dimPresent = state.deviceDetail["${key}"].features.dimLevel

                if (switchPresent) {
                    setting = switchPresent.featureId
                    if (settings["${setting}"]) app.removeSetting("${setting}")
                }
                if (dimPresent) {
                    setting = dimPresent.featureId
                    if (settings["${setting}"]) app.removeSetting("${setting}")
                }
            }
            if (state.automationName) state.remove("automationName")
            if (state.amendAutoDevice) state.remove("amendAutoDevice")
            if (state.autoDevClear) state.remove("autoDevClear")
            if (state.addExEventClear) state.remove("addExEventClear")
        }
    }

    def getFeatureWrite(featureSetId, value, attribute) {
        LOGDEBUG("getFeatureWrite()")
        LOGDEBUG("${featureSetId}, ${value}, ${attribute}")
        String strBody = '{"value": '+value+'}'
        LOGDEBUG("${strBody}")
        featureId = state.deviceDetail["${featureSetId}"].features["${attribute}"].featureId
        LOGDEBUG("${featureId}")

        params = [
            uri: getFeatureWritePath()+"${featureId}",
            contentType: "application/json",
            body: strBody,
            headers: ["Authorization": "bearer ${state.apiAccessToken}", "Content-Type": "application/json"]
        ]

        try {
            httpPost(params) { resp ->
                if (resp.status == 200) {
                    
                    //send device events to update attributes       
                    getSendChildEvent(featureId, value)
                    LOGDEBUG("${resp.data}")
                    LOGTRACE("getFeatureWrite() OK")
                }
            }
        } catch (Exception e) {
            def error = e.toString()
            LOGERROR("getFeatureWrite() - ${error}")
            state.apiLastError = error
        }
    }

    def getChildFeatureBatchReadPath(features) {
        LOGDEBUG("getChildFeatureBatchReadPath()")
        strFeatures = '{"features": '+features+'}'
        LOGDEBUG("${strFeatures}")
        def soutJson = new JsonSlurper().parseText(strFeatures)
        String jsonFeatures = JsonOutput.toJson(soutJson)
        LOGDEBUG("${jsonFeatures}")

        params = [
            uri: getFeatureBatchReadPath(),
            contentType: "application/json",
            body: jsonFeatures,
            headers: ["Authorization": "bearer ${state.apiAccessToken}", "Content-Type": "application/json"]
        ]

        try {
            httpPost(params) { resp ->
                if (resp.status == 200) {
                    data = resp.data

                    //send device events to update attributes       
                    data.each { featureId, value ->
                        pauseExecution(1)
                        getSendChildEvent(featureId, value)
                    }

                    LOGDEBUG("${resp.data}")
                    LOGTRACE("getChildFeatureReadBatch() OK")
                }
            }
        } catch (Exception e) {
            def error = e.toString()
            LOGERROR("getChildFeatureReadBatch() - ${error}")
            state.apiLastError = error
        }
    }

    def getFeatureBatchWrite(features) {
        LOGDEBUG("getFeatureBatchWrite()")
        strFeatures = '{"features": '+features+'}'
        def soutJson = new JsonSlurper().parseText(strFeatures)
        String jsonFeatures = JsonOutput.toJson(soutJson)
        LOGDEBUG("${jsonFeatures}")

        params = [
            uri: getFeatureBatchWritePath(),
            contentType: "application/json",
            body: jsonFeatures,
            headers: ["Authorization": "bearer ${state.apiAccessToken}", "Content-Type": "application/json"]
        ]

        try {
            httpPost(params) { resp ->
                if (resp.status == 200) {
                    LOGDEBUG("${resp.data}")
                    LOGTRACE("getFeatureBatchWrite() OK")
                }
            }
        } catch (Exception e) {
            def error = e.toString()
            LOGERROR("getFeatureBatchWrite() - ${error}")
            state.apiLastError = error
        }
    }

    def getFeatureReadBatch() {
        LOGDEBUG("getFeatureReadBatch()")
        arrForRead = []

        state.arrFeatureId.each { featureId ->
            String strFeatureId = '{"featureId": "'+featureId+'"}'
            arrForRead.push(strFeatureId)
        }

        strFeatureRead = '{"features": '+arrForRead+'}'
        def soutJson = new JsonSlurper().parseText(strFeatureRead)
        String jsonFeatureRead = JsonOutput.toJson(soutJson)

        params = [
            uri: getFeatureBatchReadPath(),
            contentType: "application/json",
            body: jsonFeatureRead,
            headers: ["Authorization": "bearer ${state.apiAccessToken}", "Content-Type": "application/json"]
        ]

        try {
            httpPost(params) { resp ->
                if (resp.data) {
                    data = resp.data

                    //send device events to update attributes       
                    data.each { featureId, value ->
                        pauseExecution(1)
                        getSendChildEvent(featureId, value)
                    }
                    LOGDEBUG("${resp.data}")
                    LOGTRACE("getFeatureReadBatch() OK")
                }
            }
        } catch (Exception e) {
            def error = e.toString()
            LOGERROR("getFeatureReadBatch() - ${error}")
            state.apiLastError = error
        }
    }

    def createAppData(data) {
        LOGDEBUG("createAppData()")
        state.arrFeatureId = []
        deviceList = [:]
        state.deviceDetail = [:]
        state.deviceState = [:]
        state.mapFeatureToDevice = [:]

        data.devices.each { deviceData ->
            //pauseExecution(3)

            //map parent device info to be held in featureSet device detail state
            def mapParent = [:]
            mapParent["parentDevice"] = ["parentDeviceId": deviceData.get("deviceId")]
            parentProductCode = deviceData.get("productCode")
            mapParent["parentDevice"] << ["parentProduct": deviceData.get("product")]
            mapParent["parentDevice"] << ["parentDevice": deviceData.get("device")]
            mapParent["parentDevice"] << ["parentDesc": deviceData.get("desc")]
            mapParent["parentDevice"] << ["parentType": deviceData.get("type")]
            mapParent["parentDevice"] << ["parentCat": deviceData.get("cat")]
            mapParent["parentDevice"] << ["parentGen": deviceData.get("gen")]
            
            deviceData.featureSets.eachWithIndex { featureSet, i ->

                //set featureSet Name as Device Name
                def key = featureSet.get("featureSetId")
                def deviceName = featureSet.get("name")

                //create deviceDetail state for the first time
                //state.deviceDetail[key] = mapParent
                state.deviceDetail[key] = ["featureSetName": deviceName, "parentDevice": ["parentProductCode": parentProductCode]]
            
                //needed to get dni for children add/delete
                state.deviceDetail[deviceName] = key            
            
                //create a device list
                deviceList[key] = deviceName

                state.deviceState[key] = ["webhook": false]

                featureSet.features.eachWithIndex { feature, idx ->
                    def featureId = feature.get("featureId")
                    def featureType = feature.get("type")
                
                    LOGDEBUG("deviceName: ${deviceName}")
                    LOGDEBUG("key: ${key}")
                    LOGDEBUG("featureId: ${featureId}")

                    switch(featureType) {
                        case "rgbColor":
                            featureType = "colorMode"
                            break
                        case "protection":
                            featureType = "lock"

                        default:
                            break
                    }
                
                    def featureWritable = feature.get("writable")

                    //map the feature to add to the device detail: used to map child attribute to featureId
                    switch(idx) {
                        case 0:
                            state.deviceDetail[key].features = ["${featureType}": ["featureId": "${featureId}", "writable": "${featureWritable}"]]
                            break

                        default:
                            state.deviceDetail[key].features << ["${featureType}": ["featureId": "${featureId}", "writable": "${featureWritable}"]]
                    }
          
                    //get the response type for correct event sending
                    responseType = defineResponseType(featureType)

                    //create the device feature states
                    state.deviceState[key] << ["${featureId}": ["value": "unknown"]]
                    state.mapFeatureToDevice[featureId] = ["deviceId": "${key}", "type": featureType, "responseType": responseType, "name": deviceName]

                    //Setting up map for Bulk feature read
                    state.arrFeatureId.push(featureId)
                }
            }
        }
        //add webhook value
        

        //create feature states for bulk read of features
      
        state.finalDeviceList = deviceList.sort() //{ it.value.toLowerCase() }
        state.configGetAstructure = true

        LOGDEBUG("created App Data")

    }

    def getAstructure(){
        LOGDEBUG("getStructure()")
        state.configGetAstructure = false

        params = [
            uri: getDiscoveryStructurePath() + "${state.structureId}",
            contentType: "application/json",
            headers: ["Authorization": "bearer ${state.apiAccessToken}"]
        ]
        
        try {
            httpGet(params) { resp ->
                if (resp.data) {
                    data = resp.data

                    createAppData(data)

                    LOGDEBUG("${resp.data}")
                    LOGTRACE("getAstructure() OK")
                }
            }
        } catch (Exception e) {
            def error = e.toString()
            LOGERROR("getAStructure() - ${error}")
            state.apiLastError = error
        }
    }

    def getStructure() {
        LOGDEBUG("getStructure()")
        state.configGetStructure = false
        state.structureId = ""

        params = [
            uri: getDiscoveryStructuresPath(),
            contentType: "application/json",
            headers: ["Authorization": "bearer ${state.apiAccessToken}"]
        ]
        
        try {
            httpGet(params) { resp ->
        
                if (resp.data) {
                    data = resp.data
                    structures = data.get("structures")
                    state.structureId = structures.get(0)
                    state.configGetStructure = true
                    LOGDEBUG("${resp.data}")
                    LOGTRACE("getStructure() OK")
                }
            }
        } catch (Exception e) {
            def error = e.toString()
            LOGERROR("getStructure() - ${error}")
            state.apiLastError = error
        }
    }

    def getAccessToken() {
        LOGDEBUG("getAccessToken()")
        state.session = false
        LOGDEBUG("pre-API call refresh token: ${settings.apiRefreshToken}")

        params = [
            uri: getVendorAuthPath(),
            contentType: "application/json",
            body: ["grant_type": "refresh_token", "refresh_token": "${settings.apiRefreshToken}"],
            headers: ["authorization": "basic ${settings.apiBasicToken}"]
        ]
        
        try {
            httpPost(params) { resp ->
                if (resp.data) {
                    data = resp.data
                    state.apiRefreshToken = data.get("refresh_token")
                    app.updateSetting("apiRefreshToken", [value: "${state.apiRefreshToken}", type:"string"])
                    state.apiBasicToken = settings.apiBasicToken
                    state.apiAccessToken = data.get("access_token")
                    apiSessionExpiresIn = data.get("expires_in").toInteger()
                    state.session = true
                    myRunIn(apiSessionExpiresIn, refreshSession)
                    LOGDEBUG("${resp.data}")
                    LOGTRACE("getAccessToken() OK")
                }
            }
        } catch (Exception e) {
            def error = e.toString()
            LOGERROR("getAccessToken() - ${error}")
            state.apiLastError = error 
        }
    }

    def deleteEvent(event){
        LOGDEBUG("deleteEvent()")
        result = false

        if (!events) {
            LOGDEBUG("No events")
            return
        }

        LOGDEBUG("event ${event}")
        params = [
            uri: apiEventsPath() + event.id,
            contentType: "application/json",
            headers: ["Authorization": "bearer ${state.apiAccessToken}", "Content-Type": "application/json"]
        ]

        try {
            httpDelete(params) { resp ->
                if (resp.status == 200) {
                    LOGDEBUG("${resp.data}")
                    LOGTRACE("deleteEvent() OK")   
                    result = true 
                }
            }
        } catch (Exception e) {
            def error = e.toString()
            LOGERROR("deleteEvent() - ${error}")
            state.apiLastError = error
        }
        return result
    }


    def deleteEvents(events){
        LOGDEBUG("deleteEvents()")
        deleteEventsStatus = true

        if (!events) {
            LOGDEBUG("No events")
            return
        }

        events.each { event ->
        LOGDEBUG("event ${event}")
            params = [
                uri: apiEventsPath() + event.id,
                contentType: "application/json",
                headers: ["Authorization": "bearer ${state.apiAccessToken}", "Content-Type": "application/json"]
            ]

            try {
                httpDelete(params) { resp ->
                    if (resp.status == 200) {
                        state.webhookInstalled = false
                        state.remove("webhookEvents")
                        app.updateSetting("installWebhook", [value: false, type: "bool"])
                        LOGDEBUG("${resp.data}")
                    }
                }
            } catch (Exception e) {
                def error = e.toString()
                LOGERROR("deleteEvents() - ${error}")
                deleteEventsStatus == false
                state.apiLastError = error
            }
        }
          if (deleteEventsStatus == false) {
            LOGERROR("Error in deleteEvents() call")

        } else {
            LOGTRACE("deleteEvents() OK")
        }
    }

    def getEvents() {
        LOGDEBUG("getEvents()")

        params = [
            uri: apiEventsPath(),
            contentType: "application/json",
            headers: ["Authorization": "bearer ${state.apiAccessToken}", "Content-Type": "application/json"]
        ]

        try {
            httpGet(params) { resp ->
                if (resp.status == 200) {
                    if (!state.getEvents) LOGDEBUG("${resp.data}")
                    if (state.getEvents == true) LOGTRACE("${resp.data}")
                    state.webhookEvents = resp.data
                    //state.webhookInstalled = true
                    LOGTRACE("getEvents() OK")
                    return resp.data
                }
            }
        } catch (e) {
            def error = e.toString()
            LOGTRACE("Error in getEvents() call")
            LOGERROR("${error}")
            state.apiLastError = error
        }
    }
        
   def getEventId(eventId) {
        LOGDEBUG("getEventId()")
        LOGDEBUG("${eventId}")
        params = [
            uri: apiEventsPath() + eventId,
            contentType: "application/json",
            headers: ["Authorization": "bearer ${state.apiAccessToken}", "Content-Type": "application/json"]
        ]

        try {
            httpGet(params) { resp ->
                if (resp.status == 200) {
                    if (!state.getEventId) LOGDEBUG("${resp.data}")
                    if (state.getEventId == true) LOGTRACE("${resp.data}")
                    state.webhookEventInfo = resp.data
                    LOGTRACE("getEventId() OK")
                    return resp.data
                }
            }
        } catch (e) {
            def error = e.toString()
            LOGTRACE("Error in getEventId() call")
            LOGERROR("${error}")
            state.apiLastError = error
        }
    }
    def defineResponseType(featureType) {
        LOGDEBUG("defineResponseType()")
        //setting up a database of features for api response
        def responseType = ""
        switch (featureType) {
            case "switch":
                responseType = "switch"
                break

            case "outletInUse":
                responseType = "bool"
                break
                                    
            case "lock": //protection
                responseType = "lock"
                break

            case "power":
                responseType = "number"
                break

            case "energy":
                responseType = "number"
                break

            case "identify":
                responseType = "switch"
                break

            case "reset":
                responseType = "unknown"
                break

            case "upgrade":
                responseType = "unknown"
                break

            case "diagnostics":
                responseType = "unknown"
                break

            case "periodOfBroadcast":
                responseType = "unknown"
                break

            case "colorMode": //rgbColor
                responseType = "number"
                break

            case "voltage":
                responseType = "number"
                break

            case "dimLevel":
                responseType = "number"
                break

            case "currentTime":
                responseType = "unknown"
                break
            
            case "buttonPress":
                responseType = "unknown"
                break

            case "time":
                responseType = "unknown"
                break
            
            case "date":
                responseType = "unknown"
                break

            case "monthArray":
                responseType = "unknown"
                break

            case "weekdayArray":
                responseType = "unknown"
                break

            case "timeZone":
                responseType = "unknown"
                break

            case "LocationLongtitude":
                responseType = "unknown"
                break

            case "LocationLatitude":
                responseType = "unknown"
                break

            case "duskTime":
                responseType = "unknown"
                break

            case "day":
                responseType = "unknown"
                break

            case "month":
                responseType = "unknown"
                break

            case "year":
                responseType = "unknown"
                break

        case "weekday":
                responseType = "unknown"
                break

            default:
                responseType = "default"
        }
        return responseType
    }

    def createDevices(){
        LOGDEBUG("createDevices()")
        state.finalDeviceList.each { key, value ->
            LOGDEBUG("deviceId: ${key}, value: ${value}")
            createDevice(key, value)
        }
    }

    def createNewDevices() {
        LOGDEBUG("createNewDevices()")
        state.newDevices = 0
        state.finalDeviceList.each { key, value ->
            LOGDEBUG("deviceId: ${key}, value: ${value}")
            def currentDevice = getChildDevice(key)
            if(currentDevice != null) {
                LOGDEBUG("device with ID: ${key} exsist, skipping")
            } else {
                state.newDevices += 1
                createDevice(key, value)
            }
        }
    }

    def createDevice(id, name) {
        LOGDEBUG("name: ${name}")
        deviceType = state.deviceDetail["${id}"].parentDevice.parentProductCode.split("MK")[0] //Remove extras from Product codes like MK3
        LOGDEBUG("deviceType: ${deviceType}")

        switch("${deviceType}") {
            case "LW400":
            deviceFile = "${state.ExternalName} - ${deviceType}"
            break

            case "L42":
            deviceFile = "${state.ExternalName} - ${deviceType}"
            break

            default:
                deviceFile = "${state.ExternalName} - Lightwave Switch"
        }

        try {
            createChildDevice(deviceFile, id, id, name)
        } catch (Exception e) {
            def error = e.toString()
            LOGERROR("Error creating device: ${error}")
        }
    }

    def createChildDevice(deviceFile, dni, name, label) { 
        LOGDEBUG("createChildDevice()")
        def hub = location.hubs[0]

        try {
            def existingDevice = getChildDevice(dni)
            if(!existingDevice) {
                def childDevice = addChildDevice("wogapat", deviceFile, dni, [name: name, label: label])
                LOGTRACE("Child created: ${label}")
                state.configChildrenExist = true

                if (installWebhook) {
                    addWebhook(dni, label)
                }

            } else {
                LOGERROR("Device ${dni} already exists")
            }
        } catch (Exception e) {
            def error = e.toString()
            LOGERROR("Error creating device: ${error}")
        }
    }

    def createAutomationDevice(label) {
        LOGDEBUG("createAutomationDevice()")
        deviceFile = "${state.ExternalName} - Lightwave Automation Device"
        dni = label
        name = "LWRF Automation Device - ${label}"
        def hub = location.hubs[0]

        try {
            def existingDevice = getChildDevice(dni)
            if(!existingDevice) {
                def childDevice = addChildDevice("wogapat", deviceFile, dni, [name: name, label: label])
                LOGTRACE("Child automation device created: ${label}")
                state.configChildrenExist = true

            } else {
                LOGERROR("Device ${dni} already exists")
            }
        } catch (Exception e) {
            def error = e.toString()
            LOGERROR("Error creating device: ${error}")
        }
    }

    def getSendChildEvent(featureId, value) {
        LOGDEBUG("getSendChildEvent()")
        LOGDEBUG("featureId: ${featureId}, value: ${value}")
        //map the feature Id to the device to be updated
        device = state.mapFeatureToDevice[featureId]
        LOGDEBUG("device: ${device}")
        // map the response to variables
        deviceId = device.deviceId
        deviceName = device.name
        attribute = device.type
        responseType = device.responseType

        if (responseType == "switch") {
            switch (value) {
                case(0):
                    finValue = "off"
                    break
                    
                case(1):
                    finValue = "on"
                    break
            }
        } else if (responseType == "bool") {
            switch (value) {
                case(0):
                    finValue = "false"
                    break
                    
                case(1):
                    finValue = "true"
                    break
            }
        
        } else if (responseType == "lock") {
            attribute = "childProtection"
            switch (value) {
                case(0):
                    finValue = "unlocked"
                    break
                    
                case(1):
                    finValue = "locked"
                    break
            }

        } else if (responseType == "number") {
            finValue = value

        } else if (responseType == "unknown") {
            finValue = "${attribute} Unknown: ${value}"

        } else {
            finValue = "${attribute} Default: ${value}"
        }

        def properties = ["name": attribute, "value": finValue]

        try {
            sendEvent(deviceId, properties)

            //update states
            state.deviceState["${deviceId}"]["${featureId}"] = [value: "${finValue}"]
            if (deviceName == "Outside light") {LOGDEBUG("Set ${deviceName} ${attribute} to ${finValue}")}
            if (state.installed == true ) {
                LOGTRACE("Set ${deviceName} ${attribute} to ${finValue}")
            }
        } catch (Exception e) {
            def error = e.toString()
            LOGERROR("Error sending child event - ${error}")
            return
        }
    }

        def getChildDni(deviceName) {
        dni = state.deviceDetail["${deviceName}"]

        if (dni) {
            child = getChildDevice(dni)
    
        } else {
            LOGERROR("${deviceName} mappings can't be found.")
        }
    }



    def addWebhookExternal(webhookId) {
        LOGDEBUG("addWebhookExternal()")
        result = false

        arrEvents = []
        webhookUrl = state["endpoint_${webhookId}"].url

        state["endpoint_${webhookId}"].features.each { featureId ->
            arrEvents.push('{"type": "feature", "id": "'+featureId+'"}')
        }

        strBody = '{"events": '+arrEvents+',"url":"'+webhookUrl+'","ref":"'+webhookId+'"}'

        def soutJson = new JsonSlurper().parseText(strBody)
        String jsonBody = JsonOutput.toJson(soutJson)
        LOGDEBUG("${jsonBody}")

        params = [
            uri: apiCreateEventsPath(),
            contentType: "application/json",
            body: jsonBody,
            headers: ["Authorization": "bearer ${state.apiAccessToken}", "Content-Type": "application/json"]
        ]

        try {
            httpPost(params) { resp ->
                if (resp.status == 200) {
                    LOGDEBUG("${resp.data}")
                    LOGTRACE("Webhook ID ${id} installed successfully")
                    result = true
                }
            }
        } catch (Exception e) {
            def error = e.toString()
            LOGERROR("Webhook external install - ${error}")
            state.apiLastError = error
        }
        return result
    }


    def addWebhook(dni, label) {
        LOGDEBUG("addWebhook()")
        id = dni[-5..-1]
        if (!state.accessToken) {
            createAccessToken() // create our own OAUTH access token to use in webhook url
        }
        arrEvents = []
        
        //don't add for automation devices
        if (state?.configGroupDevices != null && state.configGroupDevices.contains(dni)) {
            LOGDEBUG("addWebhook - Skipping automation device ${dni}")
            return

        } else {
            state.deviceDetail["${dni}"].features.each {key, feature ->

                featureId = feature.featureId
                arrEvents.push('{"type": "feature", "id": "'+featureId+'"}')
        
            }   
        }
    

        strBody = '{"events": '+arrEvents+',"url":"'+getWebhookUrl()+'","ref":"'+id+'"}'

        def soutJson = new JsonSlurper().parseText(strBody)
        String jsonBody = JsonOutput.toJson(soutJson)
        LOGDEBUG("${jsonBody}")

        params = [
            uri: apiCreateEventsPath(),
            contentType: "application/json",
            body: jsonBody,
            headers: ["Authorization": "bearer ${state.apiAccessToken}", "Content-Type": "application/json"]
        ]

        try {
            httpPost(params) { resp ->
                if (resp.status == 200) {
                    state.webhookInstalled = true
                    app.updateSetting("installWebhook", [value: true, type: "bool"])
                    LOGDEBUG("${resp.data}")
                    LOGTRACE("Webhook ID ${id} installed successfully")
                }
            }
        } catch (Exception e) {
            def error = e.toString()
            LOGERROR("Webhooks install - ${error}")
            state.webhookInstalled = false
            app.updateSetting("installWebhook", [value: false, type: "bool"])
            state.apiLastError = error
        }
    }

    private removeAutomationDevice(dni) {
        LOGDEBUG("removeAutomationDevice()")
	    LOGDEBUG("deleting ${dni} automation device")
        state.configGroupDevices.remove(dni)
        autoDni = 'automation_'+"${dni}"
        state.remove(autoDni)
        state.apiBatchFeatureWrite.remove(dni)
        state.automationName = dni
        deleteChildDevice(dni)
        LOGTRACE ("Automation device $dni deleted")
	}


   private removeChildDevices(delete) {
	    LOGDEBUG("removeChildDevices()")
	    LOGDEBUG("deleting ${delete.size()} devices")
	    delete.each {
		    deleteChildDevice(it.deviceNetworkId)
	    }
    }

    def getChildren() {
        LOGDEBUG("getChildren()")
        children = getChildDevices()
        LOGDEBUG("children: ${children}")
        state.configChildrenExist = false
        if (children) state.configChildrenExist = true
    }

    def childPoll(deviceId) {
        LOGDEBUG("childPoll()")
        LOGDEBUG("deviceId: ${deviceId}")
        def arrFeatures = []
        features = state.deviceDetail["${deviceId}"].features
        features.each { featureType, feature ->
            String strFeatureId = '{"featureId": "'+feature.featureId+'"}'
            arrFeatures.push(strFeatureId)
        }
        LOGDEBUG("${arrFeatures}")
        getChildFeatureBatchReadPath(arrFeatures)
    }

    def setAutoDeviceOn(deviceNetworkId) {
        LOGDEBUG("setAutoDeviceOn()")
        features = state.apiBatchFeatureWrite["${deviceNetworkId}"].features
        getFeatureBatchWrite(features)
    }

    def setChildSwitch(deviceNetworkId, value) {
        LOGDEBUG("setChildSwitch()")
        getFeatureWrite(deviceNetworkId, value, "switch")
    }

    def setChildSetLevel(deviceNetworkId, nextLevel) {
        LOGDEBUG("setChildSetLevel()")
        getFeatureWrite(deviceNetworkId, nextLevel, "dimLevel")
    }

    def setChildProtection(deviceNetworkId, value) {
        LOGDEBUG("setChildProtection()")
        getFeatureWrite(deviceNetworkId, value, "lock")
    }

    def setChildIdentify(deviceNetworkId, value) {
        LOGDEBUG("setChildIdentify()")
        getFeatureWrite(deviceNetworkId, value, "identify")
    }

    def webhook(){
        LOGDEBUG("webhook()")
        def jsonSlurper = new groovy.json.JsonSlurper()
	    def messageJSON = request.JSON
        LOGDEBUG("$messageJSON")
        triggerId = messageJSON.triggerEvent.id
        value = messageJSON.payload.value
        LOGTRACE("Webhook trigger: ${triggerId} ${value}")
        getSendChildEvent(triggerId, value)
    }


    def getHubType(){
        if (location.hubs[0].id.toString().length() > 5) { return "SmartThings" } else { return "Hubitat" }
    }

    def LOGDEBUG(txt){
        if (state.debugMode) { 
            def msgfrom = "[PARENT] "
            def appLabel = (app?.label == null) ? state.InternalName : app.label //Some child calls to parent.LOGDEBUG result in app.label being null, correct
            appLabel = appLabel.replace(" ","")
            appLabel.toUpperCase()
            if (txt?.contains("[CHILD:")) { msgfrom = "" }

            try {
                log.debug("${appLabel}(${state.version}) || ${msgfrom}${txt}")
            } catch(ex) {
                log.error("LOGDEBUG unable to output requested data! || err:${ex}")
            }
        }
    }

    def LOGTRACE(txt){
        def msgfrom = "[PARENT] "
        def appLabel = (app?.label == null) ? state.InternalName : app.label //Some child calls to parent.LOGTRACE result in app.label being null, correct
        appLabel = appLabel.replace(" ","")
        appLabel.toUpperCase()
        if (txt?.contains("[CHILD:")) { msgfrom = "" }

        try {
            log.trace("${appLabel}(${state.version}) || ${msgfrom}${txt}")
        } catch(ex) {
            log.error("LOGTRACE unable to output requested data!")
        }
    }

    def LOGERROR(txt){
        def msgfrom = "[PARENT] "
        def appLabel = (app?.label == null) ? state.InternalName : app.label //Some child calls to parent.LOGERROR result in app.label being null, correct
        appLabel = appLabel.replace(" ","")
        appLabel.toUpperCase()
        if (txt?.contains("[CHILD:")) { msgfrom = "" }
        try {
        log.error("${appLabel}(${state.version}) || ${msgfrom}ERROR: ${txt}")
        } catch(ex) {
            log.error("LOGERROR unable to output requested data!")
        }
    }

    def disableDebug(){
        LOGDEBUG("disableDebug()")
        LOGTRACE("Debug timer has expired. Disabling debugging")
        state.debugMode = false
        unschedule("disableDebug")
        app.updateSetting("debugmode", [value: false, type:"bool"])
    }

    def refreshSession() {
        LOGDEBUG("refreshSession()")
        state.session = false
        getAccessToken()
    }

    def poll() {
        LOGTRACE("Poll()")
        getFeatureReadBatch()
    }

    private def myRunIn(delay_s, func) {
        if (delay_s > 0) {
            def tms = now() + (delay_s * 1000)
            def date = new Date(tms)
            runOnce(date, func)
        }
    }

    def installed() {
        LOGDEBUG("installed()")
        state.installed = true
        LOGTRACE("Installed ${state.version}")
        initialize()
    }

    def updated() {
        LOGDEBUG("updated()")
        if (installWebhook == true && !state.webhookInstalled) {
            children = getChildDevices()
            children.each {addWebhook(it.deviceNetworkId, it.label)}
	    
        } else if (installWebhook == false && state.webhookInstalled) {
    	    deleteEvents(getEvents())
        }
        LOGDEBUG("webhook: ${state.webhookInstalled}")
        initialize()
    }

    def subscribe(){
        LOGDEBUG("subscribe()")
        subscribe(location, "systemStart", handleReboot)
    }
    def handleReboot(evt) {
        LOGDEBUG("handleReboot()")
        myRunIn(600, getAccessToken)
    }

    def initialize() {
        LOGDEBUG("initialize()")
        if (!(checkConfig())) { 
            def msg = ""
            msg = "ERROR: App not properly configured!  Can't start.\n"
            msg += "ERRORs:\n${state.configErrorList}"
            LOGTRACE(msg)
            if (state.hubType == "SmartThings") {sendNotificationEvent(msg)}
            state.polledDevices = ""
            return //App not properly configured, exit, don't subscribe
        }
        subscribe()
        getAccessToken()
        poll()
        version()
        settingsRemove()
        LOGTRACE("Initialized (Parent Version: ${state.version})")
        if (state.hubType == "SmartThings") {sendNotificationEvent("${app.label.replace(" ","").toUpperCase()}: Settings activated")}
    //End initialize()
    }


    def uninstalled() {
        LOGDEBUG("uninstalled()")
        if (state.webhookInstalled) {
    	    deleteEvents(getEvents())
        }
        
	    removeChildDevices(getChildDevices())
        unschedule()
    }


    def checkConfig() {
        LOGDEBUG("checkConfig()")
        def configErrorList = ""
        getChildren()
        if (!(state.installed == true)) {
            configErrorList += "  ** state.installed not True,"
        } 
        if (!state.apiBasicToken) {
            configErrorList += "  ** check API basic bearer token, ${state.apiBasicToken}"
        }
        if (!state.apiRefreshToken) {
            configErrorList += "  ** check API refresh token"
            state.installed = false
        }
        if (!state.configGetStructure == true) {
            configErrorList += "  ** No structure"
            state.installed = false
        }
        if (!state.configChildrenExist == true) {
            configErrorList += "  ** No devices"
            state.installed = false
        }
        if (!(configErrorList == "")) { 
            LOGDEBUG ("checkConfig() returning FALSE (${configErrorList})")
            state.configOk = false
            return false //Errors occurred.  Config check failed.
        } else {
        LOGDEBUG ("checkConfig() returning true")
            // return false during setup testing
            state.installed = true
            state.configOk = true
            return true
        }
    }

