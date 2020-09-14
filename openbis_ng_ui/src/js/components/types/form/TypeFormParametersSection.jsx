import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Container from '@src/js/components/common/form/Container.jsx'
import Header from '@src/js/components/common/form/Header.jsx'
import TextField from '@src/js/components/common/form/TextField.jsx'
import TypeFormSelectionType from '@src/js/components/types/form/TypeFormSelectionType.js'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  field: {
    paddingBottom: theme.spacing(1)
  }
})

class TypeFormParametersSection extends React.PureComponent {
  constructor(props) {
    super(props)
    this.reference = React.createRef()
    this.handleChange = this.handleChange.bind(this)
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
    const section = this.getSection(this.props)
    if (section && this.reference && this.reference.current) {
      this.reference.current.focus()
    }
  }

  handleChange(event) {
    const section = this.getSection(this.props)
    this.props.onChange(TypeFormSelectionType.SECTION, {
      id: section.id,
      field: event.target.name,
      value: event.target.value
    })
  }

  handleBlur() {
    this.props.onBlur()
  }

  render() {
    logger.log(logger.DEBUG, 'TypeFormParametersSection.render')

    const section = this.getSection(this.props)
    if (!section) {
      return null
    }

    return (
      <Container>
        <Header>Section</Header>
        {this.renderName(section)}
      </Container>
    )
  }

  renderName(section) {
    const { visible, enabled, error, value } = { ...section.name }

    if (!visible) {
      return null
    }

    const { mode, classes } = this.props
    return (
      <div className={classes.field}>
        <TextField
          reference={this.reference}
          label='Name'
          name='name'
          error={error}
          disabled={!enabled}
          value={value}
          mode={mode}
          onChange={this.handleChange}
          onBlur={this.handleBlur}
        />
      </div>
    )
  }

  getSection(props) {
    let { sections, selection } = props

    if (selection && selection.type === TypeFormSelectionType.SECTION) {
      let [section] = sections.filter(
        section => section.id === selection.params.id
      )
      return section
    } else {
      return null
    }
  }
}

export default withStyles(styles)(TypeFormParametersSection)
