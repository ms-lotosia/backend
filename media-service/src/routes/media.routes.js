const express = require('express');
const router = express.Router();
const mediaController = require('../controllers/media.controller');
const upload = require('../middleware/upload.middleware');

router.post('/upload', upload.single('image'), mediaController.uploadImage);
router.get('/files', mediaController.getFileList);
router.get('/download/:fileId', mediaController.downloadFile);
router.delete('/files', mediaController.deleteFiles);
router.put('/move', mediaController.moveFile);

module.exports = router;

