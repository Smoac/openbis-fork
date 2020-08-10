import FormUtil from '@src/js/components/common/form/FormUtil.js'

export default class TypeFormControllerAddSection {
  constructor(controller) {
    this.controller = controller
    this.context = controller.context
  }

  execute() {
    let { sections, sectionsCounter, selection } = this.context.getState()

    let newSections = Array.from(sections)
    let newSection = {
      id: 'section-' + sectionsCounter++,
      name: FormUtil.createField(),
      properties: []
    }
    let newSelection = {
      type: 'section',
      params: {
        id: newSection.id
      }
    }

    if (selection) {
      if (selection.type === 'section') {
        let index = sections.findIndex(
          section => section.id === selection.params.id
        )
        newSections.splice(index + 1, 0, newSection)
      } else if (selection.type === 'property') {
        let index = sections.findIndex(
          section => section.properties.indexOf(selection.params.id) !== -1
        )
        newSections.splice(index + 1, 0, newSection)
      } else {
        newSections.push(newSection)
      }
    } else {
      newSections.push(newSection)
    }

    this.context.setState(state => ({
      ...state,
      sections: newSections,
      sectionsCounter,
      selection: newSelection
    }))

    this.controller.changed(true)
  }
}
