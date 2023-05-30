import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import GridViewItem from "@src/js/components/database/data-browser/GridViewItem.jsx";
import Container from "@src/js/components/common/form/Container.jsx";

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
            <Container className={classes.container}>
                {files.map((file) => <GridViewItem file={file} configuration={configuration}/>)}
            </Container>
        )
    }
}

export default withStyles(styles)(GridView)
