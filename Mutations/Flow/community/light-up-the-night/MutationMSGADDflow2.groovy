
definition(
    name:"Light Up the Night",
    namespace:"smartthings",
    author:"SmartThings",
    description:"Turn your lights on when it gets dark and off when it becomes light again.",
    category:"Convenience",
    iconUrl:"https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet-luminance.png",
    iconX2Url:"https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet-luminance@2 x.png"
)

preferences{
    section("Monitor the luminosity..."){
        input"lightSensor","capability.illuminanceMeasurement"
        }
    section("Notifications"){
        input("recipients","contact",title:"Send notifications to",required:false){
            input"sendPushMessage","enum",title:"Send a push notification?",options:["Yes","No"],required:false
            input"phoneNumber","phone",title:"Send a Text Message?",required:false
            }
        }
    section("Turn on a light..."){
        input"lights","capability.switch",multiple:true
        }
}

def installed(){
    subscribe(lightSensor,"illuminance",illuminanceHandler)
}

def updated(){
    def message="Hello"
    message="${lightSensor}"
    sendSms(message,11111111111)
    unsubscribe()
    subscribe(lightSensor,"illuminance",illuminanceHandler)
}


def illuminanceHandler(evt){
    def lastStatus=state.lastStatus
    if(lastStatus!="on"&&evt.integerValue<30){
        lights.on()
        state.lastStatus="on"
    }
    else if(lastStatus!="off"&&evt.integerValue>50){
            lights.off()
            state.lastStatus="off"
        }
}

