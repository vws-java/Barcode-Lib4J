/*
 * Copyright 2023 by Viktor Wedel, https://www.vwsoft.de/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package twodcode;
import de.vwsoft.barcode.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import twodcode.aztec.*;
import twodcode.common.*;
import twodcode.datamatrix.*;
import twodcode.pdf417.*;
import twodcode.qrcode.*;


public class TwoDCode implements Cloneable {
  private static String DEFAULT_CONTENT = "ABCDEFG... 012345";

  public static final int QRCODE = 0;
  public static final int DATAMATRIX = 1;
  public static final int PDF417 = 2;
  public static final int AZTEC = 3;
  public static final int GS1_QRCODE = 4;
  public static final int GS1_DATAMATRIX = 5;
  private static final String[] CODE_NAMES =
      { "QR Code", "DataMatrix", "PDF 417", "Aztec", "GS1 QR Code", "GS1 DataMatrix" };

  private static SymbolInfo[] DM_FORMATS = SymbolInfo.PROD_SYMBOLS;
  static {
    Arrays.sort(DM_FORMATS, (SymbolInfo s1, SymbolInfo s2) -> {
      int wDiff = s1.getSymbolWidth() - s2.getSymbolWidth();
      return wDiff != 0 ? wDiff : s1.getSymbolHeight() - s2.getSymbolHeight();
    });
  }

  // common properties
  public int pType = QRCODE;
  public String pContent = DEFAULT_CONTENT;
  public boolean pHasQuietZones = true;
  public int pQuietZone = getMinQuietZone(pType);
  public boolean pIsModuleSet;
  public double pModuleSize;
  public String pCharset = "UTF-8";
  public boolean pAutoComplete; // not in use
  public Color pForegroundColor = Color.BLACK;
  public boolean pIsOpaque;
  public Color pBackgroundColor = Color.WHITE;

  // qr code properties
  public int pQRVersion = 0; // 1 to 40, 0 = automatic
  public int pQRErrCorr = 0; // corresponds to "L", which is also the default value in QRCodeWriter

  // datamatrix properties
  public int pDMSize = 0; // automatic
  public int pDMShape = 0; // corresponds to SymbolShapeHint.FORCE_NONE

  // pdf417 properties
  public java.awt.Dimension pPDSize = new java.awt.Dimension(0, 0); // 0 = "automatic" in each case
  public int pPDErrCorr = 2; // corresponds to PDF417Writer.DEFAULT_ERROR_CORRECTION_LEVEL

  // aztec properties
  public int pAZLayers = 0; // automatic
  public int pAZErrCorr = 23; // %

  private transient ArrayList<Rectangle> myCodeChunks;
  private transient java.awt.Dimension myCodeSize;


  //----
  public TwoDCode() {
  }


  //----
  public static void setDefaultContent(String s) {
    DEFAULT_CONTENT = s;
  }


  //----
  public static String[] getCodeNames() {
    return Arrays.copyOf(CODE_NAMES, CODE_NAMES.length);
  }


  //----
  public static String getCodeName(int codeType) {
    return CODE_NAMES[codeType];
  }


  //----
  public static int getMinQuietZone(int codeType) {
    switch (codeType) {
      case QRCODE:     case GS1_QRCODE:     return 1;
      case DATAMATRIX: case GS1_DATAMATRIX: return 1;
      case PDF417:                          return 2;
      case AZTEC:
    }
    return 0;
  }


  //----
  private static ErrorCorrectionLevel getQRErrorCorrectionLevel(int index) {
    switch (index) {
      case 0: return ErrorCorrectionLevel.L;
      case 1: return ErrorCorrectionLevel.M;
      case 2: return ErrorCorrectionLevel.Q;
    }
    return ErrorCorrectionLevel.H;
  }


  //----
  public static ArrayList<java.awt.Dimension> getDMAllAvailableSizes() {
    ArrayList<java.awt.Dimension> d = new ArrayList<>(DM_FORMATS.length);
    for (SymbolInfo s : DM_FORMATS)
      d.add(new java.awt.Dimension(s.getSymbolWidth(), s.getSymbolHeight()));
    return d;
  }


  //----
  private static SymbolShapeHint getDMSymbolShapeHint(int index) {
    if      (index == 1) return SymbolShapeHint.FORCE_SQUARE;
    else if (index == 2) return SymbolShapeHint.FORCE_RECTANGLE;
    return SymbolShapeHint.FORCE_NONE;
  }


  //----
  @SuppressWarnings("deprecation")
  public void update() {
    String content = pContent;
    Writer writer = null;
    BarcodeFormat barcodeFormat = null;
    Map<EncodeHintType,Object> hints = new HashMap<>(5);
    hints.put(EncodeHintType.MARGIN, 0);
    hints.put(EncodeHintType.CHARACTER_SET, pCharset); // not supported by DataMatrix

    if (pType == QRCODE || pType == GS1_QRCODE) {
      writer = new QRCodeWriter();
      barcodeFormat = BarcodeFormat.QR_CODE;
      if (pQRVersion > 0)
        hints.put(EncodeHintType.QR_VERSION, pQRVersion);
      hints.put(EncodeHintType.ERROR_CORRECTION, getQRErrorCorrectionLevel(pQRErrCorr));
    } else if (pType == DATAMATRIX || pType == GS1_DATAMATRIX) {
      writer = new DataMatrixWriter();
      barcodeFormat = BarcodeFormat.DATA_MATRIX;
      if (pDMSize > 0) {
        SymbolInfo s = DM_FORMATS[pDMSize - 1];
        twodcode.Dimension dim = new twodcode.Dimension(s.getSymbolWidth(), s.getSymbolHeight());
        hints.put(EncodeHintType.MIN_SIZE, dim);
        hints.put(EncodeHintType.MAX_SIZE, dim);
      } else {
        hints.put(EncodeHintType.DATA_MATRIX_SHAPE, getDMSymbolShapeHint(pDMShape));
      }
    } else if (pType == PDF417) {
      writer = new PDF417Writer();
      barcodeFormat = BarcodeFormat.PDF_417;
      int minCols = pPDSize.width > 0 ? pPDSize.width : 1;
      int maxCols = pPDSize.width > 0 ? pPDSize.width : 30;
      int minRows = pPDSize.height > 0 ? pPDSize.height : 3;
      int maxRows = pPDSize.height > 0 ? pPDSize.height : 90;
      hints.put(EncodeHintType.PDF417_DIMENSIONS, new Dimensions(minCols,maxCols,minRows,maxRows));
      hints.put(EncodeHintType.ERROR_CORRECTION, pPDErrCorr);
    } else if (pType == AZTEC) {
      writer = new AztecWriter();
      barcodeFormat = BarcodeFormat.AZTEC;
      hints.put(EncodeHintType.AZTEC_LAYERS, pAZLayers);
      hints.put(EncodeHintType.ERROR_CORRECTION, pAZErrCorr);
    }

    if (pType == GS1_QRCODE || pType == GS1_DATAMATRIX) {
      try {
        content = content.replace("\n", ""); // we accept input with line breaks in case of gs1
        content = new GS1(content, (char)29).getNumber().substring(1);
        hints.put(EncodeHintType.GS1_FORMAT, true);
      } catch (Exception e) {
        content = null;
      }
    }

    BitMatrix bitMatrix = null;
    if (content != null)
      try { bitMatrix = writer.encode(content, barcodeFormat, 0, 0, hints); } catch (Exception e) {}

    if (bitMatrix == null) {
      myCodeChunks = null;
      myCodeSize = null;
    } else {
      int w = bitMatrix.getWidth(), h = bitMatrix.getHeight();
      int quietZone = pHasQuietZones ? pQuietZone : 0;
      myCodeSize = new java.awt.Dimension(w + quietZone * 2, h + quietZone * 2);
      myCodeChunks = new ArrayList<Rectangle>(100);

      // horizontal chunks
      for (int j=0; j<h; j++) {
        int positionCounter = quietZone, widthCounter = 0;
        boolean barOrSpace = bitMatrix.get(0, j);
        for (int i=0; i<w; i++) {
          boolean bit = bitMatrix.get(i, j);
          if (bit != barOrSpace) {
            if (!bit)
              myCodeChunks.add(new Rectangle(positionCounter, j + quietZone, widthCounter, 1));
            barOrSpace = bit;
            positionCounter += widthCounter;
            widthCounter = 1;
          } else {
            widthCounter++;
          }
        }
        // ... and the last one ...
        if (bitMatrix.get(w - 1, j))
          myCodeChunks.add(new Rectangle(positionCounter, j + quietZone, widthCounter, 1));
      }

      // vertical chunks, this time without detection of 1x1-sized chunks (see "heightCounter > 1")
      for (int j=0; j<w; j++) {
        int positionCounter = quietZone, heightCounter = 0;
        boolean barOrSpace = bitMatrix.get(j, 0);
        for (int i=0; i<h; i++) {
          boolean bit = bitMatrix.get(j, i);
          if (bit != barOrSpace) {
            if (!bit && heightCounter > 1)
              myCodeChunks.add(new Rectangle(j + quietZone, positionCounter, 1, heightCounter));
            barOrSpace = bit;
            positionCounter += heightCounter;
            heightCounter = 1;
          } else {
            heightCounter++;
          }
        }
        // ... and the last one ...
        if (bitMatrix.get(j, h - 1))
          myCodeChunks.add(new Rectangle(j + quietZone, positionCounter, 1, heightCounter));
      }

    }
  }


  //----
  public boolean isValid() {
    return myCodeChunks != null;
  }


  //----
  public void draw(Graphics2D g2d, Rectangle2D.Double r) {
    draw(g2d, r, 0.0, 0.0, 0.0);
  }


  //----
  public void draw(Graphics2D g2d, Rectangle2D.Double r, double dotSize) {
    draw(g2d, r, dotSize, 0.0, 0.0);
  }


  //----
  public void draw(Graphics2D g2d, Rectangle2D.Double r, double dotSize,
      double horBarWidthCorrection, double verBarWidthCorrection) {
    if (!isValid())
      return;

    double moduleSize = pIsModuleSet ? pModuleSize :
        Math.min(r.width / myCodeSize.width, r.height / myCodeSize.height);
    if (dotSize > 0.0)
      moduleSize = (int)(moduleSize / dotSize) * dotSize;

    double spareWidth = r.width - moduleSize * myCodeSize.width;
    double spareHeight = r.height - moduleSize * myCodeSize.height;
    r = new Rectangle2D.Double(r.x + spareWidth / 2.0, r.y + spareHeight / 2.0, r.width, r.height);

    if (pIsOpaque) {
      r.width -= spareWidth;
      r.height -= spareHeight;
      g2d.setColor(pBackgroundColor);
      g2d.fill(r);
    }

    double horBWC2 = horBarWidthCorrection * 2.0;
    double verBWC2 = verBarWidthCorrection * 2.0;
    double shiftedX = r.x - horBarWidthCorrection;
    double shiftedY = r.y - verBarWidthCorrection;
    Rectangle2D.Double chunk = new Rectangle2D.Double();
    g2d.setColor(pForegroundColor);
    for (int i=myCodeChunks.size()-1; i>=0; i--) {
      Rectangle p = myCodeChunks.get(i);
      chunk.x = shiftedX + p.x * moduleSize;
      chunk.y = shiftedY + p.y * moduleSize;
      chunk.width = p.width * moduleSize + horBWC2;
      chunk.height = p.height * moduleSize + verBWC2;
      g2d.fill(chunk);
    }
  }


  //----
  public Object clone() {
    try { return super.clone(); } catch (Exception e) { return null; }
  }

}

