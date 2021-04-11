import TimeSpan from '../data-structures/enum/time-span';

const timeSpanTable = {
    [TimeSpan.WEEK]: {
        span: 'week',
        step: 'day',
        start: 'isoWeek'
    },
    [TimeSpan.MONTH]: {
        span: 'month',
        step: 'week',
        start: 'month'
    },
    [TimeSpan.YEAR]: {
        span: 'year',
        step: 'month',
        start: 'year'
    }
};

export default timeSpan => timeSpanTable[timeSpan];