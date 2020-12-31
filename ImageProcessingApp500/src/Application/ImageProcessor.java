package Application;

import java.util.*;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

/**
* This class provides a variety of image processing methods.
* The code is updated from Kick Ass Java Programming by Tonny Espeset
* Coriolis Group Books, 1996.
*
* @author William Edison
* @version 4.00 April 2016
*
*/

public class ImageProcessor {

	int width;
	int height;
	int centerX;
	int centerY;
	Color bgColor;

	Image image;
	PixelReader pixelReader;
	PixelWriter pixelWriter;
	WritableImage wImage;

	ImageProcessor (Image image)
	{
		this.image = image;
		width = (int)image.getWidth();
		height = (int)image.getHeight();
		centerX = Math.round(width/2);
		centerY = Math.round(height/2);
    	bgColor = Color.BLACK;

    	// Obtain PixelReader
    	pixelReader = image.getPixelReader();

    	// Create WritableImage
    	wImage = new WritableImage(
             (int)image.getWidth(),
             (int)image.getHeight());
    	pixelWriter = wImage.getPixelWriter();
	}

	/* Filters start here */

	public WritableImage copy()
	{
    	// Copy pixels
    	for(int readY=0;readY<image.getHeight();readY++){
	        for(int readX=0; readX<image.getWidth();readX++){
            	int rgb = pixelReader.getArgb(readX,readY);
            	pixelWriter.setArgb(readX,readY,rgb);
	        }
    	}
    	return wImage;
	}

	public WritableImage invert()
	{
    	// Determine the color of each pixel in a specified row
    	for(int readY=0;readY<image.getHeight();readY++){
	        for(int readX=0; readX<image.getWidth();readX++){
	            // Invert image
            	int rgb = pixelReader.getArgb(readX,readY)^0xffffff;
            	pixelWriter.setArgb(readX,readY,rgb);
	        }
    	}
    	return wImage;
	}


	public WritableImage gray()
	{
    	for(int readY=0;readY<image.getHeight();readY++){
	        for(int readX=0; readX<image.getWidth();readX++){
	        	int rgb = pixelReader.getArgb(readX,readY);
        		int r = (rgb & 0xff0000)>>16;
        		int g = (rgb & 0xff00)>>8;
        		int b = (rgb & 0xff);
        		int gray = (r*3+g*4+b*2)/9;  //calculate the right gray value based on r, g and b intensities
        		pixelWriter.setArgb(readX, readY , (rgb & 0xff000000)+(gray << 16)+(gray << 8)+gray);
	        }
    	}
    	return wImage;
	}

	public WritableImage noise(Float percent)
	{
    	Random rnd = new Random();

    	for(int readY=0;readY<image.getHeight();readY++){
	        for(int readX=0; readX<image.getWidth();readX++){
            	int rgb = pixelReader.getArgb(readX,readY);
             	int r = (rgb & 0xff0000) >> 16;
         	 	int g = (rgb & 0xff00) >> 8;
         	 	int b = (rgb & 0xff);
             	int RandomBrightness = (int)(rnd.nextFloat() * percent) + 100;
              	r = Math.min((r*RandomBrightness)/100,255);
              	g = Math.min((g*RandomBrightness)/100,255);
              	b = Math.min((b*RandomBrightness)/100,255);
            	pixelWriter.setArgb(readX, readY, (rgb & 0xff000000)+(r << 16)+(g << 8)+b);
	        }
    	}
    	return wImage;
	}

	public WritableImage brightness(int percent)
	{
		for (int readY = 0; readY < image.getHeight(); readY++) {
	        for (int readX=0; readX < image.getWidth(); readX++) {
            	int rgb = pixelReader.getArgb(readX,readY);
             	int r = (rgb & 0xff0000) >> 16;
         	 	int g = (rgb & 0xff00) >> 8;
         	 	int b = (rgb & 0xff);
				r = Math.min(255, (r*percent)/100);
				g = Math.min(255, (g*percent)/100);
				b = Math.min(255, (b*percent)/100);
     			pixelWriter.setArgb(readX, readY, (rgb & 0xff000000)+(r << 16)+(g << 8)+b);
	        }
		}
		return wImage;
	}

	public WritableImage pseudoColors(long seed)
	{
		Random rnd = new Random();
		rnd.setSeed(seed);
		int random = rnd.nextInt();

		for (int readY = 0; readY < image.getHeight(); readY++) {
		    for (int readX=0; readX < image.getWidth(); readX++) {
		    	int rgb = pixelReader.getArgb(readX,readY);
				pixelWriter.setArgb(readX, readY, (rgb & 0xff000000)+((rgb << random) & 0xffffff));
		    }
		}
		return wImage;
	}

	public WritableImage horizonalWave (double nWaves, double percent, double offset)
	{
       	double waveFrequency=(nWaves*Math.PI*2.0)/height;
    	double waveOffset=(offset*nWaves*Math.PI*2.0)/100.0;
    	double radius=(width*percent)/100.0;

    	int index=0;
    	for (int y=0;y<height;y++)
        {
    		int xOffset=(int)Math.round(Math.sin(y*waveFrequency+waveOffset)*radius);
    		for (int x=0;x<width;x++)
    		{
       			if (xOffset >= 0 && xOffset < width) {
					Color color = pixelReader.getColor(xOffset, y);
	 	            pixelWriter.setColor(index % width, index / width, color);
    			}
    			else {
	 	            pixelWriter.setColor(index % width, index / width, bgColor);
    			}
    			xOffset++;
      			index++;
    		}
        }
		return wImage;
	}

	public WritableImage ripple (double nWaves, double percent, double offset)
	{
      	  double angleRadians = (Math.PI*2.0*percent)/100.0;
    	  double maxDist = Math.sqrt(width*width+height*height);
    	  double scale = (Math.PI*2.0*nWaves)/maxDist;
    	  offset = (offset*Math.PI*2.0)/100.0;

    	  int index = 0;
    	  for (int y = -centerY; y < centerY; y++) {
    		 for (int x = -centerX; x < centerX; x++) {
    			double a = Math.sin(Math.sqrt(x*x+y*y)*scale+offset)*angleRadians;
    			double ca = Math.cos(a);
    			double sa = Math.sin(a);

    			int xs = (int)(x*ca-y*sa)+centerX;
    			int ys = (int)(y*ca+x*sa)+centerY;
    			if (xs >= 0 && xs < width && ys >= 0 && ys < height) {
    				Color color = pixelReader.getColor(xs, ys);
     	            pixelWriter.setColor(index % width, index / width, color);
    			}
    			else {
    				pixelWriter.setColor(index % width, index / width, bgColor);
    			}
   				index++;
    		 }
    	  }
    	  return wImage;
    }

	public WritableImage transparency(int percent)
	{
		for(int readY = 0; readY < image.getHeight(); readY++) {
		    for(int readX = 0; readX < image.getWidth(); readX++) {
		    	int rgb = pixelReader.getArgb(readX, readY);
		    	int a = (rgb >> 24) & 0xff;
		    	a = Math.min(255, (a*percent)/100);
		    	pixelWriter.setArgb(readX, readY, (rgb & 0xffffff) + (a<<24));
		    }
		}
		return wImage;
	}


	public WritableImage makeTransparent(int r, int g, int b, double percent)
	{
		percent*=1.28;

		//Makes given color within the given range transparent
		int rMin=(int)(r-percent);
		int gMin=(int)(g-percent);
		int bMin=(int)(b-percent);

		int rMax=(int)(r+percent);
		int gMax=(int)(g+percent);
		int bMax=(int)(b+percent);

		for(int readY = 0; readY < image.getHeight(); readY++) {
		    for(int readX = 0; readX < image.getWidth(); readX++) {
		    	int rgb = pixelReader.getArgb(readX,readY);
		    	r = (rgb & 0xff0000) >> 16;
		    	g = (rgb & 0xff00) >> 8;
		  		b = (rgb & 0xff);
		  		if ((r>=rMin) && (r<=rMax) && (g>=gMin) && (g<=gMax) && (b>=bMin) && (b<=bMax))
		  			pixelWriter.setArgb(readX, readY, rgb & 0xffffff);
		    }
		}
		return wImage;
	}

	public WritableImage lineArt(int intensity)
	{
		//The first line is undefined, give it the color of bgColor
		for(int readY = 0; readY < 1; readY++) {
		    for(int readX = 0; readX < image.getWidth(); readX++) {
		    	pixelWriter.setArgb(readX, readY, 0);
		    }
		}

		for(int readY = 1; readY < image.getHeight(); readY++) {
		    for(int readX = 0; readX < image.getWidth(); readX++) {
				//Read pixel to the left
			   	int rgb1 = 0;
			    if (readX >0)
			    	rgb1 = pixelReader.getArgb(readX-1,readY);
			   	else
			    	 rgb1 = pixelReader.getArgb(readX,readY);
				int r1 = (rgb1 & 0xff0000) >> 16;
				int g1 = (rgb1 & 0xff00) >> 8;
				int b1 = (rgb1 & 0xff);

				//Read pixel above
				int rgb2 = pixelReader.getArgb(readX,readY-1);
				int r2 = (rgb2 & 0xff0000)>>16;
				int g2 = (rgb2 & 0xff00)>>8;
				int b2 = (rgb2 & 0xff);

				//Read current pixel
				int rgb = pixelReader.getArgb(readX,readY);
				int r =(rgb & 0xff0000) >>16;
				int g =(rgb & 0xff00) >>8;
				int b =(rgb & 0xff);

				r = Math.min((Math.abs(r2-r)+Math.abs(r1-r))*intensity,255);
				g = Math.min((Math.abs(g2-g)+Math.abs(g1-g))*intensity,255);
				b = Math.min((Math.abs(b2-b)+Math.abs(b1-b))*intensity,255);

				pixelWriter.setArgb(readX, readY, (rgb & 0xff000000)+(r << 16)+(g << 8)+b);
		    }
		}
		return wImage;
	}

	public WritableImage graylineArt(int intensity)
	{
		//The first line is undefined, give it the color of bgColor
		for(int readY = 0; readY < 1; readY++) {
		    for(int readX = 0; readX < image.getWidth(); readX++) {
		    	pixelWriter.setArgb(readX, readY, 0);
		    }
		}

		for(int readY = 1; readY < image.getHeight(); readY++) {
		    for(int readX = 0; readX < image.getWidth(); readX++) {
				//Read pixel to the left
			   	int rgb1 = 0;
			    if (readX >0)
			    	rgb1 = pixelReader.getArgb(readX-1,readY);
			   	else
			    	 rgb1 = pixelReader.getArgb(readX,readY);
				int r1 = (rgb1 & 0xff0000) >> 16;
				int g1 = (rgb1 & 0xff00) >> 8;
				int b1 = (rgb1 & 0xff);
				int gray = (r1*3+g1*4+b1*2)/9;  //calculate the right gray value based on r, g and b intensities
        		r1 = gray;
        		g1 = gray;
        		b1= gray;

				//Read pixel above
				int rgb2 = pixelReader.getArgb(readX,readY-1);
				int r2 = (rgb2 & 0xff0000)>>16;
				int g2 = (rgb2 & 0xff00)>>8;
				int b2 = (rgb2 & 0xff);
				gray = (r2*3+g2*4+b2*2)/9;  //calculate the right gray value based on r, g and b intensities
        		r2 = gray;
        		g2 = gray;
        		b2 = gray;

				//Read current pixel
				int rgb = pixelReader.getArgb(readX,readY);
				int r =(rgb & 0xff0000) >>16;
				int g =(rgb & 0xff00) >>8;
				int b =(rgb & 0xff);
				gray = (r*3+g*4+b*2)/9;  //calculate the right gray value based on r, g and b intensities
        		r = gray;
        		g = gray;
        		b = gray;

				r = Math.min((Math.abs(r2-r)+Math.abs(r1-r))*intensity,255);
				g = Math.min((Math.abs(g2-g)+Math.abs(g1-g))*intensity,255);
				b = Math.min((Math.abs(b2-b)+Math.abs(b1-b))*intensity,255);

				pixelWriter.setArgb(readX, readY, (rgb & 0xff000000)+(r << 16)+(g << 8)+b);
		    }
		}
		return wImage;
	}

	public WritableImage emboss(double angle,double power,int red,int green, int blue)
	{
	    double angleRadians=angle/(180.0/Math.PI);
        int light=(int)(Math.round(Math.sin(angleRadians))*width)+(int)(Math.round(Math.cos(angleRadians)))-1;

        for (int readY = 1; readY < height; readY++) {
		    for (int readX = 0; readX < width; readX++) {

				//Read current pixel
				int rgb1 = pixelReader.getArgb(readX,readY);
				int r1 = (rgb1 & 0xff0000) >> 16;
				int g1 = (rgb1 & 0xff00) >> 8;
				int b1 = (rgb1 & 0xff);

				//Read pixel in the direction given by angle
				int index = readX + readY*width;
				int rgb2 = pixelReader.getArgb((index-light)%width,(index-light)/width);
				int r2 =(rgb2 & 0xff0000) >> 16;
				int g2 =(rgb2 & 0xff00) >> 8;
				int b2 =(rgb2 & 0xff);

				int r = Math.min(Math.max(red+(int)((r2-r1)*power),0),255);
				int g = Math.min(Math.max(green+(int)((g2-g1)*power),0),255);
				int b = Math.min(Math.max(blue+(int)((b2-b1)*power),0),255);

				pixelWriter.setArgb(readX, readY,(rgb1 & 0xff000000)+(r<<16)+(g<<8)+b);
		    }
		}

		//Borders are undefined, fill with specified color
        int color = (red<<16)+(green<<8)+blue;

	    for (int readX = 0; readX < width; readX++) {
        	int rgb1 = pixelReader.getArgb(readX, 0);
            pixelWriter.setArgb(readX, 0, (rgb1 & 0xff000000)+color);
            int rgb2 = pixelReader.getArgb(readX, height-1);
            pixelWriter.setArgb(readX, height-1, (rgb2 & 0xff000000)+color);
	    }

		for(int readY = 0; readY < height; readY++) {
        	int rgb1 = pixelReader.getArgb(0, readY);
            pixelWriter.setArgb(0, readY, (rgb1 & 0xff000000)+color);
            int rgb2 = pixelReader.getArgb(width-1, readY);
            pixelWriter.setArgb(width-1, readY, (rgb2 & 0xff000000)+color);
        }
		return wImage;
    }

	public WritableImage grayemboss(double angle,double power,int red,int green, int blue)
	{
	    double angleRadians=angle/(180.0/Math.PI);
        int light=(int)(Math.round(Math.sin(angleRadians))*width)+(int)(Math.round(Math.cos(angleRadians)))-1;

        for (int readY = 1; readY < height; readY++) {
		    for (int readX = 0; readX < width; readX++) {

				//Read current pixel
				int rgb1 = pixelReader.getArgb(readX,readY);
				int r1 = (rgb1 & 0xff0000) >> 16;
				int g1 = (rgb1 & 0xff00) >> 8;
				int b1 = (rgb1 & 0xff);
				int gray = (r1*3+g1*4+b1*2)/9;  //calculate the right gray value based on r, g and b intensities
        		r1 = gray;
        		g1 = gray;
        		b1 = gray;

				//Read pixel in the direction given by angle
				int index = readX + readY*width;
				int rgb2 = pixelReader.getArgb((index-light)%width,(index-light)/width);
				int r2 =(rgb2 & 0xff0000) >> 16;
				int g2 =(rgb2 & 0xff00) >> 8;
				int b2 =(rgb2 & 0xff);
				gray = (r2*3+g2*4+b2*2)/9;  //calculate the right gray value based on r, g and b intensities
        		r2 = gray;
        		g2 = gray;
        		b2 = gray;

				int r = Math.min(Math.max(red+(int)((r2-r1)*power),0),255);
				int g = Math.min(Math.max(green+(int)((g2-g1)*power),0),255);
				int b = Math.min(Math.max(blue+(int)((b2-b1)*power),0),255);

				pixelWriter.setArgb(readX, readY,(rgb1 & 0xff000000)+(r<<16)+(g<<8)+b);
		    }
		}

		//Borders are undefined, fill with specified color
        int color = (red<<16)+(green<<8)+blue;

	    for (int readX = 0; readX < width; readX++) {
        	int rgb1 = pixelReader.getArgb(readX, 0);
            pixelWriter.setArgb(readX, 0, (rgb1 & 0xff000000)+color);
            int rgb2 = pixelReader.getArgb(readX, height-1);
            pixelWriter.setArgb(readX, height-1, (rgb2 & 0xff000000)+color);
	    }

		for(int readY = 0; readY < height; readY++) {
        	int rgb1 = pixelReader.getArgb(0, readY);
            pixelWriter.setArgb(0, readY, (rgb1 & 0xff000000)+color);
            int rgb2 = pixelReader.getArgb(width-1, readY);
            pixelWriter.setArgb(width-1, readY, (rgb2 & 0xff000000)+color);
        }
		return wImage;
    }

	public WritableImage zoom(int percent)
	{
		int bg = 0;

		int index=0;
		for (int y = -centerY; y < centerY; y++) {
			for (int x =-centerX; x < centerX; x++) {
				int xs = (x*percent)/100+centerX;
				int ys = (y*percent)/100+centerY;
				index++;
				if (index >= width*height)
					break;
				if (xs >= 0 && xs < width && ys >= 0 && ys < height) {
				  	int rgb = pixelReader.getArgb(xs, ys);
					pixelWriter.setArgb(index%width, index/width, rgb);
				}
				else if (index < width*height) {
						pixelWriter.setArgb(index%width, index/width, bg);
				}
			}
		 }
		return wImage;
	}

}