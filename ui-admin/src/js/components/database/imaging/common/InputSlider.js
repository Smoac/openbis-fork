import * as React from 'react';
import Grid from "@material-ui/core/Grid";
import Slider from "@material-ui/core/Slider";
import Input from "@material-ui/core/Input";
import Player from "@src/js/components/database/imaging/common/Player";
import OutlinedBox from "@src/js/components/database/imaging/common/OutlinedBox";
import InputAdornment from "@material-ui/core/InputAdornment";


const InputSlider = ({ label, range, initValue, playable, speeds, disabled = false, onChange, unit= null }) => {
    const min = Number(range[0])
    const max = Number(range[1])
    const step = Number(range[2])
    const arrayRange = Array.from(
        { length: (max - min) / step + 1 },
        (value, index) => min + index * step
    );
    const [value, setValue] = React.useState(initValue == null ? min : Number(initValue));

    function roundToClosest(counts, goal) {
        return counts.reduce((prev, curr) => Math.abs(curr - goal) < Math.abs(prev - goal) ? curr : prev);
    }

    const handleSliderChange = (newValue, name, update) => {
        setValue(newValue);
        onChange(name, [newValue], update);
    };

    const handleInputChange = (event) => {
        let newValue = event.target.value === '' ? 0 : Number(event.target.value);
        setValue(newValue);
        onChange(event.target.name, [newValue]);
    };

    const handleBlur = (event) => {
        let newValue = value;
        if (value < min) {
            newValue = min;
        } else if (value > max) {
            newValue = max;
        } else if (!arrayRange.includes(value)) {
            newValue = roundToClosest(arrayRange, value);
        }
        setValue(newValue);
        onChange(event.target.name, [newValue]);
    };

    return (
        <OutlinedBox label={label}>
            <Grid container spacing={2} alignItems="center" direction="row" sx={{ mb: 1, px: 1 }}>
                <Grid item xs>
                    <Slider
                        value={initValue == null ? min : Number(initValue)}
                        name={label}
                        onChange={(event, newValue)=> handleSliderChange(newValue, label, false)}
                        min={min}
                        max={max}
                        step={step}
                        disabled={disabled}
                    />
                </Grid>
                <Grid item xs>
                    <Input
                        value={initValue == null ? min : Number(initValue)}
                        size="small"
                        name={label}
                        onChange={handleInputChange}
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
                {playable &&
                    (<Grid item xs>
                        <Player label={label} onStep={handleSliderChange} steps={arrayRange} speeds={speeds} speedable={playable} />
                    </Grid>)
                }
            </Grid>
        </OutlinedBox>
    );
}

export default InputSlider;