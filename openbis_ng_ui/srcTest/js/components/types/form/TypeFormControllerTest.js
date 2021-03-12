import ComponentContext from '@srcTest/js/components/common/ComponentContext.js'
import TypeFormControler from '@src/js/components/types/form/TypeFormController.js'
import TypeFormFacade from '@src/js/components/types/form/TypeFormFacade'

jest.mock('@src/js/components/types/form/TypeFormFacade')

export default class TypeFormControllerTest {
  static SUITE = 'TypeFormController'

  beforeEach() {
    jest.resetAllMocks()
  }

  init(object) {
    this.context = new ComponentContext()
    this.context.setProps({
      object
    })
    this.facade = new TypeFormFacade()
    this.controller = new TypeFormControler(this.facade)
    this.controller.init(this.context)
  }

  afterEach() {
    expect(this.facade.loadType).toHaveBeenCalledWith(
      this.context.getProps().object
    )
  }
}
