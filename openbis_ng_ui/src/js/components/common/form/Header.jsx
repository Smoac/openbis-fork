import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Typography from '@material-ui/core/Typography'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  header: {
    paddingBottom: theme.spacing(1)
  }
})

class Header extends React.PureComponent {
  render() {
    logger.log(logger.DEBUG, 'Header.render')

    const { classes } = this.props

    return (
      <Typography variant='h6' className={classes.header}>
        {this.props.children}
      </Typography>
    )
  }
}

export default withStyles(styles)(Header)
