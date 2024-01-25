import React from "react";
import {
    CardActionArea,
    CardContent,
    CardMedia,
    ImageList,
    ImageListItem,
    TextareaAutosize,
    Typography,
    Card,

} from "@material-ui/core";
import {makeStyles} from "@material-ui/core/styles";
import constants from "@src/js/components/database/imaging/constants.js";
import {isObjectEmpty} from "@src/js/components/database/imaging/utils.js";
import {stringify} from "csv-stringify";

const useStyles = makeStyles((theme) => ({
    card: {
        margin: '5px',
        display: 'flex',
        flexDirection: 'row',
    },
    content: {
        flex: '1 0 auto',
        alignSelf: 'center',
    },
    imageList: {
        width: '100%',
    },
    imgFixedWidth: {
        height: '350px',
    }
}));

const GalleryListView = ({previewContainerList, onOpenPreview}) => {
    const classes = useStyles();

    return (
        <ImageList className={classes.imageList} cols={1} gap={5}>
            {previewContainerList.map((previewContainer, idx) => (
                <ImageListItem style={{height: 'unset'}} key={'image-list-item-'+idx}>
                    <Card className={classes.card} key={'card-list-item-' + idx}>
                        <CardActionArea style={{width: 'unset'}}>
                            <CardMedia component="img"
                                       alt={""}
                                       className={classes.imgFixedWidth}
                                       src={previewContainer.preview.bytes ? `data:image/${previewContainer.preview.format};base64,${previewContainer.preview.bytes}` : constants.BLANK_IMG_SRC}
                                       onClick={() => onOpenPreview(previewContainer.datasetId)}
                            />
                        </CardActionArea>
                        <CardContent className={classes.content}>
                            <Typography key={`metadata-datasetid-${idx}`} gutterBottom variant="h5">
                                Metadata - {previewContainer.datasetId}
                            </Typography>
                            <Typography key={`dataset-properties-${idx}`} variant="body2" color="textSecondary" component={'span'}>
                                {isObjectEmpty(previewContainer.datasetProperties) ?
                                    <p>No Property to display</p>
                                    : Object.entries(previewContainer.datasetProperties).map(([key, value], pos) =>
                                        <p key={'property-'+idx+'-'+pos}><strong>{key}:</strong> {value}</p>)
                                }
                            </Typography>
                            <Typography key={`preview-metadata-${idx}`} variant="body2" color="textSecondary" component={'span'}>
                                {isObjectEmpty(previewContainer.preview.metadata) ?
                                    <p>No Preview metadata to display</p>
                                    : Object.entries(previewContainer.preview.metadata).map(([key, value], pos) => {
                                        console.log([key, value], pos);
                                        return (< p
                                        key = {'metadata-'+idx + '-' + pos} > < strong > {key}
                                    :</strong>
                                        {
                                            JSON.stringify(value)
                                        }
                                    </p>)
                                    })
                                }
                            </Typography>
                            <Typography key={`preview-comments-${idx}`} gutterBottom variant="h6">
                                Comments:
                            </Typography>
                            <TextareaAutosize aria-label="empty textarea"
                                              placeholder="TODO: missing comment field in data model"/>
                        </CardContent>
                    </Card>
                </ImageListItem>
            ))}
        </ImageList>
    );
}

export default GalleryListView;