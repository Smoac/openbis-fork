import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Message from '@src/js/components/common/form/Message.jsx'
import Button from '@src/js/components/common/form/Button.jsx'
import Dialog from '@src/js/components/common/dialog/Dialog.jsx'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  button: {
    marginLeft: theme.spacing(1)
  },
  content: {
    whiteSpace: 'pre'
  }
})

class ErrorDialog extends React.Component {
  constructor(props) {
    super(props)
  }

  render() {
    logger.log(logger.DEBUG, 'ErrorDialog.render')

    const { error, onClose } = this.props

    return (
      <Dialog
        open={!!error}
        onClose={onClose}
        title={messages.get(messages.ERROR)}
        content={this.renderContent()}
        actions={this.renderButtons()}
      />
    )
  }

  renderContent() {
    const { classes } = this.props

    const message = this.getErrorMessage()
    const stack = this.getErrorStack()

    return (
      <div className={classes.content}>
        <Message type='error'>
          {message && <div>{message}</div>}
          {stack && <div>{stack}</div>}
        </Message>
      </div>
    )
  }

  renderButtons() {
    const { onClose, classes } = this.props
    return (
      <div>
        <Button
          label={messages.get(messages.CLOSE)}
          styles={{ root: classes.button }}
          onClick={onClose}
        />
      </div>
    )
  }

  getErrorMessage() {
    const { error } = this.props

    if (error) {
      if (error.message) {
        return error.message
      } else {
        return error
      }
    } else {
      return null
    }
  }

  getErrorStack() {
    const { error } = this.props

    if (error && error.stack) {
      return error.stack
    } else {
      return null
    }
  }
}

export default withStyles(styles)(ErrorDialog)
