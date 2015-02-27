package work.ma.cardboardtest;


import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.google.vrtoolkit.cardboard.CardboardActivity;
import com.google.vrtoolkit.cardboard.CardboardView;
import com.google.vrtoolkit.cardboard.Eye;
import com.google.vrtoolkit.cardboard.HeadTransform;
import com.google.vrtoolkit.cardboard.Viewport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import com.easibeacon.protocol.IBeacon;
import com.easibeacon.protocol.IBeaconListener;
import com.easibeacon.protocol.IBeaconProtocol;
import com.easibeacon.protocol.Utils;

import javax.microedition.khronos.egl.EGLConfig;


public class MainActivity extends CardboardActivity implements CardboardView.StereoRenderer, IBeaconListener {

    private static final String TAG = "MAwork";

    private static final float Z_NEAR = 0.1f;
    private static final float Z_FAR = 100.0f;

    private static final float CAMERA_Z = 0.01f;
    private static final float TIME_DELTA = 0.3f;

    private static final float YAW_LIMIT = 0.12f;
    private static final float PITCH_LIMIT = 0.12f;

    private static final int COORDS_PER_VERTEX = 3;

    private static final WorldLayoutData DATA = new WorldLayoutData();

    // We keep the light always position just above the user.
    private static final float[] LIGHT_POS_IN_WORLD_SPACE = new float[] { 0.0f, 2.0f, 0.0f, 1.0f };

    private final float[] mLightPosInEyeSpace = new float[4];

    private FloatBuffer mFloorVertices;
    private FloatBuffer mFloorColors;
    private FloatBuffer mFloorNormals;

    private FloatBuffer mCubeVertices;
    private FloatBuffer mCubeColors;
    private FloatBuffer mCubeFoundColors;
    private FloatBuffer mCubeNormals;

    private int mCubeProgram;
    private int mFloorProgram;

    private int mCubePositionParam;
    private int mCubeNormalParam;
    private int mCubeColorParam;
    private int mCubeModelParam;
    private int mCubeModelViewParam;
    private int mCubeModelViewProjectionParam;
    private int mCubeLightPosParam;

    private int mFloorPositionParam;
    private int mFloorNormalParam;
    private int mFloorColorParam;
    private int mFloorModelParam;
    private int mFloorModelViewParam;
    private int mFloorModelViewProjectionParam;
    private int mFloorLightPosParam;

    private float[] mModelCube;
    private float[] mCamera;
    private float[] mView;
    private float[] mHeadView;
    private float[] mModelViewProjection;
    private float[] mModelView;
    private float[] mModelFloor;

    private int mScore = 0;
    private float mObjectDistance = 12f;
    private float mFloorDepth = 20f;

    private Vibrator mVibrator;
    private CardboardOverlayView mOverlayView;

    private static final int REQUEST_BLUETOOTH_ENABLE = 1;

    private static ArrayList<IBeacon> _beacons;
    private ArrayAdapter<IBeacon> _beaconAdapter;
    private static IBeaconProtocol _ibp;

    static final int PICK_CONTENT_REQUEST = 5;
    public List<String> data = new ArrayList<String>();

    public ArrayList<Models> bicons = new ArrayList<Models>();

    public Database db;


    @Override
    public void onCreate(Bundle savedInstaceState){
        super.onCreate(savedInstaceState);

        setContentView(R.layout.common_ui);
        CardboardView cardboardView = (CardboardView) findViewById(R.id.cardboard_view);

        cardboardView.setRenderer(this);
        setCardboardView(cardboardView);

        mModelCube = new float[16];
        mCamera = new float[16];
        mView = new float[16];
        mModelViewProjection = new float[16];
        mModelView = new float[16];
        mModelFloor = new float[16];
        mHeadView = new float[16];
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        mOverlayView = (CardboardOverlayView) findViewById(R.id.overlay);
        mOverlayView.show3DToast("Pull the magnet when you find an object.");

        if(_beacons == null)
            _beacons = new ArrayList<IBeacon>();
        //TODO edit the _beaconAdapter so that it will display the item
        //_beaconAdapter = new ArrayAdapter<IBeacon>();

        db = new Database(this);

        _ibp = IBeaconProtocol.getInstance(this);
        scanBeacons();
        _ibp.setListener(this);
    }

    @Override
    public void onNewFrame(HeadTransform headTransform) {
        // Build the Model part of the ModelView matrix.
        Matrix.rotateM(mModelCube, 0, TIME_DELTA, 0.5f, 0.5f, 1.0f);

        // Build the camera matrix and apply it to the ModelView.
        Matrix.setLookAtM(mCamera, 0, 0.0f, 0.0f, CAMERA_Z, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);

        headTransform.getHeadView(mHeadView, 0);

        checkGLError("onReadyToDraw");
    }

    /**
     * Draws a frame for an eye.
     *
     * @param eye The eye to render. Includes all required transformations.
     */
    @Override
    public void onDrawEye(Eye eye) {

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // eyes to the camera
        Matrix.multiplyMM(mView, 0, eye.getEyeView(), 0, mCamera, 0);

        // light position
        Matrix.multiplyMV(mLightPosInEyeSpace, 0, mView, 0, LIGHT_POS_IN_WORLD_SPACE, 0);
        // Build the ModelView and ModelViewProjection matrices
        // for calculating cube position and light.
        float[] perspective = eye.getPerspective(Z_NEAR, Z_FAR);
        Matrix.multiplyMM(mModelView, 0, mView, 0, mModelCube, 0);
        Matrix.multiplyMM(mModelViewProjection, 0, perspective, 0, mModelView, 0);
        //drawCube();

        // Set mModelView for the floor, so we draw floor in the correct location
        Matrix.multiplyMM(mModelView, 0, mView, 0, mModelFloor, 0);
        Matrix.multiplyMM(mModelViewProjection, 0, perspective, 0,
                mModelView, 0);
        drawFloor();
    }

    @Override
    public void onFinishFrame(Viewport viewport) {

    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        Log.i(TAG, "onSurfaceChanged: w="+width+" h="+height);

    }

    /**
     * Creates the buffers we use to store information about the 3D world.
     *
     * OpenGL doesn't use Java arrays, but rather needs data in a format it can understand.
     * Hence we use ByteBuffers.
     *
     * @param config The EGL configuration used when creating the surface.
     */
    @Override
    public void onSurfaceCreated(EGLConfig config) {
        Log.i(TAG, "onSurfaceCreated");
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 0.5f); // Dark background so text shows up well.

        ByteBuffer bbVertices = ByteBuffer.allocateDirect(DATA.CUBE_COORDS.length * 4);
        bbVertices.order(ByteOrder.nativeOrder());
        mCubeVertices = bbVertices.asFloatBuffer();
        mCubeVertices.put(DATA.CUBE_COORDS);
        mCubeVertices.position(0);

        ByteBuffer bbColors = ByteBuffer.allocateDirect(DATA.CUBE_COLORS.length * 4);
        bbColors.order(ByteOrder.nativeOrder());
        mCubeColors = bbColors.asFloatBuffer();
        mCubeColors.put(DATA.CUBE_COLORS);
        mCubeColors.position(0);

        ByteBuffer bbFoundColors = ByteBuffer.allocateDirect(DATA.CUBE_FOUND_COLORS.length * 4);
        bbFoundColors.order(ByteOrder.nativeOrder());
        mCubeFoundColors = bbFoundColors.asFloatBuffer();
        mCubeFoundColors.put(DATA.CUBE_FOUND_COLORS);
        mCubeFoundColors.position(0);

        ByteBuffer bbNormals = ByteBuffer.allocateDirect(DATA.CUBE_NORMALS.length * 4);
        bbNormals.order(ByteOrder.nativeOrder());
        mCubeNormals = bbNormals.asFloatBuffer();
        mCubeNormals.put(DATA.CUBE_NORMALS);
        mCubeNormals.position(0);

        // make a floor
        ByteBuffer bbFloorVertices = ByteBuffer.allocateDirect(DATA.FLOOR_COORDS.length * 4);
        bbFloorVertices.order(ByteOrder.nativeOrder());
        mFloorVertices = bbFloorVertices.asFloatBuffer();
        mFloorVertices.put(DATA.FLOOR_COORDS);
        mFloorVertices.position(0);

        ByteBuffer bbFloorNormals = ByteBuffer.allocateDirect(DATA.FLOOR_NORMALS.length * 4);
        bbFloorNormals.order(ByteOrder.nativeOrder());
        mFloorNormals = bbFloorNormals.asFloatBuffer();
        mFloorNormals.put(DATA.FLOOR_NORMALS);
        mFloorNormals.position(0);

        ByteBuffer bbFloorColors = ByteBuffer.allocateDirect(DATA.FLOOR_COLORS.length * 4);
        bbFloorColors.order(ByteOrder.nativeOrder());
        mFloorColors = bbFloorColors.asFloatBuffer();
        mFloorColors.put(DATA.FLOOR_COLORS);
        mFloorColors.position(0);

        int vertexShader = loadGLShader(GLES20.GL_VERTEX_SHADER, R.raw.light_vertex);
        int gridShader = loadGLShader(GLES20.GL_FRAGMENT_SHADER, R.raw.grid_fragment);
        int passthroughShader = loadGLShader(GLES20.GL_FRAGMENT_SHADER, R.raw.passthrough_fragment);

        mCubeProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mCubeProgram, vertexShader);
        GLES20.glAttachShader(mCubeProgram, passthroughShader);
        GLES20.glLinkProgram(mCubeProgram);
        GLES20.glUseProgram(mCubeProgram);

        checkGLError("Cube program");

        mCubePositionParam = GLES20.glGetAttribLocation(mCubeProgram, "a_Position");
        mCubeNormalParam = GLES20.glGetAttribLocation(mCubeProgram, "a_Normal");
        mCubeColorParam = GLES20.glGetAttribLocation(mCubeProgram, "a_Color");

        mCubeModelParam = GLES20.glGetUniformLocation(mCubeProgram, "u_Model");
        mCubeModelViewParam = GLES20.glGetUniformLocation(mCubeProgram, "u_MVMatrix");
        mCubeModelViewProjectionParam = GLES20.glGetUniformLocation(mCubeProgram, "u_MVP");
        mCubeLightPosParam = GLES20.glGetUniformLocation(mCubeProgram, "u_LightPos");

        GLES20.glEnableVertexAttribArray(mCubePositionParam);
        GLES20.glEnableVertexAttribArray(mCubeNormalParam);
        GLES20.glEnableVertexAttribArray(mCubeColorParam);

        checkGLError("Cube program params");

        mFloorProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mFloorProgram, vertexShader);
        GLES20.glAttachShader(mFloorProgram, gridShader);
        GLES20.glLinkProgram(mFloorProgram);
        GLES20.glUseProgram(mFloorProgram);

        checkGLError("Floor program");

        mFloorModelParam = GLES20.glGetUniformLocation(mFloorProgram, "u_Model");
        mFloorModelViewParam = GLES20.glGetUniformLocation(mFloorProgram, "u_MVMatrix");
        mFloorModelViewProjectionParam = GLES20.glGetUniformLocation(mFloorProgram, "u_MVP");
        mFloorLightPosParam = GLES20.glGetUniformLocation(mFloorProgram, "u_LightPos");

        mFloorPositionParam = GLES20.glGetAttribLocation(mFloorProgram, "a_Position");
        mFloorNormalParam = GLES20.glGetAttribLocation(mFloorProgram, "a_Normal");
        mFloorColorParam = GLES20.glGetAttribLocation(mFloorProgram, "a_Color");

        GLES20.glEnableVertexAttribArray(mFloorPositionParam);
        GLES20.glEnableVertexAttribArray(mFloorNormalParam);
        GLES20.glEnableVertexAttribArray(mFloorColorParam);

        checkGLError("Floor program params");

        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        // Object first appears directly in front of user.
        Matrix.setIdentityM(mModelCube, 0);
        Matrix.translateM(mModelCube, 0, 0, 0, -mObjectDistance);

        Matrix.setIdentityM(mModelFloor, 0);
        Matrix.translateM(mModelFloor, 0, 0, -mFloorDepth, 0); // Floor appears below user.

        checkGLError("onSurfaceCreated");
    }

    @Override
    public void onRendererShutdown() {
        Log.i(TAG, "Render Shutdown");
    }

    @Override
    public void onCardboardTrigger(){
        Log.i(TAG, "Cardboard trigger");

        if(isLookingAtObject()){
            mOverlayView.show3DToast("Found it! Look around for another one.\nScore = ");
        }else{
            mOverlayView.show3DToast("Look around to find the object!");
        }
        mVibrator.vibrate(50);
    }
    /**
     * Converts a raw text file into a string.
     *
     * @param resId The resource ID of the raw text file about to be turned into a shader.
     * @return The context of the text file, or null in case of error.
     */
    private String readRawTextFile(int resId) {
        InputStream inputStream = getResources().openRawResource(resId);
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Converts a raw text file, saved as a resource, into an OpenGL ES shader.
     *
     * @param type The type of shader we will be creating.
     * @param resId The resource ID of the raw text file about to be turned into a shader.
     * @return The shader object handler.
     */
    private int loadGLShader(int type, int resId) {
        String code = readRawTextFile(resId);
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, code);
        GLES20.glCompileShader(shader);

        // Get the compilation status.
        final int[] compileStatus = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

        // If the compilation failed, delete the shader.
        if (compileStatus[0] == 0) {
            Log.e(TAG, "Error compiling shader: " + GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader);
            shader = 0;
        }

        if (shader == 0) {
            throw new RuntimeException("Error creating shader.");
        }

        return shader;
    }

    /**
     * Find a new random position for the object.
     *
     * We'll rotate it around the Y-axis so it's out of sight, and then up or down by a little bit.
     */
    public void hideObject(){
        float[] rotationMatrix = new float[16];
        float[] posVec = new float[4];

        float angleXZ = (float) Math.random() * 180 + 90;
        Matrix.setRotateM(rotationMatrix, 0, angleXZ, 0f,1f,0f);
        float oldObjectDistance = mObjectDistance;
        mObjectDistance = (float) Math.random() * 15 + 5;
        float objectScalingFactor = mObjectDistance / oldObjectDistance;
        Matrix.scaleM(rotationMatrix, 0 ,objectScalingFactor, objectScalingFactor, objectScalingFactor);
        Matrix.multiplyMV(posVec, 0 ,rotationMatrix, 0 ,mModelCube, 12);

        float angleY = (float)Math.random() * 80 - 40;
        angleY = (float) Math.toRadians(angleY);
        float newY = (float) Math.tan(angleY);

        Matrix.setIdentityM(mModelCube, 0);
        Matrix.translateM(mModelCube,0,posVec[0],newY, posVec[2]);
    }

    /**
     * Check if user is looking at object by calculating where the object is in eye-space.
     *
     * @return true if the user is looking at the object.
     */
    private boolean isLookingAtObject(){
        float[] initVec = {0,0,0,1.0f};
        float[] objPositionVec= new float[4];

        Matrix.multiplyMM(mModelView,0,mHeadView,0,mModelCube,0);
        Matrix.multiplyMV(objPositionVec,0,mModelView, 0,initVec, 0);

        float pitch = (float) Math.atan2(objPositionVec[1], -objPositionVec[2]);
        float yaw = (float) Math.atan2(objPositionVec[0], -objPositionVec[2]);

        return Math.abs(pitch) < PITCH_LIMIT && Math.abs(yaw) < YAW_LIMIT;
    }

    private static void checkGLError(String label){
        int error;
        while((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR){
            Log.e(TAG, label + ": glError " + error);
            throw new RuntimeException(label + ": glError " + error);
        }
    }

    /**
     * Draw the different elements
     */

    public void drawFloor(){
        GLES20.glUseProgram(mFloorProgram);

        GLES20.glUniform3fv(mFloorLightPosParam, 1, mLightPosInEyeSpace, 0);
        GLES20.glUniformMatrix4fv(mFloorModelParam, 1, false, mModelFloor, 0);
        GLES20.glUniformMatrix4fv(mFloorModelViewParam, 1, false, mModelView, 0);
        GLES20.glUniformMatrix4fv(mFloorModelViewProjectionParam, 1, false, mModelViewProjection,0);
        GLES20.glVertexAttribPointer(mFloorPositionParam, COORDS_PER_VERTEX, GLES20.GL_FLOAT,false, 0, mFloorVertices);
        GLES20.glVertexAttribPointer(mFloorNormalParam, 3, GLES20.GL_FLOAT, false, 0,mFloorNormals);
        GLES20.glVertexAttribPointer(mFloorColorParam, 4, GLES20.GL_FLOAT, false, 0, mFloorColors);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);

        checkGLError("drawing floor");
    }

    @Override
    protected void onStop(){
        _ibp.stopScan();
        super.onStop();
    }

    private void scanBeacons() {
        Log.i(TAG, "Scan for beacons");

        if(!IBeaconProtocol.configureBluetoothAdapter(this)){
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_BLUETOOTH_ENABLE);
        }else{
            if(_ibp.isScanning())
                _ibp.stopScan();
            _ibp.reset();
            _ibp.startScan();
        }
    }

    @Override
    public void enterRegion(IBeacon ibeacon) {
        Log.i(TAG,"Enter region");
        Beacon beck = new Beacon(ibeacon.getUuidHexString(),ibeacon.getMajor(),ibeacon.getMinor(),ibeacon.getProximity());
        db.updateProx(beck);

    }

    @Override
    public void exitRegion(IBeacon ibeacon) {
        Log.i(TAG,"Exit region");
        Beacon beck = new Beacon(ibeacon.getUuidHexString(),ibeacon.getMajor(),ibeacon.getMinor(),ibeacon.getProximity());
        db.updateProx(beck);
    }

    @Override
    public void beaconFound(IBeacon ibeacon) {
        _beacons.add(ibeacon);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                _beaconsAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void searchState(int state) {
        if(state == IBeaconProtocol.SEARCH_STARTED){
            //startRefreshAnimation();

        }else if (state == IBeaconProtocol.SEARCH_END_EMPTY || state == IBeaconProtocol.SEARCH_END_SUCCESS){
            //stopRefreshAnimation();
        }
    }

    @Override
    public void operationError(int status) {

    }
}
