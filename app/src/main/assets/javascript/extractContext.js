/*
This returns the first sentence in the paragraph that
matches the required term. This is not perfect, but
it is probably happening very rarely, and even when
it happens, for the user, the context is still
interesting.
 */
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
    var title = document.getElementsByTagName("title")[0].innerHTML;

    return {
        "term": term,
        "context": context,
        "title": title,
        "url": document.URL
    };
}