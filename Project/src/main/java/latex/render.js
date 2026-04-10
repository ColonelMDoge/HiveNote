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