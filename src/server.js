import express from 'express';
import bodyParser from 'body-parser';

import init from './init';
import * as registerMachine from './registerMachine';

const app = express();
const port = process.env.PORT || 3000;

init();

app.use(bodyParser.json());

app.post('/registerMachine', registerMachine.register);
app.post('/unregisterMachine', registerMachine.unregister);

app.listen(port, function () {
    console.log(`Example app listening on port: ${port}`)
});
