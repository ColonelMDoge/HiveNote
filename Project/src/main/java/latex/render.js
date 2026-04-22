const puppeteer = require("puppeteer");

(async () => {
    const browser = await puppeteer.launch();

    const page = await browser.newPage();

    await page.goto("file://" + __dirname + "/output.html");

    await page.waitForFunction(() => window.__RENDER_DONE__ === true);

    await page.setViewport({ width: 1920, height: 10});

    await page.screenshot({
        path: "equation.png",
        fullPage: true
    });

    await browser.close();
})();

/*

PLAYWRIGHT ALTERNATIVE FOR ARM-BASED CPU
** Requires the playwright module and its dependencies **

const { chromium } = require("playwright");

(async () => {
    const browser = await chromium.launch();

    const page = await browser.newPage();

    await page.setViewportSize({ width: 1920, height: 10 });

    await page.goto("file://" + __dirname + "/output.html");

    await page.waitForFunction(() => window.__RENDER_DONE__ === true);

    await page.screenshot({
        path: "equation.png",
        fullPage: true
    });

    await browser.close();
})();
*/