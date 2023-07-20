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

const color = 'secondary'

const styles = (theme) => ({
  toolbar: {
    flex: '0 0 auto',
    display: 'flex',
    whiteSpace: 'nowrap',
    marginLeft: theme.spacing(1),
    marginBottom: theme.spacing(1)
  },
  buttons: {
    flex: '0 0 auto',
    display: 'flex',
    whiteSpace: 'nowrap',
    '& button': {
      marginRight: theme.spacing(1)
    }
  },
  leftSection: {
    flexGrow: 1,
  },
  rightSection: {
    flexShrink: 0
  },
  toggleButton: {
    marginRight: theme.spacing(1),
    border: '1px solid ' + theme.palette[color].main + '80',
    '& *': {
      color: theme.palette[color].main
    }
  }
})

class Toolbar extends React.Component {

  constructor(props, context) {
    super(props, context)
    autoBind(this)
  }

  render() {
    const { viewType, onViewTypeChange, classes, showInfo, onShowInfoChange } =
      this.props
    const size = 'small'
    return (
      <div className={classes.toolbar}>
        <div className={[classes.buttons, classes.leftSection].join(' ')}>
          <Button
            classes={{ root: classes.button }}
            color={color}
            size={size}
            variant='outlined'
            startIcon={<CreateNewFolderIcon />}
          >
            {messages.get(messages.NEW_FOLDER)}
          </Button>
        </div>
        <div className={[classes.buttons, classes.rightSection].join(' ')}>
          <ToggleButton
            classes={{ root: classes.toggleButton }}
            color={color}
            size={size}
            selected={showInfo}
            onChange={onShowInfoChange}
            value={messages.get(messages.INFO)}
            aria-label={messages.get(messages.INFO)}
          >
            <InfoIcon />
          </ToggleButton>
          <Button
            classes={{ root: classes.button }}
            color={color}
            size={size}
            variant='outlined'
          >
            <SearchIcon />
          </Button>
          {viewType === 'list' && (
            <Button
                classes={{ root: classes.button }}
                color={color}
                size={size}
                variant='outlined'
                onClick={() => onViewTypeChange('grid')}
              >
              <ViewComfyIcon />
            </Button>
          )}
          {viewType === 'grid' && (
            <Button
              classes={{ root: classes.button }}
              color={color}
              size={size}
              variant='outlined'
              onClick={() => onViewTypeChange('list')}
            >
              <ViewListIcon />
            </Button>
          )}
          <Button
            classes={{ root: classes.button }}
            color={color}
            size={size}
            variant='outlined'
            onClick={() => onViewTypeChange('list')}
          >
            <SettingsIcon />
          </Button>
          <Button
            classes={{ root: classes.button }}
            color={color}
            size={size}
            variant='contained'
            startIcon={<PublishIcon />}
            onClick={() => onViewTypeChange('list')}
          >
            {messages.get(messages.UPLOAD)}
          </Button>
        </div>
      </div>
    )
  }
}

export default withStyles(styles)(Toolbar)
