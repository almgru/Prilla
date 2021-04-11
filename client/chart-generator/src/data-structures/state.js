import dayjs from 'dayjs';
import isoWeek from 'dayjs/plugin/isoWeek';
dayjs.extend(isoWeek);

import TimeSpan from './enum/time-span';
import mapTimeSpanToObject from '../mapper/time-span-to-object';

export default class State {
    constructor() {
        this._chartType = null;
        this._onStateChanged = null;
        this._timeSpan = TimeSpan.WEEK;
        this._date = dayjs().startOf(mapTimeSpanToObject(this._timeSpan).start);
    }

    get chartType() {
        return this._chartType;
    }

    get timeSpan() {
        return this._timeSpan;
    }

    get date() {
        return this._date;
    }

    set chartType(chartType) {
        this._chartType = chartType;
        this._onStateChanged(this);
    }

    set timeSpan(timeSpan) {
        this._date = dayjs().startOf(mapTimeSpanToObject(timeSpan).start);
        this._timeSpan = timeSpan;
        this._onStateChanged(this);
    }

    set onStateChanged(callback) {
        this._onStateChanged = callback;
    }

    next = () => {
        this._date = this._date.add(1, mapTimeSpanToObject(this._timeSpan).span);
        this._onStateChanged(this);
    }

    previous = () => {
        this._date = this._date.subtract(1, mapTimeSpanToObject(this._timeSpan).span);
        this._onStateChanged(this);
    }
}