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
import InfoPanel from "@src/js/components/database/data-browser/InfoPanel.jsx";

const HTTP_SERVER_URI = "/data-store-server";

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
    flexWrap: 'wrap',
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
    this.datastoreServer = new DataStoreServer('http://localhost:8085', HTTP_SERVER_URI);

    const owner = "demo-sample"
    const source = ""
    this.datastoreServer.login("admin", "changeit", this.login);


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
      multiselectedFiles: new Set([]),
      showInfo: false
    }
  }

  login(token) {
    if (!token) {
      alert("Could not perform login.");
      return;
    }

    console.log("Token: " + token)
    this.datastoreServer.list("demo-sample", "", "true", this.displayReturnedFiles)
  }

  displayReturnedFiles(data) {
    if (data.error) {
      console.error(data.error);
      alert("Could not list files.");
      return;
    }

    const results = data.result[1];

    // Restrict the display to 50 samples
    // results = results.splice(0, 50);

    // generateTable(results);

    console.log("Received data: " + results)
  }

  handleViewTypeChange(viewType) {
    this.setState({ viewType })
  }

  handleClick(file) {
    // TODO: implement
  }

  handleSelect(selectedRow) {
    this.setState({selectedFile: selectedRow && selectedRow.data});
  }

  handleMultiselect(file) {
    // TODO: implement
  }

  async load(params) {
    return await this.state.files.map((file) => ({id: file.name, ...file}));
  }

  async onError(error) {
    await AppController.getInstance().errorChange(error)
  }

  handleShowInfoChange() {
    this.setState({showInfo: !this.state.showInfo})
  }

  render() {
    const { classes } = this.props
    const { viewType, files, selectedFile, multiselectedFiles, showInfo } = this.state

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
                  renderValue: ({ row }) => <><ItemIcon file={row} classes={{ icon: classes.icon }} configuration={configuration} /> {row.name}</>,
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
              selectedFile = {selectedFile}
              multiselectedFiles = {multiselectedFiles}
            />
          )}
          {showInfo && selectedFile && <InfoPanel file={selectedFile} configuration={configuration} />}
        </div>
      </Paper>
    )
  }
}

export default withStyles(styles)(DataBrowser)
