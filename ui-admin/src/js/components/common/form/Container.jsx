import React from 'react'
import withStyles from '@mui/styles/withStyles';

const styles = theme => ({
  containerDefault: {
    padding: `${theme.spacing(1)} ${theme.spacing(2)}`
  },
  containerSquare: {
    padding: `${theme.spacing(2)} ${theme.spacing(2)}`
  }
})

class Container extends React.Component {
  render() {
    const { square = false, children, onClick, className, classes } = this.props

    return (
      <div
        className={`${
          square ? classes.containerSquare : classes.containerDefault
        } ${className}`}
        onClick={onClick}
      >
        {children}
      </div>
    )
  }
}

export default withStyles(styles)(Container)
