package com.paad.bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.app.ListActivity;
import android.widget.*;

public class BluetoothActivity extends Activity {
  
    protected static final String TAG = "BLUETOOTH";
    protected static final int DISCOVERY_REQUEST = 1;
    BluetoothAdapter bluetooth;
    IntentFilter btFilter=new IntentFilter();
    
    ToggleButton btOnOffButton=null;
    Button btSearch=null;
    
    String gTag="BluetoothActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.main);
      
		bluetooth = BluetoothAdapter.getDefaultAdapter(); 
		if(bluetooth==null){
			showToast("No Bluetooth Adapter!");
			addText("No BT Adapter");
		}
		else{
			addText("BT adapter found");
		}
		     
		setupBTbroadcastReceiver();
		
      btOnOffButton=(ToggleButton)findViewById(R.id.toggleButton1);
      btSearch=(Button)findViewById(R.id.searchButton);
      
      updateToggleButton();
      
      setToggleButtonOnClickListener();
      
      initSpinner();
      setSearchButtonOnClickListener();
      setSelectButtonOnClickListener();

    }
    
	@Override
	public void onPause(){
		super.onPause();
	    // Unregister broadcast listeners
	    this.unregisterReceiver(onBTchange);
	}
    
	@Override
    public void onDestroy(){
    	super.onDestroy();
	    // Unregister broadcast listeners
	    this.unregisterReceiver(onBTchange);
    }

	void showToast(String message){
		Context context = this;
		String msg = message;
		int duration = Toast.LENGTH_SHORT;
		Toast toast = Toast.makeText(context, msg, duration);
		toast.show();
	}
	
    void updateToggleButton(){
        btOnOffButton.setChecked(bluetooth.isEnabled());
        /*
        if(this.bluetooth.isEnabled())
      	  btOnOff.setChecked(true);
        else
      	  btOnOff.setChecked(false);
    	*/
    }
    
	void setupBTbroadcastReceiver(){
	    // Register for broadcasts on BluetoothAdapter state change
	    // Bluetooth on/off broadcasts
        btFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        // Discovery broadcasts
        btFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        btFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        btFilter.addAction(BluetoothDevice.ACTION_FOUND);
        btFilter.addAction(BluetoothDevice.ACTION_NAME_CHANGED);
	    //register receiver	    
	    this.registerReceiver(onBTchange, btFilter);
	    addText("broadcast receiver setup OK");		
	}
	
 	void setToggleButtonOnClickListener(){
		btOnOffButton=(ToggleButton)findViewById(R.id.toggleButton1);
		btOnOffButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(bluetooth.isEnabled()){
					showToast("BT disabling not allowed");
					btOnOffButton.setChecked(true);
					//stopBluetooth();
				}
				else{
					startBluetooth();
					//bluetooth.enable();
				}
				//updateToggleButton();
			}
		});
    }
 	
    Spinner spinner1;
    ArrayList<String> btStringList=new ArrayList<String>();
    ArrayAdapter<String> dataAdapter;
    CustomOnItemSelectedListener listItemListener;
    private void initSpinner() {
    	btStringList.clear();
    	btStringList.add("Zeile1");
    	btStringList.add("Zeile2");
    	btStringList.add("Zeile3");
    	
    	spinner1 = (Spinner) findViewById(R.id.spinnerBT);
    	dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, btStringList);
    	//dataAdapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
    	spinner1.setAdapter(dataAdapter);
		spinner1 = (Spinner) findViewById(R.id.spinnerBT);
		listItemListener=new CustomOnItemSelectedListener();
		spinner1.setOnItemSelectedListener(listItemListener); 
	}

	void setSelectButtonOnClickListener(){
    	Button selectDevice=(Button)findViewById(R.id.selectButton);
    	selectDevice.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				TextView tvMac = (TextView)findViewById(R.id.editMac);
				spinner1=(Spinner)findViewById(R.id.spinnerBT);
				
				tvMac.setText(spinner1.getSelectedItem().toString());
				tvMac.refreshDrawableState();
			}
		});
    }

    
    void setSearchButtonOnClickListener(){
    	if(btSearch==null)
    		btSearch=(Button)findViewById(R.id.searchButton);
    	
    	btSearch.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (!bluetooth.isEnabled()){
					addText("Bluetooth is OFF. Enable BT first.");
					return;
				}
				startDiscovery();
			}
		});
    }
    
    void addItem(String s){
    	btStringList.add(s);
    	//dataAdapter.add(s);
    }
    
    
    static TextView txtLog=null;
    void addText(String s){
    	
    	if(txtLog==null){
    		txtLog=(TextView)findViewById(R.id.textView1);    		
    	}
    	txtLog.append(s + "\n");

		final Layout layout = txtLog.getLayout();
		if(layout != null){
		int scrollDelta = layout.getLineBottom(txtLog.getLineCount() - 1) - txtLog.getScrollY() - txtLog.getHeight();
		if(scrollDelta > 0)
			txtLog.scrollBy(0, scrollDelta);
		}
    	//mTxtOutput.refreshDrawableState();
    	
    	Log.d(gTag, s);
    }
    
    /**
     * Listing 16-2: Enabling Bluetooth
     */
    private static final int ENABLE_BLUETOOTH = 1;

    private void startBluetooth() {
        if (!bluetooth.isEnabled()) {
      	  addText("Bluetooth was off");
          // Bluetooth isn't enabled, prompt the user to turn it on.
          Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
          startActivityForResult(intent, ENABLE_BLUETOOTH);
        } else {
      	  addText("Bluetooth was on");
          // Bluetooth is enabled, initialize the UI.
          //initBluetoothUI();
        }
        if (bluetooth.isEnabled())
      	  addText("Bluetooth is on");
        else
      	  addText("Bluetooth is off");
      }

    //called when BT has been enabled or not
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == ENABLE_BLUETOOTH){
			if (resultCode == RESULT_OK) {
				// Bluetooth has been enabled, initialize the UI.
				//initBluetoothUI();
				addText("BT has been enabled");
				btOnOffButton.setChecked(true);
			}      
		}
		if (requestCode == DISCOVERY_REQUEST) {
			if (resultCode == RESULT_CANCELED) {
		      Log.d(TAG, "Discovery cancelled by user");
		    }
		}
    }
    
    private void makeDiscoverable() {
      /**
       * Listing 16-3: Enabling discoverability
       */
      startActivityForResult(
        new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE),
                   DISCOVERY_REQUEST);
    }
    
    /**
     * Listing 16-5: Discovering remote Bluetooth Devices
     */
    private ArrayList<BluetoothDevice> deviceList =  new ArrayList<BluetoothDevice>();
    
    private void startDiscovery() {
    	IntentFilter discoveryFilter = new IntentFilter();
    	discoveryFilter.addAction(BluetoothDevice.ACTION_FOUND);
    	//discoveryFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
    	//discoveryFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
    	
    	registerReceiver(discoveryResult, discoveryFilter);

      if (bluetooth.isEnabled() && !bluetooth.isDiscovering()){
      	addText("Discovery started");
        deviceList.clear();
        btStringList.clear();
        spinner1.refreshDrawableState();
        bluetooth.startDiscovery();
      }
      else{
        	addText("Discovery already running");
      }
    }

    //receiver for BT state changes
    //and Discovery changes
    private final BroadcastReceiver onBTchange = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        	String prevStateExtra = BluetoothAdapter.EXTRA_PREVIOUS_STATE;
        	String stateExtra = BluetoothAdapter.EXTRA_STATE;
        	int state = intent.getIntExtra(stateExtra, -1);
        	int previousState = intent.getIntExtra(prevStateExtra, -1);
	  	  String action = intent.getAction();
	  	  Log.d(gTag, "onReceive: " + action);
	  	  String tt="";
			switch (state) {
				case BluetoothAdapter.STATE_OFF:
					addText("Bluetooth off");
					tt = "Bluetooth off";
					btOnOffButton.setChecked(false);
					break;
				case BluetoothAdapter.STATE_TURNING_OFF:
					addText("Turning Bluetooth off...");
					tt = "Bluetooth turning off";
					break;
				case BluetoothAdapter.STATE_ON:
					addText("Bluetooth on");
					tt = "Bluetooth on";
					btOnOffButton.setChecked(true);
					break;
				case BluetoothAdapter.STATE_TURNING_ON:
					tt = "Bluetooth turning on";
					addText("Turning Bluetooth on...");
					break;
			}
			//for string compare use object.equals() not as simple == compare
			if(action.equalsIgnoreCase(BluetoothAdapter.ACTION_DISCOVERY_STARTED)){
				tt = "ACTION_DISCOVERY_STARTED";
				addText("ACTION_DISCOVERY_STARTED...");
				btSearch.setEnabled(false);
			}
			else if(action.equalsIgnoreCase(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)){
				tt = "ACTION_DISCOVERY_FINISHED";
				addText("ACTION_DISCOVERY_FINISHED...");
				btSearch.setEnabled(true);
			}
				
			Log.d(gTag, tt);
        }
    };
    
    //this is called for every discovered device
    BroadcastReceiver discoveryResult = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
    	  String action = intent.getAction();
    	  /*
    	  if(BluetoothAdapter.ACTION_DISCOVERY_STARTED==action){
    		  addText("discovery started");
    	  }
    	  else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED==action){
    		  addText("Discovery finished");
    		  dataAdapter.notifyDataSetChanged();
    	  }
    	  */
    	  if(BluetoothDevice.ACTION_FOUND==action){
				String remoteDeviceName = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
				BluetoothDevice remoteDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				
				deviceList.add(remoteDevice);
				addText(remoteDeviceName + "(" + remoteDevice.getAddress().toString() + ")");
				addItem(remoteDevice.getAddress().toString());
				
				Log.d(TAG, "Discovered " + remoteDeviceName);
    	  }
      }
    };
    
    /**
     * Listing 16-6: Listening for Bluetooth Socket connection requests
     */
    private BluetoothSocket transferSocket;

    private UUID startServerSocket(BluetoothAdapter bluetooth) {
      UUID uuid = UUID.fromString("a60f35f0-b93a-11de-8a39-08002009c666");
      String name = "bluetoothserver";

      try {
        final BluetoothServerSocket btserver = 
          bluetooth.listenUsingRfcommWithServiceRecord(name, uuid);

        Thread acceptThread = new Thread(new Runnable() {
          public void run() {
            try {
              // Block until client connection established.
              BluetoothSocket serverSocket = btserver.accept();
              // Start listening for messages.
              StringBuilder incoming = new StringBuilder();
              listenForMessages(serverSocket, incoming);
              // Add a reference to the socket used to send messages.
              transferSocket = serverSocket;
            } catch (IOException e) {
              Log.e("BLUETOOTH", "Server connection IO Exception", e);
            }
          }
        });
        acceptThread.start();
      } catch (IOException e) {
        Log.e("BLUETOOTH", "Socket listener IO Exception", e);
      }
      return uuid;
    }

    /**
     * Listing 16-7: Creating a Bluetooth client socket
     */
    private void connectToServerSocket(BluetoothDevice device, UUID uuid) {
      try{
        BluetoothSocket clientSocket 
          = device.createRfcommSocketToServiceRecord(uuid);

        // Block until server connection accepted.
        clientSocket.connect();

        // Start listening for messages.
        StringBuilder incoming = new StringBuilder();
        listenForMessages(clientSocket, incoming);

        // Add a reference to the socket used to send messages.
        transferSocket = clientSocket;

      } catch (IOException e) {
        Log.e("BLUETOOTH", "Blueooth client I/O Exception", e);
      }
    }

    /**
     * Listing 16-8: Sending and receiving strings using Bluetooth Sockets
     */
    private void sendMessage(BluetoothSocket socket, String message) {
      OutputStream outStream;
      try {
        outStream = socket.getOutputStream();

        // Add a stop character.
        byte[] byteArray = (message + " ").getBytes();
        byteArray[byteArray.length - 1] = 0;

        outStream.write(byteArray);
      } catch (IOException e) { 
        Log.e(TAG, "Message send failed.", e);
      }
    }

    private boolean listening = false;
     
    private void listenForMessages(BluetoothSocket socket, 
                                   StringBuilder incoming) {
      listening = true;


      int bufferSize = 1024;
      byte[] buffer = new byte[bufferSize];

      try {
        InputStream instream = socket.getInputStream();
        int bytesRead = -1;

        while (listening) {
          bytesRead = instream.read(buffer);
          if (bytesRead != -1) {
            String result = "";
            while ((bytesRead == bufferSize) &&
                   (buffer[bufferSize-1] != 0)){
              result = result + new String(buffer, 0, bytesRead - 1);
              bytesRead = instream.read(buffer);
            }
            result = result + new String(buffer, 0, bytesRead - 1);
            incoming.append(result);
          }
          socket.close();
        }
      } catch (IOException e) {
        Log.e(TAG, "Message received failed.", e);
      }
      finally {
      }
    }
    
    private void initBluetoothUI() {
      // TODO Update the UI now that Bluetooth is enabled. 
    	
    }
}

