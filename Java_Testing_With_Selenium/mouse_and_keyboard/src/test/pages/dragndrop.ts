let isDropped: boolean = false;

function dragStart(event: DragEvent): boolean {
    event.dataTransfer!.setData("Text", (event.target as HTMLElement).id);
    event.dataTransfer!.effectAllowed = 'move';
    const src: string = event.dataTransfer!.getData("Text");
    const srcElement: HTMLElement | null = document.getElementById(src);
    if (srcElement) {
        srcElement.textContent = "dragging";
    }
    console.log("dragStart");
    return true;
}

function dragEnter(event: DragEvent): boolean {
    const targetElement: HTMLElement | null = document.getElementById((event.target as HTMLElement).id);
    event.preventDefault();
    if (targetElement) {
        targetElement.style.backgroundColor = 'blue';
    }
    console.log("drag enter");
    return true;
}

function dragOver(event: DragEvent): boolean {
    event.preventDefault(); // Necessary for allowing a drop
    console.log("drag over");
    return false;
}

function dragDrop(event: DragEvent): boolean {
    const src: string = event.dataTransfer!.getData("Text");
    const srcElement: HTMLElement | null = document.getElementById(src);
    if (srcElement && event.target) {
        (event.target as HTMLElement).appendChild(srcElement);
        srcElement.style.backgroundColor = 'green';
        srcElement.textContent = "Dropped!";
    }
    console.log("dragDrop");
    // Change color upon drop
    event.stopPropagation();
    isDropped = true;
    return false;
}

function dragExit(event: DragEvent): boolean {
    const targetElement: HTMLElement | null = document.getElementById((event.target as HTMLElement).id);
    event.preventDefault();
    if (targetElement) {
        targetElement.style.backgroundColor = '#fd8166';
    }
    console.log("drag exit");
    return false;
}

function dragEnd(event: DragEvent): boolean {
    const srcElement: HTMLElement | null = document.getElementById((event.target as HTMLElement).id);
    if (srcElement && !isDropped) {
        srcElement.style.backgroundColor = '#A9A9A9';
        srcElement.textContent = "drag";
    }
    console.log("drag end");
    // Change color upon drop
    event.stopPropagation();
    return false;
}