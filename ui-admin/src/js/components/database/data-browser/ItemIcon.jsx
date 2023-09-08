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
import FolderIcon from '@material-ui/icons/FolderOpen.js'
import FileIcon from '@material-ui/icons/InsertDriveFileOutlined.js'
import autoBind from 'auto-bind'
import { withStyles } from '@material-ui/core/styles'

const styles = (theme) => ({
  icon: {
    verticalAlign: 'middle',
    fontSize: '6rem'
  }
})

class ItemIcon extends React.Component {
  constructor(props, context) {
    super(props, context)
    autoBind(this)

    const configuration = this.props.configuration || []

    this.extensionToIconType = new Map(
      configuration.flatMap(configObject =>
        configObject.extensions.map(extension => [extension, configObject.icon])
      )
    )
  }

  render() {
    const { classes, file } = this.props

    if (file.directory) {
      return <FolderIcon className={classes.icon} />
    } else {
      const iconType = this.extensionToIconType.get(
        file.name.substring(file.name.lastIndexOf('.') + 1)
      )
      return iconType
        ? React.createElement(iconType, { className: classes.icon })
        : <FileIcon className={classes.icon} />
    }
  }
}

export default withStyles(styles)(ItemIcon)
