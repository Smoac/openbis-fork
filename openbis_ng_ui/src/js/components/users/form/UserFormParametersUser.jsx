import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Container from '@src/js/components/common/form/Container.jsx'
import Header from '@src/js/components/common/form/Header.jsx'
import TextField from '@src/js/components/common/form/TextField.jsx'
import CheckboxField from '@src/js/components/common/form/CheckboxField.jsx'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  field: {
    paddingBottom: theme.spacing(1)
  }
})

class UserFormParametersUser extends React.PureComponent {
  constructor(props) {
    super(props)
    this.state = {}
    this.references = {
      userId: React.createRef(),
      space: React.createRef(),
      firstName: React.createRef(),
      lastName: React.createRef(),
      email: React.createRef(),
      active: React.createRef()
    }
    this.handleChange = this.handleChange.bind(this)
    this.handleFocus = this.handleFocus.bind(this)
    this.handleBlur = this.handleBlur.bind(this)
  }

  componentDidMount() {
    this.focus()
  }

  componentDidUpdate(prevProps) {
    const prevSelection = prevProps.selection
    const selection = this.props.selection

    if (prevSelection !== selection) {
      this.focus()
    }
  }

  focus() {
    const user = this.getUser(this.props)
    if (user && this.props.selection) {
      const { part } = this.props.selection.params
      if (part) {
        const reference = this.references[part]
        if (reference && reference.current) {
          reference.current.focus()
        }
      }
    }
  }

  handleChange(event) {
    this.props.onChange('user', {
      field: event.target.name,
      value: event.target.value
    })
  }

  handleFocus(event) {
    this.props.onSelectionChange('user', {
      part: event.target.name
    })
  }

  handleBlur() {
    this.props.onBlur()
  }

  render() {
    logger.log(logger.DEBUG, 'UserFormParametersUser.render')

    const user = this.getUser(this.props)
    if (!user) {
      return null
    }

    return (
      <Container>
        <Header>User</Header>
        {this.renderUserId(user)}
        {this.renderFirstName(user)}
        {this.renderLastName(user)}
        {this.renderEmail(user)}
        {this.renderActive(user)}
      </Container>
    )
  }

  renderUserId(user) {
    const { visible, enabled, error, value } = { ...user.userId }

    if (!visible) {
      return null
    }

    const { mode, classes } = this.props
    return (
      <div className={classes.field}>
        <TextField
          reference={this.references.userId}
          label='User Id'
          name='userId'
          mandatory={true}
          error={error}
          disabled={!enabled}
          value={value}
          mode={mode}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
        />
      </div>
    )
  }

  renderFirstName(user) {
    const { visible, enabled, error, value } = { ...user.firstName }

    if (!visible) {
      return null
    }

    const { mode, classes } = this.props
    return (
      <div className={classes.field}>
        <TextField
          reference={this.references.firstName}
          label='First Name'
          name='firstName'
          error={error}
          disabled={!enabled}
          value={value}
          mode={mode}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
        />
      </div>
    )
  }

  renderLastName(user) {
    const { visible, enabled, error, value } = { ...user.lastName }

    if (!visible) {
      return null
    }

    const { mode, classes } = this.props
    return (
      <div className={classes.field}>
        <TextField
          reference={this.references.lastName}
          label='Last Name'
          name='lastName'
          error={error}
          disabled={!enabled}
          value={value}
          mode={mode}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
        />
      </div>
    )
  }

  renderEmail(user) {
    const { visible, enabled, error, value } = { ...user.email }

    if (!visible) {
      return null
    }

    const { mode, classes } = this.props
    return (
      <div className={classes.field}>
        <TextField
          reference={this.references.email}
          label='Email'
          name='email'
          error={error}
          disabled={!enabled}
          value={value}
          mode={mode}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
        />
      </div>
    )
  }

  renderActive(user) {
    const { visible, enabled, error, value } = { ...user.active }

    if (!visible) {
      return null
    }

    const { mode, classes } = this.props
    return (
      <div className={classes.field}>
        <CheckboxField
          reference={this.references.active}
          label='Active'
          name='active'
          error={error}
          disabled={!enabled}
          value={value}
          mode={mode}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
        />
      </div>
    )
  }

  getUser(props) {
    let { user, selection } = props

    if (!selection || selection.type === 'user') {
      return user
    } else {
      return null
    }
  }
}

export default withStyles(styles)(UserFormParametersUser)
