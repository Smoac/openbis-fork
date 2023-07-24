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
import messages from '@src/js/common/messages.js'
import { withStyles } from '@material-ui/core/styles'

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
  render() {
    const { buttonSize, controller, classes } = this.props
    return (
      <div className={classes.buttons}>
        <Button
          classes={{ root: classes.button }}
          color={color}
          size={buttonSize}
          variant='outlined'
          startIcon={<CreateNewFolderIcon />}
          onClick={controller.handleNewFolderClick}
        >
          {messages.get(messages.NEW_FOLDER)}
        </Button>
      </div>
    )
  }
}

export default withStyles(styles)(LeftToolbar)
