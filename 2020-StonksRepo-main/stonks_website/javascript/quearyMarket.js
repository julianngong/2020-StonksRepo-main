function sleep(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

//#####################################################################################//
//-- Constants to use in the program
const b_recent = 'recent';
const b_long = 'long';
const b_lifetime = 'lifetime';
const timeBetweenRequests = 1200;
const getCompaniesUrl = "http://localhost:8080/get_companies";
const getGraphValues = "http://localhost:8080/get_history_of_stonks?withValues=";

//#####################################################################################//
//Variables to keep track of the chart

var clickedCompanies = [];
//The chart to draw stuff to
var chart = null;

var chartBehaviour = b_recent
var nextGraphName = "Stock price - recent behaviour";

//Returns a colour object of the style the graph library likes
var makeColour = function(r, g, b) {
    return {
        red: r,
        green: g,
        blue: b,
        asGraphColour: function() {
            let thing = "rgb(" + parseInt(this.red) + "," + parseInt(this.blue) + "," + parseInt(this.green) + ")";
            console.log(thing);
            return thing;
        },
        darken: function() {
            let r = this.red;
            let g = this.green;
            let b = this.blue;
            if (this.red > 20) {
                r -= 20;
            }
            if (this.green > 20) {
                g -= 20;
            }
            if (this.blue > 20) {
                b -= 20;
            }
            return makeColour(r, g, b);
        }
    }
}

//Colours for use on the graph
var graphColours = [
    makeColour(180, 10, 10), makeColour(10, 180, 10), makeColour(10, 10, 180), makeColour(180, 180, 10), makeColour(180, 10, 180), makeColour(249, 93, 106), makeColour(255, 166, 0)
];

var setChartBehaviour = function(behaviour) {
    chartBehaviour = behaviour;
    //Alter the next graph name
    nextGraphName = "Stock price - " + chartBehaviour + " behaviour";
}

var sendQueary = function(Url, responseFunc) {
    sleep(timeBetweenRequests).then(() => {
        let HttpRequest = new XMLHttpRequest();
        HttpRequest.open("GET", Url);
        HttpRequest.onreadystatechange = (e) => {
            if (HttpRequest.readyState == 4 && HttpRequest.status == 200)
                responseFunc(HttpRequest.responseText);
        }
        HttpRequest.send();
        console.log("Sent a request to URL" + Url);
    });
}

var displayAllCompanies = function(response) {

    document.getElementById("stonks").innerHTML = response;
    for (let i = 0; i < clickedCompanies.length; i++) {
        let rowNum = clickedCompanies[i];
        try {
            let button = document.getElementById("button" + parseInt(rowNum));
            button.onclick = function() {
                removeClicked(rowNum);
            }
        } catch (err) {
            //This company must not exist anymore, remove it from the list
            clickedCompanies.splice(i, i + 1);
            i -= 1;
        }
    }

    let URL = getGraphValues;
    //add the URLs the user has requested
    if (clickedCompanies.length > 0) {
        for (let i = 0; i < clickedCompanies.length - 1; i++) {
            URL = URL.concat(clickedCompanies[i].toString() + ",");
        }
        URL = URL.concat(clickedCompanies[clickedCompanies.length - 1].toString());
    } else {
        URL = URL.concat("null");
    }

    URL = URL.concat("&requestType=" + chartBehaviour);

    sendQueary(URL, runUpdateGraph);
}

var runUpdateGraph = function(response) {
    updateGraph(response);
    document.getElementById("graphTitle").innerHTML = nextGraphName;

    sendQueary(getCompaniesUrl, displayAllCompanies);
}

var main = function() {
    runUpdateGraph("");
    f();
}

//Called from the elements of the company table that the user wants to be added
var addClicked = function(rowNum) {
    let button = document.getElementById("button" + parseInt(rowNum));
    button.onclick = function() {
        removeClicked(rowNum);
    }
    clickedCompanies.push(rowNum);
    console.log("A");
}

//Called from the elements of the company table that the user wants to remove
var removeClicked = function(rowNum) {
    let button = document.getElementById("button" + parseInt(rowNum));
    button.onclick = function() {
        addClicked(rowNum);
    }
    for (let i = 0; i < clickedCompanies.length; i++) {
        if (clickedCompanies[i] === rowNum) {
            clickedCompanies.splice(i, i + 1);
            break;
        }
    }
    console.log("Gura reference");
}

var f = function() {
    chart = new Chart(
        document.getElementById("companyChart"), {
            "type": "line",
            /*"data":{
                "labels":["January","February","March","April","May","June","July"],
                "datasets":[{
                        "label":"My First Dataset",
                        "data":[65,59,80,81,56,55,40],
                        "fill":false,
                        "borderColor":"rgb(75, 192, 192)",
                        "lineTension":0.1
                    }]
                },*/
            "options": {}
        }
    );
}

var parseToFloats = function(strings) {
    let floats = []
    for (let i = 0; i < strings.length; i++) {
        floats.push(parseFloat(strings[i]));
    }
    return floats;
}

var getDataSet = function(inString, stringNum, companyName) {
    let toret = {
        "label": companyName,
        "data": parseToFloats(inString.split(" ")),
        "fill": false,
        "borderColor": graphColours[stringNum].asGraphColour(),
        "lineTension": 0.1,
        "pointRadius": 0
    }
    return toret;
}

var getTimeings = function(dataset) {
    let ret = [];
    for (let i = 0; i < dataset.length; i++) {
        let res = dataset[i].split("&");
        ret.push(res[1]);
    }
    return ret;
}

var updateGraph = function(vals) {
    if (vals.length <= 1) {
        return;
    }
    let datasets = vals.split("#");
    let firstDataset = datasets[0].split(" ");
    let sizeOfDatasets = parseToFloats(firstDataset).length;

    primeTimestamps = getTimeings(firstDataset);

    chart.data.labels = []
    chart.data.datasets = []
    for (let i = 0; i < sizeOfDatasets; i++) {
        if (i < primeTimestamps.length) {
            chart.data.labels.push(primeTimestamps[i]);
        } else {
            chart.data.labels.push("NaN");
        }
    }

    //- 1 because it ends with a '#' so there will always be a trailing empty string
    //Which knowing javascript may cause it to have a siezure
    for (let i = 0; i < datasets.length - 1; i += 2) {
        if (graphColours.length < i / 2) {
            graphColours.push(graphColours[(i / 2) % graphColours.length].darken());
        }
        chart.data.datasets.push(getDataSet(datasets[i], i / 2, datasets[i + 1]));
    }

    chart.update(0);
}