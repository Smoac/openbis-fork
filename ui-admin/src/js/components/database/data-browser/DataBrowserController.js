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

const CHUNK_SIZE = 1024 * 1024 // 1MiB

export default class DataBrowserController extends ComponentController {

  constructor(owner) {
    super()
    autoBind(this)

    this.owner = owner
    this.gridController = null
    this.path = ''
    this.fileNames = []
  }

  setSessionToken(sessionToken) {
    this.component.datastoreServer.useSession(sessionToken)
  }

  free() {
    return new Promise((resolve, reject) => {
      this.component.datastoreServer.free(this.owner, this.path)
        .then((data) => {
          if (!data.error) {
            resolve(data.result[1]);
          } else {
            reject(data.error)
          }
        })
        .catch((error) => {
          if (error.message.includes('NoSuchFileException')) {
            resolve([])
          } else {
            reject(error)
          }
        })
    })
  }

  listFiles() {
    return new Promise((resolve, reject) => {
      this.component.datastoreServer.list(this.owner, this.path, false)
        .then((data) => {
          if (!data.error) {
            const results = data.result[1]
            resolve(results.map(result => result[1]))
          } else {
            reject(data.error)
          }
        })
        .catch((error) => {
          if (error.message.includes('NoSuchFileException')) {
            resolve([])
          } else {
            reject(error)
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
    await this.component.datastoreServer.create(this.owner, this.path + name, true)
    if (this.gridController) {
      await this.gridController.load()
    }
  }

  async rename(oldName, newName) {
    await this.component.datastoreServer.move(this.owner, this.path + oldName, this.owner, this.path + newName)
    if (this.gridController) {
      await this.gridController.load()
    }
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
    await this.component.datastoreServer.delete(this.owner, file.path)
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
    if (!this.isSubdirectory(file.path, newLocation)) {
      const cleanNewLocation = this._removeLeadingSlash(newLocation) + file.name
      await this.component.datastoreServer.copy(this.owner, file.path, this.owner, cleanNewLocation)
    }
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
    if (!this.isSubdirectory(file.path, newLocation)) {
      const cleanNewLocation = this._removeLeadingSlash(newLocation) + file.name
      await this.component.datastoreServer.move(this.owner, file.path, this.owner, cleanNewLocation)
    }
  }

  isSubdirectory(parentPath, childPath) {
    // Normalize paths to remove trailing slashes and ensure uniformity
    const normalizedParentPath = parentPath.replace(/\/+$/, "")
    const normalizedChildPath = childPath.replace(/\/+$/, "")

    // Check if the child path starts with the parent path and has a directory separator after it
    return (
      normalizedChildPath.startsWith(normalizedParentPath) &&
      (normalizedChildPath[normalizedParentPath.length] === "/" ||
        normalizedParentPath.length === normalizedChildPath.length)
    )
  }

  async upload(file) {
    let offset = 0

    while (offset < file.size) {
      const chunkData = await file.slice(offset, offset + CHUNK_SIZE).arrayBuffer()
      // console.log(`Uploading chunk: ${offset} - Size: ${chunkData.byteLength}`)
      await this._uploadChunk(file.name, offset, chunkData)
      offset += CHUNK_SIZE
    }
  }

  async _uploadChunk(source, offset, data) {
    const hash = await crypto.subtle.digest("SHA-1", data)
    const base64Data = await this._arrayBufferToBase64(data)
    const base64Hash = await this._arrayBufferToBase64(hash)

    return await this.component.datastoreServer.write(this.owner, source, offset, base64Data, base64Hash)
  }

  async _arrayBufferToBase64(buffer) {
    return new Promise((resolve, reject) => {
      const blob = new Blob([buffer]);
      const reader = new FileReader();
      reader.onloadend = () => {
        const base64data = reader.result.split(',')[1];
        resolve(base64data);
      };
      reader.onerror = reject;
      reader.readAsDataURL(blob);
    });
  }

  async download(file) {
    let offset = 0
    const dataArray = []

    while (offset < file.size) {
      const blob = await this._download(file, offset)
      dataArray.push(await new Uint8Array(blob.arrayBuffer()))
      offset += CHUNK_SIZE
    }

    return dataArray
  }

  async _download(file, offset) {
    const limit = Math.min(CHUNK_SIZE, file.size - offset)
    return await this.component.datastoreServer.read(this.owner, file.path, offset, limit)
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