const express = require('express');
const cors = require('cors');

const mediaRoutes = require('./routes/media.routes');

const app = express();
const PORT = 8085;

app.use(cors());
apwp.use(express.json());
app.use(express.urlencoded({ extended: true }));

app.use('/api/v1/media', mediaRoutes);

app.get('/health', (req, res) => {
  res.status(200).json({ status: 'UP' });
});

app.listen(PORT, () => {
  console.log(`Media Service running on port ${PORT}`);
});

