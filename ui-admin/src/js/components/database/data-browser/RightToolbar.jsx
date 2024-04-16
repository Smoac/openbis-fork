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
import { ToggleButton } from '@material-ui/lab'
import messages from '@src/js/common/messages.js'
import InfoIcon from '@material-ui/icons/InfoOutlined'
import IconButton from '@material-ui/core/IconButton'
import SearchIcon from '@material-ui/icons/Search'
import ViewComfyIcon from '@material-ui/icons/ViewComfy'
import ViewListIcon from '@material-ui/icons/ViewList'
import SettingsIcon from '@material-ui/icons/Settings'
import Button from '@material-ui/core/Button'
import PublishIcon from '@material-ui/icons/Publish'
import Popover from '@material-ui/core/Popover'
import Container from '@src/js/components/common/form/Container.jsx'
import { withStyles } from '@material-ui/core/styles'
import autoBind from 'auto-bind'
import UploadButton from '@src/js/components/database/data-browser/UploadButton.jsx'
import FileIcon from '@material-ui/icons/InsertDriveFileOutlined'
import FolderIcon from '@material-ui/icons/FolderOpen'
import logger from '@src/js/common/logger.js'
import LoadingDialog from "@src/js/components/common/loading/LoadingDialog.jsx";
import FileExistsDialog from "@src/js/components/common/dialog/FileExistsDialog.jsx";

const color = 'default'
const uploadButtonsColor = 'secondary'
const iconButtonSize = 'medium'

const styles = theme => ({
  buttons: {
    flex: '0 0 auto',
    display: 'flex',
    alignItems: 'center',
    whiteSpace: 'nowrap',
    '&>button': {
      marginRight: theme.spacing(1)
    },
    '&>button:nth-last-child(1)': {
      marginRight: 0
    }
  },
  uploadButtonsContainer: {
    display: 'flex',
    flexDirection: 'column',
    '&>button': {
      marginBottom: theme.spacing(1)
    },
    '&>button:nth-last-child(1)': {
      marginBottom: 0
    }
  },
  toggleButton: {
    border: 'none',
    borderRadius: '50%',
    display: 'inline-flex',
    padding: theme.spacing(1.5) + 'px',
    '& *': {
      color: theme.palette[color].main
    }
  }
})

class RightToolbar extends React.Component {
  constructor(props, context) {
    super(props, context)
    autoBind(this)

    this.controller = this.props.controller
    this.resolveConflict = null // This function will be shared

    this.state = {
      uploadButtonsPopup: null,
      loading: false,
      progress: 0,
      allowResume: true,
      fileExistsDialogFile: null
    }
  }

  async handleUpload(event) {
    try {
      this.setState({ loading: true, progress: 0 })
      await this.controller.upload(event.target.files, this.resolveNameConflict,
        this.updateProgress)
    } finally {
      this.setState({ loading: false })
    }
  }

  updateProgress(progress) {
    this.setState({ progress })
  }

  async resolveNameConflict(newFile, allowResume) {
    return new Promise((resolve) => {
      this.setState({ allowResume })
      this.openFileExistsDialog(newFile)
      this.resolveConflict = resolve
    })
  }

  openFileExistsDialog(newFile) {
    this.setState({ fileExistsDialogFile: newFile })
  }

  closeFileExistsDialog() {
    this.setState({ fileExistsDialogFile: null })
  }

  handleUploadClick(event) {
    this.setState({
      uploadButtonsPopup: event.currentTarget
    })
  }

  handlePopoverClose() {
    this.setState({
      uploadButtonsPopup: null
    })
  }

  handleFileExistsReplace() {
    this.closeFileExistsDialog()
    this.resolveConflict && this.resolveConflict('replace')
  }

  handleFileExistsResume() {
    this.closeFileExistsDialog()
    this.resolveConflict && this.resolveConflict('resume')
  }

  handleFileExistsCancel() {
    this.closeFileExistsDialog()
    this.resolveConflict && this.resolveConflict('cancel')
  }

  renderUploadButtons() {
    const { classes, buttonSize } = this.props
    return (
      <div className={classes.uploadButtonsContainer}>
        <UploadButton
          classes={{ root: classes.button }}
          color={uploadButtonsColor}
          size={buttonSize}
          variant='contained'
          startIcon={<FileIcon />}
          folderSelector={false}
          onClick={this.handleUpload}
        >
          {messages.get(messages.FILE_UPLOAD)}
        </UploadButton>
        <UploadButton
          classes={{ root: classes.button }}
          color={uploadButtonsColor}
          size={buttonSize}
          variant='contained'
          startIcon={<FolderIcon />}
          folderSelector={true}
          onClick={this.handleUpload}
        >
          {messages.get(messages.FOLDER_UPLOAD)}
        </UploadButton>
      </div>
    )
  }

  render() {
    logger.log(logger.DEBUG, 'RightToolbar.render')

    const { classes, onViewTypeChange, buttonSize } = this.props
    const { uploadButtonsPopup, progress, loading, allowResume,
      fileExistsDialogFile } = this.state
    return ([
      <div key='right-toolbar-main' className={classes.buttons}>
        <ToggleButton
          classes={{ root: classes.toggleButton }}
          color={color}
          size={buttonSize}
          selected={this.props.selected}
          onChange={this.props.onChange}
          value={messages.get(messages.INFO)}
          aria-label={messages.get(messages.INFO)}
        >
          <InfoIcon />
        </ToggleButton>
        {this.props.viewType === 'list' && (
          <IconButton
            classes={{ root: classes.button }}
            color={color}
            size={iconButtonSize}
            variant='outlined'
            onClick={() => onViewTypeChange('grid')}
          >
            <ViewComfyIcon />
          </IconButton>
        )}
        {this.props.viewType === 'grid' && (
          <IconButton
            classes={{ root: classes.button }}
            color={color}
            size={iconButtonSize}
            variant='outlined'
            onClick={() => onViewTypeChange('list')}
          >
            <ViewListIcon />
          </IconButton>
        )}
        <Button
          classes={{ root: classes.button }}
          color={color}
          size={buttonSize}
          variant='outlined'
          startIcon={<PublishIcon />}
          onClick={this.handleUploadClick}
        >
          {messages.get(messages.UPLOAD)}
        </Button>
        <Popover
          id={'toolbar.columns-popup-id'}
          open={Boolean(uploadButtonsPopup)}
          anchorEl={uploadButtonsPopup}
          onClose={this.handlePopoverClose}
          anchorOrigin={{
            vertical: 'bottom',
            horizontal: 'left'
          }}
          transformOrigin={{
            vertical: 'top',
            horizontal: 'left'
          }}
        >
          <Container square={true}>{this.renderUploadButtons()}</Container>
        </Popover>
      </div>,
      <LoadingDialog key='right-toolbar-loaging-dialog' variant='determinate'
                     value={progress} loading={loading} />,
      <FileExistsDialog
        key='file-exists-dialog'
        open={!!fileExistsDialogFile}
        onReplace={this.handleFileExistsReplace}
        onResume={allowResume ? this.handleFileExistsResume : null}
        onCancel={this.handleFileExistsCancel}
        title={messages.get(messages.DELETE)}
        content={messages.get(messages.CONFIRMATION_FILE_NAME_CONFLICT,
          fileExistsDialogFile ? fileExistsDialogFile.name : '')}
      />
    ])
  }
}

export default withStyles(styles)(RightToolbar)
