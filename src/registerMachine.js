import * as admin from 'firebase-admin';
import uniq from 'lodash';

function fail(res) {
    res.status(400).json({status:"bad request"});
}

export function register (req, res) {
    if(!req.body.machineId) fail(res);
    if(!req.body.requesterId) fail(res);
    // TODO verify format of the ids

    const db = admin.database();
    const ref = db.ref(`machines/${req.body.machineId}`);
    ref.transaction(function(currentData) {
        if (currentData === null) {
            return { [req.body.requesterId]: true };
        } else {
            currentData[req.body.requesterId] = true;
            return currentData;
        }
    }, function(error, committed, snapshot) {
        //TODO error handling?
    });
    res.status(200).json({status:"ok"});
}
export function unregister (req, res) {
    if(!req.body.machineId) fail(res);
    if(!req.body.requesterId) fail(res);
    // TODO verify format of the ids

    const db = admin.database();
    const ref = db.ref(`machines/${req.body.machineId}/${req.body.requesterId}`);
    ref.remove();
    res.status(200).json({status:"ok"});
}
