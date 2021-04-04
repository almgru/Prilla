import ChartType from '../data-structures/chart-type';

const transformationTable = {
    [ChartType.CONSUMPTION]: data => data.map(o => ({
        label: o.label,
        value: Number(o.value)
    })),
    [ChartType.DURATION]: data => data.map(o => ({
        label: o.label,
        values: o.values.map(e => Number(e))
    })),
    [ChartType.DURATION_BETWEEN]: data => data.map(o => ({
        label: o.label,
        values: o.values.map(e => Number(e))
    }))
};

export default (charttype, data) => transformationTable[charttype](data);