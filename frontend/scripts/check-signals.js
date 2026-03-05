const fs = require("fs");
const path = require("path");

const srcDir = path.join(__dirname, "../src");
let hasErrors = false;

function walkDir(dir, callback) {
  fs.readdirSync(dir).forEach((f) => {
    let dirPath = path.join(dir, f);
    let isDirectory = fs.statSync(dirPath).isDirectory();
    isDirectory ? walkDir(dirPath, callback) : callback(dirPath);
  });
}

console.log("--- Custom Lint: OnPush and Signals ---");

walkDir(srcDir, (filePath) => {
  if (filePath.endsWith(".component.ts")) {
    const content = fs.readFileSync(filePath, "utf8");
    const templatePath = filePath.replace(".component.ts", ".component.html");
    const templateContent = fs.existsSync(templatePath)
      ? fs.readFileSync(templatePath, "utf8")
      : "";

    // Check for ChangeDetectionStrategy.OnPush
    if (!content.includes("ChangeDetectionStrategy.OnPush")) {
      console.error(
        `Error: Component at ${filePath} does not use ChangeDetectionStrategy.OnPush`,
      );
      hasErrors = true;
    }

    const signalPattern =
      /\bsignal\s*(?:<[^>]+>)?\s*\(|computed\s*(?:<[^>]+>)?\s*\(|effect\s*\(|input\s*\(|input\.required\s*\(/;
    const usesSignals = signalPattern.test(content);
    const usesAsyncPipe =
      /\bAsyncPipe\b/.test(content) || /\|\s*async\b/.test(templateContent);

    const hasUiStateProperty =
      /^\s*(?:public|private|protected)?\s*(?:isLoading|loading|shoppingLists|listItems|availableItems|items|categories|units|users|permissions|selected[A-Z]\w*|editing[A-Z]\w*|hidePassword|isLargeScreen|quantity)\s*(?::[^=\n]+)?=\s*.*;\s*$/m.test(
        content,
      );

    const mutableUiStatePattern =
      /^\s*(?:public|private|protected)?\s*(?:isLoading|loading|shoppingLists|listItems|availableItems|items|categories|units|users|permissions|selected[A-Z]\w*|editing[A-Z]\w*|hidePassword|isLargeScreen|quantity)\s*(?::[^=\n]+)?=\s*(?!signal\s*(?:<[^>]+>)?\s*\().*;\s*$/m;

    if (mutableUiStatePattern.test(content)) {
      console.error(
        `Error: Component at ${filePath} has mutable UI state not declared as signal().`,
      );
      hasErrors = true;
    }

    if (hasUiStateProperty && !usesSignals && !usesAsyncPipe) {
      console.error(
        `Error: Component at ${filePath} must use Signals or AsyncPipe for reactive state.`,
      );
      hasErrors = true;
    }
  }
});

if (hasErrors) {
  console.error("\nCustom linting failed.");
  process.exit(1);
} else {
  console.log("\nCustom linting passed!");
  process.exit(0);
}
