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

const MAX_READ_SIZE_IN_BYTES = 1024

export default class DataBrowserController extends ComponentController {

  constructor() {
    super()
    autoBind(this)

    // TODO: change it to sample permId
    this.owner = 'demo-sample'
    this.gridController = null
    this.path = ''
    this.fileNames = []
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
    this.fileNames = files.map(file => file.name)
    return files.map(file => ({ id: file.name, ...file }))
  }

  async loadFolders() {
    const files = await this.listFiles()
    this.fileNames = files.map(file => file.name)
    return files.filter(file => file.directory).map(file => ({ id: file.name, ...file }))
  }

  async createNewFolder(name) {
    return new Promise((resolve, reject) => {
      this.component.datastoreServer.create(this.owner, this.path + name, true, async (response) => {
        if (response) {
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
      this.component.datastoreServer.move(this.owner, this.path + oldName, this.owner, this.path + newName, async (response) => {
        if (response) {
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

  async delete(files) {
    for (const file of files) {
      await this._delete(file)
    }

    if (this.gridController) {
      await this.gridController.load()
    }
  }

  async _delete(file) {
    return new Promise((resolve, reject) => {
      this.component.datastoreServer.delete(this.owner, file.path, async (response) => {
        if (response) {
          resolve()
        } else {
          reject()
        }
      })
    })
  }

  async copy(files, newLocation) {
    for (const file of files) {
      await this._copy(file, newLocation);
    }

    if (this.gridController) {
      await this.gridController.clearSelection()
    }
  }

  async _copy(file, newLocation){
    const cleanNewLocation = this._removeLeadingSlash(newLocation) + file.name
    return new Promise((resolve, reject) => {
      this.component.datastoreServer.copy(this.owner, file.path, this.owner, cleanNewLocation, async (response) => {
        if (response) {
          resolve()
        } else {
          reject()
        }
      })
    })
  }

  async move(files, newLocation) {
    for (const file of files) {
      await this._move(file, newLocation)
    }

    if (this.gridController) {
      await this.gridController.load()
    }
  }

  async _move(file, newLocation){
    const cleanNewLocation = this._removeLeadingSlash(newLocation) + file.name
    return new Promise((resolve, reject) => {
      this.component.datastoreServer.move(this.owner, file.path, this.owner, cleanNewLocation, async (response) => {
        if (response) {
          resolve()
        } else {
          reject()
        }
      })
    })
  }

  async download(file) {
    let offset = 0
    const dataArray = []

    while (offset < file.size) {
      dataArray.push(await this._download(file, offset))
      offset += MAX_READ_SIZE_IN_BYTES
    }

    return dataArray
  }

  async _download(file, offset) {
    return new Promise((resolve, reject) => {
      const limit = Math.min(MAX_READ_SIZE_IN_BYTES, file.size - offset)
      this.component.datastoreServer.read(this.owner, file.path, offset, limit, async (responseData) => {
        if (responseData) {
          resolve(responseData)
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