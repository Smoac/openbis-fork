import FormUtil from '@src/js/components/common/form/FormUtil.js'

export default class TypeFormControllerAddProperty {
  constructor(controller) {
    this.controller = controller
    this.context = controller.context
  }

  execute() {
    let {
      sections,
      properties,
      propertiesCounter,
      selection
    } = this.context.getState()

    let sectionIndex = null
    let sectionPropertyIndex = null

    if (selection) {
      if (selection.type === 'section') {
        sectionIndex = sections.findIndex(
          section => section.id === selection.params.id
        )
        sectionPropertyIndex = sections[sectionIndex].properties.length
      } else if (selection.type === 'property') {
        sections.forEach((section, i) => {
          section.properties.forEach((property, j) => {
            if (property === selection.params.id) {
              sectionIndex = i
              sectionPropertyIndex = j + 1
            }
          })
        })
      }
    }

    let section = sections[sectionIndex]

    let newProperties = Array.from(properties)
    let newProperty = {
      id: 'property-' + propertiesCounter++,
      scope: FormUtil.createField({
        value: 'local'
      }),
      code: FormUtil.createField(),
      label: FormUtil.createField(),
      description: FormUtil.createField(),
      dataType: FormUtil.createField(),
      plugin: FormUtil.createField(),
      vocabulary: FormUtil.createField({
        visible: false
      }),
      materialType: FormUtil.createField({
        visible: false
      }),
      sampleType: FormUtil.createField({
        visible: false
      }),
      schema: FormUtil.createField({
        visible: false
      }),
      transformation: FormUtil.createField({
        visible: false
      }),
      mandatory: FormUtil.createField({
        value: false
      }),
      showInEditView: FormUtil.createField({
        value: true
      }),
      showRawValueInForms: FormUtil.createField({
        value: false
      }),
      initialValueForExistingEntities: FormUtil.createField({
        visible: false
      }),
      section: section.id,
      assignments: 0,
      usagesLocal: 0,
      usagesGlobal: 0,
      errors: 0
    }
    newProperties.push(newProperty)

    let newSection = {
      ...section,
      properties: Array.from(section.properties)
    }
    newSection.properties.splice(sectionPropertyIndex, 0, newProperty.id)

    let newSections = Array.from(sections)
    newSections[sectionIndex] = newSection

    let newSelection = {
      type: 'property',
      params: {
        id: newProperty.id,
        part: 'code'
      }
    }

    this.context.setState(state => ({
      ...state,
      sections: newSections,
      properties: newProperties,
      propertiesCounter,
      selection: newSelection
    }))

    this.controller.changed(true)
  }
}
