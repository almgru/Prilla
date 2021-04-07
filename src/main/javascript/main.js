import 'core-js/stable'; // General polyfill
import 'regenerator-runtime/runtime'; // Polyfill for async/await
import 'whatwg-fetch'; // Polyfill for fetch

import config from './config';

import State from './data-structures/state';
import ChartType from './data-structures/enum/chart-type';
import TimeSpan from './data-structures/enum/time-span';

import stateToApiRequestUrl from './mapper/state-to-api-request-mapper';
import dataTransformer from './mapper/data-transformer';
import chartTypeToChartMapper from './mapper/chart-type-to-chart-mapper';
import * as labelMapper from './mapper/label-mapper';

window.onload = () => {
    const state = new State();
    setupClickListeners(state);

    state.onStateChanged = state => (
        fetch(stateToApiRequestUrl(state))
            .then(response => handleResponse(response))
            .then(data => dataTransformer(state, data))
            .then(data => generateChart(data, state, config))
            .then(chart => appendChart(chart))
            .catch(err => console.log(err))
    );

    state.chartType = ChartType.CONSUMPTION;
};

const setupClickListeners = state => {
    document.querySelector('#nextBtn').onclick = state.next;
    document.querySelector('#prevBtn').onclick = state.previous;
    document.querySelector('#rangeWeekBtn').onclick = () => state.timeSpan = TimeSpan.WEEK;
    document.querySelector('#rangeMonthBtn').onclick = () => state.timeSpan = TimeSpan.MONTH;
    document.querySelector('#rangeYearBtn').onclick = () => state.timeSpan = TimeSpan.YEAR;
    document.querySelector('#chartConsBtn').onclick = () => state.chartType = ChartType.CONSUMPTION;
    document.querySelector('#chartDurBtn').onclick = () => state.chartType = ChartType.DURATION;
    document.querySelector('#chartBtwnBtn').onclick = () => state.chartType = ChartType.DURATION_BETWEEN;
};

const handleResponse = response => {
    if (!response.ok) {
        throw new Error(`Error received from API server. Status code: ${response.status}`);
    }

    return response.json()
}

const generateChart = (data, state, config) => {
    const chart = chartTypeToChartMapper(state.chartType);
    return chart(data, {
        ...config,
        labels: {
            x: labelMapper.mapXLabel(state.chartType, state.timeSpan, state.date),
            y: labelMapper.mapYLabel(state.chartType, state.timeSpan, state.date)
        }
    });
};

const appendChart = chart => {
    document.querySelector('#chart-area').innerHTML = '';
    document.querySelector('#chart-area').appendChild(chart);
};
