import UserBrowserController from '@src/js/components/users/browser/UserBrowserController.js'
import ComponentContext from '@srcTest/js/components/common/ComponentContext.js'
import pages from '@src/js/common/consts/pages.js'
import objectType from '@src/js/common/consts/objectType.js'
import actions from '@src/js/store/actions/actions.js'

export default class UserBrowserControllerTest {
  static SUITE = 'UserBrowserController'

  beforeEach() {
    jest.resetAllMocks()

    this.context = new ComponentContext()
    this.controller = new UserBrowserController()
    this.controller.init(this.context)
  }

  expectOpenUserAction(userId) {
    this.context.expectAction(
      actions.objectOpen(pages.USERS, objectType.USER, userId)
    )
  }

  expectOpenGroupAction(groupId) {
    this.context.expectAction(
      actions.objectOpen(pages.USERS, objectType.USER_GROUP, groupId)
    )
  }

  expectOpenUsersOverviewAction() {
    this.context.expectAction(
      actions.objectOpen(pages.USERS, objectType.OVERVIEW, objectType.USER)
    )
  }
}
