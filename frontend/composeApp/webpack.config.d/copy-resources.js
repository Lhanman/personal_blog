const CopyPlugin = require("copy-webpack-plugin");
const path = require("path");

config.plugins.push(
    new CopyPlugin({
        patterns: [
            {
                from: path.resolve(__dirname, "../../../../frontend/composeApp/build/processedResources/wasmJs/main"),
                to: ".",
                noErrorOnMissing: true,
                force: false,
            },
        ],
    })
);
