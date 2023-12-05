import * as React from 'react';
import {FormControl, MenuItem, Select} from "@material-ui/core";
import OutlinedBox from "@src/js/components/database/premise/common/OutlinedBox";

const Dropdown = ({ label, values, initValue, isMulti, onSelectChange = null }) => {
    const [value, setValue] = React.useState(initValue);

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
                    id={"select-" + label}
                    value={value}
                    multiple={isMulti}
                    label={label}
                    name={label}
                    onChange={handleChange}
                >
                    {values.map((v, i) => <MenuItem key={"select-" + label + "-menuitem-" + i} value={v}>{v}</MenuItem>)}
                </Select>
            </FormControl>
        </OutlinedBox>
    );
}

export default Dropdown;