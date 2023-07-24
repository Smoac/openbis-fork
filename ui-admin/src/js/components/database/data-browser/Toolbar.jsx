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
import autoBind from 'auto-bind'
import LeftToolbar from '@src/js/components/database/data-browser/LeftToolbar.jsx'
import RightToolbar from '@src/js/components/database/data-browser/RightToolbar.jsx'

const buttonSize = 'small'

const styles = theme => ({
  toolbar: {
    flex: '0 0 auto',
    display: 'flex',
    whiteSpace: 'nowrap',
    marginLeft: theme.spacing(1),
    marginRight: theme.spacing(1)
  }
})

class Toolbar extends React.Component {
  constructor(props, context) {
    super(props, context)
    autoBind(this)

    this.controller = this.props.controller
  }

  handleUploadFiles() {}

  handleUploadFolders() {}

  render() {
    const { viewType, onViewTypeChange, classes, showInfo, onShowInfoChange } =
      this.props
    return (
      <div className={classes.toolbar}>
        <LeftToolbar buttonSize={buttonSize} controller={this.controller} />
        <RightToolbar
          buttonSize={buttonSize}
          selected={showInfo}
          onChange={onShowInfoChange}
          viewType={viewType}
          onViewTypeChange={onViewTypeChange}
          controller={this.controller}
        />
      </div>
    )
  }
}

export default withStyles(styles)(Toolbar)
