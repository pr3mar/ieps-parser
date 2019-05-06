var gallery = {};

$(function() {
    
    if($('.smallGallery').length > 0) {
        gallery.smallGallery = $('.smallGallery').adGallery({
            width: '300',
            height: '230',
            update_window_hash: false,
            loader_image: '/images/redesign/galleryLoader.gif',
            enable_keyboard_move: false,
            slideshow: {
                enable: false
            },
            callbacks: {
                init: true,
                afterImageVisible: function() {
                    gallery.currentImage = this.current_index;
                }
            }
        });
        $(".smallGallery").on("click", ".ad-image", function() {
            showGalleryInPopup();
        });
    }
    $(document).keyup(function(e) {
        if (e.keyCode == 27) {
            if($('#galleryBigHolder').length>0) {
                $('#galleryBigHolder').hide();
                $('.overlay').hide();
            }
        }   // esc
    });

    initGalleryHashChange();
    
//    if($('.kalkulatorRegistracije').length > 0) {
//        initSelectBoxCustomization('.kalkulatorSelect');
//    }
});

function showGalleryInPopup() {
    window.location.hash = 'galleryPopup';
    gallery.popupGallery = [];
    if($('#galleryBigHolder').length === 0) {
        $('body').append('<div id="galleryBigHolder"><div class="closeBtn"></div>'+$('#galleryBigDataHolder').html()+'</div>');
        $('#galleryBigDataHolder').empty();
        $('.overlay').show();
    }
    else {
        $('#galleryBigHolder').show();
        $('.overlay').show();
    }
    gallery.popupGallery = $('.galleryBig').adGallery({
        width: '600',
        height: '480',
        update_window_hash: false, 
        loader_image: '/images/redesign/galleryLoader.gif',
        start_at_index: gallery.currentImage,
        slideshow: {
            enable: false
        }
    });
    $('#galleryBigHolder').css({
        top: parseInt(($(window).height()-$('#galleryBigHolder').outerHeight())/2),
        left: parseInt(($(window).width()-$('#galleryBigHolder').outerWidth())/2)
    });
    $(window).resize(function() {
       $('#galleryBigHolder').css({
           top: parseInt(($(window).height()-$('#galleryBigHolder').outerHeight())/2),
           left: parseInt(($(window).width()-$('#galleryBigHolder').outerWidth())/2)
       }); 
    });
    $( "#galleryBigHolder" ).draggable();
    $('#galleryBigHolder .closeBtn, div.overlay').click(function() {
        $('#galleryBigHolder').hide();
        $('.overlay').hide();
        window.location.hash = '';
    });
}

function initGalleryHashChange() {
    $(window).bind('hashchange', function() {
        var hash = window.location.hash.replace(/\#/, '');
        if(hash == '' && $('#galleryBigHolder').length > 0) {
            $('#galleryBigHolder').hide();
            $('.overlay').hide();
        }
//        else if (hash == 'galleryPopup') {
//            showGalleryInPopup();
//        }
    });
}
