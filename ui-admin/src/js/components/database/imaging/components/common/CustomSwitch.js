import React from 'react';
import Switch from '@material-ui/core/Switch';
import FormGroup from '@material-ui/core/FormGroup';
import FormControlLabel from '@material-ui/core/FormControlLabel';

export default function CustomSwitch({ label = 'default', labelPlacement = null, size = 'medium', isChecked = true, onChange, disabled = false}) {
    const [checked, setChecked] = React.useState();

    const toggleChecked = (event) => {
        setChecked(event.target.checked);
        onChange(event.target.checked);
    };

    if (labelPlacement)
        return <FormGroup>
            <FormControlLabel
                name='default-control-switch'
                control={<Switch size={size} checked={isChecked} onChange={event => toggleChecked(event)} color="primary"/>}
                disabled={disabled}
                label={label}
                labelPlacement={labelPlacement}
            />
        </FormGroup>
    else
        return <Switch disabled={disabled} name='default-switch' size={size} checked={isChecked} onChange={event => toggleChecked(event)} color="primary"/>;
}