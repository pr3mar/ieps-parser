var currentPos = 0;
var elementsHeight = 0;
var elementMargins = 3;
var activeMainFormTab = 'searchHolderWrapper';
var activeAddTabForm = 'dodaj-automobili';
var definedClickEvents = false;

$(function() {
   initMainFormTabs();
   initSearchForm();
   initAddFormTabs();
//   $('a[form="automobili"]').click();
   setActiveSearchFormTab();
});

function initSearchForm() {
    var searchForm = $('.searchHolder');
    //initialization of form tabs controls (up and down)
    $('.typeWrapper ul').attr('current-position', currentPos);
    $('.upArrow').css({
        opacity: 0.5,
        cursor: 'default'
    });
    searchForm.find('.searchVehicleTypeHolder ul li a').each(function() {
       elementsHeight = elementsHeight + ($(this).outerHeight()+elementMargins);
    });

    if(elementsHeight > 180) {
        $('.searchTabsNavigation a').click(function() {
            var direction = $(this).attr('direction');
            var elementHeight = searchForm.find('.searchVehicleTypeHolder ul li a').outerHeight()+elementMargins;
            if(direction == 'up' && currentPos < 0)
            {
                currentPos = currentPos+elementHeight;
                $('.typeWrapper ul').animate({
                        marginTop: currentPos+'px'
                });
                if(currentPos == 0) {
                    $('.upArrow').css({
                    opacity: 0.5,
                    cursor: 'default'
                });
                }
                $('.downArrow').css({
                    opacity: 1,
                    cursor: 'pointer'
                 });
            }
            else if(direction == 'down' && currentPos>=-elementHeight*4) {
                $('.upArrow').css({
                    opacity: 1,
                    cursor: 'pointer'
                });
                currentPos = currentPos-elementHeight;
                $('.typeWrapper ul').animate({
                   marginTop: currentPos
                });
                if(currentPos==-elementHeight*5) {
                    $('.downArrow').css({
                       opacity: 0.5,
                       cursor: 'default'
                    });
                }
            }
            $('.typeWrapper ul').attr('current-position', currentPos);
            return false;
        });
    }
    $('.searchMenuItems a').click(function() {
       $('.searchMenuItems').removeClass('active');
       $('.searchMenuItems a').removeClass('activeTab');
       $(this).addClass('activeTab');
       $(this).parent().addClass('active');
       var pointer = $(this).find('.pointer');
       $(this).find('.pointer').css({
           background: 'url(/images/redesign/ajax-loader-white.gif) no-repeat 0px 8px'
       });
       $.ajax({
           type: 'POST',
           url: '/form/?formName='+$(this).attr('form'),
           dataType: 'HTML',
           success: function(retForm) {
               $('.searchVehicleForm').html(retForm);
                pointer.css({
                    background: 'url("/images/redesign/iconsSprite.png") no-repeat scroll 0 0 transparent'
                });
                $('.typeWrapper').height(175);
                $('.searchTabsNavigation').show();
           }
       });
       if(!definedClickEvents) {
            $(document).on("click", 'a.advancedForm', function(e) {
            e.preventDefault();
            if($(this).hasClass('basicForm')) return;
             if($(this).attr('form') !='' || $(this).attr('form') != undefined) {
//                 $('.searchVehicleForm').empty();
                 $('.searchVehicleForm').html('<div class="ajaxFormLoader"></div>');
                 $.ajax({
                     type: 'POST',
                     url: '/form/?formName='+$(this).attr('form'),
                     dataType: 'HTML',
                     success: function(retForm) {
                         $('.searchVehicleForm').html(retForm);
                         if($(this).hasClass('basicForm')) {
                            $('.typeWrapper').height(175);
                            $('.searchTabsNavigation').show();
                         }
                         else {
                            $('.typeWrapper').height('auto');
                            $('.typeWrapper ul').css({marginTop: 0});
                            $('.searchTabsNavigation').hide();
                         }
                     }
                     });
             }
         });
         $(document).on("click", "a.basicForm", function(e) {
            e.preventDefault();
            $('.searchVehicleForm').html('<div class="ajaxFormLoader"></div>');
            $.ajax({
                type: 'POST',
                url: '/form/?formName='+$(this).attr('form'),
                dataType: 'HTML',
                success: function(retForm) {
//                    console.log('test');
                    $('.searchVehicleForm').html(retForm);
                    $('.typeWrapper').height(175);
                    $('.searchTabsNavigation').show();
                    $('.typeWrapper ul').css({marginTop: $('.typeWrapper ul').attr('current-position')+'px'});
                }
            });
        });
       }
    definedClickEvents = true;
       return false;
    });
}

function getFormByAjax(formName) {
   if(formName!='') {
       $.ajax({
           type: 'POST',
           url: '/form/?formName='+formName,
           dataType: 'HTML',
           success: function(retForm) {
               $('.searchVehicleForm').html(retForm);
           }
       });
   }
}

function setActiveSearchFormTab() {
   if ($(".searchHolder")[0]) {

    var activeTabStr = document.location.pathname.replace('/', '');
    activeTabArr = activeTabStr.split('/',1);
    activeTab = activeTabArr[0];

   activeTab = activeTab || 'automobili';
   var activeTabForm = 'automobili';

   switch(activeTab) {
       case 'auto_oglasi_auto':
           activeTabForm = 'automobili';
           activeAddTabForm = 'dodaj-automobili';
           break;
       case 'auto_oglasi_moto':
           activeTabForm = 'motori';
           activeAddTabForm = 'dodaj-motori';
           break;
       case 'auto_oglasi_kamion':
           activeTabForm = 'kamioni';
           activeAddTabForm = 'dodaj-kamioni';
           break;
       case 'auto_oglasi_kombi':
           activeTabForm = 'kombi';
           activeAddTabForm = 'dodaj-kombi';
           break;
       case 'auto_oglasi_autobusi':
           activeTabForm = 'autobusi';
           activeAddTabForm = 'dodaj-autobusi';
           break;
       case 'auto_oglasi_deo':
           activeTabForm = 'rezervni-delovi';
           activeAddTabForm = 'dodaj-rezervni-delovi';
           break;
       case 'auto_oglasi_brod':
           activeTabForm = 'plovila';
           activeAddTabForm = 'dodaj-plovila';
           break;
       case 'auto_oglasi_bicikli':
           activeTabForm = 'bicikli';
           activeAddTabForm = 'dodaj-bicikli';
           break;
       case 'auto_oglasi_masine':
           activeTabForm = 'gradjevinske-masine';
           activeAddTabForm = 'dodaj-gradjevinske-masine';
           break;
   }
   scrollToActiveTabItem(activeTabForm);
   $('a[form="'+activeTabForm+'"]').click();
//   scrollToActiveTabItem(activeAddTabForm);
//   $('a[form="'+activeAddTabForm+'"]').click();
   }
}

function scrollToActiveTabItem(activeTabForm) {
    currentPos = -$('a[form="'+activeTabForm+'"]').position().top+5;
    var elementHeight = $('.searchHolder').find('.searchVehicleTypeHolder ul li.searchMenuItems a').outerHeight()+elementMargins;
    var numOfTabItems = $('.searchHolder').find('.searchVehicleTypeHolder ul li.searchMenuItems a').length;
    var maxPos = -((elementHeight*numOfTabItems)-(elementHeight*4));
    if(currentPos < 0) {
        $('.upArrow').css({
            opacity: 1,
            cursor: 'pointer'
        });
    }
    if(currentPos<=maxPos) {
        currentPos = maxPos;
        $('.downArrow').css({
            opacity: 0.5,
            cursor: 'default'
        });
    }
    $('a[form="'+activeTabForm+'"]').parent().parent().animate({
        marginTop: currentPos
    }, 1000).attr('current-position', currentPos);
}

function initMainFormTabs() {
    $('.searchHolder .navigation ul li a').click(function(e) {
        $('.searchHolder .navigation ul li a').removeClass('active').addClass('inactiveTab');
        $(this).addClass('active').removeClass('inactiveTab');
        if($(this).attr('rel') === 'addFormHolderWrapper') {
            $('.typeWrapper2').height('auto');
            $('.searchTabsNavigation').hide();
            if($(this).attr('loaded') === undefined) {
                $('a[form="'+activeAddTabForm+'"]').click();
            }
            $(this).attr('loaded', true);
        }
        else {
            $('.typeWrapper').height(175);
            $('.searchTabsNavigation').show();
        }
        $('.'+activeMainFormTab).hide();
        $('.'+$(this).attr('rel')).show();
        activeMainFormTab = $(this).attr('rel');
        e.preventDefault();
    });
}

function initAddFormTabs() {
    $('.addMenuItems  a').click(function() {
       $('.addMenuItems ').removeClass('active');
       $('.addMenuItems a').removeClass('activeTab');
       $(this).addClass('activeTab');
       $(this).parent().addClass('active');
       var pointer = $(this).find('.pointer');
       $(this).find('.pointer').css({
           background: 'url(/images/redesign/ajax-loader-white.gif) no-repeat 0px 8px'
       });
       $.ajax({
           type: 'POST',
           url: '/form/?formName='+$(this).attr('form'),
           dataType: 'HTML',
           success: function(retForm) {
               $('.addVehicleForm').html(retForm);
                pointer.css({
                    background: 'url("/images/redesign/iconsSprite.png") no-repeat scroll 0 0 transparent'
                });
//                $('.typeWrapper').height(175);
//                $('.searchTabsNavigation').show();
           }
       });
       return false;
    });
}