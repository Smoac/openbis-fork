import * as React from "react";
import {
    CardActionArea,
    CardActions,
    CardMedia,
    ImageList,
    ImageListItem,
    Grid2,
    Card,
    Checkbox,
    FormControlLabel
} from "@mui/material";
import makeStyles from '@mui/styles/makeStyles';
import constants from "@src/js/components/common/imaging/constants.js";
import CustomSwitch from "@src/js/components/common/imaging/components/common/CustomSwitch.jsx";

const useStyles = makeStyles((theme) => ({
    card: {
        margin: '5px',
    },
    content: {
        flex: '1 0 auto',
        alignSelf: 'center',
        justifyContent: 'space-evenly'
    }
}));

const GalleryGridView = ({
    previewContainerList,
    cols,
    selectAll,
    onOpenPreview,
    handleShowPreview,
    handleSelectPreview
}) => {
    const classes = useStyles();

    return (
        <ImageList sx={{ width: '100%', height: '800px' }} cols={cols} gap={5}>
            {previewContainerList.map((previewContainer, idx) => (
                <ImageListItem style={{ height: 'unset' }} key={`image-grid-item-${idx}`}>
                    <Card className={classes.card}>
                        <CardActionArea>
                            <CardMedia component="img"
                                alt={""}
                                src={previewContainer.preview.bytes ? `data:image/${previewContainer.preview.format};base64,${previewContainer.preview.bytes}` : constants.BLANK_IMG_SRC}
                                onClick={() => onOpenPreview(previewContainer.datasetId)}
                            />
                        </CardActionArea>
                        {selectAll && <CardActions className={classes.content}>
                            <Grid2 container>
                                <Grid2 sx={{ alignContent: 'center' }}>
                                    <CustomSwitch
                                        size="small"
                                        label="Show"
                                        labelPlacement="start"
                                        isChecked={previewContainer.preview.show}
                                        onChange={() => handleShowPreview(previewContainer)} />
                                </Grid2>
                                {previewContainer.preview.bytes &&
                                    <Grid2 sx={{ alignContent: 'center' }}>
                                        <FormControlLabel
                                            value="start"
                                            control={<Checkbox value={previewContainer.select}
                                                onChange={() => handleSelectPreview(idx)}
                                                color="primary" />}
                                            label="Export"
                                            labelPlacement="start" />
                                    </Grid2>
                                }
                            </Grid2>
                        </CardActions>}
                    </Card>
                </ImageListItem>
            ))}
        </ImageList>
    );
}

export default GalleryGridView;
