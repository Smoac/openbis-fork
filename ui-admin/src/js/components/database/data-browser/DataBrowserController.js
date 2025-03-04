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
import openbis from '@src/js/services/openbis.js'

const CHUNK_SIZE = 1024 * 1024 * 10// 10MiB

export default class DataBrowserController extends ComponentController {

  constructor(owner) {
    super()
    autoBind(this)

    this.owner = owner
    this.gridController = null
    this.path = ''
    this.fileNames = []
  }

  async free() {
    try {
      return await openbis.free(this.owner, this.path)
    } catch (error) {
      if (error.message.includes('NoSuchFileException')) {
        return []
      } else {
        throw error
      }
    }
  }

  async listFiles(path) {
    // Use this.path if path is not specified
    const pathToList = path ? path : this.path
    try {
      return await openbis.list(this.owner, pathToList, false)
    } catch (error) {
      if (error.message.includes('NoSuchFileException')) {
        return []
      } else {
        throw error
      }
    }
  }

  async load() {
    return await this.handleError(async() => {
      const files = await this.listFiles()
      this.fileNames = files.map(file => file.name)
      return files.map(file => ({ id: file.name, ...file }))
    })
  }

  async loadFolders() {
    return await this.handleError(async() => {
      const files = await this.listFiles()
      this.fileNames = files.map(file => file.name)
      return files.filter(file => file.directory).map(file => ({ id: file.name, ...file }))
    })
  }

  async createNewFolder(name) {
    await this.handleError(async () => {
      await openbis.create(this.owner, this.path + name, true)
    })

    if (this.gridController) {
      await this.gridController.load()
    }
  }

  async rename(oldName, newName) {
    await this.handleError(async () => {
      await openbis.move(this.owner, this.path + oldName, this.owner, this.path + newName)
    })
    if (this.gridController) {
      await this.gridController.load()
    }
  }

  async delete(files) {
    await this.handleError(async () => {
      for (const file of files) {
        await this._delete(file)
      }
    })

    if (this.gridController) {
      await this.gridController.load()
    }
  }

  async _delete(file) {
    await openbis.delete(this.owner, file.path)
  }

  async copy(files, newLocation) {
    await this.handleError(async () => {
      for (const file of files) {
        await this._copy(file, newLocation)
      }
    })

    if (this.gridController) {
      await this.gridController.clearSelection()
    }
  }

  async _copy(file, newLocation){
    if (!this.isSubdirectory(file.path, newLocation)) {
      const cleanNewLocation = this._removeLeadingSlash(newLocation) + file.name
      await openbis.copy(this.owner, file.path, this.owner, cleanNewLocation)
    }
  }

  async move(files, newLocation) {
    await this.handleError(async () => {
      for (const file of files) {
        await this._move(file, newLocation)
      }
    })

    if (this.gridController) {
      await this.gridController.load()
    }
  }

  async _move(file, newLocation){
    if (!this.isSubdirectory(file.path, newLocation)) {
      const cleanNewLocation = this._removeLeadingSlash(newLocation) + file.name
      await openbis.move(this.owner, file.path, this.owner, cleanNewLocation)
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

  async upload(fileList, onNameConflictFound, onProgressUpdate) {
    await this.handleError(async() => {
      let totalUploaded = 0
      const totalSize = Array.from(fileList)
        .reduce((acc, file) => acc + file.size, 0)
      console.time("Total upload time for " + totalSize/(1024*1024) + " mb :")
      for (const file of fileList) {
        const filePath = file.webkitRelativePath ? file.webkitRelativePath
          : file.name
        const targetFilePath = this.path + '/' + filePath
        const existingFiles = await this.listFiles(targetFilePath)

        const existingFileSize = existingFiles.length === 0 ? 0
          : existingFiles[0].size
        // If the file is smaller than 2 chunks we better replace it
        const allowResume = file.size >= 2 * CHUNK_SIZE
          && file.size >= existingFileSize
        const resolutionResult = existingFiles.length === 0 ? 'replace'
          : await onNameConflictFound(file, allowResume)

        if (resolutionResult !== 'cancel') {
          // Replace or resume upload from the last point in the file
          let offset = resolutionResult === 'replace' ? 0 : existingFileSize
          totalUploaded += Math.min(offset, file.size)
          while (offset < file.size) {
            const blob = file.slice(offset, offset + CHUNK_SIZE)
            const arrayBuffer = await blob.arrayBuffer();
            const data = new Uint8Array(arrayBuffer);

            console.time("Upload time");
            await this._uploadChunk(targetFilePath, offset, data)
            console.timeEnd("Upload time");

            offset += blob.size
            totalUploaded += blob.size

            // Calculate and update progress
            const progress = Math.round((totalUploaded / totalSize) * 100)
            onProgressUpdate(Math.min(progress, 100))
          }
        } else {
          // We stop uploading after cancel
          onProgressUpdate(100)
          break
        }
      }
      console.timeEnd("Total upload time for " + totalSize/(1024*1024) + " mb :")
    })

    if (this.gridController) {
      await this.gridController.load()
    }
  }

  async handleError(fn) {
    try {
      return await fn()
    } catch (e) {
      const message = e.message || (e.t0 ? e.t0.message || e.t0 : e)
      this.setState({ errorMessage: message })
    }
  }

  async _fileSliceToBinaryString(blob) {
    return new Promise((resolve, reject) => {
      const reader = new FileReader()
      reader.onload = () => resolve(reader.result)
      reader.onerror = (error) => reject(error)
      reader.readAsBinaryString(blob)
    })
  }

  async _uploadChunk(source, offset, data) {
    return await openbis.write(this.owner, source, offset, data)
  }

  async download(file) {
    let offset = 0
    const dataArray = []

    while (offset < file.size) {
      const blob = await this._download(file, offset)
      dataArray.push(await blob.arrayBuffer())
      offset += CHUNK_SIZE
    }

    return dataArray
  }

  async _download(file, offset) {
    const limit = Math.min(CHUNK_SIZE, file.size - offset)
    return await openbis.read(this.owner, file.path, offset, limit)
  }

  _removeLeadingSlash(path) {
    return path && path[0] === '/' ? path.substring(1) : path
  }

  setPath(path) {
    this.path = path
  }

  async getRights(ids) {
    return await openbis.getRights(ids, new openbis.RightsFetchOptions())
  }
}