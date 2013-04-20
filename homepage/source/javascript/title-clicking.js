// Clicking the title should navigate to home. The title shouldn't really be a link, because that would indicate that it can
// be clicked. It shouldn't look like it can be clicked. Rather, it should just work in case someone does click it.
(function makeTitleClickable() {
	function goHome() {
		location.hash = "#/android/index.html";
	}
	if (addEventListenerAvailable) {
		title.addEventListener("click", goHome, false);
	} else {
		title.onclick = goHome;
	}
})();
