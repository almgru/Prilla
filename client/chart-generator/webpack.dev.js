const path = require("path");
const { merge } = require('webpack-merge');
const common = require('./webpack.common.js');

module.exports = merge(common, {
  devtool: "source-map",
  mode: "development",
  devServer: {
    host: "0.0.0.0",
    port: 8081,
    contentBase: path.resolve(__dirname, "out"),
    watchContentBase: true,
    watchOptions: {
      poll: true,
    },
  }
});
