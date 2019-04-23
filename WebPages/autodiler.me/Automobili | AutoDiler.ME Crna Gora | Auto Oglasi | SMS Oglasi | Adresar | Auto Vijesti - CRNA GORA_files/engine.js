function $$(o){return(document.getElementById(o))}

function $S(o,s){if (o.currentStyle)return o.currentStyle[s];else if (window.getComputedStyle)return document.defaultView.getComputedStyle(o,null).getPropertyValue(s)}

function $A(o,a){return(o.getAttribute(a))}

function addEvt(o,e,f){if(o.addEventListener)o.addEventListener(e,f,true);else o.attachEvent("on"+e, f);}

function removeEvt(o,e,f){if(o.detachEvent)o.detachEvent('on'+e,f);else o.removeEventListener(e,f,false);}

function remove(o){o.parentNode.removeChild(o)}



function replaceHtml(el,html){

var oldEl=typeof el==="string"?$$(el):el;
var newEl=oldEl.cloneNode(false);

newEl.innerHTML=html;

oldEl.parentNode.replaceChild(newEl,oldEl);

return newEl;

}

function isArray(o){return o.constructor == Array};

//Function.prototype.bind = function (scope, args) {
//
//args = args || [];
//
//scope = scope || window;
//
//var fn = this;
//
//return function(){return fn.apply(scope, args);};
//
//};

function isset(){

var a = arguments, l = a.length, i = 0, undef;

if (l === 0) throw new Error('Empty isset');

while (i !== l) {

if (a[i] === undef || a[i] === null) {

return false;

}i++;}

return true;

}

function SetOpacity(elem, opacityAsInt){

var opacityAsDecimal = opacityAsInt;

if (opacityAsInt > 100) opacityAsInt = opacityAsDecimal = 100;

else if (opacityAsInt < 0) opacityAsInt = opacityAsDecimal = 0;

opacityAsDecimal /= 100;

if (opacityAsInt < 1) opacityAsInt = 1;

elem.style.opacity = opacityAsDecimal;

elem.style.filter = "alpha(opacity=" + opacityAsInt + ")";

}

function SetOpacity(elem, opacityAsInt){

var opacityAsDecimal = opacityAsInt;

if (opacityAsInt > 100) opacityAsInt = opacityAsDecimal = 100;

else if (opacityAsInt < 0) opacityAsInt = opacityAsDecimal = 0;

opacityAsDecimal /= 100;

if (opacityAsInt < 1) opacityAsInt = 1;

elem.style.opacity = opacityAsDecimal;

elem.style.filter  = "alpha(opacity=" + opacityAsInt + ")";

}

function FadeOpacity(e, fromOpacity, toOpacity, time, fps){

var steps = Math.ceil(fps * (time / 1000));

var delta = (toOpacity - fromOpacity) / steps;

FadeOpacityStep(e, 0, steps, fromOpacity, delta, (time / steps));

}

function FadeOpacityStep(e, stepNum, steps, fromOpacity, delta, timePerStep){

SetOpacity($$(e), Math.round(parseInt(fromOpacity) + (delta * stepNum)));

if (stepNum < steps) setTimeout("FadeOpacityStep('" + e + "', " + (stepNum+1) + ", " + steps + ", " + fromOpacity + ", " + delta + ", " + timePerStep + ");", timePerStep);

}

function fadein(e){

$$(e).style.display="block";

if($$(e).style.opacity<1)FadeOpacity(e, 0, 100, 250, 10);

}

function fadeout(e){

$$(e).style.display="none";

SetOpacity($$(e), 0);


}

function toggle(id){

	var o = $$(id);

	if(o.style.display!='none')fadeout(o.id);else fadein(o.id);

}

function izaberi_tab(o) {

var s = o.parentNode.getElementsByTagName('a');

var id = $A(o,'rel');

for (i=0;i<s.length;i++){

var r = $A(s[i],'rel');

var c = s[i].className

c = c.replace("active","");

s[i].className=(r==id)?c+' active':c;

if($$(r))$$(r).style.display='none';

}

if($$(id))$$(id).style.display='block';

return false;

}



function izaberi_novost(o) {

var s = o.parentNode.getElementsByTagName('div');

for (i=0;i<s.length;i++){

var c = s[i].className;

c = c.replace("active","");

s[i].className=(s[i]==o)?c+' active':c;

}

}



function bookmark(){

	if(document.all){

		window.external.AddFavorite(location.href,document.title);

	}else if(window.sidebar) {

		window.sidebar.addPanel(document.title,location.href,'');

	}else alert(txt_bookmark);

}



function imgUploaded(){

	if (validate_form(document.submitform)) document.submitform.submit();

}



function izaberi_autoplac(o){

	var value = o.options[o.selectedIndex].value;

	if (value != 'NULL') {

		$$('prodavac').disabled=true;

		$$('prodavac').value='';

		$$('grad').disabled=true;

		$$('grad').value='';

		$$('telefon').disabled=true;

		$$('telefon').value='';

		$$('email').disabled=true;

		$$('email').value='';

		x_update_autoplac(value,update_fields);

	} else {

		$$('prodavac').disabled=false;

		$$('grad').disabled=false;

		$$('telefon').disabled=false;

		$$('email').disabled=false;

		$$('autoplac_grad').value='';

		$$('autoplac_tel').value='';

		$$('autoplac_email').value='';

	}

}



function update_fields(r){

	for (i in r) {

		$$(i).value=r[i];

	}

}



function validate_form(o){

	var tags = new Array('input','select');
       
	for (var t=0;t<tags.length;t++) {

		var s = o.getElementsByTagName(tags[t]);
                 
		for (i=0;i<s.length;i++){
                    
			if ($A(s[i],'required')!=null){

				if (s[i].value == '') {

					s[i].className+=' invalid';

					if ($A(s[i],'title')) alert($A(s[i],'title'));

					return false;

				} else s[i].className+=' valid';

			}
                        
                            if(s[i].name == 'cena' && $A(s[i],'rel')!= 'rez') {
                                 if(s[i].value.length < 3) {
                                        alert('Potrebno je upisati cijenu vozila - najmanje 3 cifre');
                                        return false;
                                    }
                            }
                        
		}

	}

	return true;

}



function compare(id){

	if (!id) id = 0;

	if (Cookie.test()) {

		var ids = [];

		if (id >= 0){

			if (Cookie.get('cmp')) {

				ids = Cookie.get('cmp').split(",");

			}



			var ima = -1;

			for(var i=0;i<ids.length;i++)if(ids[i]==id)ima=i;



			if (ima>=0){

				ids.splice(ima,1);

			} else if(id>0) ids.push(id);



			if (ids.length > 6) {

				alert('Možete porediti najviše 6 oglasa.');

				return false;

			}

		}



		if (ids.length > 0){

			Cookie.set('cmp',ids.join(','));

			$$('compare').innerHTML=txt_compare_num.replace('%s',ids.length)+' <a href="#/" class="btn" onclick="compare(-1)" title="'+txt_compare_clear+'" style="float:none;padding:3px 6px">&times;</a>'+((ids.length>1)?' <a href="/auto_cmp" class="submit_btn2">'+txt_compare+'</a>':'');

		} else {

			var chk = document.getElementsByTagName('input');

			for (var i = 0; i < chk.length; i++){

			if (chk[i].type === 'checkbox' && $A(chk[i],'rel') == 'cmp') chk[i].checked = false;

			}

			Cookie.unset('cmp');

			$$('compare').innerHTML = txt_empty_list;

		}

		$$('compare').style.display = 'block';

	}

}

function initSelectBoxCustomization(element) {
    $(element).multiselect({
        multiple: false,
        header: false,
        minWidth: 'auto',
        selectedList: 1,
        open : multiselect_fit_widget,
        close : multiselect_free_keyboard
    });
}

function multiselect_fit_widget(event) {
    var select = jQuery(event.target);
    multiselect_keyboard(select);
} 

function multiselect_keyboard(select){
    window.option_index = {};
    jQuery('option', select).each(function() {
    var first_letter= jQuery(this).text().slice(0, 1).toUpperCase();
    var position = jQuery(this).index() +'';
    if (window.option_index.hasOwnProperty(first_letter))
    {
        window.option_index[first_letter].push( position );
    }
    else
    {
        window.option_index[first_letter] = [position];
    }
    });

    jQuery(document).on('keydown', function(event) {
        var pressed = String.fromCharCode(event.which);
        var is_letter = /^[a-zA-Z]|[^\u0000-\u0080]$/;
        if (is_letter.test(pressed))
        {
            if (window.option_index.hasOwnProperty(pressed))
            {
                var options = option_index[pressed];
                if (options.length > 1)
                {
                    multiselect_focus(select, options[0]);
                    options.push(options[0]);
                    window.option_index[pressed] = options.slice(1);
                }
                else
                {
                    multiselect_focus(select, options[0]);
                }
            }
        }
    });
} 

function multiselect_focus(select, position){
var labels = select.multiselect('widget').find('label');
labels.eq(position).trigger('mouseover').trigger('mouseenter').find('input').trigger('focus');
} 

function multiselect_free_keyboard(){
jQuery(document).off('keydown');
}
