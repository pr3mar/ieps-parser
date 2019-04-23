var loadingImage = '/images/loading.gif';
var closeButton = '/images/close.gif';
function getPageScroll(){
	var yScroll;
	if (self.pageYOffset){
		yScroll = self.pageYOffset;
	} else if (document.documentElement && document.documentElement.scrollTop){
		yScroll = document.documentElement.scrollTop;
	} else if (document.body) {
		yScroll = document.body.scrollTop;
	}
	arrayPageScroll = new Array('',yScroll);
	return arrayPageScroll;
}
function getPageSize(){
	var xScroll, yScroll;
	if (window.innerHeight && window.scrollMaxY) {
		xScroll = document.body.scrollWidth;
		yScroll = window.innerHeight + window.scrollMaxY;
	} else if (document.body.scrollHeight > document.body.offsetHeight){
		xScroll = document.body.scrollWidth;
		yScroll = document.body.scrollHeight;
	} else {
		xScroll = document.body.offsetWidth;
		yScroll = document.body.offsetHeight;
	}
	var windowWidth, windowHeight;
	if (self.innerHeight) {
		windowWidth = self.innerWidth;
		windowHeight = self.innerHeight;
	} else if (document.documentElement && document.documentElement.clientHeight) {
		windowWidth = document.documentElement.clientWidth;
		windowHeight = document.documentElement.clientHeight;
	} else if (document.body) {
		windowWidth = document.body.clientWidth;
		windowHeight = document.body.clientHeight;
	}
	if(yScroll < windowHeight){
		pageHeight = windowHeight;
	} else {
		pageHeight = yScroll;
	}
	if(xScroll < windowWidth){
		pageWidth = windowWidth;
	} else {
		pageWidth = xScroll;
	}
	arrayPageSize = new Array(pageWidth,pageHeight,windowWidth,windowHeight)
	return arrayPageSize;
}
function pause(numberMillis){
	var now = new Date();
	var exitTime = now.getTime() + numberMillis;
	while (true) {
		now = new Date();
		if (now.getTime() > exitTime)
			return;
	}
}
function showLightbox(objLink){
	var objOverlay = document.getElementById('overlay');
	var objLightbox = document.getElementById('lightbox');
	var objCaption = document.getElementById('lightboxCaption');
	var objThumbs = document.getElementById('lightboxThumbs');
	var objTxt = document.getElementById('lightboxTxt');
	var objImage = document.getElementById('lightboxImage');
	var objLoadingImage = document.getElementById('loadingImage');
	var objLightboxDetails = document.getElementById('lightboxDetails');
	var arrayPageSize = getPageSize();
	var arrayPageScroll = getPageScroll();
	if (objLoadingImage) {
		objLoadingImage.style.top = (arrayPageScroll[1] + ((arrayPageSize[3] - 35 - objLoadingImage.height) / 2) + 'px');
		objLoadingImage.style.left = (((arrayPageSize[0] - 20 - objLoadingImage.width) / 2) + 'px');
		objLoadingImage.style.display = 'block';
	}
	objOverlay.style.height = (arrayPageSize[1] + 'px');
	objOverlay.style.display = 'block';
	if (objLink.href.indexOf("#")>=0){
		eval('x_'+objLink.href.substring(objLink.href.indexOf("#")+1,objLink.href.length-1)+',showAjaxbox)');
	} else {
		imgPreload = new Image();
		imgPreload.onload=function(){
			if(objLink.getAttribute('title')){
				objCaption.style.display = 'block';
				objCaption.innerHTML = objLink.getAttribute('title');
			} else {
				objCaption.style.display = 'none';
			}
			objTxt.style.display = 'none';
			LightboxSetImage(objLink.href);
			if(objLink.getAttribute('max')){
				max = objLink.getAttribute('max');
				auto_id = objLink.getAttribute('auto_id');
				t = '';
				for (i = 0; i != max; i++){
					t += '<a href="#" onclick="return LightboxSetImage(\'/images/oglasi/'+auto_id+'/'+i+'.jpg\')"><img height="67" width="90" alt="thumb" src="/images/oglasi/'+auto_id+'/.tb/'+i+'_90x67.jpg"></a><br/>';
				}
				objThumbs.innerHTML = t;
				objThumbs.style.display = 'inline-block';
			} else if (objLink.getAttribute('data-list')){
				list = eval("(" + objLink.getAttribute('data-list')+")");
				var t = '';
				for (var i in list) {
					var sl = list[i].split('/').splice(-1,1);
					var tb = '.tb/'+sl[0].replace('.jpg','_90x67.jpg');
					t += '<a href="#" onclick="return LightboxSetImage(\'/'+list[i]+'\')"><img height="67" width="90" alt="thumb" src="/'+list[i].replace(sl,tb)+'"></a><br/>';
				}
				objThumbs.innerHTML = t;
				objThumbs.style.display = 'inline-block';
			} else {
				objThumbs.style.display = 'none';
			}
			if (navigator.appVersion.indexOf("MSIE")!=-1){
				pause(250);
			}
			if (objLoadingImage){objLoadingImage.style.display='none'}
			selects = document.getElementsByTagName("select");
			for (i = 0; i != selects.length; i++) {
				selects[i].style.visibility = "hidden";
			}
			objLightbox.style.display = 'block';
			arrayPageSize = getPageSize();
			objOverlay.style.height = (arrayPageSize[1] + 'px');
			return false;
		}
		imgPreload.src = objLink.href;
	}
	function showAjaxbox(r){
		objImage.style.display = 'none';
		objThumbs.style.display = 'none';
		objTxt.style.display = 'block';
		objTxt.innerHTML = r;
		if(objLink.getAttribute('title')){
			objCaption.style.display = 'block';
			objCaption.innerHTML = objLink.getAttribute('title');
		} else {
			objCaption.style.display = 'none';
		}
		if (objLoadingImage){objLoadingImage.style.display='none'}
		objLightbox.style.display = 'block';
	}
}
function hideLightbox(){
	objOverlay = document.getElementById('overlay');
	objLightbox = document.getElementById('lightbox');
	objOverlay.style.display = 'none';
	objLightbox.style.display = 'none';
	selects = document.getElementsByTagName("select");
	for (i = 0; i != selects.length; i++) {
		selects[i].style.visibility = "visible";
	}
}
function initLightbox(){
	if (!document.getElementsByTagName){ return; }
	var anchors = document.getElementsByTagName("a");
	for (var i=0; i<anchors.length; i++){
		var anchor = anchors[i];
		if (anchor.getAttribute("href") && (anchor.getAttribute("rel") == "lightbox")){
			anchor.onclick = function () {showLightbox(this); return false;}
		}
	}
	var objBody = document.getElementsByTagName("body").item(0);
	var objOverlay = document.createElement("div");
	objOverlay.setAttribute('id','overlay');
	objOverlay.onclick = function () {hideLightbox(); return false;}
	objOverlay.style.display = 'none';
	objOverlay.style.position = 'absolute';
	objOverlay.style.top = '0';
	objOverlay.style.left = '0';
	objOverlay.style.zIndex = '90';
 	objOverlay.style.width = '100%';
	objBody.insertBefore(objOverlay, objBody.firstChild);
	var arrayPageSize = getPageSize();
	var arrayPageScroll = getPageScroll();
	var imgPreloader = new Image();
	imgPreloader.onload=function(){
		var objLoadingImageLink = document.createElement("a");
		objLoadingImageLink.setAttribute('href','#');
		objLoadingImageLink.onclick = function () {hideLightbox(); return false;}
		objOverlay.appendChild(objLoadingImageLink);
		var objLoadingImage = document.createElement("img");
		objLoadingImage.src = loadingImage;
		objLoadingImage.setAttribute('id','loadingImage');
		objLoadingImage.style.position = 'absolute';
		objLoadingImage.style.zIndex = '150';
		objLoadingImageLink.appendChild(objLoadingImage);
		imgPreloader.onload=function(){};
		return false;
	}
	imgPreloader.src = loadingImage;
	var objLightbox = document.createElement("div");
	objLightbox.setAttribute('id','lightbox');
	objLightbox.style.display = 'none';
	objLightbox.style.zIndex = '100';
	objBody.insertBefore(objLightbox, objOverlay.nextSibling);
	var objLightboxDetails = document.createElement("div");
	objLightboxDetails.setAttribute('id','lightboxDetails');
	objLightbox.appendChild(objLightboxDetails);
	var objCaption = document.createElement("div");
	objCaption.setAttribute('id','lightboxCaption');
	objCaption.style.display = 'none';
	objLightboxDetails.appendChild(objCaption);
	var objThumbs = document.createElement("span");
	objThumbs.setAttribute('id','lightboxThumbs');
	objThumbs.style.display = 'none';
	objLightbox.appendChild(objThumbs);
	var objTxt = document.createElement("div");
	objTxt.setAttribute('id','lightboxTxt');
	objTxt.style.display = 'none';
	objLightbox.appendChild(objTxt);
	var objLink = document.createElement("a");
	objLink.setAttribute('href','#');
	objLink.onclick = function () {hideLightbox(); return false;}
	objLightbox.appendChild(objLink);
	var imgPreloadCloseButton = new Image();
	imgPreloadCloseButton.onload=function(){
		var objCloseButton = document.createElement("img");
		objCloseButton.src = closeButton;
		objCloseButton.setAttribute('id','closeButton');
		objCloseButton.style.position = 'absolute';
		objCloseButton.style.zIndex = '200';
		objLink.appendChild(objCloseButton);
		return false;
	}
	imgPreloadCloseButton.src = closeButton;
	var objImage = document.createElement("div");
	objImage.setAttribute('id','lightboxImage');
	objImage.oncontextmenu = function(){return false}
	objLightbox.appendChild(objImage);
}
function LightboxSetImage(img){
	$('#lightboxImage').css('background-image', 'url('+img+')');
	return false;
}
function addLoadEvent(func){
	var oldonload = window.onload;
	if (typeof window.onload != 'function'){
		window.onload = func;
	} else {
		window.onload = function(){
		oldonload();
		func();
		}
	}
}
addLoadEvent(initLightbox);