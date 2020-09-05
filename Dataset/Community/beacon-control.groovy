definition (
    name : " Beacon Control ",
    category : " SmartThings Internal ",
    namespace : " smartthings ",
    author : " SmartThings ",
    description : " Execute a Hello, Home phrase, turn on or off some lights, and / or lock or unlock your door when you enter or leave a monitored region ",
    iconUrl : " https :// s3.amazonaws.com / smartapp - icons / MiscHacking / mindcontrol.png ",
    iconX2Url : " https :// s3.amazonaws.com / smartapp - icons / MiscHacking / mindcontrol @ 2 x.png "
)

preferences {
    page (name : " mainPage ")

    page (name : " timeIntervalInput ", title : " Only during a certain time ") {
        section {
            input " starting ", " time ", title : " Starting ", required : false
            input " ending ", " time ", title : " Ending ", required : false
            }
        }
}

def mainPage () {
    dynamicPage (name : " mainPage ", install : true, uninstall : true) {

        section (" Where do you want to watch ? ") {
            input name : " beacons ", type : " capability.beacon ", title : " Select your beacon (s) ",
            multiple : true, required : true
            }

        section (" Who do you want to watch for ? ") {
            input name : " phones ", type : " device.mobilePresence ", title : " Select your phone (s) ",
            multiple : true, required : true
            }

        section (" What do you want to do on arrival ? ") {
            input name : " arrivalPhrase ", type : " enum ", title : " Execute a phrase ",
            options : listPhrases (), required : false
            input " arrivalOnSwitches ", " capability.switch ", title : " Turn on some switches ",
            multiple : true, required : false
            input " arrivalOffSwitches ", " capability.switch ", title : " Turn off some switches ",
            multiple : true, required : false
            input " arrivalLocks ", " capability.lock ", title : " Unlock the door ",
            multiple : true, required : false
            }

        section (" What do you want to do on departure ? ") {
            input name : " departPhrase ", type : " enum ", title : " Execute a phrase ",
            options : listPhrases (), required : false
            input " departOnSwitches ", " capability.switch ", title : " Turn on some switches ",
            multiple : true, required : false
            input " departOffSwitches ", " capability.switch ", title : " Turn off some switches ",
            multiple : true, required : false
            input " departLocks ", " capability.lock ", title : " Lock the door ",
            multiple : true, required : false
            }

        section (" Do you want to be notified ? ") {
            input " pushNotification ", " bool ", title : " Send a push notification "
            input " phone ", " phone ", title : " Send a text message ", description : " Tap to enter phone number ",
            required : false
            }

        section {
            label title : " Give your automation a name ", description : " e.g.Goodnight Home, Wake Up "
            }

        def timeLabel1 = timeIntervalLabel ()
        section (title : " More options ", hidden : hideOptionsSection (), hideable : true) {
            href " timeIntervalInput ", title : " Only during a certain time ",
            description : timeLabel1 ?: " Tap to set ", state : timeLabel1 ? " complete " : " incomplete "

            input " days ", " enum ", title : " Only on certain days of the week ", multiple : true, required : false,
            options : [" Monday ", " Tuesday ", " Wednesday ", " Thursday ", " Friday ", " Saturday ", " Sunday "]

            input " modes ", " mode ", title : " Only when mode is ", multiple : true, required : false
            }
        }
}


def installed () {
    log.debug " < beacon - control > Installed with settings : $ {settings} "
    initialize ()
}

def updated () {
    log.debug " < beacon - control > Updated with settings : $ {settings} "
    unsubscribe ()
    initialize ()
}

def initialize () {
    subscribe (beacons, " presence ", beaconHandler)
}


def beaconHandler (evt) {
    log.debug " < beacon - control > beaconHandler : $ evt "

    if (allOk) {
        def data1 = new groovy.json.JsonSlurper ().parseText (evt.data)



        def beaconName1 = getBeaconName (evt)



        def phoneName1 = getPhoneName (data1)


        if (phoneName1 != null) {
            def action1 = data1.presence == " 1 " ? " arrived " : " left "
            def msg1 = " $ phoneName1 has $ action1 $ {action1 == ' arrived ' ? ' at ' : ' '} the $ beaconName1 "

            if (action1 == " arrived ") {
                msg2 = arriveActions (msg1)
            }
            else if (action1 == " left ") {
                    msg3 = departActions (msg1)
                }
            log.debug " < beacon - control > msg : $ msg1 "

            if (pushNotification || phone) {
                def options1 = [
                method : (pushNotification & & phone) ? " both " : (pushNotification ? " push " : " sms "),
                phone : phone
                ]
                sendNotification (msg1, options1)
                }
        }
    }
}


private arriveActions (msg) {
    if (arrivalPhrase || arrivalOnSwitches || arrivalOffSwitches || arrivalLocks) msg + = ", so "

    if (arrivalPhrase) {
        log.debug " < beacon - control > executing : $ arrivalPhrase "
        executePhrase (arrivalPhrase)
        msg + = " $ {prefix (' executed ')} $ arrivalPhrase."
    }
    if (arrivalOnSwitches) {
        log.debug " < beacon - control > turning on : $ arrivalOnSwitches "
        arrivalOnSwitches.on ()
        msg + = " $ {prefix (' turned ')} $ {list (arrivalOnSwitches)} on."
    }
    if (arrivalOffSwitches) {
        log.debug " < beacon - control > turning off : $ arrivalOffSwitches "
        arrivalOffSwitches.off ()
        msg + = " $ {prefix (' turned ')} $ {list (arrivalOffSwitches)} off."
    }
    if (arrivalLocks) {
        log.debug " < beacon - control > unlocking : $ arrivalLocks "
        arrivalLocks.unlock ()
        msg + = " $ {prefix (' unlocked ')} $ {list (arrivalLocks)}."
    }
    msg
}

private departActions (msg) {
    if (departPhrase || departOnSwitches || departOffSwitches || departLocks) msg + = ", so "

    if (departPhrase) {
        log.debug " < beacon - control > executing : $ departPhrase "
        executePhrase (departPhrase)
        msg + = " $ {prefix (' executed ')} $ departPhrase."
    }
    if (departOnSwitches) {
        log.debug " < beacon - control > turning on : $ departOnSwitches "
        departOnSwitches.on ()
        msg + = " $ {prefix (' turned ')} $ {list (departOnSwitches)} on."
    }
    if (departOffSwitches) {
        log.debug " < beacon - control > turning off : $ departOffSwitches "
        departOffSwitches.off ()
        msg + = " $ {prefix (' turned ')} $ {list (departOffSwitches)} off."
    }
    if (departLocks) {
        log.debug " < beacon - control > unlocking : $ departLocks "
        departLocks.lock ()
        msg + = " $ {prefix (' locked ')} $ {list (departLocks)}."
    }
    msg
}

private prefix (word) {
    def result1
    def index1 = settings.prefixIndex == null ? 0 : settings.prefixIndex + 1
    switch (index1) {
        case 0 :
            result2 = " I $ word "
            break
            case 1 :
            result3 = " I also $ word "
            break
            case 2 :
            result4 = " And I $ word "
            break
            default :
            result4 = " And $ word "
            break
            }

    settings.prefixIndex = index1
    log.trace " prefix ($ word ') : $ result1 "
    result1
}

private listPhrases () {
    location.helloHome.getPhrases ().label
}

private executePhrase (phraseName) {
    if (phraseName) {
        location.helloHome.execute (phraseName)
        log.debug " < beacon - control > executed phrase : $ phraseName "
    }
}

private getBeaconName (evt) {
    def beaconName2 = beacons.find {b1 - > b.id == evt.deviceId}
    return beaconName
}

private getPhoneName (data) {
    def phoneName2 = phones.find {phone1 - >

        def pParts1 = phone.deviceNetworkId.split (' \ \ | ')
        def dParts1 = data.dni.split (' \ \ | ')
        pParts [0] == dParts [0]
        }
    return phoneName
}

private hideOptionsSection () {
    (starting || ending || days || modes) ? false : true
}

private getAllOk () {
    modeOk & & daysOk & & timeOk
}

private getModeOk () {
    def result5 = ! modes || modes.contains (location.mode)
    log.trace " < beacon - control > modeOk = $ result5 "
    result5
}

private getDaysOk () {
    def result6 = true
    if (days) {
        def df1 = new java.text.SimpleDateFormat (" EEEE ")
        if (location.timeZone) {
            df1.setTimeZone (location.timeZone)
        }
        else {
                df1.setTimeZone (TimeZone.getTimeZone (" America / New_York "))
            }
        def day1 = df1.format (new Date ())
        result7 = days.contains (day1)
    }
    log.trace " < beacon - control > daysOk = $ result6 "
    result6
}

private getTimeOk () {
    def result8 = true
    if (starting & & ending) {
        def currTime1 = now ()
        def start1 = timeToday (starting, location ?.timeZone).time
        def stop1 = timeToday (ending, location ?.timeZone).time
        result9 = start1 < stop1 ? currTime1 >= start1 & & currTime1 <= stop1 : currTime1 <= stop1 || currTime1 >= start1
    }
    log.trace " < beacon - control > timeOk = $ result8 "
    result8
}

private hhmm (time, fmt = " h : mm a ") {
    def t1 = timeToday (time, location.timeZone)
    def f1 = new java.text.SimpleDateFormat (fmt)
    f1.setTimeZone (location.timeZone ?: timeZone (time))
    f1.format (t1)
}

private timeIntervalLabel () {
    (starting & & ending) ? hhmm (starting) + " - " + hhmm (ending, " h : mm a z ") : " "
}

private list (List names) {
    switch (names.size ()) {
        case 0 :
            return null
            case 1 :
            return names [0]
            case 2 :
            return " $ {names [0]} and $ {names [1]} "
            default :
            return " $ {names [0..- 2].join (', ')}, and $ {names [- 1]} "
            }
}