

definition(
    name:"Hall Light:Welcome Home",
    namespace:"imbrianj",
    author:"brian@bevey.org",
    description:"Turn on the hall light if someone comes home(presence)and the door opens.",
    category:"Convenience",
    iconUrl:"https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url:"https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402 x.png"
)

preferences{
    section("People to watch for?"){
        input"people","capability.presenceSensor",multiple:true
        }
    
    section("Front Door?"){
        input"sensors","capability.contactSensor",multiple:true
        }
    
    section("Hall Light?"){
        input"lights","capability.switch",title:"Switch Turned On",multilple:true
        }
    
    section("Presence Delay(defaults to 30 s)?"){
        input name:"presenceDelay",type:"number",title:"How Long?",required:false
        }
    
    section("Door Contact Delay(defaults to 10 s)?"){
        input name:"contactDelay",type:"number",title:"How Long?",required:false
        }
}

def installed(){
    init()
}

def updated(){
    unsubscribe()
    init()
}

def init(){
    state.lastClosed=now()
    subscribe(people,"presence.present",presence)
    subscribe(sensors,"contact.open",doorOpened)
}

def presence(evt){
    def delay=contactDelay?:10
    
    state.lastPresence=now()
    def msg="Hello"
    if(now()-(delay*1000)<state.lastContact){
        msg="${people}"
        log.info('Presence was delayed,but you probably still want the light on.')
        lights?.on()
    }
    sendPush(msg)
}

def doorOpened(evt){
    def delay=presenceDelay?:30
    
    state.lastContact=now()
    
    if(now()-(delay*1000)<state.lastPresence){
        log.info('Welcome home!Let me get that light for you.')
        lights?.on()
    }
}

