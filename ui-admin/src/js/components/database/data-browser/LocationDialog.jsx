import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Button from '@src/js/components/common/form/Button.jsx'
import Dialog from '@src/js/components/common/dialog/Dialog.jsx'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'
import { DialogContentText } from '@material-ui/core'
import autoBind from 'auto-bind'
import ItemIcon from '@src/js/components/database/data-browser/ItemIcon.jsx'
import Grid from '@src/js/components/common/grid/Grid.jsx'
import DataBrowserController from '@src/js/components/database/data-browser/DataBrowserController.js'
import NavigationBar from "@src/js/components/database/data-browser/NavigationBar.jsx";

const styles = theme => ({
  button: {
    marginLeft: theme.spacing(1)
  },
  icon: {
    fontSize: '2rem',
    paddingRight: '0.5rem'
  },
  grid: {
    flexGrow: 1,
    flex: 1,
    height: 'auto',
    overflowY: 'auto',
    paddingTop: 0,
    paddingBottom: 0
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

class LocationDialog extends React.Component {
  constructor(props) {
    super(props)
    autoBind(this)

    const { path, sessionToken, datastoreServer} = this.props

    this.controller = new DataBrowserController()
    this.controller.attach(this)
    this.datastoreServer = datastoreServer
    this.controller.setSessionToken(sessionToken)
    // this.initialPath = path

    this.state = {
      path
    }
    this.controller.setPath(path)

    this.handleClose = this.handleClose.bind(this)
  }

  handleClose() {
    const { onCancel } = this.props
    if (onCancel) {
      onCancel()
    }
  }

  updateValue(event) {
    const path = event.target.value
    this.setState({
      path
    })
    this.controller.setPath(path)
  }

  handleConfirmClick() {
    const { onConfirm } = this.props
    const { path } = this.state
    onConfirm(path)
  }

  handleCancelClick() {
    const { onCancel } = this.props
    onCancel()
  }

  async setPath(path) {
    if (this.state.path !== path + '/') {
      this.setState({ path: path + '/' })
      this.controller.setPath(path + '/')
      await this.controller.gridController.load()
    }
  }

  async handleRowDoubleClick(row) {
    const { directory, path } = row.data
    if (directory) {
      await this.setPath(path)
    }
  }

  handleGridControllerRef(gridController) {
    this.controller.gridController = gridController
  }

  async handlePathChange(path) {
    await this.setPath(path)
  }

  async onError(error) {
    await AppController.getInstance().errorChange(error)
  }

  renderButtons() {
    const { classes, title } = this.props
    return (
      <div>
        <Button
          name='confirm'
          label={title}
          type={this.getButtonType()}
          styles={{ root: classes.button }}
          onClick={this.handleConfirmClick}
        />
        <Button
          name='cancel'
          label={messages.get(messages.CANCEL)}
          styles={{ root: classes.button }}
          onClick={this.handleCancelClick}
        />
      </div>
    )
  }

  renderGrid() {
    const { classes } = this.props
    return (
      <Grid
        id='location-grid'
        key='location-grid'
        controllerRef={this.handleGridControllerRef}
        filterModes={[]}
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
                />
                <span>{row.name}</span>
              </div>
            ),
            renderFilter: null
          }
        ]}
        loadRows={this.controller.loadFolders}
        exportable={false}
        selectable={false}
        multiselectable={false}
        loadSettings={null}
        showHeaders={false}
        showPaging={true}
        showConfigs={false}
        showRowsPerPage={false}
        onSettingsChange={null}
        onError={this.onError}
        onRowDoubleClick={this.handleRowDoubleClick}
        exportXLS={null}
      />
    )
  }

  getMessageType() {
    const type = this.getType()

    if (type === 'warning') {
      return 'warning'
    } else if (type === 'info') {
      return 'info'
    } else {
      throw new Error('Unsupported type: ' + type)
    }
  }

  getButtonType() {
    const type = this.getType()

    if (type === 'warning') {
      return 'risky'
    } else if (type === 'info') {
      return null
    } else {
      throw new Error('Unsupported type: ' + type)
    }
  }

  getType() {
    return this.props.type || 'warning'
  }

  render() {
    logger.log(logger.DEBUG, 'LocationDialog.render')

    const { open, title, content } = this.props
    const { path } = this.state

    return (
      <Dialog
        open={open}
        onClose={this.handleClose}
        title={title}
        content={[<DialogContentText key='dialog-content'>{content}</DialogContentText>,
          <NavigationBar
            key='navigation-bar'
            path={path}
            onPathChange={this.handlePathChange}
          />,
          this.renderGrid()]}
        actions={this.renderButtons()}
      />
    )
  }
}

export default withStyles(styles)(LocationDialog)
