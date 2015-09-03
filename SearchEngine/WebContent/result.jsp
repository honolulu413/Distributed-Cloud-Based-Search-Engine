<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>Mini Google Engine</title>
<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<%
	String jsonResult = (String)request.getAttribute("googleResult");
	String searchTerm = (String)request.getAttribute("term");
	String originalString = (String)request.getAttribute("original");
	String correctString = (String)request.getAttribute("corrected");
	String time = (String)request.getAttribute("time");
	String numOfFile = (String)request.getAttribute("numFiles");
	//String time ="0.4sec";
	//String numOfFile ="2300";
	//out.println(jsonResult);
%>
</head>
<link rel="stylesheet" type="text/css" href="css/starbuzz.css" />
<link rel="stylesheet" type="text/css" href="css/style.css" />
<link rel="stylesheet" type="text/css" href="css/main.css" />
<link rel="stylesheet" type="text/css" href="css/welcome.css" />
<link rel="stylesheet" type="text/css" href="css/form.css" />
<script src="js/jquery-2.1.3.min.js" type="text/javascript" ></script>
<script src="js/jquery-1.3.2.js" type="text/javascript" ></script>
<script src=js/jquery.paginate.js type="text/javascript"></script>
<script>
function validateForm(){
	var x = document.forms["form"]["searchTerm"].value;
    if (x == null||x=="") {
        return false;
    }
}


$(document).ready(function() {

	var queryTerm = "<%=searchTerm%>";
	var result =<%=jsonResult%>;
	
	var original = "<%=originalString%>";
	var correct = "<%=correctString%>";
	var time = "<%=time%>";
	var numFile = "<%=numOfFile%>";
	original = original.split(" ");
	correct = correct.split(" ");
	var recommend = 0;
	for(var i=0;i<original.length;i++){
		if(original[i]!=correct[i]){
			recommend =1;
			break;
		}
	}
	
	var recDiv = document.getElementById("recommend");
	var numOfFileDiv = document.createElement("p");
	numOfFileDiv.innerHTML = "About: "+ numFile + " results";
	recDiv.appendChild(numOfFileDiv);
	
	var timeDiv = document.createElement("p");
	timeDiv.innerHTML = "Search time: " + time;
	recDiv.appendChild(timeDiv);
	
	
	if(recommend!=0){
		console.log("changed");
		var recDiv = document.getElementById("recommend");
		var corDiv = document.createElement("p");
		var newRecStr = "Did you mean:  ";
		for(var j=0;j<correct.length;j++){
			newRecStr = newRecStr + correct[j] + " ";
		}
		corDiv.innerHTML = newRecStr;
		recDiv.appendChild(corDiv);
	}

	
	$("#mainpage").paginate({
				count 		: 6,
				start 		: 1,
				display     : 6,
				border					: true,
				border_color			: '#fff',
				text_color  			: '#fff',
				background_color    	: 'black',	
				border_hover_color		: '#ccc',
				text_hover_color  		: '#000',
				background_hover_color	: '#fff', 
				images					: false,
				mouse					: 'press',
				onChange     			: function(page){
											$('._current','#mainbar').removeClass('_current').hide();
											$('._current','#sidebar').removeClass('_current').hide();
											$('#mainpage'+page).addClass('_current').show();
											$('#sidepage'+page).addClass('_current').show();
										 }
	});
	
	var y = 1;
	var idx = 0;
	var len = result.length;
	while(idx < len){
		var mainDiv = document.getElementById("mainpage"+y);
		/*
		for(var k=0;k<2;k++){
			var brdiv = document.createElement("br");
			mainDiv.appendChild(brdiv);
		}*/
		
		var titleDiv = document.createElement("h1");
		titleDiv.className = "titleClass";
		titleDiv.style.fontSize = "170%";
		var titleref = document.createElement("a");
		titleref.innerHTML = result[idx].name;
		titleref.href = result[idx].url;	
		titleDiv.appendChild(titleref);
			
		var urldiv = document.createElement("h2");
		urldiv.className = "urlClass";
		urldiv.style.fontSize = "120%";
		var urlStr = result[idx].url;
		var urlLength = urlStr.length;
		if(urlLength > 90){
			urlStr = urlStr.substring(0,87);
			urlStr = urlStr + "...";
		}
		urldiv.innerHTML = urlStr;
			
        mainDiv.appendChild(titleDiv);
        var brspace = document.createElement("br");
        mainDiv.appendChild(brspace);
        mainDiv.appendChild(urldiv);
        	
        var brdiv1 = document.createElement("br");
        var brdiv2 = document.createElement("br");
		mainDiv.appendChild(brdiv1);
		mainDiv.appendChild(brdiv2);
			
		idx++;
    	if(idx/10 > y){
    		y++;
    	}
	}


		        	
	$.ajax({ 
	    type: 'POST', 
	    url: 'YelpServlet', 
	    data: { term: queryTerm }, 
	    dataType: 'json',
	    success: function (data) { 
	    	var i = 0;
	    	var j = 1;
	        $.each(data, function(index, element) {
	        	var name = element.name;
	        	var url = element.url;
	        	var text = element.snippet_text;
	        	var imgurl = element.snippet_url;
	        	var addr = element.display_address;

	        	var sidebarDiv = document.getElementById("sidepage"+j);		        	
	        	var sideDiv = document.createElement("div");
	        	sidebarDiv.appendChild(sideDiv);
	        	sideDiv.className="side";
	        	sideDiv.id="side"+i;
	        	var nameref = document.createElement("a");
	        	nameref.innerHTML = name;
	        	nameref.href = url;
	        	
	        	var pname = document.createElement("p");
	        	pname.appendChild(nameref);
	        	
	      	    var purl = document.createElement("p");
	        	purl.innerHTML = url;
	        	var ptext = document.createElement("p");
	        	ptext.innerHTML = text;
	        			        	
	        	var pimg = document.createElement("img");
	        	pimg.src = imgurl;
	        	var psnippet_img = document.createElement("p");
	        	psnippet_img.appendChild(pimg);
	        			        	
	        	sideDiv.appendChild(pname);
	        	sideDiv.appendChild(purl);
	        	sideDiv.appendChild(ptext);
	        	sideDiv.appendChild(pimg);
	        	i++;
	        	if(i/5 > j){
	        		j++;
	        	}
	        });
	    },
	    error: function (result) {
	    	console.log("fail\n");
            alert("Error");
        }
	});			
})		

</script>

<body>
	<div id="header">
    	<form name="form" action="/SearchEngine/SearchServlet" method="GET" onsubmit="return validateForm()" class="form-wrapper1">
    		<input type="text" name="searchTerm" id="search"/>
    		<input type="submit" value="Search" id="submit"/> <br/>
    		<input type="radio" name="searchtype" value="word" checked/> word
    		<input type="radio" name="searchtype" value="image"/> image
		</form>
    </div>
    
    <div id="mainbar">
    	<div id="recommend"><h2 style="font-size:140%">Search Result</h2></div>
  		<div id="mainpage1" class="mainpage _current" style=""></div>	
  		<div id="mainpage2" class="mainpage" style="display:none;"></div>	
  		<div id="mainpage3" class="mainpage" style="display:none;"></div>	
  		<div id="mainpage4" class="mainpage" style="display:none;"></div>	
  		<div id="mainpage5" class="mainpage" style="display:none;"></div>	
  		<div id="mainpage6" class="mainpage" style="display:none;"></div>	
  		<div id="mainpage"></div>
    </div>
    
    <div id="sidebar" class="sidebar">
        <div id="sidepage1" class="pagedemo _current" style=""><h3>Yelp Web Service</h3></div>
		<div id="sidepage2" class="pagedemo" style="display:none;"><h3>Yelp Web Service Result</h3></div>
		<div id="sidepage3" class="pagedemo" style="display:none;"><h3>Yelp Web Service Result</h3></div>
		<div id="sidepage4" class="pagedemo" style="display:none;"><h3>Yelp Web Service Result</h3></div>
		<div id="sidepage5" class="pagedemo" style="display:none;"><h3>Yelp Web Service Result</h3></div>
		<div id="sidepage6" class="pagedemo" style="display:none;"><h3>Yelp Web Service Result</h3></div>
		<div id="sidepage"></div>
    </div>
</body>
</html>