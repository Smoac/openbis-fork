import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import LockIcon from '@material-ui/icons/Lock'
import logger from '@src/js/common/logger.js'

const styles = theme => ({

})

class LockLabel extends React.PureComponent {
  render() {
      logger.log(logger.DEBUG, 'LockIcon.render')

      const { fontSize='inherit', color='disabled', label } = this.props

      var icon;
      if(color == 'disabled') {
          icon = (<LockIcon fontSize={fontSize} color='disabled' />)
      } else {
        icon =(<LockIcon fontSize={fontSize} htmlColor={color} />)
      }

      if(label) {
        return <span><span style={{'verticalAlign': 'text-top'}}>{icon}</span>{label}</span>
      } else {
        return <span style={{'verticalAlign': 'text-top'}}>{icon}</span>
      }
    }

  }

  export default withStyles(styles)(LockLabel)
