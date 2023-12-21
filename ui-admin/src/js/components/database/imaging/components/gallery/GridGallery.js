import * as React from "react";
import {ImageList, ImageListItem, Switch} from "@material-ui/core";
import PaperBox from "@src/js/components/database/imaging/components/common/PaperBox";
import Grid from "@material-ui/core/Grid";
import BlankImage from "@src/js/components/database/imaging/components/common/BlankImage";
import FormControlLabel from "@material-ui/core/FormControlLabel";
import {makeStyles} from "@material-ui/core/styles";

const useStyles = makeStyles((theme) => ({
    root: {
        display: 'flex',
        flexWrap: 'wrap',
        justifyContent: 'space-around',
        overflow: 'hidden',
        backgroundColor: theme.palette.background.paper,
    },
    imageList: {
        width: '100%',
        /*height: 800,*/
    },
    paperFullHeight: {
        height: '90%'
    },
    imgFullWidth: {
        width: '100%',
        height: 'unset'
    },
    pageSize: {
        display: 'none'
    }
}));

const GridGallery = ({ previews }) => {
    const classes = useStyles();
    const [showAll, setShowAll] = React.useState(true);

    return (
        <ImageList className={classes.imageList} cols={zoom}>
            {previews.map(preview => (
                    <ImageListItem style={{height: 'unset'}} key={item.img}>
                        <PaperBox className={classes.paperFullHeight}>
                            <Grid container justifyContent='space-around'>
                                <Grid container item alignItems="flex-start">
                                    {preview.bytes ? <img alt={""}
                                                          className={classes.imgFullWidth}
                                                          src={`data:image/${image.previews[0].format};base64,${image.previews[0].bytes}`}
                                    /> : <BlankImage className={classes.imgFullWidth}/>}
                                </Grid>
                                <Grid container item alignItems="flex-end">
                                    <FormControlLabel
                                        control={<Switch size="small" defaultChecked />}
                                        label="Show"
                                        labelPlacement="start"
                                    />
                                    <FormControlLabel
                                        control={<Switch size="small" defaultChecked />}
                                        label="Select"
                                        labelPlacement="start"
                                    />
                                </Grid>
                            </Grid>
                        </PaperBox>
                    </ImageListItem>
                ))}
        </ImageList>
    );
}

export default GridGallery;
