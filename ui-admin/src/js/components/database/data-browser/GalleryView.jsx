import React from "react";
import { withStyles } from "@material-ui/core/styles";

const styles = theme => ({
    containerDefault: {
        padding: `${theme.spacing(1)}px ${theme.spacing(2)}px`
    },
    containerSquare: {
        padding: `${theme.spacing(2)}px ${theme.spacing(2)}px`
    }
})

class GalleryView extends React.Component {
    render() {
        return (
            <div>GalleryBrowser</div>
        )
    }
}

export default withStyles(styles)(GalleryView)
