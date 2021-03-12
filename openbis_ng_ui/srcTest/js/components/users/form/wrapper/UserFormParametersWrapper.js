import BaseWrapper from '@srcTest/js/components/common/wrapper/BaseWrapper.js'
import UserFormParametersUser from '@src/js/components/users/form/UserFormParametersUser.jsx'
import UserFormParametersUserWrapper from './UserFormParametersUserWrapper.js'
import UserFormParametersGroup from '@src/js/components/users/form/UserFormParametersGroup.jsx'
import UserFormParametersGroupWrapper from './UserFormParametersGroupWrapper.js'
import RoleParameters from '@src/js/components/users/form/common/RoleParameters.jsx'
import RoleParametersWrapper from '@srcTest/js/components/users/form/common/wrapper/RoleParametersWrapper.js'

export default class UserFormParametersWrapper extends BaseWrapper {
  getUser() {
    return new UserFormParametersUserWrapper(
      this.findComponent(UserFormParametersUser)
    )
  }

  getGroup() {
    return new UserFormParametersGroupWrapper(
      this.findComponent(UserFormParametersGroup)
    )
  }

  getRole() {
    return new RoleParametersWrapper(this.findComponent(RoleParameters))
  }

  toJSON() {
    return {
      user: this.getUser().toJSON(),
      group: this.getGroup().toJSON(),
      role: this.getRole().toJSON()
    }
  }
}
