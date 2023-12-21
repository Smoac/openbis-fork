import * as React from 'react';
import Dropdown from "@src/js/components/database/imaging/components/common/Dropdown";
import Slider from "@src/js/components/database/imaging/components/common/InputSlider";
import Range from "@src/js/components/database/imaging/components/common/InputRangeSlider";
import Grid from "@material-ui/core/Grid";
import { Switch} from "@material-ui/core";
import OutlinedBox from "@src/js/components/database/imaging/components/common/OutlinedBox";
import ColorMap from "@src/js/components/database/imaging/components/viewer/ColorMap";


const Components = {
    Dropdown,
    Slider,
    Range,
    ColorMap
}

const InputsPanel = ({ inputsConfig, prevConfigValues, onConfigChange }) => {
    const [show, setShow] = React.useState(true);

    /*React.useEffect(() => {
        const listInputsComponents = inputsConfig.map((c, idx) => {
            if (c.visibility) {
                for (const condition of c.visibility){
                    if(condition.values.includes(prevConfigValues[condition.label])){
                        c.range = condition.range;
                    }
                }
            }
            let Component = Components[c.type];
            return (<Component key={"InputsPanel-"+c.type +"-"+ idx}
                               label={c.label}
                               initValue={prevConfigValues[c.label]}
                               values={c.values}
                               isMulti={c.multiselect}
                               onSelectChange={(event) => onConfigChange(event.target.name, event.target.value)}
                               range={c.range}
                               playable={c.playable}
                               speeds={c.speeds}
                               onChange={(name, value, update) => onConfigChange(name, value, update)}/>);

        });
        setComponents(listInputsComponents);
    }, [prevConfigValues]);*/
    //console.log('InputsPanel = ', prevConfigValues);

    /*const listInputsComp = inputsConfig.map((c, idx) => {
        //console.log(panelConfig, initConfig);
        switch (c.type) {
            case 'Dropdown':
                return <Dropdown key={"inputpanel-"+c.type +"-"+ idx}
                                 label={c.label}
                                 initValue={prevConfigValues[c.label]}
                                 values={c.values}
                                 isMulti={c.multiselect}
                                 onSelectChange={(event) => onConfigChange(event.target.name, event.target.value)} />
            case 'Slider':
                return <Slider key={"inputpanel-"+c.type +"-"+ idx}
                               label={c.label}
                               initValue={prevConfigValues[c.label]}
                               range={c.range}
                               playable={c.playable}
                               speeds={c.speeds}
                               onChange={(name, value, update) => onConfigChange(name, value, update)}/>
            case 'RangeSlider':
                return <RangeSlider key={"inputpanel-"+c.type +"-"+ idx}
                                    label={c.label}
                                    initValue={prevConfigValues[c.label]}
                                    range={c.range}
                                    playable={c.playable}
                                    speeds={c.speeds}
                                    onChange={(name, value, update) => onConfigChange(name, value, update)}/>
        }
    });*/


    const listInputsComponents = inputsConfig.map((c, idx) => {
        if (c.visibility) {
            for (const condition of c.visibility){
                if(condition.values.includes(prevConfigValues[condition.label])){
                    c.range = condition.range;
                    c.unit = condition.unit;
                }
            }
        }
        let Component = Components[c.type];
        return (<Component key={"InputsPanel-"+c.type +"-"+ idx}
                           label={c.label}
                           initValue={prevConfigValues[c.label]}
                           values={c.values}
                           isMulti={c.multiselect}
                           onSelectChange={(event) => onConfigChange(event.target.name, event.target.value)}
                           range={c.range}
                           unit={c.unit}
                           playable={c.playable}
                           speeds={c.speeds}
                           onChange={(name, value, update) => onConfigChange(name, value, update)}/>);

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