import * as React from 'react';
import Dropdown from "@src/js/components/database/premise/common/Dropdown";
import Slider from "@src/js/components/database/premise/common/InputSlider";
import RangeSlider from "@src/js/components/database/premise/common/InputRangeSlider";
import Grid from "@material-ui/core/Grid";
import { Switch} from "@material-ui/core";
import OutlinedBox from "@src/js/components/database/premise/common/OutlinedBox";


const Components = {
    Dropdown,
    Slider,
    RangeSlider
}

const InputsPanel = ({ inputsConfig, prevConfigValues, onConfigChange }) => {
    const [show, setShow] = React.useState(true);

    console.log('InputsPanel = ', prevConfigValues);

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

    const listInputsComponents = inputsConfig.map((c, idx) => {
        //console.log(c.label, prevConfigValues[c.label]);
        let Component = Components[c.type];
        return (<Component key={"inputpanel-"+c.type +"-"+ idx}
                           label={c.label}
                           initValue={prevConfigValues[c.label]}
                           values={c.values}
                           isMulti={c.multiselect}
                           onSelectChange={(event) => onConfigChange(event.target.name, event.target.value)}
                           range={c.range}
                           onChange={(name, value) => onConfigChange(name, value)}/>);
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