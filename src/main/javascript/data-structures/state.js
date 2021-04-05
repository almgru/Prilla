import * as dayjs from "dayjs";
import * as isoWeek from "dayjs/plugin/isoWeek";

import TimeSpan from './time-span';

dayjs.extend(isoWeek);

export default class State {
    constructor() {
        this._chartType = null;
        this._onStateChanged = null;
        this._timeSpan = TimeSpan.WEEK;
        this._defaultDate = dayjs().startOf('week');
        this._date = dayjs(this._defaultDate);
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
        this._date = dayjs(this._defaultDate);
        this._timeSpan = timeSpan;
        this._onStateChanged(this);
    }

    set onStateChanged(callback) {
        this._onStateChanged = callback;
    }

    next = () => {
        this._date = this._date.add(1, this._timeSpan);
        this._onStateChanged(this);
    }

    previous = () => {
        this._date = this._date.subtract(1, this._timeSpan);
        this._onStateChanged(this);
    }
}