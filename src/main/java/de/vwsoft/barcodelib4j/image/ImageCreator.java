/*
 * Copyright (c) 2025 Viktor Wedel
 *
 * Website EN: https://www.vw-software.com/java-barcode-library/
 * Website DE: https://www.vwsoft.de/barcode-library-for-java/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.vwsoft.barcodelib4j.image;
import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.awt.image.renderable.*;
import java.io.*;
import java.lang.reflect.*;
import java.nio.charset.*;
import java.text.*;
import java.util.*;


/**
 * Exports 1D and 2D barcodes to vector (PDF, EPS, SVG) and raster (PNG, BMP, JPG) formats.
 * <p>
 * The following example demonstrates the general usage of this class:
 * <pre>
 *     // Step 1: Create a 1D or 2D Barcode
 *     Barcode barcode = Barcode.newInstance(BarcodeType.EAN13);
 *     // ...
 *
 *     // Step 2: Specify dimensions of the resulting image in millimeters
 *     final double widthMM = 50.0, heightMM = 30.0;
 *
 *     // Step 3: Initialize an ImageCreator and obtain a Graphics2D
 *     ImageCreator imageCreator = new ImageCreator(widthMM, heightMM);
 *     Graphics2D g2d = imageCreator.getGraphics2D();
 *
 *     // Step 4: Use the specified dimensions again to draw the barcode
 *     barcode.draw(g2d, 0.0, 0.0, widthMM, heightMM);
 *     g2d.dispose();
 *
 *     // Step 5: Write the image file
 *     try (FileOutputStream fos = new FileOutputStream("ean-13.eps")) {
 *       imageCreator.writeEPS(fos, ImageCreator.COLORSPACE_CMYK);
 *     } catch (IOException ex) {
 *       // ...
 *     }
 * </pre>
 * <p>
 * When exporting to raster formats (PNG, BMP, JPG) and/or when the barcode graphic is intended for
 * later printing on a low-resolution printer, you should specify a resolution. The resolution
 * should be used in the {@code write} method, while the {@code draw} method should receive the dot
 * size in millimeters. The dot size is calculated using the formula: 25.4 / resolution. Example:
 * <pre>
 *     // An average label printer's typical (low) resolution
 *     int resolutionDPI = 300;
 *
 *     // Calculate the dot size in millimeters
 *     double dotSizeMM = 25.4 / resolutionDPI;
 *
 *     // Adjust the 'draw' and the 'write' method calls
 *     barcode.draw(g2d, 0.0, 0.0, widthMM, heightMM, dotSizeMM, 0.0, 0.0);
 *     // ...
 *     imageCreator.writePNG(fos, resolutionDPI, resolutionDPI);
 * </pre>
 * See also the description of the {@link #isFlat(int) isFlat} method for when to use horizontal and
 * when to use vertical resolution.
 */
public class ImageCreator {


  // Image Format Constants

  /** Image format constant for PDF format. */
  public static final int FORMAT_PDF = 0;

  /** Image format constant for EPS format. */
  public static final int FORMAT_EPS = 1;

  /** Image format constant for SVG format. */
  public static final int FORMAT_SVG = 2;

  /** Image format constant for PNG format. */
  public static final int FORMAT_PNG = 3;

  /** Image format constant for BMP format. */
  public static final int FORMAT_BMP = 4;

  /** Image format constant for JPG format. */
  public static final int FORMAT_JPG = 5;


  // Color Space Constants

  /** Color space constant for RGB color space. */
  public static final int COLORSPACE_RGB  = 0;

  /** Color space constant for CMYK color space. */
  public static final int COLORSPACE_CMYK = 1;


  // Transformation Constants

  /** Transformation constant for rotation by 0 degrees. */
  public static final int TRANSFORM_0    = 0;

  /** Transformation constant for rotation by 90 degrees. */
  public static final int TRANSFORM_90   = 1;

  /** Transformation constant for rotation by 180 degrees. */
  public static final int TRANSFORM_180  = 2;

  /** Transformation constant for rotation by 270 degrees. */
  public static final int TRANSFORM_270  = 3;

  /** Transformation constant for horizontal flip (0 degrees with mirrored image). */
  public static final int TRANSFORM_0N   = 4;

  /** Transformation constant for vertical flip (90 degrees with mirrored image). */
  public static final int TRANSFORM_90N  = 5;

  /** Transformation constant for horizontal and vertical flip (180 degrees with mirrored image). */
  public static final int TRANSFORM_180N = 6;

  /** Transformation constant for horizontal and vertical flip (270 degrees with mirrored image). */
  public static final int TRANSFORM_270N = 7;


  // Formats coordinates in EPS and SVG files, rounding to a maximum of 6 decimal places.
  private static final DecimalFormat DEC_FORMAT =
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



  /**
   * Constructs a new instance with the specified dimensions for the image to be created.
   *
   * @param widthMM  the width of the image in millimeters
   * @param heightMM the height of the image in millimeters
   */
  public ImageCreator(double widthMM, double heightMM) {
    mySize = new Point2D.Double(widthMM, heightMM);
  }



  /**
   * {@return a {@code Graphics2D} object for drawing the barcode to be exported} Note that the
   * returned {@code Graphics2D} object implements only the functionality needed for drawing
   * barcodes and that any necessary {@code RenderingHints} are set internally.
   * <p>
   * The implemented methods are:
   * <ul>
   *   <li>{@link Graphics2D#fill(Shape) fill(Shape s)}</li>
   *   <li>{@link Graphics2D#drawString(String, float, float)
   *     drawString(String str, float x, float y)}</li>
   *   <li>{@link Graphics2D#setFont(Font) setFont(Font font)}</li>
   *   <li>{@link Graphics2D#getFont() getFont()}</li>
   *   <li>{@link Graphics2D#getFontMetrics(Font) getFontMetrics(Font f)}</li>
   *   <li>{@link Graphics2D#getFontRenderContext() getFontRenderContext()}</li>
   *   <li>{@link Graphics2D#dispose() dispose()}</li>
   * </ul>
   * <p>
   * The only method you are most likely to use in practice is {@code dispose()} to release the
   * object's resources when finished.
   */
  public Graphics2D getGraphics2D() {
    return myGraphics2D;
  }



  /**
   * Sets the title metadata for the image file to be created. This is only supported for PDF, EPS
   * and SVG formats. The title is added to the metadata of the file. Ensure that all characters in
   * the title string are supported by the selected file format. However, in the case of SVG,
   * characters such as &lt;, &gt;, &amp;, ' and " are allowed, as they are automatically converted
   * to the appropriate HTML entities.
   *
   * @param title the title string to set as metadata for the image file
   */
  public void setTitle(String title) {
    myTitle = title;
  }



  /**
   * Sets the creator metadata for the image file to be created. This method sets both the
   * "Creator" and "Producer" metadata fields in PDF files at the same time.
   *
   * @param creator the creator string to set as metadata for the image file
   */
  public void setCreator(String creator) {
    myCreator = creator;
  }



  /**
   * Sets whether the background of the exported barcode image should be opaque or transparent. The
   * latter option is only supported by the PDF, EPS, SVG and PNG formats. The default is
   * {@code true} (opaque).
   *
   * @param opaque {@code true} for an opaque background or {@code false} for a transparent
   *               background
   */
  public void setOpaque(boolean opaque) {
    myIsOpaque = opaque;
  }



  /**
   * Sets the foreground color for the exported barcode image. The specified color is used for the
   * bars and any associated text elements in the barcode. The default color is
   * {@link CompoundColor#CC_BLACK}.
   * <p>
   * Note that passing any color to the object returned by {@link #getGraphics2D()} has no effect.
   *
   * @param color the foreground color for the barcode image
   */
  public void setForeground(CompoundColor color) {
    myForeground = color;
  }



  /**
   * Sets the background color for the exported barcode image. The specified color is used for the
   * spaces, quiet zones and other non-bar areas in the barcode. The default color is
   * {@link CompoundColor#CC_WHITE}.
   * <p>
   * Note that passing any color to the object returned by {@link #getGraphics2D()} has no effect.
   *
   * @param color the background color for the barcode image
   */
  public void setBackground(CompoundColor color) {
    myBackground = color;
  }



  /**
   * Sets the transformation for the exported barcode image. The parameter can be any of the
   * {@code TRANSFORM_*} constants defined in this class. The default is {@link #TRANSFORM_0}.
   *
   * @param transform any of the {@code TRANSFORM_*} constants
   * @see #isFlat(int transform)
   */
  public void setTransform(int transform) {
    myTransform = transform;
  }



  /**
   * Sets the resolution of the embedded TIFF preview when exporting to EPS format. A value of
   * {@code 0} (default) means that no TIFF preview is embedded in the EPS file.
   * <p>
   * EPS files can have a TIFF preview to provide a visual representation of the content,
   * particularly useful for viewers that do not natively support EPS. A typical resolution for the
   * TIFF preview should be at least 72 DPI, which provides sufficient quality for preview purposes
   * without increasing the file size excessively.
   *
   * @param dpiRes resolution in DPI for the embedded TIFF preview,
   *               {@code 0} means disabling the preview
   */
  public void setTiffResolution(int dpiRes) {
    myTiffRes = dpiRes;
  }



  /**
   * Writes the barcode image in one of the supported image formats.
   * Note that both resolution parameters must be greater than {@code 0} for raster image formats.
   *
   * @param out        the {@code OutputStream} to write the barcode image to
   * @param format     any of the {@code FORMAT_*} constants
   * @param colorSpace must be either {@link #COLORSPACE_RGB} or {@link #COLORSPACE_CMYK}
   * @param dpiResX    the horizontal resolution in DPI
   * @param dpiResY    the vertical resolution in DPI
   * @throws IOException if an I/O error occurs while writing the image
   */
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



  /**
   * Writes the barcode image in PNG format.
   *
   * @param out        the {@code OutputStream} to write the barcode image to
   * @param dpiResX    the horizontal resolution in DPI
   * @param dpiResY    the vertical resolution in DPI
   * @throws IOException if an I/O error occurs while writing the image
   */
  public void writePNG(OutputStream out, int dpiResX, int dpiResY) throws IOException {
    write(out, FORMAT_PNG, COLORSPACE_RGB, dpiResX, dpiResY);
  }



  /**
   * Writes the barcode image in BMP format.
   *
   * @param out        the {@code OutputStream} to write the barcode image to
   * @param dpiResX    the horizontal resolution in DPI
   * @param dpiResY    the vertical resolution in DPI
   * @throws IOException if an I/O error occurs while writing the image
   */
  public void writeBMP(OutputStream out, int dpiResX, int dpiResY) throws IOException {
    write(out, FORMAT_BMP, COLORSPACE_RGB, dpiResX, dpiResY);
  }



  /**
   * Writes the barcode image in JPG format. A compression quality of {@code 1} is always used.
   *
   * @param out        the {@code OutputStream} to write the barcode image to
   * @param dpiResX    the horizontal resolution in DPI
   * @param dpiResY    the vertical resolution in DPI
   * @throws IOException if an I/O error occurs while writing the image
   */
  public void writeJPG(OutputStream out, int dpiResX, int dpiResY) throws IOException {
    write(out, FORMAT_JPG, COLORSPACE_RGB, dpiResX, dpiResY);
  }



  /**
   * Writes the barcode image in PDF format.
   *
   * @param out        the {@code OutputStream} to write the barcode image to
   * @param colorSpace must be either {@link #COLORSPACE_RGB} or {@link #COLORSPACE_CMYK}
   * @throws IOException if an I/O error occurs while writing the image
   */
  public void writePDF(OutputStream out, int colorSpace) throws IOException {
    try {
      // Load OpenPDF classes dynamically
      Class<?> documentClass       = Class.forName("com.lowagie.text.Document");
      Class<?> rectangleClass      = Class.forName("com.lowagie.text.Rectangle");
      Class<?> byteBufferClass     = Class.forName("com.lowagie.text.pdf.ByteBuffer");
      Class<?> cmykColorClass      = Class.forName("com.lowagie.text.pdf.CMYKColor");
      Class<?> fontMapperClass     = Class.forName("com.lowagie.text.pdf.FontMapper");
      Class<?> pdfActionClass      = Class.forName("com.lowagie.text.pdf.PdfAction");
      Class<?> pdfContentByteClass = Class.forName("com.lowagie.text.pdf.PdfContentByte");
      Class<?> pdfDestinationClass = Class.forName("com.lowagie.text.pdf.PdfDestination");
      Class<?> pdfGraphics2DClass  = Class.forName("com.lowagie.text.pdf.PdfGraphics2D");
      Class<?> pdfNameClass        = Class.forName("com.lowagie.text.pdf.PdfName");
      Class<?> pdfObjectClass      = Class.forName("com.lowagie.text.pdf.PdfObject");
      Class<?> pdfStreamClass      = Class.forName("com.lowagie.text.pdf.PdfStream");
      Class<?> pdfWriterClass      = Class.forName("com.lowagie.text.pdf.PdfWriter");

      // Ensure 6 digits after decimal point instead of only 2
      Field highPrecisionField = byteBufferClass.getField("HIGH_PRECISION");
      final boolean oldPrecision = highPrecisionField.getBoolean(null);
      highPrecisionField.setBoolean(null, true);

      final double scale = 72.0 / 25.4; // mm to 1/72 inch
      Point2D.Double size = getMySize();
      Point2D.Float docSize = new Point2D.Float((float)(size.x * scale), (float)(size.y * scale));

      // Initialize and configure 'Document'
      Object rect = rectangleClass.getConstructor(float.class, float.class)
          .newInstance(docSize.x, docSize.y);
      if (myIsOpaque)
        rectangleClass.getMethod("setBackgroundColor", Color.class)
            .invoke(rect, getColorForPDF(myBackground, colorSpace, cmykColorClass));
      Object doc = documentClass.getConstructor(rectangleClass).newInstance(rect);

      // Initialize and configure 'PdfWriter'
      Object writer = pdfWriterClass.getMethod("getInstance", documentClass, OutputStream.class)
          .invoke(null, doc, out);
      pdfWriterClass.getMethod("setCompressionLevel", int.class)
          .invoke(writer, pdfStreamClass.getField("BEST_COMPRESSION").getInt(null));
      pdfWriterClass.getMethod("setPDFXConformance", int.class).invoke(writer,
          colorSpace == COLORSPACE_CMYK ? pdfWriterClass.getField("PDFX1A2001").getInt(null) :
                                          pdfWriterClass.getField("PDFX32002").getInt(null));

      // Open previously initialized 'Document' and add more properties
      documentClass.getMethod("open").invoke(doc);
      if (myTitle != null)
        documentClass.getMethod("addTitle", String.class).invoke(doc, myTitle);
      if (myCreator != null) {
        documentClass.getMethod("addCreator", String.class).invoke(doc, myCreator);
        documentClass.getMethod("addProducer", String.class).invoke(doc, myCreator);
      }
      documentClass.getMethod("addSubject", String.class).invoke(doc,
          colorSpace == COLORSPACE_CMYK ? "PDF/X-1a:2001, CMYK colors" :
                                          "PDF/X-3:2002, RGB colors");

      // Retrieve 'Graphics2D', draw content, then dispose
      Object g2d = pdfGraphics2DClass.getConstructor(pdfContentByteClass, float.class, float.class,
          fontMapperClass, boolean.class, boolean.class, float.class)
              .newInstance(pdfWriterClass.getMethod("getDirectContent").invoke(writer),
                  docSize.x, docSize.y, null, true, false, 1F);
      AffineTransform at = AffineTransform.getScaleInstance(scale, scale);
      at.concatenate(getMyTransform());
      pdfGraphics2DClass.getMethod("setTransform", AffineTransform.class).invoke(g2d, at);
      pdfGraphics2DClass.getMethod("setColor", Color.class)
          .invoke(g2d, getColorForPDF(myForeground, colorSpace, cmykColorClass));
      pdfGraphics2DClass.getMethod("fill", Shape.class).invoke(g2d, myGraphics2D.getAllShapes());
      pdfGraphics2DClass.getMethod("dispose").invoke(g2d);

      // By default PDF readers should NOT scale the document when printing
      pdfWriterClass.getMethod("addViewerPreference", pdfNameClass, pdfObjectClass).invoke(writer,
          pdfNameClass.getField("PRINTSCALING").get(null), pdfNameClass.getField("NONE").get(null));

      // Set zoom to 100% when opening the document (otherwise it fits to window)
      Object pdfDest =
          pdfDestinationClass.getConstructor(int.class, float.class, float.class, float.class)
              .newInstance(pdfDestinationClass.getField("XYZ").getInt(null), 0F, docSize.y, 1F);
      pdfWriterClass.getMethod("setOpenAction", pdfActionClass)
          .invoke(writer, pdfActionClass.getMethod("gotoLocalPage", int.class,
              pdfDestinationClass, pdfWriterClass).invoke(null, 1, pdfDest, writer));

      documentClass.getMethod("close").invoke(doc);
      pdfWriterClass.getMethod("close").invoke(writer);

      // Restore old value for precision within OpenPDF
      highPrecisionField.setBoolean(null, oldPrecision);

    } catch (ClassNotFoundException e) {
      throw new IOException("OpenPDF library not available, please add it to your classpath", e);
    } catch (ReflectiveOperationException e) {
      throw new IOException("Error accessing OpenPDF library via reflection", e);
    }
  }



  private Color getColorForPDF(CompoundColor cc, int colorSpace, Class<?> cmykColorClass)
      throws ReflectiveOperationException {
    return colorSpace == COLORSPACE_RGB ? cc :
        (Color)cmykColorClass.getConstructor(float.class, float.class, float.class, float.class)
            .newInstance(cc.getCyan()   / 100F,   cc.getMagenta() / 100F,
                         cc.getYellow() / 100F,   cc.getKey()     / 100F);
  }



  /**
   * Writes the barcode image in EPS format.
   *
   * @param out        the {@code OutputStream} to write the barcode image to
   * @param colorSpace must be either {@link #COLORSPACE_RGB} or {@link #COLORSPACE_CMYK}
   * @throws IOException if an I/O error occurs while writing the image
   */
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



  // creates an eps file without an embedded tiff-preview
  private void writePureEPS(OutputStream out, int colorSpace) throws IOException {
    final double scale = 72.0 / 25.4; // mm to 1/72 inch
    final Point2D.Double size = getMySize();
    final Point2D.Double pageSize = new Point2D.Double(size.x * scale, size.y * scale);
    final String br = "\n";
    final StringBuilder sb = new StringBuilder(10_000);

    apd(sb, "%!PS-Adobe-3.0 EPSF-3.0", br);
    if (myTitle != null)
      apd(sb, "%%Title: ", myTitle, br);
    apd(sb, "%%HiResBoundingBox: 0 0 ", pageSize.x, ' ', pageSize.y, br, br);

    apd(sb, "/m {moveto} bind def", br);
    apd(sb, "/l {lineto} bind def", br);
    apd(sb, "/c {curveto} bind def", br);
    apd(sb, "/z {closepath} bind def", br, br);

    if (myIsOpaque) {
      apd(sb, getColorAsPostScriptCommand(myBackground, colorSpace), br);
      apd(sb, "0 0 ", pageSize.x, ' ', pageSize.y, " rectfill", br, br);
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
        case PathIterator.SEG_CLOSE:
          apd(sb, 'z', br);
      }
      pathIterator.next();
    }
    apd(sb, "fill", br);

    out.write(sb.toString().getBytes(StandardCharsets.US_ASCII));
  }



  private static String getColorAsPostScriptCommand(CompoundColor cc, int colorSpace) {
    return colorSpace == COLORSPACE_RGB ?
        DEC_FORMAT.format(cc.getRed()   / 255.0) + " " +
        DEC_FORMAT.format(cc.getGreen() / 255.0) + " " +
        DEC_FORMAT.format(cc.getBlue()  / 255.0) + " setrgbcolor" :
        (cc.getCyan()   / 100F) + " " + (cc.getMagenta() / 100F) + " " +
        (cc.getYellow() / 100F) + " " + (cc.getKey()     / 100F) + " setcmykcolor";
  }



  /**
   * Writes the barcode image in SVG format.
   *
   * @param out the {@code OutputStream} to write the barcode image to
   * @throws IOException if an I/O error occurs while writing the image
   */
  public void writeSVG(OutputStream out) throws IOException {
    final String br = "\n";
    final StringBuilder sb = new StringBuilder(10_000);
    final Point2D.Double size = getMySize();

    apd(sb, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>", br);
    apd(sb, "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"", size.x, "mm\" height=\"", size.y,
        "mm\" viewBox=\"0 0 ", size.x, ' ', size.y, "\">", br);
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
        case PathIterator.SEG_CLOSE  : apd(sb, 'Z');
      }
      pathIterator.next();
    }
    apd(sb, "\"/>", br);

    apd(sb, "</g>", br);

    apd(sb, "</svg>", br);

    out.write(sb.toString().getBytes(StandardCharsets.UTF_8));
  }



  private static String[] HTML4_ESC_CHARS = { "<", ">", "\"", "'", "&" };
  private static String[] HTML4_ESC_ENTITIES = { "&lt;", "&gt;", "&quot;", "&apos;", "&amp;" };
  private static String escapeHtml4(String s) {
    for (int i=4; i>=0; i--) // order is important! '&' must be replaced first
      s = s.replace(HTML4_ESC_CHARS[i], HTML4_ESC_ENTITIES[i]);
    return s;
  }



  private static String getColorAsHexString(Color c) {
    return String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
  }



  private static void apd(StringBuilder sb, Object... tokens) {
    for (Object t : tokens) {
      if (t instanceof Double)
        sb.append(DEC_FORMAT.format(t));
      else
        sb.append(t);
    }
  }



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



  private Point2D.Double getMySize() {
    return isFlat(myTransform) ? mySize : new Point2D.Double(mySize.y, mySize.x);
  }



  /**
   * Checks whether the specified transformation represents a flat rotation (0&deg; or 180&deg;).
   * <p>
   * This is a convenience method that can be used to determine whether the horizontal or vertical
   * resolution of the output medium is relevant in a particular case.
   * <p>
   * For 1D barcodes, which mainly consist of vertical bars, only one of the two resolutions is
   * relevant. For example, when creating a 1D barcode at a 90&deg; or 270&deg; angle, the vertical
   * resolution is crucial as the bar widths must be adjusted to it. Similarly, at a 0&deg; or
   * 180&deg; angle, the horizontal resolution is important.
   * <p>
   * (However, for 2D codes, if the output medium has differing horizontal and vertical resolutions,
   * regardless of the transformation used, always use the smaller resolution. For example, in a
   * setting of 300x600 DPI, use 300 DPI as the relevant resolution.)
   *
   * @param transform the transformation constant to check
   * @return {@code true} if the transformation represents a flat rotation (0 or 180 degrees),
   *         {@code false} otherwise.
   * @see #setTransform(int)
   */
  public static boolean isFlat(int transform) {
    return transform == TRANSFORM_0  || transform == TRANSFORM_180 ||
           transform == TRANSFORM_0N || transform == TRANSFORM_180N;
  }



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



  private class BarcodeGraphics2D extends Graphics2D {
    private Graphics2D dummyG2D =
        new BufferedImage(1, 1, BufferedImage.TYPE_BYTE_GRAY).createGraphics();
    private Area barsShapes = new Area();
    private Area textShapes = new Area();


    BarcodeGraphics2D() {
      dummyG2D.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                                RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    }


    Area getBarsShapes() { return barsShapes; }
    Area getTextShapes() { return textShapes; }
    Area getAllShapes() {
      Area a = new Area();
      a.add(barsShapes);
      a.add(textShapes);
      return a;
    }


    // only a subset of methods from Graphics2D are needed for our purposes
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
    public void dispose() { dummyG2D.dispose(); }


    // unused Graphics2D methods
    public void addRenderingHints(Map<?,?> hints) {}
    public void clearRect(int x, int y, int width, int height) {}
    public void clip(Shape s) {}
    public void clipRect(int x, int y, int width, int height) {}
    public void copyArea(int x, int y, int width, int height, int dx, int dy) {}
    public Graphics create() { return null; }
    public void draw(Shape s) {}
    public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle) {}
    public void drawGlyphVector(GlyphVector g, float x, float y) {}
    public boolean drawImage(Image i, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2,
                             int sy2, Color bgcolor, ImageObserver observer) { return false; }
    public boolean drawImage(Image i, int x, int y, int width, int height, Color bgcolor,
                             ImageObserver observer) { return false; }
    public boolean drawImage(Image i, int x, int y, Color bgrClr, ImageObserver o) { return false; }
    public boolean drawImage(Image i, int x, int y, int w, int h, ImageObserver o) { return false; }
    public boolean drawImage(Image i, int x, int y, ImageObserver observer) { return false; }
    public boolean drawImage(Image i, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2,
                             int sy2, ImageObserver observer) { return false; }
    public void drawImage(BufferedImage i, BufferedImageOp op, int x, int y) {}
    public boolean drawImage(Image i, AffineTransform xform, ImageObserver obs) { return false; }
    public void drawLine(int x1, int y1, int x2, int y2) {}
    public void drawOval(int x, int y, int width, int height) {}
    public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {}
    public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints) {}
    public void drawRenderableImage(RenderableImage i, AffineTransform xform) {}
    public void drawRenderedImage(RenderedImage i, AffineTransform xform) {}
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

}
