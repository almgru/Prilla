const BarChartRange = Object.freeze({
    WEEK: {
        name: 'week',
        url: '/api/week-data',
        interval: 'weeks'
    },
    MONTH: {
        name: 'month',
        url: '/api/month-data',
        interval: 'months'
    },
    YEAR: {
        name: 'year',
        url: '/api/year-data',
        interval: 'years'
    }
});

const startOfWeek = moment().startOf('week').add(1, 'days');
let state = {
    range: BarChartRange.WEEK,
    date: moment(startOfWeek),
    params: {
        year: null,
        week: null
    }
};

window.onload = () => {
    state.params = getParams(state);
    setupListeners();
    fetchData(state);
};

const setupListeners = () => {
    document.querySelector('#nextBtn').onclick = nextBtnClicked;
    document.querySelector('#previousBtn').onclick = previousBtnClicked;
    document.querySelector('#rangeWeekBtn').onclick = rangeWeekBtnClicked;
    document.querySelector('#rangeMonthBtn').onclick = rangeMonthBtnClicked;
    document.querySelector('#rangeYearBtn').onclick = rangeYearBtnClicked;
};

const previousBtnClicked = () => {
    state.date.subtract(1, state.range.interval);
    state.params = getParams(state);
    fetchData(state);
};

const nextBtnClicked = () => {
    state.date.add(1, state.range.interval);
    state.params = getParams(state);
    fetchData(state);
};

const rangeWeekBtnClicked = () => {
    state.date = moment(startOfWeek);
    state.range = BarChartRange.WEEK;
    state.params = getParams(state);
    fetchData(state);
};

const rangeMonthBtnClicked = () => {
    state.date = moment(startOfWeek);
    state.range = BarChartRange.MONTH;
    state.params = getParams(state);
    fetchData(state);
}

const rangeYearBtnClicked = _ => {
    state.date = moment(startOfWeek);
    state.range =  BarChartRange.YEAR;
    state.params = getParams(state);
    fetchData(state);
}

const fetchData = state => {
    let paramStr = Object.entries(state.params)
        .map(e => e.join('='))
        .join('&');

    d3.json(`${state.range.url}?${paramStr}`)
        .then(castData)
        .then(data => updateBarChart(data, getXLabelText(state)))
        .catch(err => console.log(err))
};

const castData = data => data.map(dataPoint => ({
    label: dataPoint.label,
    value: Number(dataPoint.value)
}));

const updateBarChart = (data, xLabelText) => {
    const MARGIN = { LEFT: 50, RIGHT: 50, TOP: 50, BOTTOM: 50 };
    const WIDTH = 600 - MARGIN.LEFT - MARGIN.RIGHT;
    const HEIGHT = 400 - MARGIN.TOP - MARGIN.BOTTOM;

    d3.select('#chart-area').selectAll('*').remove();

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
        .text(xLabelText);

    // Y label
    g.append('text')
        .attr('class', 'y axis-label')
        .attr('x', -(HEIGHT / 2))
        .attr('y', -25)
        .attr('font-size', '16px')
        .attr('text-anchor', 'middle')
        .attr('transform', 'rotate(-90)')
        .text('Snus portions consumed');

    const x = d3.scaleBand()
        .domain(data.map(d => d.label))
        .range([0, WIDTH])
        .paddingInner(0.2)
        .paddingOuter(0.2);

    const y = d3.scaleLinear()
        .domain([0, Math.max(1, d3.max(data, d => d.value))])
        .range([HEIGHT, 0]);

    const xAxisCall = d3.axisBottom(x);

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
        .attr('x', d => x(d.label))
        .attr('y', d => y(d.value))
        .attr('width', x.bandwidth())
        .attr('height', d => HEIGHT - y(d.value))
        .attr('fill', 'blue');
};

const getXLabelText = state => {
    switch (state.range.name.toLowerCase()) {
        case 'week':
            return `Day of week ${state.params.week}, ${state.params.year}`;
        case 'month':
            return `Week, ${state.date.format("MMMM")} ${state.date.year()}`;
        case 'year':
            return `Month, ${state.params.year}`;
        default:
            throw new Error('Invalid range');
    }
}

const getParams = state => {
    switch(state.range.name.toLowerCase()) {
        case 'week':
            return {
                year: state.date.year(),
                week: state.date.isoWeek()
            }
        case 'month':
            return {
                year: state.date.year(),
                month: state.date.month() + 1
            }
        case 'year':
            return {
                year: state.date.year()
            }
        default:
            throw new Error('Invalid range');
    }
};
