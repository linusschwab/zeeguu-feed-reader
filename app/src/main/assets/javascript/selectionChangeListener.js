document.addEventListener("selectionchange", function() {
    Android.updateTranslation(getExtendedSelection());
    //Android.updateText(getExtendedSelection());
}, false);