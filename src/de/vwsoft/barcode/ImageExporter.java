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

package de.vwsoft.barcode;
import de.vwsoft.common.awt.BitmapImageIO;
import de.vwsoft.common.awt.CompoundColor;
import com.lowagie.text.pdf.*;
import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.awt.image.renderable.*;
import java.io.*;
import java.nio.charset.*;
import java.text.*;
import java.util.*;


public class ImageExporter {
  public static final int FORMAT_PDF = 0;
  public static final int FORMAT_EPS = 1;
  public static final int FORMAT_SVG = 2;
  public static final int FORMAT_PNG = 3;
  public static final int FORMAT_BMP = 4;
  public static final int FORMAT_JPG = 5;

  public static final int COLORSPACE_RGB  = 0;
  public static final int COLORSPACE_CMYK = 1;

  public static final int TRANSFORM_0    = 0;
  public static final int TRANSFORM_90   = 1;
  public static final int TRANSFORM_180  = 2;
  public static final int TRANSFORM_270  = 3;
  public static final int TRANSFORM_0N   = 4;
  public static final int TRANSFORM_90N  = 5;
  public static final int TRANSFORM_180N = 6;
  public static final int TRANSFORM_270N = 7;

  // for 'eps' and 'svg'; round to max. 6 decimal places
  private static final DecimalFormat DEC_FORMAT = // second parameter ensures '.' as separator
      new DecimalFormat("#.######", new DecimalFormatSymbols(Locale.US));

  private BarcodeGraphics2D myGraphics2D = new BarcodeGraphics2D();
  private Point2D.Double mySize;
  private String myTitle;
  private String myCreator;
  private boolean myIsOpaque = true;
  private CompoundColor myForeground = CompoundColor.CC_BLACK;
  private CompoundColor myBackground = CompoundColor.CC_WHITE;
  private int myTransform = TRANSFORM_0;
  private int myTiffRes;


  //----
  public ImageExporter(double widthMM, double heightMM) {
    mySize = new Point2D.Double(widthMM, heightMM);
  }


  //----
  public Graphics2D getGraphics2D() {
    return myGraphics2D;
  }


  //---- supported by vector formats only.
  // be sure all characters in the title are supported by the chosen file format.
  public void setTitle(String title) {
    myTitle = title;
  }


  //---- sets "Creator" and "Producer" metadata in pdf files at once.
  public void setCreator(String creator) {
    myCreator = creator;
  }


  //---- transparent background is supported by: pdf, eps, svg, png
  public void setOpaque(boolean opaque) {
    myIsOpaque = opaque;
  }


  //----
  public void setForeground(CompoundColor c) {
    myForeground = c;
  }


  //----
  public void setBackground(CompoundColor c) {
    myBackground = c;
  }


  //---- one of the above constants, default: TRANSFORM_0
  public void setTransform(int transform) {
    myTransform = transform;
  }


  //---- eps only: sets resolution of the embedded tiff file.
  // 0 (default) = no preview will be embedded.
  public void setTiffRes(int dpiRes) {
    myTiffRes = dpiRes;
  }


  //---- dpiResX, dpiResY - required for bitmap image formats
  public void write(OutputStream out, int format, int colorSpace, int dpiResX, int dpiResY)
      throws IOException {
    if (format == FORMAT_PDF)
      writePDF(out, colorSpace);
    else if (format == FORMAT_EPS)
      writeEPS(out, colorSpace);
    else if (format == FORMAT_SVG)
      writeSVG(out);
    else {
      BufferedImage img = createBufferedImage(dpiResX, dpiResY, format, myForeground, myBackground);
      if (format == FORMAT_PNG)
        BitmapImageIO.writePNG(img, out, dpiResX, dpiResY);
      else if (format == FORMAT_BMP)
        BitmapImageIO.writeBMP(img, out, dpiResX, dpiResY);
      else
        BitmapImageIO.writeJPG(img, out, dpiResX, dpiResY, 1F);
    }
  }


  //----
  public void writePNG(OutputStream out, int dpiResX, int dpiResY) throws IOException {
    write(out, FORMAT_PNG, COLORSPACE_RGB, dpiResX, dpiResY);
  }
  public void writeBMP(OutputStream out, int dpiResX, int dpiResY) throws IOException {
    write(out, FORMAT_BMP, COLORSPACE_RGB, dpiResX, dpiResY);
  }
  public void writeJPG(OutputStream out, int dpiResX, int dpiResY) throws IOException {
    write(out, FORMAT_JPG, COLORSPACE_RGB, dpiResX, dpiResY);
  }


  //----
  public void writePDF(OutputStream out, int colorSpace) throws IOException {
    boolean oldPrecision = ByteBuffer.HIGH_PRECISION;
    ByteBuffer.HIGH_PRECISION = true; // 6 digits after decimal point instead of 2

    final double scale = 72.0 / 25.4; // mm to 1/72 inch
    Point2D.Double size = getMySize();
    Point2D.Float docSize = new Point2D.Float((float)(size.x * scale), (float)(size.y * scale));

    com.lowagie.text.Rectangle rect = new com.lowagie.text.Rectangle(docSize.x, docSize.y);
    if (myIsOpaque)
      rect.setBackgroundColor(getColorForPDF(myBackground, colorSpace));
    com.lowagie.text.Document document = new com.lowagie.text.Document(rect);

    PdfWriter writer = PdfWriter.getInstance(document, out);
    writer.setCompressionLevel(PdfStream.BEST_COMPRESSION);
    writer.setPDFXConformance(colorSpace == COLORSPACE_CMYK ?
        PdfWriter.PDFX1A2001 : PdfWriter.PDFX32002);

    document.open();
    if (myTitle != null)
      document.addTitle(myTitle);
    if (myCreator != null) {
      document.addCreator(myCreator);
      document.addProducer(myCreator);
    }
    document.addSubject(colorSpace == COLORSPACE_CMYK ?
        "PDF/X-1a:2001, CMYK colors" : "PDF/X-3:2002, RGB colors");

    PdfGraphics2D g2d = new PdfGraphics2D(writer.getDirectContent(), docSize.x, docSize.y,
        null, true, false, 1F);

    AffineTransform at = AffineTransform.getScaleInstance(scale, scale);
    at.concatenate(getMyTransform());
    g2d.setTransform(at);

    g2d.setColor(getColorForPDF(myForeground, colorSpace));
    g2d.fill(myGraphics2D.getAllShapes());

    g2d.dispose();

    // by default pdf readers should NOT scale the document when printing
    writer.addViewerPreference(PdfName.PRINTSCALING, PdfName.NONE);
    // set zoom to 100% when opening the document (otherwise it fits to window)
    PdfDestination pdfDest = new PdfDestination(PdfDestination.XYZ, 0F, docSize.y, 1F);
    writer.setOpenAction(PdfAction.gotoLocalPage(1, pdfDest, writer));

    document.close();

    writer.close();

    ByteBuffer.HIGH_PRECISION = oldPrecision; // set static variable back to former value
  }


  //----
  private static Color getColorForPDF(CompoundColor cc, int colorSpace) {
    return colorSpace == COLORSPACE_RGB ? cc :
        new CMYKColor(cc.getCyan()   / 100F, cc.getMagenta() / 100F,
                      cc.getYellow() / 100F, cc.getKey()     / 100F);
  }


  //----
  public void writeEPS(OutputStream out, int colorSpace) throws IOException {
    if (myTiffRes <= 0) {
      writePureEPS(out, colorSpace);
      return;
    }

    ByteArrayOutputStream epsArray = new ByteArrayOutputStream(10_000);
    writePureEPS(epsArray, colorSpace);

    ByteArrayOutputStream tiffArray = new ByteArrayOutputStream(10_000);
    BitmapImageIO.writeTIFF(createBufferedImage(
        myTiffRes, myTiffRes, FORMAT_EPS, myForeground, myBackground), tiffArray);

    DataOutputStream dos = new DataOutputStream(out);
    dos.writeInt(-976170042); // 4 bytes "magic number"
    dos.writeInt(Integer.reverseBytes(30 + tiffArray.size()));
    dos.writeInt(Integer.reverseBytes(epsArray.size()));
    dos.writeInt(0);
    dos.writeInt(0);
    dos.writeInt(Integer.reverseBytes(30));
    dos.writeInt(Integer.reverseBytes(tiffArray.size()));
    dos.writeShort(0xFFFF);

    tiffArray.writeTo(dos);
    epsArray.writeTo(dos);
  }


  //---- creates an eps file without an embedded tiff-preview
  private void writePureEPS(OutputStream out, int colorSpace) throws IOException {
    final double scale = 72.0 / 25.4; // mm to 1/72 inch
    final Point2D.Double size = getMySize();
    final Point2D.Double pageSize = new Point2D.Double(size.x * scale, size.y * scale);
    final String br = "\n";
    final StringBuilder sb = new StringBuilder(10_000);

    apd(sb, "%!PS-Adobe-3.0 EPSF-3.0", br);
    if (myTitle != null)
      apd(sb, "%%Title: ", myTitle, br);
    apd(sb, "%%HiResBoundingBox: 0 0 ", pageSize.x, ' ', pageSize.y, br);

    apd(sb, "/l {lineto} bind def", br);
    apd(sb, "/m {moveto} bind def", br);
    apd(sb, "/c {curveto} bind def", br);

    if (myIsOpaque) {
      apd(sb, getColorAsPostScriptCommand(myBackground, colorSpace), br);
      apd(sb, "0 0 ", pageSize.x, ' ', pageSize.y, " rectfill", br);
    }

    apd(sb, getColorAsPostScriptCommand(myForeground, colorSpace), br);

    final double[] d = new double[6];
    final double[] lastPoint = new double[2];
    final double[] controlPoint = new double[4];
    AffineTransform at = AffineTransform.getTranslateInstance(0.0, pageSize.y);
    at.scale(scale, -scale);
    at.concatenate(getMyTransform());
    PathIterator pathIterator = myGraphics2D.getAllShapes().getPathIterator(at);
    while (!pathIterator.isDone()) {
      switch (pathIterator.currentSegment(d)) {
        case PathIterator.SEG_MOVETO:
          apd(sb, d[0], ' ', d[1], " m", br);
          lastPoint[0] = d[0];
          lastPoint[1] = d[1];
          break;
        case PathIterator.SEG_LINETO:
          apd(sb, d[0], ' ', d[1], " l", br);
          lastPoint[0] = d[0];
          lastPoint[1] = d[1];
          break;
        case PathIterator.SEG_QUADTO:
          controlPoint[0] = d[0] + (lastPoint[0] - d[0]) / 3.0;
          controlPoint[1] = d[1] + (lastPoint[1] - d[1]) / 3.0;
          controlPoint[2] = d[0] + (d[2] - d[0]) / 3.0;
          controlPoint[3] = d[1] + (d[3] - d[1]) / 3.0;
          apd(sb, controlPoint[0], ' ', controlPoint[1], ' ', controlPoint[2], ' ',
              controlPoint[3], ' ', d[2], ' ', d[3], " c", br);
          lastPoint[0] = d[2];
          lastPoint[1] = d[3];
          break;
        case PathIterator.SEG_CUBICTO:
          apd(sb, d[0], ' ', d[1], ' ', d[2], ' ', d[3], ' ', d[4], ' ', d[5], " c", br);
          lastPoint[0] = d[4];
          lastPoint[1] = d[5];
          break;
        case PathIterator.SEG_CLOSE: // nothing
      }
      pathIterator.next();
    }
    apd(sb, "fill", br);

    OutputStreamWriter osw = new OutputStreamWriter(out, StandardCharsets.US_ASCII);
    osw.write(sb.toString());
    osw.flush();
  }


  //----
  private static String getColorAsPostScriptCommand(CompoundColor cc, int colorSpace) {
    return colorSpace == COLORSPACE_RGB ?
        DEC_FORMAT.format(cc.getRed()   / 255.0) + " " +
        DEC_FORMAT.format(cc.getGreen() / 255.0) + " " +
        DEC_FORMAT.format(cc.getBlue()  / 255.0) + " setrgbcolor" :
        (cc.getCyan()   / 100F) + " " + (cc.getMagenta() / 100F) + " " +
        (cc.getYellow() / 100F) + " " + (cc.getKey()     / 100F) + " setcmykcolor";
  }


  //----
  public void writeSVG(OutputStream out) throws IOException {
    final String br = "\n";
    final StringBuilder sb = new StringBuilder(10_000);
    final Point2D.Double size = getMySize();

    apd(sb, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>", br);
    apd(sb, "<svg version=\"1.1\" baseProfile=\"tiny\" xmlns=\"http://www.w3.org/2000/svg\" ",
        "width=\"", size.x, "mm\" height=\"", size.y, "mm\" ",
        "viewBox=\"0 0 ", size.x, ' ', size.y, "\">", br);
    if (myTitle != null)
      apd(sb, "<title>", escapeHtml4(myTitle), "</title>", br);

    apd(sb, "<g>", br);
    apd(sb, "<rect ", (myIsOpaque ? "fill=\"" + getColorAsHexString(myBackground) : "opacity=\"0"),
        "\" width=\"", size.x, "\" height=\"", size.y, "\"/>", br);

    apd(sb, "<path fill=\"", getColorAsHexString(myForeground), "\" d=\"");
    final double[] d = new double[6];
    PathIterator pathIterator = myGraphics2D.getAllShapes().getPathIterator(getMyTransform());
    while (!pathIterator.isDone()) {
      switch (pathIterator.currentSegment(d)) {
        case PathIterator.SEG_MOVETO : apd(sb, 'M', d[0], ',', d[1]);
          break;
        case PathIterator.SEG_LINETO : apd(sb, 'L', d[0], ',', d[1]);
          break;
        case PathIterator.SEG_QUADTO : apd(sb, 'Q', d[0], ',', d[1], ' ', d[2], ',', d[3]);
          break;
        case PathIterator.SEG_CUBICTO: apd(sb, 'C', d[0], ',', d[1], ' ', d[2], ',', d[3], ' ',
            d[4], ',', d[5]);
          break;
        case PathIterator.SEG_CLOSE  : /* apd(sb, 'Z'); */
      }
      pathIterator.next();
    }
    apd(sb, "\"/>", br);

    apd(sb, "</g>", br);

    apd(sb, "</svg>", br);

    OutputStreamWriter osw = new OutputStreamWriter(out, StandardCharsets.UTF_8);
    osw.write(sb.toString());
    osw.flush();
  }


  //----
  private static String[] HTML4_ESC_CHARS = { "<", ">", "\"", "'", "&" };
  private static String[] HTML4_ESC_ENTITIES = { "&lt;", "&gt;", "&quot;", "&apos;", "&amp;" };
  public static String escapeHtml4(String s) {
    for (int i=4; i>=0; i--) // order is important! '&' must be replaced first
      s = s.replace(HTML4_ESC_CHARS[i], HTML4_ESC_ENTITIES[i]);
    return s;
  }


  //----
  private static String getColorAsHexString(Color c) {
    return String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
  }


  //----
  private static void apd(StringBuilder sb, Object... tokens) {
    for (Object t : tokens) {
      if (t instanceof Double)
        sb.append(DEC_FORMAT.format(t));
      else
        sb.append(t);
    }
  }


  //----
  private AffineTransform getMyTransform() {
    final AffineTransform at = new AffineTransform();
    final double w = mySize.x, h = mySize.y;
    if (myTransform == TRANSFORM_90) {
      at.rotate(Math.PI / -2.0);
      at.translate(-w, 0.0);
    } else if (myTransform == TRANSFORM_180) {
      at.rotate(Math.PI);
      at.translate(-w, -h);
    } else if (myTransform == TRANSFORM_270) {
      at.rotate(Math.PI / 2.0);
      at.translate(0.0, -h);
    } else if (myTransform == TRANSFORM_0N) {
      at.scale(-1.0, 1.0);
      at.translate(-w, 0.0);
    } else if (myTransform == TRANSFORM_90N) {
      at.scale(1.0, -1.0);
      at.rotate(Math.PI / -2.0);
    } else if (myTransform == TRANSFORM_180N) {
      at.scale(-1.0, 1.0);
      at.rotate(-Math.PI);
      at.translate(0.0, -h);
    } else if (myTransform == TRANSFORM_270N) {
      at.scale(1.0, -1.0);
      at.rotate(Math.PI / 2.0);
      at.translate(-w, -h);
    }
    return at;
  }


  //----
  private Point2D.Double getMySize() {
    return isFlat(myTransform) ? mySize : new Point2D.Double(mySize.y, mySize.x);
  }


  //----
  public static boolean isFlat(int transform) {
    return transform == TRANSFORM_0  || transform == TRANSFORM_180 ||
           transform == TRANSFORM_0N || transform == TRANSFORM_180N;
  }


  //----
  private BufferedImage createBufferedImage(int dpiResX, int dpiResY, int format,
      Color fgColor, Color bgColor) {
    final double resolutionMmX = dpiResX / 25.4;
    final double resolutionMmY = dpiResY / 25.4;
    final Point2D.Double size = getMySize();
    final int pxlWidth = Math.round((float)(size.x * resolutionMmX + 0.5));
    final int pxlHeight = Math.round((float)(size.y * resolutionMmY + 0.5));

    // create image with transparency? (FORMAT_EPS actually stands for preview tiff here)
    boolean ensureTransparency = !myIsOpaque && (format == FORMAT_PNG || format == FORMAT_EPS);

    BufferedImage bi;
    if (ensureTransparency) {
      bi = new BufferedImage(pxlWidth, pxlHeight, BufferedImage.TYPE_INT_ARGB);
    } else if (format == FORMAT_JPG && // can we make a grayscale jpeg?
        fgColor.getRed() == fgColor.getGreen() && fgColor.getGreen() == fgColor.getBlue() &&
        bgColor.getRed() == bgColor.getGreen() && bgColor.getGreen() == bgColor.getBlue()) {
      bi = new BufferedImage(pxlWidth, pxlHeight, BufferedImage.TYPE_BYTE_GRAY);
    } else {
      bi = new BufferedImage(pxlWidth, pxlHeight, BufferedImage.TYPE_INT_RGB);
    }
    Graphics2D g2d = bi.createGraphics();

    if (!ensureTransparency) {
      g2d.setColor(bgColor);
      g2d.fillRect(0, 0, pxlWidth, pxlHeight);
    }

    AffineTransform at = AffineTransform.getScaleInstance(resolutionMmX, resolutionMmY);
    at.concatenate(getMyTransform());
    g2d.setTransform(at);

    g2d.setColor(fgColor);
    g2d.fill(myGraphics2D.getBarsShapes());
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2d.fill(myGraphics2D.getTextShapes());

    g2d.dispose();
    return bi;
  }

}


class BarcodeGraphics2D extends Graphics2D {
  private Graphics2D dummyG2D =
      new BufferedImage(1, 1, BufferedImage.TYPE_BYTE_GRAY).createGraphics();
  private Area barsShapes = new Area();
  private Area textShapes = new Area();


  //----
  BarcodeGraphics2D() {
    dummyG2D.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                              RenderingHints.VALUE_FRACTIONALMETRICS_ON);
  }


  //----
  Area getBarsShapes() { return barsShapes; }
  Area getTextShapes() { return textShapes; }
  Area getAllShapes() {
    Area a = new Area();
    a.add(barsShapes);
    a.add(textShapes);
    return a;
  }


  //---- we will need only a few methods from Graphics2D
  public void fill(Shape shape) {
    barsShapes.add(new Area(shape));
  }
  public void drawString(String text, float x, float y) {
    FontRenderContext rc = getFontRenderContext();
    char[] c = text.toCharArray();
    GlyphVector gv = getFont().layoutGlyphVector(rc, c, 0, c.length, Font.LAYOUT_LEFT_TO_RIGHT);
    textShapes.add(new Area(gv.getOutline(x, y)));
  }
  public void setFont(Font font) { dummyG2D.setFont(font); }
  public Font getFont() { return dummyG2D.getFont(); }
  public FontMetrics getFontMetrics(Font f) { return dummyG2D.getFontMetrics(); }
  public FontRenderContext getFontRenderContext() { return dummyG2D.getFontRenderContext(); }


  //---- unused Graphics2D methods
  public void addRenderingHints(Map<?,?> hints) {}
  public void clearRect(int x, int y, int width, int height) {}
  public void clip(Shape s) {}
  public void clipRect(int x, int y, int width, int height) {}
  public void copyArea(int x, int y, int width, int height, int dx, int dy) {}
  public Graphics create() { return null; }
  public void dispose() {}
  public void draw(Shape s) {}
  public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle) {}
  public void drawGlyphVector(GlyphVector g, float x, float y) {}
  public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2,
                           int sy2, Color bgcolor, ImageObserver observer) { return false; }
  public boolean drawImage(Image img, int x, int y, int width, int height, Color bgcolor,
                           ImageObserver observer) { return false; }
  public boolean drawImage(Image img, int x, int y, Color bgrClr, ImageObserver o) { return false; }
  public boolean drawImage(Image img, int x, int y, int w, int h, ImageObserver o) { return false; }
  public boolean drawImage(Image img, int x, int y, ImageObserver observer) { return false; }
  public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2,
                           int sy2, ImageObserver observer) { return false; }
  public void drawImage(BufferedImage img, BufferedImageOp op, int x, int y) {}
  public boolean drawImage(Image img, AffineTransform xform, ImageObserver obs) { return false; }
  public void drawLine(int x1, int y1, int x2, int y2) {}
  public void drawOval(int x, int y, int width, int height) {}
  public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {}
  public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints) {}
  public void drawRenderableImage(RenderableImage img, AffineTransform xform) {}
  public void drawRenderedImage(RenderedImage img, AffineTransform xform) {}
  public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {}
  public void drawString(AttributedCharacterIterator iterator, float x, float y) {}
  public void drawString(AttributedCharacterIterator iterator, int x, int y) {}
  public void drawString(String str, int x, int y) {}
  public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle) {}
  public void fillOval(int x, int y, int width, int height) {}
  public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {}
  public void fillRect(int x, int y, int width, int height) {}
  public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {}
  public Color getBackground() { return null; }
  public GraphicsConfiguration getDeviceConfiguration() { return null; }
  public Shape getClip() { return null; }
  public Rectangle getClipBounds() { return null; }
  public Color getColor() { return null; }
  public Composite getComposite() { return null; }
  public Paint getPaint() { return null; }
  public Object getRenderingHint(RenderingHints.Key hintKey) { return null; }
  public RenderingHints getRenderingHints() { return null; }
  public Stroke getStroke() { return null; }
  public AffineTransform getTransform() { return null; }
  public boolean hit(Rectangle rect, Shape s, boolean onStroke) { return false; }
  public void rotate(double theta, double x, double y) {}
  public void rotate(double theta) {}
  public void scale(double sx, double sy) {}
  public void setBackground(Color c) {}
  public void setClip(int x, int y, int width, int height) {}
  public void setClip(Shape clip) {}
  public void setColor(Color c) {}
  public void setComposite(Composite comp) {}
  public void setPaint(Paint paint) {}
  public void setPaintMode() {}
  public void setRenderingHint(RenderingHints.Key hintKey, Object hintValue) {}
  public void setRenderingHints(Map<?,?> hints) {}
  public void setStroke(Stroke s) {}
  public void setTransform(AffineTransform Tx) {}
  public void setXORMode(Color c1) {}
  public void shear(double shx, double shy) {}
  public void transform(AffineTransform Tx) {}
  public void translate(int x, int y) {}
  public void translate(double tx, double ty) {}

}
