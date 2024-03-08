/*
 *  Copyright ETH 2024 Zürich, Scientific IT Services
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
import PropTypes from 'prop-types'
import { makeStyles, withStyles } from "@material-ui/core/styles"
import LinearProgress from '@material-ui/core/LinearProgress'
import Typography from '@material-ui/core/Typography'
import Box from '@material-ui/core/Box'

const styles = theme => ({
  root: {
    width: '100%',
  },
})

class FreeSpaceBar extends React.Component {
  render() {
    const {free, total} = this.props;
    const value = Math.round(free * 100 / total);
    return (
      <Box display='flex' alignItems='center'>
        <Box width='100%' mr={1}>
          <LinearProgress variant='determinate' value={value} />
        </Box>
        <Box minWidth={35}>
          <Typography variant='body2' color='textSecondary'>
            {`${value}%`}
          </Typography>
        </Box>
      </Box>
    )
  }
}

export default withStyles(styles)(FreeSpaceBar)