window.onload = _ => {
    d3.json("/api/week-data?year=2021&week=13")
        .then(dataLoaded)
        .catch(err => console.log(err));
};

const castData = data => data.map(d => ({
    "date": new Date(d.date),
    "amount": Number(d.amount)
}));

const dataLoaded = data => {
    const MARGIN = { LEFT: 50, RIGHT: 50, TOP: 50, BOTTOM: 50 };
    const WIDTH = 600 - MARGIN.LEFT - MARGIN.RIGHT;
    const HEIGHT = 400 - MARGIN.TOP - MARGIN.BOTTOM;
    const week = "13";

    const svg = d3.select('#chart-area').append('svg')
        .attr('width', WIDTH + MARGIN.LEFT + MARGIN.RIGHT)
        .attr('height', HEIGHT + MARGIN.TOP + MARGIN.BOTTOM);

    const g = svg.append('g')
        .attr('transform', `translate(${MARGIN.LEFT}, ${MARGIN.TOP})`);

    // X label
    g.append('text')
        .attr('class', 'x axis-label')
        .attr('x', WIDTH / 2)
        .attr('y', HEIGHT + 40)
        .attr('font-size', '16px')
        .attr('text-anchor', 'middle')
        .text(`Day (Week ${week})`);

    // Y label
    g.append('text')
        .attr('class', 'y axis-label')
        .attr('x', -(HEIGHT / 2))
        .attr('y', -25)
        .attr('font-size', '16px')
        .attr('text-anchor', 'middle')
        .attr('transform', 'rotate(-90)')
        .text('Snus portions consumed');

    data = castData(data);

    const x = d3.scaleBand()
        .domain(data.map(d => d.date))
        .range([0, WIDTH])
        .paddingInner(0.2)
        .paddingOuter(0.2);

    const y = d3.scaleLinear()
        .domain([0, d3.max(data, d => d.amount)])
        .range([HEIGHT, 0]);

    const xAxisCall = d3.axisBottom(x)
        .tickFormat(d3.timeFormat("%a"))

    g.append('g')
        .attr('class', 'x axis')
        .attr('transform', `translate(0, ${HEIGHT})`)
        .call(xAxisCall);

    const yAxisCall = d3.axisLeft(y)
        .tickValues(y.ticks().filter(tick => Number.isInteger(tick)))
        .tickFormat(d3.format('d'));

    g.append('g')
        .attr('class', 'y axis')
        .call(yAxisCall);

    const rects = g.selectAll('rect')
        .data(data);

    rects.enter().append('rect')
        .attr('x', d => x(d.date))
        .attr('y', d => y(d.amount))
        .attr('width', x.bandwidth())
        .attr('height', d => HEIGHT - y(d.amount))
        .attr('fill', 'blue');
};

