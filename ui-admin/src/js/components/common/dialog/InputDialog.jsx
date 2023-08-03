import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Button from '@src/js/components/common/form/Button.jsx'
import Dialog from '@src/js/components/common/dialog/Dialog.jsx'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'
import { DialogContentText } from '@material-ui/core'
import TextField from '@material-ui/core/TextField'
import autoBind from 'auto-bind'

const styles = theme => ({
  button: {
    marginLeft: theme.spacing(1)
  }
})

class InputDialog extends React.Component {
  constructor(props) {
    super(props)
    autoBind(this)

    this.state = {
      inputValue: ''
    }

    this.handleClose = this.handleClose.bind(this)
  }

  handleClose() {
    const { onCancel } = this.props
    if (onCancel) {
      onCancel()
    }
  }

  updateInputValue(event) {
    const value = event.target.value
    this.setState({
      inputValue: value
    })
  }

  handleConfirmClick() {
    const { onConfirm } = this.props
    const { inputValue } = this.state
    onConfirm(inputValue)
    this.clearInput()
  }

  handleCancelClick() {
    const { onCancel } = this.props
    onCancel()
    this.clearInput()
  }

  clearInput() {
    this.setState({
      inputValue: ''
    })
  }

  renderButtons() {
    const { classes } = this.props
    return (
      <div>
        <Button
          name='confirm'
          label={messages.get(messages.CONFIRM)}
          type={this.getButtonType()}
          styles={{ root: classes.button }}
          onClick={this.handleConfirmClick}
        />
        <Button
          name='cancel'
          label={messages.get(messages.CANCEL)}
          styles={{ root: classes.button }}
          onClick={this.handleCancelClick}
        />
      </div>
    )
  }

  getMessageType() {
    const type = this.getType()

    if (type === 'warning') {
      return 'warning'
    } else if (type === 'info') {
      return 'info'
    } else {
      throw new Error('Unsupported type: ' + type)
    }
  }

  getButtonType() {
    const type = this.getType()

    if (type === 'warning') {
      return 'risky'
    } else if (type === 'info') {
      return null
    } else {
      throw new Error('Unsupported type: ' + type)
    }
  }

  getType() {
    return this.props.type || 'warning'
  }

  render() {
    logger.log(logger.DEBUG, 'ConfirmationDialog.render')

    const { open, title, inputLabel, inputType, content } = this.props
    const { inputValue } = this.state

    return (
      <Dialog
        open={open}
        onClose={this.handleClose}
        title={title || messages.get(messages.INPUT)}
        content={[<DialogContentText key='dialog-content'>{content}</DialogContentText>,
          <TextField
            key='dialog-text'
            autoFocus
            margin='dense'
            label={inputLabel}
            type={inputType || 'text'}
            fullWidth
            variant='standard'
            value={inputValue}
            onChange={this.updateInputValue}
            />]}
        actions={this.renderButtons()}
      />
    )
  }
}

export default withStyles(styles)(InputDialog)
