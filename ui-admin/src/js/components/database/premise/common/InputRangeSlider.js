import * as React from 'react';
import Player from "@src/js/components/database/premise/common/Player";
import Grid from '@material-ui/core/Grid';
import Slider from "@material-ui/core/Slider";
import { Input } from "@material-ui/core";
import OutlinedBox from "@src/js/components/database/premise/common/OutlinedBox";
import InputAdornment from "@material-ui/core/InputAdornment";

const InputRangeSlider = ({ label, range, initValue = null, playable, speeds, disabled = false, onChange, unit = null}) => {
    const min = Number(range[0]);
    const max = Number(range[1]);
    const step = Number(range[2]);
    const arrayRange = Array.from(
        { length: (max - min) / step + 1 },
        (value, index) => min + index * step
    );
    const [value, setValue] = React.useState(initValue == null ? [min,max] : initValue);

    function roundToClosest(counts, goal){
        return counts.reduce((prev, curr) => Math.abs(curr - goal) < Math.abs(prev - goal) ? curr : prev);
    }

    const handleSliderChange = (newValue, name) => {
        setValue(newValue);
        onChange(name, newValue);
    };

    const handleInputMinChange = (event) => {
        let newValue = event.target.value === '' ? [value[0], value[1]] : [Number(event.target.value), value[1]];
        setValue(newValue);
        onChange(event.target.name, newValue);
    };

    const handleInputMaxChange = (event) => {
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
                        value={initValue == null ? min : initValue[0]}
                        size="small"
                        onChange={handleInputMinChange}
                        onBlur={handleBlur}
                        disabled={disabled}
                        endAdornment={unit && <InputAdornment position="end">{unit}</InputAdornment>}
                        inputProps={{
                            step: step,
                            min: min,
                            max: max,
                            type: 'number'
                        }}
                    />
                </Grid>
                <Grid item xs>
                    <Slider
                        value={initValue == null ? [min,max] : initValue}
                        name={label}
                        disabled={disabled}
                        onChange={(event, newValue) => handleSliderChange(newValue, label)}
                        min={min}
                        max={max}
                        step={step}
                    />
                </Grid>
                <Grid item xs>
                    <Input
                        name={label}
                        disabled={disabled}
                        value={initValue == null ? max : initValue[1]}
                        size="small"
                        onChange={handleInputMaxChange}
                        onBlur={handleBlur}
                        endAdornment={unit && <InputAdornment position="end">{unit}</InputAdornment>}
                        inputProps={{
                            step: step,
                            min: min,
                            max: max,
                            type: 'number',
                        }}
                    />
                </Grid>
            </Grid>
            {playable && <Player steps={arrayRange} speeds={speeds} speedable={playable}
                     onStep={() => console.log('DEFAULT onStep InputRangeSlider!')}/>}
        </OutlinedBox>
    );
}

export default InputRangeSlider;