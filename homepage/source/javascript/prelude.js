/**
 * Whether addEventListener is available on the window, and therefore probably on other objects as well.
 *
 * @const
 * @type {boolean}
 */
var addEventListenerAvailable = "addEventListener" in window;
/**
 * An empty array. Keep it empty, please!
 *
 * @const
 * @type {Array.<*>}
 */
var emptyArray = [];
/**
 * Iterates over all of the elements in a list. Probably based on Array.prototype.forEach.
 *
 * @const
 * @type {function(Function, Object=):undefined}
 */
var iterate = (function() {
	// Return the native forEach implementation for arrays, if that exists.
	if ("forEach" in emptyArray) {
		return emptyArray["forEach"];
	} else {
		return function(callback, scope) {
			for (var index = 0, length = this.length; index < length; ++index) {
				callback.call(scope, this[index], index, this);
			}
		};
	}
})();
/**
 * A null object implementation of a function
 */
var nop = function() {
};
// Create a bind function for browsers that don't support it natively. IE 8, looking at you.
if (!("bind" in nop)) {
	/**
	 * @param {Object} scope
	 * @param {...*} overflowedArguments
	 * @return {!Function}
	 */
	Function.prototype["bind"] = function(scope, overflowedArguments) {
		/**
		 * @const
		 * @type {Array.<*>}
		 */
		var boundArguments = emptyArray.slice.call(arguments, 1);
		/**
		 * @const
		 * @type {Function}
		 */
		var boundFunction = this;
		return function() {
			return boundFunction.apply(scope, boundArguments.concat(boundArguments.slice.call(arguments)));
		};
	};
}
/**
 * Returns a list-like object with all of the elements that have the passed class. Might be a NodeList or an HTMLCollection.
 * Probably based on document.getElementsByClassName.
 *
 * @const
 * @type {function(string)}
 */
var getElementsByClassName = (function() {
	if ("getElementsByClassName" in document) {
		return document.getElementsByClassName.bind(document);
	} else {
		return function getElementsByClassName(className) {
			return document.querySelectorAll("." + className);
		};
	}
})();
/**
 * The title element ("MathDoku").
 * @const
 * @type {HTMLElement}
 */
var title = document.getElementsByTagName("h1")[0];
