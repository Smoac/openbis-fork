import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import FolderIcon from "@material-ui/icons/FolderOpen";
import FileIcon from "@material-ui/icons/InsertDriveFileOutlined";
import autoBind from "auto-bind";

const styles = theme => ({
    icon: {
        verticalAlign: 'middle',
        fontSize: '4rem'
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
            <div>
                <>{this.getIcon(file)} {file.name}</>
            </div>
        )
    }
}

export default withStyles(styles)(GridViewItem)
