window.onload = _ => {
    d3.json("/api/entries?page=1")
        .then(dataLoaded)
        .catch(err => console.log(err));
};

const dateWithoutTime = date => new Date(date.getFullYear(), date.getMonth(), date.getDate());
const plusDays = (date, days) => (
    new Date(date.getFullYear(), date.getMonth(), date.getDate() + days)
);

const dataLoaded = data => {
    const svg = d3.select('#chart-area').append('svg')
        .attr('width', 400)
        .attr('height', 400);

    data.forEach(d => {
        d.inserted = new Date(d.inserted);
        d.removed = new Date(d.removed);
        d.amount = Number(d.amount);
    })

    const minDate = d3.min(data.map(e => e.inserted));
    const dates = Array.from({length: 7}, (_, i) => plusDays(minDate, i));

    data = dates.map(date => ({
        "date": date,
        "amount": data
            .filter(e => dateWithoutTime(e.inserted).getTime() === date.getTime())
            .reduce((res, e) => res + e.amount, 0)
    }));

    console.log(data);

    const x = d3.scaleTime()
        .domain(d3.extent(dates))
        .range([0, 400]);

    const y = d3.scaleLinear()
        .domain(d3.extent(data.map(e => e.amount)))
        .range([0, 400]);

    const rects = svg.selectAll('rect').data(data).enter().append('rect')
        .attr('x', d => x(d.date))
        .attr('y', 0)
        .attr('width', 20)
        .attr('height', d => y(d.amount))
        .attr('fill', 'blue');
};

