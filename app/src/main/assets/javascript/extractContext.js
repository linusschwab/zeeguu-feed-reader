function extractContext () {

    var selection = window.getSelection();
    var term = selection.toString();
    var surrounding_paragraph = $(selection.baseNode.parentNode).text();

    // debug information
    console.log(surrounding_paragraph);
    console.log(term);

    var context = "Error";
    try {
        var sentenceRegEx = /\(?[^\.!\?]+[\.!\?]\)?/g;
        context = $.trim(surrounding_paragraph.match(sentenceRegEx).filter(
            function (eachSentence) {
            return eachSentence.indexOf(term) >= 0;
        })[0])
    } catch (e) {
    }
    return context;
}