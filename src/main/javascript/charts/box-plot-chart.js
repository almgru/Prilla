import { create } from "d3-selection";
import { scaleBand, scaleLinear } from "d3-scale";
import { axisBottom, axisLeft } from "d3-axis";
import { format } from 'd3-format';

const getXLabelText = (interval, date) => {
    switch (interval.toLowerCase()) {
        case 'week':
            return `Day of week ${date.isoWeek()}, ${date.year()}`;
        case 'month':
            return `Week, ${date.format("MMMM")} ${date.year()}`;
        case 'year':
            return `Month, ${date.year()}`;
        default:
            throw new Error('Invalid interval');
    }
}

export default (data, interval, date, config) => {
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
        .text(getXLabelText(interval, date));

    // Y label
    g.append('text')
        .attr('class', 'y axis-label')
        .attr('x', -(HEIGHT_EXCL_MARGIN / 2))
        .attr('y', -30)
        .attr('font-size', config.fontSize)
        .attr('text-anchor', 'middle')
        .attr('transform', 'rotate(-90)')
        .text('Duration (minutes)');

    const x = scaleBand()
        .domain(data.labels)
        .range([0, WIDTH_EXCL_MARGIN])
        .paddingInner(config.canvas.padding.inner)
        .paddingOuter(config.canvas.padding.outer);

    const yScaleMax = Math.max(...data.summary.map(o => o.max));
    const yScaleMin = Math.min(...data.summary.map(o => o.min)) <= 0 ? -(yScaleMax / 50) : 0;
    const y = scaleLinear()
        .domain([yScaleMin, Math.max(1, yScaleMax)])
        .range([HEIGHT_EXCL_MARGIN, 0]);

    const xAxisCall = axisBottom(x);

    g.append('g')
        .attr('class', 'x axis')
        .attr('transform', `translate(0, ${HEIGHT_EXCL_MARGIN})`)
        .call(xAxisCall);

    const yAxisCall = axisLeft(y);

    g.append('g')
        .attr('class', 'y axis')
        .call(yAxisCall);

    g.selectAll('vertLines')
        .data(data.summary)
        .enter()
        .append('line')
            .attr('x1', d => x(d.label) + x.bandwidth() / 2)
            .attr('x2', d => x(d.label) + x.bandwidth() / 2)
            .attr('y1', d => y(d.min))
            .attr('y2', d => y(d.max))
            .attr('stroke', 'black')
            .style('width', 40);

    g.selectAll('boxes')
        .data(data.summary)
        .enter()
        .append('rect')
            .attr('x', d => x(d.label))
            .attr('y', d => y(d.q3))
            .attr('width', x.bandwidth())
            .attr('height', d => y(d.q1) - y(d.q3))
            .attr('stroke', 'black')
            .attr('fill', '#69b3a2');

    g.selectAll('medianLines')
        .data(data.summary)
        .enter()
        .append('line')
            .attr('x1', d => x(d.label))
            .attr('x2', d => x(d.label) + x.bandwidth())
            .attr('y1', d => y(d.median))
            .attr('y2', d => y(d.median))
            .attr('stroke', 'black');

    g.selectAll('minLines')
        .data(data.summary)
        .enter()
        .append('line')
        .attr('x1', d => x(d.label) + x.bandwidth() / 4)
        .attr('x2', d => x(d.label) + x.bandwidth() * 3 / 4)
        .attr('y1', d => y(d.min))
        .attr('y2', d => y(d.min))
        .attr('stroke', 'black');

    g.selectAll('maxLines')
        .data(data.summary)
        .enter()
        .append('line')
        .attr('x1', d => x(d.label) + x.bandwidth() / 4)
        .attr('x2', d => x(d.label) + x.bandwidth() * 3 / 4)
        .attr('y1', d => y(d.max))
        .attr('y2', d => y(d.max))
        .attr('stroke', 'black');

    const jitterWidth = x.bandwidth() / 2;
    g.selectAll('dots')
        .data(data.dots)
        .enter()
        .append('circle')
            .attr('cx', d => x(d.label) + x.bandwidth() / 2 - jitterWidth / 2 + Math.random() * jitterWidth)
            .attr('cy', d => y(d.value))
            .attr('r', 4)
            .style('fill', 'white')
            .attr('stroke', 'black');

    return svg.node();
};