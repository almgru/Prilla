import ChartType from "../data-structures/chart-type";

const urlTable = {
    [[ChartType.CONSUMPTION, 'week']]:  date => `/api/week-data?year=${date.year()}&week=${date.isoWeek()}`,
    [[ChartType.CONSUMPTION, 'month']]: date => `/api/month-data?year=${date.year()}&month=${date.month()}`,
    [[ChartType.CONSUMPTION, 'year']]: date => `/api/year-data?year=${date.year()}`,
    [[ChartType.DURATION, 'year']]: date => `year=${date.year()}`,
    [[ChartType.DURATION, 'year']]: date => `year=${date.year()}`,
    [[ChartType.DURATION, 'year']]: date => `year=${date.year()}`,
    [[ChartType.DURATION_BETWEEN, 'year']]: date => `year=${date.year()}`,
    [[ChartType.DURATION_BETWEEN, 'year']]: date => `year=${date.year()}`,
    [[ChartType.DURATION_BETWEEN, 'year']]: date => `year=${date.year()}`,
};

export default (chartType, interval, date) => urlTable[[chartType, interval]](date);