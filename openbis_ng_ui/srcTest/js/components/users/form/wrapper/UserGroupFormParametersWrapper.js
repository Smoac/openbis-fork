import BaseWrapper from '@srcTest/js/components/common/wrapper/BaseWrapper.js'
import UserGroupFormParametersGroup from '@src/js/components/users/form/UserGroupFormParametersGroup.jsx'
import UserGroupFormParametersGroupWrapper from './UserGroupFormParametersGroupWrapper.js'
import UserGroupFormParametersUser from '@src/js/components/users/form/UserGroupFormParametersUser.jsx'
import UserGroupFormParametersUserWrapper from './UserGroupFormParametersUserWrapper.js'
import RoleParameters from '@src/js/components/users/form/common/RoleParameters.jsx'
import RoleParametersWrapper from '@srcTest/js/components/users/form/common/wrapper/RoleParametersWrapper.js'

export default class UserGroupFormParametersWrapper extends BaseWrapper {
  getGroup() {
    return new UserGroupFormParametersGroupWrapper(
      this.findComponent(UserGroupFormParametersGroup)
    )
  }

  getUser() {
    return new UserGroupFormParametersUserWrapper(
      this.findComponent(UserGroupFormParametersUser)
    )
  }

  getRole() {
    return new RoleParametersWrapper(this.findComponent(RoleParameters))
  }

  toJSON() {
    return {
      group: this.getGroup().toJSON(),
      user: this.getUser().toJSON(),
      role: this.getRole().toJSON()
    }
  }
}
