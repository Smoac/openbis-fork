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
import Link from "@material-ui/core/Link";

const buttonSize = 'small'

const styles = theme => ({
  navigationBar: {
    flex: '0 0 auto',
    display: 'flex',
    whiteSpace: 'nowrap',
    marginLeft: theme.spacing(1),
    marginRight: theme.spacing(1),
  },
  link: {
    fontSize: theme.typography.body2.fontSize
  }
})

class NavigationBar extends React.Component {
  constructor(props, context) {
    super(props, context)
    autoBind(this)
  }

  splitPath(path) {
    const folders = path.split('/').filter((folder) => folder.length > 0)
    let paths = new Array(folders.length)

    if (paths.length > 0) {
      paths[0] = '/' + folders[0]
      for (let i = 1; i < paths.length; i++) {
        paths[i] = paths[i - 1] + '/' + folders[i]
      }
    }

    return { folders, paths }
  }

  renderLinks() {
    const { classes, path, onPathChange } = this.props
    const { folders, paths } = this.splitPath(path)
    const components = new Array(2 * paths.length + 1)

    components[0] = '/'
    for (let i = 0; i < paths.length; i++) {
      components[2 * i + 1] = <Link
        key={'path-' + i}
        classes={{ root: classes.link }}
        component="button"
        onClick={() => onPathChange(paths[i])}
        disabled={i === path.length - 1}
      >
        {folders[i]}
      </Link>
      components[2 * i + 2] = '/'
    }

    return components
  }

  render() {
    logger.log(logger.DEBUG, 'NavigationBar.render')

    return (
      <Container>
        { this.renderLinks() }
      </Container>
    )
  }
}

export default withStyles(styles)(NavigationBar)
