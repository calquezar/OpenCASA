package gui;

import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import data.Spermatozoon;
import functions.ComputerVision;
import functions.Paint;
import functions.VideoRecognition;
import ij.ImagePlus;

public class ViabilityWindow extends ImageAnalysisWindow implements ChangeListener{

  private boolean         isThresholding  = false;
  
  public ViabilityWindow() {
    super();
    setChangeListener(this);
  }

  /**
   * This method refreshes the showed image after changing the threshold with
   * the sliderbar
   */
  private void doSliderRefresh() {
    if (!isThresholding) {
      isThresholding = true;
      Thread t1 = new Thread(new Runnable() {
        public void run() {
          processImage(true);
          isThresholding = false;
        }
      });
      t1.start();
    }
  }
  protected void processImage(boolean eventType){
    if (eventType || threshold == -1) {// If true, the threshold has changed or
      // it needs to be calculated
      ComputerVision cv = new ComputerVision();
      impTh = impOrig.duplicate();
      cv.convertToGrayscale(impTh);
      impGray = impTh.duplicate();
      thresholdImagePlus(impTh);
      // Update sliderbar with the new threshold
      sldThreshold.setValue((int) threshold);
      VideoRecognition vr = new VideoRecognition();
      List<Spermatozoon>[] sperm = vr.detectSpermatozoa(impTh);
      if (sperm != null)
      spermatozoa = sperm[0];
      // Calculate outlines
      impOutline = impTh.duplicate();
      cv.outlineThresholdImage(impOutline);
      idenfitySperm();
      }
      impDraw = impOrig.duplicate();
      Paint paint = new Paint();
      paint.drawOutline(impDraw, impOutline);
      paint.drawBoundaries(impDraw, spermatozoa);
      setImage();    
  }
  
  @Override
  public void stateChanged(ChangeEvent e) {
    Object auxWho = e.getSource();
    if ((auxWho == sldThreshold)) {
      // Updating threshold value from slider
      threshold = sldThreshold.getValue();
      doSliderRefresh();
    }
  }
  
}
