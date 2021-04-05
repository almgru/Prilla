import { create } from "d3-selection";
import { scaleBand, scaleLinear } from "d3-scale";
import { axisBottom, axisLeft } from "d3-axis";
import { format } from "d3-format";

const getXLabelText = (timeSpan, date) => {
    switch (timeSpan.toLowerCase()) {
        case 'week':
            return `Day of week ${date.isoWeek()}, ${date.year()}`;
        case 'month':
            return `Week, ${date.format("MMMM")} ${date.year()}`;
        case 'year':
            return `Month, ${date.year()}`;
        default:
            throw new Error('Invalid time span');
    }
}

export default (data, timeSpan, date, config) => {
    const WIDTH_EXCL_MARGIN = config.canvas.size.width - config.canvas.margin.left - config.canvas.margin.right;
    const HEIGHT_EXCL_MARGIN = config.canvas.size.height - config.canvas.margin.top - config.canvas.margin.bottom;

    const svg = create('svg')
        .attr('width', config.canvas.size.width)
        .attr('height', config.canvas.size.height);

    const g = svg.append('g')
        .attr('transform', `translate(${config.canvas.margin.left}, ${config.canvas.margin.top})`);

    // X label
    g.append('text')
        .attr('class', 'x axis-label')
        .attr('x', WIDTH_EXCL_MARGIN / 2)
        .attr('y', HEIGHT_EXCL_MARGIN + 40)
        .attr('font-size', config.fontSize)
        .attr('text-anchor', 'middle')
        .text(getXLabelText(timeSpan, date));

    // Y label
    g.append('text')
        .attr('class', 'y axis-label')
        .attr('x', -(HEIGHT_EXCL_MARGIN / 2))
        .attr('y', -30)
        .attr('font-size', config.fontSize)
        .attr('text-anchor', 'middle')
        .attr('transform', 'rotate(-90)')
        .text('Snus portions consumed');

    const x = scaleBand()
        .domain(data.map(d => d.label))
        .range([0, WIDTH_EXCL_MARGIN])
        .paddingInner(config.canvas.padding.inner)
        .paddingOuter(config.canvas.padding.outer);

    const y = scaleLinear()
        .domain([0, Math.max(1, Math.max(...data.map(d => d.value)))])
        .range([HEIGHT_EXCL_MARGIN, 0]);

    const xAxisCall = axisBottom(x);

    g.append('g')
        .attr('class', 'x axis')
        .attr('transform', `translate(0, ${HEIGHT_EXCL_MARGIN})`)
        .call(xAxisCall);

    const yAxisCall = axisLeft(y)
        .tickValues(y.ticks().filter(tick => Number.isInteger(tick)))
        .tickFormat(format('d'));

    g.append('g')
        .attr('class', 'y axis')
        .call(yAxisCall);

    const rects = g.selectAll('rect')
        .data(data);

    rects.enter().append('rect')
        .attr('x', d => x(d.label))
        .attr('y', d => y(d.value))
        .attr('width', x.bandwidth())
        .attr('height', d => HEIGHT_EXCL_MARGIN - y(d.value))
        .attr('fill', 'blue');

    return svg.node();
};