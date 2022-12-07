    import java.text.DecimalFormat
    import groovy.json.JsonSlurper
    import groovy.json.JsonOutput

    definition (
    author: "Patrick Wogan",
    category: "Home Automation",
    documentationLink: "",
    iconUrl: "",
    importUrl: "",
    iconX2Url: "",
    name: "WogaLWRF",
    description: "LightwaveRF Link Plus Smart Series Integration",
    namespace: "wogapat",
    oauth: true,
    singleInstance: true
    )
    
    {
    appSetting "apiBasicToken"
    appSetting "apiRefreshToken"
    }

    preferences {
        page(name: "mainPage")
        page(name: "pageIntegrationSettings")
        page(name: "pageTestApi")
        page(name: "pageAppState")
        page(name: "startUpTest")
        page(name: "pageCreateDevices")
        //page(name: "pageCreateNewDevices")
        page(name: "pageDiscoverAstructure")
        page(name: "pageDiscoveryStructures")
        page(name: "pagePostInstallConfigure")
        page(name: "pageAutoDeviceAdmin")
        page(name: "pageEnterAutomationDevice")
        page(name: "pageCreateAutomationDevice")
        page(name: "pageAdminAutomationDevice")
        page(name: "pageAutomationDeviceCreated")
        page(name: "pageTestBatchWrite")
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
    private apiCreateEventsPath() { getApiUrl()+"/v1/events"}
    private getWebhookUrl()		{ getServerUrl()+ "/webhook?access_token="+URLEncoder.encode("${state.accessToken}", "UTF-8") }
    private getServerUrl() 		{ return getFullApiServerUrl() }
    
    mappings {
        path("/webhook") {action: [GET: "webhook", POST: "webhook"]}
    }

    def mainPage(){
        state.namespace = "wogapat"
        setVersion()
        state.hubType = getHubType()
        LOGDEBUG("Hub Type: ${state.hubType}")
        setFormatting()
        if (checkConfig()) { 
            // Do nothing here, but run checkConfig() 
        } 
        dynamicPage(name: "mainPage", title: "WogaLWRF", install: false, uninstall: (app.getInstallationState() == "COMPLETE")){
            section(){
                LOGDEBUG("install state=${app.getInstallationState()}.")
                def mydebug_pollnow = ""
                if (!state.configOk == true) { 
                    href "pageIntegrationSettings", title:"Configure", description:"Tap to configure the lwRF API settings"
                } else {
                    settingsRemove()
                    href "pageAppState", title:"Status", description:"Tap to view the application status"
                    href "pagePostInstallConfigure", title: "App Configuration, Preferences & Testing", description: "Configuration settings, App preferences & debugging"
                    href "pageAutoDeviceAdmin", title: "Automation Devices", description: "Add, change or delete devices for routines"
                    //href "pageCreateNewDevices", title:"Discover a new device", description:"Tap to discover new Lightwave devices"
                }
            }
            section("About"){
                def AboutApp = ""
                if (state.hubType == "Hubitat") {AboutApp += "<HR>Integrate Hubitat directly with Lightwave Link using ${state.ExternalName} and the Link Plus Smart Series API)\n\n"}
                AboutApp += "In order to integrate with Lightwave Link you will need to have a valid and active Lightwave Link Plus Account. You’ll then need to request an API Bearer and a Refresh Token. To do this go to the ‘settings’ page on either the Link Plus app or the web client <a target='_blank' href='https://my.lightwaverf.com'>https://my.lightwaverf.com</a>.\n\nIn your Lightwave Link Plus Account section, look for ‘API Integration’ and ‘Get Token'. At this point you will be required to enter your Link Plus account password.\n\nOnce successful you will be presented with the bearer and refresh tokens. These tokens can then be copied to ${state.ExternalName} settings.\n\nIMPORTANT\nRefresh token only useable once. ${state.ExternalName} uses a new refresh token from the response to connect and updates the settings to show the new code. In case of error you can create a new refresh token directly in the your Link account and update it in the  ${state.ExternalName} settings.\n"
                paragraph(AboutApp)
            }
                
            section("") {
                //updateCheck()
                //checkButtons()
                displayVersionStatus()
            }
            section("<HR><HR><B><CENTER>Donations</CENTER></B>"){
                def DonateOptions = ""
                DonateOptions += "${state.ExternalName} is provided to the community for free.  It takes a lot of time to build and support any complex app.  If you wish to support the time and effort put into development you may submit a donation with one of the following:<BR>"
                DonateOptions += "<ul>"
                DonateOptions += '<li><B>Paypal.me</B> = <a target="_blank" href="https://paypal.me/pswogan">https://paypal.me/pswogan</a></li>'
                paragraph(DonateOptions)                    
            }
        }
    }

    def pageViewStructure() {}
    def pageAppState() {
    dynamicPage(name: "pageAppState", title: "Application state", install: false, uninstall: false){
        section() {
            def AboutState = ""
                AboutState += "<B>Lightwave session</B>\n"
                AboutState += "Refresh Token: ${state.apiRefreshToken}\n"
                AboutState += "API Access Token: ${state.apiAccessToken}\n"
                AboutState += "Active Session: ${state.session}\n\n"
                AboutState += "<B>Device details</B>\n"
                state.finalDeviceList.each { dev ->
                devId = dev.getKey()
                devName = dev.getValue()
                AboutState += "<B>${devName}</B>\n"
                state.deviceDetail[devId].each {
                AboutState += "${it}\n"
                }
                AboutState += "\n"
                }
                paragraph(AboutState)
            }
        }
    }

    def pageTestApi() {
        dynamicPage(name: "pageTestApi", title: "Test Api", install: true, uninstall: false){
            section(""){
                input "debugmode", "bool", title: "${state.formatSettingRootStart}Enable debug logging${state.formatSettingRootEnd}", required: true, defaultValue: false, submitOnChange: true
                input "testapi", "bool", title: "${state.formatSettingRootStart}Check API${state.formatSettingRootEnd}", required: true, defaultValue: false, submitOnChange: true
                input "stateread", "bool", title: "${state.formatSettingRootStart}Batch state read from Lightwave${state.formatSettingRootEnd}", required: true, defaultValue: false, submitOnChange: true
            }
            section("Preferences") {
        	    input "installWebhook", "bool", title: "Activate webhooks", description: "", required: true, submitOnChange: false
            }
            if (debugmode) { 
                if (state.debugMode == false || state?.debugMode == null){
                    state.debugMode = true; myRunIn(1800, disableDebug); LOGTRACE("Debug logging has been enabled.  Will auto-disable in 30 minutes.")
                }
            } else {
                state.debugMode = false; unschedule("disableDebug");  LOGTRACE("Debug logging is not enabled.")
            }
            if (testapi) { 
                getAccessToken()
                testapi = false
            }
            if (stateread) { 
                poll()
                stateread = false
            }
        }
    } 

    def pageCreateDevices() {
        createDevices()
        dynPageProperties = [
            name:       "pageCreateDevices",
            title:      "Creating devices",
            install:    true,
            uninstall:  false
        ]
        return dynamicPage(dynPageProperties) {
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
            section("<HR><HR><B><CENTER>Donations</CENTER></B>"){
                def DonateOptions = ""
                DonateOptions += "${state.ExternalName} is provided to the community for free.  It takes a lot of time to build and support any complex app.  If you wish to support the time and effort put into development you may submit a donation with one of the following:<BR>"
                DonateOptions += "<ul>"
                DonateOptions += '<li><B>Paypal.me</B> = <a target="_blank" href="https://paypal.me/pswogan">https://paypal.me/pswogan</a></li>'  
                paragraph(DonateOptions)              
            }             
        }
    }

    def pageDiscoverAstructure() {
        getAstructure()
        if (state.configGetAstructure == true) {
        dynPageProperties = [
                name:       "pageDiscoverAstructure",
                title:      "Discovering your devices",
                nextPage:   "pageCreateDevices",
                install:    false,
                uninstall:  false
            ]
        } else {
        dynPageProperties = [
                name:       "pageDiscoverAstructure",
                title:      "Discovering your devices",
                install:    true,
                uninstall:  false
            ]
        }
        return dynamicPage(dynPageProperties) {
            section() {
                def DiscoverAstructure = ""
                if (state.configGetAstructure == true) {
                DiscoverAstructure += "Lightwave devices were discovered. Your devices will be created in the Hubitat ecosystem."
                } else {
                    DiscoveryAStructure += "There was an error when discovering your Lightwave devices:\n"
                    DiscoverAstructure += "${state.apiLastError}"
                }
                paragraph(DiscoverAstructure)
            }
            
            if (state.configGetAstructure == true) {
                section("Preferences") {
                    input "installWebhook", "bool", title: "Activate webhooks", description: "", required: true
                }
            }
        }
    }

    def pageDiscoveryStructures() {
        getStructure()
        if (state.configGetStructure == true) {}
            dynPageProperties = [
                name:       "pageDiscoveryStructures",
                title:      "Discovering your structure",
                nextPage:   "pageDiscoverAstructure",
                install:    false,
                uninstall:  false
            ]      
        return dynamicPage(dynPageProperties) {
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

    def startUpTest() {
        getAccessToken()
        def myNextPage = ""
        if (!state.installed == true && state.session == true) {
            dynPageProperties = [
                name:       "startUpTest",
                title:      "API Testing",
                nextPage:   "pageDiscoveryStructures",
                install:    false,
                uninstall:  false
            ]

        } else if (!state.session == true) {
            dynPageProperties = [
                name:       "startUpTest",
                title:      "API Testing",
                nextPage:   "pageIntegrationSettings",
                install:    false,
                uninstall:  false
            ]

        } else {
            dynPageProperties = [
                name:       "startUpTest",
                title:      "API Testing",
                install:    true,
                uninstall:  false
            ]
            StartupTest += "API tested successfully. Session established.\n\n"
        }
        return dynamicPage(dynPageProperties) {
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

    def pagePostInstallConfigure() {
        getAccessToken()
        dynamicPage(name: "pagePostInstallConfigure", nextPage: "mainPage", install: true, uninstall: false){
            section("<HR><B><CENTER>API Configuration</CENTER></B>"){
            }
            section ("<B>Basic Token:</B>"){
                input "apiBasicToken", "string", title: "Enter the API basic token", multiple: false, required: true, submitOnChange: true
                }
            section ("<B>Refresh Token:</B>"){
                input "apiRefreshToken", "string", title: "Enter the API refresh token:", multiple: false, required: true, submitOnChange: true
                input "apiTestButton", "button", title: "Test API", submitOnChange: false
            }
            section ("<B>API Status:</B>") {
                def APIState = ""
                
                if (state.session == true) {
                    APIState += "Lightwave Linkplus connection established.\n"
                }
                else {
                    APIState += "Lightwave Linkplus connection failed.\n"
                    APIState += "Error: ${state.apiLastError}"
                }
                paragraph(APIState)
            }
            section("<HR><B><CENTER>Preferences</CENTER></B>") {
        	    input "installWebhook", "bool", title: "Activate webhooks", description: "", submitOnChange: false
            }
            section ("<HR><B><CENTER>Debug</CENTER></B>") {
                input "debugmode", "bool", title: "Enable debug logging", defaultValue: false, submitOnChange: true  
                input "apiBatchFeatureRead", "button", title: "Batch Feature Read", submitOnChange: false
            }
            if (debugmode) { 
                if (state.debugMode == false || state?.debugMode == null){
                    state.debugMode = true; myRunIn(1800, disableDebug); LOGTRACE("Debug logging has been enabled.  Will auto-disable in 30 minutes.")
                }
            }
        }
    }

    def pageIntegrationSettings() {
        def myNextPage = ""
        dynamicPage(name: "pageIntegrationSettings", title: "Configure settings", nextPage: "startUpTest", install: false, uninstall: false){
            section("${state.formatSettingRootStart}API Client Token${state.formatSettingRootEnd}"){
                input "apiBasicToken", "string", title: "Enter the API basic token", multiple: false, required: true, submitOnChange: true
            }
            section ("${state.formatSettingRootStart}Refresh Token:${state.formatSettingRootEnd}"){
                input "apiRefreshToken", "string", title: "Enter the API refresh token:", multiple: false, required: true, submitOnChange: true
            }
            section(){
                input "debugmode", "bool", title: "${state.formatSettingRootStart}Enable debug logging${state.formatSettingRootEnd}", required: true, defaultValue: false, submitOnChange: true
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

    def pageAutoDeviceAdmin() {
        settingsRemove()
        dynamicPage(name: "pageAutoDeviceAdmin", title: "Group Device Administration", install: true, uninstall: false) {
            section(){
                LOGDEBUG("install state=${app.getInstallationState()}.")
                def mydebug_pollnow = ""
                if (!state.configGroupDevices) { 
                    href "pageEnterAutomationDevice", title: "Create an Automation Device", description: "Group device features into a single device for routines"
                } else {
                    href "pageEnterAutomationDevice", title: "Create an Automation Device", description: "Group device into a single device for routines"
                    href "pageAdminAutomationDevice", title: "Administer Automation Devices", description: "Change or delete automation devices"
                    //href "pageTestBatchWrite", title: "Test Batch Write", description: "Test Batch Feature Write"
                }
            }       
        }
    }

    def pageEnterAutomationDevice() { 
        def existingDevice = state["automation_${automationName}"]
        if (!existingDevice) {
            dynPageProperties = [
                name:       "pageEnterAutomationDevice",
                title:      "Enter The Automation Device Details",
                nextPage:   "pageCreateAutomationDevice",
                install:    false,
                uninstall:  false
            ]
            return dynamicPage(dynPageProperties) {
                section("${state.formatSettingRootStart}Automation Name${state.formatSettingRootEnd}"){
                    input "automationName", "string", title: "Enter the automation device name", multiple: false, required: true, submitOnChange: true
                }

                section ("${state.formatSettingRootStart}Available Switches:${state.formatSettingRootEnd}"){
                    state.finalDeviceList.each { key, value ->
                        deviceId = key
                        //LOGDEBUG("deviceId: ${deviceId}")
                        name = value
                        //LOGDEBUG("name: ${name}")

                        switchPresent = state.deviceDetail["${key}"].features.switch
                        dimPresent = state.deviceDetail["${key}"].features.dimLevel
                    
                        //LOGDEBUG("deviceType: ${deviceType}")
                        if (switchPresent)  {
                            input "${key}", "bool", title: "${name}", defaultValue: false, submitOnChange: true  
                        }
                    }
                }
            }
        } 
        else {
            dynPageProperties = [
                name:       "pageEnterAutomationDevice",
                title:      "Change The Automation Device Details",
                nextPage:   "pageAutoDeviceAdmin",
                install:    false,
                uninstall:  false
            ]
            return dynamicPage(dynPageProperties) {
                section() {
                    def ErrorMessage = ""
                    ErrorMessage += "Automation device exists - Delete or choose another name"
                    paragraph(ErrorMessage)
                }
                section("${state.formatSettingRootStart}Automation Name${state.formatSettingRootEnd}"){
                    input "automationName", "string", title: "Change the automation device name", multiple: false, required: true, submitOnChange: true
                }
            }
        }
    }



    def pageCreateAutomationDevice() {
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

        dynamicPage(name: "pageCreateAutomationDevice", title: "Enter Group Details", nextPage: "pageAutomationDeviceCreated", install: false, uninstall: false){
            section() {
                // def DeviceCreate = ""
                // DeviceCreate += "Done.\n\n"
                // paragraph(DeviceCreate)
                state.finalDeviceList.each { key, value ->
                    deviceId = key
                    //LOGDEBUG("deviceId: ${deviceId}")
                    name = value
                    if (settings["${key}"] == true) {
                        if (state.deviceDetail["${key}"].features.switch) {
                            switchFeatureId = state.deviceDetail["${key}"].features.switch.featureId
                            input "${switchFeatureId}", "enum", title: "Select ${name} switch value", options: switchOptions, required: true
                            state.availGroupFeatures.push(switchFeatureId)
                        }

                        if (state.deviceDetail["${key}"].features.dimLevel) {
                            dimLevelFeatureId = state.deviceDetail["${key}"].features.dimLevel.featureId
                            input "${dimLevelFeatureId}", "enum", title: "Select ${name} dim value", options: dimOptions, required: true
                            state.availGroupFeatures.push(dimLevelFeatureId)

                        }
                    }
                }
            }
        }
    }


    def autoDeviceCreate() {
        state["automation_${settings.automationName}"] = [:]
        state["automation_${settings.automationName}"].features = []

        if (!state.configGroupDevices) {
            state.configGroupDevices = []
        }
        state.configGroupDevices.push(settings.automationName)

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
                state["automation_${settings.automationName}"].features.push('{"featureId": "'+value+'", "value": '+finValue+'}')
            }
        }
        createAutomationDevice("${settings.automationName}")
    }

    def pageAutomationDeviceCreated() {
        autoDeviceCreate()
        dynamicPage(name: "pageAutomationDeviceCreated", title: "Automation Device Creation", install: true, uninstall: false){
            section() {
                def DeviceCreate = ""
                DeviceCreate += "Automation device ${settings.automationName} created.\n\n"
                paragraph(DeviceCreate)
            }
        }
    }


    def pageAdminAutomationDevice() {
        dynamicPage(name: "pageAdminAutomationDevice", title: "Administer Devices", nextPage: "pageAutoDeviceAdmin", install: false, uninstall: false){
            section() {
                state.configGroupDevices.each { value ->
                    input "${value}", "button", title: "Delete ${value}", submitOnChange: false
                }
            }
        }


    }

    def pageTestBatchWrite() {
        dynamicPage(name: "pageTestBatchWrite", title: "Test Device", install: false, uninstall: false){
            section() {
                state.configGroupDevices.each { value ->
                    input "testbatch", "button", title: "Test ${value}", submitOnChange: false
                }
            }
        }


    }

    void appButtonHandler(btn) {
        if (btn == "apiTestButton") {
            getAccessToken()
            LOGTRACE ("API test button handled")
        }
        else if (btn == "apiBatchFeatureRead") {
            poll()
            LOGTRACE ("Batch read button handled")

        }
        // if (btn == "testbatch") {
        //     features = state['automation_group 3'].features
        //     getFeatureBatchWrite(features)
        // }
        else {
            state.configGroupDevices.remove(btn)
            setting = 'automation_'+"${btn}"
            state.remove(setting)
            removeAutomationDevice(btn)
            LOGTRACE ("Automation device $btn deleted")
        }
    }


    def settingsRemove() {
        state.remove("availGroupFeatures")
        state.finalDeviceList.each { key, value ->
                app.removeSetting("${key}")
                switchPresent = state.deviceDetail["${key}"].features.switch
                dimPresent = state.deviceDetail["${key}"].features.dimLevel
                if (switchPresent) {
                    setting = switchPresent.featureId
                    app.removeSetting("${setting}")
                }
                if (dimPresent) {
                    setting = dimPresent.featureId
                    app.removeSetting("${setting}")
                }
            }

        if (automationName != "") {
            state.finalDeviceList.each { key, value ->
                app.removeSetting("${automationName}_${key}")
            }
            app.removeSetting("automationName", [value:"", type:"string"])
        }
    }


    def getFeatureWrite(featureSetId, value, attribute) {
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
                    LOGDEBUG("${resp.data}")

                    //send device events to update attributes       
                    getSendChildEvent(featureId, value)
        
                    LOGDEBUG("getFeatureWrite() OK")
                }
            }
        } catch (e) {
            def error = e.toString()
            LOGTRACE("Error in getFeatureWrite() call")
            LOGERROR("${error}")
            state.apiLastError = error
        }
    }

    def getChildFeatureBatchReadPath(features) {
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
                if (resp.data) {
                    data = resp.data

                    //send device events to update attributes       
                    data.each { featureId, value ->
                        pauseExecution(1)
                        getSendChildEvent(featureId, value)
                    }

                    LOGTRACE("getChildFeatureReadBatch() OK")
                }
            }
        } catch (e) {
            def error = e.toString()
            LOGTRACE("Error in getChildFeatureReadBatch() call")
            LOGERROR("${error}")
            state.apiLastError = error
        }
    }

    def getFeatureBatchWrite(features) {
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

                    //send device events to update attributes       
                    //getSendChildEvent(featureId, value)
        
                    LOGDEBUG("getFeatureBatchWrite() OK")
                }
            }
        } catch (e) {
            def error = e.toString()
            LOGTRACE("Error in getFeatureBatchWrite() call")
            LOGERROR("${error}")
            state.apiLastError = error
        }
    }


    def getFeatureReadBatch() {
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

                    LOGTRACE("getFeatureReadBatch() OK")
                }
            }
        } catch (e) {
            def error = e.toString()
            LOGTRACE("Error in getFeatureReadBatch() call")
            LOGERROR("${error}")
            state.apiLastError = error
        }
    }

    def createAppData(data) {
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
                    //LOGDEBUG("deviceDetail: ${state.deviceDetail[key]}")

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

                LOGTRACE("getAstructure() OK")
            }
            }
        } catch (e) {
            def error = e.toString()
            LOGTRACE("Error in getAStructure() call")
            LOGERROR("${error}")
            state.apiLastError = error
        }
    }

    def getStructure() {
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

                    LOGTRACE("getStructure() OK")
                    LOGDEBUG("structures = ${state.structureId}")
                }
            }
        } catch (e) {
            def error = e.toString()
            LOGTRACE("Error in getStructure() call")
            LOGERROR("${error}")
            state.apiLastError = error
        }
    }

    def getAccessToken() {
        state.session = false
        LOGDEBUG("pre: ${settings.apiRefreshToken}")
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
                    LOGTRACE("getAccessToken() OK")
                    state.apiRefreshToken = data.get("refresh_token")
                    app.updateSetting("apiRefreshToken", [value: "${state.apiRefreshToken}", type:"string"])
                    state.apiBasicToken = settings.apiBasicToken
                    state.apiAccessToken = data.get("access_token")
                    apiSessionExpiresIn = data.get("expires_in").toInteger()
                    
                    state.session = true
                    myRunIn(apiSessionExpiresIn, refreshSession)
                }
            }
        } catch (e) {
        def error = e.toString()
        LOGTRACE("Error in token handling")
        LOGERROR("${error}")
        state.apiLastError = error 
        }
    }

    def deleteEvents(events){
        if (!events) {
            return
        }
        deleteEventStatus = false
        events.each { event ->

            params = [
                uri: apiEventsPath() + event.id,
                contentType: "application/json",
                //body: jsonBody,
                headers: ["Authorization": "bearer ${state.apiAccessToken}", "Content-Type": "application/json"]
                ]

            try {
                httpDelete(params) { resp ->
                    if (resp.status == 200) {
                        LOGDEBUG("${resp.data}")
                    }
                }
            } catch (e) {
                def error = e.toString()
                LOGTRACE("Error in deleteEvents() call")
                LOGERROR("${error}")
                state.apiLastError = error
                deleteEventStatus == true
            }
        }
        if (deleteEventStatus == true) {
            LOGERROR("Error in deleteEvents() call")

        } else {
            LOGTRACE("deleteEvents() OK")
        }
    }

    def getEvents() {
        params = [
            uri: apiEventsPath(),
            contentType: "application/json",
            headers: ["Authorization": "bearer ${state.apiAccessToken}", "Content-Type": "application/json"]
            ]

        try {
            httpGet(params) { resp ->
                if (resp.status == 200) {
                    LOGDEBUG("${resp.data}")
             
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

    def defineResponseType(featureType) {
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
        state.finalDeviceList.each { key, value ->
            deviceId = key
            LOGDEBUG("deviceId: ${deviceId}")
            name = value
            LOGDEBUG("name: ${name}")

            deviceType = state.deviceDetail["${deviceId}"].parentDevice.parentProductCode
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
                createChildDevice(deviceFile, deviceId, deviceId, name)

            } catch (Exception e) {
                LOGERROR("Error creating device: ${e}")
            }
        }
    }

    def createChildDevice(deviceFile, dni, name, label) { 
        def hub = location.hubs[0]
        try {
            def existingDevice = getChildDevice(dni)
            if(!existingDevice) {
                def childDevice = addChildDevice("wogapat", deviceFile, dni, [name: name, label: label])
                LOGTRACE("Child created: ${label}")
                
                if (installWebhook) {
                    addWebhook(dni, label)
                }

                state.configChildrenExist = true
            } else {
                LOGERROR("Device ${dni} already exists")
            }
        } catch (e) {
            LOGERROR("Error creating device: ${e}")
        }
    }

    def createAutomationDevice(label) { 
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
        } catch (e) {
            LOGERROR("Error creating device: ${e}")
        }
    }

    def getSendChildEvent(featureId, value) {
        LOGDEBUG("featureId: ${featureId}")
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
        } catch (e) {
            def error = e.toString()
            LOGTRACE("Error in getSendChildEvent()")
            LOGERROR("${error}")
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

    def addWebhook(dni, label) {
        id = dni[-5..-1]
        state.webhookInstalled = true
        if (!state.accessToken) {
            createAccessToken() // create our own OAUTH access token to use in webhook url
        }
        arrEvents = []
        
        state.deviceDetail["${dni}"].features.each {key, feature ->
            featureId = feature.featureId
            arrEvents.push('{"type": "feature", "id": "'+featureId+'"}')
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
                    LOGDEBUG("${resp.data}")
                    LOGTRACE("addWebhook() OK")
                }
            }
        } catch (e) {
            def error = e.toString()
            LOGTRACE("Error in addWebhook() call")
            LOGERROR("${error}")
            state.apiLastError = error
            state.webhookInstalled = false
        }
    }

    private removeAutomationDevice(dni) {
		deleteChildDevice(dni)
	}


   private removeChildDevices(delete) {
	    LOGDEBUG("In removeChildDevices")
	    LOGDEBUG("deleting ${delete.size()} devices")
	    delete.each {
		    deleteChildDevice(it.deviceNetworkId)
	    }
    }

    def getChildren() {
        children = getChildDevices()

        LOGDEBUG("children: ${children}")

        if (children) {
            state.configChildrenExist = true
        } else {
            state.configChildrenExist = false
        }  
    }

    def childPoll(deviceId) {
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
        features = state["automation_${deviceNetworkId}"].features
        getFeatureBatchWrite(features)
    }

    def setChildSwitch(deviceNetworkId, value) {
        getFeatureWrite(deviceNetworkId, value, "switch")
    }

    def setChildSetLevel(deviceNetworkId, nextLevel) {
        getFeatureWrite(deviceNetworkId, nextLevel, "dimLevel")
    }

    def setChildProtection(deviceNetworkId, value) {
        getFeatureWrite(deviceNetworkId, value, "protection")
    }

    def setChildIdentify(deviceNetworkId, value) {
        getFeatureWrite(deviceNetworkId, value, "identify")
    }

    def webhook(){

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
        LOGTRACE("Debug timer has expired. Disabling debugging")
        state.debugMode = false
        unschedule("disableDebug")
        app.updateSetting("debugmode", [value: false, type:"bool"])
    }

    def refreshSession() {
        state.session = false
        getAccessToken()
    }

    def disableGetStructure(){
        LOGTRACE("stopped API check")
        state.getstructureMode = false
        unschedule("disableCheckAPI")
        settings.getstructure = false
    }

    def disablePoll(){
        LOGTRACE("Stopped Poll")
        state.checkapiMode = false
        unschedule("disablePoll")
        settings.stateread = false
    }

    def setFormatting(){
        if (state.hubType == "Hubitat") {
            state.formatSettingRootStart = "<B><span style='color: blue;'>"
            state.formatSettingRootEnd = "</span></B>"
            state.formatSettingOptionalStart = "<B><span style='color: #6897bb;'>"
            state.formatSettingOptionalEnd = "</font></B>"
        }
        if (state.hubType == "SmartThings") { 
            state.formatSettingRootStart = ""
            state.formatSettingRootEnd = ""
            state.formatSettingOptionalStart = ""
            state.formatSettingOptionalEnd = ""
        }
    }

    def poll() {
        LOGTRACE("Poll")
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
        state.installed = true
        LOGTRACE("Installed ${state.version}")
        initialize()
        //myRunIn(60, poll) 
    //End installed()
    }

    def updated() {
        LOGDEBUG("updated()")
        if (installWebhook == true && !state.webhookInstalled) {
            children = getChildDevices()
            children.each {addWebhook(it.deviceNetworkId, it.label)}
            
            state.webhookInstalled = true
	    
        } else if (installWebhook == false && state.webhookInstalled) {
    	    deleteEvents(getEvents())
            state.webhookInstalled = false
        }
        LOGDEBUG("webhook: ${state.webhookInstalled}")
        initialize()
    }

    def subscribe(){
        subscribe(location, "systemStart", handleReboot)
    }
    def handleReboot(evt) {
       myRunIn(600, initialize)
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
        if (state.webhookInstalled) {
    	    deleteEvents(getEvents())
            state.webhookInstalled = false
        }
        
	    removeChildDevices(getChildDevices())
        unschedule()

	    log.debug "uninstalled()"
    }

    def version(){
        //Cobra update code, modified by Rayzurbock
        //resetBtnName()
        //schedule("0 0 9 ? * FRI *", updateCheck) //  Check for updates at 9am every Friday
        //updateCheck()  
        //checkButtons()
        //pauseOrNot()
    }

    def checkConfig() {
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

    def displayVersionStatus(){
        state.versionStatus = "Current"
        section{paragraph "<BR>${state.ExternalName} - Version: $state.version <BR><font face='Lucida Handwriting'></font>"}
    }

    def setVersion(){
            //Cobra update code, modified by Rayzurbock
            state.version = "1.2.5"
            state.InternalName = "wogalwrf"
            state.ExternalName = "WogaLWRF"
    }