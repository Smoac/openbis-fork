import PageMode from '@src/js/components/common/page/PageMode.js'
import FormValidator from '@src/js/components/common/form/FormValidator.js'
import actions from '@src/js/store/actions/actions.js'

export default class PageControllerLoad {
  constructor(controller) {
    this.controller = controller
    this.context = controller.getContext()
    this.facade = controller.getFacade()
    this.object = controller.getObject()
  }

  // eslint-disable-next-line no-unused-vars
  async load(object, isNew) {
    throw 'Method not implemented'
  }

  async execute() {
    try {
      await this.context.setState({
        loading: true,
        validate: FormValidator.MODE_BASIC
      })

      const isNew = this.object.type === this.controller.getNewObjectType()

      if (isNew) {
        await this.context.setState({
          mode: PageMode.EDIT
        })
      } else {
        await this.context.setState({
          mode: PageMode.VIEW
        })
      }

      await this.load(this.object, isNew)
    } catch (error) {
      this.context.dispatch(actions.errorChange(error))
    } finally {
      this.controller.changed(false)
      this.context.setState({
        loaded: true,
        loading: false
      })
    }
  }
}
