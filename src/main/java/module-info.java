module de.vwsoft.barcodelib4j {

  requires java.desktop;                       // AWT, Graphics2D, ImageIO & Stuff

  exports de.vwsoft.barcodelib4j.image;
  exports de.vwsoft.barcodelib4j.oned;
  exports de.vwsoft.barcodelib4j.twod;
  exports de.vwsoft.barcodelib4j.twod.aztec;
  exports de.vwsoft.barcodelib4j.twod.datamatrix;
  exports de.vwsoft.barcodelib4j.twod.pdf417;
  exports de.vwsoft.barcodelib4j.twod.qrcode;
  exports de.vwsoft.barcodelib4j.twod.zxing;
  exports de.vwsoft.barcodelib4j.twod.zxing.common;

}
