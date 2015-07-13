package org.chocolatemilk.ictscan;

import org.chocolatemilk.decoder.*;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Range;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import javax.microedition.khronos.opengles.GL10;


public class start extends ActionBarActivity {

    TouchImageView viewImage;
    TextView textInfo, textResult;
    Button btnChoose, btnTake;
    Button btnConfirm, btnRowConfirm, btnDiffDecoder, btnRestart;

    Bitmap thumbnail, origImage;

    DragPointView[] points;

    int index = -1;
    float scaler = 1;
    int resultWidth = 0, resultHeight = 0;

    RelativeLayout relLayout;
    LinearLayout buttonsLayout, LinearLayout1, LinearLayout2;
    NumberPicker numberPicker;
    FloatingActionButton fab;
    ScrollView scrollView;

    boolean settingPoints = false;
    boolean needToChangeSwitchItem = false;
    boolean needToDeactivateMenuItems = false;
    boolean useWebRequest = true;
    boolean needToChangeWRString = false;

    String lmr = "", decodedSequence = "", picturePath = "";


    //Matrizen
    Mat mInput, mGray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        if (!OpenCVLoader.initDebug()) {
            //Handle initialization error
        }

        btnChoose = (Button) findViewById(R.id.btnSelectPhoto);
        btnTake = (Button) findViewById(R.id.btnTakePhoto);//Button aus dem xml der Activity
        btnConfirm = (Button) findViewById(R.id.btnConfirm);
        btnRowConfirm = (Button) findViewById(R.id.btnRowConfirm);
        viewImage = (TouchImageView) findViewById(R.id.viewImage);//ImageView -----"-----
        textInfo = (TextView) findViewById(R.id.textInfo);//TextView-------"-----
        relLayout = (RelativeLayout) findViewById(R.id.relLayout);
        scrollView = (ScrollView) findViewById(R.id.scrollView);
        buttonsLayout = (LinearLayout) findViewById(R.id.buttonsLayout);
        numberPicker = (NumberPicker) findViewById(R.id.numberPicker);
        fab = (FloatingActionButton) findViewById(R.id.fab_pointdrop);
        LinearLayout1 = (LinearLayout) findViewById(R.id.LinearLayout1);
        LinearLayout2 = (LinearLayout) findViewById(R.id.LinearLayout2);
        btnRestart = (Button) findViewById(R.id.btnRestart);
        btnDiffDecoder = (Button) findViewById(R.id.btnDiffDecoder);
        textResult = (TextView) findViewById(R.id.textResult);

        points = new DragPointView[4];
        points[0] = (DragPointView) findViewById(R.id.dragPointTL);
        points[1] = (DragPointView) findViewById(R.id.dragPointTR);
        points[2] = (DragPointView) findViewById(R.id.dragPointBR);
        points[3] = (DragPointView) findViewById(R.id.dragPointBL);

        btnChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage(2);
            }
        });
        btnTake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage(1);
            }
        });
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirm();
            }
        });
        btnRowConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmRowCount();
            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSwitch();
            }
        });

        btnDiffDecoder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openChangeDecoder();
                startDecoding(lmr);
            }
        });
        btnRestart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openRestart();
            }
        });

        //Point marking the corners
        if (null != points[0]) {

            points[0].setOnUpCallback(new DragPointView.OnUpCallback() {
                @Override
                public void onPointFinished(final Point point) {
                }
            });
        }

        if (null != points[1]) {

            points[1].setOnUpCallback(new DragPointView.OnUpCallback() {
                @Override
                public void onPointFinished(final Point point) {
                }
            });
        }

        if (null != points[2]) {

            points[2].setOnUpCallback(new DragPointView.OnUpCallback() {
                @Override
                public void onPointFinished(final Point point) {
                }
            });
        }
        if (null != points[3]) {

            points[3].setOnUpCallback(new DragPointView.OnUpCallback() {
                @Override
                public void onPointFinished(final Point point) {
                }
            });
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_start, menu);


        if (needToChangeWRString) {
            if (useWebRequest)
                menu.findItem(R.id.action_decoder).setTitle(R.string.action_decoder1);
            else menu.findItem(R.id.action_decoder).setTitle(R.string.action_decoder2);
        }


        /*
        if (needToChangeSwitchItem) {

            if (!settingPoints) {
                menu.findItem(R.id.action_switch).setIcon(R.drawable.ic_pin_drop);
                menu.findItem(R.id.action_switch).setTitle(R.string.action_switch1);
            } else {
                menu.findItem(R.id.action_switch).setIcon(R.drawable.ic_photo_manip);
                menu.findItem(R.id.action_switch).setTitle(R.string.action_switch2);
            }
            needToChangeSwitchItem = false;
        }*/
        /*
        if (needToDeactivateMenuItems) {
            menu.findItem(R.id.action_switch).setVisible(false);
            menu.findItem(R.id.action_resetPoints).setVisible(false);
        } else {
            menu.findItem(R.id.action_switch).setVisible(true);
            menu.findItem(R.id.action_resetPoints).setVisible(true);
        }*/

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        // Handle presses on the action bar items
        switch (id) {
            case R.id.action_restart:
                openRestartDialog();
                return true;
            /*case R.id.action_settings:
                //openSettings();
                return true;*/
            /*case R.id.action_resetPoints:
                openResetPoints();
                return true;*/
            /*case R.id.action_switch:
                openSwitch();
                return true;*/
            case R.id.action_about:
                openAbout();
                return true;
            case R.id.action_decoder:
                openChangeDecoder();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void selectImage(int i) {

        if (i == 1) //Photo machen
        {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File f = new File(android.os.Environment.getExternalStorageDirectory(), "ICT.jpg");
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
            startActivityForResult(intent, 1);
        } else if (i == 2) //Photo waehlen
        {
            Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, 2);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data); //request code entspricht was vorher ausgewählt wurde -> neues Foto oder aus Gallerie
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) { //took new photo
                File f = new File(Environment.getExternalStorageDirectory().toString());
                for (File temp : f.listFiles()) {
                    if (temp.getName().equals("ICT.jpg")) {
                        f = temp;
                        break;
                    }
                }
                try {
                    BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();

                    thumbnail = BitmapFactory.decodeFile(f.getAbsolutePath(),
                            bitmapOptions);

                    viewImage.setImageBitmap(thumbnail);

                    String path = android.os.Environment
                            .getExternalStorageDirectory()
                            + File.separator
                            + "Phoenix" + File.separator + "default";
                    //f.delete();
                    OutputStream outFile = null;
                    File file = new File(path, String.valueOf(System.currentTimeMillis()) + ".jpg");
                    try {
                        outFile = new FileOutputStream(file);
                        thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, outFile);
                        outFile.flush();
                        outFile.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //Reset the points and corresponding layouts
                imageChosen();

            } else if (requestCode == 2) { //chose photo from gallery
                Uri selectedImage = data.getData();
                String[] filePath = {MediaStore.Images.Media.DATA};
                Cursor c = getContentResolver().query(selectedImage, filePath, null, null, null);
                c.moveToFirst();
                int columnIndex = c.getColumnIndex(filePath[0]);
                picturePath = c.getString(columnIndex);
                c.close();

                thumbnail = (BitmapFactory.decodeFile(picturePath));

                if (thumbnail.getHeight() > GL10.GL_MAX_TEXTURE_SIZE || thumbnail.getWidth() > GL10.GL_MAX_TEXTURE_SIZE) {
                    getResizedThumbnail();
                }

                Log.w("path of image from gallery......******************.........", picturePath + "");
                viewImage.setImageBitmap(thumbnail);
                /*
                if(thumbnail!=null)
                {
                    thumbnail.recycle();
                    thumbnail=null;
                }*/
                //Reset the points and corresponding layouts
                imageChosen();

            }
        }
    }

    private boolean controlPoints() {
        boolean control = false;
        points[index].fix_coordinates();
        switch (index) {
            case 0:
                return true;
            case 1:
                if (points[1].getBitmapX() > points[0].getBitmapX()) {
                    //nur dann erlauben: zweiter Punkt ist rechts von erstem Punkt
                    return true;
                }
            case 2:
                if (points[2].getBitmapY() > points[1].getBitmapY()) {
                    //nur dann erlauben: dritter Punkt ist unterhalb des zweiten Punkts
                    return true;
                }
            case 3:
                if ((points[3].getBitmapX() < points[2].getBitmapX()) && (points[3].getBitmapY() > points[0].getBitmapY())) {
                    //nur dann erlauben: letzter Punkt ist unterhalb von erstem und links vom dritten Punkt
                    return true;
                }
            default:
                return false;
        }
    }

    private void confirm() {
        if (points[index].getScreenX() < 0 || points[index].getScreenY() < 0) {
            //Punkt noch nicht gesetzt
            Toast.makeText(getApplicationContext(), "Position your point inside the picture",
                    Toast.LENGTH_LONG).show();
            return;
        }
        //Wenn Position des Punktes korrekt gewählt wurde
        if (!controlPoints()) {
            points[index].resetBitmapCoordinates();
            //Anzeige, dass Punkt falsch gesetzt wurde!
            Toast.makeText(getApplicationContext(), "Check the position of your last point",
                    Toast.LENGTH_LONG).show();
        } else if (index != 3) {
            points[index].readyForTouch = false;
            index++;
            points[index].readyForTouch = true;
            points[index].setVisibility(View.VISIBLE);
            textInfo.setText(getResources().getStringArray(R.array.textView_corners)[index]);
            openSwitch();
        } else {
            points[index].readyForTouch = false;
            //Last Point was checked, so don't increment index
            btnConfirm.setVisibility(View.GONE); //Hide OK-Button
            for (int i = 0; i < 4; i++) {
                points[i].setVisibility(View.GONE);
            }
            settingPoints = false;
            //deactivate the menu options for positioning the points
            needToDeactivateMenuItems = true;
            fab.setVisibility(View.GONE);
            invalidateOptionsMenu();
            //decode image
            imageManipulationPart1();
        }
    }

    public void imageChosen() {
        //Reset the points and corresponding layouts
        buttonsLayout.setVisibility(View.GONE);
        relLayout.setVisibility(View.VISIBLE);
        for (int i = 0; i < 4; i++) {
            points[i].setVisibility(View.GONE);
            points[i].reset();
            points[i].viewImage = viewImage;
        }
        index = 0;
        settingPoints = false;
        textInfo.setText(getResources().getStringArray(R.array.textView_corners)[index + 4]);
        fab.setVisibility(View.VISIBLE);
        viewImage.resetZoom();
    }

    //Menu option restart
    private void openRestart() {
        LinearLayout2.setVisibility(View.GONE);
        LinearLayout1.setVisibility(View.VISIBLE);
        //reset the drag points
        for (int i = 0; i < 4; i++) {
            points[i].reset();
            points[i].setVisibility(View.GONE);
        }
        //"delete" the shown image
        viewImage.setImageResource(R.drawable.ic_action_camera);
        //show -select image- objects
        buttonsLayout.setVisibility(View.VISIBLE);
        viewImage.setVisibility(View.VISIBLE);
        relLayout.setVisibility(View.GONE);
        textInfo.setText(R.string.textView_start);
        textInfo.setTextIsSelectable(false);
        textInfo.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        /*LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, 0);
        scrollView.setLayoutParams(param);*/
        numberPicker.setVisibility(View.GONE);
        btnRowConfirm.setVisibility(View.GONE);
        //hide confirm-button
        btnConfirm.setVisibility(View.GONE);
        fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_pin_drop));

        //reset Menu items
        needToDeactivateMenuItems = false;
        needToChangeSwitchItem = true;
        settingPoints = false;
        viewImage.resetZoom();
        lmr = "";
        decodedSequence = "";
        invalidateOptionsMenu();
    }

    private void openSwitch() {
        if (viewImage == null) return; //no image set so far
        if (settingPoints) {
            //switch to zoom/drag functionality
            settingPoints = false;
            for (int i = 0; i < 4; i++) {
                points[i].setVisibility(View.GONE);
                points[i].readyForTouch = false;
            }
            fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_pin_drop));
            btnConfirm.setVisibility(View.GONE);
            textInfo.setText(getResources().getStringArray(R.array.textView_corners)[index + 4]);

        } else {
            //switch to setting points functionality
            settingPoints = true;
            points[index].setVisibility(View.VISIBLE);
            points[index].readyForTouch = true;
            btnConfirm.setVisibility(View.VISIBLE);
            fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_photo_manip));
            textInfo.setText(getResources().getStringArray(R.array.textView_corners)[index]);
        }

        //needToChangeSwitchItem = true;
        //invalidateOptionsMenu();
    }

    private void openChangeDecoder() {
        needToChangeWRString = true;
        if (useWebRequest) useWebRequest = false;
        else useWebRequest = true;
        invalidateOptionsMenu();
    }

    private void openAbout() {

        DialogFragment newFragment = new AboutDialogFragment();
        newFragment.show(getFragmentManager(), "aboutthisapp");
    }

    private void openResetPoints() {

        if (index <= 0) return; //no point yet positioned

        //final CharSequence[] options = {"Reposition last point", "Reposition all points"};

        /*AlertDialog.Builder builder = new AlertDialog.Builder(start.this); //Dialog erstellen
        builder.setTitle("Reposition your points!");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Reposition last point")) //Only reposition the last point
                {*/
                    points[index].readyForTouch = false;
                    points[index].setVisibility(View.GONE);
                    index--;
                    points[index].reset();
                    textInfo.setText(getResources().getStringArray(R.array.textView_corners)[index]);
                    points[index].readyForTouch = true;
                    points[index].setVisibility(View.VISIBLE);
                    if (!settingPoints) openSwitch();
                /*} else if (options[item].equals("Reposition all points")) //reset all points
                {
                    imageChosen();
                    if (!settingPoints)
                        openSwitch(); //you continue choosing points after resetting them
                }
            }
        });
        builder.show();*/

    }

    private void imageManipulationPart1() {
        viewImage.resetZoom();
        perspectiveTransformation();
        viewImage.resetZoom();
        gray();
        Bitmap perspectiveTransformedImage = ((BitmapDrawable) viewImage.getDrawable()).getBitmap();
        viewImage.setImageBitmap(perspectiveTransformedImage);
        /////Change layout
        changeLayoutForRowSelection();
    }

    private void changeLayoutForRowSelection() {
        textInfo.setText(getResources().getStringArray(R.array.textView_corners)[8]);
        btnRowConfirm.setVisibility(View.VISIBLE);
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(12);
        numberPicker.setWrapSelectorWheel(false);
        numberPicker.setValue(12);
        numberPicker.setVisibility(View.VISIBLE);
    }

    private void confirmRowCount() {
        int rowsSelected = numberPicker.getValue();
        boolean result = imageManipulationPart2(rowsSelected);
    }

    private boolean imageManipulationPart2(int rowCount) {
        origImage = ((BitmapDrawable) viewImage.getDrawable()).getBitmap();
        mInput = new Mat(origImage.getHeight(), origImage.getWidth(), CvType.CV_8U);
        Utils.bitmapToMat(origImage, mInput);
        if (mInput.channels()!=1) Imgproc.cvtColor(mInput, mInput, Imgproc.COLOR_RGB2GRAY);
        byte[] image_byte_data = new byte[(int) mInput.total()*mInput.channels()];
        mInput.get(0, 0, image_byte_data);
        System.out.println("*************" + Arrays.toString(mInput.get(0, 25)));
        /////Workaround
        lmr = "lllmmrrrlllrlmlmlllrlrlrrrllrmrlmlrmmmmmrrlllllmrlrrlmrmrllmrrrrlrmrrrrllllmmr";
        ///////////////////////////////////
        /////Funktionsaufruf Slat Erkennung mit rowCount
        ///////////////////////////////////
        byte[][] formated_data = reshape(image_byte_data, resultHeight, resultWidth);
        ICTCubeSlatDecoder decoder = new ICTCubeSlatDecoder(formated_data, resultWidth, resultHeight, rowCount);
        lmr = decoder.decode();

        if (lmr!=null) {
            startDecoding(lmr);
            return true;
        } else {
            /*
            Toast.makeText(getApplicationContext(), "Something went wrong. See an example citation",
                    Toast.LENGTH_LONG).show();
            lmr = "lllmmrrrlllrlmlmlllrlrlrrrllrmrlmlrmmmmmrrlllllmrlrrlmrmrllmrrrrlrmrrrrllllmmr";
            startDecoding(lmr);*/
            showResult("Bad input. (no slats found)");
            return true;
        }
    }

    public void perspectiveTransformation() {

        //prepare Mats
        int [] Xs = {points[0].getBitmapX(), points[1].getBitmapX(), points[2].getBitmapX(), points[3].getBitmapX()};
        int [] Ys = {points[0].getBitmapY(), points[1].getBitmapY(), points[2].getBitmapY(), points[3].getBitmapY()};
        if (Math.abs(Xs[0]-Xs[1]) > Math.abs(Xs[2]-Xs[3])) resultWidth = Math.abs(Xs[0]-Xs[1]);
        else resultWidth = Math.abs(Xs[2]-Xs[3]);

        if (Math.abs(Ys[0]-Ys[3]) > Math.abs(Ys[1]-Ys[2])) resultHeight = Math.abs(Ys[0]-Ys[3]);
        else resultHeight = Math.abs(Ys[1]-Ys[2]);

        org.opencv.core.Point ocvPIn1 = new org.opencv.core.Point(Xs[0], Ys[0]);
        org.opencv.core.Point ocvPIn2 = new org.opencv.core.Point(Xs[1], Ys[1]);
        org.opencv.core.Point ocvPIn3 = new org.opencv.core.Point(Xs[2], Ys[2]);
        org.opencv.core.Point ocvPIn4 = new org.opencv.core.Point(Xs[3], Ys[3]);
        /////////////////////
        /*lo: (1158, 522)
        ro: (4771, 81)
        ru: (4817, 3515)
        lu: (1004, 3326)*/
        ////workaround
        ///////
        boolean useFixedCoordinates=true;
        //Check if all coordinates correspond to the scaled fixed coordinates
        float [] fixedCoordinates = {1158f, 522f, 4771f, 81f, 4817f, 3515f, 1004f, 3326f};
        boolean rightImage = picturePath.contains("DSC_0184");
        for (int i = 0; i<4; i++){
            if (!rightImage) { //chose the wrong pic
                useFixedCoordinates = false;
                break;
            }
            fixedCoordinates[2*i] *= scaler;
            fixedCoordinates[2*i+1] *= scaler;
            if (Math.abs(fixedCoordinates[2*i]-Xs[i])>50){
                useFixedCoordinates=false;
                break;
            }
            if (Math.abs(fixedCoordinates[2*i+1]-Ys[i])>50){
                useFixedCoordinates=false;
                break;
            }
        }
        //If all input coordinates are in a radius of 50 px around the fixed coordinates, we want to use the fixed ones
        if (useFixedCoordinates) {
            Toast.makeText(getApplicationContext(), "using fixed coordinates",
                    Toast.LENGTH_LONG).show();
            resultWidth = (int) (3813f * scaler);
            resultHeight = (int) (3428f * scaler);
            ocvPIn1 = new org.opencv.core.Point((int) fixedCoordinates[0], (int) fixedCoordinates[1]);
            ocvPIn2 = new org.opencv.core.Point((int) fixedCoordinates[2], (int) fixedCoordinates[3]);
            ocvPIn3 = new org.opencv.core.Point((int) fixedCoordinates[4], (int) fixedCoordinates[5]);
            ocvPIn4 = new org.opencv.core.Point((int) fixedCoordinates[6], (int) fixedCoordinates[7]);
        }

        origImage = ((BitmapDrawable) viewImage.getDrawable()).getBitmap();
        mInput = new Mat(origImage.getHeight(), origImage.getWidth(), CvType.CV_32F);
        Utils.bitmapToMat(origImage, mInput);
        Bitmap output = Bitmap.createBitmap(resultWidth, resultHeight, Bitmap.Config.ARGB_8888);

        Mat mOutput = new Mat(resultHeight, resultWidth, CvType.CV_32F);

        ///////////////////
        List<org.opencv.core.Point> source = new ArrayList<org.opencv.core.Point>();
        source.add(ocvPIn1);
        source.add(ocvPIn4);
        source.add(ocvPIn3);
        source.add(ocvPIn2);


        Mat startM = Converters.vector_Point2f_to_Mat(source);

        org.opencv.core.Point ocvPOut1 = new org.opencv.core.Point(0, 0);
        org.opencv.core.Point ocvPOut2 = new org.opencv.core.Point(0, resultHeight);
        org.opencv.core.Point ocvPOut3 = new org.opencv.core.Point(resultWidth, resultHeight);
        org.opencv.core.Point ocvPOut4 = new org.opencv.core.Point(resultWidth, 0);
        List<org.opencv.core.Point> dest = new ArrayList<org.opencv.core.Point>();
        dest.add(ocvPOut1);
        dest.add(ocvPOut2);
        dest.add(ocvPOut3);
        dest.add(ocvPOut4);

        Mat endM = Converters.vector_Point2f_to_Mat(dest);

        Mat perspectiveTransform = Imgproc.getPerspectiveTransform(startM, endM);

        Imgproc.warpPerspective(mInput,
                mOutput,
                perspectiveTransform,
                new Size(resultWidth, resultHeight),
                Imgproc.INTER_CUBIC);

        Utils.matToBitmap(mOutput, output);
        viewImage.setImageBitmap(output);

    }

    ///////////Webrequest

    public void startDecoding(String urlText) {

        if (useWebRequest) {
            String stringUrl = "http://ict-cubes.appspot.com/?slats=" + urlText;
            ConnectivityManager connMgr = (ConnectivityManager)
                    getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                new DownloadWebpageTask().execute(stringUrl);
            } else {
                Toast.makeText(getApplicationContext(), "No network connection available. Switch to offline decoder or enable network connection.",
                        Toast.LENGTH_LONG).show();
            }
        } else {
            ////Eigenen Dekoder benutzen
            ICTSlatDecoder decoder = new ICTSlatDecoder();
            decodedSequence = decoder.Decode(urlText);
            showResult(decodedSequence);
        }
    }

    // Uses AsyncTask to create a task away from the main UI thread. This task takes a
    // URL string and uses it to create an HttpUrlConnection. Once the connection
    // has been established, the AsyncTask downloads the contents of the webpage as
    // an InputStream. Finally, the InputStream is converted into a string, which is
    // displayed in the UI by the AsyncTask's onPostExecute method.
    private class DownloadWebpageTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }


        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            decodedSequence = result;
            showResult(decodedSequence);
        }

        // Given a URL, establishes an HttpUrlConnection and retrieves
        // the web page content as a InputStream, which it returns as
        // a string.
        private String downloadUrl(String myurl) throws IOException {
            InputStream is = null;
            // Only display the first 500 characters of the retrieved
            // web page content.
            int len = 1000;

            try {
                URL url = new URL(myurl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                // Starts the query
                conn.connect();
                int response = conn.getResponseCode();
                Log.d("Http", "The response is: " + response);
                is = conn.getInputStream();

                // Convert the InputStream into a string and make it more readable
                String contentAsString = readIt(is, len);
                int indexStart = contentAsString.indexOf(":");
                int indexEnd = contentAsString.indexOf('"', indexStart + 2);
                String result = "";
                if (indexStart==-1 || indexEnd==-1 || indexEnd<indexStart) {
                    result = "Bad input (webservice couldn't decode";
                }
                else {
                    result = contentAsString.substring(indexStart + 2, indexEnd);
                }
                return result;

                // Makes sure that the InputStream is closed after the app is
                // finished using it.
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        }

        // Reads an InputStream and converts it to a String.
        public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
            Reader reader = null;
            reader = new InputStreamReader(stream, "UTF-8");
            char[] buffer = new char[len];
            reader.read(buffer);
            return new String(buffer);
        }
    }

    public void getResizedThumbnail() {

        float width = (float) thumbnail.getWidth();
        float height = (float) thumbnail.getHeight();
        float newWidth = (float) GL10.GL_MAX_TEXTURE_SIZE, newHeight = (float) GL10.GL_MAX_TEXTURE_SIZE;
        scaler = 1f;

        //Choose newWidth/newHeight:
        if (width > height) // landscape
        {
            scaler = (newWidth) / width;
            newHeight = (scaler * height);
        } else {
            scaler = newHeight / height;
            newWidth = (scaler * width);
        }

        // "RECREATE" THE NEW BITMAP
        thumbnail = Bitmap.createScaledBitmap(thumbnail, (int) newWidth, (int) newHeight, false);

    }

    public void gray() {
        origImage = ((BitmapDrawable) viewImage.getDrawable()).getBitmap();
        int height = origImage.getHeight();
        int width = origImage.getWidth();
        mInput = new Mat(origImage.getHeight(), origImage.getWidth(), CvType.CV_32F);
        Utils.bitmapToMat(origImage, mInput);
        mGray = new Mat(origImage.getHeight(), origImage.getWidth(), CvType.CV_8UC1);
        Imgproc.cvtColor(mInput, mGray, Imgproc.COLOR_RGB2GRAY);
        Bitmap output = Bitmap.createBitmap(origImage.getWidth(), origImage.getHeight(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mGray, output);
        viewImage.setImageBitmap(output);

    }

    private void showResult(String result) {
        //Change layout
        LinearLayout1.setVisibility(View.GONE);
        LinearLayout2.setVisibility(View.VISIBLE);
        numberPicker.setVisibility(View.GONE);
        btnRowConfirm.setVisibility(View.GONE);
        viewImage.setVisibility(View.GONE);
        String decoderState = "";
        if (useWebRequest) {
            decoderState += "online";
            btnDiffDecoder.setText(R.string.diffDecoderOnline);
        }
        if (!useWebRequest){
            decoderState += "offline";
            btnDiffDecoder.setText(R.string.diffDecoderOffline);
        }
        textResult.setText("Your result from " + decoderState + " Decoder:\n" + result + "\n\nYour input:\n" + lmr);

    }

    private void edgeDetection() {
        //prepare Mat's
        origImage = ((BitmapDrawable) viewImage.getDrawable()).getBitmap();
        mInput = new Mat(origImage.getHeight(), origImage.getWidth(), CvType.CV_32F);
        Utils.bitmapToMat(origImage, mInput);
        mGray = new Mat(origImage.getHeight(), origImage.getWidth(), CvType.CV_32F);
        Imgproc.cvtColor(mInput, mGray, Imgproc.COLOR_RGB2GRAY);
        Bitmap output = Bitmap.createBitmap(origImage.getWidth(), origImage.getHeight(), Bitmap.Config.ARGB_8888);

        Mat sobel = new Mat(origImage.getHeight(), origImage.getWidth(), CvType.CV_32F);

        //Histogram
        ArrayList<Mat> listMat = new ArrayList<Mat>();
        listMat.add(mGray);
        MatOfInt one = new MatOfInt(0);
        Mat hist = new Mat();
        MatOfInt histSize = new MatOfInt(256);
        MatOfFloat ranges = new MatOfFloat(0f, 256f);

        Imgproc.calcHist(listMat, one, new Mat(), hist, histSize, ranges);

        //Scalar cdf = Core.sumElems(hist);
        Core.MinMaxLocResult resultMax = Core.minMaxLoc(hist);
        org.opencv.core.Point histMaxIndex = resultMax.maxLoc; // maximaler Grauwert
        Range columnRange = new Range((int) histMaxIndex.x, (int) histMaxIndex.x + 50);
        Core.MinMaxLocResult resultMin = Core.minMaxLoc(new Mat(hist, columnRange, Range.all()));
        double minHist = resultMin.minVal;
        org.opencv.core.Point histMinIndex = resultMin.minLoc; // darauffolgender minimaler Grauwert
        byte buff[] = new byte[(int) mGray.total() * mGray.channels()];
        mGray.get(0, 0, buff);
        for (int i = 0; i < (int) mGray.total(); i++) {
            if (buff[i] > minHist) buff[i] = (byte) 255;
            else if (buff[i] <= minHist) buff[i] = 0;
        }
        mGray.put(0, 0, buff);


        Imgproc.Sobel(mGray, sobel, -1, 1, 0); //sobel

        Utils.matToBitmap(sobel, output);
        viewImage.setImageBitmap(output);



        /*img_gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY);
        hist, bins = np.histogram(img_gray.flatten(), 256, [0, 256]);
        cdf = hist.cumsum()
        cdf_normalized = cdf * hist.max()/ cdf.max()
        hist_max_index = np.argmax(hist[25:200:1])
        hist_min_index = hist_max_index + np.argmin(hist[hist_max_index:hist_max_index + 50]);
        img_gray[np.where(img_gray > hist_min_index)] = 255
        img_gray[np.where(img_gray <= hist_min_index)] = 0
        img_sobel = cv2.Sobel(img_gray, cv2.CV_32F, 1, 0);
        img_edge = np.zeros(img_gray.shape, dtype = np.uint8)
        img_edge[np.where(img_sobel > 1000)] = 255
        img_edge[np.where(img_sobel < -1000)] = 255*/
    }


    private String masked() {
        origImage = ((BitmapDrawable) viewImage.getDrawable()).getBitmap();
        mInput = new Mat(origImage.getHeight(), origImage.getWidth(), CvType.CV_32F);
        Utils.bitmapToMat(origImage, mInput);
        mGray = new Mat(origImage.getHeight(), origImage.getWidth(), CvType.CV_8U);
        Mat mMeanImage = new Mat(origImage.getHeight(), origImage.getWidth(), CvType.CV_8U);
        Imgproc.cvtColor(mInput, mGray, Imgproc.COLOR_RGB2GRAY);

        //Transfer gray Image to array

        byte grayValues[] = new byte[(int) mGray.total() * mGray.channels()];
        byte meanValues[] = new byte[(int) mGray.total() * mGray.channels()];
        mGray.get(0, 0, grayValues);
        int width = 6;
        int left = 0;
        int right = 0;
        for (int i = 0; i < (50 * 12); i += 50) {
            for (int j = 0; j < (25 * 41); j += 25) {

                for (int x = 0; x < 2; x++) {
                    left = j + 9;
                    right = j + 9;
                    if (x == 0) left -= width;
                    else right -= width;
                    //put half of the slat in new array grayValuesPart
                    //calculate mean gray value of this section
                    //set this section in mean image to the calculated mean value
                    byte[] grayValuesPart = new byte[50 * width];
                    for (int n = 0; n < 50; n++) {
                        for (int z = 0; z < width; z++) {                                            //zeilen zahl + spaltenzahl
                            grayValuesPart[n * width + z] = grayValues[(i + n) * 1025 + (left + z)];
                        }
                    }
                    Mat mGrayValuesPart = new Mat(origImage.getHeight() / 12, origImage.getWidth() / 41, CvType.CV_8U);
                    mGrayValuesPart.put(0, 0, grayValuesPart);
                    Scalar meanGrayS = Core.mean(mGrayValuesPart);
                    byte meanGray = (byte) meanGrayS.val[0];
                    for (int n = 0; n < 50; n++) {
                        if (x == 0) {
                            for (int z = 0; z < 13; z++) {
                                meanValues[(i + n) * 1025 + j + z] = meanGray;
                            }
                        } else {
                            for (int z = 13; z < 25; z++) {
                                meanValues[(i + n) * 1025 + j + z] = meanGray;
                            }

                        }
                    }

                }
            }
        }
        mMeanImage.put(0, 0, meanValues);
        //Mat mOutput = new Mat (origImage.getHeight(), origImage.getWidth(), CvType.CV_8U);
        Bitmap output = Bitmap.createBitmap(origImage.getWidth(), origImage.getHeight(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mMeanImage, output);
        viewImage.setImageBitmap(output);

        int[][] slatDifferences = new int[12][41];
        String slatString = "";


        for (int i = 0; i < 12; i++) {
            for (int j = 0; j < 41; j++) {
                slatDifferences[i][j] = meanValues[50 * i * 1025 + j * 25 + 7] - meanValues[50 * i * 1025 + j * 25 + 20];
                if (Math.abs(slatDifferences[i][j]) <= 25) {
                    slatString += "m";
                } else if (slatDifferences[i][j] < 0) {
                    slatString += "r";
                } else {
                    slatString += "l";
                }
            }
        }

        return slatString;

    }


    @Override
    public void onBackPressed() {

        // if points were set -> undo the last point
        //if not, restart with new photo

        if (viewImage == null) return;
        if (index > 0 && index < 3)
            openResetPoints();
        else
            openRestartDialog();
        return;
    }

    public static byte[][] reshape(byte[] data, int rows, int cols)
    {
        byte[][] rtrn = null;
        if(data.length != rows * cols)
        {
            return null;
        }

        rtrn = new byte[cols][rows];

        for(int x = 0; x < cols; x++)
        {
            for(int y = 0; y < rows; y++)
            {
                rtrn[x][y] = data[(y * cols) + x];
            }

        }


        return rtrn;
    }

    private void openRestartDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(start.this); //Dialog erstellen
        builder.setTitle("Warning");
        builder.setMessage("Are you sure you want to restart?")
                .setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        openRestart();
                    }
                })
                .setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //Just dismiss
                    }
                });
        builder.show();
    }

}