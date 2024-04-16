import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Button from '@src/js/components/common/form/Button.jsx'
import Message from '@src/js/components/common/form/Message.jsx'
import Dialog from '@src/js/components/common/dialog/Dialog.jsx'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  button: {
    marginLeft: theme.spacing(1)
  }
})

class FileExistsDialog extends React.Component {
  constructor(props) {
    super(props)
    this.handleClose = this.handleClose.bind(this)
  }

  handleClose() {
    const { onCancel } = this.props
    if (onCancel) {
      onCancel()
    }
  }

  render() {
    logger.log(logger.DEBUG, 'FileExistsDialog.render')

    const { open, title, content } = this.props

    return (
      <Dialog
        open={open}
        onClose={this.handleClose}
        title={title || messages.get(messages.CONFIRMATION)}
        content={<Message type={'warning'}>{content}</Message>}
        actions={this.renderButtons()}
      />
    )
  }

  renderButtons() {
    const { onReplace, onResume, onCancel, classes } = this.props
    return (
      <div>
        {!!onReplace && (
          <Button
            name='replace'
            label={messages.get(messages.REPLACE)}
            type={'risky'}
            styles={{ root: classes.button }}
            onClick={onReplace}
          />
        )}
        {!!onResume && (
          <Button
            name='resume'
            label={messages.get(messages.RESUME)}
            type={'risky'}
            styles={{ root: classes.button }}
            onClick={onResume}
          />
        )}
        {!!onCancel && (
          <Button
            name='cancel'
            label={messages.get(messages.CANCEL)}
            styles={{ root: classes.button }}
            onClick={onCancel}
          />
        )}
      </div>
    )
  }
}

export default withStyles(styles)(FileExistsDialog)
