import * as d3Selection from "d3-selection";

const createCanvas = config => {
    const WIDTH_EXCL_MARGIN = config.canvas.size.width - config.canvas.margin.left - config.canvas.margin.right;
    const HEIGHT_EXCL_MARGIN = config.canvas.size.height - config.canvas.margin.top - config.canvas.margin.bottom;

    const canvas = d3Selection.create('svg')
        .attr('width', config.canvas.size.width)
        .attr('height', config.canvas.size.height);

    const root = canvas.append('g')
        .attr('transform', `translate(${config.canvas.margin.left}, ${config.canvas.margin.top})`)

    root.append(() => _createAxisLabel(config.labels.x, config.fontSize, WIDTH_EXCL_MARGIN / 2, HEIGHT_EXCL_MARGIN + 40)
        .attr('class', 'x axis-label')
        .node());
    root.append(() => _createAxisLabel(config.labels.y, config.fontSize, -(HEIGHT_EXCL_MARGIN) / 2, -30)
        .attr('class', 'y axis-label')
        .attr('transform', 'rotate(-90)')
        .node());

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