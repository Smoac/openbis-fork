import React from 'react';
import {FormControl, MenuItem, Select} from "@material-ui/core";
import OutlinedBox from "@src/js/components/common/imaging/components/common/OutlinedBox";
import constants from '@src/js/components/common/imaging/constants.js';

const ColorItem = ({colorMapValue}) => {
    return (
        <span style={{background: `linear-gradient(90deg, ${constants.DEFAULT_COLORMAP[colorMapValue]})`, width: '70%', height: '15px', marginLeft:'10px'}} />
    )
}

const ColorMap = ({values, initValue, label, disabled = false, onSelectChange=null}) => {
    const [value, setValue] = React.useState(initValue);

    React.useEffect(() => {
        //console.log("useEffect DROPDOWN: ", label, values, initValue, isMulti);
        if (initValue !== value)
            setValue(initValue);
    }, [initValue]);

    const handleChange = (event) => {
        setValue(event.target.value);
        if (onSelectChange != null) {
            onSelectChange(event);
        }
    };

    return (
        <OutlinedBox label={label}>
            <FormControl fullWidth >
                <Select
                    labelId={"select-" + label + "-label"}
                    disabled={disabled}
                    id={"select-" + label}
                    value={value}
                    multiple={false}
                    label={label}
                    name={label}
                    onChange={handleChange}
                >
                    {values.map((v, i) => <MenuItem key={"select-" + label + "-menuitem-" + i} value={v}>
                        <span style={{width: '30%'}}>{v}</span> <ColorItem colorMapValue={v} />
                    </MenuItem>)}
                </Select>
            </FormControl>
        </OutlinedBox>
    )
}

export default ColorMap;