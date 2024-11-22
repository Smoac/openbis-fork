import React from 'react'
import Link from '@mui/material/Link'
import Collapse from '@mui/material/Collapse'
import messages from '@src/js/common/messages.js'

export default class HistoryGridContentCell extends React.PureComponent {
  constructor(props) {
    super(props)
    this.state = {
      visible: false
    }
    this.handleVisibilityChange = this.handleVisibilityChange.bind(this)
  }

  handleVisibilityChange() {
    this.setState(state => ({
      visible: !state.visible
    }))
  }

  render() {
    const { value } = this.props
    const { visible } = this.state

    if (value) {
      return (
        <div>
          <Link
            underline='none'
            onClick={() => {
              this.handleVisibilityChange()
            }}
          >
            {visible
              ? messages.get(messages.HIDE)
              : messages.get(messages.SHOW)}
          </Link>
          <Collapse in={visible} mountOnEnter={true} unmountOnExit={true}>
            <pre>{value}</pre>
          </Collapse>
        </div>
      )
    } else {
      return null
    }
  }
}
