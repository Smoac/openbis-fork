import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import AutocompleterField from '@src/js/components/common/form/AutocompleterField.jsx'
import CheckboxField from '@src/js/components/common/form/CheckboxField.jsx'
import TextField from '@src/js/components/common/form/TextField.jsx'
import SelectField from '@src/js/components/common/form/SelectField.jsx'
import openbis from '@src/js/services/openbis.js'
import logger from '@src/js/common/logger.js'

import TypeFormMessage from './TypeFormMessage.jsx'
import TypeFormHeader from './TypeFormHeader.jsx'

const styles = theme => ({
  container: {
    padding: theme.spacing(2)
  },
  header: {
    paddingBottom: theme.spacing(2)
  },
  field: {
    paddingBottom: theme.spacing(2)
  }
})

class TypeFormParametersProperty extends React.PureComponent {
  constructor(props) {
    super(props)
    this.state = {}
    this.references = {
      code: React.createRef(),
      label: React.createRef(),
      description: React.createRef(),
      dataType: React.createRef(),
      vocabulary: React.createRef(),
      materialType: React.createRef(),
      schema: React.createRef(),
      transformation: React.createRef(),
      initialValueForExistingEntities: React.createRef(),
      mandatory: React.createRef(),
      plugin: React.createRef(),
      showInEditView: React.createRef()
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
    const property = this.getProperty(this.props)
    if (property) {
      const { part } = this.props.selection.params
      if (part) {
        const reference = this.references[part]
        if (reference && reference.current) {
          reference.current.focus()
        }
      }
    }
  }

  params(event) {
    const property = this.getProperty(this.props)

    return {
      id: property.id,
      field: event.target.name,
      part: event.target.name,
      value: event.target.value
    }
  }

  handleChange(event) {
    this.props.onChange('property', this.params(event))
  }

  handleFocus(event) {
    this.props.onSelectionChange('property', this.params(event))
  }

  handleBlur(event) {
    this.props.onBlur('property', this.params(event))
  }

  render() {
    logger.log(logger.DEBUG, 'TypeFormParametersProperty.render')

    const property = this.getProperty(this.props)
    if (!property) {
      return null
    }

    const { classes } = this.props

    return (
      <div className={classes.container}>
        <TypeFormHeader className={classes.header}>Property</TypeFormHeader>
        {this.renderMessageGlobal(property)}
        {this.renderMessageUsage(property)}
        {this.renderMessageAssignments(property)}
        {this.renderScope(property)}
        {this.renderCode(property)}
        {this.renderDataType(property)}
        {this.renderVocabulary(property)}
        {this.renderMaterialType(property)}
        {this.renderSchema(property)}
        {this.renderTransformation(property)}
        {this.renderLabel(property)}
        {this.renderDescription(property)}
        {this.renderDynamicPlugin(property)}
        {this.renderVisible(property)}
        {this.renderMandatory(property)}
        {this.renderInitialValue(property)}
      </div>
    )
  }

  renderMessageGlobal(property) {
    if (property.scope.value === 'global') {
      const { classes } = this.props
      return (
        <div className={classes.field}>
          <TypeFormMessage type='warning'>
            This property is global. Changes will also influence other types
            where this property is used.
          </TypeFormMessage>
        </div>
      )
    } else {
      return null
    }
  }

  renderMessageUsage(property) {
    const { classes } = this.props

    function entities(number) {
      return number === 0 || number > 1
        ? `${number} entities`
        : `${number} entity`
    }

    function message(property) {
      return `This property is already used by ${entities(
        property.usagesGlobal
      )} (${entities(property.usagesLocal)} of this type and ${entities(
        property.usagesGlobal - property.usagesLocal
      )} of other types).`
    }

    if (property.usagesLocal !== 0 || property.usagesGlobal !== 0) {
      return (
        <div className={classes.field}>
          <TypeFormMessage type='info'>{message(property)}</TypeFormMessage>
        </div>
      )
    } else {
      return null
    }
  }

  renderMessageAssignments(property) {
    const { classes } = this.props

    function types(number) {
      return number === 0 || number > 1 ? `${number} types` : `${number} type`
    }

    function message(property) {
      return `This property is already assigned to ${types(
        property.assignments
      )}.`
    }

    if (
      (property.original && property.assignments > 1) ||
      (!property.original && property.assignments > 0)
    ) {
      return (
        <div className={classes.field}>
          <TypeFormMessage type='info'>{message(property)}</TypeFormMessage>
        </div>
      )
    } else {
      return null
    }
  }

  renderScope(property) {
    const { visible, enabled, error, value } = { ...property.scope }

    if (!visible) {
      return null
    }

    const options = [
      { label: 'Local', value: 'local' },
      { label: 'Global', value: 'global' }
    ]

    const { classes } = this.props
    return (
      <div className={classes.field}>
        <SelectField
          reference={this.references.scope}
          label='Scope'
          name='scope'
          mandatory={true}
          error={error}
          disabled={!enabled}
          value={value}
          options={options}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
        />
      </div>
    )
  }

  renderLabel(property) {
    const { visible, enabled, error, value } = { ...property.label }

    if (!visible) {
      return null
    }

    const { classes } = this.props
    return (
      <div className={classes.field}>
        <TextField
          reference={this.references.label}
          label='Label'
          name='label'
          mandatory={true}
          error={error}
          disabled={!enabled}
          value={value}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
        />
      </div>
    )
  }

  renderCode(property) {
    const { visible, enabled, error, value } = { ...property.code }

    if (!visible) {
      return null
    }

    const { classes, controller } = this.props

    if (property.scope.value === 'local') {
      return (
        <div className={classes.field}>
          <TextField
            reference={this.references.code}
            label='Code'
            name='code'
            mandatory={true}
            error={error}
            disabled={!enabled}
            value={value}
            onChange={this.handleChange}
            onFocus={this.handleFocus}
            onBlur={this.handleBlur}
          />
        </div>
      )
    } else if (property.scope.value === 'global') {
      const { globalPropertyTypes = [] } = controller.getDictionaries()

      const options = globalPropertyTypes.map(globalPropertyType => {
        return globalPropertyType.code
      })

      return (
        <div className={classes.field}>
          <AutocompleterField
            reference={this.references.code}
            label='Code'
            name='code'
            options={options}
            mandatory={true}
            error={error}
            disabled={!enabled}
            value={value}
            onChange={this.handleChange}
            onFocus={this.handleFocus}
            onBlur={this.handleBlur}
          />
        </div>
      )
    }
  }

  renderDescription(property) {
    const { visible, enabled, error, value } = { ...property.description }

    if (!visible) {
      return null
    }

    const { classes } = this.props
    return (
      <div className={classes.field}>
        <TextField
          reference={this.references.description}
          label='Description'
          name='description'
          mandatory={true}
          error={error}
          disabled={!enabled}
          value={value}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
        />
      </div>
    )
  }

  renderDataType(property) {
    const { visible, enabled, error, value } = { ...property.dataType }

    if (!visible) {
      return null
    }

    const options = openbis.DataType.values.sort().map(dataType => {
      return {
        label: dataType,
        value: dataType
      }
    })

    const { classes } = this.props
    return (
      <div className={classes.field}>
        <SelectField
          reference={this.references.dataType}
          label='Data Type'
          name='dataType'
          mandatory={true}
          error={error}
          disabled={!enabled}
          value={value}
          options={options}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
        />
      </div>
    )
  }

  renderVocabulary(property) {
    const { visible, enabled, error, value } = { ...property.vocabulary }

    if (!visible) {
      return null
    }

    const { classes, controller } = this.props
    const { vocabularies = [] } = controller.getDictionaries()

    let options = []

    if (vocabularies.length > 0) {
      options = vocabularies.map(vocabulary => {
        return {
          label: vocabulary.code,
          value: vocabulary.code
        }
      })
      options.unshift({})
    }

    return (
      <div className={classes.field}>
        <SelectField
          reference={this.references.vocabulary}
          label='Vocabulary'
          name='vocabulary'
          mandatory={true}
          error={error}
          disabled={!enabled}
          value={value}
          options={options}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
        />
      </div>
    )
  }

  renderMaterialType(property) {
    const { visible, enabled, error, value } = { ...property.materialType }

    if (!visible) {
      return null
    }

    const { classes, controller } = this.props
    const { materialTypes = [] } = controller.getDictionaries()

    let options = []

    if (materialTypes.length > 0) {
      options = materialTypes.map(materialType => {
        return {
          label: materialType.code,
          value: materialType.code
        }
      })
      options.unshift({})
    }

    return (
      <div className={classes.field}>
        <SelectField
          reference={this.references.materialType}
          label='Material Type'
          name='materialType'
          mandatory={true}
          error={error}
          disabled={!enabled}
          value={value}
          options={options}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
        />
      </div>
    )
  }

  renderSchema(property) {
    const { visible, enabled, error, value } = { ...property.schema }

    if (!visible) {
      return null
    }

    const { classes } = this.props

    return (
      <div className={classes.field}>
        <TextField
          reference={this.references.schema}
          label='XML Schema'
          name='schema'
          error={error}
          disabled={!enabled}
          value={value}
          multiline={true}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
        />
      </div>
    )
  }

  renderTransformation(property) {
    const { visible, enabled, error, value } = { ...property.transformation }

    if (!visible) {
      return null
    }

    const { classes } = this.props

    return (
      <div className={classes.field}>
        <TextField
          reference={this.references.transformation}
          label='XSLT Script'
          name='transformation'
          error={error}
          disabled={!enabled}
          value={value}
          multiline={true}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
        />
      </div>
    )
  }

  renderDynamicPlugin(property) {
    const { visible, enabled, error, value } = { ...property.plugin }

    if (!visible) {
      return null
    }

    const { classes, controller } = this.props
    const { dynamicPlugins = [] } = controller.getDictionaries()

    let options = []

    if (dynamicPlugins.length > 0) {
      options = dynamicPlugins.map(dynamicPlugin => {
        return {
          label: dynamicPlugin.name,
          value: dynamicPlugin.name
        }
      })
      options.unshift({})
    }

    return (
      <div className={classes.field}>
        <SelectField
          reference={this.references.plugin}
          label='Dynamic Plugin'
          name='plugin'
          error={error}
          disabled={!enabled}
          value={value}
          options={options}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
        />
      </div>
    )
  }

  renderMandatory(property) {
    const { visible, enabled, error, value } = { ...property.mandatory }

    if (!visible) {
      return null
    }

    const { classes } = this.props
    return (
      <div className={classes.field}>
        <CheckboxField
          reference={this.references.mandatory}
          label='Mandatory'
          name='mandatory'
          error={error}
          disabled={!enabled}
          value={value}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
        />
      </div>
    )
  }

  renderInitialValue(property) {
    const { visible, enabled, error, value } = {
      ...property.initialValueForExistingEntities
    }

    if (!visible) {
      return null
    }

    const { classes } = this.props
    return (
      <div className={classes.field}>
        <TextField
          reference={this.references.initialValueForExistingEntities}
          label='Initial Value'
          name='initialValueForExistingEntities'
          mandatory={true}
          error={error}
          disabled={!enabled}
          value={value}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
        />
      </div>
    )
  }

  renderVisible(property) {
    const { visible, enabled, error, value } = { ...property.showInEditView }

    if (!visible) {
      return null
    }

    const { classes } = this.props
    return (
      <div className={classes.field}>
        <CheckboxField
          reference={this.references.showInEditView}
          label='Visible'
          name='showInEditView'
          error={error}
          disabled={!enabled}
          value={value}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
        />
      </div>
    )
  }

  getType() {
    return this.props.type
  }

  getProperty(props) {
    let { properties, selection } = props

    if (selection && selection.type === 'property') {
      let [property] = properties.filter(
        property => property.id === selection.params.id
      )
      return property
    } else {
      return null
    }
  }
}

export default withStyles(styles)(TypeFormParametersProperty)
