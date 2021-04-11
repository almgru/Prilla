import mapTimeSpanToObject from './time-span-to-object';
import ChartType from "../data-structures/enum/chart-type";

const endPointTable = {
    [ChartType.CONSUMPTION]: '/api/amount-data',
    [ChartType.DURATION]: '/api/duration-data',
    [ChartType.DURATION_BETWEEN]: '/api/duration-between'
};

const params = state => (
    `span=${mapTimeSpanToObject(state.timeSpan).span.toUpperCase()}&start=${state.date.format('YYYY-MM-DD')}`
);

export default state => `${endPointTable[state.chartType]}?${params(state)}`