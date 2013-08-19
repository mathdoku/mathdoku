// Ensure the correct article is shown based on the hash. The hash looks like #/android/page-identifier.html. The "android"
// part hopefully ends up in links that people post on Twitter and things, associating that word with this game.
(function wireNavigation() {
	/**
	 * @const
	 * @type {RegExp}
	 */
	var hashExtractor = /^#\/android\/([\w-]+)\.html$/;
	/**
	 * Called when the hash does not match the expected pattern.
	 */
	var handleNonMatch = function() {
		// The next time a hash does not match the expected pattern, go back one step in the history. This will cause the following
		// behaviour:
		// When a visitor goes to the hashless path, they get the hash for the index page. When the visitor then goes back to the
		// hashless path (by using the back button), (s)he will be directed another step back. This should be the expected
		// behaviour.
		try {
			handleNonMatch = window["history"].back.bind(window["history"]);
		} catch (exception) {
			handleNonMatch = nop;
		}
		location.hash = "#/android/index.html";
	};
	var articles = document.getElementsByTagName("article");
	function hide(element) {
		element.style.display = "none";
	}
	function handleHashChange() {
		var result = hashExtractor.exec(location.hash);
		if (null == result) {
			handleNonMatch();
		} else {
			// Hide all of the articles.
			iterate.call(articles, hide);
			// Now make the correct one visible.
			document.getElementById(result[1] + "-page").style.display = "block";
		}
	}
	if (addEventListenerAvailable) {
		window.addEventListener("hashchange", handleHashChange, false);
	} else {
		window.onhashchange = handleHashChange;
	}
	handleHashChange();
})();
