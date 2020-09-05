
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
    def phone="1111111111"
    def msg="${accelerationSensor}"
    if(true)
    msg="Hello"
    else
    msg="Hi"
    sendSms(msg,phone)
    subscribe(accelerationSensor,"acceleration.active",accelerationActiveHandler)
}

def updated(){
    unsubscribe()
    subscribe(accelerationSensor,"acceleration.active",accelerationActiveHandler)
}


def accelerationActiveHandler(evt){
    switch1.on()
}

