import * as React from 'react';
import {CardContent, Grid, Typography} from '@material-ui/core';
import {makeStyles} from "@material-ui/core/styles";
import Card from "@material-ui/core/Card";

const useStyles = makeStyles({
    root: {
        maxWidth: 250
    },
    spaceAround: {
        justifyContent: 'space-around'
    },
});
const MetadataViewer = ({configMetadata, previews}) => {
    const classes = useStyles();

    return (
        <Grid container spacing={2} direction="column" >
            <Grid item sx={{ textAlign: 'left' }}>
                <h1>Config Metadata section</h1>
                <Card className={classes.root}>
                    <CardContent>
                        {Object.entries(configMetadata).map(([key, value], idx) => <Typography variant="body2" component="p" key={idx}><strong>{key}:</strong> {value}</Typography>)}
                    </CardContent>
                </Card>

            </Grid>
            <Grid item sx={{ textAlign: 'left' }} >
                <h1>Image section</h1>
            </Grid>
            <Grid item container className={classes.spaceAround} >
                {previews.map(preview =>
                    <Grid key={'preview_md_'+preview.previewIdx} item sx={{ textAlign: 'left' }}>
                        <Card className={classes.root}>
                            <Typography gutterBottom variant="h5" component="h2">
                                Preview {preview.previewIdx}
                            </Typography>
                            <CardContent>
                                {Object.entries(preview.metadata).map(([key, value], idx) => <Typography variant="body2" component="p" key={idx}><strong>{key}:</strong> {value}</Typography>)}
                            </CardContent>
                        </Card>
                    </Grid>
                )}

            </Grid>
        </Grid>
    );
}

export default MetadataViewer;