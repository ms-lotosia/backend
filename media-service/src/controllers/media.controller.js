const teraboxService = require('../services/terabox.service');
const fs = require('fs').promises;
const path = require('path');

class MediaController {
  async uploadImage(req, res) {
    try {
      if (!req.file) {
        return res.status(400).json({
          success: false,
          message: 'No file uploaded'
        });
      }

      const filePath = req.file.path;
      const directory = req.body.directory || '/uploads';

      const result = await teraboxService.uploadFile(filePath, directory, false);

      // Clean up temp file
      await fs.unlink(filePath);

      res.status(200).json({
        success: true,
        message: 'Image uploaded successfully',
        data: result.fileDetails
      });
    } catch (error) {
      // Clean up temp file on error
      if (req.file && req.file.path) {
        try {
          await fs.unlink(req.file.path);
        } catch (unlinkError) {
          console.error('Error deleting temp file:', unlinkError);
        }
      }

      res.status(500).json({
        success: false,
        message: error.message
      });
    }
  }

  async getFileList(req, res) {
    try {
      const directory = req.query.directory || '/';
      const fileList = await teraboxService.fetchFileList(directory);

      res.status(200).json({
        success: true,
        data: fileList
      });
    } catch (error) {
      res.status(500).json({
        success: false,
        message: error.message
      });
    }
  }

  async downloadFile(req, res) {
    try {
      const { fileId } = req.params;
      
      if (!fileId) {
        return res.status(400).json({
          success: false,
          message: 'File ID is required'
        });
      }

      const result = await teraboxService.downloadFile(fileId);

      res.status(200).json({
        success: true,
        data: result
      });
    } catch (error) {
      res.status(500).json({
        success: false,
        message: error.message
      });
    }
  }

  async deleteFiles(req, res) {
    try {
      const { filePaths } = req.body;

      if (!filePaths || !Array.isArray(filePaths) || filePaths.length === 0) {
        return res.status(400).json({
          success: false,
          message: 'File paths array is required'
        });
      }

      const result = await teraboxService.deleteFiles(filePaths);

      res.status(200).json({
        success: true,
        message: 'Files deleted successfully',
        data: result
      });
    } catch (error) {
      res.status(500).json({
        success: false,
        message: error.message
      });
    }
  }

  async moveFile(req, res) {
    try {
      const { oldPath, newPath, newName } = req.body;

      if (!oldPath || !newPath || !newName) {
        return res.status(400).json({
          success: false,
          message: 'oldPath, newPath, and newName are required'
        });
      }

      const result = await teraboxService.moveFile(oldPath, newPath, newName);

      res.status(200).json({
        success: true,
        message: 'File moved successfully',
        data: result
      });
    } catch (error) {
      res.status(500).json({
        success: false,
        message: error.message
      });
    }
  }
}

module.exports = new MediaController();

