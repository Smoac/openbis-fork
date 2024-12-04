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

  async download(file, onProgressUpdate) {
    let offset = 0
    const dataArray = []

    while (offset < file.size) {
      const blob = await this._download(file, offset)
      dataArray.push(await blob.arrayBuffer())
      offset += CHUNK_SIZE
      onProgressUpdate(CHUNK_SIZE)
    }

    return dataArray
  }

async _appendToFile(dirHandle, fileName, newData) {
try {
    // Get a handle to the existing file
    const fileHandle = await dirHandle.getFileHandle(fileName, { create: true });

    // Get the size of the existing file
    const existingFile = await fileHandle.getFile();
    const existingSize = existingFile.size;

    // Open the writable stream
    const writable = await fileHandle.createWritable();

    // Move the write pointer to the end of the file
    await writable.seek(existingSize);

    // Write the new data
    await writable.write(newData);

    // Finalize changes
    await writable.close();

    console.log(`Successfully appended data to "${fileName}".`);
  } catch (err) {
    console.error(`Failed to append data to "${fileName}":`, err);
    throw err;
  }
}

async renameFile(dirHandle, oldName, newName) {
  try {
    // Get the original file handle
    const oldFileHandle = await dirHandle.getFileHandle(oldName);
    const oldFile = await oldFileHandle.getFile();

    // Create a new file with the desired name
    const newFileHandle = await dirHandle.getFileHandle(newName, { create: true });
    const writable = await newFileHandle.createWritable();

    // Copy the contents of the old file to the new file
    const reader = oldFile.stream().getReader();
    let chunk;
    while (!(chunk = await reader.read()).done) {
      await writable.write(chunk.value);
    }

    // Finalize the new file
    await writable.close();

    // Remove the original file
    await dirHandle.removeEntry(oldName);

    console.log(`Successfully renamed "${oldName}" to "${newName}".`);
  } catch (err) {
    console.error(`Failed to rename "${oldName}" to "${newName}":`, err);
    throw err;
  }
}

  async downloadAndAssemble_resume(file, dirHandle, onProgressUpdate) {
    let offset = 0;

    // Create a writable stream for the file in the selected directory
    //const fileStream = await this._createWritableStream(dirHandle, file.name);

    let writeToDiskOffset = 0;
    var dataArrayForDisk = []
    while (offset < file.size) {

      const downloadStartTime = Date.now();
      const blob = await this._download(file, offset);
      const downloadEndTime = Date.now();
      dataArrayForDisk.push(await blob.arrayBuffer());
      offset += CHUNK_SIZE;

      writeToDiskOffset += CHUNK_SIZE;

      const elapsedTime = (downloadEndTime - downloadStartTime) / 1000; // Seconds
      const speed = blob.size / elapsedTime; 

      // write to file only when almost 100MB
      if(writeToDiskOffset > 100_000_000 || offset >= file.size){
        // Write the chunk directly to the file
        const combinedBuffer = new Uint8Array(dataArrayForDisk.reduce((acc, buf) => acc + buf.byteLength, 0));
        let offset = 0;
        for (const buf of dataArrayForDisk) {
            combinedBuffer.set(new Uint8Array(buf), offset);
            offset += buf.byteLength;
        }
        await this._appendToFile(dirHandle, file.name, combinedBuffer);

        writeToDiskOffset = 0;
        dataArrayForDisk = []
      }

      onProgressUpdate(CHUNK_SIZE,file.name, speed)
    }

    //await fileStream.close();
    console.log(`Download of ${file.name} complete!`);
  }

  async _createWritableStream(dirHandle, fileName) {
    // Create or access the file in the selected directory
    const fileHandle = await dirHandle.getFileHandle(fileName, { create: true });
    return await fileHandle.createWritable();
  }

  async downloadAndAssemble(file, dirHandle, onProgressUpdate) {
    let offset = 0;

    // Create a writable stream for the file in the selected directory
    const fileStream = await this._createWritableStream(dirHandle, file.name);

    let writeToDiskOffset = 0;
    var dataArrayForDisk = []
    while (offset < file.size) {
      const downloadStartTime = Date.now();
      const blob = await this._download(file, offset);
      const downloadEndTime = Date.now();

      dataArrayForDisk.push(await blob.arrayBuffer());
      offset += CHUNK_SIZE;

      writeToDiskOffset += CHUNK_SIZE;

      // Calculate download speed      
      const elapsedTime = (downloadEndTime - downloadStartTime) / 1000; // Seconds
      const speed = blob.size / elapsedTime; 

      // write to file only when almost 100MB
      if(writeToDiskOffset > 100_000_000 || offset >= file.size){
        onProgressUpdate(CHUNK_SIZE,file.name, "write to disk")

        // Write the chunk directly to the file
        var combinedBuffer = new Uint8Array(dataArrayForDisk.reduce((acc, buf) => acc + buf.byteLength, 0));
        let offset = 0;
        for (const buf of dataArrayForDisk) {
            combinedBuffer.set(new Uint8Array(buf), offset);
            offset += buf.byteLength;
        }
        await fileStream.write(combinedBuffer);        

        combinedBuffer = null;
        writeToDiskOffset = 0;
        dataArrayForDisk = []
      }

      onProgressUpdate(CHUNK_SIZE,file.name, speed)
    }

    onProgressUpdate(CHUNK_SIZE,file.name, "Finalizing...")
    await fileStream.close();    
    console.log(`Download of ${file.name} complete!`);
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