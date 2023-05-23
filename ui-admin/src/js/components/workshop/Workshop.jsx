import React from 'react'
import { withStyles } from '@material-ui/core/styles'

const styles = () => ({
  container: {
    display: 'flex',
    width: '100%'
  }
})

class Workshop extends React.PureComponent {
  render() {
    return "Hello World!"
  }
}

export default withStyles(styles)(Workshop)
