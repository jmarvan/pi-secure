var webSocket;
var loginToken = '';
var timer;
var timeout = function() {
	
	showLogin();
}
var setTimer = function() {
	timer = setInterval(timeout,300000);
}
var resetTimer = function() {
	window.clearTimeout(timer);
	setTimer();
}
var showLogin = function() {
	loginToken = null;
	$("#ipin").val('');
	$("#controls").addClass('hidden');
	$("#login").removeClass('hidden');
	$("#controlPanel").addClass('hidden');
}
var showControls = function() {
	$("#login").addClass('hidden');
	$("#controls").removeClass('hidden');
}
var showEvents = function() {	
	getEvents('eventTable');
	$("#controlPanel").addClass('hidden');
	$("#events").removeClass('hidden');
}
var refreshState = function(message) {	
	var data = JSON.parse(message.data);
	//TODO retrieve translation as well.
	$("#ipin").attr('class', data.state);
	$("#currentState").html(data.state);
	$("#currentState").attr('class', "status "+data.state);
}
var setState = function(state) {
	$.ajax({										
		url: "state?token="+loginToken,
		type: "POST",
		data: {"token" : loginToken, "value" : state},
		success: function (data) {			
			showLogin();			
		},
		error: function (jqXHR) {
			displayError(jqXHR);								
		}
	});	
}
var login = function() {
	resetTimer();
	var pin = $("#ipin").val();
	$("#ipin").val('');
	
	$.ajax({
		url: "login",
		type: "POST",
		data: pin,
		success: function (data) {
			loginToken = data;
			showControls();			
		},
		error: function (jqXHR) {
			displayError(jqXHR);							
		}
	});
}
var getEvents = function(id, offset) {
 
	$.ajax({
		url: "event",
		type: "GET",
		data: {"offset": offset ? offset : 0},
		success: function (data) {
			var template = document.getElementById(id);
			cloneNode(template, null, JSON.parse(data));			
		},
		error: function (jqXHR) {
			displayError(jqXHR);							
		}
	});
	
}
var compileTemplate = function(template) {
	var exp = /(\@\{[A-Za-z0-9 _.,!"'/$]+\})/g;
	var replExp = /(\@\{)([A-Za-z0-9 _.,!"'/$]+)(\})/g;
	var parts = template.split(exp);

	for (var i=0 ; i<parts.length; i++) {
		if (parts[i].match(exp)) {
			var val = parts[i].replace(replExp, "$2");
			parts[i] = new Object();
			parts[i].value = val;
		}
	}
	return parts;
}
var applyValues = function(template, data) {
	var result = ""
	template.forEach(function(part){
		if (typeof part == "string") {
			result += part;
		} else {
			result += data[part.value];
		}
	});

	return result;
}
var appendClone = function(copy, parent, data) {
	if (copy.removeAttribute) {
		if (copy.getAttribute("id") != null) {
			copy.setAttribute("id", copy.getAttribute("id")+"-clone")
		}
		copy.className = !copy.className ? copy.className : copy.className.replace(/^(.*)(template)(.*)$/, "$1$3");
		for (var i=0; i<copy.attributes.length; i++) {
			var newValue = applyValues(compileTemplate(copy.attributes[i].value), data);
			copy.setAttribute(copy.attributes[i].name, newValue);
		}
	}
	parent.appendChild(copy);
}
var cloneNode = function(node, parent, data) {
	if (parent == null) {		
		parent = node.parentNode;
		var clone = document.getElementById(node.getAttribute("id")+"-clone");
		if (clone) {
			parent.removeChild(clone);
		}
	}	
	
	if (node.nodeType == 3 || node.nodeType == 2) {
		var value = applyValues(compileTemplate(node.nodeValue), data);
		var copy = node.cloneNode(false);
		copy.nodeValue = value;
		appendClone(copy, parent, data);
	} else if (node.nodeType == 1) {
		if (node.getAttribute('data-template') != null)	 {
			var template = compileTemplate(node.innerHTML);
			var dataSource = node.getAttribute("data-datasource");
			for (var i=0; i<data[dataSource].length; i++) {
				var copy = node.cloneNode(false);
				copy.innerHTML = applyValues(template, data[dataSource][i]);
				appendClone(copy, parent, data.data[i]);
			}
		} else {
			var copy = node.cloneNode(false);
			appendClone(copy, parent, data);
			for (var i=0;i<node.childNodes.length;i++){
				cloneNode(node.childNodes[i], copy, data);
			}
		}		
	}
}
var displayError = function(jqXHR) {
	alert("Server responds with http"+jqXHR.status+".\nClick OK to reload the page, if problem persists, you are screwed.");
	location.reload();
}
$(document).ready(function() {
	setTimer();
	$("#benter").click(function() {					
		login();										
	});
	$("#bclear").click(function() {
		resetTimer();
		$("#ipin").val('');
	});
	$("#barm, #bdisarm").click(function() {
		resetTimer();
		setState($(this).attr("data-state"));
	});
	$(".digit").click(function() {
		resetTimer();
		$("#ipin").val($("#ipin").val()+$(this).val());
	});
	$("#ipin").keypress(function(e) {
	    if(e.which == 13) {				    	
	        login();
	        return false;
	    }
	});
	webSocket = new WebSocket("ws://" + location.hostname + ":" + location.port + "/state/");
	webSocket.onmessage = function (msg) { refreshState(msg); };
	webSocket.onclose = function () { location.reload(); };
	window.scrollTo(0,1);
	getEvents('eventTable');
});	