import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import autoBind from 'auto-bind'
import Toolbar from '@src/js/components/database/data-browser/Toolbar.jsx'
import GridView from '@src/js/components/database/data-browser/GridView.jsx'
import DescriptionIcon from '@material-ui/icons/DescriptionOutlined'
import AudioIcon from '@material-ui/icons/MusicNoteOutlined'
import VideoIcon from '@material-ui/icons/LocalMovies'
import ImageIcon from '@material-ui/icons/Image'
import Paper from '@material-ui/core/Paper'
import Grid from '@src/js/components/common/grid/Grid.jsx'
import GridFilterOptions from '@src/js/components/common/grid/GridFilterOptions.js'
import AppController from '@src/js/components/AppController.js'
import ItemIcon from '@src/js/components/database/data-browser/ItemIcon.jsx'

const styles = theme => ({
  boundary: {
    padding: theme.spacing(1),
    borderWidth: '2px',
    borderStyle: 'solid',
    borderColor: theme.palette.border.secondary,
    backgroundColor: theme.palette.background.paper
  },
  icon: {
    fontSize: '4rem',
  },
})

const configuration =
  [
    {
      icon: AudioIcon,
      extensions: ['wav', 'mp3', 'acc', 'ogg']
    },
    {
      icon: DescriptionIcon,
      extensions: ['txt', 'rtf', 'doc', 'pdf']
    },
    {
      icon: VideoIcon,
      extensions: ['mp4', 'mkv', 'avi']
    },
    {
      icon: ImageIcon,
      extensions: ['tif', 'gif', 'jpg', 'jpeg', 'png']
    }
  ]

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
        },
        {
          name: 'lock',
          folder: false,
          size: 0,
          creationTime: new Date('2020-08-13 14:45:54.034563'),
          lastModifiedTime: new Date('2023-05-30 15:33:14.048038'),
          lastAccessTime: new Date('2023-05-30 15:33:14.048038')
        }
      ],
      selectedFile: null,
      multiselectedFiles: new Set([])
    }
  }

  handleViewTypeChange(viewType) {
    this.setState({ viewType })
  }

  handleClick(file) {
    // TODO: implement
  }

  handleSelect(file) {
    this.setState({selectedFile: file});
  }

  handleMultiselect(file) {
    // TODO: implement
  }

  async load(params) {
    return await this.state.files;
  }

  async onError(error) {
    await AppController.getInstance().errorChange(error)
  }

  render() {
    const { viewType, files, selectedFile, multiselectedFiles } = this.state
    const { classes } = this.props

    return (
      <Paper className={classes.boundary}>
        <Toolbar
          viewType={viewType}
          onViewTypeChange={this.handleViewTypeChange}
        />
        {viewType === 'list' && (
          <Grid
            // id={id}
            // settingsId={id}
            filterModes={[GridFilterOptions.COLUMN_FILTERS]}
            header='Files'
            columns={[
              {
                name: 'name',
                label: 'Name',
                sortable: true,
                getValue: ({ row }) => row,
                renderValue: ({ value }) => <><ItemIcon file={value} classes={classes} configuration={configuration} /> {value.name}</>,
                renderFilter: null
              },
              {
                name: 'size',
                label: 'Size',
                sortable: true,
                getValue: ({ row }) => row.size
              },
              {
                name: 'modified',
                label: 'Modified',
                sortable: false,
                getValue: ({ row }) => row.lastModifiedTime.toLocaleString()
              },
            ]}
            loadRows={this.load}
            sort='registrationDate'
            sortDirection='desc'
            exportable={false}
            selectable={true}
            loadSettings={null}
            onSettingsChange={null}
            onError={this.onError}
            exportXLS={null}
          />
        )}
        {viewType === 'grid' && (
          <GridView
            clickable={true}
            selectable={true}
            multiselectable={true}
            onClick={this.handleClick}
            onSelect={this.handleSelect}
            onMultiselect={this.handleMultiselect}
            configuration={configuration}
            files={files}
            selectedFile = {selectedFile}
            multiselectedFiles = {multiselectedFiles}
          />
        )}
      </Paper>
    )
  }
}

export default withStyles(styles)(DataBrowser)
