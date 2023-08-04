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

    this.owner = 'demo-sample'
    this.source = ''
    this.gridController = null
  }

  async login() {
    return new Promise((resolve, reject) => {
      this.component.datastoreServer.login('admin', 'changeit', token => {
        if (token) {
          resolve(token)
        } else {
          reject('Could not perform login.')
        }
      })
    })
  }

  async listFiles() {
    return new Promise((resolve, reject) => {
      this.component.datastoreServer.list(this.owner, this.source, true, (data) => {
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
    await this.login()
    const files = await this.listFiles()
    await this.setState({ files })
    return await files.map(file => ({ id: file.name, ...file }))
  }

  async createNewFolder(name) {
    return new Promise((resolve, reject) => {
      this.component.datastoreServer.create(this.owner, this.source + name, true, async (success) => {
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

  handleUploadClick(event) {
    console.log(event.target)
  }

}