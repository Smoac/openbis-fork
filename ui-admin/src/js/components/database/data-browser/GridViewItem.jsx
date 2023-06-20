import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Grid from '@material-ui/core/Grid'
import Card from "@material-ui/core/Card";
import { CardContent, CardMedia } from "@material-ui/core";
import ItemIcon from '@src/js/components/database/data-browser/ItemIcon.jsx'

const styles = (theme) => ({
    cell: {
        display: 'block',
        position: 'relative',
        width: '8rem',
        height: '8rem',
        overflow: 'hidden',
        margin: '0.25rem',
        textAlign: 'center',
        cursor: 'pointer',
        '&:hover': {
            backgroundColor: '#0000000a'
        },
    },
})

class GridViewItem extends React.Component {

    render() {
        const { classes, file, configuration } = this.props

        return (
            <Grid item component={Card} variant="outlined" className={classes.cell}>
                <CardMedia>
                    <ItemIcon file={file} configuration={configuration} />
                </CardMedia>
                <CardContent>
                    {file.name}
                </CardContent>
            </Grid>
        )
    }
}

export default withStyles(styles)(GridViewItem)
