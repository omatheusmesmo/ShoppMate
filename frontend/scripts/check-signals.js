const fs = require('fs');
const path = require('path');

function walkDir(dir, callback) {
    fs.readdirSync(dir).forEach(f => {
        let dirPath = path.join(dir, f);
        let isDirectory = fs.statSync(dirPath).isDirectory();
        isDirectory ? walkDir(dirPath, callback) : callback(path.join(dir, f));
    });
}

const distinctComponents = [];

walkDir('src/app', (filePath) => {
    if (filePath.endsWith('.ts')) {
        const content = fs.readFileSync(filePath, 'utf8');

        if (content.includes('@Component')) {
            const componentNameMatch = content.match(/class\s+(\w+)/);
            const componentName = componentNameMatch ? componentNameMatch[1] : 'UnknownComponent';

            const usesOnPush = content.includes('ChangeDetectionStrategy.OnPush');

            const usesSignals = content.includes('signal(') || content.includes('computed(') || content.includes('effect(');

            if (!usesOnPush) {
                console.warn(`[WARNING] Component ${componentName} (${filePath}) is NOT using OnPush change detection.`);
            }

            if (!usesSignals) {
                console.info(`[INFO] Component ${componentName} (${filePath}) does not appear to use Signals. Consider migrating state management to Signals.`);
            }
        }
    }
});

console.log('Custom lint check completed.');
process.exit(0);
