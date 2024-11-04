import React from 'react'
import Typography from '@mui/material/Typography'
import InfoIcon from '@mui/icons-material/Info'
import WarningIcon from '@mui/icons-material/Warning'
import LockIcon from '@mui/icons-material/Lock'
import CheckCircleIcon from '@mui/icons-material/CheckCircle'
import withStyles from '@mui/styles/withStyles';
import util from '@src/js/common/util.js'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  message: {
    display: 'flex',
    '& svg': {
      marginRight: theme.spacing(1)
    },
    fontSize: theme.typography.body2.fontSize,
    color: theme.palette.text.primary
  },
  lock: {
    '& svg': {
      color: theme.palette.hint.main
    }
  },
  lock_dark: {
    '& svg': {
      color: theme.palette.hint.dark
    }
  },
  error: {
    '& svg': {
      color: theme.palette.error.main
    }
  },
  warning: {
    '& svg': {
      color: theme.palette.warning.main
    }
  },
  info: {
    '& svg': {
      color: theme.palette.info.main
    }
  },
  success: {
    '& svg': {
      color: theme.palette.success.main
    }
  }
})

class Message extends React.PureComponent {
  render() {
    logger.log(logger.DEBUG, 'Message.render')

    const { classes, styles = {}, children, type } = this.props

    return (
      <Typography
        component='div'
        className={util.classNames(classes.message, classes[type], styles.root)}
      >
        {this.renderIcon(type)}
        {children}
      </Typography>
    )
  }

  renderIcon(type) {
    if (type === 'success') {
      return <CheckCircleIcon fontSize='small' />
    } else if (type === 'info' || type === 'error') {
      return <InfoIcon fontSize='small' />
    } else if (type === 'warning') {
      return <WarningIcon fontSize='small' />
    } else if (type === 'lock' || type === 'lock_dark') {
      return <LockIcon fontSize='small' />
    } else {
      return null
    }
  }
}

export default withStyles(styles)(Message)
