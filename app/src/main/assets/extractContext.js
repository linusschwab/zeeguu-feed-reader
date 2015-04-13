var selection = window.getSelection();
var term = selection.toString();

var surroundingParagraph = $(selection.baseNode.parentNode).text();
extract_context(surroundingParagraph, term);

function extract_context (surrounding_paragraph, term) {
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