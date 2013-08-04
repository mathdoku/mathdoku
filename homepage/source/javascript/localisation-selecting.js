// Display the most appropriate localisation, based on navigator.language if available. It might be a good idea to base this on
// the Accept-Language HTTP header as well (which some browsers do anyway).
(function localize() {
	/**
	 * @const
	 * @type {string}
	 */
	var defaultLocale = "default";
	/**
	 * The list of accepted (supported) locales.
	 *
	 * @const
	 * @type {Array.<string>}
	 */
	var availableLocalizations = [defaultLocale, "nl"];
	/**
	 * An object that contains functions that set the page title to fit a locale.
	 *
	 * @const
	 * @type {Object.<string, function():void>}
	 */
	var titleSetters = (function() {
		function setTitle(value) {
			document.title = value;
		}
		var result = {"nl": setTitle.bind(this, "MathDoku \u00B7 Puzzelspel voor Android")};
		result[defaultLocale] = nop;
		return result;
	})();
	// Get the preferred locale from navigator.language.
	var locale = "navigator" in window && "language" in navigator ? (function extractPrimaryLanguageSubtag(input) {
		var match = /^[a-z]{2,8}/.exec(input.toLocaleLowerCase());
		if (null == match) {
			return defaultLocale;
		} else {
			return match[0];
		}
	})(navigator.language) : defaultLocale;
	// Check whether the preferred locale is an accepted one (whether there is actually a localisation available in the HTML
	// document). Use the default one if not.
	if ("indexOf" in availableLocalizations) {
		if (-1 == availableLocalizations.indexOf(locale)) {
			locale = defaultLocale;
		}
	// Use an alternative implementation if the engine does not support indexOf.
	} else {
		(function() {
			var accepted = false;
			iterate.call(availableLocalizations, function(candidate) {
				if (candidate == locale) {
					accepted = true;
				}
			});
			if (false == accepted) {
				locale = defaultLocale;
			}
		})();
	}
	// Change the title of the page.
	titleSetters[locale]();
	// Find all of the elements that are involved in the localisation.
	var localizationElements = getElementsByClassName("localizable");
	// Show all of the elements for the preferred locale. Hide all others.
	var localeClassName = "locale-" + locale;
	iterate.call(localizationElements, "classList" in localizationElements[0] ? function(localizationElement) {
		localizationElement.style.display = localizationElement.classList.contains(localeClassName) ? "" : "none";
	// Use an alternatie implementation if the engine does not support classList.
	} : (function() {
		var localeClassNameFound;
		function checkClassName(className) {
			localeClassNameFound = localeClassNameFound || localeClassName == className;
		}
		return function(localizationElement) {
			localeClassNameFound = false;
			iterate.call(localizationElement.className.split(" "), checkClassName);
			localizationElement.style.display = localeClassNameFound ? "" : "none";
		}
	})());
})();
