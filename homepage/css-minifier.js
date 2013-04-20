#!/usr/bin/env node

const fileSystem = require("fs");
// Load the CSS file.
const originalCSS = fileSystem.readFileSync("page/unprocessed-style.css", "utf-8");
// Minify the CSS clean-css. Write the result to the target CSS file.
(function() {
	const minify = require("clean-css").process;
	const minifiedCSS = minify(originalCSS);
	fileSystem.writeFileSync("page/style.css", minifiedCSS, "utf-8");
})();
