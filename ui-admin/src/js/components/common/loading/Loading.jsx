import React from 'react'
import withStyles from '@mui/styles/withStyles';
import Mask from '@src/js/components/common/loading/Mask.jsx'
import CircularProgress from '@mui/material/CircularProgress'
import logger from '@src/js/common/logger.js'

const styles = () => ({})

class Loading extends React.Component {
  render() {
    logger.log(logger.DEBUG, 'Loading.render')

    const { loading, styles, children } = this.props

    return (
      <Mask visible={loading} styles={styles} icon={<CircularProgress />}>
        {children}
      </Mask>
    )
  }
}

export default withStyles(styles)(Loading)
