import * as d3Scale from "d3-scale";
import * as d3Axis from "d3-axis";

import * as chartUtils from './chart-util';

export default (data, config) => {
    const WIDTH_EXCL_MARGIN = config.canvas.size.width - config.canvas.margin.left - config.canvas.margin.right;
    const HEIGHT_EXCL_MARGIN = config.canvas.size.height - config.canvas.margin.top - config.canvas.margin.bottom;

    const canvas = chartUtils.createCanvas(config);

    const x = d3Scale.scaleBand()
        .domain(data.labels)
        .range([0, WIDTH_EXCL_MARGIN])
        .paddingInner(config.canvas.padding.inner)
        .paddingOuter(config.canvas.padding.outer);

    const yScaleMax = Math.max(...[...data.dots.map(d => d.value), ...data.summary.map(d => d.max)]);
    const yScaleMin = Math.min(...[...data.dots.map(d => d.value), ...data.summary.map(d => d.min)]);
    const y = d3Scale.scaleLinear()
        .domain([yScaleMin, Math.max(1, yScaleMax)])
        .range([HEIGHT_EXCL_MARGIN, 0]);

    const xAxisCall = d3Axis.axisBottom(x);

    canvas.append('g')
        .attr('class', 'x axis')
        .attr('transform', `translate(0, ${HEIGHT_EXCL_MARGIN})`)
        .call(xAxisCall);

    const yAxisCall = d3Axis.axisLeft(y);

    canvas.append('g')
        .attr('class', 'y axis')
        .call(yAxisCall);

    canvas.selectAll('vertLines')
        .data(data.summary)
        .enter()
        .append('line')
            .attr('class', 'range-line')
            .attr('x1', d => x(d.label) + x.bandwidth() / 2)
            .attr('x2', d => x(d.label) + x.bandwidth() / 2)
            .attr('y1', d => y(d.min))
            .attr('y2', d => y(d.max))
            .attr('stroke', 'black');

    canvas.selectAll('boxes')
        .data(data.summary)
        .enter()
        .append('rect')
            .attr('class', 'box')
            .attr('x', d => x(d.label))
            .attr('y', d => y(d.q3))
            .attr('width', x.bandwidth())
            .attr('height', d => y(d.q1) - y(d.q3))
            .attr('fill', 'white')
            .attr('stroke', 'black');

    canvas.selectAll('medianLines')
        .data(data.summary)
        .enter()
        .append('line')
            .attr('class', 'median-line')
            .attr('x1', d => x(d.label))
            .attr('x2', d => x(d.label) + x.bandwidth())
            .attr('y1', d => y(d.median))
            .attr('y2', d => y(d.median))
            .attr('stroke', 'black');

    canvas.selectAll('minLines')
        .data(data.summary)
        .enter()
        .append('line')
            .attr('class', 'min-line')
            .attr('x1', d => x(d.label) + x.bandwidth() / 4)
            .attr('x2', d => x(d.label) + x.bandwidth() * 3 / 4)
            .attr('y1', d => y(d.min))
            .attr('y2', d => y(d.min))
            .attr('stroke', 'black');

    canvas.selectAll('maxLines')
        .data(data.summary)
        .enter()
        .append('line')
            .attr('class', 'max-line')
            .attr('x1', d => x(d.label) + x.bandwidth() / 4)
            .attr('x2', d => x(d.label) + x.bandwidth() * 3 / 4)
            .attr('y1', d => y(d.max))
            .attr('y2', d => y(d.max))
            .attr('stroke', 'black');

    const jitterWidth = x.bandwidth() / 2;
    canvas.selectAll('dots')
        .data(data.dots)
        .enter()
        .append('circle')
            .attr('class', 'dot')
            .attr('cx', d => x(d.label) + x.bandwidth() / 2 - jitterWidth / 2 + Math.random() * jitterWidth)
            .attr('cy', d => y(d.value))
            .attr('r', 2)
            .attr('fill', 'white')
            .attr('stroke', 'black');

    return canvas.node().parentNode;
};