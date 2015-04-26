function getExtendedSelection() {
    var selection = getSelectionPosition()

    var min = selection.selectionStart
    var max = selection.selectionEnd
    var text = selection.text
    var inLoop

    // Select complete word if partially selected (to the left side)
    if (min != 0 && min != text.length) {
        var current = text.substring(min, min + 1);
        var next = text.substring(min - 1, min);

        inLoop = false;
        while (min != 0
                && !(current == " " || next == " ")) {
            next = text.substring(min - 1, min);
            min -= 1;
            inLoop = true;
        }
        if (inLoop && min != 0)
            min += 1;
    }

    // Select complete word if partially selected (to the right side)
    if (max != 0 && max != text.length) {
        var current = text.substring(max - 1, max);
        var next = text.substring(max, max + 1);

        inLoop = false;
        while (max != text.length
                && !(current == " " || next == " ")
                && !(current == "." || next == ".")
                && !(current == "," || next == ",")) {
            next = text.substring(max, max + 1);
            max += 1;
            inLoop = true;
        }
        if (inLoop && max != text.length)
            max -=1;
    }

    var selectedWords = text.substring(min,max);

    // Debug information
    //console.log(selectedWords);

    return selectedWords;
}

function getSelectionPosition() {
    var selection = window.getSelection();
    var selectionLength = selection.toString().length;

    /*
      The start and end offset need to be calculated separately because they might be in
      different nodes (both are needed because the selection can be from left to right or
      from right to left).

      anchor and base offset are for the beginning of the selection,
      extent and focus offset are for the end of the selection.

      http://www.w3schools.com/jsref/dom_obj_all.asp
    */
    var start = selectionOffset(selection.anchorNode, selection.anchorOffset);
    var end = selectionOffset(selection.extentNode, selection.extentOffset);

    var selectionStart = start.offset;
    var selectionEnd = end.offset
    var node = start.node;

    // Switch start and end if the selection is from right to left
    if (start.offset > end.offset) {
        selectionStart = end.offset;
        selectionEnd = start.offset;
    }

    // Debug information
    //console.log(node.textContent.substring(selectionStart,selectionEnd));

    return {
        "selection": selection,
        "selectionStart": selectionStart,
        "selectionEnd": selectionEnd,
        "text": node.textContent
    };
}

function selectionOffset(node, offset) {
    var nodePosition;

    while (node.parentNode.parentNode != null) {
        if (node.parentNode.childNodes.length > 1) {
            // Find current node in the parent node
            for (i = 0; i < node.parentNode.childNodes.length; i++) {
              if (node.isSameNode(node.parentNode.childNodes[i]))
                nodePosition = i;
                }
            // Calculate position of current node in parent node
            for (i = 0; i < nodePosition; i++) {
              var childNode = node.parentNode.childNodes[i]
              offset += childNode.textContent.length;
            }
        }
        node = node.parentNode;
    }

    return {
        "node": node,
        "offset": offset
    };
}