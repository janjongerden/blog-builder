// ==UserScript==
// @name        nu-div-remover
// @namespace   janjongerden.nl
// @description Removes the div that complains about adblockers
// @include     http://www.nu.nl/*
// @version     1
// @grant       none
// @noframes
// ==/UserScript==

document.getElementsByClassName('adblocker')[0].remove();
