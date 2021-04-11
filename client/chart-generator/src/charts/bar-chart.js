import * as d3Scale from "d3-scale";
import * as d3Axis from "d3-axis";
import * as d3Format from "d3-format";

import * as chartUtils from './chart-util';

export default (data, config) => {
    const WIDTH_EXCL_MARGIN = config.canvas.size.width - config.canvas.margin.left - config.canvas.margin.right;
    const HEIGHT_EXCL_MARGIN = config.canvas.size.height - config.canvas.margin.top - config.canvas.margin.bottom;

    const canvas = chartUtils.createCanvas(config);

    const x = d3Scale.scaleBand()
        .domain(data.map(d => d.label))
        .range([0, WIDTH_EXCL_MARGIN])
        .paddingInner(config.canvas.padding.inner)
        .paddingOuter(config.canvas.padding.outer);

    const y = d3Scale.scaleLinear()
        .domain([0, Math.max(1, Math.max(...data.map(d => d.value)))])
        .range([HEIGHT_EXCL_MARGIN, 0]);

    const xAxisCall = d3Axis.axisBottom(x);

    canvas.append('g')
        .attr('class', 'x axis')
        .attr('transform', `translate(0, ${HEIGHT_EXCL_MARGIN})`)
        .call(xAxisCall);

    const yAxisCall = d3Axis.axisLeft(y)
        .tickValues(y.ticks().filter(tick => Number.isInteger(tick)))
        .tickFormat(d3Format.format('d'));

    canvas.append('g')
        .attr('class', 'y axis')
        .call(yAxisCall);

    canvas.selectAll('rects')
        .data(data)
        .enter()
        .append('rect')
            .attr('class', 'bar')
            .attr('x', d => x(d.label))
            .attr('y', d => y(d.value))
            .attr('width', x.bandwidth())
            .attr('height', d => HEIGHT_EXCL_MARGIN - y(d.value))
            .attr('fill', 'white')
            .attr('stroke', 'black');

    return canvas.node().parentNode;
};