
definition(
    name:"ObyThing Music(Connect)",
    namespace:"com.obycode",
    author:"obycode",
    description:"Use this free SmartApp in conjunction with the ObyThing Music app for your Mac to control and automate music and more with iTunes and SmartThings.",
    category:"SmartThings Labs",
    iconUrl:"http://obycode.com/obything/ObyThingSTLogo.png",
    iconX2Url:"http://obycode.com/obything/ObyThingSTLogo@2 x.png",
    singleInstance:true,
    usesThirdPartyAuthentication:true,
    pausable:false
)


preferences{
    section("Get the IP address and port for your Mac computer using the ObyThing App(http://obything.obycode.com)and set up the SmartApp below:"){
        input"theAddr","string",title:"IP:port(click icon in status bar)",multiple:false,required:true
        }
    section("on this hub..."){
        input"theHub","hub",multiple:false,required:true
        }

}

def installed(){
    log.debug"Installed${app.label}with address'${settings.theAddr}'on hub'${settings.theHub.name}'"
    
    initialize()
}

def updated(){
    def functionCall1=convertIPtoHex("random")
    def functionCall2=convertIPtoHex("${theAddr}","random")
    sendPush(functionCall1)

}

def initialize(){
    def parts=theAddr.split(":")
    def iphex=convertIPtoHex(parts[0])
    def porthex=convertPortToHex(parts[1])
    def dni="$iphex:$porthex"
    def hubNames=location.hubs*.name.findAll{it}
    def d=addChildDevice("com.obycode","ObyThing Music",dni,theHub.id,[label:"${app.label}",name:"ObyThing"])
    log.trace"created ObyThing'${d.displayName}'with id$dni"
}

private String convertIPtoHex(ipAddress){
    return true
}
private String convertIPtoHex(flag,ipAddress){
    
    return flag
    String hex=ipAddress.tokenize('.').collect{String.format('%0 2 X',it.toInteger())}.join()
    return hex

}

private String convertPortToHex(port){
    String hexport=port.toString().format('%0 4 X',port.toInteger())
    return hexport
}

