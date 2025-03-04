import React from 'react'
import withStyles from '@mui/styles/withStyles';
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
import LoadingDialog from '@src/js/components/common/loading/LoadingDialog.jsx'
import ErrorDialog from '@src/js/components/common/error/ErrorDialog.jsx'

// 2GB limit for total download size
const sizeLimit = 2147483648

const styles = theme => ({
  columnFlexContainer: {
    flexDirection: 'column',
    display: 'flex',
    height: 'calc(100vh - ' + theme.spacing(21) + 'px)'
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
      extensions: ['wav', 'mp3', 'aac', 'ogg', 'oga', 'flac', 'm4a', 'wma',
        'opus', 'flac', 'aiff', 'weba']
    },
    {
      icon: 'file-text',
      extensions: ['txt', 'rtf', 'html', 'htm', 'epub', 'md', 'tex', 'pages',
        'numbers', 'key', 'mobi', 'indd', 'csv', 'tsv',
        'odt', 'ods', 'odp', 'otp', 'odm', 'ott', 'ots', 'odf', 'odft']
    },
    {
      icon: 'file-video',
      extensions: ['mp4', 'avi', 'mov', 'wmv', 'flv', 'mkv', 'webm',
        'mpeg', 'mpg', 'mpe', 'vob', 'm4v', 'ogv']
    },
    {
      icon: 'file-image',
      extensions: ['tif', 'tiff', 'gif', 'jpg', 'jpeg', 'png', 'bmp', 'svg',
        'webp', 'psd', 'raw', 'heif', 'heic', 'odc', 'otc', 'odg', 'otg',
        'odi', 'oti']
    },
    {
      icon: 'file-archive',
      extensions:  ['zip', 'rar', '7z', 'tar', 'gz', 'bz', 'bz2', 'xz', 'iso',
        'zipx', 'cab', 'arj', 'lz', 'lzma', 'z', 'tgz', 'ace', 'dmg']
    },
    {
      icon: 'file-code',
      extensions:  ['xml', 'js', 'html', 'css', 'c', 'cpp', 'h', 'cs', 'php',
        'rb', 'swift', 'go', 'rs', 'ts', 'json', 'sh', 'bat', 'sql', 'yaml',
        'yml', 'jsx', 'tsx', 'pl', 'scala', 'kt']
    },
    // Fine-grained file formats
    {
      icon: 'file-pdf',
      extensions: ['pdf']
    },
    {
      icon: 'file-word',
      extensions: ['doc', 'dot', 'docx']
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

const mimeTypeMap = {
    '.7z': 'application/x-7z-compressed',
    '.aac': 'audio/aac',
    '.ace': 'application/x-ace-compressed',
    '.aiff': 'audio/aiff',
    '.arj': 'application/x-arj',
    '.avi': 'video/x-msvideo',
    '.bmp': 'image/bmp',
    '.bz': 'application/x-bzip',
    '.bz2': 'application/x-bzip2',
    '.bat': 'application/x-msdownload',
    '.c': 'text/x-c',
    '.cab': 'application/vnd.ms-cab-compressed',
    '.cpp': 'text/x-c',
    '.cs': 'text/x-csharp',
    '.css': 'text/css',
    '.csv': 'text/csv',
    '.dmg': 'application/x-apple-diskimage',
    '.doc': 'application/msword',
    '.docx': 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
    '.dot': 'application/msword',
    '.epub': 'application/epub+zip',
    '.flac': 'audio/flac',
    '.flv': 'video/x-flv',
    '.gif': 'image/gif',
    '.go': 'text/plain',
    '.gz': 'application/gzip',
    '.h': 'text/x-c',
    '.heic': 'image/heic',
    '.heif': 'image/heif',
    '.htm': 'text/html',
    '.html': 'text/html',
    '.indd': 'application/octet-stream',
    '.iso': 'application/x-iso9660-image',
    '.jpeg': 'image/jpeg',
    '.jpg': 'image/jpeg',
    '.js': 'application/javascript',
    '.json': 'application/json',
    '.jsx': 'text/jsx',
    '.key': 'application/vnd.apple.keynote',
    '.kt': 'text/plain',
    '.lz': 'application/octet-stream',
    '.lzma': 'application/x-lzma',
    '.m4a': 'audio/mp4',
    '.m4v': 'video/x-m4v',
    '.md': 'text/markdown',
    '.mkv': 'video/x-matroska',
    '.mobi': 'application/x-mobipocket-ebook',
    '.mov': 'video/quicktime',
    '.mp3': 'audio/mpeg',
    '.mp4': 'video/mp4',
    '.mpe': 'video/mpeg',
    '.mpeg': 'video/mpeg',
    '.mpg': 'video/mpeg',
    '.oga': 'audio/ogg',
    '.ogg': 'audio/ogg',
    '.ogv': 'video/ogg',
    '.pdf': 'application/pdf',
    '.odc': 'application/vnd.oasis.opendocument.chart',
    '.odg': 'application/vnd.oasis.opendocument.graphics',
    '.odf': 'application/vnd.oasis.opendocument.formula',
    '.odft': 'application/vnd.oasis.opendocument.formula-template',
    '.odi': 'application/vnd.oasis.opendocument.image',
    '.odm': 'application/vnd.oasis.opendocument.text-master',
    '.odt': 'application/vnd.oasis.opendocument.text',
    '.odp': 'application/vnd.oasis.opendocument.presentation',
    '.ods': 'application/vnd.oasis.opendocument.spreadsheet',
    '.otc': 'application/vnd.oasis.opendocument.chart-template',
    '.otg': 'application/vnd.oasis.opendocument.graphics-template',
    '.oth': 'application/vnd.oasis.opendocument.text-web',
    '.oti': 'application/vnd.oasis.opendocument.image-template',
    '.otp': 'application/vnd.oasis.opendocument.presentation-template',
    '.ots': 'application/vnd.oasis.opendocument.spreadsheet-template',
    '.ott': 'application/vnd.oasis.opendocument.text-template',
    '.opus': 'audio/opus',
    '.pages': 'application/vnd.apple.pages',
    '.php': 'application/x-httpd-php',
    '.pl': 'application/x-perl',
    '.png': 'image/png',
    '.pot': 'application/vnd.ms-powerpoint',
    '.pps': 'application/vnd.ms-powerpoint',
    '.ppt': 'application/vnd.ms-powerpoint',
    '.pptx': 'application/vnd.openxmlformats-officedocument.presentationml.presentation',
    '.psd': 'image/vnd.adobe.photoshop',
    '.rar': 'application/x-rar-compressed',
    '.raw': 'image/x-panasonic-raw',
    '.rb': 'text/x-ruby',
    '.rs': 'application/rls-services+xml',
    '.rtf': 'application/rtf',
    '.scala': 'text/x-scala',
    '.sh': 'application/x-sh',
    '.svg': 'image/svg+xml',
    '.sql': 'application/x-sql',
    '.swift': 'text/x-swift',
    '.tar': 'application/x-tar',
    '.tex': 'application/x-tex',
    '.tgz': 'application/gzip',
    '.tif': 'image/tiff',
    '.tiff': 'image/tiff',
    '.ts': 'text/typescript',
    '.tsv': 'text/tab-separated-values',
    '.tsx': 'text/tsx',
    '.txt': 'text/plain',
    '.vob': 'video/x-ms-vob',
    '.wav': 'audio/wav',
    '.weba': 'audio/webm',
    '.webm': 'video/webm',
    '.webp': 'image/webp',
    '.wma': 'audio/x-ms-wma',
    '.wmv': 'video/x-ms-wmv',
    '.xls': 'application/vnd.ms-excel',
    '.xlt': 'application/vnd.ms-excel',
    '.xlsx': 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    '.xml': 'application/xml',
    '.xz': 'application/x-xz',
    '.yaml': 'application/yaml',
    '.yml': 'application/yaml',
    '.z': 'application/x-compress',
    '.zip': 'application/zip',
    '.zipx': 'application/zip'
  }

class DataBrowser extends React.Component {
  constructor(props, context) {
    super(props, context)
    autoBind(this)

    const { sessionToken, controller, id } = this.props

    this.controller = controller || new DataBrowserController(id)
    this.controller.attach(this)

    this.state = {
      viewType: props.viewType,
      files: [],
      selectedFile: null,
      multiselectedFiles: new Set([]),
      showInfo: false,
      path: '/',
      freeSpace: -1,
      totalSpace: -1,
      loading: false,
      errorMessage: null,
      editable: false
    }
    this.zip = new JSZip()
  }

  handleViewTypeChange(viewType) {
    this.setState({ viewType })
  }

  handleClick(file) {
    // TODO: implement
  }

  async handleRowDoubleClick(row) {
    const file = row.data
    const { directory, path } = file
    if (directory) {
      await this.setPath(path)
    } else {
      await this.downloadFile(file)
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
    const files = multiselectedFiles.values()
    const file = files.next().value

    if (multiselectedFiles.size > 1 || file.directory) {
      // ZIP download
      await this.downloadFiles()
    } else {
      // Single file download
      await this.downloadFile(file)
    }
  }

  async downloadFiles() {
    const { multiselectedFiles } = this.state
    const { id } = this.props

    if ((await this.calculateTotalSize(multiselectedFiles)) <= sizeLimit) {
      this.setState({ loading: true })
      const zipBlob = await this.prepareZipBlob(multiselectedFiles)
      this.downloadBlob(zipBlob, id)
      this.zip = new JSZip()
      this.setState({ loading: false })
    } else {
      this.showDownloadErrorDialog()
    }
  }

  async calculateTotalSize(files) {
    let size = 0
    for (let file of files) {
      if (!file.directory) {
        size += file.size
      } else {
        const nestedFiles = await this.controller.listFiles(file.path)
        size += await this.calculateTotalSize(nestedFiles)
      }
    }
    return size
  }

  async prepareZipBlob(files) {
    for (let file of files) {
      if (!file.directory) {
        const dataArray = await this.controller.download(file)
        this.zip.file(
          file.path,
          new Blob(dataArray, { type: this.inferMimeType(file.path) })
        )
      } else {
        this.zip.folder(file.path)
        const nestedFiles = await this.controller.listFiles(file.path)
        await this.prepareZipBlob(nestedFiles)
      }
    }
    return await this.zip.generateAsync({ type: 'blob' })
  }

  async downloadFile(file) {
    if (file.size <= sizeLimit) {
      try {
        this.setState({ loading: true })
        const blob = await this.fileToBlob(file)
        this.downloadBlob(blob, file.name)
      } finally {
        this.setState({ loading: false })
      }
    } else {
      this.showDownloadErrorDialog()
    }
  }

  showDownloadErrorDialog() {
    this.openErrorDialog(messages.get(messages.CANNOT_DOWNLOAD, sizeLimit))
  }

  downloadBlob(blob, fileName) {
    const link = document.createElement('a')
    link.href = window.URL.createObjectURL(blob)
    link.download = fileName
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
  }

  async fileToBlob(file) {
    const dataArray = await this.controller.download(file)
    return new Blob(dataArray, { type: this.inferMimeType(file.path) })
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

  inferMimeType(fileName) {
    const extension = fileName.slice(fileName.lastIndexOf('.')).toLowerCase()
    return mimeTypeMap[extension] || 'application/octet-stream'
  }

  fetchRights() {
    const { id, kind } = this.props
    this.controller.getRights([{permId: id, entityKind: kind}]).then(right => {
      if (right[id] && right[id].rights) {
        const editable = right[id].rights.includes("UPDATE")
        this.setState({ editable: editable })
      } else {
        this.setState({ editable: false })
      }
    })
  }

  componentDidMount() {
    this.fetchSpaceStatus()
    this.fetchRights()
  }

  openErrorDialog(errorMessage) {
    this.setState({ errorMessage })
  }

  closeErrorDialog() {
    this.setState({ errorMessage: null })
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
      totalSpace,
      loading,
      errorMessage,
      editable
    } = this.state

    return [
      <div
        key='data-browser-content'
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
          sessionToken={sessionToken}
          owner={id}
          editable={editable}
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
                  visible: true,
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
                  visible: false,
                  getValue: ({ row }) => (row.directory ? 'Directory' : 'File')
                },
                {
                  name: 'size',
                  label: messages.get(messages.SIZE),
                  sortable: true,
                  visible: true,
                  getValue: ({ row }) => this.sizeToString(row.size)
                },
                {
                  name: 'created',
                  label: messages.get(messages.CREATED),
                  sortable: true,
                  visible: false,
                  getValue: ({ row }) => row.creationTime,
                  renderValue: ({ row }) => this.timeToString(row.creationTime)
                },
                {
                  name: 'modified',
                  label: messages.get(messages.MODIFIED),
                  sortable: true,
                  visible: true,
                  getValue: ({ row }) => row.lastModifiedTime,
                  renderValue: ({ row }) =>
                    this.timeToString(row.lastModifiedTime)
                },
                {
                  name: 'accessed',
                  label: messages.get(messages.ACCESSED),
                  sortable: true,
                  visible: false,
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
      </div>,
      <LoadingDialog
        key='data-browser-loaging-dialog'
        variant='indeterminate'
        loading={loading}
        message={messages.get(messages.PREPARING_FILE)}
      />,
      <ErrorDialog
        key='data-browser-error-dialog'
        open={!!errorMessage}
        error={errorMessage}
        onClose={this.closeErrorDialog}
      />
    ]
  }
}

export default withStyles(styles)(DataBrowser)
