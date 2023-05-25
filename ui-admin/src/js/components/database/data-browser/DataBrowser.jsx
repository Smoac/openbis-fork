import React from "react";
import { withStyles } from "@material-ui/core/styles";
import Toolbar from "@src/js/components/database/data-browser/Toolbar.jsx";
import ListView from "@src/js/components/database/data-browser/ListView.jsx";
import GalleryView from "@src/js/components/database/data-browser/GalleryView.jsx";

const styles = theme => ({
    containerDefault: {
        padding: `${theme.spacing(1)}px ${theme.spacing(2)}px`
    },
    containerSquare: {
        padding: `${theme.spacing(2)}px ${theme.spacing(2)}px`
    }
})

class DataBrowser extends React.Component {
    render() {
        const { view } = this.props;

        return (
            <div>
                <Toolbar/>
                {view === "list" ? <ListView/> : null}
                {view === "gallery" ? <GalleryView/> : null}
            </div>
        )
    }
}

export default withStyles(styles)(DataBrowser)
