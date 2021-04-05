import flow from 'lodash.flow';

import ChartType from '../data-structures/chart-type';
import TimeSpan from '../data-structures/time-span';

const path = {
    [ChartType.CONSUMPTION]: '/api/amount-data',
    [ChartType.DURATION]: '/api/duration-data',
    [ChartType.DURATION_BETWEEN]: '/api/duration-between'
};

const query = flow(
    state => params(state),
    params => paramsToQuery(params)
);

const params = state => (
    paramKeys[state.timeSpan].reduce(
        (acc, val) => ({
            ...acc,
            [val]: values[val](state)}),
        {}
    )
);

const paramKeys = {
    [TimeSpan.WEEK]: ['span', 'year', 'week'],
    [TimeSpan.MONTH]: ['span', 'year', 'month'],
    [TimeSpan.YEAR]: ['span', 'year']
}

const values = {
    span: state => state.timeSpan.toUpperCase(),
    week: state => state.date.isoWeek(),
    month: state => state.date.month(),
    year: state => state.date.year()
}

const paramsToQuery = params => (
    Object.entries(params)
        .map(arr => arr.join('='))
        .join('&')
);

export default state => `${path[state.chartType]}?${query(state)}`