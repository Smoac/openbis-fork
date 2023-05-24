import React from "react";
import Toolbar from "@src/js/components/database/data-browser/Toolbar";
import ListBrowser from "@src/js/components/database/data-browser/ListBrowser";
import GalleryBrowser from "@src/js/components/database/data-browser/GalleryBrowser";

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
        const { view } = this.state;

        return (
            <div>
                <Toolbar/>
                {view === "list" ? <ListBrowser/> : null}
                {view === "gallery" ? <GalleryBrowser/> : null}
            </div>
        )
    }
}

// export default withStyles(styles)(DataBrowser)
export default DataBrowser
