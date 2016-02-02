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
}
var showControls = function() {
	$("#login").addClass('hidden');
	$("#controls").removeClass('hidden');
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
});	