/*
 *  Copyright ETH 2023 Zürich, Scientific IT Services
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

import React from 'react'
import ResizeObserver from 'rc-resize-observer'
import Button from '@material-ui/core/Button'
import CreateNewFolderIcon from '@material-ui/icons/CreateNewFolderOutlined'
import DownloadIcon from '@material-ui/icons/GetApp'
import DeleteIcon from '@material-ui/icons/Delete'
import RenameIcon from '@material-ui/icons/Create'
import CopyIcon from '@material-ui/icons/FileCopy'
import MoveIcon from '@material-ui/icons/ArrowRightAlt'
import MoreIcon from '@material-ui/icons/MoreVert'
import messages from '@src/js/common/messages.js'
import { withStyles } from '@material-ui/core/styles'
import logger from '@src/js/common/logger.js'
import autoBind from 'auto-bind'
import IconButton from '@material-ui/core/IconButton'
import { debounce } from '@material-ui/core'
import Container from '@src/js/components/common/form/Container.jsx'
import Popover from '@material-ui/core/Popover'
import InputDialog from '@src/js/components/common/dialog/InputDialog.jsx'
import ConfirmationDialog from '@src/js/components/common/dialog/ConfirmationDialog.jsx'
import LocationDialog from '@src/js/components/database/data-browser/LocationDialog.jsx'
import LoadingDialog from '@src/js/components/common/loading/LoadingDialog.jsx'

const color = 'default'
const iconButtonSize = 'medium'
const moveLocationMode = 'move'
const copyLocationMode = 'copy'

const styles = theme => ({
  buttons: {
    flex: '1 1 auto',
    display: 'flex',
    alignItems: 'center',
    whiteSpace: 'nowrap',
    overflow: 'hidden',
    '&>button': {
      marginRight: theme.spacing(1)
    },
    '&>button:nth-last-child(1)': {
      marginRight: 0
    }
  },
  toggleButton: {},
  collapsedButtonsContainer: {
    display: 'flex',
    flexDirection: 'column',
    '&>button': {
      marginBottom: theme.spacing(1)
    },
    '&>button:nth-last-child(1)': {
      marginBottom: 0
    }
  },
})

class LeftToolbar extends React.Component {

  constructor(props, context) {
    super(props, context)
    autoBind(this)

    this.state = {
      width: 0,
      hiddenButtonsPopup: null,
      newFolderDialogOpen: false,
      deleteDialogOpen: false,
      renameDialogOpen: false,
      locationDialogMode: null,
      loading: false
    }

    this.controller = this.props.controller
    this.onResize = debounce(this.onResize, 1)
  }

  async handleNewFolderCreate(folderName) {
    this.closeNewFolderDialog()
    if (folderName && folderName.trim()) {
      await this.controller.createNewFolder(folderName.trim())
    }
  }

  handleNewFolderCancel() {
    this.closeNewFolderDialog()
  }

  openNewFolderDialog() {
    this.setState({ newFolderDialogOpen: true })
  }

  closeNewFolderDialog() {
    this.setState({ newFolderDialogOpen: false })
  }

  openDeleteDialog() {
    this.setState({ deleteDialogOpen: true })
  }

  closeDeleteDialog() {
    this.setState({ deleteDialogOpen: false })
  }

  openRenameDialog() {
    this.setState({ renameDialogOpen: true })
  }

  closeRenameDialog() {
    this.setState({ renameDialogOpen: false })
  }

  async handleRenameConfirm(newName) {
    const { multiselectedFiles } = this.props
    const oldName = multiselectedFiles.values().next().value.name
    this.closeRenameDialog()

    try {
      this.setState({ loading: true })
      await this.controller.rename(oldName, newName)
    } finally {
      this.setState({ loading: false })
    }
  }

  handleRenameCancel() {
    this.closeRenameDialog()
  }

  openMoveLocationDialog() {
    this.setState({ locationDialogMode: moveLocationMode })
  }

  openCopyLocationDialog() {
    this.setState({ locationDialogMode: copyLocationMode })
  }

  closeLocationDialog() {
    this.setState({ locationDialogMode: null })
  }

  async handleLocationConfirm(newPath) {
    const { multiselectedFiles } = this.props
    const { locationDialogMode} = this.state
    this.closeLocationDialog()

    try {
      this.setState({ loading: true })
      if (locationDialogMode === moveLocationMode) {
        await this.controller.move(multiselectedFiles, newPath)
      } else {
        await this.controller.copy(multiselectedFiles, newPath)
      }
    } finally {
      this.setState({ loading: false })
    }
  }

  handleLocationCancel() {
    this.closeLocationDialog()
  }

  async handleDeleteConfirm() {
    const { multiselectedFiles } = this.props

    this.closeDeleteDialog()
    await this.controller.delete(multiselectedFiles)
  }

  handleDeleteCancel() {
    this.closeDeleteDialog()
  }

  renderNoSelectionContextToolbar() {
    const { classes, buttonSize, editable } = this.props
    return ([
      <Button
        key='new-folder'
        classes={{ root: classes.button }}
        color={color}
        size={buttonSize}
        variant='outlined'
        disabled={editable}
        startIcon={<CreateNewFolderIcon />}
        onClick={this.openNewFolderDialog}
      >
        {messages.get(messages.NEW_FOLDER)}
      </Button>,
      <InputDialog
        key='new-folder-dialog'
        open={this.state.newFolderDialogOpen}
        title={messages.get(messages.NEW_FOLDER)}
        inputLabel={messages.get(messages.FOLDER_NAME)}
        onCancel={this.handleNewFolderCancel}
        onConfirm={this.handleNewFolderCreate}
        />
    ])
  }

  renderSelectionContextToolbar() {
    const {
      classes,
      buttonSize,
      multiselectedFiles,
      sessionToken,
      owner,
      path,
      onDownload
    } = this.props
    const {
      width,
      hiddenButtonsPopup,
      deleteDialogOpen,
      renameDialogOpen,
      locationDialogMode
    } = this.state

    const ellipsisButtonSize = 24
    const buttonsCount = 5
    const minSize = 500
    const roughButtonSize = Math.floor(minSize / buttonsCount)
    const hideButtons = width < minSize
    const visibleButtonsCount = Math.max(hideButtons ? Math.floor((width - 3 * ellipsisButtonSize) / roughButtonSize) : buttonsCount, 0)

    const buttons = [
      <Button
        key='download'
        classes={{ root: classes.button }}
        color={color}
        size={buttonSize}
        variant='outlined'
        disabled={multiselectedFiles.size === 0}
        startIcon={<DownloadIcon />}
        onClick={onDownload}
      >
        {messages.get(messages.DOWNLOAD)}
      </Button>,
      <Button
        key='delete'
        classes={{ root: classes.button }}
        color={color}
        size={buttonSize}
        variant='text'
        startIcon={<DeleteIcon />}
        onClick={this.openDeleteDialog}
      >
        {messages.get(messages.DELETE)}
      </Button>,
      <Button
        key='rename'
        classes={{ root: classes.button }}
        color={color}
        size={buttonSize}
        variant='text'
        disabled={multiselectedFiles.size !== 1}
        startIcon={<RenameIcon />}
        onClick={this.openRenameDialog}
      >
        {messages.get(messages.RENAME)}
      </Button>,
      <Button
        key='copy'
        classes={{ root: classes.button }}
        color={color}
        size={buttonSize}
        variant='text'
        startIcon={<CopyIcon />}
        onClick={this.openCopyLocationDialog}
      >
        {messages.get(messages.COPY)}
      </Button>,
      <Button
        key='move'
        classes={{ root: classes.button }}
        color={color}
        size={buttonSize}
        variant='text'
        startIcon={<MoveIcon />}
        onClick={this.openMoveLocationDialog}
      >
        {messages.get(messages.MOVE)}
      </Button>
    ]
    const ellipsisButton = (
      <IconButton
        key='ellipsis'
        classes={{ root: classes.button }}
        color={color}
        size={iconButtonSize}
        variant='outlined'
        onClick={this.handleOpen}
      >
        <MoreIcon />
      </IconButton>
    )

    const popover = (
      <Popover
        key='more'
        open={Boolean(hiddenButtonsPopup)}
        anchorEl={hiddenButtonsPopup}
        onClose={this.handleClose}
        anchorOrigin={{
          vertical: 'bottom',
          horizontal: 'left'
        }}
        transformOrigin={{
          vertical: 'top',
          horizontal: 'left'
        }}
      >
        <Container square={true}>{this.renderCollapsedButtons(buttons.slice(visibleButtonsCount))}</Container>
      </Popover>
    )

    const selectedValue = multiselectedFiles.values().next().value;
    return (
      <div className={classes.buttons}>
        {hideButtons
          ? [...buttons.slice(0, visibleButtonsCount), ellipsisButton, popover]
          : buttons}
        <ConfirmationDialog
          key='delete-dialog'
          open={deleteDialogOpen}
          onConfirm={this.handleDeleteConfirm}
          onCancel={this.handleDeleteCancel}
          title={messages.get(messages.DELETE)}
          content={messages.get(messages.CONFIRMATION_DELETE_SELECTED)}
        />
        <InputDialog
          key='rename-dialog'
          open={renameDialogOpen}
          title={selectedValue.directory ? messages.get(messages.RENAME_FOLDER) : messages.get(messages.RENAME_FILE)}
          inputLabel={selectedValue.directory ? messages.get(messages.FOLDER_NAME) : messages.get(messages.FILE_NAME)}
          inputValue={selectedValue.name}
          onCancel={this.handleRenameCancel}
          onConfirm={this.handleRenameConfirm}
        />
        <LocationDialog
          key='location-dialog'
          open={!!locationDialogMode}
          title={locationDialogMode === moveLocationMode ? messages.get(messages.MOVE) : messages.get(messages.COPY)}
          content={messages.get(messages.FILE_OR_FILES, multiselectedFiles.size)}
          sessionToken={sessionToken}
          owner={owner}
          path={path}
          multiselectedFiles={multiselectedFiles}
          onCancel={this.handleLocationCancel}
          onConfirm={this.handleLocationConfirm}
          />
      </div>
    );
  }

  renderCollapsedButtons(buttons) {
    const { classes } = this.props
    return (
      <div className={classes.collapsedButtonsContainer}>
        {buttons}
      </div>
    )
  }

  onResize({ width }) {
    if (width !== this.state.width) {
      this.setState({ width, hiddenButtonsPopup: null })
    }
  }

  handleOpen(event) {
    this.setState({
      hiddenButtonsPopup: event.currentTarget
    })
  }

  handleClose() {
    this.setState({
      hiddenButtonsPopup: null
    })
  }

  render() {
    logger.log(logger.DEBUG, 'LeftToolbar.render')

    const { multiselectedFiles, classes, owner } = this.props
    const { loading } = this.state

    return ([
      <ResizeObserver key='resize-observer' onResize={this.onResize}>
        <div className={classes.buttons}>
          {multiselectedFiles && multiselectedFiles.size > 0
            ? this.renderSelectionContextToolbar()
            : this.renderNoSelectionContextToolbar()}
        </div>
      </ResizeObserver>,
      <LoadingDialog key='left-toolbar-loaging-dialog' variant='indeterminate' loading={loading} />
    ])
  }
}

export default withStyles(styles)(LeftToolbar)
