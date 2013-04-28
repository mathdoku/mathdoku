// Make sure the title ("MathDoku") fits on the screen.
(function addTitleSizingListener() {
	function sizeTitle() {
		// Make the title 3.5em, and reduce the size by one-fourth as long as the entire title cannot be shown.
		for (var size = 3.5; size >= 2; size -= .25) {
			title.style.fontSize = size + "em";
			title.style.marginLeft = "-" + Math.floor(size) + "px";
			if (title.clientWidth == title.scrollWidth) {
				break;
			}
		}
	}
	if (addEventListenerAvailable) {
		window.addEventListener("resize", sizeTitle, false);
	} else {
		window.onresize = sizeTitle;
	}
	sizeTitle();
})();
