import React from 'react'
import withStyles from '@mui/styles/withStyles';
import logger from '@src/js/common/logger.js'
import Dialog from '@mui/material/Dialog'
import CircularProgress from '@mui/material/CircularProgress'
import DialogContent from '@mui/material/DialogContent'
import DialogTitle from '@mui/material/DialogTitle'

const styles = theme => ({
  dialogPaper: {
    padding: theme.spacing(1),
    maxWidth: '80vw',
    maxHeight: '80vh'
  },
  title: {
    fontFamily: theme.typography.h6.fontFamily,
    fontSize: theme.typography.h6.fontSize,
    textAlign: 'center',
    padding: theme.spacing(2)
  },
  content: {
    fontFamily: theme.typography.body2.fontFamily,
    fontSize: theme.typography.body2.fontSize,
    textAlign: 'center',
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    justifyContent: 'center',
    padding: '0 !important'
  },
  icon: {
    margin: theme.spacing(2)
  }
})

class LoadingDialog extends React.Component {
  render() {
    logger.log(logger.DEBUG, 'LoadingDialog.render')

    const { loading, classes, variant, value, message } = this.props

    return (
      (<Dialog
        open={loading}
        classes={{ paper: classes.dialogPaper }}
        keepMounted
      >
        <DialogContent className={classes.content}>
          <CircularProgress
            size={68}
            variant={variant}
            value={value}
            className={classes.icon}
          />
        </DialogContent>
        {message ? (
          <DialogTitle className={classes.title}>
            {message}
          </DialogTitle>
        ) : null}
      </Dialog>)
    );
  }
}

export default withStyles(styles)(LoadingDialog)
