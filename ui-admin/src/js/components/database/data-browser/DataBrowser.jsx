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
import InfoPanel from '@src/js/components/database/data-browser/InfoPanel.jsx'

const HTTP_SERVER_URI = '/data-store-server'

const styles = theme => ({
  boundary: {
    padding: theme.spacing(1),
    borderColor: theme.palette.border.secondary,
    backgroundColor: theme.palette.background.paper,
    height: '100%'
  },
  icon: {
    fontSize: '4rem',
  },
  flexContainer: {
    display: 'flex',
    '&>*': {
      flex: '0 0 auto',
      padding: theme.spacing(1),
      borderWidth: '1px',
      borderStyle: 'solid',
      borderColor: theme.palette.border.secondary,
      backgroundColor: theme.palette.background.paper
    },
  },
  container: {
    flexGrow: '1',
  }
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
    this.datastoreServer = new DataStoreServer(
      'http://localhost:8085',
      HTTP_SERVER_URI
    )

    this.state = {
      viewType: props.viewType,
      files: [],
      selectedFile: null,
      multiselectedFiles: new Set([]),
      showInfo: false
    }
  }
  handleViewTypeChange(viewType) {
    this.setState({ viewType })
  }

  handleClick(file) {
    // TODO: implement
  }

  handleSelect(selectedRow) {
    this.setState({ selectedFile: selectedRow && selectedRow.data })
  }

  handleMultiselect(file) {
    // TODO: implement
  }

  async login() {
    return new Promise((resolve, reject) => {
      this.datastoreServer.login('admin', 'changeit', token => {
        if (token) {
          resolve(token)
        } else {
          reject('Could not perform login.')
        }
      })
    })
  }

  async listFiles() {
    return new Promise((resolve, reject) => {
      this.datastoreServer.list('demo-sample', '', 'true', (data) => {
        if (!data.error) {
          const results = data.result[1]
          const files = results.map(result => result[1])
          resolve(files)
        } else {
          reject(data.error)
        }
      })
    })
  }

  async load() {
    await this.login()
    const files = await this.listFiles()
    this.setState({ files })
    return await files.map(file => ({ id: file.name, ...file }))
  }

  async onError(error) {
    await AppController.getInstance().errorChange(error)
  }

  handleShowInfoChange() {
    this.setState({ showInfo: !this.state.showInfo })
  }

  render() {
    const { classes } = this.props
    const { viewType, files, selectedFile, multiselectedFiles, showInfo } =
      this.state

    return (
      <Paper className={classes.boundary}>
        <Toolbar
          viewType={viewType}
          onViewTypeChange={this.handleViewTypeChange}
          onShowInfoChange={this.handleShowInfoChange}
          showInfo={showInfo}
        />
        <div className={[classes.flexContainer, classes.boundary].join(' ')}>
          {viewType === 'list' && (
            <Grid
              // id={id}
              // settingsId={id}
              filterModes={[GridFilterOptions.COLUMN_FILTERS]}
              header='Files'
              classes={{ container: classes.container }}
              columns={[
                {
                  name: 'name',
                  label: 'Name',
                  sortable: true,
                  getValue: ({ row }) => row.name,
                  renderValue: ({ row }) => (
                    <>
                      <ItemIcon
                        file={row}
                        classes={{ icon: classes.icon }}
                        configuration={configuration}
                      />{' '}
                      {row.name}
                    </>
                  ),
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
                }
              ]}
              loadRows={this.load}
              sort='registrationDate'
              sortDirection='desc'
              exportable={false}
              selectable={true}
              multiselectable={true}
              loadSettings={null}
              onSettingsChange={null}
              onError={this.onError}
              onSelectedRowChange={this.handleSelect}
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
              selectedFile={selectedFile}
              multiselectedFiles={multiselectedFiles}
            />
          )}
          {showInfo && selectedFile && (
            <InfoPanel file={selectedFile} configuration={configuration} />
          )}
        </div>
      </Paper>
    )
  }
}

export default withStyles(styles)(DataBrowser)
