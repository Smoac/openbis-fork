import React from 'react'
import ConfirmationDialog from '@src/js/components/common/dialog/ConfirmationDialog.jsx'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'

class BrowserDialogRemoveNode extends React.Component {
  render() {
    logger.log(logger.DEBUG, 'BrowserDialogRemoveNode.render')

    const { open, onConfirm, onCancel } = this.props

    return (
      <ConfirmationDialog
        open={open}
        onConfirm={onConfirm}
        onCancel={onCancel}
        title={this.getTitle()}
        content={this.getContent()}
      />
    )
  }

  getTitle() {
    const { node } = this.props

    if (node) {
      return messages.get(messages.CONFIRMATION_REMOVE, node.text)
    } else {
      return null
    }
  }

  getContent() {
    const { node } = this.props

    if (node) {
      return messages.get(messages.CONFIRMATION_REMOVE, node.text)
    } else {
      return null
    }
  }
}

export default BrowserDialogRemoveNode
