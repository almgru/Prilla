import * as dayjs from "dayjs";
import * as isoWeek from "dayjs/plugin/isoWeek";

import IntervalUnit from './interval-unit';

dayjs.extend(isoWeek);

export default class State {
    constructor() {
        this._chartType = null;
        this._interval = null;
        this._changeCallback = null;
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
        this._changeCallback(this);
    }

    set interval(interval) {
        if (![ 'week', 'month', 'year' ].includes(interval)) {
            throw new Error('Invalid interval');
        }

        this._date = dayjs(this._defaultDate);
        this._interval = interval;
        this._changeCallback(this);
    }

    set onChangedCallback(onChangedCallback) {
        this._changeCallback = onChangedCallback;
    }

    set date(intervalUnit) {
        if (intervalUnit === IntervalUnit.NEXT) {
            this._date = this._date.add(1, this._interval);
        } else if (intervalUnit === IntervalUnit.PREVIOUS) {
            this._date = this._date.subtract(1, this._interval);
        }
        this._changeCallback(this);
    }
}