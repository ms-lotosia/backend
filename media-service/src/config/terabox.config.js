require('dotenv').config();

module.exports = {
    credentials: {
        ndus: process.env.TERABOX_NDUS,
        appId: process.env.TERABOX_APP_ID,
        uploadId: process.env.TERABOX_UPLOAD_ID,
        jsToken: process.env.TERABOX_JS_TOKEN,
        browserId: process.env.TERABOX_BROWSER_ID
    },
    uploadDir: process.env.UPLOAD_DIR || '/uploads'
};