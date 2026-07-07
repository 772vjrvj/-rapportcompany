$(function () {
    const $window = $(window);
    const $document = $(document);
    const $header = $('#siteHeader');
    const $navToggle = $('#navToggle');
    const $headerNav = $('#headerNav');
    const $pagerDots = $('.pager-dot');
    const $sections = $('.page-section');
    const $quickContact = $('#quickContact');
    const $revealItems = $('.reveal-up');

    function getHeaderHeight() {
        return $header.outerHeight() || 0;
    }

    function updateHeader() {
        if ($window.scrollTop() > 8) {
            $header.addClass('scrolled');
        } else {
            $header.removeClass('scrolled');
        }
    }

    function openMenu() {
        $headerNav.addClass('open');
        $navToggle.addClass('open').attr('aria-expanded', 'true');
    }

    function closeMenu() {
        $headerNav.removeClass('open');
        $navToggle.removeClass('open').attr('aria-expanded', 'false');
    }

    function moveToSection(target) {
        const $target = $(target);

        if ($target.length === 0) {
            return;
        }

        $('html, body').stop().animate({
            scrollTop: $target.offset().top - getHeaderHeight() + 1
        }, 650);
    }

    function updatePager() {
        let currentId = '';
        const scrollTop = $window.scrollTop();
        const checkPoint = scrollTop + getHeaderHeight() + 130;

        $sections.each(function () {
            const $section = $(this);
            const sectionTop = $section.offset().top;
            const sectionBottom = sectionTop + $section.outerHeight();

            if (checkPoint >= sectionTop && checkPoint < sectionBottom) {
                currentId = '#' + $section.attr('id');
            }
        });

        if (currentId === '' && scrollTop < 50) {
            const firstSectionId = $sections.first().attr('id');
            currentId = firstSectionId ? '#' + firstSectionId : '#hero';
        }

        $pagerDots.removeClass('active');
        $pagerDots.filter('[data-target="' + currentId + '"]').addClass('active');
    }

    function revealOnScroll() {
        const windowBottom = $window.scrollTop() + $window.height();

        $revealItems.each(function () {
            const $item = $(this);
            const itemTop = $item.offset().top;

            if (windowBottom > itemTop + 60) {
                $item.addClass('is-visible');
            }
        });
    }

    $navToggle.on('click', function () {
        if ($headerNav.hasClass('open')) {
            closeMenu();
        } else {
            openMenu();
        }
    });

    $document.on('click', function (e) {
        const isMenu = $headerNav.is(e.target) || $headerNav.has(e.target).length > 0;
        const isToggle = $navToggle.is(e.target) || $navToggle.has(e.target).length > 0;

        if (!isMenu && !isToggle) {
            closeMenu();
        }
    });

    $('.header-nav .nav-link').on('click', function () {
        closeMenu();
    });

    $pagerDots.on('click', function () {
        const target = $(this).data('target');
        moveToSection(target);
    });

    $quickContact.on('click', function () {
        moveToSection('#contact');
    });

    $('a[href^="#"]').on('click', function (e) {
        const target = $(this).attr('href');

        if (target.length > 1 && $(target).length > 0) {
            e.preventDefault();
            moveToSection(target);
            closeMenu();
        }
    });

    $window.on('scroll', function () {
        updateHeader();
        updatePager();
        revealOnScroll();
    });

    $window.on('resize', function () {
        if ($window.width() > 900) {
            closeMenu();
        }
        updatePager();
        revealOnScroll();
    });

    updateHeader();
    updatePager();
    revealOnScroll();
});


/* ========================================================================== 
   RAPPORT SUB PAGES FINAL PACKAGE
   ========================================================================== */
$(function () {
    const $window = $(window);
    const $tabs = $('.sticky-tabs a');
    const $targets = $tabs.map(function () {
        const id = $(this).attr('href');
        return id && id.indexOf('#') === 0 ? $(id)[0] : null;
    });

    function getOffset() {
        return ($('#siteHeader').outerHeight() || 0) + ($('.sticky-tabs').outerHeight() || 0);
    }

    function moveToAnchor(target) {
        const $target = $(target);
        if ($target.length === 0) {
            return;
        }
        $('html, body').stop().animate({
            scrollTop: $target.offset().top - getOffset() + 2
        }, 650);
    }

    function updateStickyTabs() {
        if ($tabs.length === 0) {
            return;
        }
        let currentId = '';
        const checkPoint = $window.scrollTop() + getOffset() + 80;
        $($targets).each(function () {
            const $section = $(this);
            const top = $section.offset().top;
            const bottom = top + $section.outerHeight();
            if (checkPoint >= top && checkPoint < bottom) {
                currentId = '#' + $section.attr('id');
            }
        });
        if (currentId) {
            $tabs.removeClass('active');
            $tabs.filter('[href="' + currentId + '"]').addClass('active');
        }
    }

    $('.sticky-tabs a, .sub-btn[href^="#"]').on('click', function (e) {
        const target = $(this).attr('href');
        if (target && target.indexOf('#') === 0 && $(target).length > 0) {
            e.preventDefault();
            moveToAnchor(target);
        }
    });

    $window.on('scroll resize', updateStickyTabs);
    updateStickyTabs();
});


/* ========================================================================== 
   EMAILJS CONTACT FORM
   EmailJS 가입 후 아래 3개 값만 채우면 contact.html 문의폼이 동작합니다.
   - EMAILJS_PUBLIC_KEY
   - EMAILJS_SERVICE_ID
   - EMAILJS_TEMPLATE_ID
   ========================================================================== */
$(function () {
    const EMAILJS_PUBLIC_KEY = '';
    const EMAILJS_SERVICE_ID = '';
    const EMAILJS_TEMPLATE_ID = '';
    const $contactForm = $('#contactForm');

    if ($contactForm.length === 0) {
        return;
    }

    if (typeof emailjs !== 'undefined' && EMAILJS_PUBLIC_KEY !== '') {
        emailjs.init({
            publicKey: EMAILJS_PUBLIC_KEY
        });
    }

    $contactForm.on('submit', function (e) {
        e.preventDefault();

        const $form = $(this);
        const $button = $form.find('button[type="submit"]');
        const originalHtml = $button.html();

        if (!this.checkValidity()) {
            this.reportValidity();
            return;
        }

        if (EMAILJS_PUBLIC_KEY === '' || EMAILJS_SERVICE_ID === '' || EMAILJS_TEMPLATE_ID === '') {
            alert('EmailJS 키 설정이 필요합니다. main.js의 EMAILJS_PUBLIC_KEY, EMAILJS_SERVICE_ID, EMAILJS_TEMPLATE_ID 값을 입력해주세요.');
            return;
        }

        if (typeof emailjs === 'undefined') {
            alert('메일 발송 모듈을 불러오지 못했습니다. 잠시 후 다시 시도해주세요.');
            return;
        }

        $button.prop('disabled', true).text('전송 중...');

        emailjs.sendForm(EMAILJS_SERVICE_ID, EMAILJS_TEMPLATE_ID, this)
            .then(function () {
                alert('문의가 정상적으로 접수되었습니다. 확인 후 연락드리겠습니다.');
                $form[0].reset();
            })
            .catch(function (error) {
                console.error('EmailJS Error:', error);
                alert('메일 전송 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
            })
            .finally(function () {
                $button.prop('disabled', false).html(originalHtml);
            });
    });
});
