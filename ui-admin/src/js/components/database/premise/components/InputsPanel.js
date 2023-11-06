import * as React from 'react';
import Dropdown from "@src/js/components/database/premise/common/Dropdown";
import Slider from "@src/js/components/database/premise/common/InputSlider";
import RangeSlider from "@src/js/components/database/premise/common/InputRangeSlider";
import Grid from "@material-ui/core/Grid";
import {FormGroup, Switch} from "@material-ui/core";
import FormControlLabel from "@material-ui/core/FormControlLabel";
import OutlinedBox from "@src/js/components/database/premise/common/OutlinedBox";


const Components = {
    Dropdown,
    Slider,
    RangeSlider
}

const InputsPanel = ({ extendedConfig, prevConfigValues, onConfigChange }) => {
    const [show, setShow] = React.useState(true);
    const [currentPreviewConfig, setCurrentPreviewConfig] = React.useState(prevConfigValues);
    const [currentExtendedConfig, setCurrentExtendedConfig] = React.useState(extendedConfig);

    //console.log('InputsPanel = ', prevConfigValues);
    const handleSliderOnChange = (name, value) => {
        //console.log(name, value);
        let newConfig = {...currentPreviewConfig};
        newConfig[name] = value;
        //setCurrentPreviewConfig(newConfig);
        onConfigChange(newConfig);
         //console.log("handleOnChange - ", newConfig);
    };

    const handleDropdownOnChange = (event) => {
        //console.log(event);
        let newConfig = {...currentPreviewConfig}
        newConfig[event.target.name] = event.target.value;
        //setCurrentPreviewConfig(newConfig);
        onConfigChange(newConfig);
        //console.log("handleDropdownOnChange - ", newConfig);
    }

    /*    const listInputsComp = extendedConfig.map((c, idx) => {
        //console.log(panelConfig, initConfig);
        switch (c.type) {
            case 'Dropdown':
                return <Dropdown key={idx} label={c.label} initValue={c.initValue} values={c.values} isMulti={c.multiselect} onSelectChange={handleDropdownOnChange} />
            case 'Slider':
                return <Slider key={idx} label={c.label} initValue={c.initValue} range={c.range} onChange={handleSliderOnChange}/>
            case 'RangeSlider':
                return <RangeSlider key={idx} label={c.label} initValue={c.initValue} range={c.range} onChange={handleSliderOnChange}/>
        }
    });*/

    const listInputsComponents = extendedConfig.map((c, idx) => {
        //console.log(c.label, prevConfigValues[c.label]);
        let Component = Components[c.type];
        return (<Component key={"inputpanel-"+c.type +"-"+ idx}
                           label={c.label}
                           initValue={prevConfigValues[c.label]}
                           values={c.values}
                           isMulti={c.multiselect}
                           onSelectChange={handleDropdownOnChange}
                           range={c.range}
                           onChange={handleSliderOnChange}/>);
    });

    return (
        <Grid item xs={12} >
            <OutlinedBox style={{width:'fit-content'}} label="Show">
                <Switch checked={show} onChange={() => setShow(!show)} color="primary"  />
            </OutlinedBox>
            {listInputsComponents}
        </Grid>
    );
}

export default InputsPanel;