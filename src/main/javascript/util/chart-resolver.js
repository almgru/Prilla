import ChartType from '../data-structures/chart-type';

import barChart from '../charts/bar-chart';
import boxPlotChart from '../charts/box-plot-chart';

const chartTable = {
    [ChartType.CONSUMPTION]: barChart,
    [ChartType.DURATION]: boxPlotChart,
    [ChartType.DURATION_BETWEEN]: boxPlotChart
};

export default (chartType, data, interval, date, config) => chartTable[chartType](data, interval, date, config);