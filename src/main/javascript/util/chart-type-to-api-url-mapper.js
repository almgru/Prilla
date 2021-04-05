import ChartType from "../data-structures/chart-type";

const urlTable = {
    [[ChartType.CONSUMPTION, 'week']]:  date => `/api/week-data?year=${date.year()}&week=${date.isoWeek()}`,
    [[ChartType.CONSUMPTION, 'month']]: date => `/api/month-data?year=${date.year()}&month=${date.month()}`,
    [[ChartType.CONSUMPTION, 'year']]: date => `/api/year-data?year=${date.year()}`,
    [[ChartType.DURATION, 'week']]: date => `/api/duration-data/week?year=${date.year()}&week=${date.isoWeek()}`,
    [[ChartType.DURATION, 'month']]: date => `/api/fixed-data`,
    [[ChartType.DURATION, 'year']]: date => `/api/fixed-data`,
    [[ChartType.DURATION_BETWEEN, 'week']]: date => `/api/fixed-data`,
    [[ChartType.DURATION_BETWEEN, 'month']]: date => `/api/fixed-data`,
    [[ChartType.DURATION_BETWEEN, 'year']]: date => `/api/fixed-data`
};

export default (chartType, interval, date) => urlTable[[chartType, interval]](date);