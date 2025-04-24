package com.example.utlite.helper;

import static com.zebra.rfid.api3.CdcAcmSerialDriver.LOGGER;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import com.zebra.rfid.api3.ACCESS_OPERATION_CODE;
import com.zebra.rfid.api3.ACCESS_OPERATION_STATUS;
import com.zebra.rfid.api3.AccessFilter;
import com.zebra.rfid.api3.AntennaInfo;
import com.zebra.rfid.api3.Antennas;
import com.zebra.rfid.api3.DYNAMIC_POWER_OPTIMIZATION;
import com.zebra.rfid.api3.ENUM_TRANSPORT;
import com.zebra.rfid.api3.ENUM_TRIGGER_MODE;
import com.zebra.rfid.api3.HANDHELD_TRIGGER_EVENT_TYPE;
import com.zebra.rfid.api3.INVENTORY_STATE;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.MEMORY_BANK;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.RFIDReader;
import com.zebra.rfid.api3.RFIDResults;
import com.zebra.rfid.api3.ReaderDevice;
import com.zebra.rfid.api3.Readers;
import com.zebra.rfid.api3.RfidEventsListener;
import com.zebra.rfid.api3.RfidReadEvents;
import com.zebra.rfid.api3.RfidStatusEvents;
import com.zebra.rfid.api3.SESSION;
import com.zebra.rfid.api3.SL_FLAG;
import com.zebra.rfid.api3.START_TRIGGER_TYPE;
import com.zebra.rfid.api3.STATUS_EVENT_TYPE;
import com.zebra.rfid.api3.STOP_TRIGGER_TYPE;
import com.zebra.rfid.api3.TagAccess;
import com.zebra.rfid.api3.TagData;
import com.zebra.rfid.api3.TriggerInfo;

import java.util.ArrayList;
import java.util.logging.Level;

public class RFIDHandler implements Readers.RFIDReaderEventHandler {

    final static String TAG = "RFID_SAMPLE";
    // RFID Reader
    private static Readers readers;
    private static ArrayList<ReaderDevice> availableRFIDReaderList;
    private static ReaderDevice readerDevice;
    private static RFIDReader reader;
    private EventHandler eventHandler;
    // UI and context
    TextView textView;
    private ResponseHandlerInterface listener;
    private Context context;
    // general
    private int MAX_POWER = 270;
    private int POWER_REQUIRED = 130;
    // In case of RFD8500 change reader name with intended device below from list of paired RFD8500
    String readername = "RFD8500123";

    public void init(ResponseHandlerInterface listener,Context context,int antennaPower) {
        // application context
        this.listener = listener;
        this.context = context;
        POWER_REQUIRED=antennaPower;
        // Status UI
        //textView = activity.statusTextViewRFID;
        // SDK
        InitSDK();
    }

    // TEST BUTTON functionality
    // following two tests are to try out different configurations features

    public String Test1() {
        // check reader connection
        if (!isReaderConnected())
            return "Not connected";
        // set antenna configurations - reducing power to 200
        try {
            Antennas.AntennaRfConfig config = null;
            config = reader.Config.Antennas.getAntennaRfConfig(1);
            config.setTransmitPowerIndex(100);
            config.setrfModeTableIndex(0);
            config.setTari(0);
            reader.Config.Antennas.setAntennaRfConfig(1, config);
        } catch (InvalidUsageException e) {
            e.printStackTrace();
        } catch (OperationFailureException e) {
            e.printStackTrace();
            return e.getResults().toString() + " " + e.getVendorMessage();
        }
        return "Antenna power Set to 220";
    }

    public String Test2() {
        // check reader connection
        if (!isReaderConnected())
            return "Not connected";
        // Set the singulation control to S2 which will read each tag once only
        try {
            Antennas.SingulationControl s1_singulationControl = reader.Config.Antennas.getSingulationControl(1);
            s1_singulationControl.setSession(SESSION.SESSION_S2);
            s1_singulationControl.Action.setInventoryState(INVENTORY_STATE.INVENTORY_STATE_A);
            s1_singulationControl.Action.setSLFlag(SL_FLAG.SL_ALL);
            reader.Config.Antennas.setSingulationControl(1, s1_singulationControl);
        } catch (InvalidUsageException e) {
            e.printStackTrace();
        } catch (OperationFailureException e) {
            e.printStackTrace();
            return e.getResults().toString() + " " + e.getVendorMessage();
        }
        return "Session set to S2";
    }

    public String Defaults() {
        // check reader connection
        if (!isReaderConnected())
            return "Not connected";
        ;
        try {
            // Power to 270
            Antennas.AntennaRfConfig config = null;
            config = reader.Config.Antennas.getAntennaRfConfig(1);
            config.setTransmitPowerIndex(MAX_POWER);
            config.setrfModeTableIndex(0);
            config.setTari(0);
            reader.Config.Antennas.setAntennaRfConfig(1, config);
            // singulation to S0
            Antennas.SingulationControl s1_singulationControl = reader.Config.Antennas.getSingulationControl(1);
            s1_singulationControl.setSession(SESSION.SESSION_S0);
            s1_singulationControl.Action.setInventoryState(INVENTORY_STATE.INVENTORY_STATE_A);
            s1_singulationControl.Action.setSLFlag(SL_FLAG.SL_ALL);
            reader.Config.Antennas.setSingulationControl(1, s1_singulationControl);
        } catch (InvalidUsageException e) {
            e.printStackTrace();
        } catch (OperationFailureException e) {
            e.printStackTrace();
            return e.getResults().toString() + " " + e.getVendorMessage();
        }
        return "Default settings applied";
    }

    public boolean isReaderConnected() {
        if (reader != null && reader.isConnected())
            return true;
        else {
            Log.d(TAG, "reader is not connected");
            return false;
        }
    }

    //
    //  Activity life cycle behavior
    //

    public String onResume() {
        return connect();
    }

    public void onPause() {
        disconnect();
    }

    public void onDestroy() {
        dispose();
    }

    //
    // RFID SDK
    //

    void InitSDK() {
        Log.d(TAG, "InitSDK");
        if (readers == null) {
            new CreateInstanceTask().execute();
        } else
            new ConnectionTask().execute();
    }

    // Enumerates SDK based on host device
    private class CreateInstanceTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            Log.d(TAG, "CreateInstanceTask");
            // Based on support available on host device choose the reader type
            try
            {
                InvalidUsageException invalidUsageException = null;
                readers = new Readers(context, ENUM_TRANSPORT.ALL);
                availableRFIDReaderList = readers.GetAvailableRFIDReaderList();
                if (invalidUsageException != null) {
                    readers.Dispose();
                    readers = null;
                    if (readers == null) {
                        readers = new Readers(context, ENUM_TRANSPORT.BLUETOOTH);
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            new ConnectionTask().execute();
        }
    }

    private class ConnectionTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            Log.d(TAG, "ConnectionTask");
            try {
                GetAvailableReader();
            } catch (InvalidUsageException e) {
                throw new RuntimeException(e);
            }
            if (reader != null)
                return connect();
            return "Failed to find or connect reader";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            //textView.setText(result);
            Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
        }
    }

    private synchronized void GetAvailableReader() throws InvalidUsageException {
        Log.d(TAG, "GetAvailableReader");
        if (readers != null) {
            readers.attach(this);
            if (readers.GetAvailableRFIDReaderList() != null) {
                availableRFIDReaderList = readers.GetAvailableRFIDReaderList();
                if (availableRFIDReaderList.size() != 0) {
                    // if single reader is available then connect it
                    if (availableRFIDReaderList.size() == 1) {
                        readerDevice = availableRFIDReaderList.get(0);
                        reader = readerDevice.getRFIDReader();
                    } else {
                        // search reader specified by name
                        for (ReaderDevice device : availableRFIDReaderList) {
                            if (device.getName().equals(readername)) {
                                readerDevice = device;
                                reader = readerDevice.getRFIDReader();
                            }
                        }
                    }
                }
            }
        }
    }

    // handler for receiving reader appearance events
    @Override
    public void RFIDReaderAppeared(ReaderDevice readerDevice) {
        Log.d(TAG, "RFIDReaderAppeared " + readerDevice.getName());
        new ConnectionTask().execute();
    }

    @Override
    public void RFIDReaderDisappeared(ReaderDevice readerDevice) {
        Log.d(TAG, "RFIDReaderDisappeared " + readerDevice.getName());
        if (readerDevice.getName().equals(reader.getHostName()))
            disconnect();
    }


    private synchronized String connect() {
        if (reader != null) {
            Log.d(TAG, "connect " + reader.getHostName());
            try {
                if (!reader.isConnected()) {
                    // Establish connection to the RFID Reader
                    reader.connect();
                    ConfigureReader();
                    return "Connected";
                }
            } catch (InvalidUsageException e) {
                e.printStackTrace();
            } catch (OperationFailureException e) {
                e.printStackTrace();
                Log.d(TAG, "OperationFailureException " + e.getVendorMessage());
                String des = e.getResults().toString();
                return "Connection failed" + e.getVendorMessage() + " " + des;
            }
        }
        return "";
    }

    private void ConfigureReader() {
        Log.d(TAG, "ConfigureReader " + reader.getHostName());
        if (reader.isConnected()) {
            TriggerInfo triggerInfo = new TriggerInfo();
            triggerInfo.StartTrigger.setTriggerType(START_TRIGGER_TYPE.START_TRIGGER_TYPE_IMMEDIATE);
            triggerInfo.StopTrigger.setTriggerType(STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_IMMEDIATE);
            try {
                // receive events from reader
                if (eventHandler == null)
                    eventHandler = new EventHandler();
                reader.Events.addEventsListener(eventHandler);
                // HH event
                reader.Events.setHandheldEvent(true);
                // tag event with tag data
                reader.Events.setTagReadEvent(true);
                reader.Events.setAttachTagDataWithReadEvent(false);
                // set trigger mode as rfid so scanner beam will not come
                reader.Config.setTriggerMode(ENUM_TRIGGER_MODE.RFID_MODE, true);
                // set start and stop triggers
                reader.Config.setStartTrigger(triggerInfo.StartTrigger);
                reader.Config.setStopTrigger(triggerInfo.StopTrigger);
                // power levels are index based so maximum power supported get the last one
                MAX_POWER = reader.ReaderCapabilities.getTransmitPowerLevelValues().length - 1;
                // set antenna configurations
                Antennas.AntennaRfConfig config = reader.Config.Antennas.getAntennaRfConfig(1);
                //POWER_REQUIRED = Utils.getSharedPrefsInteger(context, Constants.ANTENNA_POWER, Constants.ANTENNA_POWER_SHARED);
                config.setTransmitPowerIndex(POWER_REQUIRED);
                config.setrfModeTableIndex(0);
                config.setTari(0);
                reader.Config.Antennas.setAntennaRfConfig(1, config);
                // Set the singulation control
                Antennas.SingulationControl s1_singulationControl = reader.Config.Antennas.getSingulationControl(1);
                s1_singulationControl.setSession(SESSION.SESSION_S0);
                s1_singulationControl.Action.setInventoryState(INVENTORY_STATE.INVENTORY_STATE_A);
                s1_singulationControl.Action.setSLFlag(SL_FLAG.SL_ALL);
                reader.Config.Antennas.setSingulationControl(1, s1_singulationControl);
                // delete any prefilters
                reader.Actions.PreFilters.deleteAll();
                //
            } catch (InvalidUsageException | OperationFailureException e) {
                e.printStackTrace();
            }
        }
    }

    private synchronized void disconnect() {
        Log.d(TAG, "disconnect " + reader);
        try {
            if (reader != null) {
                reader.Events.removeEventsListener(eventHandler);
                reader.disconnect();
                ((Activity)context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //textView.setText("Disconnected");
                        Toast.makeText(context, "Disconnected", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } catch (InvalidUsageException e) {
            e.printStackTrace();
        } catch (OperationFailureException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private synchronized void dispose() {
        try {
            if (readers != null) {
                reader = null;
                readers.Dispose();
                readers = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void performInventory() {
        // check reader connection
        if (!isReaderConnected())
            return;
        try {
            reader.Actions.Inventory.perform();
        } catch (InvalidUsageException e) {
            e.printStackTrace();
        } catch (OperationFailureException e) {
            e.printStackTrace();
        }
    }

    public synchronized void stopInventory() {
        // check reader connection
        if (!isReaderConnected())
            return;
        try {
            reader.Actions.Inventory.stop();
        } catch (InvalidUsageException e) {
            e.printStackTrace();
        } catch (OperationFailureException e) {
            e.printStackTrace();
        }
    }

    public void disableDpoState() {
        if (!isReaderConnected())
            return;
        try {
            reader.Config.setDPOState(DYNAMIC_POWER_OPTIMIZATION.DISABLE);
        } catch (InvalidUsageException e) {
            e.printStackTrace();
        } catch (OperationFailureException e) {
            e.printStackTrace();
        }
    }

    public void enableDpoState() {
        if (!isReaderConnected())
            return;
        try {
            reader.Config.setDPOState(DYNAMIC_POWER_OPTIMIZATION.ENABLE);
        } catch (InvalidUsageException e) {
            e.printStackTrace();
        } catch (OperationFailureException e) {
            e.printStackTrace();
        }
    }
  /*  public boolean writeTagData(String sourceEPC, String targetData) {
        Log.d(TAG, "WriteTag " + targetData);
        try {
            // Stop Inventory and disable DPO
            reader.Actions.Inventory.stop();
            reader.Config.setDPOState(DYNAMIC_POWER_OPTIMIZATION.DISABLE);

            // Convert EPC and Data to Hex
            String tagD = asciitohex.convert(sourceEPC);
            String targetDataNew = asciitohex.convert(targetData);

            // Initialize TagAccess and WriteAccessParams
            TagAccess tagAccess = new TagAccess();
            TagAccess.WriteAccessParams writeAccessParams = tagAccess.new WriteAccessParams();

            // Set WriteAccessParams
            writeAccessParams.setOffset(2);
            writeAccessParams.setWriteData(targetDataNew);
            writeAccessParams.setMemoryBank(MEMORY_BANK.MEMORY_BANK_EPC);
            writeAccessParams.setWriteRetries(3);
            // Set the correct length in words (1 word = 2 bytes)
            writeAccessParams.setWriteDataLength(targetDataNew.length() / 4);  // Assuming Hex string length is twice the byte length
            writeAccessParams.setAccessPassword(0x00);  // Use appropriate password

            // Perform Write Operation
            reader.Actions.TagAccess.writeWait(tagD, writeAccessParams, null, null);

            // Enable DPO again
            reader.Config.setDPOState(DYNAMIC_POWER_OPTIMIZATION.ENABLE);

            Log.d(TAG, "Write successful");
            return true;  // Indicate success

        } catch (InvalidUsageException | OperationFailureException e) {
            e.printStackTrace();
            return false;  // Indicate failure
        }
    }*/


    private void setAntennaPower(int power) {
        Log.d(TAG, "setAntennaPower " + power);
        try {
            // set antenna configurations
            Antennas.AntennaRfConfig config = reader.Config.Antennas.getAntennaRfConfig(1);
            config.setTransmitPowerIndex(power);
            config.setrfModeTableIndex(0);
            config.setTari(0);
            reader.Config.Antennas.setAntennaRfConfig(1, config);
        } catch (InvalidUsageException e) {
            e.printStackTrace();
        } catch (OperationFailureException e) {
            e.printStackTrace();
        }
    }


    private void setDPO(boolean bEnable) {
        Log.d(TAG, "setDPO " + bEnable);
        try {
            // control the DPO
            reader.Config.setDPOState(bEnable ? DYNAMIC_POWER_OPTIMIZATION.ENABLE : DYNAMIC_POWER_OPTIMIZATION.DISABLE);
        } catch (InvalidUsageException e) {
            e.printStackTrace();
        } catch (OperationFailureException e) {
            e.printStackTrace();
        }
    }

    private void setAccessOperationConfiguration() {
        // set required power and profile
        setAntennaPower(240);
        // in case of RFD8500 disable DPO
        if (reader.getHostName().contains("RFD8500"))
            setDPO(false);
        //
        try {
            // set access operation time out value to 1 second, so reader will tries for a second
            // to perform operation before timing out
            reader.Config.setAccessOperationWaitTimeout(1000);
        } catch (InvalidUsageException e) {
            e.printStackTrace();
        } catch (OperationFailureException e) {
            e.printStackTrace();
        }
    }



/*
    public void writeTagData(String sourceEPC, String targetData, RfidListeners rfidListeners) {
        Log.d(TAG, "WriteTag " + targetData);
        try {
            // Stop Inventory and disable DPO
            reader.Actions.Inventory.stop();
            reader.Config.setDPOState(DYNAMIC_POWER_OPTIMIZATION.DISABLE);

            // Convert EPC and Data to Hex
            String tagD = asciitohex.convert(sourceEPC);
            String targetDataNew = asciitohex.convert(targetData);

            // Initialize TagAccess and WriteAccessParams
            TagAccess tagAccess = new TagAccess();
            TagAccess.WriteAccessParams writeAccessParams = tagAccess.new WriteAccessParams();

            // Set WriteAccessParams
            //writeAccessParams.setAccessPassword(0x00);
            writeAccessParams.setOffset(2);
            writeAccessParams.setWriteData(targetDataNew);
            writeAccessParams.setMemoryBank(MEMORY_BANK.MEMORY_BANK_EPC);
            writeAccessParams.setWriteRetries(3);
            // Set the correct length in words (1 word = 2 bytes)
            writeAccessParams.setWriteDataLength(targetDataNew.length() / 4);  // Assuming Hex string length is twice the byte length
            writeAccessParams.setAccessPassword(0x00);  // Use appropriate password

            // Perform Write Operation
            //reader.Actions.TagAccess.writeWait(tagD, writeAccessParams, null, null);


            new AsyncTask<Void, Void, Boolean>() {
                private Boolean bResult = false;
                private InvalidUsageException invalidUsageException;
                private OperationFailureException operationFailureException;

                @Override
                protected Boolean doInBackground(Void... voidArr) {
                    try {
                        reader.Actions.TagAccess.writeWait(tagD, writeAccessParams, null, null);
                        this.bResult = true;
                    } catch (InvalidUsageException e2) {
                        this.invalidUsageException = e2;
                        if (e2.getStackTrace().length > 0) {
                            Log.e("tAG", e2.getStackTrace()[0].toString());
                        }
                    } catch (OperationFailureException e3) {
                        this.operationFailureException = e3;
                        if (e3.getStackTrace().length > 0) {
                            Log.e("tAG", e3.getStackTrace()[0].toString());
                        }
                    }
                    return this.bResult;
                }

                @Override
                protected void onPostExecute(Boolean bool) {
                    if (!bool) {
                        if (this.invalidUsageException != null) {
                            rfidListeners.onFailure(this.invalidUsageException);
                        } else if (this.operationFailureException != null) {
                            rfidListeners.onFailure(this.operationFailureException);
                        }
                    } else {
                        rfidListeners.onSuccess(null);
                    }
                }
            }.execute();



            // Enable DPO again
            reader.Config.setDPOState(DYNAMIC_POWER_OPTIMIZATION.ENABLE);

            Log.d(TAG, "Write successful");

        } catch (InvalidUsageException | OperationFailureException e) {
            e.printStackTrace();
        }
    }*/

/*
   public void writeTagData(String sourceEPC, String targetData) {
       Log.d(TAG, "WriteTag " + targetData);
       try {

           // make sure Inventory is stopped
           reader.Actions.Inventory.stop();

           // make sure DPO is disabled
           reader.Config.setDPOState(DYNAMIC_POWER_OPTIMIZATION.DISABLE);

           String tagD = asciitohex.convert(sourceEPC);
           String targetDataNew = asciitohex.convert(targetData);

           java.lang.String r2 = "0X";

           TagData tagData = null;
           String tagId = sourceEPC;
           String writeData = targetData;


           TagAccess tagAccess = new TagAccess();
           TagAccess.WriteAccessParams writeAccessParams = tagAccess.new WriteAccessParams();

           writeAccessParams.setOffset(2);
           writeAccessParams.setWriteData(targetDataNew);
           writeAccessParams.setMemoryBank(MEMORY_BANK.MEMORY_BANK_EPC);
           writeAccessParams.setWriteRetries(3);
           writeAccessParams.setWriteDataLength(0);
           writeAccessParams.setAccessPassword(00);
           reader.Actions.TagAccess.writeWait(tagD, writeAccessParams, null, tagData);
           Log.e("Tag data ", tagData.getTagID());

           //enable again
           reader.Config.setDPOState(DYNAMIC_POWER_OPTIMIZATION.ENABLE);
           //reader.Actions.Inventory.perform();

       } catch (InvalidUsageException | OperationFailureException e) {
           e.printStackTrace();
       }
   }
*/



    // Utility method to convert hex string to byte array
    private byte[] convertHexToByteArray(String hex) {
        if (hex == null || hex.length() % 2 != 0) {
            Log.e(TAG, "Invalid hex string.");
            return null;
        }
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }

    // Utility method to convert byte array to hex string
    private String byteArrayToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    // Implement the RfidEventsLister class to receive event notifications
    public class EventHandler implements RfidEventsListener {
        // Read Event Notification
        public void eventReadNotify(RfidReadEvents e) {
            // Recommended to use new method getReadTagsEx for better performance in case of large tag population
            TagData[] myTags = reader.Actions.getReadTags(100);
            if (myTags != null) {
                for (int index = 0; index < myTags.length; index++) {
                    Log.d(TAG, "Tag ID " + myTags[index].getTagID());
                    if (myTags[index].getOpCode() == ACCESS_OPERATION_CODE.ACCESS_OPERATION_READ &&
                            myTags[index].getOpStatus() == ACCESS_OPERATION_STATUS.ACCESS_SUCCESS) {
                        if (myTags[index].getMemoryBankData().length() > 0) {
                            Log.d(TAG, " Mem Bank Data " + myTags[index].getMemoryBankData());
                        }
                    }
                    if (myTags[index].isContainsLocationInfo()) {
                        short dist = myTags[index].LocationInfo.getRelativeDistance();
                        Log.d(TAG, "Tag relative distance " + dist);
                    }
                }
                // possibly if operation was invoked from async task and still busy
                // handle tag data responses on parallel thread thus THREAD_POOL_EXECUTOR
                new AsyncDataUpdate().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, myTags);
            }
        }

        // Status Event Notification
        public void eventStatusNotify(RfidStatusEvents rfidStatusEvents) {
            Log.d(TAG, "Status Notification: " + rfidStatusEvents.StatusEventData.getStatusEventType());
            if (rfidStatusEvents.StatusEventData.getStatusEventType() == STATUS_EVENT_TYPE.HANDHELD_TRIGGER_EVENT) {
                if (rfidStatusEvents.StatusEventData.HandheldTriggerEventData.getHandheldEvent() == HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_PRESSED) {

                  new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... voids) {
                            listener.handleTriggerPress(true);
                            return null;
                        }
                    }.execute();
                }
                if (rfidStatusEvents.StatusEventData.HandheldTriggerEventData.getHandheldEvent() == HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_RELEASED) {
                   new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... voids) {
                            listener.handleTriggerPress(false);
                            return null;
                        }
                    }.execute();
                }
            }
        }
    }

    private class AsyncDataUpdate extends AsyncTask<TagData[], Void, Void> {
        @Override
        protected Void doInBackground(TagData[]... params) {
            listener.handleTagdata(params[0]);
            return null;
        }
    }

    public interface ResponseHandlerInterface {
        void handleTagdata(TagData[] tagData);

        void handleTriggerPress(boolean pressed);
        //void handleStatusEvents(Events.StatusEventData eventData);
    }

}