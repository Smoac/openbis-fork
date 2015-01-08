/**
 *  Attachment file name.
 *  
 *  @author pkupczyk
 */
define(["dto/id/attachment/IAttachmentId"], function (IAttachmentId) {
    var AttachmentFileName = /**
     *  @param fileName Attachment file name, e.g. "my_file.txt".
     */
    function(fileName) {
        this.setFileName(fileName);
    };
    stjs.extend(AttachmentFileName, null, [IAttachmentId], function(constructor, prototype) {
        prototype['@type'] = 'AttachmentFileName';
        constructor.serialVersionUID = 1;
        prototype.fileName = null;
        prototype.getFileName = function() {
            return this.fileName;
        };
        prototype.setFileName = function(fileName) {
            if (fileName == null) {
                 throw new IllegalArgumentException("File name cannot be null");
            }
            this.fileName = fileName;
        };
        prototype.toString = function() {
            return this.getFileName();
        };
        prototype.hashCode = function() {
            return ((this.getFileName() == null) ? 0 : this.getFileName().hashCode());
        };
        prototype.equals = function(obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (this.getClass() != obj.getClass()) {
                return false;
            }
            var other = obj;
            return this.getFileName() == null ? this.getFileName() == other.getFileName() : this.getFileName().equals(other.getFileName());
        };
    }, {});
    return AttachmentFileName;
})