import {IconButton, TextareaAutosize} from "@mui/material";
import EditIcon from "@mui/icons-material/Edit";
import DeleteIcon from "@mui/icons-material/Delete";
import SaveIcon from "@mui/icons-material/Save";
import React from "react";
import DefaultMetadaField
    from "@src/js/components/common/imaging/components/gallery/DefaultMetadaField.js";

const CommentMetadataField = ({keyProp, valueProp, idx, pos, onEditComment}) => {
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
        onEditComment(editableValue);
    }

    if (keyProp === 'comment') {
        return (
            <p key={'metadata-comment-' + idx + '-' + pos}>
                <IconButton aria-label="edit" size="small" color="primary"
                            onClick={toggleEditMode}>
                    <EditIcon/>
                </IconButton>
                {/*<IconButton aria-label="delete" size="small" color="primary">
                    <DeleteIcon/>
                </IconButton>*/}
                <IconButton aria-label="save" size="small" disabled={!editMode}
                            color="primary" onClick={saveComment}>
                    <SaveIcon/>
                </IconButton>
                <strong> {keyProp}:</strong>
                {editMode ? <TextareaAutosize aria-label="empty textarea"
                                              placeholder="Add a comment"
                                              value={editableValue}
                                              onChange={event => setEditableValue(event.target.value)}/>
                    : JSON.stringify(valueProp)}

            </p>
        )
    } else {
        return <DefaultMetadaField key={'metadata-'+keyProp+'-' + idx + '-' + pos} keyProp={keyProp} valueProp={valueProp}/>
    }
}

export default CommentMetadataField;