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
import Button from '@material-ui/core/Button'
import CreateNewFolderIcon from '@material-ui/icons/CreateNewFolderOutlined'
import DownloadIcon from '@material-ui/icons/GetApp'
import DeleteIcon from '@material-ui/icons/Delete'
import messages from '@src/js/common/messages.js'
import { withStyles } from '@material-ui/core/styles'
import logger from "@src/js/common/logger.js";
import autoBind from "auto-bind";

const color = 'secondary'

const styles = theme => ({
  buttons: {
    flex: '1 0 auto',
    display: 'flex',
    alignItems: 'center',
    whiteSpace: 'nowrap'
  },
  toggleButton: {}
})

class LeftToolbar extends React.Component {

  constructor(props, context) {
    super(props, context)
    autoBind(this)

    this.controller = this.props.controller
  }

  renderNoSelectionContextToolbar() {
    const { classes, buttonSize } = this.props
    return (
      <div className={classes.buttons}>
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
    )
  }

  renderSelectionContextToolbar() {
    const { classes, buttonSize } = this.props
    return (
      <div className={classes.buttons}>
        <Button
          classes={{ root: classes.button }}
          color={color}
          size={buttonSize}
          variant='outlined'
          startIcon={<DownloadIcon />}
          onClick={this.controller.handleNewFolderClick}
        >
          {messages.get(messages.DOWNLOAD)}
        </Button>
        <Button
          classes={{ root: classes.button }}
          color={color}
          size={buttonSize}
          variant='outlined'
          startIcon={<DeleteIcon />}
          onClick={this.controller.handleNewFolderClick}
        >
          {messages.get(messages.DELETE)}
        </Button>
      </div>
    )
  }

  render() {
    logger.log(logger.DEBUG, 'LeftToolbar.render')

    const { selectedFile } = this.props
    return selectedFile
        ? this.renderSelectionContextToolbar()
        : this.renderNoSelectionContextToolbar()
  }
}

export default withStyles(styles)(LeftToolbar)
