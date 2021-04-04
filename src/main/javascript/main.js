import 'core-js/stable'; // General polyfill
import 'regenerator-runtime/runtime'; // Polyfill for async/await
import 'whatwg-fetch'; // Polyfill for fetch

import config from './config';

import State from './data-structures/state';
import ChartType from './data-structures/chart-type';
import Interval from './data-structures/interval';

import chartTypeToApiUrlMapper from './util/chart-type-to-api-url-mapper';
import dataTransformer from './util/data-transformer';
import chartTypeToChartMapper from './util/chart-type-to-chart-mapper';

window.onload = () => {
    const state = new State();
    setupClickListeners(state);

    state.onStateChanged = state => (
        fetch(mapChartTypeToApiUrl(state))
            .then(handleResponse)
            .then(data => dataTransformer(state.chartType, data))
            .then(data => generateChart(data, state, config))
            .then(appendChart)
            .catch(err => console.log(err))
    );

    state.chartType = ChartType.CONSUMPTION;
};

const setupClickListeners = state => {
    document.querySelector('#nextBtn').onclick = state.next;
    document.querySelector('#prevBtn').onclick = state.previous;
    document.querySelector('#rangeWeekBtn').onclick = () => state.interval = Interval.WEEK;
    document.querySelector('#rangeMonthBtn').onclick = () => state.interval = Interval.MONTH;
    document.querySelector('#rangeYearBtn').onclick = () => state.interval = Interval.YEAR;
    document.querySelector('#chartConsBtn').onclick = () => state.chartType = ChartType.CONSUMPTION;
    document.querySelector('#chartDurBtn').onclick = () => state.chartType = ChartType.DURATION;
    document.querySelector('#chartBtwnBtn').onclick = () => state.chartType = ChartType.DURATION_BETWEEN;
};

const mapChartTypeToApiUrl = state => chartTypeToApiUrlMapper(state.chartType, state.interval, state.date);

const handleResponse = response => {
    if (!response.ok) {
        throw new Error(`Error received from API server. Status code: ${response.status}`);
    }

    return response.json()
}

const generateChart = (data, state, config) => {
    const chart = chartTypeToChartMapper(state.chartType);
    return chart(data, state.interval, state.date, config);
};

const appendChart = chart => {
    document.querySelector('#chart-area').innerHTML = '';
    document.querySelector('#chart-area').appendChild(chart);
};
