import * as React from "react";
import {
    CardActionArea,
    CardActions,
    CardMedia,
    ImageList,
    ImageListItem,
    Grid,
    Card,
    Checkbox,
    FormControlLabel
} from "@material-ui/core";
import {makeStyles} from "@material-ui/core/styles";
import constants from "@src/js/components/common/imaging/constants.js";
import CustomSwitch from "@src/js/components/common/imaging/components/common/CustomSwitch.jsx";

const useStyles = makeStyles((theme) => ({
    card: {
        margin: '5px',
    },
    imageList: {
        width: '100%',
        /*height: 800,*/
    },
    content: {
        flex: '1 0 auto',
        alignSelf: 'center',
    },
    imgFullWidth: {
        width: '100%',
        height: 'unset'
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
        <ImageList className={classes.imageList} cols={cols} gap={5}>
            {previewContainerList.map((previewContainer, idx) => (
                <ImageListItem style={{height: 'unset'}} key={`image-grid-item-${idx}`}>
                    <Card className={classes.card}>
                        <CardActionArea>
                            <CardMedia component="img"
                                       alt={""}
                                       src={previewContainer.preview.bytes ? `data:image/${previewContainer.preview.format};base64,${previewContainer.preview.bytes}` : constants.BLANK_IMG_SRC}
                                       onClick={() => onOpenPreview(previewContainer.datasetId)}
                            />
                        </CardActionArea>
                        {selectAll && <CardActions className={classes.content}>
                            <Grid container alignItems={"center"}
                                  justifyContent={"space-evenly"}>
                                <Grid item>
                                    <CustomSwitch
                                        size="small"
                                        label="Show"
                                        labelPlacement="start"
                                        isChecked={previewContainer.preview.show}
                                        onChange={() => handleShowPreview(previewContainer)}/>
                                </Grid>
                                {previewContainer.preview.bytes &&
                                    <Grid item>
                                        <FormControlLabel
                                            value="start"
                                            control={<Checkbox value={previewContainer.select}
                                                               onChange={() => handleSelectPreview(idx)}
                                                               color="primary"/>}
                                            label="Export"
                                            labelPlacement="start"/>
                                    </Grid>
                                }
                            </Grid>
                        </CardActions>}
                    </Card>
                </ImageListItem>
            ))}
        </ImageList>
    );
}

export default GalleryGridView;
