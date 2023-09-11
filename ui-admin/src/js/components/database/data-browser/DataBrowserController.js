/*
 * Copyright ETH 2023 Zürich, Scientific IT Services
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

import ComponentController from '@src/js/components/common/ComponentController.js'
import autoBind from 'auto-bind'

export default class DataBrowserController extends ComponentController {

  constructor() {
    super()
    autoBind(this)

    // TODO: change it to sample permId
    this.owner = 'demo-sample'
    this.gridController = null
    this.path = ''
  }

  setSessionToken(sessionToken) {
    this.component.datastoreServer.useSession(sessionToken)
  }

  async listFiles() {
    return new Promise((resolve, reject) => {
      this.component.datastoreServer.list(this.owner, this.path, false, (data) => {
        if (!data.error) {
          const results = data.result[1]
          const files = results.map(result => result[1])
          resolve(files)
        } else {
          reject(data.error)
        }
      })
    })
  }

  async load() {
    const files = await this.listFiles()
    await this.setState({ files })
    return files.map(file => ({ id: file.name, ...file }))
  }

  async loadFolders() {
    const files = await this.listFiles()
    return files.filter(file => file.directory).map(file => ({ id: file.name, ...file }))
  }

  async createNewFolder(name) {
    return new Promise((resolve, reject) => {
      this.component.datastoreServer.create(this.owner, this.path + name, true, async (success) => {
        if (success) {
          if (this.gridController) {
            await this.gridController.load()
          }
          resolve()
        } else {
          reject()
        }
      })
    })
  }

  async rename(oldName, newName) {
    return new Promise((resolve, reject) => {
      this.component.datastoreServer.move(this.owner, this.path + oldName, this.owner, this.path + newName, async (success) => {
        if (success) {
          if (this.gridController) {
            await this.gridController.load()
          }
          resolve()
        } else {
          reject()
        }
      })
    })
  }

  async delete(paths) {
    for (const path of paths) {
      await this._delete(path)
    }

    if (this.gridController) {
      await this.gridController.load()
    }
  }

  async _delete(path) {
    return new Promise((resolve, reject) => {
      this.component.datastoreServer.delete(this.owner, path, async (success) => {
        if (success) {
          resolve()
        } else {
          reject()
        }
      })
    })
  }

  async copy(paths, newLocation) {
    for (const path of paths) {
      await this._copy(path, newLocation)
    }
  }

  async _copy(path, newLocation){
    const cleanNewLocation = this._removeLeadingSlash(newLocation)
    return new Promise((resolve, reject) => {
      this.component.datastoreServer.copy(this.owner, this.path + path, this.owner, cleanNewLocation, async (success) => {
        if (success) {
          resolve()
        } else {
          reject()
        }
      })
    })
  }

  async move(paths, newLocation) {
    for (const path of paths) {
      await this._move(path, newLocation)
    }
  }

  async _move(path, newLocation){
    const cleanNewLocation = this._removeLeadingSlash(newLocation)
    return new Promise((resolve, reject) => {
      this.component.datastoreServer.move(this.owner, this.path + path, this.owner, cleanNewLocation, async (success) => {
        if (success) {
          resolve()
        } else {
          reject()
        }
      })
    })
  }
  
  _removeLeadingSlash(path) {
    return path && path[0] === '/' ? path.substring(1) : path
  }

  handleUploadClick(event) {
    console.log(event.target)
  }

  setPath(path) {
    this.path = path
  }

}