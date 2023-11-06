import * as React from 'react';
import { Grid } from '@material-ui/core';

const MetadataViewer = ({configMetadata, previews}) => {
    return (
        <Grid container spacing={2} direction="column" >
            <Grid item sx={{ textAlign: 'left' }}>
                <h1>Config Metadata section</h1>
                {Object.entries(configMetadata).map(([key, value], idx) => <p key={idx}><strong>{key}:</strong> {value}</p>)}
            </Grid>
            <Grid item sx={{ textAlign: 'left' }} >
                <h1>Image section</h1>
            </Grid>
            <Grid item container sx={{ justifyContent: 'space-between' }} >
                {previews.map(preview =>
                    <Grid key={'preview_md_'+preview.previewIdx} item sx={{ textAlign: 'left' }}>
                        <h3>Preview {preview.previewIdx}</h3>
                        {Object.entries(preview.metadata).map(([key, value], idx) => <p key={idx}><strong>{key}:</strong> {value}</p>)}
                    </Grid>
                )}

            </Grid>
        </Grid>
    );
}

export default MetadataViewer;