var isDropped = false;
function dragStart(event) {
    event.dataTransfer.setData("Text", event.target.id);
    event.dataTransfer.effectAllowed = 'move';
    var src = event.dataTransfer.getData("Text");
    var srcElement = document.getElementById(src);
    if (srcElement) {
        srcElement.textContent = "dragging";
    }
    console.log("dragStart");
    return true;
}
function dragEnter(event) {
    var targetElement = document.getElementById(event.target.id);
    event.preventDefault();
    if (targetElement) {
        targetElement.style.backgroundColor = 'blue';
    }
    console.log("drag enter");
    return true;
}
function dragOver(event) {
    event.preventDefault(); // Necessary for allowing a drop
    console.log("drag over");
    return false;
}
function dragDrop(event) {
    var src = event.dataTransfer.getData("Text");
    var srcElement = document.getElementById(src);
    if (srcElement && event.target) {
        event.target.appendChild(srcElement);
        srcElement.style.backgroundColor = 'green';
        srcElement.textContent = "Dropped!";
    }
    console.log("dragDrop");
    // Change color upon drop
    event.stopPropagation();
    isDropped = true;
    return false;
}
function dragExit(event) {
    var targetElement = document.getElementById(event.target.id);
    event.preventDefault();
    if (targetElement) {
        targetElement.style.backgroundColor = '#fd8166';
    }
    console.log("drag exit");
    return false;
}
function dragEnd(event) {
    var srcElement = document.getElementById(event.target.id);
    if (srcElement && !isDropped) {
        srcElement.style.backgroundColor = '#A9A9A9';
        srcElement.textContent = "drag";
    }
    console.log("drag end");
    // Change color upon drop
    event.stopPropagation();
    return false;
}
