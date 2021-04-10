package com.altimeter.bdureau.bearconsole.Flash;

/**
 *   @description: This is used to flash the altimeter firmware from the Android device using an OTG cable
 *   so that the store Android application is compatible with altimeter. This works with the
 *   ATMega328 based altimeters as well as the STM32 based altimeters
 *
 *   @author: boris.dureau@neuf.fr
 *
 **/
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;

import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.altimeter.bdureau.bearconsole.R;

import com.physicaloid.lib.Boards;
import com.physicaloid.lib.Physicaloid;

import com.physicaloid.lib.programmer.avr.UploadErrors;
import com.physicaloid.lib.usb.driver.uart.UartConfig;

import java.io.IOException;

import java.io.InputStream;
import java.util.ArrayList;

import static com.physicaloid.misc.Misc.toHexStr;


public class FlashFirmware extends AppCompatActivity {
    Physicaloid mPhysicaloid;

boolean recorverFirmware = false;
    Boards mSelectedBoard;
    Button btOpen;
    RadioButton rdbAltiMulti,rdbAltiMultiV2, rbAltiServo, rbAltiDuo, rbAltiMultiSTM32, rbAltiGPS;
    TextView tvRead;
    private AlertDialog.Builder builder = null;
    private AlertDialog alert;
    private ArrayList<Boards> mBoardList;
    private UartConfig uartConfig;

    private static final String ASSET_FILE_NAME_ALTIMULTIV2       = "firmwares/2021-04-09-V1_24.altimultiV2.hex";
    private static final String ASSET_FILE_NAME_ALTIMULTI         = "firmwares/2021-04-09-V1_24.altimulti.hex";
    private static final String ASSET_FILE_NAME_ALTISERVO         = "firmwares/2021-04-09-AltiServoV1_3.hex";
    private static final String ASSET_FILE_NAME_ALTIDUO         = "firmwares/2021-04-09-V1_7.AltiDuo.hex";
    private static final String ASSET_FILE_NAME_ALTIMULTISTM32  = "firmwares/2021-04-09-V1_24.altimultiSTM32.bin";
    private static final String ASSET_FILE_NAME_ALTIGPS  = "firmwares/2021-04-05-V1_2RocketGPSLogger.bin";

    private static final String ASSET_FILE_RESET_ALTIDUO = "recover_firmwares/ResetAltiConfigAltiDuo.ino.hex";
    private static final String ASSET_FILE_RESET_ALTIMULTI = "recover_firmwares/ResetAltiConfigAltimulti.ino.hex";
    private static final String ASSET_FILE_RESET_ALTISERVO = "recover_firmwares/ResetAltiConfigAltiServo.ino.hex";
    private static final String ASSET_FILE_RESET_ALTISTM32 = "recover_firmwares/ResetAltiConfigAltimultiSTM32.ino.bin";

    private String[] itemsBaudRate;
    private Spinner dropdownBaudRate;

    // fast way to call Toast
    private void msg(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flash_firmware);

        btOpen          = (Button) findViewById(R.id.btFlash);
        tvRead          = (TextView) findViewById(R.id.tvRead);
        rdbAltiMulti = (RadioButton) findViewById(R.id.radioButAltiMulti);
        rdbAltiMultiV2 = (RadioButton) findViewById(R.id.radioButAltiMultiV2);
        rbAltiServo = (RadioButton) findViewById(R.id.radioButAltiServo);
        rbAltiDuo = (RadioButton) findViewById(R.id.radioButAltiDuo);
        rdbAltiMulti.setChecked(true);
        rbAltiMultiSTM32 = (RadioButton) findViewById(R.id.radioButAltiMultiSTM32);
        rbAltiGPS = (RadioButton) findViewById(R.id.radioButAltiGPS);
        mPhysicaloid = new Physicaloid(this);
        mBoardList = new ArrayList<Boards>();
        for(Boards board : Boards.values()) {
            if(board.support>0) {
                mBoardList.add(board);
            }
        }

        mSelectedBoard = mBoardList.get(0);
        uartConfig = new UartConfig(115200, UartConfig.DATA_BITS8, UartConfig.STOP_BITS1, UartConfig.PARITY_NONE, false, false);

        btOpen.setEnabled(true);
        if(mPhysicaloid.open()) {
            mPhysicaloid.setConfig(uartConfig);


        } else {
            //cannot open
            Toast.makeText(this, getResources().getString(R.string.msg13), Toast.LENGTH_LONG).show();
            //btOpen.setEnabled(false);
        }


        //baud rate
        dropdownBaudRate = (Spinner)findViewById(R.id.spinnerBaud);
        itemsBaudRate = new String[]{ "300",
                "1200",
                "2400",
                "4800",
                "9600",
                "14400",
                "19200",
                "28800",
                "38400",
                "57600",
                "115200",
                "230400"};
        ArrayAdapter<String> adapterBaudRate = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, itemsBaudRate);
        dropdownBaudRate.setAdapter(adapterBaudRate);
        dropdownBaudRate.setSelection(10);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        close();
    }
    public void onClickDismiss(View v) {
        close();
        finish();
    }

    public void onClickRecover(View v) {
        String recoverFileName;
        recoverFileName = ASSET_FILE_RESET_ALTIMULTI;
        if (rdbAltiMulti.isChecked())
            recoverFileName =ASSET_FILE_RESET_ALTIMULTI;
        if (rdbAltiMultiV2.isChecked())
            recoverFileName =ASSET_FILE_RESET_ALTIMULTI;
        if (rbAltiServo.isChecked())
            recoverFileName =ASSET_FILE_RESET_ALTISERVO;
        if (rbAltiDuo.isChecked())
            recoverFileName =ASSET_FILE_RESET_ALTIDUO;
        if (rbAltiMultiSTM32.isChecked())
            recoverFileName =ASSET_FILE_RESET_ALTISTM32;
        if (rbAltiGPS.isChecked())
            recoverFileName =ASSET_FILE_RESET_ALTISTM32;

        tvRead.setText("");
        tvRead.setText(getResources().getString(R.string.after_complete_upload));
        if (!rbAltiMultiSTM32.isChecked() && !rbAltiGPS.isChecked()) {
            try {
                builder = new AlertDialog.Builder(FlashFirmware.this);
                //Recover firmware...
                builder.setMessage(getResources().getString(R.string.msg18))
                        .setTitle(getResources().getString(R.string.msg11))
                        .setCancelable(false)
                        .setNegativeButton(getResources().getString(R.string.firmware_cancel), new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, final int id) {

                                dialog.cancel();
                                mPhysicaloid.cancelUpload();
                            }
                        });
                alert = builder.create();
                alert.show();
                mPhysicaloid.setBaudrate(Integer.parseInt(itemsBaudRate[(int) this.dropdownBaudRate.getSelectedItemId()]));
                mPhysicaloid.upload(mSelectedBoard, getResources().getAssets().open(recoverFileName), mUploadCallback);
            } catch (RuntimeException e) {
                //Log.e(TAG, e.toString());
            } catch (IOException e) {
                //Log.e(TAG, e.toString());
            }
        }
        else {
            recorverFirmware = true;
            new UploadSTM32Asyc().execute();
        }
    }

    public void onClickFlash(View v) {
        String firmwareFileName;

        firmwareFileName = ASSET_FILE_NAME_ALTIMULTI;

        if (rdbAltiMulti.isChecked())
            firmwareFileName = ASSET_FILE_NAME_ALTIMULTI;
        if (rdbAltiMultiV2.isChecked())
            firmwareFileName = ASSET_FILE_NAME_ALTIMULTIV2;
        if (rbAltiServo.isChecked())
            firmwareFileName =ASSET_FILE_NAME_ALTISERVO;
        if (rbAltiDuo.isChecked())
            firmwareFileName =ASSET_FILE_NAME_ALTIDUO;
        if(rbAltiMultiSTM32.isChecked())
            firmwareFileName = ASSET_FILE_NAME_ALTIMULTISTM32;

        tvRead.setText("");
        if(!rbAltiMultiSTM32.isChecked()) {
            try {
                builder = new AlertDialog.Builder(FlashFirmware.this);
                //Flashing firmware...
                builder.setMessage(getResources().getString(R.string.msg10))
                        .setTitle(getResources().getString(R.string.msg11))
                        .setCancelable(false)
                        .setNegativeButton(getResources().getString(R.string.firmware_cancel), new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, final int id) {

                                dialog.cancel();
                                mPhysicaloid.cancelUpload();
                            }
                        });
                alert = builder.create();
                alert.show();

                mPhysicaloid.setBaudrate(Integer.parseInt(itemsBaudRate[(int) this.dropdownBaudRate.getSelectedItemId()]));
                mPhysicaloid.upload(mSelectedBoard, getResources().getAssets().open(firmwareFileName), mUploadCallback);
            } catch (RuntimeException e) {
                //Log.e(TAG, e.toString());
            } catch (IOException e) {
                //Log.e(TAG, e.toString());
            }
        }
        else{
                //uploadSTM32(firmwareFileName);
            recorverFirmware = false;
            new UploadSTM32Asyc().execute();
        }


    }


    private class UploadSTM32Asyc  extends AsyncTask<Void, Void, Void>  // UI thread
    {

        @Override
        protected void onPreExecute() {
            builder = new AlertDialog.Builder(FlashFirmware.this);
            //Flashing firmware...
            builder.setMessage(getResources().getString(R.string.msg10))
                    .setTitle(getResources().getString(R.string.msg11))
                    .setCancelable(false)
                    .setNegativeButton(getResources().getString(R.string.firmware_cancel), new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, final int id) {

                            dialog.cancel();

                        }
                    });
            alert = builder.create();
            alert.show();
        }
        @Override
        protected Void doInBackground(Void... voids) {
            if (!recorverFirmware) {
                if (rbAltiMultiSTM32.isChecked())
                    uploadSTM32(ASSET_FILE_NAME_ALTIMULTISTM32, mUploadSTM32Callback);
                if (rbAltiGPS.isChecked())
                    uploadSTM32(ASSET_FILE_NAME_ALTIGPS, mUploadSTM32Callback);
            } else {
                uploadSTM32(ASSET_FILE_RESET_ALTISTM32, mUploadSTM32Callback);
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            alert.dismiss();
        }
    }
    public void uploadSTM32 ( String fileName, UploadSTM32CallBack UpCallback){
        boolean failed =false;
        InputStream is=null;

        try {
            is = getAssets().open(fileName);

        } catch (IOException e) {
            //e.printStackTrace();
            tvAppend(tvRead, "file not found: " + ASSET_FILE_NAME_ALTIMULTISTM32+ "\n");
        } catch (Exception e) {
            e.printStackTrace();
            tvAppend(tvRead, "gethexfile : " + ASSET_FILE_NAME_ALTIMULTISTM32+ "\n");
        }

        dialogAppend("Starting ...");
        CommandInterface cmd;

        cmd = new CommandInterface(UpCallback, mPhysicaloid);

        cmd.open(Integer.parseInt(itemsBaudRate[(int) this.dropdownBaudRate.getSelectedItemId()]));
        int ret = cmd.initChip();
        if (ret == 1)
            dialogAppend("Chip has been initiated:" + ret);
        else {
            dialogAppend("Chip has not been initiated:" + ret);
            failed = true;
        }
        int bootversion = 0;
        if (!failed) {
            bootversion = cmd.cmdGet();
            //dialogAppend("bootversion:"+ bootversion);
            tvAppend(tvRead, " bootversion:" + bootversion + "\n");
            if (bootversion < 20 || bootversion >= 100) {
                tvAppend(tvRead, " bootversion not good:" + bootversion + "\n");
                failed =true;
            }
        }

        if (!failed) {
            byte chip_id[]; // = new byte [4];
            chip_id = cmd.cmdGetID();
            tvAppend(tvRead, " chip id:" + toHexStr(chip_id, 2) + "\n");
        }

        if (!failed) {
            if (bootversion < 0x30) {
                tvAppend(tvRead,  "Erase 1\n");
                cmd.cmdEraseMemory();
            }
            else {
                tvAppend(tvRead,  "Erase 2\n");
                cmd.cmdExtendedEraseMemory();
            }
        }
        if (!failed) {
            cmd.drain();
            tvAppend(tvRead, "writeMemory" + "\n");
            ret = cmd.writeMemory(0x8000000, is);
            tvAppend(tvRead, "writeMemory finish" + "\n\n\n\n");
            if (ret == 1) {
                tvAppend(tvRead, "writeMemory success" + "\n\n\n\n");
            }
        }
        if (!failed) {
            cmd.cmdGo(0x8000000);
        }
        cmd.releaseChip();
    }


    Physicaloid.UploadCallBack mUploadCallback = new Physicaloid.UploadCallBack() {

        @Override
        public void onUploading(int value) {
            dialogAppend(getResources().getString(R.string.msg12)+value+" %");
        }

        @Override
        public void onPreUpload() {
            //Upload : Start
            tvAppend(tvRead, getResources().getString(R.string.msg14));
        }

        public void info(String value) {
            tvAppend(tvRead, value);
        }
        @Override
        public void onPostUpload(boolean success) {
            if(success) {
                //Upload : Successful
                tvAppend(tvRead, getResources().getString(R.string.msg16));
            } else {
                //Upload fail
                tvAppend(tvRead, getResources().getString(R.string.msg15));
            }

            alert.dismiss();
        }

        @Override
        //Cancel uploading
        public void onCancel() {
            tvAppend(tvRead, getResources().getString(R.string.msg17));
        }

        @Override
        //Error  :
        public void onError(UploadErrors err) {
               tvAppend(tvRead, getResources().getString(R.string.msg18)+err.toString()+"\n");
        }

    };

    UploadSTM32CallBack mUploadSTM32Callback = new UploadSTM32CallBack() {

        @Override
        public void onUploading(int value) {

            dialogAppend(getResources().getString(R.string.msg12)+value+" %");
        }

        @Override
        public void onInfo(String value) {
            tvAppend(tvRead, value);
        }

        @Override
        public void onPreUpload() {
            //Upload : Start
            tvAppend(tvRead, getResources().getString(R.string.msg14));

        }

        public void info(String value) {
            tvAppend(tvRead, value);
        }
        @Override
        public void onPostUpload(boolean success) {
            if(success) {
                //Upload : Successful
                tvAppend(tvRead, getResources().getString(R.string.msg16));
            } else {
                //Upload fail
                tvAppend(tvRead, getResources().getString(R.string.msg15));
            }

            alert.dismiss();
        }

        @Override
        //Cancel uploading
        public void onCancel() {
            tvAppend(tvRead, getResources().getString(R.string.msg17));
        }

        @Override
        //Error  :
        public void onError(UploadSTM32Errors err) {
            tvAppend(tvRead, getResources().getString(R.string.msg18)+err.toString()+"\n");
        }

    };
    Handler mHandler = new Handler();
    private void tvAppend(TextView tv, CharSequence text) {
        final TextView ftv = tv;
        final CharSequence ftext = text;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                ftv.append(ftext);

            }
        });
    }
    private void dialogAppend(CharSequence text) {
        final CharSequence ftext = text;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                alert.setMessage(ftext);
            }
        });
    }

    private void close() {
        if(mPhysicaloid.close()) {

        }
    }

}