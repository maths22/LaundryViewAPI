import * as admin from 'firebase-admin';
import {decode} from "firebase-encode";

let laundryViewEndpoint = null;

export default function (lvEndpoint) {
    laundryViewEndpoint = lvEndpoint;
    run();
}

function run() {
    const db = admin.database();
    const ref = db.ref(`machines`);
    ref.once('value').then((res) => {
        const machines = res.val();
        if(!machines) return;

        for(let [key, registrationTokens] of Object.entries(machines)) {
            const splitKey = key.split('|');
            const name = splitKey[0];
            const roomId = splitKey[1];
            const machineId = splitKey[2];

            laundryViewEndpoint.machineStatus({roomId}, (err, res) => {
                if(!res) {
                    console.log(err);
                    return;
                }
                let type = 'WASHER';
                let machine = res['washers'].find((m) => m.id === machineId);
                if(!machine) {
                    type = 'DRYER';
                    machine = res['dryers'].find((m) => m.id === machineId);
                }
                if(!machine) return;
                if(machine.status === 'AVAILBLE' || machine.status === 'DONE') {
                    const notificationPayload = {
                        notification: {
                            title: type === 'WASHER' ? 'Washing Machine Done' : 'Dryer Done',
                            body: `${name}: Machine #${machine.number}`,
                            icon: 'washing_machine',
                            sound: 'default',
                            color: '#000075'
                        }
                    };
                    const dataPayload = {
                        data: {
                            completed: decode(key)
                        }
                    };

                    admin.messaging().sendToDevice(Object.keys(registrationTokens), notificationPayload)
                        .then(function(response) {
                            // See the MessagingDevicesResponse reference documentation for
                            // the contents of response.
                            //TODO should I do anything here?
                        })
                        .catch(function(error) {
                            console.log("Error sending message:", error);
                        });
                    admin.messaging().sendToDevice(Object.keys(registrationTokens), dataPayload)
                        .then(function(response) {
                            // See the MessagingDevicesResponse reference documentation for
                            // the contents of response.
                            //TODO should I do anything here?
                        })
                        .catch(function(error) {
                            console.log("Error sending message:", error);
                        });
                    db.ref(`machines/${key}`).remove();
                }
                //TODO out of service, etc?
            });
        }
    });

    setTimeout(run, 60000);
}
