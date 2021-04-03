import { nodeResolve } from '@rollup/plugin-node-resolve';

export default {
    input: 'src/main/javascript/main.js',
    output: {
        file: 'src/main/resources/static/js/trabacco.js',
        format: 'iife'
    },
    plugins: [
        nodeResolve()
    ]
};