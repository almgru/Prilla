import { quantileSorted, median } from 'd3-array';
import flow from 'lodash.flow';

import ChartType from '../data-structures/chart-type';

const castConsumptionData = data => data.map(o => ({
    label: o.label,
    value: Number(o.value)
}));

const castDurationData = data => data.map(o => ({
    label: o.label,
    values: o.values.map(e => Number(e))
}));

const transformDurationData = flow(
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
            const sorted = d.values.sort();
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

const transformationTable = {
    [ChartType.CONSUMPTION]: castConsumptionData,
    [ChartType.DURATION]: transformDurationData,
    [ChartType.DURATION_BETWEEN]: transformDurationData
};

export default (charttype, data) => transformationTable[charttype](data);