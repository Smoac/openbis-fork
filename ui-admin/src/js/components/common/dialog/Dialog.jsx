import _ from 'lodash'
import React from 'react'
import withStyles from '@mui/styles/withStyles';
import Dialog from '@mui/material/Dialog'
import DialogActions from '@mui/material/DialogActions'
import DialogContent from '@mui/material/DialogContent'
import DialogTitle from '@mui/material/DialogTitle'
import Slide from '@mui/material/Slide'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  dialog: {
    position: 'relative',
    zIndex: '2000 !important'
  },
  title: {
    fontFamily: theme.typography.h6.fontFamily,
    fontSize: theme.typography.h6.fontSize,
    padding: theme.spacing(2)
  },
  content: {
    fontFamily: theme.typography.body2.fontFamily,
    fontSize: theme.typography.body2.fontSize,
    padding: 0,
    paddingLeft: theme.spacing(2),
    paddingRight: theme.spacing(2)
  },
  actions: {
    padding: theme.spacing(2)
  }
})

const Transition = React.forwardRef(function Transition(props, ref) {
  return <Slide ref={ref} direction='up' {...props} />
})

class DialogWindow extends React.Component {
  constructor(props) {
    super(props)
  }

  render() {
    logger.log(logger.DEBUG, 'DialogWindow.render')

    const { open, title, content, actions, onClose, classes } = this.props

    return (
      (<Dialog
        open={open}
        onClose={onClose}
        scroll='paper'
        fullWidth={true}
        maxWidth='md'
        //classes={{ root: classes.dialog }}
        TransitionComponent={Transition}
      >
        <DialogTitle classes={{ root: classes.title }}>
          {_.isFunction(title) ? title(this) : title}
        </DialogTitle>
        <DialogContent classes={{ root: classes.content }}>
          {_.isFunction(content) ? content(this) : content}
        </DialogContent>
        <DialogActions classes={{ root: classes.actions }}>
          {_.isFunction(actions) ? actions(this) : actions}
        </DialogActions>
      </Dialog>)
    );
  }
}

export default withStyles(styles)(DialogWindow)
