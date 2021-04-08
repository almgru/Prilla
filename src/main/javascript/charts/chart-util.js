import * as d3Selection from "d3-selection";

const createCanvas = config => {
    const WIDTH_EXCL_MARGIN = config.canvas.size.width - config.canvas.margin.left - config.canvas.margin.right;
    const HEIGHT_EXCL_MARGIN = config.canvas.size.height - config.canvas.margin.top - config.canvas.margin.bottom;

    const canvas = d3Selection.create('svg')
        .attr('viewBox', `0 0 ${config.canvas.size.width} ${config.canvas.size.height}`);

    const root = canvas.append('g')
        .attr('transform', `translate(${config.canvas.margin.left}, ${config.canvas.margin.top})`)

    root.append('text')
        .attr('x', WIDTH_EXCL_MARGIN / 2, HEIGHT_EXCL_MARGIN + 40)
        .attr('y', HEIGHT_EXCL_MARGIN + 40)
        .attr('font-size', config.fontSize)
        .attr('text-anchor', 'middle')
        .text(config.labels.x);

    root.append('text')
        .attr('x', -(HEIGHT_EXCL_MARGIN) / 2)
        .attr('y', -30)
        .attr('font-size', config.fontSize)
        .attr('text-anchor', 'middle')
        .attr('transform', 'rotate(-90)')
        .text(config.labels.y);

    return root;
};

const _createAxisLabel = (text, fontSize, x, y) => (
    d3Selection.create('text')
        .attr('class', 'x axis-label')
        .attr('x', x)
        .attr('y', y)
        .attr('font-size', fontSize)
        .attr('text-anchor', 'middle')
        .text(text)
);

export { createCanvas };