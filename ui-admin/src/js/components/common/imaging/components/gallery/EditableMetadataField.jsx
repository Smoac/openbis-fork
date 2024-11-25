import React from "react";
import {IconButton, TextareaAutosize} from "@mui/material";
import EditIcon from "@mui/icons-material/Edit";
import DeleteIcon from "@mui/icons-material/Delete";
import SaveIcon from "@mui/icons-material/Save";

const EditableMetadataField = ({ keyProp, valueProp, idx, onEdit }) => {
	const [editMode, setEditMode] = React.useState(false);
	const [editableValue, setEditableValue] = React.useState("");

	React.useEffect(() => {
		setEditableValue(valueProp);
	}, [])

	const toggleEditMode = () => {
		setEditMode(!editMode);
	}

	const saveComment = () => {
		setEditMode(false);
		onEdit(editableValue);
	}

	return (
		<p key={'metadata-' + keyProp + '-' + idx}>
			<IconButton aria-label="edit" size="small" color="primary"
				onClick={toggleEditMode}>
				<EditIcon />
			</IconButton>
			{/*<IconButton aria-label="delete" size="small" color="primary">
            <DeleteIcon/>
        </IconButton>*/}
			<IconButton aria-label="save" size="small" disabled={!editMode}
				color="primary" onClick={saveComment}>
				<SaveIcon />
			</IconButton>
			<strong> {keyProp}: </strong>
			{editMode ? <TextareaAutosize aria-label="empty textarea"
				placeholder="Add a comment"
				value={editableValue}
				onChange={event => setEditableValue(event.target.value)} />
				: JSON.stringify(valueProp)}

		</p>
	)
}

export default EditableMetadataField;