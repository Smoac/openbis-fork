import * as React from 'react';
import Player from "@src/js/components/database/premise/common/Player";
import {makeStyles} from "@material-ui/core/styles";
import Grid from '@material-ui/core/Grid';
import Slider from "@material-ui/core/Slider";
import { OutlinedInput, Input } from "@material-ui/core";
import OutlinedBox from "@src/js/components/database/premise/common/OutlinedBox";
import * as PropTypes from "prop-types";
import InputAdornment from "@material-ui/core/InputAdornment";

const useStyles = makeStyles({
    input: {
        width: 42,
    },
});

const InputRangeSlider = ({ label, range, initValue, playable, speeds, onChange}) => {
    const min = range[0]
    const max = range[1]
    const step = range[2]
    const arrayRange = Array.from(
        { length: (max - min) / step + 1 },
        (value, index) => min + index * step
    );

    const [value, setValue] = React.useState(initValue == null ? [min,max] : initValue);

    //console.log("InputSlider: ", label, initValue, value);
    /*React.useEffect(() => {
        if (initValue !== value) {
            setValue(initValue);
        }
    }, [initValue]);*/

    function roundToClosest(counts, goal){
        return counts.reduce((prev, curr) => Math.abs(curr - goal) < Math.abs(prev - goal) ? curr : prev);
    }

    const handleSliderChange = (newValue, name) => {
        setValue(newValue);
        onChange(name, newValue);
    };

    const handleInputMinChange = (event) => {
        //console.log('handleInputMinChange:', event.target);
        let newValue = event.target.value === '' ? [value[0], value[1]] : [Number(event.target.value), value[1]];
        setValue(newValue);
        onChange(event.target.name, newValue);
    };

    const handleInputMaxChange = (event) => {
        //console.log('handleInputMaxChange:', event.target);
        let newValue = event.target.value === '' ? [value[0], value[1]] : [value[0], Number(event.target.value)];
        setValue(newValue);
        onChange(event.target.name, newValue);
    };

    const handleBlur = (event) => {
        let newValue = value;
        if (value[0] < min) {
            newValue = [min, value[1]];
        } else if (value[1] > max) {
            newValue = [value[0], max];
        } else if (!arrayRange.includes(value[0])){
            newValue = [roundToClosest(arrayRange, value[0]), value[1]]
        } else if (!arrayRange.includes(value[1])){
            newValue = [value[0], roundToClosest(arrayRange, value[1])]
        }
        setValue(newValue);
        onChange(event.target.name, newValue);
    };

    return (
        <OutlinedBox label={label}>
            <Grid container spacing={2} alignItems="center" direction="row">
                <Grid item >
                    <Input
                        name={label}
                        value={initValue == null ? [min,max] : initValue[0]}
                        size="small"
                        onChange={handleInputMinChange}
                        onBlur={handleBlur}
                        inputProps={{
                            step: step,
                            min: min,
                            max: max,
                            type: 'number',
                            'aria-label': 'weight',
                        }}
                        endAdornment={<InputAdornment position="end">Kg</InputAdornment>}
                    />
                </Grid>
                <Grid item xs>
                    <Slider
                        value={initValue == null ? [min,max] : initValue}
                        name={label}
                        onChange={(event, newValue) => handleSliderChange(newValue, label)}
                        min={min}
                        max={max}
                        step={step}
                    />
                </Grid>
                <Grid item xs>
                    <Input
                        name={label}
                        value={initValue == null ? [min,max] : initValue[1]}
                        size="small"
                        onChange={handleInputMaxChange}
                        onBlur={handleBlur}
                        inputProps={{
                            step: step,
                            min: min,
                            max: max,
                            type: 'number',
                        }}
                    />
                </Grid>
            </Grid>
            <Player />
        </OutlinedBox>
    );
}

export default InputRangeSlider;