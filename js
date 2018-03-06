function createFolderLink() {
    var sel = document.getElementById("department");
    var department = sel.options[sel.selectedIndex].value;
    var folder1 =  document.getElementById("folder1").value ;
    var folder2 = document.getElementById("folder2").value ;
    var folder3 = document.getElementById("folder3").value ;
    var folder4 =  document.getElementById("folder4").value ;
    var fileLocation = "";
    if(department!= "Department"){
    	fileLocation = fileLocation + department;
    	if(!isEmptyValue(folder1)){
    		fileLocation = fileLocation + "\\" + folder1;
    	}
    	if(!isEmptyValue(folder2)){
    		fileLocation = fileLocation + "\\" + folder2;
    	}
    	if(!isEmptyValue(folder3)){
    		fileLocation = fileLocation + "\\" + folder3;
    	}
    	if(!isEmptyValue(folder4)){
    		fileLocation = fileLocation + "\\" + folder4;
    	}
    }
    document.getElementById("reviewFolderLocation").value = fileLocation;
}

function isEmptyValue(value){
	if(value == null || value == "" || value ==" "){
		return true;
	}
	return false;
}

var filesToUpload = [];

$(document).ready(function (e) {
	displayToolTipMessage();
	
	$("#department").change(function() {
		   var val = $(this).text();
		   if (val != "Department") {
		      $("#folderLocation").show();
		   }
		});
	
	if($("#department").val()){
		var value = $(this).text();
		   if (value != "Department") {
		      $("#folderLocation").show();
		   }
	}	
	associateEvents();

});

function associateEvents(){
	
	$("#fileUploadForm").on('submit',function (event) {
		event.preventDefault ? event.preventDefault() : (event.returnValue = false);
		var formParams = new FormData();
		formParams.append("toAddresses", $("#toAddresses").val());
		formParams.append("ccAddresses", $("#ccAddresses").val());
		formParams.append("uploadNotificationAddresses", $("#uploadNotificationAddresses").val());
		formParams.append("linkValidityInDays", $("#linkValidityInDays").val());
		formParams.append("subject", $("#subject").val());
		formParams.append("body", $("#body").val());
		formParams.append("department", $("#department").val());
		formParams.append("folders[0]", $("#folder1").val());
		formParams.append("folders[1]", $("#folder2").val());
		formParams.append("folders[2]", $("#folder3").val());
		formParams.append("folders[3]", $("#folder4").val());
		formParams.append("folderLocation", $("#reviewFolderLocation").val());
		for (var i = 0; i < filesToUpload.length; i++) {
    		formParams.append("files", filesToUpload[i]);
  		}
		$.ajax ({
			type: "POST",
			enctype: 'multipart/form-data',
		    url: "/request-document/review",
		    data: formParams,
			processData: false,
			contentType: false,
		    timeout: 600000,
		    success: function (data) {
		    	$("#fileUploadForm").html( data );
		    },
		    complete:function(){
		        $('body, html').animate({scrollTop:$('#fileUploadForm').offset().top}, 'slow');
	        }
		});
	});
	
$("#fileBrowser").on('click',function (event) {
	$(this).val(null);
});

$("#fileBrowser").on('change',function (event) {
	var files = $(this)[0].files;
	for (var i = 0; i < files.length; i++) {
		filesToUpload.push(files[i]);
		}
	
		updateFilesSelected();
});

}
function updateFilesSelected() {

		if (filesToUpload != null) {

		var filesSelectedHtml = "<p></p><p class='filesSelected'><b>Files selected</b></p><ul>";
		
		for (var i = 0; i < filesToUpload.length; i++) {
			filesSelectedHtml = filesSelectedHtml + "<li class='fileName'>" + "<span>" + formatFileName(filesToUpload[i].name) + "</span>"  + "  "  + "<span>"  +
			formatBytes(filesToUpload[i].size)  + "</span>"  + "<span class='remove'><img src='/request-document/resources/images/gray-round-close-x.svg'/></span></li>"; 
			}
	
		var filesSelectedHtml = filesSelectedHtml + "</ul>";
	
		$("#filesSelected").html(filesSelectedHtml);
		bindRemoveEvent();
		
	} else {
		$("#filesSelected").html('');
	} 
}

function formatFileName(fileName) {
	var nameSuffix = '...';
	var numberOfCharactersFromFileNameToBeDisplayed = 15;
	
	var expectedNameLength = numberOfCharactersFromFileNameToBeDisplayed + nameSuffix.length;  
	
	if (fileName.length <= numberOfCharactersFromFileNameToBeDisplayed) {
		return fileName;
	}
	
	return fileName.substr(0, numberOfCharactersFromFileNameToBeDisplayed) + nameSuffix;
}

function bindRemoveEvent() {
	$("li.fileName > span.remove").bind('click',function (event) {
		
		var fileIndexToRemove = $(this).parent().index();
		filesToUpload.splice(fileIndexToRemove, 1);
		updateFilesSelected();
	});
}

function formatBytes(a, b) {
	if (0 == a) {
		return "0 Bytes";
	}
		
	var c = 1024;
	var d = b || 2;
	var e = ["Bytes", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB"];
	var f = Math.floor(Math.log(a) / Math.log(c));
	
	return Math.ceil(parseFloat((a / Math.pow(c, f)).toFixed(d))) + " " + e[f];
}

function blockSpecialChar(e) {
	
    var k = e.which || e.keyCode || e.charCode;
    return ((k > 96 && k < 123) || (k > 64 && k < 91) || (k > 47 && k < 58) || k == 32 || k == 45 || k == 95 || k == 9 || k == 8 || k == 46); //alpha numeric, spaces, dashes, underscores, tab, backspace, delete
}

function displayToolTipMessage(){
    $('[data-toggle="tooltip"]').tooltip();
}






$(".spinner").hide();
removeSpinner();

function submitForm() {
    $("#confirmForm").submit();
    launchSpinner();    
}

function launchSpinner() {
    $(".spinner").css({
        "display": "block",
        "pointer-events": "none"
    });
    $("#spinnerSection").addClass("modalOverlay");
    $("body").css({
        "pointer-events": "none"
    });
}

function removeSpinner() {
    $(".spinner").css("display", "none");
    $("#spinnerSection").removeClass("modalOverlay");
    $("body").css({
        "pointer-events": "auto"
    });
}


