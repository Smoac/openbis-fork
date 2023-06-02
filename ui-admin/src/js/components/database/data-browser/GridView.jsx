import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import GridViewItem from '@src/js/components/database/data-browser/GridViewItem.jsx'
import Grid from '@material-ui/core/Grid'
import Paper from '@material-ui/core/Paper'

const styles = (theme) => ({
    container: {
        fontFamily: theme.typography.fontFamily,
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fill, minmax(8rem, 1fr))',
        gridGap: '0.5rem'
    }
})

class GridView extends React.Component {

    render() {
        const { classes, configuration, files } = this.props
        return (
            <Grid container component={Paper} className={classes.container}>
                {files.map((file) => <GridViewItem file={file} configuration={configuration}/>)}
            </Grid>
        )
    }
}

export default withStyles(styles)(GridView)
