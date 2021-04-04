import 'core-js/stable'; // General polyfill
import 'regenerator-runtime/runtime'; // Polyfill for async/await
import 'whatwg-fetch'; // Polyfill for fetch

import config from './config';

import State from './data-structures/state';
import ChartType from './data-structures/chart-type';
import IntervalUnit from './data-structures/interval-unit';

import urlResolver from './util/url-resolver';
import dataCaster from './util/data-caster';
import chartResolver from './util/chart-resolver';

window.onload = () => {
    const state = new State();
    setupListeners(state);
    state.chartType = ChartType.CONSUMPTION;
};

const resolveUrl = state => urlResolver(state.chartType, state.interval, state.date);

const handleResponse = response => {
    if (!response.ok) {
        throw new Error(`Response not OK. Status code: ${response.status}`);
    }

    return response.json()
}

const castData = (state, data) => dataCaster(state.chartType, data);

const generateChart = (data, state, config) => (
    chartResolver(state.chartType, data, state.interval, state.date, config)
);

const appendChart = chart => {
    document.querySelector('#chart-area').innerHTML = '';
    document.querySelector('#chart-area').appendChild(chart);
};

const setupListeners = state => {
    document.querySelector('#nextBtn').onclick = () => state.date = IntervalUnit.NEXT;
    document.querySelector('#prevBtn').onclick = () => state.date = IntervalUnit.PREVIOUS;
    document.querySelector('#rangeWeekBtn').onclick = () => state.interval = 'week';
    document.querySelector('#rangeMonthBtn').onclick = () => state.interval = 'month';
    document.querySelector('#rangeYearBtn').onclick = () => state.interval = 'year';
    document.querySelector('#chartConsBtn').onclick = () => state.chartType = ChartType.CONSUMPTION;
    document.querySelector('#chartDurBtn').onclick = () => state.chartType = ChartType.DURATION;
    document.querySelector('#chartBtwnBtn').onclick = () => state.chartType = ChartType.DURATION_BETWEEN;

    state.onChangedCallback = state => (
        fetch(resolveUrl(state))
            .then(handleResponse)
            .then(data => castData(state, data))
            .then(data => generateChart(data, state, config))
            .then(appendChart)
            .catch(err => console.log(err))
    );
};
