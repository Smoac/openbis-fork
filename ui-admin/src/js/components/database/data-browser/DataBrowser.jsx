import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import autoBind from 'auto-bind'
import Toolbar from '@src/js/components/database/data-browser/Toolbar.jsx'
import GridView from '@src/js/components/database/data-browser/GridView.jsx'

import Grid from '@src/js/components/common/grid/Grid.jsx'
import GridFilterOptions from '@src/js/components/common/grid/GridFilterOptions.js'
import AppController from '@src/js/components/AppController.js'
import ItemIcon from '@src/js/components/database/data-browser/ItemIcon.jsx'
import InfoPanel from '@src/js/components/database/data-browser/InfoPanel.jsx'
import DataBrowserController from '@src/js/components/database/data-browser/DataBrowserController.js'
import messages from '@src/js/common/messages.js'
import InfoBar from '@src/js/components/database/data-browser/InfoBar.jsx'

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
    fontSize: '1.5rem',
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
    // Coarse file formats
    {
      icon: 'file-audio',
      extensions: ['wav', 'mp3', 'acc', 'ogg', 'flac', 'm4a', 'wma', 'opus',
        'alac', 'aiff']
    },
    {
      icon: 'file-text',
      extensions: ['txt', 'rtf', 'odt', 'ods', 'odp', 'html', 'htm', 'epub',
        'md', 'tex', 'pages', 'numbers', 'key', 'mobi', 'indd', 'csv', 'tsv']
    },
    {
      icon: 'file-video',
      extensions: ['mp4', 'mkv', 'avi', 'mov', 'wmv', 'flv', 'mkv', 'webm',
        'mpeg', 'mpg', 'vob', 'm4v']
    },
    {
      icon: 'file-image',
      extensions: ['tif', 'tiff', 'gif', 'jpg', 'jpeg', 'png', 'bmp', 'svg',
        'webp', 'psd', 'raw', 'heif', 'heic']
    },
    {
      icon: 'file-archive',
      extensions:  ['zip', 'rar', '7z', 'tar', 'gz', 'bz2', 'xz', 'iso', 'zipx',
        'cab', 'arj', 'lz', 'lzma', 'z', 'tgz', 'ace', 'dmg']
    },
    {
      icon: 'file-code',
      extensions:  ['xml', 'js', 'html', 'css', 'c', 'cpp', 'cs', 'php', 'rb',
        'swift', 'go', 'rs', 'ts', 'json', 'sh', 'bat', 'sql', 'yaml', 'yml',
        'jsx', 'tsx', 'pl', 'scala', 'kt']
    },
    // Fine-grained file formats
    {
      icon: 'file-pdf',
      extensions: ['pdf']
    },
    {
      icon: 'file-word',
      extensions: ['doc', 'docx']
    },
    {
      icon: 'file-excel',
      extensions: ['xls', 'xlsx']
    },
    {
      icon: 'file-powerpoint',
      extensions: ['ppt', 'pptx']
    }
  ]

class DataBrowser extends React.Component {
  constructor(props, context) {
    super(props, context)
    autoBind(this)

    const { sessionToken, controller, id } = this.props

    this.controller = controller || new DataBrowserController(id)
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
      path: '/',
      freeSpace: -1,
      totalSpace: -1
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
    } else {

    }
  }

  handleSelect(selectedRow) {
    this.setState({ selectedFile: selectedRow && selectedRow.data })
  }

  handleMultiselect(selectedRow) {
    this.setState({
      multiselectedFiles: new Set(
        Object.values(selectedRow).map(value => value.data)
      )
    })
  }

  async handleDownload() {
    const { multiselectedFiles } = this.state
    const file = multiselectedFiles.values().next().value;

    try {
      this.setState({ loading: true })

      const dataArray = await this.controller.download(file)
      const blob = new Blob(dataArray, { type: "application/octet-stream" })
      const link = document.createElement('a')
      link.href = window.URL.createObjectURL(blob)
      link.download = file.name
      document.body.appendChild(link);
      link.click()
      document.body.removeChild(link);
    }  finally {
      this.setState({ loading: false })
    }
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

  timeToString(time) {
    return new Date(time).toLocaleString()
  }

  sizeToString(bytes) {
    if (!bytes) {
      return null
    }

    if (typeof bytes == 'string') {
      bytes = parseInt(bytes)
    }

    let size
    let unit
    const kbytes = bytes / 1024.0
    const mbytes = kbytes / 1024.0
    const gbytes = mbytes / 1024.0
    if (gbytes > 1.0) {
      size = gbytes
      unit = 'GB'
    } else if (mbytes > 1.0) {
      size = mbytes
      unit = 'MB'
    } else if (kbytes > 1.0) {
      size = kbytes
      unit = 'kB'
    } else {
      size = bytes
      unit = 'bytes'
    }
    return size.toFixed(1) + '\xa0' + unit
  }

  fetchSpaceStatus() {
    this.controller.free().then(space => {
      this.setState({ freeSpace: space.free, totalSpace: space.total })
    })
  }

  componentDidMount() {
    this.fetchSpaceStatus()
  }

  render() {
    const { classes, sessionToken, id } = this.props
    const {
      viewType,
      files,
      selectedFile,
      multiselectedFiles,
      showInfo,
      path,
      freeSpace,
      totalSpace
    } = this.state

    return (
      <div
        className={[classes.boundary, classes.columnFlexContainer].join(' ')}
      >
        <Toolbar
          controller={this.controller}
          viewType={viewType}
          onViewTypeChange={this.handleViewTypeChange}
          onShowInfoChange={this.handleShowInfoChange}
          onDownload={this.handleDownload}
          showInfo={showInfo}
          multiselectedFiles={multiselectedFiles}
          datastoreServer={this.datastoreServer}
          sessionToken={sessionToken}
          owner={id}
          path={path}
        />
        <InfoBar
          path={path}
          onPathChange={this.handlePathChange}
          free={freeSpace}
          total={totalSpace}
        />
        <div
          className={[
            classes.flexContainer,
            classes.boundary,
            classes.content
          ].join(' ')}
        >
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
                  label: messages.get(messages.NAME),
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
                  name: 'type',
                  label: messages.get(messages.TYPE),
                  sortable: true,
                  getValue: ({ row }) => (row.directory ? 'Directory' : 'File')
                },
                {
                  name: 'size',
                  label: messages.get(messages.SIZE),
                  sortable: true,
                  getValue: ({ row }) => this.sizeToString(row.size)
                },
                {
                  name: 'created',
                  label: messages.get(messages.CREATED),
                  sortable: true,
                  getValue: ({ row }) => row.creationTime,
                  renderValue: ({ row }) => this.timeToString(row.creationTime)
                },
                {
                  name: 'modified',
                  label: messages.get(messages.MODIFIED),
                  sortable: true,
                  getValue: ({ row }) => row.lastModifiedTime,
                  renderValue: ({ row }) =>
                    this.timeToString(row.lastModifiedTime)
                },
                {
                  name: 'accessed',
                  label: messages.get(messages.ACCESSED),
                  sortable: true,
                  getValue: ({ row }) => row.lastAccessTime,
                  renderValue: ({ row }) =>
                    this.timeToString(row.lastAccessTime)
                }
              ]}
              loadRows={this.controller.load}
              exportable={false}
              selectable={true}
              multiselectable={true}
              loadSettings={null}
              showHeaders={true}
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
            <InfoPanel
              selectedFile={selectedFile}
              configuration={configuration}
            />
          )}
        </div>
      </div>
    )
  }
}

export default withStyles(styles)(DataBrowser)
