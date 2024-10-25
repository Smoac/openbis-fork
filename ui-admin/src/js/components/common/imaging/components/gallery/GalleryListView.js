import React from "react";
import {
    CardActionArea,
    CardContent,
    CardMedia,
    ImageList,
    ImageListItem,
    Typography,
    Card, Divider
} from "@mui/material";
import makeStyles from '@mui/styles/makeStyles';
import constants from "@src/js/components/common/imaging/constants.js";
import {isObjectEmpty} from "@src/js/components/common/imaging/utils.js";
import CommentMetadataField from "@src/js/components/common/imaging/components/gallery/CommentMetadataField.jsx";
import DefaultMetadaField
    from "@src/js/components/common/imaging/components/gallery/DefaultMetadaField.js";

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

const GalleryListView = ({previewContainerList, onOpenPreview, onEditComment}) => {
    const classes = useStyles();

    return (
        (<ImageList className={classes.imageList} cols={1} gap={5}>
            {previewContainerList.map((previewContainer, idx) => (
                <ImageListItem style={{height: 'unset'}} key={'image-list-item-' + idx}>
                    <Card className={classes.card} key={'card-list-item-' + idx}>
                        <CardActionArea style={{width: 'unset'}}>
                            <CardMedia component="img"
                                       alt={""}
                                       src={previewContainer.preview.bytes ? `data:image/${previewContainer.preview.format};base64,${previewContainer.preview.bytes}` : constants.BLANK_IMG_SRC}
                                       onClick={() => onOpenPreview(previewContainer.datasetId)}
                            />
                        </CardActionArea>
                        <CardContent className={classes.content}>
                            <Typography key={`metadata-datasetid-${idx}`} gutterBottom variant="h5">
                                Data Set ID - {previewContainer.datasetId}
                            </Typography>
                            <Divider/>
                            <Typography key={`dataset-types-header-${idx}`} gutterBottom variant="h6">
                                Data Set Types
                            </Typography>
                            <Typography key={`dataset-types-${idx}`} variant="body2"
                                        component={'span'} sx={{
                                color: "textSecondary"
                            }}>
                                {isObjectEmpty(previewContainer.datasetProperties) ?
                                    <p>No Property to display</p>
                                    : Object.entries(previewContainer.datasetProperties).map(([key, value], pos) =>
                                        <DefaultMetadaField key={'property-' + idx + '-' + pos} keyProp={key} valueProp={value} idx={idx} pos={pos}/>
                                        /*<p key={'property-' + idx + '-' + pos}>
                                            <strong>{key}:</strong> {value}</p>*/)
                                }
                            </Typography>
                            <Divider/>
                            <Typography key={`preview-metadata-header-${idx}`} gutterBottom variant="h6">
                                Preview Metadata
                            </Typography>
                            <Typography key={`preview-metadata-${idx}`} variant="body2"
                                        component={'span'} sx={{
                                color: "textSecondary"
                            }}>
                                {isObjectEmpty(previewContainer.preview.metadata) ?
                                    <p>No Preview metadata to display</p>
                                    : Object.entries(previewContainer.preview.metadata)
                                        .map(([key, value], pos) =>
                                            <CommentMetadataField key={key}
                                                                  keyProp={key}
                                                                  valueProp={value}
                                                                  pos={pos}
                                                                  idx={idx}
                                                                  onEditComment={newVal => onEditComment(newVal, previewContainer, idx)}/>
                                        )
                                }
                            </Typography>
                        </CardContent>
                    </Card>
                </ImageListItem>
            ))}
        </ImageList>)
    );
}

export default GalleryListView;