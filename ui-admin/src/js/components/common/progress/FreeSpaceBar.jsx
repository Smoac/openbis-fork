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
import { withStyles } from "@material-ui/core/styles"
import LinearProgress from '@material-ui/core/LinearProgress'
import Typography from '@material-ui/core/Typography'
import Box from '@material-ui/core/Box'

const styles = theme => ({
  root: {
    width: '100%'
  },
  progressRoot: {
    width: '100%'
  },
  bar: {
    width: '100%',
    borderRadius: '5px',
    height: '10px',
  },
})

class FreeSpaceBar extends React.Component {

  sizeToString(bytes) {
    if (!bytes) {
      return null
    }

    if (typeof bytes == "string") {
      bytes = parseInt(bytes)
    }

    let size
    let unit
    const kbytes = bytes / 1024.0
    const mbytes = kbytes / 1024.0
    const gbytes = mbytes / 1024.0
    const tbytes = gbytes / 1024.0
    if (tbytes > 1.0) {
      size = tbytes
      unit = 'TB'
    } else if (gbytes > 1.0) {
      size = gbytes
      unit = 'GB'
    } else if (mbytes > 1.0) {
      size = mbytes
      unit = 'MB'
    } else if (kbytes > 1.0) {
      size = kbytes
      unit = 'kB'
    } else {
      size = bytes
      unit = 'bytes'
    }
    return size.toFixed(1) + '\xa0' + unit;
  }

  render() {
    const {free, total, classes} = this.props;
    const used = total - free
    const value = Math.round(used * 100 / total);
    return (
      <Box className={classes.root} alignItems='center'>
        <Box className={classes.progressRoot} mr={1}>
          <LinearProgress className={classes.bar} variant='determinate' value={value} />
        </Box>
        <Box minWidth={35}>
          <Typography variant='body2' color='textSecondary'>
            {`${this.sizeToString(used)} / ${this.sizeToString(total)}`}
          </Typography>
        </Box>
      </Box>
    )
  }
}

export default withStyles(styles)(FreeSpaceBar)