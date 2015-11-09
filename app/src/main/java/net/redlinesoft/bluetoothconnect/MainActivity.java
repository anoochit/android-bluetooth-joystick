package net.redlinesoft.bluetoothconnect;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;

public class MainActivity extends AppCompatActivity {

    CoordinatorLayout coordinatorLayout;
    Context context;
    BluetoothSPP bt;
    Boolean btConnect=false;
    Menu menu;

    RelativeLayout layout_joystick;
    //ImageView image_joystick, image_border;
    TextView textView1, textView2, textView3, textView4, textView5;

    JoyStickClass js;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(false);
        }

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinateLayout);
        bt = new BluetoothSPP(getApplicationContext());

        checkBluetoothState();


        bt.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
            public void onDeviceConnected(String name, String address) {
                // Do something when successfully connected
                showSnackBarMessage("Connected");
                btConnect=true;
                MenuItem settingsItem = menu.findItem(R.id.action_connect);
                // set your desired icon here based on a flag if you like
                settingsItem.setIcon(getResources().getDrawable(R.drawable.ic_action_process_end));
            }

            public void onDeviceDisconnected() {
                // Do something when connection was disconnected
                showSnackBarMessage("Disconnected");
                btConnect=false;
                btConnect=true;
                MenuItem settingsItem = menu.findItem(R.id.action_connect);
                // set your desired icon here based on a flag if you like
                settingsItem.setIcon(getResources().getDrawable(R.drawable.ic_action_bluetooth));
            }

            public void onDeviceConnectionFailed() {
                // Do something when connection failed
                showSnackBarMessage("Connection failed");
                btConnect=false;
            }
        });


    }


    public String getPWM(float Distant) {
        String pwm="100";
        if (Distant >= 255) {
            pwm = "255";
        } else if (Distant <=50 ) {
            pwm="100";
        } else {
            pwm=String.valueOf(Distant);
        }
        return pwm;
    }

    public void setup() {

        textView1 = (TextView) findViewById(R.id.textView1);
        textView2 = (TextView) findViewById(R.id.textView2);
        textView3 = (TextView) findViewById(R.id.textView3);
        textView4 = (TextView) findViewById(R.id.textView4);
        textView5 = (TextView) findViewById(R.id.textView5);

        layout_joystick = (RelativeLayout) findViewById(R.id.layout_joystick);

        js = new JoyStickClass(getApplicationContext(),layout_joystick, R.drawable.image_button);
        js.setStickSize(150, 150);
        js.setLayoutSize(800, 800);
        js.setOffset(90);
        js.setMinimumDistance(10);
        js.setLayoutAlpha(150);
        js.setStickAlpha(100);

        layout_joystick.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View arg0, MotionEvent arg1) {
                js.drawStick(arg1);
                if (arg1.getAction() == MotionEvent.ACTION_DOWN
                        || arg1.getAction() == MotionEvent.ACTION_MOVE) {
                    textView1.setText("X : " + String.valueOf(js.getX()));
                    textView2.setText("Y : " + String.valueOf(js.getY()));
                    textView3.setText("Angle : " + String.valueOf(js.getAngle()));
                    textView4.setText("Distance : " + String.valueOf(js.getDistance()));

                    int direction = js.get8Direction();
                    if (direction == JoyStickClass.STICK_UP) {
                        textView5.setText("Direction : Up");
                        // sent up
                        bt.send("*10|10|"+getPWM(js.getDistance())+"#",true);
                    } else if (direction == JoyStickClass.STICK_UPRIGHT) {
                        textView5.setText("Direction : Up Right");
                    } else if (direction == JoyStickClass.STICK_RIGHT) {
                        textView5.setText("Direction : Right");
                        // sent right
                        bt.send("*10|40|"+getPWM(js.getDistance())+"#",true);
                    } else if (direction == JoyStickClass.STICK_DOWNRIGHT) {
                        textView5.setText("Direction : Down Right");
                    } else if (direction == JoyStickClass.STICK_DOWN) {
                        textView5.setText("Direction : Down");
                        // sent left
                        bt.send("*10|20|"+getPWM(js.getDistance())+"#",true);
                    } else if (direction == JoyStickClass.STICK_DOWNLEFT) {
                        textView5.setText("Direction : Down Left");
                    } else if (direction == JoyStickClass.STICK_LEFT) {
                        textView5.setText("Direction : Left");
                        // sent left
                        bt.send("*10|30|"+getPWM(js.getDistance())+"#",true);
                    } else if (direction == JoyStickClass.STICK_UPLEFT) {
                        textView5.setText("Direction : Up Left");
                    } else if (direction == JoyStickClass.STICK_NONE) {
                        textView5.setText("Direction : Center");
                        bt.send("*10|11|"+getPWM(js.getDistance())+"#",true);
                    }
                } else if (arg1.getAction() == MotionEvent.ACTION_UP) {
                    textView1.setText("X :");
                    textView2.setText("Y :");
                    textView3.setText("Angle :");
                    textView4.setText("Distance :");
                    textView5.setText("Direction :");
                    bt.send("*10|11|3#",true);
                }
                return true;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(!bt.isBluetoothEnabled()) {
            // Do something if bluetooth is disable
            Snackbar.make(coordinatorLayout, "Bluetooth Disable", Snackbar.LENGTH_LONG)
                    .setAction("Turn On", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            loadBlueToothSetting();
                        }
                    }).show();
        }

    }


    protected void showSnackBarMessage(String message) {
        Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_SHORT).show();
    }
    protected  void  checkBluetoothState() {

        if (bt.isBluetoothEnabled()) {

            if (this.btConnect==true) {
                bt.disconnect();
            }

            bt.setupService();
            bt.startService(BluetoothState.DEVICE_OTHER);

            Snackbar.make(coordinatorLayout, "Choose device to connect", Snackbar.LENGTH_LONG)
                    .setAction("Setup", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            loadDeviceList();
                        }
                    }).show();
        }
    }

    protected  void loadDeviceList() {
        Intent intent = new Intent(getApplicationContext(), DeviceList.class);
        startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
    }

    protected void loadBlueToothSetting() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, BluetoothState.REQUEST_ENABLE_BT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if(resultCode == Activity.RESULT_OK)
                bt.connect(data);
                setup();
        } else if(requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER);
                setup();
            } else {
                // Do something if user doesn't choose any device (Pressed back)
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bt.stopService();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        this.menu=menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id=item.getItemId();

        if (id==R.id.action_connect) {
            checkBluetoothState();
        }

        if (id==R.id.action_settings) {
            Intent i = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(i);
        }

        return super.onOptionsItemSelected(item);
    }
}
