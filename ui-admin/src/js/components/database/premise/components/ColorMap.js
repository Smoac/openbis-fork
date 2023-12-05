import React from 'react';
import {FormControl, MenuItem, Select} from "@material-ui/core";
import OutlinedBox from "@src/js/components/database/premise/common/OutlinedBox";

//https://docs.bokeh.org/en/latest/_modules/bokeh/palettes.html#inferno
const DEFAULT_COLORMAP = {
    "gray":["#000000", "#252525", "#525252", "#737373", "#969696", "#bdbdbd", "#d9d9d9", "#f0f0f0", "#ffffff"],
    "YlOrBr":["#ffffe5","#fff7bc","#fee391","#fec44f","#fe9929","#ec7014","#cc4c02","#993404","#662506"],
    "viridis":['#440154', '#472B7A', '#3B518A', '#2C718E', '#208F8C', '#27AD80', '#5BC862', '#AADB32', '#FDE724'],
    "cividis":['#00204C', '#01356E', '#404C6B', '#5F636E', '#7B7B78', '#9B9377', '#BCAE6E', '#DFCB5D', '#FFE945'],
    "inferno":['#000003', '#1F0C47', '#550F6D', '#88216A', '#BA3655', '#E35832', '#F98C09', '#F8C931', '#FCFEA4'],
    "rainbow":['#882E72', '#1965B0', '#7BAFDE', '#4EB265', '#CAE0AB', '#F7F056', '#EE8026', '#DC050C', '#72190E'],
    "Spectral":["#3288bd", "#66c2a5", "#abdda4", "#e6f598", "#ffffbf", "#fee08b", "#fdae61", "#f46d43", "#d53e4f"],
    "RdBu":["#2166ac", "#4393c3", "#92c5de", "#d1e5f0", "#f7f7f7", "#fddbc7", "#f4a582", "#d6604d", "#b2182b"],
    "RdGy":["#4d4d4d", "#878787", "#bababa", "#e0e0e0", "#ffffff", "#fddbc7", "#f4a582", "#d6604d", "#b2182b"]
}

const ColorItem = ({colorMapValue}) => {
    return (
        <span style={{background: `linear-gradient(90deg, ${DEFAULT_COLORMAP[colorMapValue]})`, width: '70%', height: '15px', marginLeft:'10px'}} />
    )
}

const ColorMap = ({values, initValue, label, onSelectChange=null}) => {
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