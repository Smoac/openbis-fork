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

import autoBind from 'auto-bind'
import Container from '@src/js/components/common/form/Container.jsx'
import Table from '@material-ui/core/Table'
import TableBody from '@material-ui/core/TableBody'
import TableCell from '@material-ui/core/TableCell'
import TableRow from '@material-ui/core/TableRow'
import Header from '@src/js/components/common/form/Header.jsx';
import ItemIcon from '@src/js/components/database/data-browser/ItemIcon.jsx';
import { withStyles } from "@material-ui/core/styles";

const styles = () => ({
  icon: {
    verticalAlign: 'middle',
    fontSize: '12rem'
  }
})

class InfoPanel extends React.Component {
  constructor(props, context) {
    super(props, context)
    autoBind(this)
  }

  render() {
    const {
      classes,
      file,
      configuration
    } = this.props

    // TODO: extract strings to messages
    return (file &&
      <Container>
        <Header size='big'>{file.name}</Header>
        <ItemIcon file={file} classes={classes} configuration={configuration} />
        <Table>
          <TableBody>
            <TableRow>
              <TableCell variant='head' component='th'>Size</TableCell>
              <TableCell>{file.size}</TableCell>
            </TableRow>
            <TableRow>
              <TableCell variant='head' component='th'>Created</TableCell>
              <TableCell>{file.creationTime.toLocaleString()}</TableCell>
            </TableRow>
            <TableRow>
              <TableCell variant='head' component='th'>Modified</TableCell>
              <TableCell>{file.lastModifiedTime.toLocaleString()}</TableCell>
            </TableRow>
            <TableRow>
              <TableCell variant='head' component='th'>Accessed</TableCell>
              <TableCell>{file.lastAccessTime.toLocaleString()}</TableCell>
            </TableRow>
          </TableBody>
        </Table>
      </Container>
    )
  }
}

export default withStyles(styles)(InfoPanel)
