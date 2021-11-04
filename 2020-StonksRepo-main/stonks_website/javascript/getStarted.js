var teamName = ""
var teamCode = ""
var botName = "";
const timeBetweenRequests = 10;

function GETISNAMEVALID(){
    return "http://localhost:8080/is_bot_name_valid?botName=" + botName;
}

function GETREGISTERBOT(){
    let q =  "http://localhost:8080/add_bot?";
    q += "teamName=" + teamName;
    q += "&teamCode=" + teamCode;
    q += "&botName=" + botName;
    return q;
}

function sleep(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

var sendQueary = function(Url, responseFunc){
    sleep(timeBetweenRequests).then( ()=>{
        let HttpRequest = new XMLHttpRequest();
        HttpRequest.open("GET", Url);
        HttpRequest.onreadystatechange=(e)=>{
            if (HttpRequest.readyState == 4 && HttpRequest.status == 200)
                responseFunc(HttpRequest.responseText);
        }
        HttpRequest.send();
        console.log("Sent a request to URL" + Url);
    });
}

var sayIfAllGood = function(resp){
    if(resp === "{\"result\":\"true\",\"information\":\"Bot successfully added\"}"){
        alert("Bot successfully added!");
    }
    else{
        alert(resp);
    }
}

var respondToValidCheck = function(resp){
    if(resp === "{\"result\":\"true\",\"information\":\"Bot name is valid\"}"){
        sendQueary(GETREGISTERBOT(), sayIfAllGood);
    }
    else{
        alert("That bot name is already in use");
    }
}

var doTheThing = function(){
    teamName = document.getElementById("teamname").value;
    teamCode = document.getElementById("teamCode").value;
    botName = document.getElementById("bot").value;

    sendQueary(GETISNAMEVALID(), respondToValidCheck);

}