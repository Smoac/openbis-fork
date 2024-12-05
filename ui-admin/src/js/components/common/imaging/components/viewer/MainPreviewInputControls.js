import React from 'react'
import { Divider, Grid2, TextField, Autocomplete, Checkbox } from "@mui/material";
import { inRange, isObjectEmpty } from "@src/js/components/common/imaging/utils.js";
import PaperBox from "@src/js/components/common/imaging/components/common/PaperBox.js";
import Dropdown from "@src/js/components/common/imaging/components/common/Dropdown.jsx";
import OutlinedBox from "@src/js/components/common/imaging/components/common/OutlinedBox.js";
import InputSlider from "@src/js/components/common/imaging/components/common/InputSlider.jsx";
import InputRangeSlider
	from "@src/js/components/common/imaging/components/common/InputRangeSlider.jsx";
import ColorMap from "@src/js/components/common/imaging/components/viewer/ColorMap.jsx";
import constants from "@src/js/components/common/imaging/constants.js";
import CustomSwitch from "@src/js/components/common/imaging/components/common/CustomSwitch.jsx";
import RefreshIcon from "@mui/icons-material/Refresh";
import CheckBoxOutlineBlankIcon from '@mui/icons-material/CheckBoxOutlineBlank';
import CheckBoxIcon from '@mui/icons-material/CheckBox';
import messages from '@src/js/common/messages.js'
import Message from '@src/js/components/common/form/Message.jsx'
import Button from '@src/js/components/common/form/Button.jsx'
import makeStyles from '@mui/styles/makeStyles';

const useStyles = makeStyles((theme) => ({
	noBorderNoShadow: {
		border: 'unset',
		boxShadow: 'none',
	}
}));

const MainPreviewInputControls = ({ activePreview, configInputs, configResolutions, resolution, isChanged, onClickUpdate, onChangeShow, onSelectChangeRes, onChangeActConf, imagingTags, handleTagImage}) => {
	const classes = useStyles();
	const [tags, setTags] = React.useState([])
    const [inputValue, setInputValue] = React.useState('');


    React.useEffect(() => {
        if (activePreview) {
            var trasformedTags = []
            for (const activePreviewTag of activePreview.tags) {
                const matchTag = imagingTags.find(imagingTag => imagingTag.value === activePreviewTag);
                trasformedTags.push(matchTag);
            }
            setTags(trasformedTags);
            setInputValue(trasformedTags.join(', '));
        }
    }, [activePreview])

    const handleTagsChange = (event, newTags) => {
		setTags(newTags);
        const tagsArray = newTags.map(tag => tag.value);
        handleTagImage(false, tagsArray);
    }

	const createInitValues = (inputsConfig, activeConfig) => {
		const isActiveConfig = isObjectEmpty(activeConfig);
		return Object.fromEntries(inputsConfig.map(input => {
			switch (input.type) {
				case constants.DROPDOWN:
					if (!isActiveConfig)
						return [input.label, activeConfig[input.label] ? activeConfig[input.label] : input.multiselect ? [input.values[0]] : input.values[0]];
					else
						return [input.label, input.multiselect ? [input.values[0]] : input.values[0]];
				case constants.SLIDER:
					if (!isActiveConfig) {
						if (input.visibility) {
							for (const condition of input.visibility) {
								if (condition.values.includes(activeConfig[condition.label])) {
									input.range = condition.range;
									input.unit = condition.unit;
								}
							}
						}
						return [input.label, activeConfig[input.label] ? activeConfig[input.label] : input.range[0]];
					} else {
						if (input.visibility) {
							input.range = input.visibility[0].range[0];
						}
						return [input.label, input.range[0]];
					}
				case constants.RANGE:
					if (!isActiveConfig) {
						if (input.visibility) {
							for (const condition of input.visibility) {
								if (condition.values.includes(activeConfig[condition.label])) {
									input.range = condition.range;
									input.unit = condition.unit;
								}
							}
						}
						let rangeInitValue = [inRange(activeConfig[input.label][0], input.range[0], input.range[1]) ? activeConfig[input.label][0] : input.range[0],
						inRange(activeConfig[input.label][1], input.range[0], input.range[1]) ? activeConfig[input.label][1] : input.range[1]]
						return [input.label, rangeInitValue];
					} else {
						if (input.visibility) {
							return [input.label, [input.visibility[0].range[0], input.visibility[0].range[1]]];
						}
						return [input.label, [input.range[0], input.range[1]]];
					}
				case constants.COLORMAP:
					if (!isActiveConfig)
						return [input.label, activeConfig[input.label] ? activeConfig[input.label] : input.values[0]];
					else
						return [input.label, input.values[0]];
			}
		}));
	};

	const inputValues = createInitValues(configInputs, activePreview.config);
	activePreview.config = inputValues;
	const currentMetadata = activePreview.metadata;
	const isUploadedPreview = isObjectEmpty(currentMetadata) ? false : ("file" in currentMetadata);
	return (
		<Grid2 size={{ xs: 12, sm: 4 }}>
			<PaperBox className={classes.noBorderNoShadow}>
				<Grid2 container sx={{ justifyContent: "space-between", alignItems: "center" }}>
					<Button label={messages.get(messages.UPDATE)}
						variant='outlined'
						color='primary'
						startIcon={<RefreshIcon />}
						onClick={onClickUpdate}
						disabled={!isChanged || isUploadedPreview} />

					{isChanged && !isUploadedPreview && (
						<Message type='info'>
							{messages.get(messages.UPDATE_CHANGES)}
						</Message>
					)}

					<OutlinedBox style={{ width: 'fit-content' }}
						label={messages.get(messages.SHOW)}>
						<CustomSwitch isChecked={activePreview.show}
							onChange={onChangeShow} />
					</OutlinedBox>

					<Dropdown onSelectChange={onSelectChangeRes}
						label={messages.get(messages.RESOLUTIONS)}
						values={configResolutions}
						initValue={resolution.join('x')} />

					<OutlinedBox label="Imaging Tags">
						<Autocomplete
							multiple
							id="tags-outlined"
							options={imagingTags}
							disableCloseOnSelect
							getOptionLabel={(option) => option.label}
							inputValue={inputValue}
							value={tags}
							onInputChange={(event, newInputValue) => {
								setInputValue(newInputValue);
							}}
							renderInput={(params) => (
								<TextField variant='standard' {...params} placeholder="Search Tag"/>
							)}
							renderOption={(props, option, { selected }) => {
								const { key, ...optionProps } = props;
								return (
									<li key={key} {...optionProps}>
										<Checkbox
											icon={<CheckBoxOutlineBlankIcon fontSize="small" />}
											checkedIcon={<CheckBoxIcon fontSize="small" />}
											style={{ marginRight: 8 }}
											checked={selected}
										/>
										{option.label}
									</li>
								);
							}}
							onChange={handleTagsChange}
						/>
					</OutlinedBox>
				</Grid2>

				<Divider sx={{ margin: '16px 8px 16px 8px', borderWidth: '1px'}}/> 

				{configInputs.map((c, idx) => {
					switch (c.type) {
						case constants.DROPDOWN:
							return <Dropdown key={`InputsPanel-${c.type}-${idx}`}
								label={c.label}
								initValue={inputValues[c.label]}
								values={c.values}
								isMulti={c.multiselect}
								disabled={isUploadedPreview}
								onSelectChange={(event) => onChangeActConf(event.target.name, event.target.value)} />;
						case constants.SLIDER:
							return <InputSlider key={`InputsPanel-${c.type}-${idx}`}
								label={c.label}
								initValue={inputValues[c.label]}
								range={c.range}
								unit={c.unit}
								playable={c.playable && !isUploadedPreview}
								speeds={c.speeds}
								disabled={isUploadedPreview}
								onChange={(name, value, update) => onChangeActConf(name, value, update)} />;
						case constants.RANGE:
							return <InputRangeSlider key={`InputsPanel-${c.type}-${idx}`}
								label={c.label}
								initValue={inputValues[c.label]}
								range={c.range}
								disabled={isUploadedPreview || c.range.findIndex(n => n === 'nan') !== -1}
								unit={c.unit}
								playable={c.playable && !isUploadedPreview}
								speeds={c.speeds}
								onChange={(name, value, update) => onChangeActConf(name, value, update)} />;
						case constants.COLORMAP:
							return <ColorMap key={`InputsPanel-${c.type}-${idx}`}
								values={c.values}
								disabled={isUploadedPreview}
								initValue={inputValues[c.label]}
								label={c.label}
								onSelectChange={(event) => onChangeActConf(event.target.name, event.target.value)} />;
					}
				})
				}
			</PaperBox>
		</Grid2>
	);
};

export default MainPreviewInputControls;