import { quantileSorted, median } from 'd3-array';
import flow from 'lodash.flow';
import dayjs from 'dayjs';
import isoWeek from 'dayjs/plugin/isoWeek';
import isoWeeksInYear from 'dayjs/plugin/isoWeeksInYear';
import isLeapYear from 'dayjs/plugin/isLeapYear';
dayjs.extend(isoWeek);
dayjs.extend(isoWeeksInYear);
dayjs.extend(isLeapYear);

import ChartType from '../data-structures/enum/chart-type';
import TimeSpan from '../data-structures/enum/time-span';

import mapTimeSpanToObject from './time-span-to-object';

const weekOfMonth = date => {
    const weekOfYearOfFirstDayOfMonth = dayjs(date).startOf('month').isoWeek();
    const weeksInPreviousYear = date.startOf('month').subtract(1, 'day').isoWeeksInYear();
    const currentWeek = date.isoWeek();
    return 1 + (weekOfYearOfFirstDayOfMonth === weeksInPreviousYear
        ? (currentWeek + weekOfYearOfFirstDayOfMonth) % weeksInPreviousYear
        : currentWeek - weekOfYearOfFirstDayOfMonth);
};

const formatDate = (timeSpan, date) => {
    switch (timeSpan) {
        case TimeSpan.WEEK:
            return date.format('YYYY-MM-DD');

        case TimeSpan.MONTH:
            return `${date.format('MM')}-W${weekOfMonth(date)}`

        case TimeSpan.YEAR:
            return date.format('YY-MM');
    }
};

const fillInGaps = (state, data, val) => {
    const timeSpanObj = mapTimeSpanToObject(state.timeSpan);
    const end = state.date.add(1, timeSpanObj.span);
    let date = dayjs(state.date);

    while (date.isBefore(end)) {
        const key = formatDate(state.timeSpan, date);

        if (!data.hasOwnProperty(key)) {
            data[key] = val;
        }

        date = date.add(1, timeSpanObj.step);
    }

    return data;
}

const compareDateStr = (ds1, ds2) => (
    Number(ds1.replace(/\D/g, '')) - Number(ds2.replace(/\D/g, ''))
);

const flattenMap = data => (
    Object.entries(data)
        .sort((la, ra) => compareDateStr(la[0], ra[0]))
        .reduce(
            (arr, e) => ([
                ...arr, {
                    label: e[0],
                    value: e[1]
                }
            ]),
            []
        )
);

const castConsumptionData = data => data.map(o => ({
    label: o.label,
    value: Number(o.value)
}));

const castDurationData = data => data.map(o => ({
    label: o.label,
    values: o.value.map(e => Number(e))
}));

const transformAmountData = flow(
    (state, data) => fillInGaps(state, data, 0),
    data => flattenMap(data),
    data => castConsumptionData(data)
);

const transformDurationData = flow(
    (state, data) => fillInGaps(state, data, []),
    data => flattenMap(data),
    data => castDurationData(data),
    data => ({
        labels: data.map(d => d.label),
        summary: summarizeDurationData(data),
        dots: groupIndividualDurationDataPointsByLabel(data)
    })
);

const summarizeDurationData = data => (
    data.filter(d => d.values.length > 0)
        .map(d => {
            const sorted = d.values.sort((a, b) => a - b);
            const q1 = quantileSorted(sorted, 0.25);
            const q3 = quantileSorted(sorted, 0.75);
            const iqr = q3 - q1;

            return {
                label: d.label,
                q1: q1,
                median: median(sorted),
                q3: q3,
                iqr: iqr,
                min: Math.max(0, q1 - 1.5 * iqr),
                max: q3 + 1.5 * iqr
            }
        }
    )
);

const groupIndividualDurationDataPointsByLabel = data => (
    data.reduce(
        (acc, obj) => acc.concat(obj.values.map(val => ({
            label: obj.label,
            value: Number(val)})),
        ),
        []
    )
);

export default (state, data) => {
    switch (state.chartType) {
        case ChartType.CONSUMPTION:
            return transformAmountData(state, data);

        case ChartType.DURATION:
        case ChartType.DURATION_BETWEEN:
            return transformDurationData(state, data);

        default:
            throw new Error('Unknown chart type.');
    }
};