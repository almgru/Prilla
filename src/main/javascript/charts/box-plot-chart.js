import { create } from "d3-selection";

export default (data, config) => {
    return create('svg').node();
};