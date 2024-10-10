// ==UserScript==
// @name         Mastodon Image Liberator
// @namespace    janjongerden.nl
// @version      1.0
// @description  Hide the buttons on top of images
// @author       Jan Jongerden
// @include      https://mas.to/*
// @grant        none
// ==/UserScript==

const cleanButtons = () => {
    const hideButtons = document.querySelectorAll('.media-gallery__actions__pill');

    hideButtons.forEach(element => {
        element.style.display = 'none';
    });

    const altButtons = document.querySelectorAll('.media-gallery__item__badges');

    altButtons.forEach(element => {
        element.style.bottom = '-9px';
    });
};

var repeater = setInterval(cleanButtons, 1000);
