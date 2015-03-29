package com.github.pocmo.sensordashboard;

import android.hardware.Sensor;
import android.net.Uri;
import android.util.Log;
import java.sql.Timestamp;
import java.util.Date;

import com.github.pocmo.sensordashboard.shared.DataMapKeys;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.Arrays;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.eclipse.paho.client.mqttv3.*;

public class SensorReceiverService extends WearableListenerService {
    private static final String TAG = "SensorDashboard/SensorReceiverService";

    private RemoteSensorManager sensorManager;
/*
org=dsd6xv
type=MOTO
id=aabbcc009911
auth-method=token
auth-token=asdasdasdsadsadas
 */

    private static MqttClient client = null;
    public  String clientId = "d:dsd6xv:MOTO:aabbcc009911";
    public String org = "dsd6xv";
    public String SETTINGS_MQTT_SERVER = ".messaging.internetofthings.ibmcloud.com";
    public String SETTINGS_MQTT_PORT = "1883";
    public String SETTINGS_USERNAME = "use-token-auth";

    float myStepCounter = 0;
    float myHRCounter = 0;
    float myGZ = 0;
    float myGY = 0;
    float myGX = 0;
    public String SETTINGS_TOKEN = "uPyR6Wu?oOTJF-YfbH"; ///auth-token=uPyR6Wu?oOTJF-YfbH
    float msgcnt = 0;

    public void sendMQTTData( int sensor, float[] sensorValues) {
        msgcnt++;

        if ((client != null) && client.isConnected()) {
            // Don't do anything
        } else {
            try {
                // File persist not working on Glass
                MemoryPersistence persistence = new MemoryPersistence();

                String serverHost =  SETTINGS_MQTT_SERVER;
                String serverPort =  SETTINGS_MQTT_PORT;
                Log.d("MVK", ".initMqttConnection() - Host name: "+ org + serverHost + ", Port: " + serverPort
                        + ", client id: " + clientId +"<<<");

                String connectionUri = "tcp://" + org + serverHost + ":" + serverPort;


                client = new MqttClient(connectionUri, clientId, persistence);
            } catch (MqttException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName(SETTINGS_USERNAME);
            options.setPassword(SETTINGS_TOKEN.toCharArray());

            // Connect to the broker
            try {
                Log.d("MVK", "TRY  to connect to IOT");
                //client.connect(options, context, listener);
                client.connect(options);
                client.setCallback(new MqttCallback(){

                    private Object TTS_DATA_CHECK=1;

                    @Override
                    public void connectionLost(Throwable arg0) {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken arg0) {
                        // TODO Auto-generated method stub
                        try {
                            if(arg0.getMessage()==null){
                                //messageDelivered = true;
                            }
                        } catch (MqttException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void messageArrived(String topic, MqttMessage message)
                            throws Exception {
                        // TODO Auto-generated method stub

                        String time = new Timestamp(System.currentTimeMillis()).toString();

                        Log.d(TAG,"MQTT messageArrived");
                        Log.d(TAG,"MQTT Time:\t" +time +
                                " Topic:\t" + topic +
                                " Message:\t" + new String(message.getPayload()) +
                                " QoS:\t" + message.getQos());
                    }
                });
                client.subscribe("iot-2/cmd/*/fmt/json",0);

                Log.d("MVK", "MQTT connected ");
            } catch (MqttSecurityException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (MqttException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }//esle connected


        if ((client != null) && client.isConnected()) {
            MqttTopic topic = client.getTopic("iot-2/evt/status/fmt/json");
            String messageData;
/*

    values[0]: Acceleration minus Gx on the x-axis
    values[1]: Acceleration minus Gy on the y-axis
    values[2]: Acceleration minus Gz on the z-axis

 */

            //Sensor.TYPE_STEP_COUNTER;
            messageData = "";

/*
            if (sensor == Sensor.TYPE_STEP_DETECTOR)
            {
                Log.d("MVK", "STEP_DETECTOR("+sensor+") = "+sensorValues[0]);

                messageData = "{ \"d\": {" +
                        "\"myName\":\"MOTO\" ," +
                        "\"Sensor\":" + sensor + " ," +
                        "\"Gx\":" + myGX + " ," +
                        "\"Gy\":" + myGY + " ," +
                        "\"Gz\":" + myGZ + " ," +
                        "\"HEART_RATE\":" + myHRCounter + " ," +
                        "\"STEP_COUNTER\":" + myStepCounter + " " +
                        "} }";

            }
*/
            if (sensor == Sensor.TYPE_STEP_COUNTER)
            {
                Log.d("MVK", "STEP_COUNTER("+sensor+") = "+sensorValues[0] + " old value : "+myStepCounter);
                if (sensorValues[0] > 0) {

                    myStepCounter = sensorValues[0];
                    if  (myStepCounter >9000)
                    {
                        myStepCounter = myStepCounter -9000;
                    }

                    if  (myStepCounter >5000)
                    {
                        myStepCounter = myStepCounter -5000;
                    }

                    if  (myStepCounter >2000)
                    {
                        myStepCounter = myStepCounter -2000;
                    }

                    if  (myStepCounter >500)
                    {
                        myStepCounter = myStepCounter -500;
                    }

                    messageData = "{ \"d\": {" +
                            "\"myName\":\"MOTO\" ," +
                            "\"Sensor\":" + sensor + " ," +
                            "\"Gx\":" + myGX + " ," +
                            "\"Gy\":" + myGY + " ," +
                            "\"Gz\":" + myGZ + " ," +
                            "\"HEART_RATE\":" + myHRCounter + " ," +
                            "\"STEP_COUNTER\":" + myStepCounter + " " +
                            "} }";

                }

            }

            if (sensor == Sensor.TYPE_HEART_RATE)
            {
                Log.d("MVK", "HEART_RATE("+sensor+") = "+sensorValues[0] + " old value : "+myHRCounter);

                if (sensorValues[0] > 0) {
                    myHRCounter = sensorValues[0];
                    messageData = "{ \"d\": {" +
                            "\"myName\":\"MOTO\" ," +
                            "\"Sensor\":" + sensor + " ," +
                            "\"Gx\":" + myGX + " ," +
                            "\"Gy\":" + myGY + " ," +
                            "\"Gz\":" + myGZ + " ," +
                            "\"HEART_RATE\":" + myHRCounter + " ," +
                            "\"STEP_COUNTER\":" + myStepCounter + " " +
                            "} }";
                }
            }

            if (sensor == Sensor.TYPE_ACCELEROMETER || sensor == Sensor.TYPE_LINEAR_ACCELERATION)
            {

                Log.d("MVK", "ACCELEROMETER("+sensor+") (z)= "+sensorValues[2]+ " old value : "+myGZ);

                //if (sensorValues[1] > 0 && sensorValues[1] != myGZ) {
                    myGX = sensorValues[0];
                    myGY = sensorValues[1];
                    myGZ = sensorValues[2];

                    messageData = "{ \"d\": {" +
                            "\"myName\":\"MOTO\" ," +
                            "\"Sensor\":" + sensor + " ," +
                            "\"Gx\":" + myGX + " ," +
                            "\"Gy\":" + myGY + " ," +
                            "\"Gz\":" + myGZ + " ," +
                            "\"HEART_RATE\":" + myHRCounter + " ," +
                            "\"STEP_COUNTER\":" + myStepCounter + " " +
                            "} }";
//              }else{
                //messageData = "";
     //           }
            }else{
//                messageData = "";
            }
        /*else if (sensor == Sensor.TYPE_HEART_RATE)
            {
                 messageData = "{ \"d\": {" +
                        "\"myName\":\"MOTO\" ," +
                        "\"Sensor\":\"HeartRate\" ," +
                        "\"Value\":" + sensorValues[0] + " " +
                        "} }";
            }else {
                 messageData = "{ \"d\": {" +
                        "\"myName\":\"MOTO\" ," +
                        "\"Sensor\":" + sensor + " ," +
                        "\"Value\":" + sensorValues[0] + " " +
                        "} }";
            }
*/
            // Log.d("MVK", "Send Message:"+messageData);

            MqttMessage message = new MqttMessage(messageData.getBytes());
            message.setQos(0);


            try {

                // Give the message to the client for publishing. For QoS 2, this
                // will involve multiple network calls, which will happen
                // asynchronously after this method has returned.
		/*
			  	var myJsonData = {
		                   "d": {
		                     "myName": "Nest Data",
		                     "deviceName" : device.name,
		                     "deviceId"    : deviceId,
		                     "currentTemp" : device.current_temperature,
		                     "targetTemp"  :  device.target_temperature

		                    }
		                  };
*/


                //topic iot-2/evt/status/fmt/json
                if(messageData.equalsIgnoreCase("") )
                {
                    Log.d("MVK", "No MQTT message(\" + msgcnt + \"):  for Sensor: " + sensor);
                }else{
                    if (msgcnt % 3 == 0) // send every 5 mesg due to to many on iot
                    {
                        topic.publish(message);
                        Log.d("MVK", "Send MQTT message(" + msgcnt + "): " + message);
                    }else{
                        Log.d("MVK", "Skip MQTT message(" + msgcnt + "): " + message);
                    }
                }
            } catch (MqttException ex) {

                // Client has not accepted the message due to a failure
                // Depending on the exception's reason code, we could always retry
                System.err.println("Failed to send message");
                Log.d("MVK", "Failed to send message: "+ex.toString());
            }
        }

    }

    @Override
    public void onCreate() {
        super.onCreate();

        sensorManager = RemoteSensorManager.getInstance(this);
    }

    @Override
    public void onPeerConnected(Node peer) {
        super.onPeerConnected(peer);

        Log.i(TAG, "Connected: " + peer.getDisplayName() + " (" + peer.getId() + ")");
    }

    @Override
    public void onPeerDisconnected(Node peer) {
        super.onPeerDisconnected(peer);

        Log.i(TAG, "Disconnected: " + peer.getDisplayName() + " (" + peer.getId() + ")");
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d(TAG, "onDataChanged()");

        for (DataEvent dataEvent : dataEvents) {
            if (dataEvent.getType() == DataEvent.TYPE_CHANGED) {
                DataItem dataItem = dataEvent.getDataItem();
                Uri uri = dataItem.getUri();
                String path = uri.getPath();

                if (path.startsWith("/sensors/")) {
                    unpackSensorData(
                            Integer.parseInt(uri.getLastPathSegment()),
                            DataMapItem.fromDataItem(dataItem).getDataMap()
                    );
                }
            }
        }
    }

    private void unpackSensorData(int sensorType, DataMap dataMap) {
        int accuracy = dataMap.getInt(DataMapKeys.ACCURACY);
        long timestamp = dataMap.getLong(DataMapKeys.TIMESTAMP);
        float[] values = dataMap.getFloatArray(DataMapKeys.VALUES);

//        Log.d(TAG, "Received sensor data " + sensorType + " = " + Arrays.toString(values));
        Log.d(TAG, "MVK: Received data from " + sensorType + " = " + Arrays.toString(values));
        sendMQTTData(sensorType,values);
        sensorManager.addSensorData(sensorType, accuracy, timestamp, values);
    }
}
