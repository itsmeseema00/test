// var host="http://www.mapquestapi.com"
var host = window.location.protocol + "//" + window.location.hostname + "/mapquestapi"
alert(host);
var key = "Gmjtd%7Clu6y2lurn9%2C80%3Do5-lwagl";
var branches = new Array();
var distance = 0;
var totalMiles = 0;
var mileageRate = 0.535;

function Main() {
	document.getElementById("outputText").value = "Loading data...";
	$("#txtRateReimburse").val(mileageRate);
	$(".displayRate").text(mileageRate);

	var url = getURL();
	$.get(url, null, getMQData, "json");
}

function getURL() {
	var table = "34283_locations";
	var filter = "";
	//var filter = "lobby1!= ''";		//use for extra criteria to filter by
	var fields = "RecordId,N,Lat,Lng";
	var maxMatches = "500";
	
	return host + "/search/v2/recordinfo?key=" + key + "&hostedData=mqap." + table + "|"
		+ filter + "||" + fields + "&maxMatches=" + maxMatches;
}

function getMQData(data) {
	var branchStrings = new Array();

	if (data == null || data.searchResults == null || data.searchResults.length <= 0) {
		document.getElementById("outputText").value += "\n" + "Could not load data from MapQuest.";
		return;
	}

	// for each result in data
	for (i = 0; i < data.searchResults.length; i++) {
		var id = data.searchResults[i].fields.RecordId;

		// if result is a branch or administrative building
		if (id.startsWith("BR") || id.startsWith("ADMIN")) {
			var name = formatBranchName(data.searchResults[i].fields.N);
			var lat = data.searchResults[i].fields.Lat;
			var lng = data.searchResults[i].fields.Lng;

			var branchString = name + ";" + lat + ";" + lng;

			branchStrings.push(branchString);
		}
	}

	setBranchDataAndElements(branchStrings);
}
if (typeof String.prototype.startsWith != 'function') {
	String.prototype.startsWith = function (str) {
		if (str && str.length <= this.length) {
			return this.indexOf(str) == 0;
		}
		return false;
	};
}

function formatBranchName(name) {
	return name.replace("RBFCU - ", "").replace(/\s+\(.*\)/g, "");
}

function setBranchDataAndElements(branchStrings) {
	branchStrings.sort();

	for (i = 0; i < branchStrings.length; i++) {
		var data = branchStrings[i].split(";");
		branches.push(new branch(data[0], data[1], data[2]));
	}

	setOptionElements();
}

function setOptionElements() {
	var start = document.getElementById("startBranch");
	var next = document.getElementById("nextBranch");
	var out = document.getElementById("outputText");

	for (i = 0; i < branches.length; i++) {
		start[start.length] = new Option(branches[i].name, i);
		next[next.length] = new Option(branches[i].name, i);
	}

	start.disabled = false;
	out.value = "";
}

function branch(branchName, branchLat, branchLng) {
	this.name = branchName;
	this.lat = branchLat;
	this.lng = branchLng;
}

function doInitialize() {
	var first = parseInt(document.getElementById("startBranch").value);
	
	if(first != -1) {
		document.getElementById("startBranch").disabled = true;
		document.getElementById("nextBranch").disabled = false;	
		document.getElementById("outputText").value = ("Starting from " + branches[first].name + ".\n");
	}

	return;
}

function doGetMileage() {
	var first = parseInt(document.getElementById("startBranch").value);
	var second = parseInt(document.getElementById("nextBranch").value);

	if(second != -1) {
		getDirections(first, second);
	}
}

function getDirections(from, to) {
	var fromLoc = branches[from].lat + "," + branches[from].lng;
	var toLoc = branches[to].lat + "," + branches[to].lng;
	var url = host + "/directions/v2/route?key=" + key + "&from=" + fromLoc + "&to=" + toLoc;

	$.post(url, null, function(data) { getMQDistance(data, from, to) }, "json");
}

function getMQDistance(data, from, to) {
	distance = data.route.distance;
	distance = Math.ceil(distance);

	updateElements(from, to);
}

function updateElements(first, second) {
	document.getElementById("outputText").value += branches[first].name + " to " + branches[second].name + ":\t" + distance + " miles.\n";

	document.getElementById("startBranch").value = document.getElementById("nextBranch").value;
	document.getElementById("nextBranch").value = -1;	

	totalMiles += distance;
	document.getElementById("txtTotalMiles").value = totalMiles;

	var totalReimburse = totalMiles * mileageRate;
	
	document.getElementById("txtTotalReimburse").value = formatCurrency(totalReimburse); 		
}

function formatCurrency(strValue) {
	strValue = strValue.toString().replace(/\$|\,/g,'');
	var dblValue = parseFloat(strValue);

	var blnSign = (dblValue == (dblValue = Math.abs(dblValue)));
	dblValue = Math.floor(dblValue * 100 + mileageRate);
	var intCents = dblValue % 100;
	var strCents = intCents.toString();
	dblValue = Math.floor(dblValue / 100).toString();

	if(intCents < 10) {
		strCents = "0" + strCents;
	}

	for (var i = 0; i < Math.floor((dblValue.length-(1+i))/3); i++) {
		dblValue = dblValue.substring(0,dblValue.length - (4 * i + 3)) + ',' +
			dblValue.substring(dblValue.length-(4*i+3));
	}

	if (blnSign) {
		return ('' + '$' + dblValue + '.' + strCents);
	} else {
		return ('-' + '$' + dblValue + '.' + strCents);
	}
}

function doClear() {
	// if (branches.length <= 0) {
	// 	Main();

	// 	return;
	// }

	document.getElementById("startBranch").value = -1;
	document.getElementById("nextBranch").value = -1;

	document.getElementById("outputText").value = "";

	document.getElementById("txtTotalMiles").value = "0";
	document.getElementById("txtTotalReimburse").value = "0";

	document.getElementById("startBranch").disabled = false;
	document.getElementById("nextBranch").disabled = true;

	totalMiles = 0;
}
