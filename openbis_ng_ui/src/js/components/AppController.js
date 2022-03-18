import _ from 'lodash'
import React from 'react'
import { createBrowserHistory } from 'history'
import openbis from '@src/js/services/openbis.js'
import objectType from '@src/js/common/consts/objectType.js'
import objectOperation from '@src/js/common/consts/objectOperation.js'
import routes from '@src/js/common/consts/routes.js'
import cookie from '@src/js/common/cookie.js'
import url from '@src/js/common/url.js'

export class AppController {
  constructor() {
    this.AppContext = React.createContext()

    const history = createBrowserHistory({
      basename: url.getApplicationPath() + '#'
    })

    history.listen(location => {
      let route = routes.parse(location.pathname)
      if (route.path !== this.getRoute()) {
        this.routeChange(route.path, location.state)
      }
    })

    this.history = history
  }

  init(context) {
    this.context = context
    context.initState(this.initialState())
  }

  initialState() {
    return {
      loaded: false,
      loading: false,
      session: null,
      route: null,
      search: null,
      pages: [],
      error: null,
      lastObjectModifications: {}
    }
  }

  async load() {
    const { loaded } = this.context.getState()

    if (!loaded) {
      try {
        await this.context.setState({ loading: true })
        await openbis.init()

        const sessionToken = cookie.read(cookie.OPENBIS_COOKIE)

        if (sessionToken) {
          try {
            openbis.useSession(sessionToken)

            const sessionInformation = await openbis.getSessionInformation()

            if (sessionInformation) {
              await this.context.setState({
                session: {
                  sessionToken: sessionToken,
                  userName: sessionInformation.userName
                }
              })
            } else {
              openbis.useSession(null)
            }
          } catch (e) {
            openbis.useSession(null)
          }
        }

        await this.context.setState({ loaded: true })
      } catch (e) {
        await this.context.setState({ error: e })
      } finally {
        await this.context.setState({ loading: false })
      }
    }
  }

  async login(username, password) {
    try {
      await this.context.setState({ loading: true })

      const sessionToken = await openbis.login(username, password)

      this.context.setState({
        session: {
          sessionToken: sessionToken,
          userName: username
        }
      })
      cookie.create(cookie.OPENBIS_COOKIE, sessionToken, 7)

      const { route } = this.context.getState()
      const routeObject = routes.parse(route)
      await this.routeChange(routeObject.path)
    } catch (e) {
      await this.context.setState({ error: 'Incorrect user or password' })
    } finally {
      await this.context.setState({ loading: false })
    }
  }

  async logout() {
    try {
      await this.context.setState({ loading: true })
      await openbis.logout()
      this.context.setState(state => ({
        ...this.initialState(),
        loaded: state.loaded
      }))
      cookie.erase(cookie.OPENBIS_COOKIE)
      await this.routeChange('/')
    } catch (e) {
      await this.context.setState({ error: e })
    } finally {
      await this.context.setState({ loading: false })
    }
  }

  async search(page, text) {
    if (text.trim().length > 0) {
      await this.objectOpen(page, objectType.SEARCH, text.trim())
    }
    await this.context.setState({ search: '' })
  }

  async searchChange(text) {
    await this.context.setState({ search: text })
  }

  async pageChange(page) {
    const pageRoute = this.getCurrentRoute(page)
    if (pageRoute) {
      await this.routeChange(pageRoute)
    } else {
      const route = routes.format({ page })
      await this.routeChange(route)
    }
  }

  async errorChange(error) {
    await this.context.setState({ error: error })
  }

  async routeChange(path) {
    const newRoute = routes.parse(path)

    if (newRoute.type && newRoute.id) {
      const object = { type: newRoute.type, id: newRoute.id }
      const openTabs = this.getOpenTabs(newRoute.page)

      if (openTabs) {
        let found = false
        let id = 1

        openTabs.forEach(openTab => {
          if (_.isMatch(openTab.object, object)) {
            found = true
          }
          if (openTab.id >= id) {
            id = openTab.id + 1
          }
        })

        if (!found) {
          await this.addOpenTab(newRoute.page, id, { id, object })
        }
      }
    }

    await this.context.setState({ route: newRoute.path })
    await this.setCurrentRoute(newRoute.page, newRoute.path)

    if (newRoute.path !== this.history.location.pathname) {
      this.history.push(newRoute.path)
    }
  }

  async routeReplace(route, state) {
    this.history.replace(route, state)
  }

  async objectNew(page, type) {
    let id = 1
    const openObjects = this.getOpenObjects(page)
    openObjects.forEach(openObject => {
      if (openObject.type === type) {
        id++
      }
    })

    const route = routes.format({ page, type, id })
    await this.routeChange(route)
  }

  async objectCreate(page, oldType, oldId, newType, newId) {
    const openTabs = this.getOpenTabs(page)
    const oldTab = _.find(openTabs, { object: { type: oldType, id: oldId } })

    if (oldTab) {
      const newTab = {
        ...oldTab,
        object: { type: newType, id: newId },
        changed: false
      }
      await this.replaceOpenTab(page, oldTab.id, newTab)
      await this.setLastObjectModification(
        newType,
        objectOperation.CREATE,
        Date.now()
      )

      const route = routes.format({ page, type: newType, id: newId })
      await this.routeReplace(route)
    }
  }

  async objectOpen(page, type, id) {
    const route = routes.format({ page, type, id })
    await this.routeChange(route)
  }

  async objectUpdate(type) {
    await this.setLastObjectModification(
      type,
      objectOperation.UPDATE,
      Date.now()
    )
  }

  async objectDelete(page, type, id) {
    await this.objectClose(page, type, id)
    await this.setLastObjectModification(
      type,
      objectOperation.DELETE,
      Date.now()
    )
  }

  async objectChange(page, type, id, changed) {
    const openTabs = this.getOpenTabs(page)
    const oldTab = _.find(openTabs, { object: { type, id } })

    if (oldTab) {
      const newTab = { ...oldTab, changed }
      await this.replaceOpenTab(page, oldTab.id, newTab)
    }
  }

  async objectClose(page, type, id) {
    const openTabs = this.getOpenTabs(page)
    const objectToClose = { type, id }

    let selectedObject = this.getSelectedObject(page)
    if (selectedObject && _.isEqual(selectedObject, objectToClose)) {
      if (_.size(openTabs) === 1) {
        selectedObject = null
      } else {
        let selectedIndex = _.findIndex(openTabs, { object: selectedObject })
        if (selectedIndex === 0) {
          selectedObject = openTabs[selectedIndex + 1].object
        } else {
          selectedObject = openTabs[selectedIndex - 1].object
        }
      }
    }

    let tabToClose = _.find(openTabs, { object: objectToClose })
    if (tabToClose) {
      await this.removeOpenTab(page, tabToClose.id)
    }

    if (selectedObject) {
      const route = routes.format({
        page,
        type: selectedObject.type,
        id: selectedObject.id
      })
      await this.routeChange(route)
    } else {
      const route = routes.format({ page })
      await this.routeChange(route)
    }
  }

  getLoaded() {
    return this.context.getState().loaded
  }

  getLoading() {
    return this.context.getState().loading
  }

  getSession() {
    return this.context.getState().session
  }

  getRoute() {
    return this.context.getState().route
  }

  getSearch() {
    return this.context.getState().search
  }

  getError() {
    return this.context.getState().error
  }

  getCurrentPage() {
    const { route } = this.context.getState()
    return routes.parse(route).page
  }

  getCurrentRoute(page) {
    const { pages } = this.context.getState()
    return pages[page] ? pages[page].currentRoute : null
  }

  async setCurrentRoute(page, route) {
    await this.context.setState(state => ({
      pages: {
        ...state.pages,
        [page]: {
          ...state.pages[page],
          currentRoute: route
        }
      }
    }))
  }

  getSelectedObject(page) {
    const path = this.getCurrentRoute(page)
    if (path) {
      const route = routes.parse(path)
      if (route && route.type && route.id) {
        return { type: route.type, id: route.id }
      }
    }
    return null
  }

  getSelectedTab(page) {
    const selectedObject = this.getSelectedObject(page)
    if (selectedObject) {
      const openTabs = this.getOpenTabs(page)
      return _.find(openTabs, { object: selectedObject })
    } else {
      return null
    }
  }

  getOpenObjects(page) {
    const openTabs = this.getOpenTabs(page)
    return openTabs.map(openTab => {
      return openTab.object
    })
  }

  getOpenTabs(page) {
    const { pages } = this.context.getState()
    return (pages[page] && pages[page].openTabs) || []
  }

  async setOpenTabs(page, newOpenTabs) {
    await this.context.setState(state => ({
      pages: {
        ...state.pages,
        [page]: {
          ...state.pages[page],
          openTabs: newOpenTabs
        }
      }
    }))
  }

  async addOpenTab(page, id, tab) {
    const openTabs = this.getOpenTabs(page)
    const index = _.findIndex(openTabs, { id: id }, _.isMatch)
    if (index === -1) {
      const newOpenTabs = Array.from(openTabs)
      newOpenTabs.push(tab)
      await this.setOpenTabs(page, newOpenTabs)
    }
  }

  async removeOpenTab(page, id) {
    const openTabs = this.getOpenTabs(page)
    const index = _.findIndex(openTabs, { id: id }, _.isMatch)
    if (index !== -1) {
      const newOpenTabs = Array.from(openTabs)
      newOpenTabs.splice(index, 1)
      await this.setOpenTabs(page, newOpenTabs)
    }
  }

  async replaceOpenTab(page, id, tab) {
    const openTabs = this.getOpenTabs(page)
    const index = _.findIndex(openTabs, { id: id }, _.isMatch)
    if (index !== -1) {
      const newOpenTabs = Array.from(openTabs)
      newOpenTabs[index] = tab
      await this.setOpenTabs(page, newOpenTabs)
    }
  }

  getLastObjectModifications() {
    return this.context.getState().lastObjectModifications
  }

  async setLastObjectModification(type, operation, timestamp) {
    const { lastObjectModifications } = this.context.getState()

    if (
      !lastObjectModifications[type] ||
      !lastObjectModifications[type][operation] ||
      lastObjectModifications[type][operation] < timestamp
    ) {
      await this.context.setState({
        lastObjectModifications: {
          ...lastObjectModifications,
          [type]: { ...lastObjectModifications[type], [operation]: timestamp }
        }
      })
    }
  }

  withState(additionalPropertiesFn) {
    const WithContext = Component => {
      const WithConsumer = props => {
        return React.createElement(this.AppContext.Consumer, {}, () => {
          const additionalProperties = additionalPropertiesFn
            ? additionalPropertiesFn(props)
            : {}
          return React.createElement(Component, {
            ...props,
            ...additionalProperties
          })
        })
      }
      WithConsumer.displayName = 'WithConsumer'
      return WithConsumer
    }
    WithContext.displayName = 'WithContext'
    return WithContext
  }
}

let INSTANCE = new AppController()

export function setInstance(instance) {
  INSTANCE = instance
}

export function getInstance() {
  return INSTANCE
}

export default INSTANCE
