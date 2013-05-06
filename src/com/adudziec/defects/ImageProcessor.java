package com.adudziec.defects;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class ImageProcessor {
	
	private double a1, b1, c1, a2, b2, c2, inflection, tmpDouble, L, M, S;
	private float[] 			buff;
	private Mat					 tmp;
	private double[]			 RGB_LMS = {0.05059983, 0.08585369, 0.00952420,
											0.01893033, 0.08925308, 0.01370054,
											0.00292202, 0.00975732, 0.07145979};
	private double[]			 LMS_RGB = {30.830854, -29.832659, 1.610474, 
											-6.481468, 17.715578, -2.532642,
											-0.375690, -1.199062, 14.273846};
	
	public void setup(ViewModes viewMode){
		double anchor_e[] = new double[3];
		double anchor[] = new double[12];

		anchor[0] = 0.08008;  anchor[1]  = 0.1579;    anchor[2]  = 0.5897;
		anchor[3] = 0.1284;   anchor[4]  = 0.2237;    anchor[5]  = 0.3636;
		anchor[6] = 0.9856;   anchor[7]  = 0.7325;    anchor[8]  = 0.001079;
		anchor[9] = 0.0914;   anchor[10] = 0.007009;  anchor[11] = 0.0;

		anchor_e[0] = 0.05059983 + 0.08585369 + 0.00952420;
		anchor_e[1] = 0.01893033 + 0.08925308 + 0.01370054;
		anchor_e[2] = 0.00292202 + 0.00975732 + 0.07145979;

		switch (viewMode)
		{
		case VIEW_MODE_RGBA:
			break;
		case VIEW_MODE_DEUTERANOPE:
			a1 = anchor_e[1] * anchor[8] - anchor_e[2] * anchor[7];
			b1 = anchor_e[2] * anchor[6] - anchor_e[0] * anchor[8];
			c1 = anchor_e[0] * anchor[7] - anchor_e[1] * anchor[6];
			a2 = anchor_e[1] * anchor[2] - anchor_e[2] * anchor[1];
			b2 = anchor_e[2] * anchor[0] - anchor_e[0] * anchor[2];
			c2 = anchor_e[0] * anchor[1] - anchor_e[1] * anchor[0];
			inflection = (anchor_e[2] / anchor_e[0]);
			break;
		case VIEW_MODE_PROTANOPE:
			a1 = anchor_e[1] * anchor[8] - anchor_e[2] * anchor[7];
			b1 = anchor_e[2] * anchor[6] - anchor_e[0] * anchor[8];
			c1 = anchor_e[0] * anchor[7] - anchor_e[1] * anchor[6];
			a2 = anchor_e[1] * anchor[2] - anchor_e[2] * anchor[1];
			b2 = anchor_e[2] * anchor[0] - anchor_e[0] * anchor[2];
			c2 = anchor_e[0] * anchor[1] - anchor_e[1] * anchor[0];
			inflection = (anchor_e[2] / anchor_e[1]);
			break;
		case VIEW_MODE_TRITANOPE:
			a1 = anchor_e[1] * anchor[11] - anchor_e[2] * anchor[10];
			b1 = anchor_e[2] * anchor[9]  - anchor_e[0] * anchor[11];
			c1 = anchor_e[0] * anchor[10] - anchor_e[1] * anchor[9];
			a2 = anchor_e[1] * anchor[5]  - anchor_e[2] * anchor[4];
			b2 = anchor_e[2] * anchor[3]  - anchor_e[0] * anchor[5];
			c2 = anchor_e[0] * anchor[4]  - anchor_e[1] * anchor[3];
			inflection = (anchor_e[1] / anchor_e[0]);
			break;
		default:
			break;
		}
	}
	
	public Mat process(Mat src, ViewModes viewMode){
		Mat result = src.clone();
		switch (viewMode)
		{
		case VIEW_MODE_RGBA:
			break;
		case VIEW_MODE_DEUTERANOPE:
			deuteranope(result);
			break;
		case VIEW_MODE_PROTANOPE:
			protanope(result);
			break;
		case VIEW_MODE_TRITANOPE:
			tritanope(result);
			break;
		default:
			break;
		}
		return result;
	}
	
	public void deuteranope(Mat mRgba){
		if (tmp == null || (!tmp.size().equals(mRgba.size()) || tmp.total() != mRgba.total() || tmp.channels() != mRgba.channels())){
			tmp = new Mat(mRgba.size(), CvType.CV_32F);
			buff = new float[(int) (mRgba.total() * mRgba.channels())];
		}
		mRgba.convertTo(tmp, CvType.CV_32F);
		// get image matrix into Java primitive
		tmp.get(0, 0, buff);
		for (int i = 0; i < (int) (tmp.total() * tmp.channels()); i+=4){


			// convert RGB to LMS color space
			L = RGB_LMS[0] * buff[i] + RGB_LMS[1] * buff[i+1] + RGB_LMS[2] * buff[i+2];
			M = RGB_LMS[3] * buff[i] + RGB_LMS[4] * buff[i+1] + RGB_LMS[5] * buff[i+2];
			S = RGB_LMS[6] * buff[i] + RGB_LMS[7] * buff[i+1] + RGB_LMS[8] * buff[i+2];

			// apply color blindness 
			tmpDouble = S / L;

			if (tmpDouble < inflection)
				M = -(a1 * L + c1 * S) / b1;
			else
				M = -(a2 * L + c2 * S) / b2;

			// convert LMS to RGB color space
			buff[i] = (float) (LMS_RGB[0] * L + LMS_RGB[1] * M + LMS_RGB[2] * S);
			buff[i+1] = (float) (LMS_RGB[3] * L + LMS_RGB[4] * M + LMS_RGB[5] * S);
			buff[i+2] = (float) (LMS_RGB[6] * L + LMS_RGB[7] * M + LMS_RGB[8] * S);
		}
		// put new data into image matrix
		tmp.put(0, 0, buff);
		tmp.convertTo(mRgba, mRgba.depth());	
	}
	
	public void protanope(Mat mRgba){
		if (tmp == null || (tmp.size() != mRgba.size() && tmp.total() != mRgba.total() && tmp.channels() != mRgba.channels())){
			tmp = new Mat(mRgba.size(), CvType.CV_32F);
			buff = new float[(int) (mRgba.total() * mRgba.channels())];
		}
		mRgba.convertTo(tmp, CvType.CV_32F);
		// get image matrix into Java primitive
		tmp.get(0, 0, buff);
		for (int i = 0; i < (int) (tmp.total() * tmp.channels()); i+=4){
			// convert RGB to LMS color space
			L = RGB_LMS[0] * buff[i] + RGB_LMS[1] * buff[i+1] + RGB_LMS[2] * buff[i+2];
			M = RGB_LMS[3] * buff[i] + RGB_LMS[4] * buff[i+1] + RGB_LMS[5] * buff[i+2];
			S = RGB_LMS[6] * buff[i] + RGB_LMS[7] * buff[i+1] + RGB_LMS[8] * buff[i+2];

			// apply color blindness 
			tmpDouble = S / M;

			if (tmpDouble < inflection)
				L = -(b1 * M + c1 * S) / a1;
			else
				L = -(b2 * M + c2 * S) / a2;

			// convert LMS to RGB color space
			buff[i] = (float) (LMS_RGB[0] * L + LMS_RGB[1] * M + LMS_RGB[2] * S);
			buff[i+1] = (float) (LMS_RGB[3] * L + LMS_RGB[4] * M + LMS_RGB[5] * S);
			buff[i+2] = (float) (LMS_RGB[6] * L + LMS_RGB[7] * M + LMS_RGB[8] * S);
		}
		// put new data into image matrix
		tmp.put(0, 0, buff);
		tmp.convertTo(mRgba, mRgba.depth());
	}
	
	public void tritanope(Mat mRgba){
		if (tmp == null || (tmp.size() != mRgba.size() && tmp.total() != mRgba.total() && tmp.channels() != mRgba.channels())){
			tmp = new Mat(mRgba.size(), CvType.CV_32F);
			buff = new float[(int) (mRgba.total() * mRgba.channels())];
		}
		mRgba.convertTo(tmp, CvType.CV_32F);
		// get image matrix into Java primitive
		tmp.get(0, 0, buff);
		for (int i = 0; i < (int) (tmp.total() * tmp.channels()); i+=4){
			// convert RGB to LMS color space
			L = RGB_LMS[0] * buff[i] + RGB_LMS[1] * buff[i+1] + RGB_LMS[2] * buff[i+2];
			M = RGB_LMS[3] * buff[i] + RGB_LMS[4] * buff[i+1] + RGB_LMS[5] * buff[i+2];
			S = RGB_LMS[6] * buff[i] + RGB_LMS[7] * buff[i+1] + RGB_LMS[8] * buff[i+2];

			// apply color blindness 
			tmpDouble = M / L;

			if (tmpDouble < inflection)
				S = -(a1 * L + b1 * M) / c1;
			else
				S = -(a2 * L + b2 * M) / c2;

			// convert LMS to RGB color space
			buff[i] = (float) (LMS_RGB[0] * L + LMS_RGB[1] * M + LMS_RGB[2] * S);
			buff[i+1] = (float) (LMS_RGB[3] * L + LMS_RGB[4] * M + LMS_RGB[5] * S);
			buff[i+2] = (float) (LMS_RGB[6] * L + LMS_RGB[7] * M + LMS_RGB[8] * S);
		}
		// put new data into image matrix
		tmp.put(0, 0, buff);
		tmp.convertTo(mRgba, mRgba.depth());
		
	}
	
	

}
