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
import { withStyles } from '@material-ui/core/styles'
import messages from '@src/js/common/messages.js'
import ViewComfyIcon from '@material-ui/icons/ViewComfy'
import ViewListIcon from '@material-ui/icons/ViewList'
import PublishIcon from '@material-ui/icons/Publish'
import SettingsIcon from '@material-ui/icons/Settings'
import SearchIcon from '@material-ui/icons/Search'
import InfoIcon from '@material-ui/icons/InfoOutlined'
import CreateNewFolderIcon from '@material-ui/icons/CreateNewFolderOutlined'
import autoBind from 'auto-bind'
import { ToggleButton } from '@material-ui/lab'
import Button from '@material-ui/core/Button'
import IconButton from '@material-ui/core/IconButton'
import Container from "@src/js/components/common/form/Container.jsx";
import Popover from "@material-ui/core/Popover";
import UploadButton from "@src/js/components/database/data-browser/UploadButton.jsx";

const color = 'secondary'
const buttonSize = 'small'
const iconButtonSize = 'medium'

const styles = (theme) => ({
  toolbar: {
    flex: '0 0 auto',
    display: 'flex',
    whiteSpace: 'nowrap',
    marginLeft: theme.spacing(1),
    marginRight: theme.spacing(1)
  },
  buttons: {
    flex: '0 0 auto',
    display: 'flex',
    alignItems: 'center',
    whiteSpace: 'nowrap',
  },
  leftSection: {
    flexGrow: 1,
  },
  rightSection: {
    flexShrink: 0
  },
  toggleButton: {
    border: 'none',
    borderRadius: '50%',
    display: 'inline-flex',
    padding: theme.spacing(1.5) + 'px',
    '& *': {
      color: theme.palette[color].main
    }
  },
  uploadButtonsContainer: {
    display: 'flex',
    flexDirection: 'column'
  },
  marginBottom: {
    marginBottom: theme.spacing(1)
  }
})

class Toolbar extends React.Component {

  constructor(props, context) {
    super(props, context)
    autoBind(this)

    this.controller = this.props.controller
    this.state = {
      el: null
    }
  }

  handleOpen(event) {
    this.setState({
      el: event.currentTarget
    })
  }

  handleClose() {
    this.setState({
      el: null
    })
  }

  handleUploadFiles() {

  }

  handleUploadFolders() {

  }

  renderUploadButtons() {
    const { classes } = this.props
    return (
      <div className={classes.uploadButtonsContainer}>
        <UploadButton
          classes={{ root: [classes.button, classes.marginBottom].join(' ') }}
          color={color}
          size={buttonSize}
          variant='contained'
          onClick={this.handleUploadFiles}
        >
          File upload
        </UploadButton>
        <UploadButton
          classes={{ root: classes.button }}
          color={color}
          size={buttonSize}
          variant='contained'
          onClick={this.handleUploadFolders}
        >
          Folder upload
        </UploadButton>
      </div>
    )
  }

  render() {
    const { viewType, onViewTypeChange, classes, showInfo, onShowInfoChange } = this.props
    const { el } = this.state
    return (
      <div className={classes.toolbar}>
        <div className={[classes.buttons, classes.leftSection].join(' ')}>
          <Button
            classes={{ root: classes.button }}
            color={color}
            size={buttonSize}
            variant='outlined'
            startIcon={<CreateNewFolderIcon />}
            onClick={this.controller.handleNewFolderClick}
          >
            {messages.get(messages.NEW_FOLDER)}
          </Button>
        </div>
        <div className={[classes.buttons, classes.rightSection].join(' ')}>
          <ToggleButton
            classes={{ root: classes.toggleButton }}
            color={color}
            size={buttonSize}
            selected={showInfo}
            onChange={onShowInfoChange}
            value={messages.get(messages.INFO)}
            aria-label={messages.get(messages.INFO)}
          >
            <InfoIcon />
          </ToggleButton>
          <IconButton
            classes={{ root: classes.button }}
            color={color}
            size={iconButtonSize}
            variant='outlined'
          >
            <SearchIcon />
          </IconButton>
          {viewType === 'list' && (
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
          {viewType === 'grid' && (
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
          <IconButton
            classes={{ root: classes.button }}
            color={color}
            size={iconButtonSize}
            variant='outlined'
          >
            <SettingsIcon />
          </IconButton>
          <Button
            classes={{ root: classes.button }}
            color={color}
            size={buttonSize}
            variant='outlined'
            startIcon={<PublishIcon />}
            onClick={this.handleOpen}
          >
            {messages.get(messages.UPLOAD)}
          </Button>
          <Popover
            id={'toolbar.columns-popup-id'}
            open={Boolean(el)}
            anchorEl={el}
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
            <Container square={true}>{this.renderUploadButtons()}</Container>
          </Popover>
        </div>
      </div>
    )
  }
}

export default withStyles(styles)(Toolbar)
