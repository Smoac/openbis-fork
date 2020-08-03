import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import TextField from '@material-ui/core/TextField'
import InputAdornment from '@material-ui/core/InputAdornment'
import logger from '@src/js/common/logger.js'

import FormFieldContainer from './FormFieldContainer.jsx'
import FormFieldLabel from './FormFieldLabel.jsx'
import FormFieldView from './FormFieldView.jsx'

const styles = theme => ({
  startAdornment: {
    marginRight: 0
  },
  endAdornment: {
    marginLeft: 0
  },
  textField: {
    margin: 0
  },
  input: {
    fontSize: theme.typography.body2.fontSize
  }
})

class TextFormField extends React.PureComponent {
  render() {
    logger.log(logger.DEBUG, 'TextFormField.render')

    const { mode = 'edit' } = this.props

    if (mode === 'view') {
      return this.renderView()
    } else if (mode === 'edit') {
      return this.renderEdit()
    } else {
      throw 'Unsupported mode: ' + mode
    }
  }

  renderView() {
    const { label, value } = this.props
    return <FormFieldView label={label} value={value} />
  }

  renderEdit() {
    const {
      reference,
      id,
      type,
      name,
      label,
      description,
      value,
      mandatory,
      disabled,
      autoComplete = 'off',
      error,
      multiline,
      metadata,
      startAdornment,
      endAdornment,
      styles,
      classes,
      onClick,
      onKeyPress,
      onChange,
      onFocus,
      onBlur
    } = this.props

    return (
      <FormFieldContainer
        description={description}
        error={error}
        metadata={metadata}
        styles={styles}
        onClick={onClick}
      >
        <TextField
          inputRef={reference}
          id={id}
          type={type}
          label={
            <FormFieldLabel
              label={label}
              mandatory={mandatory}
              styles={styles}
            />
          }
          InputProps={{
            startAdornment: startAdornment ? (
              <InputAdornment
                position='start'
                classes={{ positionStart: classes.startAdornment }}
              >
                {startAdornment}
              </InputAdornment>
            ) : null,
            endAdornment: endAdornment ? (
              <InputAdornment
                position='end'
                classes={{ positionEnd: classes.endAdornment }}
              >
                {endAdornment}
              </InputAdornment>
            ) : null,
            classes: {
              input: classes.input
            }
          }}
          name={name}
          value={value || ''}
          error={!!error}
          disabled={disabled}
          multiline={multiline}
          onKeyPress={onKeyPress}
          onChange={onChange}
          onFocus={onFocus}
          onBlur={onBlur}
          fullWidth={true}
          autoComplete={autoComplete}
          variant='filled'
          margin='dense'
          classes={{
            root: classes.textField
          }}
        />
      </FormFieldContainer>
    )
  }
}

export default withStyles(styles)(TextFormField)
