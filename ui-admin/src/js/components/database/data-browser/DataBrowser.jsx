import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Toolbar from '@src/js/components/database/data-browser/Toolbar.jsx'
import ListView from '@src/js/components/database/data-browser/ListView.jsx'
import GridView from '@src/js/components/database/data-browser/GridView.jsx'

const styles = theme => ({
  containerDefault: {
    padding: `${theme.spacing(1)}px ${theme.spacing(2)}px`
  },
  containerSquare: {
    padding: `${theme.spacing(2)}px ${theme.spacing(2)}px`
  },
  boundary: {
    padding: theme.spacing(1),
    borderWidth: '2px',
    borderStyle: 'solid',
    borderColor: theme.palette.border.secondary,
    backgroundColor: theme.palette.background.paper
  }
})

class DataBrowser extends React.Component {


  constructor(props, context) {
    super(props, context)
    this.state = {
      viewType: props.viewType
    }
  }

  render() {
    const { viewType } = this.state
    const { classes } = this.props

    return (
      <div className={ classes.boundary }>
        <Toolbar viewType={viewType} />
        {viewType === 'list' ? <ListView /> : null}
        {viewType === 'grid' ? <GridView /> : null}
      </div>
    )
  }
}

export default withStyles(styles)(DataBrowser)
