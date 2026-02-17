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

    // Check for ChangeDetectionStrategy.OnPush
    if (!content.includes("ChangeDetectionStrategy.OnPush")) {
      console.error(
        `Error: Component at ${filePath} does not use ChangeDetectionStrategy.OnPush`,
      );
      hasErrors = true;
    }

    // Optional: Check for signal usage (simple regex check for signal() or input.required(), etc.)
    // This is a naive check but can be expanded.
    const signalPattern =
      /\bsignal\s*\(|computed\s*\(|effect\s*\(|input\s*\(|input\.required\s*\(/;
    const usesSignals = signalPattern.test(content);

    // We might want to warn if a component doesn't use signals at all in Angular 19+
    if (!usesSignals) {
      console.warn(
        `Warning: Component at ${filePath} might not be using Signals. Consider migrating.`,
      );
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
