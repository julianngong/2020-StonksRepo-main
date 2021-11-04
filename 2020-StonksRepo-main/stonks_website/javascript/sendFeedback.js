/* SmtpJS.com - v3.0.0 */
var Email = { send: function (a) { return new Promise(function (n, e) { a.nocache = Math.floor(1e6 * Math.random() + 1), a.Action = "Send"; var t = JSON.stringify(a); Email.ajaxPost("https://smtpjs.com/v3/smtpjs.aspx?", t, function (e) { n(e) }) }) }, ajaxPost: function (e, n, t) { var a = Email.createCORSRequest("POST", e); a.setRequestHeader("Content-type", "application/x-www-form-urlencoded"), a.onload = function () { var e = a.responseText; null != t && t(e) }, a.send(n) }, ajax: function (e, n) { var t = Email.createCORSRequest("GET", e); t.onload = function () { var e = t.responseText; null != n && n(e) }, t.send() }, createCORSRequest: function (e, n) { var t = new XMLHttpRequest; return "withCredentials" in t ? t.open(e, n, !0) : "undefined" != typeof XDomainRequest ? (t = new XDomainRequest).open(e, n) : t = null, t } };

// ^ free libary to send emails, tutorial at https://netcorecloud.com/tutorials/how-to-send-emails-with-javascript/

function gatherAllDataAndSendEmail(){
    let experience = document.getElementById("experience").value;
    let u18 = document.getElementById("0-18").checked;
    let v1825 = document.getElementById("18-25").checked;
    let v2545 = document.getElementById("25-45").checked;
    let v45moar = document.getElementById("45+").checked;
    let likedDarkMode = document.getElementById("yes").checked;

    let likedFeature = document.getElementById("feature").value;
    let recommendation = document.getElementById("recommendation").value;

    let additionalInfo = document.getElementById("extra").value;
    
    let age = "Unknown";
    if(u18){
        age = "under 18";
    }
    else if(v1825){
        age = "18-25";
    }
    else if(v2545){
        age = "25-45";
    }
    else if(v45moar){
        age = "45+";
    }

    let mainBody = "Experience: " + experience + "\nAge range: " + age;
    mainBody += "\nLiked dark mode: " + (likedDarkMode ? "yes" : "no");
    mainBody+= "\nMost liked feature: " + likedFeature;
    mainBody += "\nChance of recommending: " + recommendation;
    mainBody += "\nAdditional user comments:\n" + additionalInfo;
    
    sendEmail(mainBody);
}

function redirect(){
    let x = document.getElementById("javascriptTool");
    x.innerHTML = '<a href = "submission.html", id = "sendLink"></a>';
    document.getElementById("sendLink").click();
}

function sendEmail(emailBody) {
	Email.send({
	Host: "smtp.gmail.com",
	Username : "uob.stonks.team@gmail.com",
	Password : "UOBSPEstonks2021",
	To : 'uob.stonks.team@gmail.com',
	From : "uob.stonks.team@gmail.com",
	Subject : "Feedback",
	Body : emailBody,
	}).then(
	    message => redirect()
	);
}