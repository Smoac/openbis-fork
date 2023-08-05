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
import logger from "@src/js/common/logger.js";
import Container from "@src/js/components/common/form/Container.jsx";

const buttonSize = 'small'

const styles = theme => ({
  navigationBar: {
    flex: '0 0 auto',
    display: 'flex',
    whiteSpace: 'nowrap',
    marginLeft: theme.spacing(1),
    marginRight: theme.spacing(1)
  }
})

class NavigationBar extends React.Component {
  constructor(props, context) {
    super(props, context)
    autoBind(this)

    this.controller = this.props.controller
  }

  handleUploadFiles() {}

  handleUploadFolders() {}

  render() {
    logger.log(logger.DEBUG, 'NavigationBar.render')

    const { classes, path } = this.props
    return (
      <Container>
        { path }
      </Container>
    )
  }
}

export default withStyles(styles)(NavigationBar)
