import _ from 'lodash'
import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import { connect } from 'react-redux'
import flow from 'lodash/flow'

import Card from '@material-ui/core/Card'
import Typography from '@material-ui/core/Typography'

import FormValidator from '@src/js/components/common/form/FormValidator.js'
import Container from '@src/js/components/common/form/Container.jsx'
import TextField from '@src/js/components/common/form/TextField.jsx'
import Button from '@src/js/components/common/form/Button.jsx'

import actions from '@src/js/store/actions/actions.js'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  card: {
    marginTop: '10%',
    marginBottom: '10em',
    width: '30em',
    margin: '0 auto'
  },
  header: {
    marginBottom: theme.spacing(1)
  },
  field: {
    marginBottom: theme.spacing(1)
  },
  button: {
    marginTop: theme.spacing(1)
  },
  container: {
    width: '100%',
    height: '100%',
    overflow: 'auto'
  }
})

function mapStateToProps() {
  return {}
}

function mapDispatchToProps(dispatch) {
  return {
    login: (user, password) => dispatch(actions.login(user, password))
  }
}

class WithLogin extends React.Component {
  constructor(props) {
    super(props)
    this.state = {
      user: {
        value: null,
        error: null
      },
      password: {
        value: null,
        error: null
      },
      selection: 'user',
      validate: FormValidator.MODE_BASIC
    }
    this.references = {
      user: React.createRef(),
      password: React.createRef()
    }

    this.handleKeyPress = this.handleKeyPress.bind(this)
    this.handleChange = this.handleChange.bind(this)
    this.handleBlur = this.handleBlur.bind(this)
    this.handleLogin = this.handleLogin.bind(this)
  }

  componentDidMount() {
    if (this.state.selection) {
      this.focus()
    }
  }

  componentDidUpdate(prevProps, prevState) {
    if (this.state.selection !== prevState.selection) {
      this.focus()
    }
  }

  focus() {
    const reference = this.references[this.state.selection]
    if (reference) {
      reference.current.focus()
    }
  }

  validate(autofocus) {
    const validator = new FormValidator(this.state.validate)
    validator.validateNotEmpty(this.state, 'user', messages.get(messages.USER))
    validator.validateNotEmpty(
      this.state,
      'password',
      messages.get(messages.PASSWORD)
    )

    let selection = null

    if (autofocus && !_.isEmpty(validator.getErrors())) {
      selection = new String(validator.getErrors()[0].name)
    }

    this.setState({
      ...validator.withErrors(this.state),
      selection
    })

    return _.isEmpty(validator.getErrors())
  }

  handleKeyPress(event) {
    if (event.key === 'Enter') {
      this.handleLogin()
    }
  }

  handleChange(event) {
    this.setState({
      [event.target.name]: {
        ...this.state[event.target.name],
        value: event.target.value
      }
    })
  }

  handleBlur() {
    this.validate()
  }

  handleLogin() {
    if (this.props.disabled) {
      return
    }

    this.setState(
      {
        validate: FormValidator.MODE_FULL
      },
      () => {
        if (this.validate(true)) {
          this.props.login(this.state.user.value, this.state.password.value)
        }
      }
    )
  }

  render() {
    logger.log(logger.DEBUG, 'Login.render')

    const { classes } = this.props

    return (
      <div>
        <div className={classes.container}>
          <form>
            <Card classes={{ root: classes.card }}>
              <Container square={true}>
                <Typography variant='h6' classes={{ root: classes.header }}>
                  Login
                </Typography>
                <div className={classes.field}>
                  <TextField
                    reference={this.references.user}
                    id='standard-name'
                    name='user'
                    label={messages.get(messages.USER)}
                    value={this.state.user.value}
                    error={this.state.user.error}
                    mandatory={true}
                    autoComplete='username'
                    onKeyPress={this.handleKeyPress}
                    onChange={this.handleChange}
                    onBlur={this.handleBlur}
                  />
                </div>
                <div className={classes.field}>
                  <TextField
                    reference={this.references.password}
                    id='standard-password-input'
                    name='password'
                    label={messages.get(messages.PASSWORD)}
                    type='password'
                    value={this.state.password.value}
                    error={this.state.password.error}
                    mandatory={true}
                    autoComplete='current-password'
                    onKeyPress={this.handleKeyPress}
                    onChange={this.handleChange}
                    onBlur={this.handleBlur}
                  />
                </div>
                <Button
                  label={messages.get(messages.LOGIN)}
                  type='final'
                  styles={{ root: classes.button }}
                  onClick={this.handleLogin}
                />
              </Container>
            </Card>
          </form>
        </div>
      </div>
    )
  }
}

export default flow(
  connect(mapStateToProps, mapDispatchToProps),
  withStyles(styles)
)(WithLogin)
