/*
    Some people may wonder why all of this is a bunch of global variables enforcing some
    state, like a badly written imperative program. That is because this is a badly written
    imperative program. No one likes Javascript. You don't, I don't, the internet doesn't.
    I have tried making this have classes. I have tried adding reusability. Chrome spits
    errors at me. I cry, I ctrl Z. This is how it is. It works. Be happy
*/

function sleep(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

var mainURL = "http://localhost:8080/";
var quearyMaxBotValue = "get_max_bot_value";
var quearyBotHTML = "get_bot_status?maxValueString=";
var graphDataURL = "http://localhost:8080/get_bot_status";

var currentTransactionNumber = 0;
var getTransactionsURL = "http://localhost:8080/get_transactions?from=";
var transactionUrlEnd = "&to=end";

var currentNewsIndex = 0;
var getNewsURL = "http://localhost:8080/get_news_from?index=";
const maxNewsfeedSize = 15;

const timeBetweenRequests = 2100;

var chart = null;
var data = null;
var lastStates = [];
var getBotState = function(returnedString){
    let splits = returnedString.split(',');
    return {
        name : splits[0],
        value : parseFloat(splits[1])
    }
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

var getGraphData = function(response) {
    let splitStrings = response.split('#');
    console.log(response);
    let botStates = [];
    for(let i = 0; i < splitStrings.length - 1; i++){
        botStates.push(getBotState(splitStrings[i]));
    }
    //Has the bot order changed?
    let changed = false;
    //If it is a different length it changed
    if(botStates.length != lastStates.length){
        changed = true;
    } else{ //Otherwise you have to look through the ordering to see if it is the same
        for(let i = 0; i < botStates.length; i++){
            if(botStates[i].name != lastStates[i].name){
                changed = true;
                break;//Its changed, no need to look further
            }
        }
    }
    lastStates = botStates;

    chart.data.labels = [];
    let datum = chart.data.datasets[0];
    datum.data = [];
    for(let i = 0; i < botStates.length; i++){
        chart.data.labels.push(botStates[i].name);
        datum.data.push(botStates[i].value);
    }

    let updateValue = 1;
    if(changed){
        updateValue = 0;
    }
    chart.update(updateValue);

    sendQueary(graphDataURL, getGraphData);
}

var addTransactions = function(response){
    if(response != ""){
        let table = document.getElementById("transactionTable");
        let adds = response.split('#');
        
        currentTransactionNumber += adds.length;
            
        for(let i = 0; i < adds.length; i++){
            table.innerHTML += adds[i];
        }
    }


    sendQueary(getTransactionsURL + currentTransactionNumber.toString() + transactionUrlEnd, addTransactions);
}

var setupNewsfeed = function(){
    let newsContainer = document.getElementById("Recent_News_Feed");
    for(let i = 0; i < maxNewsfeedSize; i++){
        if(i != 0){
            newsContainer.innerHTML += "<hr>";
        }
        newsContainer.innerHTML += "<div id=\"innerNews" + i.toString() + "\"></div>"
    }
}

var pushDownNews = function(){
    for(let i = maxNewsfeedSize-1; i > 0; i--){
        console.log("innerNews" + (i-1).toString() + " to " + "innerNews" + i.toString());
        let up = document.getElementById("innerNews" + (i-1).toString());
        let down = document.getElementById("innerNews" + i.toString());
        down.innerHTML = up.innerHTML;
    }
}

var addNews = function(response){
    if(response != "None"){
        let newsContainer = document.getElementById("Recent_News_Feed");
        let additions = response.split('#');
        for(let i = 0; i < additions.length; i++){
            pushDownNews();
            document.getElementById("innerNews0").innerHTML = additions[i];
            currentNewsIndex++;
        }
    }

    sendQueary(getNewsURL + currentNewsIndex.toString(), addNews);
}

var main = function() {
    chart = new Chart(document.getElementById("leaderboardGraph"),
    {
        "type":"bar",
        "data":{
            "labels":["January","February","March","April","May","June","July"],
            "datasets":[{
                 "label":"Total Capital",
                 "data":[65,59,80,81,56,55,40],
                 "fill":false,
                 "backgroundColor":[
                    "rgba(255, 99, 132, 0.2)","rgba(255, 159, 64, 0.2)","rgba(255, 205, 86, 0.2)","rgba(75, 192, 192, 0.2)",
                    "rgba(54, 162, 235, 0.2)","rgba(153, 102, 255, 0.2)","rgba(201, 203, 207, 0.2)"
                 ],
                 "borderColor":[
                    "rgb(255, 99, 132)","rgb(255, 159, 64)","rgb(255, 205, 86)","rgb(75, 192, 192)","rgb(54, 162, 235)",
                    "rgb(153, 102, 255)","rgb(201, 203, 207)"
                 ],
                 "borderWidth":1
            }]
        },
        "options":{
            "scales":{
                "yAxes":[{
                    "ticks":{
                        "beginAtZero":true
                    }
                 }]
            }
        }
    });

    getGraphData("");
    addTransactions("");
    setupNewsfeed();
    addNews("None");
}
