package com.app;

import java.awt.image.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

import com.app.util.OtsuProcessor;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.image.*;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

import javax.imageio.ImageIO;

/**
 * This class provides a variety of image processing methods.
 * The code is updated from Kick Ass Java Programming by Tonny Espeset
 * Coriolis Group Books, 1996.
 *
 * @author William Edison
 * @version 4.00 April 2016
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

    ImageProcessor(Image image) {
        this.image = image;
        width = (int) image.getWidth();
        height = (int) image.getHeight();
        centerX = Math.round(width / 2);
        centerY = Math.round(height / 2);
        bgColor = Color.BLACK;

        // Obtain PixelReader
        pixelReader = image.getPixelReader();

        // Create WritableImage
        wImage = new WritableImage(
                (int) image.getWidth(),
                (int) image.getHeight());
        pixelWriter = wImage.getPixelWriter();
    }

    /* Filters start here */

    public WritableImage copy() {
        // Copy pixels
        for (int readY = 0; readY < image.getHeight(); readY++) {
            for (int readX = 0; readX < image.getWidth(); readX++) {
                int rgb = pixelReader.getArgb(readX, readY);
                pixelWriter.setArgb(readX, readY, rgb);
            }
        }
        return wImage;
    }

    public WritableImage invert() {
        // Determine the color of each pixel in a specified row
        for (int readY = 0; readY < image.getHeight(); readY++) {
            for (int readX = 0; readX < image.getWidth(); readX++) {
                // Invert image
                int rgb = pixelReader.getArgb(readX, readY);
                int r = (rgb & 0xff0000) >> 16;
                int g = (rgb & 0xff00) >> 8;
                int b = (rgb & 0xff);
                r = 255 - r;
                g = 255 - g;
                b = 255 - b;
                pixelWriter.setArgb(readX, readY, (rgb & 0xff000000) + (r << 16) + (g << 8) + b);
            }
        }
        return wImage;
    }

    public WritableImage addConst(int i) {
        for (int readY = 0; readY < image.getHeight(); readY++) {
            for (int readX = 0; readX < image.getWidth(); readX++) {
                // Add const
                int rgb = pixelReader.getArgb(readX, readY);
                int r = (rgb & 0xff0000) >> 16;
                int g = (rgb & 0xff00) >> 8;
                int b = (rgb & 0xff);
                r += i;
                g += i;
                b += i;
                pixelWriter.setArgb(readX, readY, (rgb & 0xff000000) + (r << 16) + (g << 8) + b);
            }
        }
        return wImage;
    }

    public WritableImage substractConst(int i) {
        for (int readY = 0; readY < image.getHeight(); readY++) {
            for (int readX = 0; readX < image.getWidth(); readX++) {
                // Substract const 10
                int rgb = pixelReader.getArgb(readX, readY);
                int r = (rgb & 0xff0000) >> 16;
                int g = (rgb & 0xff00) >> 8;
                int b = (rgb & 0xff);
                r -= i;
                g -= i;
                b -= i;
                pixelWriter.setArgb(readX, readY, (rgb & 0xff000000) + (r << 16) + (g << 8) + b);
            }
        }
        return wImage;
    }

    public WritableImage log() {
        for (int readY = 0; readY < image.getHeight(); readY++) {
            for (int readX = 0; readX < image.getWidth(); readX++) {
                // Log
                int rgb = pixelReader.getArgb(readX, readY);
                int r = (rgb & 0xff0000) >> 16;
                int g = (rgb & 0xff00) >> 8;
                int b = (rgb & 0xff);
                r = (int) Math.log(r);
                g = (int) Math.log(g);
                b = (int) (int) Math.log(b);
                pixelWriter.setArgb(readX, readY, (rgb & 0xff000000) + (r << 16) + (g << 8) + b);
            }
        }
        return wImage;
    }

    public WritableImage pow(int i) {
        int fmax = Arrays.stream(getGrayscaleArray()).max().getAsInt();
        for (int readY = 0; readY < image.getHeight(); readY++) {
            for (int readX = 0; readX < image.getWidth(); readX++) {
                // Pow
                int rgb = pixelReader.getArgb(readX, readY);
                int max = fmax & 0xff;
                int r = (rgb & 0xff0000) >> 16;
                int g = (rgb & 0xff00) >> 8;
                int b = (rgb & 0xff);
                r = (int) (255 * Math.pow((double) r / (double) max, i));
                g = (int) (255 * Math.pow((double) g / (double) max, i));
                b = (int) (255 * Math.pow((double) b / (double) max, i));
                pixelWriter.setArgb(readX, readY, (rgb & 0xff000000) + (r << 16) + (g << 8) + b);
            }
        }
        return wImage;
    }

    public LineChart hist() {
        NumberAxis x = new NumberAxis();
        x.setAnimated(true);
        x.setLabel("Brightness");
        NumberAxis y = new NumberAxis();
        y.setAnimated(true);
        y.setLabel("Number of pixels");
        LineChart<Number, Number> hist = getNumberNumberLineChart(x, y);
        XYChart.Series<Number, Number> aSr = new XYChart.Series();
        int[] arr = new int[256];
        for (int readY = 0; readY < image.getHeight(); readY++) {
            for (int readX = 0; readX < image.getWidth(); readX++) {
                int rgb = pixelReader.getArgb(readX, readY);
                int b = (rgb & 0xff);
                arr[b]++;
            }
        }
        for (int i = 0; i < 256; i++) {
            aSr.getData().add(new XYChart.Data(i, arr[i]));
        }
        hist.getData().add(aSr);
        return hist;
    }

    public LineChart hist(WritableImage im) {
        NumberAxis x = new NumberAxis();
        x.setAnimated(true);
        x.setLabel("Brightness");
        NumberAxis y = new NumberAxis();
        y.setAnimated(true);
        y.setLabel("Number of pixels");
        LineChart<Number, Number> hist = getNumberNumberLineChart(x, y);
        XYChart.Series<Number, Number> aSr = new XYChart.Series();
        int[] arr = new int[256];
        for (int readY = 0; readY < im.getHeight(); readY++) {
            for (int readX = 0; readX < im.getWidth(); readX++) {
                int rgb = im.getPixelReader().getArgb(readX, readY);
                int b = (rgb & 0xff);
                arr[b]++;
            }
        }
        for (int i = 0; i < 256; i++) {
            aSr.getData().add(new XYChart.Data(i, arr[i]));
        }
        hist.getData().add(aSr);
        return hist;
    }

    private int[] getRChannelHistArr() {
        int[] arr = new int[256];
        for (int readY = 0; readY < image.getHeight(); readY++) {
            for (int readX = 0; readX < image.getWidth(); readX++) {
                int rgb = pixelReader.getArgb(readX, readY);
                int r = (rgb & 0xff0000) >> 16;
                arr[r]++;
            }
        }
        return arr;
    }

    private int[] getGChannelHistArr() {
        int[] arr = new int[256];
        for (int readY = 0; readY < image.getHeight(); readY++) {
            for (int readX = 0; readX < image.getWidth(); readX++) {
                int rgb = pixelReader.getArgb(readX, readY);
                int g = (rgb & 0xff00) >> 8;
                arr[g]++;
            }
        }
        return arr;
    }

    private int[] getBChannelHistArr() {
        int[] arr = new int[256];
        for (int readY = 0; readY < image.getHeight(); readY++) {
            for (int readX = 0; readX < image.getWidth(); readX++) {
                int rgb = pixelReader.getArgb(readX, readY);
                int b = (rgb & 0xff);
                arr[b]++;
            }
        }
        return arr;
    }

    private int[] getBChannelHistArr(WritableImage im) {
        int[] arr = new int[256];
        for (int readY = 0; readY < im.getHeight(); readY++) {
            for (int readX = 0; readX < im.getWidth(); readX++) {
                int rgb = im.getPixelReader().getArgb(readX, readY);
                int b = (rgb & 0xff);
                arr[b]++;
            }
        }
        return arr;
    }

    private LineChart<Number, Number> getNumberNumberLineChart(NumberAxis x, NumberAxis y) {
        LineChart<Number, Number> hist = new LineChart<Number, Number>(x, y) {
            //Overriding the layoutPlotChildren method
            protected void layoutPlotChildren() {
                super.layoutPlotChildren();
                Series<Number, Number> series = (Series<Number, Number>)
                        getData().get(0);
                ObservableList<Data<Number, Number>> listOfData = series.getData();
                for (int i = 0; i < listOfData.size() - 1; i++) {
                    double x1 = getXAxis().getDisplayPosition(listOfData.get(i).getXValue());
                    double y1 = getYAxis().getDisplayPosition(0);
                    double x2 = getXAxis().getDisplayPosition(listOfData.get((i + 1)).getXValue());
                    double y2 = getYAxis().getDisplayPosition(0);
                    Polygon polygon = new Polygon();
                    polygon.getPoints().addAll(new Double[]{
                            x1, y1, x1, getYAxis().getDisplayPosition(listOfData.get(i).getYValue()), x2, getYAxis().getDisplayPosition(listOfData.get((i + 1)).getYValue()), x2, y2
                    });
                    getPlotChildren().add(polygon);
                    polygon.toFront();
                    polygon.setFill(Color.DIMGRAY);
                }
            }
        };
        hist.setAnimated(true);
        hist.setCreateSymbols(false);
        return hist;
    }

    public WritableImage linearContrast() {
        int fMIN = 0;
        int fMAX = 255;
        int[] rArr = getRChannelHistArr();
        int[] gArr = getGChannelHistArr();
        int[] bArr = getBChannelHistArr();
        double rMin = getMin(rArr);
        double gMin = getMin(gArr);
        double bMin = getMin(bArr);
        double rMax = getMax(rArr);
        double gMax = getMax(gArr);
        double bMax = getMax(bArr);
        for (int readY = 0; readY < image.getHeight(); readY++) {
            for (int readX = 0; readX < image.getWidth(); readX++) {
                int rgb = pixelReader.getArgb(readX, readY);
                int r = (rgb & 0xff0000) >> 16;
                int g = (rgb & 0xff00) >> 8;
                int b = (rgb & 0xff);
                r = (int) (((r - rMin) / (rMax - rMin)) * (fMAX - fMIN) + fMIN);
                g = (int) (((g - gMin) / (gMax - gMin)) * (fMAX - fMIN) + fMIN);
                b = (int) (((b - bMin) / (bMax - bMin)) * (fMAX - fMIN) + fMIN);
                pixelWriter.setArgb(readX, readY, (rgb & 0xff000000) + (r << 16) + (g << 8) + b);
            }
        }
        return wImage;
    }

    public WritableImage bitSlice(int d) {
        BufferedImage src = SwingFXUtils.fromFXImage(image, null);
        BufferedImage transform = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);

        for (int i = 0; i < src.getWidth(); i++)
            for (int j = 0; j < src.getHeight(); j++) {

                int col = 0;
                int fcol = src.getRGB(i, j);
                if (((fcol >>> d) & 1) > 0) col = 0xffffff;
                transform.setRGB(i, j, col);
            }
        return convertToFxImage(transform);
    }

    public WritableImage otsuGlobalThreshold() {
        BufferedImage src = SwingFXUtils.fromFXImage(image, null);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(src, "jpg", baos);
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] srcData = baos.toByteArray();
        byte[] dstData = new byte[srcData.length];
        OtsuProcessor processor = new OtsuProcessor();
        int otsuGt = processor.doThreshold(srcData, dstData);
        for (int readY = 0; readY < image.getHeight(); readY++) {
            for (int readX = 0; readX < image.getWidth(); readX++) {
                int rgb = pixelReader.getArgb(readX, readY);
                int r = (rgb & 0xff0000) >> 16;
                int g = (rgb & 0xff00) >> 8;
                int b = (rgb & 0xff);
                if ((r + g + b) / 3 > otsuGt) {
                    r = 0;
                    g = 0;
                    b = 0;
                } else {
                    r = 255;
                    g = 255;
                    b = 255;
                }
                pixelWriter.setArgb(readX, readY, (rgb & 0xff000000) + (r << 16) + (g << 8) + b);
            }
        }
        return wImage;
    }

    public WritableImage histThreshold() {
        int t = findHistThreshold();
        for (int readY = 0; readY < image.getHeight(); readY++) {
            for (int readX = 0; readX < image.getWidth(); readX++) {
                int rgb = pixelReader.getArgb(readX, readY);
                int r = (rgb & 0xff0000) >> 16;
                int g = (rgb & 0xff00) >> 8;
                int b = (rgb & 0xff);
                if ((r + g + b) / 3 > t) {
                    r = 0;
                    g = 0;
                    b = 0;
                } else {
                    r = 255;
                    g = 255;
                    b = 255;
                }
                pixelWriter.setArgb(readX, readY, (rgb & 0xff000000) + (r << 16) + (g << 8) + b);
            }
        }
        return wImage;
    }

    public WritableImage gradThreshold() {
        int t = findBrightnessGradientThreshold();
        for (int readY = 0; readY < image.getHeight(); readY++) {
            for (int readX = 0; readX < image.getWidth(); readX++) {
                int rgb = pixelReader.getArgb(readX, readY);
                int r = (rgb & 0xff0000) >> 16;
                int g = (rgb & 0xff00) >> 8;
                int b = (rgb & 0xff);
                if ((r + g + b) / 3 > t) {
                    r = 0;
                    g = 0;
                    b = 0;
                } else {
                    r = 255;
                    g = 255;
                    b = 255;
                }
                pixelWriter.setArgb(readX, readY, (rgb & 0xff000000) + (r << 16) + (g << 8) + b);
            }
        }
        return wImage;
    }

    private int findHistThreshold() {
        int t = 230;
        int t_prev = t;
        double e = 0.0001;
        int[] g1 = new int[(int) (image.getWidth() * image.getHeight())];
        int[] g2 = new int[(int) (image.getWidth() * image.getHeight())];
        int i = 0;
        int j = 0;
        do {
            t_prev = t;
            for (int readY = 0; readY < image.getHeight(); readY++) {
                for (int readX = 0; readX < image.getWidth(); readX++) {
                    int rgb = pixelReader.getArgb(readX, readY);
                    int r = (rgb & 0xff0000) >> 16;
                    int g = (rgb & 0xff00) >> 8;
                    int b = (rgb & 0xff);
                    if ((r + g + b) / 3 < t) {
                        g1[i] = (r + g + b) / 3;
                        i++;
                    } else {
                        g2[j] = (r + g + b) / 3;
                        j++;
                    }
                }
            }
            int g1Avg = (int) Arrays.stream(g1).average().getAsDouble();
            int g2Avg = (int) Arrays.stream(g2).average().getAsDouble();
            t = (g1Avg + g2Avg) / 2;
            i = 0;
            j = 0;
        } while (Math.abs(t - t_prev) > e);
        return t;
    }

    private int findBrightnessGradientThreshold() {
        int gm = 0;
        int gn = 0;
        int gFunc = 0;
        int fgAccum = 0;
        int gAccum = 0;
        for (int readY = 1; readY < image.getHeight() - 1; readY++) {
            for (int readX = 1; readX < image.getWidth() - 1; readX++) {
                int rgb = pixelReader.getArgb(readX, readY);
                int rgb1 = pixelReader.getArgb(readX + 1, readY);
                int rgb2 = pixelReader.getArgb(readX - 1, readY);
                int rgb3 = pixelReader.getArgb(readX, readY + 1);
                int rgb4 = pixelReader.getArgb(readX, readY - 1);
                int r = (rgb & 0xff0000) >> 16;
                int r1 = (rgb1 & 0xff0000) >> 16;
                int r2 = (rgb2 & 0xff0000) >> 16;
                int r3 = (rgb3 & 0xff0000) >> 16;
                int r4 = (rgb4 & 0xff0000) >> 16;
                int g = (rgb & 0xff00) >> 8;
                int g1 = (rgb1 & 0xff00) >> 8;
                int g2 = (rgb2 & 0xff00) >> 8;
                int g3 = (rgb3 & 0xff00) >> 8;
                int g4 = (rgb4 & 0xff00) >> 8;
                int b = (rgb & 0xff);
                int b1 = (rgb1 & 0xff);
                int b2 = (rgb2 & 0xff);
                int b3 = (rgb3 & 0xff);
                int b4 = (rgb4 & 0xff);
                gm = ((r1 + g1 + b1) / 3) - ((r2 + g2 + b2) / 3);
                gn = ((r3 + g3 + b3) / 3) - ((r4 + g4 + b4) / 3);
                gFunc = Math.max(Math.abs(gm), Math.abs(gn));
                fgAccum += ((r + g + b) / 3) * gFunc;
                gAccum += gFunc;
            }
        }
        return fgAccum / gAccum;
    }

    public int[] getGrayscaleArray() {
        int[] arr = new int[(int) (image.getWidth() * image.getHeight())];
        int i = 0;
        for (int readY = 0; readY < image.getHeight(); readY++) {
            for (int readX = 0; readX < image.getWidth(); readX++) {
                int rgb = pixelReader.getArgb(readX, readY);
                int r = (rgb & 0xff0000) >> 16;
                int g = (rgb & 0xff00) >> 8;
                int b = (rgb & 0xff);
                int avg = (r + g + b) / 3;
                arr[i] = (rgb & 0xff000000) + (avg << 16) + (avg << 8) + avg;
                i++;
            }
        }
        return arr;
    }

    public WritableImage equalize() {
        BufferedImage src = SwingFXUtils.fromFXImage(image, null);
        BufferedImage nImg = new BufferedImage(src.getWidth(), src.getHeight(),
                BufferedImage.TYPE_BYTE_GRAY);
        WritableRaster wr = src.getRaster();
        WritableRaster er = nImg.getRaster();
        int totpix = wr.getWidth() * wr.getHeight();
        int[] histogram = new int[256];

        for (int x = 0; x < wr.getWidth(); x++) {
            for (int y = 0; y < wr.getHeight(); y++) {
                histogram[wr.getSample(x, y, 0)]++;
            }
        }

        int[] chistogram = new int[256];
        chistogram[0] = histogram[0];
        for (int i = 1; i < 256; i++) {
            chistogram[i] = chistogram[i - 1] + histogram[i];
        }

        float[] arr = new float[256];
        for (int i = 0; i < 256; i++) {
            arr[i] = (float) ((chistogram[i] * 255.0) / (float) totpix);
        }

        for (int x = 0; x < wr.getWidth(); x++) {
            for (int y = 0; y < wr.getHeight(); y++) {
                int nVal = (int) arr[wr.getSample(x, y, 0)];
                er.setSample(x, y, 0, nVal);
            }
        }
        nImg.setData(er);
        return convertToFxImage(nImg);
    }

    private int getMin(int[] source) {
        int min = 255;
        for (int i = 255; i >= 0; i--) {
            if (source[i] != 0) {
                min = i;
            }
        }
        return min;
    }

    private int getMax(int[] source) {
        int max = 0;
        for (int i = 0; i < 256; i++) {
            if (source[i] != 0) {
                max = i;
            }
        }
        return max;
    }

    public WritableImage gray() {
        for (int readY = 0; readY < image.getHeight(); readY++) {
            for (int readX = 0; readX < image.getWidth(); readX++) {
                int rgb = pixelReader.getArgb(readX, readY);
                int r = (rgb & 0xff0000) >> 16;
                int g = (rgb & 0xff00) >> 8;
                int b = (rgb & 0xff);
                int gray = (r * 3 + g * 4 + b * 2) / 9;  //calculate the right gray value based on r, g and b intensities
                pixelWriter.setArgb(readX, readY, (rgb & 0xff000000) + (gray << 16) + (gray << 8) + gray);
            }
        }
        return wImage;
    }

    public WritableImage noise(Float percent) {
        Random rnd = new Random();

        for (int readY = 0; readY < image.getHeight(); readY++) {
            for (int readX = 0; readX < image.getWidth(); readX++) {
                int rgb = pixelReader.getArgb(readX, readY);
                int r = (rgb & 0xff0000) >> 16;
                int g = (rgb & 0xff00) >> 8;
                int b = (rgb & 0xff);
                int RandomBrightness = (int) (rnd.nextFloat() * percent) + 100;
                r = Math.min((r * RandomBrightness) / 100, 255);
                g = Math.min((g * RandomBrightness) / 100, 255);
                b = Math.min((b * RandomBrightness) / 100, 255);
                pixelWriter.setArgb(readX, readY, (rgb & 0xff000000) + (r << 16) + (g << 8) + b);
            }
        }
        return wImage;
    }

    public WritableImage brightness(int percent) {
        for (int readY = 0; readY < image.getHeight(); readY++) {
            for (int readX = 0; readX < image.getWidth(); readX++) {
                int rgb = pixelReader.getArgb(readX, readY);
                int r = (rgb & 0xff0000) >> 16;
                int g = (rgb & 0xff00) >> 8;
                int b = (rgb & 0xff);
                r = Math.min(255, (r * percent) / 100);
                g = Math.min(255, (g * percent) / 100);
                b = Math.min(255, (b * percent) / 100);
                pixelWriter.setArgb(readX, readY, (rgb & 0xff000000) + (r << 16) + (g << 8) + b);
            }
        }
        return wImage;
    }

    public WritableImage pseudoColors(long seed) {
        Random rnd = new Random();
        rnd.setSeed(seed);
        int random = rnd.nextInt();

        for (int readY = 0; readY < image.getHeight(); readY++) {
            for (int readX = 0; readX < image.getWidth(); readX++) {
                int rgb = pixelReader.getArgb(readX, readY);
                pixelWriter.setArgb(readX, readY, (rgb & 0xff000000) + ((rgb << random) & 0xffffff));
            }
        }
        return wImage;
    }

    public WritableImage horizonalWave(double nWaves, double percent, double offset) {
        double waveFrequency = (nWaves * Math.PI * 2.0) / height;
        double waveOffset = (offset * nWaves * Math.PI * 2.0) / 100.0;
        double radius = (width * percent) / 100.0;

        int index = 0;
        for (int y = 0; y < height; y++) {
            int xOffset = (int) Math.round(Math.sin(y * waveFrequency + waveOffset) * radius);
            for (int x = 0; x < width; x++) {
                if (xOffset >= 0 && xOffset < width) {
                    Color color = pixelReader.getColor(xOffset, y);
                    pixelWriter.setColor(index % width, index / width, color);
                } else {
                    pixelWriter.setColor(index % width, index / width, bgColor);
                }
                xOffset++;
                index++;
            }
        }
        return wImage;
    }

    public WritableImage ripple(double nWaves, double percent, double offset) {
        double angleRadians = (Math.PI * 2.0 * percent) / 100.0;
        double maxDist = Math.sqrt(width * width + height * height);
        double scale = (Math.PI * 2.0 * nWaves) / maxDist;
        offset = (offset * Math.PI * 2.0) / 100.0;

        int index = 0;
        for (int y = -centerY; y < centerY; y++) {
            for (int x = -centerX; x < centerX; x++) {
                double a = Math.sin(Math.sqrt(x * x + y * y) * scale + offset) * angleRadians;
                double ca = Math.cos(a);
                double sa = Math.sin(a);

                int xs = (int) (x * ca - y * sa) + centerX;
                int ys = (int) (y * ca + x * sa) + centerY;
                if (xs >= 0 && xs < width && ys >= 0 && ys < height) {
                    Color color = pixelReader.getColor(xs, ys);
                    pixelWriter.setColor(index % width, index / width, color);
                } else {
                    pixelWriter.setColor(index % width, index / width, bgColor);
                }
                index++;
            }
        }
        return wImage;
    }

    public WritableImage transparency(int percent) {
        for (int readY = 0; readY < image.getHeight(); readY++) {
            for (int readX = 0; readX < image.getWidth(); readX++) {
                int rgb = pixelReader.getArgb(readX, readY);
                int a = (rgb >> 24) & 0xff;
                a = Math.min(255, (a * percent) / 100);
                pixelWriter.setArgb(readX, readY, (rgb & 0xffffff) + (a << 24));
            }
        }
        return wImage;
    }


    public WritableImage makeTransparent(int r, int g, int b, double percent) {
        percent *= 1.28;

        //Makes given color within the given range transparent
        int rMin = (int) (r - percent);
        int gMin = (int) (g - percent);
        int bMin = (int) (b - percent);

        int rMax = (int) (r + percent);
        int gMax = (int) (g + percent);
        int bMax = (int) (b + percent);

        for (int readY = 0; readY < image.getHeight(); readY++) {
            for (int readX = 0; readX < image.getWidth(); readX++) {
                int rgb = pixelReader.getArgb(readX, readY);
                r = (rgb & 0xff0000) >> 16;
                g = (rgb & 0xff00) >> 8;
                b = (rgb & 0xff);
                if ((r >= rMin) && (r <= rMax) && (g >= gMin) && (g <= gMax) && (b >= bMin) && (b <= bMax))
                    pixelWriter.setArgb(readX, readY, rgb & 0xffffff);
            }
        }
        return wImage;
    }

    public WritableImage lineArt(int intensity) {
        //The first line is undefined, give it the color of bgColor
        for (int readY = 0; readY < 1; readY++) {
            for (int readX = 0; readX < image.getWidth(); readX++) {
                pixelWriter.setArgb(readX, readY, 0);
            }
        }

        for (int readY = 1; readY < image.getHeight(); readY++) {
            for (int readX = 0; readX < image.getWidth(); readX++) {
                //Read pixel to the left
                int rgb1 = 0;
                if (readX > 0)
                    rgb1 = pixelReader.getArgb(readX - 1, readY);
                else
                    rgb1 = pixelReader.getArgb(readX, readY);
                int r1 = (rgb1 & 0xff0000) >> 16;
                int g1 = (rgb1 & 0xff00) >> 8;
                int b1 = (rgb1 & 0xff);

                //Read pixel above
                int rgb2 = pixelReader.getArgb(readX, readY - 1);
                int r2 = (rgb2 & 0xff0000) >> 16;
                int g2 = (rgb2 & 0xff00) >> 8;
                int b2 = (rgb2 & 0xff);

                //Read current pixel
                int rgb = pixelReader.getArgb(readX, readY);
                int r = (rgb & 0xff0000) >> 16;
                int g = (rgb & 0xff00) >> 8;
                int b = (rgb & 0xff);

                r = Math.min((Math.abs(r2 - r) + Math.abs(r1 - r)) * intensity, 255);
                g = Math.min((Math.abs(g2 - g) + Math.abs(g1 - g)) * intensity, 255);
                b = Math.min((Math.abs(b2 - b) + Math.abs(b1 - b)) * intensity, 255);

                pixelWriter.setArgb(readX, readY, (rgb & 0xff000000) + (r << 16) + (g << 8) + b);
            }
        }
        return wImage;
    }

    public WritableImage graylineArt(int intensity) {
        //The first line is undefined, give it the color of bgColor
        for (int readY = 0; readY < 1; readY++) {
            for (int readX = 0; readX < image.getWidth(); readX++) {
                pixelWriter.setArgb(readX, readY, 0);
            }
        }

        for (int readY = 1; readY < image.getHeight(); readY++) {
            for (int readX = 0; readX < image.getWidth(); readX++) {
                //Read pixel to the left
                int rgb1 = 0;
                if (readX > 0)
                    rgb1 = pixelReader.getArgb(readX - 1, readY);
                else
                    rgb1 = pixelReader.getArgb(readX, readY);
                int r1 = (rgb1 & 0xff0000) >> 16;
                int g1 = (rgb1 & 0xff00) >> 8;
                int b1 = (rgb1 & 0xff);
                int gray = (r1 * 3 + g1 * 4 + b1 * 2) / 9;  //calculate the right gray value based on r, g and b intensities
                r1 = gray;
                g1 = gray;
                b1 = gray;

                //Read pixel above
                int rgb2 = pixelReader.getArgb(readX, readY - 1);
                int r2 = (rgb2 & 0xff0000) >> 16;
                int g2 = (rgb2 & 0xff00) >> 8;
                int b2 = (rgb2 & 0xff);
                gray = (r2 * 3 + g2 * 4 + b2 * 2) / 9;  //calculate the right gray value based on r, g and b intensities
                r2 = gray;
                g2 = gray;
                b2 = gray;

                //Read current pixel
                int rgb = pixelReader.getArgb(readX, readY);
                int r = (rgb & 0xff0000) >> 16;
                int g = (rgb & 0xff00) >> 8;
                int b = (rgb & 0xff);
                gray = (r * 3 + g * 4 + b * 2) / 9;  //calculate the right gray value based on r, g and b intensities
                r = gray;
                g = gray;
                b = gray;

                r = Math.min((Math.abs(r2 - r) + Math.abs(r1 - r)) * intensity, 255);
                g = Math.min((Math.abs(g2 - g) + Math.abs(g1 - g)) * intensity, 255);
                b = Math.min((Math.abs(b2 - b) + Math.abs(b1 - b)) * intensity, 255);

                pixelWriter.setArgb(readX, readY, (rgb & 0xff000000) + (r << 16) + (g << 8) + b);
            }
        }
        return wImage;
    }

    public WritableImage emboss(double angle, double power, int red, int green, int blue) {
        double angleRadians = angle / (180.0 / Math.PI);
        int light = (int) (Math.round(Math.sin(angleRadians)) * width) + (int) (Math.round(Math.cos(angleRadians))) - 1;

        for (int readY = 1; readY < height; readY++) {
            for (int readX = 0; readX < width; readX++) {

                //Read current pixel
                int rgb1 = pixelReader.getArgb(readX, readY);
                int r1 = (rgb1 & 0xff0000) >> 16;
                int g1 = (rgb1 & 0xff00) >> 8;
                int b1 = (rgb1 & 0xff);

                //Read pixel in the direction given by angle
                int index = readX + readY * width;
                int rgb2 = pixelReader.getArgb((index - light) % width, (index - light) / width);
                int r2 = (rgb2 & 0xff0000) >> 16;
                int g2 = (rgb2 & 0xff00) >> 8;
                int b2 = (rgb2 & 0xff);

                int r = Math.min(Math.max(red + (int) ((r2 - r1) * power), 0), 255);
                int g = Math.min(Math.max(green + (int) ((g2 - g1) * power), 0), 255);
                int b = Math.min(Math.max(blue + (int) ((b2 - b1) * power), 0), 255);

                pixelWriter.setArgb(readX, readY, (rgb1 & 0xff000000) + (r << 16) + (g << 8) + b);
            }
        }

        //Borders are undefined, fill with specified color
        int color = (red << 16) + (green << 8) + blue;

        for (int readX = 0; readX < width; readX++) {
            int rgb1 = pixelReader.getArgb(readX, 0);
            pixelWriter.setArgb(readX, 0, (rgb1 & 0xff000000) + color);
            int rgb2 = pixelReader.getArgb(readX, height - 1);
            pixelWriter.setArgb(readX, height - 1, (rgb2 & 0xff000000) + color);
        }

        for (int readY = 0; readY < height; readY++) {
            int rgb1 = pixelReader.getArgb(0, readY);
            pixelWriter.setArgb(0, readY, (rgb1 & 0xff000000) + color);
            int rgb2 = pixelReader.getArgb(width - 1, readY);
            pixelWriter.setArgb(width - 1, readY, (rgb2 & 0xff000000) + color);
        }
        return wImage;
    }

    public WritableImage grayemboss(double angle, double power, int red, int green, int blue) {
        double angleRadians = angle / (180.0 / Math.PI);
        int light = (int) (Math.round(Math.sin(angleRadians)) * width) + (int) (Math.round(Math.cos(angleRadians))) - 1;

        for (int readY = 1; readY < height; readY++) {
            for (int readX = 0; readX < width; readX++) {

                //Read current pixel
                int rgb1 = pixelReader.getArgb(readX, readY);
                int r1 = (rgb1 & 0xff0000) >> 16;
                int g1 = (rgb1 & 0xff00) >> 8;
                int b1 = (rgb1 & 0xff);
                int gray = (r1 * 3 + g1 * 4 + b1 * 2) / 9;  //calculate the right gray value based on r, g and b intensities
                r1 = gray;
                g1 = gray;
                b1 = gray;

                //Read pixel in the direction given by angle
                int index = readX + readY * width;
                int rgb2 = pixelReader.getArgb((index - light) % width, (index - light) / width);
                int r2 = (rgb2 & 0xff0000) >> 16;
                int g2 = (rgb2 & 0xff00) >> 8;
                int b2 = (rgb2 & 0xff);
                gray = (r2 * 3 + g2 * 4 + b2 * 2) / 9;  //calculate the right gray value based on r, g and b intensities
                r2 = gray;
                g2 = gray;
                b2 = gray;

                int r = Math.min(Math.max(red + (int) ((r2 - r1) * power), 0), 255);
                int g = Math.min(Math.max(green + (int) ((g2 - g1) * power), 0), 255);
                int b = Math.min(Math.max(blue + (int) ((b2 - b1) * power), 0), 255);

                pixelWriter.setArgb(readX, readY, (rgb1 & 0xff000000) + (r << 16) + (g << 8) + b);
            }
        }

        //Borders are undefined, fill with specified color
        int color = (red << 16) + (green << 8) + blue;

        for (int readX = 0; readX < width; readX++) {
            int rgb1 = pixelReader.getArgb(readX, 0);
            pixelWriter.setArgb(readX, 0, (rgb1 & 0xff000000) + color);
            int rgb2 = pixelReader.getArgb(readX, height - 1);
            pixelWriter.setArgb(readX, height - 1, (rgb2 & 0xff000000) + color);
        }

        for (int readY = 0; readY < height; readY++) {
            int rgb1 = pixelReader.getArgb(0, readY);
            pixelWriter.setArgb(0, readY, (rgb1 & 0xff000000) + color);
            int rgb2 = pixelReader.getArgb(width - 1, readY);
            pixelWriter.setArgb(width - 1, readY, (rgb2 & 0xff000000) + color);
        }
        return wImage;
    }

    public WritableImage zoom(int percent) {
        int bg = 0;

        int index = 0;
        for (int y = -centerY; y < centerY; y++) {
            for (int x = -centerX; x < centerX; x++) {
                int xs = (x * percent) / 100 + centerX;
                int ys = (y * percent) / 100 + centerY;
                index++;
                if (index >= width * height)
                    break;
                if (xs >= 0 && xs < width && ys >= 0 && ys < height) {
                    int rgb = pixelReader.getArgb(xs, ys);
                    pixelWriter.setArgb(index % width, index / width, rgb);
                } else if (index < width * height) {
                    pixelWriter.setArgb(index % width, index / width, bg);
                }
            }
        }
        return wImage;
    }

    private static WritableImage convertToFxImage(BufferedImage image) {
        WritableImage wr = null;
        if (image != null) {
            wr = new WritableImage(image.getWidth(), image.getHeight());
            PixelWriter pw = wr.getPixelWriter();
            for (int x = 0; x < image.getWidth(); x++) {
                for (int y = 0; y < image.getHeight(); y++) {
                    pw.setArgb(x, y, image.getRGB(x, y));
                }
            }
        }
        return wr;
    }

}