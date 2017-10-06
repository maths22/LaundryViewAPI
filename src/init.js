import * as admin from 'firebase-admin';
import gapi from 'googleapis';
import periodicCheck from './periodicCheck';

import serviceAccount from '../private/serviceAccountKey.json';

export default function () {
    admin.initializeApp({
        credential: admin.credential.cert(serviceAccount),
        databaseURL: 'https://numeric-drummer-119720.firebaseio.com/'
    });
    gapi.discover('https://laundryview-1197.appspot.com/_ah/api/discovery/v1/apis', function (err, result) {
        const laundryViewEndpoint = gapi.laundryView('v1').laundryViewEndpoint;
        periodicCheck(laundryViewEndpoint);
    });
}
