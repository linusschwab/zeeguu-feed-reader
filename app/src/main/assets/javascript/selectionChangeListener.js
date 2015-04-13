document.addEventListener("selectionchange", function() {
    Android.updateTranslation(window.getSelection().toString());
}, false);