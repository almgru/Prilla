const path = require("path");
const es5 = require("are-you-es5");

const includeModules = es5
    .checkModules({
        path: "",
        checkAllNodeModules: true,
        ignoreBabelAndWebpackPackages: true,
    })
    .es6Modules.filter((v) => !v.includes("core-js"))
    .map((v) => path.resolve(__dirname, "node_modules", v));

module.exports = {
    entry: path.resolve(__dirname, "src", "main.js"),
    output: {
        path: path.resolve(__dirname, "out"),
        filename: "prilla.js",
    },
    module: {
        rules: [
            {
                test: /\.(js|mjs)$/,
                include: [path.resolve(__dirname, "src"), ...includeModules],
                use: "babel-loader",
            },
        ],
    },
};
