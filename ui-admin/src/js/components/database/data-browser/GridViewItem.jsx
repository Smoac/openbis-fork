import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import FolderIcon from '@material-ui/icons/FolderOpen'
import FileIcon from '@material-ui/icons/InsertDriveFileOutlined'
import Grid from '@material-ui/core/Grid'
import autoBind from 'auto-bind'
import Card from "@material-ui/core/Card";
import { CardContent, CardMedia } from "@material-ui/core";

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
    icon: {
        verticalAlign: 'middle',
        fontSize: '6rem'
    },
})

class GridViewItem extends React.Component {

    constructor(props, context) {
        super(props, context)
        autoBind(this)

        const { configuration } = this.props

        this.extensionToIconType = new Map(
          configuration.flatMap(
            (configObject) => configObject.extensions.map(extension => [extension, configObject.icon])
          )
        )
    }

    // TODO: move out this method in favour of using a utility method from DataBrowser for example
    getIcon(file) {
        const { classes } = this.props

        if (file.folder) {
            return <FolderIcon className={classes.icon} />
        } else {
            const iconType = this.extensionToIconType.get(file.name.substring(file.name.lastIndexOf(".") + 1))
            return iconType ? React.createElement(iconType, { className: classes.icon }) : <FileIcon className={classes.icon} />
        }
    }

    render() {
        const { classes, file } = this.props

        return (
            <Grid item component={Card} variant="outlined" className={classes.cell}>
                <CardMedia>{this.getIcon(file)}</CardMedia>
                <CardContent>
                 {file.name}
                </CardContent>
            </Grid>
        )
    }
}

export default withStyles(styles)(GridViewItem)
