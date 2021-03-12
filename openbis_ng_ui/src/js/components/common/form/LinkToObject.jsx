import _ from 'lodash'
import React from 'react'
import autoBind from 'auto-bind'
import { connect } from 'react-redux'
import { withStyles } from '@material-ui/core/styles'
import Link from '@material-ui/core/Link'
import actions from '@src/js/store/actions/actions.js'

const styles = () => ({
  link: {
    fontSize: 'inherit'
  }
})

function mapDispatchToProps(dispatch) {
  return {
    objectOpen: (page, objectType, objectId) => {
      dispatch(actions.objectOpen(page, objectType, objectId))
    }
  }
}

class LinkToObject extends React.Component {
  constructor(props) {
    super(props)
    autoBind(this)
  }

  handleClick() {
    const { page, object } = this.props
    this.props.objectOpen(page, object.type, object.id)
  }

  render() {
    const { children, classes } = this.props
    return (
      <Link
        component='button'
        classes={{ root: classes.link }}
        onClick={this.handleClick}
      >
        {children}
      </Link>
    )
  }
}

export default _.flow(
  connect(null, mapDispatchToProps),
  withStyles(styles)
)(LinkToObject)
