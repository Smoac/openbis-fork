import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import autoBind from 'auto-bind'
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
    autoBind(this)
    this.state = {
      viewType: props.viewType,
      files: [
        {
          name: 'Processed',
          folder: true,
          size: 0,
          creationTime: new Date('2020-08-13 14:45:54.034563'),
          lastModifiedTime: new Date('2022-02-24 04:35:21.486930'),
          lastAccessTime: new Date('2023-05-25 14:55:31.902857')
        },
        {
          name: 'Text.txt',
          folder: false,
          size: 21432,
          creationTime: new Date('2020-08-13 14:45:54.034563'),
          lastModifiedTime: new Date('2022-02-24 04:35:21.486930'),
          lastAccessTime: new Date('2023-05-25 14:55:31.902857')
        },
        {
          name: 'Movie.mp4',
          folder: false,
          size: 2143243443537,
          creationTime: new Date('2020-08-13 14:45:54.034563'),
          lastModifiedTime: new Date('2022-02-24 04:35:21.486930'),
          lastAccessTime: new Date('2023-05-25 14:55:31.902857')
        },
        {
          name: 'Music.mp3',
          folder: false,
          size: 21432443,
          creationTime: new Date('2020-08-13 14:45:54.034563'),
          lastModifiedTime: new Date('2022-02-24 04:35:21.486930'),
          lastAccessTime: new Date('2023-05-25 14:55:31.902857')
        },
        {
          name: 'Image.png',
          folder: false,
          size: 214323234,
          creationTime: new Date('2020-08-13 14:45:54.034563'),
          lastModifiedTime: new Date('2022-02-24 04:35:21.486930'),
          lastAccessTime: new Date('2023-05-25 14:55:31.902857')
        }
      ]
    }
  }

  handleViewTypeChange(viewType) {
    this.setState({ viewType })
  }

  render() {
    const { viewType, files } = this.state
    const { classes } = this.props

    return (
      <div className={classes.boundary}>
        <Toolbar viewType={viewType} onViewTypeChange={this.handleViewTypeChange} />
        {viewType === 'list' && <ListView files={files} />}
        {viewType === 'grid' && <GridView files={files} />}
      </div>
    )
  }
}

export default withStyles(styles)(DataBrowser)
