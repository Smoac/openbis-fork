import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import autoBind from 'auto-bind'
import Toolbar from '@src/js/components/database/data-browser/Toolbar.jsx'
import GridView from '@src/js/components/database/data-browser/GridView.jsx'
import DescriptionIcon from '@material-ui/icons/DescriptionOutlined'
import AudioIcon from '@material-ui/icons/MusicNoteOutlined'
import VideoIcon from '@material-ui/icons/LocalMovies'
import ImageIcon from '@material-ui/icons/Image'
import Grid from '@src/js/components/common/grid/Grid.jsx'
import GridFilterOptions from '@src/js/components/common/grid/GridFilterOptions.js'
import AppController from '@src/js/components/AppController.js'
import ItemIcon from '@src/js/components/database/data-browser/ItemIcon.jsx'
import InfoPanel from '@src/js/components/database/data-browser/InfoPanel.jsx'
import DataBrowserController from '@src/js/components/database/data-browser/DataBrowserController.js'
import NavigationBar from '@src/js/components/database/data-browser/NavigationBar.jsx'

const HTTP_SERVER_URI = '/data-store-server'

const styles = theme => ({
  columnFlexContainer: {
    flexDirection: 'column',
    display: 'flex',
    height: 'calc(100vh - ' + theme.spacing(12) + 'px)'
  },
  boundary: {
    padding: theme.spacing(1),
    borderColor: theme.palette.border.secondary,
    backgroundColor: theme.palette.background.paper
  },
  icon: {
    fontSize: '2rem',
    paddingRight: '0.5rem'
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
  grid: {
    flexGrow: 1,
    flex: 1,
    height: 'auto',
    overflowY: 'auto',
    paddingTop: 0,
    paddingBottom: 0
  },
  content: {
    flex: '1 1 100%',
    height: 0,
  },
  nameCell: {
    display: 'flex',
    alignItems: 'center',
    '&>span': {
      flex: 1,
      whiteSpace: 'nowrap',
      overflow: 'hidden',
      textOverflow: 'ellipsis'
    }
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

    const { sessionToken, controller } = this.props

    this.controller = controller || new DataBrowserController()
    this.controller.attach(this)
    this.datastoreServer = new DataStoreServer(
      'http://localhost:8085',
      HTTP_SERVER_URI
    )
    this.controller.setSessionToken(sessionToken)

    this.state = {
      viewType: props.viewType,
      files: [],
      selectedFile: null,
      multiselectedFiles: new Set([]),
      showInfo: false,
      path: '/'
    }
  }
  handleViewTypeChange(viewType) {
    this.setState({ viewType })
  }

  handleClick(file) {
    // TODO: implement
  }

  async handleRowDoubleClick(row) {
    const { directory, path } = row.data
    if (directory) {
      await this.setPath(path)
    }
  }

  handleSelect(selectedRow) {
    this.setState({ selectedFile: selectedRow && selectedRow.data })
  }

  handleMultiselect(selectedRow) {
    this.setState({
      multiselectedFiles: new Set(Object.values(selectedRow)
        .map(value => value.data))
    })
  }

  async onError(error) {
    await AppController.getInstance().errorChange(error)
  }

  handleShowInfoChange() {
    this.setState({ showInfo: !this.state.showInfo })
  }

  handleGridControllerRef(gridController) {
    this.controller.gridController = gridController
  }

  async handlePathChange(path) {
    await this.setPath(path)
  }

  async setPath(path) {
    if (this.state.path !== path + '/') {
      this.setState({ path: path + '/' })
      this.controller.setPath(path + '/')
      await this.controller.gridController.load()
    }
  }

  render() {
    const { classes, sessionToken } = this.props
    const {
      viewType,
      files,
      selectedFile,
      multiselectedFiles,
      showInfo,
      path
    } = this.state

    return (
      <div className={[classes.boundary, classes.columnFlexContainer].join(' ')}>
        <Toolbar
          controller={this.controller}
          viewType={viewType}
          onViewTypeChange={this.handleViewTypeChange}
          onShowInfoChange={this.handleShowInfoChange}
          showInfo={showInfo}
          selectedFile={selectedFile}
          multiselectedFiles={multiselectedFiles}
          datastoreServer={this.datastoreServer}
          sessionToken={sessionToken}
          path={path}
        />
        <NavigationBar
          path={path}
          onPathChange={this.handlePathChange}
        />
        <div className={[classes.flexContainer, classes.boundary, classes.content].join(' ')}>
          {viewType === 'list' && (
            <Grid
              id='data-browser-grid'
              controllerRef={this.handleGridControllerRef}
              filterModes={[GridFilterOptions.COLUMN_FILTERS]}
              header='Files'
              classes={{ container: classes.grid }}
              columns={[
                {
                  name: 'name',
                  label: 'Name',
                  sortable: true,
                  getValue: ({ row }) => row.name,
                  renderValue: ({ row }) => (
                    <div className={classes.nameCell}>
                      <ItemIcon
                        file={row}
                        classes={{ icon: classes.icon }}
                        configuration={configuration}
                      />
                      <span>{row.name}</span>
                    </div>
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
              loadRows={this.controller.load}
              exportable={false}
              selectable={true}
              multiselectable={true}
              loadSettings={null}
              onSettingsChange={null}
              onError={this.onError}
              onSelectedRowChange={this.handleSelect}
              onMultiselectedRowsChange={this.handleMultiselect}
              onRowDoubleClick={this.handleRowDoubleClick}
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
            <InfoPanel selectedFile={selectedFile} configuration={configuration} />
          )}
        </div>
      </div>
    )
  }
}

export default withStyles(styles)(DataBrowser)
