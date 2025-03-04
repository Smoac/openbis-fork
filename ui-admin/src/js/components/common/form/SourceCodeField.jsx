import React from 'react'
import autoBind from 'auto-bind'
import withStyles from '@mui/styles/withStyles';
import { highlight, languages } from 'prismjs/components/prism-core.js'
import 'prismjs/components/prism-clike.js'
import 'prismjs/components/prism-python.js'
import 'prismjs/components/prism-sql.js'
import 'prismjs/components/prism-log.js'
import 'prismjs/themes/prism.css'
import Editor from 'react-simple-code-editor'
import InputLabel from '@mui/material/InputLabel'
import FormFieldLabel from '@src/js/components/common/form/FormFieldLabel.jsx'
import FormFieldContainer from '@src/js/components/common/form/FormFieldContainer.jsx'
import FormFieldView from '@src/js/components/common/form/FormFieldView.jsx'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  container: {
    overflowX: 'auto'
  },
  view: {
    fontFamily: theme.typography.sourceCode.fontFamily,
    fontSize: theme.typography.body2.fontSize,
    whiteSpace: 'pre',
    tabSize: 4
  },

  edit: {
    fontFamily: theme.typography.sourceCode.fontFamily,
    fontSize: theme.typography.body2.fontSize,
    lineHeight: theme.typography.body2.lineHeight,
    backgroundColor: theme.palette.background.field,
    tabSize: 4,
    '& *': {
      background: 'none !important'
    },
    '& textarea': {
      whiteSpace: 'pre !important',
      padding: '23px 12px 6px 12px !important',
      border: `1px solid ${theme.palette.border.primary} !important`,
      borderBottom: `1px solid ${theme.palette.border.field} !important`,
      outline: 'none !important'
    },
    '& textarea:focus': {
      borderBottom: `2px solid ${theme.palette.primary.main} !important`
    },
    '& pre': {
      whiteSpace: 'pre !important',
      padding: '23px 12px 6px 12px !important'
    }
  },
  error: {
    '&$edit textarea': {
      borderBottom: `2px solid ${theme.palette.error.main} !important`
    }
  },
  disabled: {
    '&$edit pre': {
      opacity: 0.5
    }
  }
})

class SourceCodeField extends React.PureComponent {
  static defaultProps = {
    mode: 'edit',
    variant: 'filled'
  }

  constructor(props) {
    super(props)
    autoBind(this)
    this.state = { focused: false }
    this.containerRef = React.createRef()
  }

  handleValueChange(value) {
    const { name, onChange } = this.props
    if (onChange) {
      onChange({
        target: {
          name,
          value
        }
      })
    }
  }

  handleFocus() {
    this.setState({
      focused: true
    })

    const { onFocus } = this.props

    if (onFocus) {
      onFocus(event)
    }
  }

  handleBlur(event) {
    this.setState({
      focused: false
    })

    const { onBlur } = this.props

    if (onBlur) {
      onBlur(event)
    }
  }

  componentDidUpdate() {
    const { reference } = this.props
    if (reference) {
      const containerElement = this.containerRef.current
      if (containerElement) {
        reference.current = containerElement.querySelector('textarea')
      }
    }
  }

  render() {
    logger.log(logger.DEBUG, 'SourceCodeField.render')

    const { mode } = this.props

    if (mode === 'view') {
      return this.renderView()
    } else if (mode === 'edit') {
      return this.renderEdit()
    } else {
      throw 'Unsupported mode: ' + mode
    }
  }

  renderView() {
    const { label, value, classes } = this.props
    const html = {
      __html: highlight(value || '', this.getLanguageDefinition())
    }

    return (
      <FormFieldView
        label={label}
        value={<div className={classes.view} dangerouslySetInnerHTML={html} />}
        classes={{
          container: classes.container
        }}
      />
    )
  }

  renderEdit() {
    const {
      name,
      label,
      value,
      description,
      mandatory,
      disabled,
      readOnly,
      error,
      variant,
      onClick,
      styles,
      classes
    } = this.props

    const { focused } = this.state

    return (
      <FormFieldContainer
        description={description}
        error={error}
        styles={styles}
        onClick={onClick}
        classes={{
          container: classes.container
        }}
      >
        <div
          ref={this.containerRef}
          className={`
            ${classes.edit} 
            ${error ? classes.error : ''} 
            ${disabled ? classes.disabled : ''}
          `}
        >
          <InputLabel
            shrink={!!value || focused}
            error={!!error}
            variant={variant}
            margin='dense'
          >
            <FormFieldLabel
              label={label}
              mandatory={mandatory}
              styles={styles}
              onClick={onClick}
            />
          </InputLabel>
          <Editor
            name={name}
            value={value || ''}
            highlight={code => highlight(code, this.getLanguageDefinition())}
            disabled={disabled}
            readOnly={readOnly}
            tabSize={4}
            onValueChange={this.handleValueChange}
            onFocus={this.handleFocus}
            onBlur={this.handleBlur}
          />
        </div>
      </FormFieldContainer>
    )
  }

  getLanguageDefinition() {
    const { language } = this.props
    if (language === 'python') {
      return languages.python
    } else if (language === 'sql') {
      return languages.sql
    } else if (language === 'log') {
      return languages.log
    } else {
      throw new Error('Unsupported language: ' + language)
    }
  }
}

export default withStyles(styles)(SourceCodeField)
