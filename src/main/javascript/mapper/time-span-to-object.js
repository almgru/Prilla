import TimeSpan from '../data-structures/enum/time-span';

const timeSpanTable = {
    [TimeSpan.WEEK]: {
        span: 'week',
        step: 'day'
    },
    [TimeSpan.MONTH]: {
        span: 'month',
        step: 'week'
    },
    [TimeSpan.YEAR]: {
        span: 'year',
        step: 'month'
    }
};

export default timeSpan => timeSpanTable[timeSpan];