import ChartType from '../data-structures/enum/chart-type';
import TimeSpan from '../data-structures/enum/time-span';

const mapXLabel = (chartType, timeSpan, date) => {
    if (chartType === ChartType.CONSUMPTION || chartType === ChartType.DURATION) {
        switch (timeSpan) {
            case TimeSpan.WEEK:
                return `Days in week ${date.isoWeek()}`;
            case TimeSpan.MONTH:
                return `Weeks in ${date.format("MMMM")}, ${date.year()}`;
            case TimeSpan.YEAR:
                return `Months in ${date.year()}`;
            default:
                throw new Error('Invalid time span');
        }
    } else if (chartType === ChartType.DURATION_BETWEEN) {
        throw new Error('Not implemented.');
    } else {
        throw new Error('Invalid chart type.');
    }
}

const mapYLabel = (chartType, timeSpan, date) => {
    if (chartType === ChartType.CONSUMPTION) {
        return 'Snus portions consumed';
    } else if (chartType === ChartType.DURATION) {
        return 'Duration (minutes)'
    } else if (chartType === ChartType.DURATION_BETWEEN) {
        throw new Error('Not implemented.');
    } else {
        throw new Error('Invalid chart type.');
    }
};

export {mapXLabel, mapYLabel};