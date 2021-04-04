import * as dayjs from "dayjs";
import * as isoWeek from "dayjs/plugin/isoWeek";

dayjs.extend(isoWeek);

export default class State {
    constructor() {
        this._chartType = null;
        this._interval = null;
        this._onStateChanged = null;
        this._defaultDate = dayjs().startOf('week');
        this._date = dayjs(this._defaultDate);
    }

    get chartType() {
        return this._chartType;
    }

    get interval() {
        return this._interval;
    }

    get date() {
        return this._date;
    }

    set chartType(chartType) {
        this._chartType = chartType;
        this._interval = 'week';
        this._onStateChanged(this);
    }

    set interval(interval) {
        this._date = dayjs(this._defaultDate);
        this._interval = interval;
        this._onStateChanged(this);
    }

    set onStateChanged(callback) {
        this._onStateChanged = callback;
    }

    next = () => {
        this._date = this._date.add(1, this._interval);
        this._onStateChanged(this);
    }

    previous = () => {
        this._date = this._date.subtract(1, this._interval);
        this._onStateChanged(this);
    }
}