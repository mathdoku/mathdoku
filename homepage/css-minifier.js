#!/usr/bin/env node

const fileSystem = require("fs");
// Load the CSS file.
require("async").waterfall([fileSystem.readFile.bind(fileSystem, "page/unprocessed-style.css", {"encoding": "utf8"}),
// Minify the CSS.
function minify(original, callback) {
	var minified;
	try {
		minified = require("clean-css").process(original);
	} catch (error) {
		callback(error);
		return;
	}
	callback(null, minified);
},
// Write the result to the target CSS file.
function writeResult(minified, callback) {
	fileSystem.writeFile("page/style.css", minified, {"encoding": "utf8"}, callback);
}],
function complete(error) {
	if (null !== error) {
		process.stderr.write(error + "\n");
	}
});
