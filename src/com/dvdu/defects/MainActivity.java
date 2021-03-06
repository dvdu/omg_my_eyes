package com.dvdu.defects;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class MainActivity extends Activity implements CvCameraViewListener2, OnClickListener {
	// Views
	private MyCameraView 		cameraView;
	private ImageView 			imageView;
	private ViewFlipper 		viewFlipper;
	
	// Menus
	private MenuItem[] 			sourceMenuItems;
	private MenuItem[] 			defectMenuItems;
	private SubMenu	            sourceMenu;				// set the Source
	private SubMenu	            defectMenu;				// set the Defect
	
	// Image processing
	private ImageProcessor		imageProcessor;
	private Mat                 cameraFrame;
	private Mat                 galleryOriginal;
	private Mat                 galleryEffect;
	private Bitmap				galleryBitmap = null;

	// Control
	private Boolean				cameraSource = true;
	private Boolean				imageSaved = false;
	private static ViewModes    viewMode = ViewModes.VIEW_MODE_RGBA;

	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS:
			{
				cameraView.enableView();
			} break;
			default:
			{
				super.onManagerConnected(status);
			} break;
			}
		}
	};

	public MainActivity() {
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		imageProcessor = new ImageProcessor();
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.main_view);
		initViews();
	}
	
	private void initViews(){
		cameraView = (MyCameraView) findViewById(R.id.main_camera_view);
		cameraView.setVisibility(SurfaceView.VISIBLE);
		cameraView.setOnClickListener(MainActivity.this);
		cameraView.setCvCameraViewListener(this);
		
		imageView = (ImageView) findViewById(R.id.imageView1);
		imageView.setVisibility(SurfaceView.INVISIBLE);
		imageView.setOnClickListener(MainActivity.this);
		
		viewFlipper=(ViewFlipper)findViewById(R.id.ViewFlipper01);
	}

	@Override
	public void onPause()
	{
		super.onPause();
		if (cameraView != null)
			cameraView.disableView();
	}

	@Override
	public void onResume()
	{
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
	}

	public void onDestroy() {
		super.onDestroy();
		if (cameraView != null)
			cameraView.disableView();
		
		// Trigger multimedia scanner if something was saved
		if (imageSaved)
			sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
	}

	public void onCameraViewStarted(int width, int height) {
		cameraFrame = new Mat();
		galleryOriginal = new Mat();
		galleryEffect = new Mat();
	}

	public void onCameraViewStopped() {
	}

	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		cameraFrame = inputFrame.rgba();
		switch (viewMode)
		{
		case VIEW_MODE_RGBA:
			break;
		case VIEW_MODE_DEUTERANOPE:
			imageProcessor.deuteranope(cameraFrame);
			break;
		case VIEW_MODE_PROTANOPE:
			imageProcessor.protanope(cameraFrame);
			break;
		case VIEW_MODE_TRITANOPE:
			imageProcessor.tritanope(cameraFrame);
			break;
		case VIEW_MODE_CATARACT:
			imageProcessor.cataract(cameraFrame);
			break;
		case VIEW_MODE_DIABETIC_RETINOPATHY:
			imageProcessor.diabeticRetinopathy(cameraFrame);
			break;
		case VIEW_MODE_RETINIS_PIGMENTOSA:
			imageProcessor.retinisPigmentosa(cameraFrame);
			break;
		default:
			break;
		}
		return cameraFrame;
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		defectMenu = menu.addSubMenu(R.string.defectTitle);
		defectMenuItems = new MenuItem[7];
		defectMenuItems[0] = defectMenu.add(0, 1, Menu.NONE, R.string.defectNormal);
		defectMenuItems[1] = defectMenu.add(0, 2, Menu.NONE, R.string.defectDeuteranope);
		defectMenuItems[2] = defectMenu.add(0, 3, Menu.NONE, R.string.defectProtanope);
		defectMenuItems[3] = defectMenu.add(0, 4, Menu.NONE, R.string.defectTritanope);
		defectMenuItems[4] = defectMenu.add(0, 5, Menu.NONE, R.string.defectCataract);
		defectMenuItems[5] = defectMenu.add(0, 6, Menu.NONE, R.string.defectDiabeticRetinopathy);
		defectMenuItems[6] = defectMenu.add(0, 7, Menu.NONE, R.string.defectRetinitisPigmentosa);

		sourceMenu = menu.addSubMenu(R.string.sourceTitle);
		sourceMenuItems = new MenuItem[2];
		sourceMenuItems[0] = sourceMenu.add(1, 1, Menu.NONE, R.string.sourceGallery);
		sourceMenuItems[1] = sourceMenu.add(1, 2, Menu.NONE, R.string.sourceCamera);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getGroupId() == 0){
			int id = item.getItemId();
			switch (id){
			case 1:
				viewMode = ViewModes.VIEW_MODE_RGBA;
				imageProcessor.setupColorBlindness(viewMode);
				break;
			case 2:
				viewMode = ViewModes.VIEW_MODE_DEUTERANOPE;
				imageProcessor.setupColorBlindness(viewMode);
				break;
			case 3:
				viewMode = ViewModes.VIEW_MODE_PROTANOPE;
				imageProcessor.setupColorBlindness(viewMode);
				break;
			case 4:
				viewMode = ViewModes.VIEW_MODE_TRITANOPE;
				imageProcessor.setupColorBlindness(viewMode);
				break;
			case 5:
				viewMode = ViewModes.VIEW_MODE_CATARACT;
				break;
			case 6:
				viewMode = ViewModes.VIEW_MODE_DIABETIC_RETINOPATHY;
				if (cameraSource) {
					imageProcessor.setupRetinopathy(cameraFrame);
				} else {
					imageProcessor.setupRetinopathy(galleryOriginal);
				}
				break;
			case 7:
				viewMode = ViewModes.VIEW_MODE_RETINIS_PIGMENTOSA;
				if (cameraSource) {
					imageProcessor.setupPigmentosa(cameraFrame);
				} else {
					imageProcessor.setupPigmentosa(galleryOriginal);
				}
				break;
			default:

			}
			// process image only if it is from gallery, otherwise onCameraFrame will take care
			if (!cameraSource && galleryBitmap != null && !galleryEffect.empty()){
				processImage();
			}
		}
		else if (item.getGroupId() == 1){
			int id = item.getItemId();
			if (id == 1){
				// Gallery source
				// Open gallery
				Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				startActivityForResult(intent, 0);
				// Switch view
				if (cameraSource){
					flipToGallery();
				}
			} else if (id == 2){
				// Camera source
				// Switch view
				if (!cameraSource){
					flipToCamera();
				}
			}
		}
		return true;
	}



	@Override
	public void onClick(View v) { // save on click
		Boolean isSDPresent = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
		// check if memory card is present
		if(isSDPresent && (galleryBitmap != null || !cameraFrame.empty()))
		{
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
			String currentDateandTime = sdf.format(new Date());
			// create directory 
			File folder = new File(Environment.getExternalStorageDirectory().toString()+getString(R.string.saveDir));
			folder.mkdirs();
			// define filename
			String fileName = Environment.getExternalStorageDirectory().getPath() +	getString(R.string.saveDir) + "img" + currentDateandTime + ".jpg";
			Bitmap bmp = null;
			if (cameraSource){
				bmp = Bitmap.createBitmap(cameraFrame.cols(),cameraFrame.rows(),Bitmap.Config.ARGB_8888);
				// convert cv::Mat to Bitmap
				Utils.matToBitmap(cameraFrame, bmp, true);
			} else {
				bmp = galleryBitmap;
			}
			try {
				FileOutputStream out = new FileOutputStream(fileName);
				// try to save the image
				Boolean result = bmp.compress(Bitmap.CompressFormat.JPEG, 90, out);
				// print user message
				if (result){
					// Success
					Toast.makeText(this, getString(R.string.saveSuccess) + getString(R.string.saveDir) , Toast.LENGTH_SHORT).show();
					imageSaved = true;
				}
				else
				{
					// Fail
					Toast.makeText(this, getString(R.string.saveError), Toast.LENGTH_SHORT).show();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		else
		{
			Toast.makeText(this, getString(R.string.saveNoCard), Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK){
			// Gallery image chosen
			Uri targetUri = data.getData();
			try {
				galleryBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(targetUri));
				Utils.bitmapToMat(galleryBitmap, galleryOriginal);
				if(galleryOriginal.empty()) {
					// Error - image not opened
					Toast.makeText(this, getString(R.string.openError), Toast.LENGTH_LONG).show();
				} else {
					// Image opened successfully
					processImage();
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		} else {
			// No image chosen - back to camera view
			flipToCamera();
		}

	}
	private void processImage(){
		galleryEffect = imageProcessor.process(galleryOriginal, viewMode);
		galleryBitmap = Bitmap.createBitmap(galleryEffect.cols(), galleryEffect.rows(), Bitmap.Config.ARGB_8888);
		Utils.matToBitmap(galleryEffect, galleryBitmap);
		imageView.setImageBitmap(galleryBitmap);
	}
	
	private void flipToGallery(){
		cameraSource = false;
		imageView.setVisibility(SurfaceView.VISIBLE);
		cameraView.setVisibility(SurfaceView.INVISIBLE);
		viewFlipper.showNext();
	}
	
	private void flipToCamera(){
		cameraSource = true;
		cameraView.setVisibility(SurfaceView.VISIBLE);
		imageView.setVisibility(SurfaceView.INVISIBLE);
		viewFlipper.showPrevious();
	}

}
