import PageControllerChange from '@src/js/components/common/page/PageControllerChange.js'
import FormUtil from '@src/js/components/common/form/FormUtil.js'

export default class VocabularyFormControllerChange extends PageControllerChange {
  constructor(controller) {
    super(controller)
    this.gridController = controller.gridController
  }

  async execute(type, params) {
    if (type === 'vocabulary') {
      await this._handleChangeVocabulary(params)
    } else if (type === 'term') {
      await this._handleChangeTerm(params)
    }
  }

  async _handleChangeVocabulary(params) {
    await this.context.setState(state => {
      const { newObject } = FormUtil.changeObjectField(
        state.vocabulary,
        params.field,
        params.value
      )
      return {
        vocabulary: newObject
      }
    })
    await this.controller.changed(true)
  }

  async _handleChangeTerm(params) {
    await this.context.setState(state => {
      const { newCollection } = FormUtil.changeCollectionItemField(
        state.terms,
        params.id,
        params.field,
        params.value
      )
      return {
        terms: newCollection
      }
    })

    if (this.gridController) {
      await this.gridController.showSelectedRow()
    }

    await this.controller.changed(true)
  }
}
