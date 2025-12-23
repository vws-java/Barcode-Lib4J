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
import java.nio.charset.*;
import java.security.*;
import java.text.*;
import java.util.*;
import java.util.zip.*;
import javax.imageio.*;
import javax.imageio.metadata.*;
import javax.imageio.stream.*;
import org.w3c.dom.*;


/**
 * Exports 1D and 2D barcodes to vector (PDF, EPS, SVG) and raster (PNG, BMP, JPG) images.
 * <p>
 * The following example demonstrates the general usage of this class:
 * <pre>
 *     // Step 1: Create a 1D or 2D Barcode
 *     Barcode barcode = Barcode.newInstance(BarcodeType.CODE128);
 *     // ...
 *
 *     // Step 2: Specify dimensions of the resulting image in millimeters
 *     final double widthMM = 50.0, heightMM = 30.0;
 *
 *     // Step 3: Initialize a BarExporter and obtain a Graphics2D
 *     BarExporter exporter = new BarExporter(widthMM, heightMM);
 *     Graphics2D g2d = exporter.getGraphics2D();
 *
 *     // Step 4: Use the specified dimensions again to draw the barcode
 *     barcode.draw(g2d, 0.0, 0.0, widthMM, heightMM);
 *     g2d.dispose();
 *
 *     // Step 5: Write the image file
 *     try (FileOutputStream fos = new FileOutputStream("ean-13.eps")) {
 *       exporter.writeEPS(fos, ImageColorModel.CMYK);
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
 *     exporter.writePNG(fos, resolutionDPI, resolutionDPI);
 * </pre>
 * See also the description of the {@link ImageTransform#isFlat()} method for when to use horizontal
 * and when to use vertical resolution.
 */
public class BarExporter {
  private static final double MM_TO_POINTS = 72.0 / 25.4;

  // Formats coordinates in vector files, rounding to a maximum of 6 decimal places.
  private final DecimalFormat myDecimalFormat =
      new DecimalFormat("#.######", new DecimalFormatSymbols(Locale.US));

  private final BarcodeGraphics2D myGraphics2D = new BarcodeGraphics2D();
  private final Point2D.Double mySize;
  private String myTitle;
  private String myCreator;
  private boolean myIsOpaque = true;
  private CompoundColor myForeground = CompoundColor.CC_BLACK;
  private CompoundColor myBackground = CompoundColor.CC_WHITE;
  private ImageTransform myTransform = ImageTransform.ROTATE_0;
  private boolean myIsInlineSVG;
  private int myTiffRes;



  /**
   * Constructs a new instance with the specified dimensions for the image to be created.
   *
   * @param widthMM  the width of the image in millimeters
   * @param heightMM the height of the image in millimeters
   * @throws IllegalArgumentException if {@code widthMM} or {@code heightMM} is &lt;= {@code 0}
   */
  public BarExporter(double widthMM, double heightMM) {
    if (widthMM <= 0.0 || heightMM <= 0.0)
      throw new IllegalArgumentException("Width and height must be greater than 0");
    mySize = new Point2D.Double(widthMM, heightMM);
  }



  /**
   * {@return a {@code Graphics2D} object for drawing the barcode to be exported}
   * <p>
   * <b>Note:</b> The returned {@code Graphics2D} object implements only the functionality needed
   * for drawing barcodes. Any necessary {@code RenderingHints} are set internally. In practice,
   * the only method you'll typically need to call is {@code dispose()} to release the object's
   * resources when finished. The implemented methods are:
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
   */
  public Graphics2D getGraphics2D() {
    return myGraphics2D;
  }



  /**
   * Sets the title metadata for the image file to be created.
   * <p>
   * This is only supported for PDF, EPS, and SVG formats. The title is added to the metadata of the
   * file. Ensure that all characters in the title string are supported by the selected file format.
   * Note that SVG automatically escapes the characters &lt;, &gt;, &amp;, ' and " to
   * appropriate XML entities.
   *
   * @param title the title string to set as metadata for the image file, or {@code null} to omit it
   */
  public void setTitle(String title) {
    myTitle = title == null || title.isBlank() ? null : title;
  }



  /**
   * Sets the creator metadata for the image file to be created.
   * <p>
   * This is only supported for EPS and PDF files, where this method sets the "Creator" metadata.
   * Ensure that all characters in the creator string are supported by the selected file format.
   *
   * @param creator the creator string to set as metadata for the image file,
   *                or {@code null} to omit it
   */
  public void setCreator(String creator) {
    myCreator = creator == null || creator.isBlank() ? null : creator;
  }



  /**
   * Sets whether the background of the exported barcode image should be opaque or transparent.
   * <p>
   * Transparency is only supported by the PDF, EPS, SVG, and PNG formats. The default is
   * {@code true} (opaque).
   *
   * @param opaque {@code true} for an opaque background or {@code false} for a transparent
   *               background
   */
  public void setOpaque(boolean opaque) {
    myIsOpaque = opaque;
  }



  /**
   * Sets the foreground color for the exported barcode image.
   * <p>
   * The specified color is used for the bars and any associated text elements in the barcode. The
   * default color is {@link CompoundColor#CC_BLACK}.
   * <p>
   * <b>Note:</b> Colors passed to the {@link #getGraphics2D()} object are ignored. Use this method
   * instead.
   *
   * @param color the foreground color for the barcode image
   * @throws NullPointerException if {@code color} is {@code null}
   */
  public void setForeground(CompoundColor color) {
    myForeground = Objects.requireNonNull(color, "Foreground must not be null");
  }



  /**
   * Sets the background color for the exported barcode image.
   * <p>
   * The specified color is used for the spaces, quiet zones and other non-bar areas in the barcode.
   * The default color is {@link CompoundColor#CC_WHITE}.
   * <p>
   * <b>Note:</b> Colors passed to the {@link #getGraphics2D()} object are ignored. Use this method
   * instead.
   *
   * @param color the background color for the barcode image
   * @throws NullPointerException if {@code color} is {@code null}
   */
  public void setBackground(CompoundColor color) {
    myBackground = Objects.requireNonNull(color, "Background must not be null");
  }



  /**
   * Sets the transformation for the exported barcode image.
   * <p>
   * The default is {@link ImageTransform#ROTATE_0}.
   *
   * @param transform the {@code ImageTransform} to apply to the image
   * @throws NullPointerException if {@code transform} is {@code null}
   * @see ImageTransform#isFlat()
   */
  public void setTransform(ImageTransform transform) {
    myTransform = Objects.requireNonNull(transform, "Transform must not be null");
  }



  /**
   * Sets whether SVG output should be generated as inline SVG or as standalone SVG document.
   * <p>
   * When set to {@code true}, the XML declaration and xmlns attribute are omitted, making
   * the SVG suitable for direct embedding in HTML5 documents. When {@code false} (default),
   * a complete standalone SVG document with XML declaration is generated.
   *
   * @param isInline {@code true} for inline SVG (no XML declaration, no xmlns attribute),
   *                 {@code false} for standalone SVG document
   */
  public void setInlineSVG(boolean isInline) {
    myIsInlineSVG = isInline;
  }



  /**
   * Sets the resolution of the embedded TIFF preview when exporting to EPS format.
   * <p>
   * A value of {@code 0} (default) means that no TIFF preview is embedded in the EPS file.
   * <p>
   * EPS files can have a TIFF preview to provide a visual representation of the content,
   * particularly useful for viewers that do not natively support EPS. A typical resolution for the
   * TIFF preview should be at least 150 DPI, which provides sufficient quality for preview purposes
   * without increasing the file size excessively.
   *
   * @param dpiRes resolution in DPI for the embedded TIFF preview,
   *               {@code 0} to disable the preview
   * @throws IllegalArgumentException if {@code dpiRes} is negative
   */
  public void setTiffResolution(int dpiRes) {
    if (dpiRes < 0)
      throw new IllegalArgumentException("TIFF resolution must be >= 0");
    myTiffRes = dpiRes;
  }



  /**
   * Writes the barcode image in one of the supported image formats.
   * <p>
   * For raster image formats, both resolution parameters must be greater than {@code 0}.
   *
   * @param out        the {@code OutputStream} to write the barcode image to
   * @param format     the {@code ImageFormat} to use
   * @param colorModel the {@code ImageColorModel} to use
   * @param dpiResX    the horizontal resolution in DPI
   * @param dpiResY    the vertical resolution in DPI
   * @throws IOException if an I/O error occurs while writing the image
   * @throws IllegalArgumentException if {@code dpiResX} or {@code dpiResY} is {@code <= 0}
   *                                  for raster formats
   */
  public void write(OutputStream out, ImageFormat format, ImageColorModel colorModel,
      int dpiResX, int dpiResY) throws IOException {
    if (format == ImageFormat.PDF)
      writePDF(out, colorModel);
    else if (format == ImageFormat.EPS)
      writeEPS(out, colorModel);
    else if (format == ImageFormat.SVG)
      writeSVG(out);
    else {
      if (dpiResX <= 0 || dpiResY <= 0)
        throw new IllegalArgumentException("Resolution must be > 0 for raster formats");
      BufferedImage img = createBufferedImage(dpiResX, dpiResY, format);
      if (format == ImageFormat.PNG)
        toPNG(img, out, dpiResX, dpiResY);
      else if (format == ImageFormat.BMP)
        toBMP(img, out, dpiResX, dpiResY);
      else
        toJPG(img, out, dpiResX, dpiResY, 1F);
    }
  }



  /**
   * Writes the barcode image in PNG format.
   *
   * @param out        the {@code OutputStream} to write the barcode image to
   * @param dpiResX    the horizontal resolution in DPI
   * @param dpiResY    the vertical resolution in DPI
   * @throws IOException if an I/O error occurs while writing the image
   * @throws IllegalArgumentException if {@code dpiResX} or {@code dpiResY} is {@code <= 0}
   */
  public void writePNG(OutputStream out, int dpiResX, int dpiResY) throws IOException {
    write(out, ImageFormat.PNG, ImageColorModel.RGB, dpiResX, dpiResY);
  }



  /**
   * Writes the barcode image in BMP format.
   *
   * @param out        the {@code OutputStream} to write the barcode image to
   * @param dpiResX    the horizontal resolution in DPI
   * @param dpiResY    the vertical resolution in DPI
   * @throws IOException if an I/O error occurs while writing the image
   * @throws IllegalArgumentException if {@code dpiResX} or {@code dpiResY} is {@code <= 0}
   */
  public void writeBMP(OutputStream out, int dpiResX, int dpiResY) throws IOException {
    write(out, ImageFormat.BMP, ImageColorModel.RGB, dpiResX, dpiResY);
  }



  /**
   * Writes the barcode image in JPG format.
   * <p>
   * A compression quality of {@code 1} is always used.
   *
   * @param out        the {@code OutputStream} to write the barcode image to
   * @param dpiResX    the horizontal resolution in DPI
   * @param dpiResY    the vertical resolution in DPI
   * @throws IOException if an I/O error occurs while writing the image
   * @throws IllegalArgumentException if {@code dpiResX} or {@code dpiResY} is {@code <= 0}
   */
  public void writeJPG(OutputStream out, int dpiResX, int dpiResY) throws IOException {
    write(out, ImageFormat.JPG, ImageColorModel.RGB, dpiResX, dpiResY);
  }



  /**
   * Writes the barcode image in PDF format.
   *
   * @param out        the {@code OutputStream} to write the barcode image to
   * @param colorModel the {@code ImageColorModel} to use
   * @throws IOException if an I/O error occurs while writing the image
   */
  public void writePDF(OutputStream out, ImageColorModel colorModel) throws IOException {
    final Point2D.Double size = getEffectiveSize();
    final Point2D.Double docSize = new Point2D.Double(size.x * MM_TO_POINTS, size.y * MM_TO_POINTS);
    final ArrayList<Long> xrefOffsets = new ArrayList<>(5);
    final StringBuilder sb = new StringBuilder(500);

    // Initialize PDF header first to calculate correct offsets
    byte[] header = "%PDF-1.4\n%\u00E2\u00E3\u00CF\u00D3\n".getBytes(StandardCharsets.ISO_8859_1);
    long offset = header.length;
    xrefOffsets.add(offset);

    // Object 1: Catalog
    apd(sb, "1 0 obj\n<</Type/Catalog/Pages 2 0 R/ViewerPreferences<</PrintScaling/None>>",
        "/OpenAction[3 0 R/XYZ 0 ", docSize.y, " 1]>>\nendobj\n");
    xrefOffsets.add(offset + sb.length());

    // Object 2: Pages
    apd(sb, "2 0 obj\n<</Kids[3 0 R]/Type/Pages/Count 1>>\nendobj\n");
    xrefOffsets.add(offset + sb.length());

    // Object 3: Page
    apd(sb, "3 0 obj\n<</Type/Page/Parent 2 0 R/MediaBox [0 0 ", docSize.x, ' ', docSize.y, "]",
        "/Contents 4 0 R>>\nendobj\n");
    xrefOffsets.add(offset + sb.length());

    // Object 4: Content Stream
    byte[] contentStream = writePDFContentStream(docSize, colorModel);
    apd(sb, "4 0 obj\n<</Filter/FlateDecode/Length ", contentStream.length, ">>\nstream\n");
    final String pdfBodyBeforeContentStream = sb.toString();
    offset += pdfBodyBeforeContentStream.length() + contentStream.length;
    sb.setLength(0);
    apd(sb, "\nendstream\nendobj\n");
    xrefOffsets.add(offset + sb.length());

    // Object 5: Info Dictionary
    apd(sb, "5 0 obj\n<</Producer", encodePDFString("Barcode-Lib4J"));
    if (myTitle != null)
      apd(sb, "/Title", encodePDFString(myTitle));
    if (myCreator != null)
      apd(sb, "/Creator", encodePDFString(myCreator));
    apd(sb, "/Subject", encodePDFString(colorModel == ImageColorModel.CMYK ?
        "CMYK colors used" : "RGB colors used"));
    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    String timestamp = String.format("(D:%04d%02d%02d%02d%02d%02dZ)",
        cal.get(Calendar.YEAR),        cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH),
        cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE),    cal.get(Calendar.SECOND));
    apd(sb, "/CreationDate", timestamp, "/ModDate", timestamp, ">>\nendobj\n");

    // Generate file ID for the trailer
    String fileId = null;
    try {
      String idSource = timestamp + myTitle + myCreator + mySize + System.nanoTime();
      byte[] hash = MessageDigest.getInstance("MD5").digest(idSource.getBytes());
      StringBuilder sb32 = new StringBuilder(32);
      for (byte b : hash)
        sb32.append(String.format("%02X", b));
      fileId = sb32.toString();
    } catch (NoSuchAlgorithmException e) { /* MD5 is always available per Java spec */ }

    // Build cross-reference table and append trailer
    offset += sb.length();
    apd(sb, "xref\n0 ", xrefOffsets.size() + 1, "\n0000000000 65535 f \n");
    for (Long o : xrefOffsets)
      apd(sb, String.format("%010d 00000 n \n", o));
    apd(sb, "trailer\n<</Size ", xrefOffsets.size() + 1, "/Root 1 0 R/Info 5 0 R/ID[<",
        fileId, "><", fileId, ">]>>\nstartxref\n", offset, "\n%%EOF\n");

    // Write complete PDF to output
    out.write(header);
    out.write(pdfBodyBeforeContentStream.getBytes(StandardCharsets.US_ASCII));
    out.write(contentStream);
    out.write(sb.toString().getBytes(StandardCharsets.US_ASCII));
  }



  private byte[] writePDFContentStream(Point2D.Double docSize, ImageColorModel colorModel)
      throws IOException {
    final String br = "\n";
    final StringBuilder sb = new StringBuilder(10_000);

    // Draw background if requested
    if (myIsOpaque) {
      apd(sb, getColorCommand(myBackground, colorModel, ImageFormat.PDF), br);
      apd(sb, "0 0 ", docSize.x, ' ', docSize.y, " re f", br);
    }

    // Draw barcode content
    apd(sb, getColorCommand(myForeground, colorModel, ImageFormat.PDF), br);
    AffineTransform at = new AffineTransform(MM_TO_POINTS, 0.0, 0.0, -MM_TO_POINTS, 0.0, docSize.y);
    at.concatenate(createTransform());
    for (Rectangle2D r : myGraphics2D.barRectangles) {
      r = at.createTransformedShape(r).getBounds2D();
      apd(sb, r.getX(), ' ', r.getY(), ' ', r.getWidth(), ' ', r.getHeight(), " re", br);
    }
    final double[] d = new double[6];
    final double[] lastPoint = new double[2];
    final double[] controlPoint = new double[4];
    for (Shape shape : myGraphics2D.textShapes) {
      PathIterator pathIterator = shape.getPathIterator(at);
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
            apd(sb, 'h', br);
        }
        pathIterator.next();
      }
    }
    apd(sb, "f", br); // Fill all paths at once

    byte[] uncompressed = sb.toString().getBytes(StandardCharsets.US_ASCII);

    Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
    deflater.setStrategy(Deflater.FILTERED); // Measured best compression for coordinate data
    deflater.setInput(uncompressed);
    deflater.finish();
    ByteArrayOutputStream baos = new ByteArrayOutputStream(uncompressed.length / 2);
    byte[] buffer = new byte[4096];
    while (!deflater.finished())
      baos.write(buffer, 0, deflater.deflate(buffer));
    deflater.end();

    return baos.toByteArray();
  }



  private String encodePDFString(String s) {
    boolean needsUnicode = s.codePoints().anyMatch(cp -> cp > 126);

    if (needsUnicode) { // Encode as UTF-16BE hex string with BOM
      StringBuilder sb = new StringBuilder(s.length() * 6);
      sb.append("<FEFF");
      for (int i = 0; i < s.length(); i++) {
        char c = s.charAt(i);
        if (Character.isHighSurrogate(c) && i + 1 < s.length()) {
          char low = s.charAt(i + 1);
          if (Character.isLowSurrogate(low)) {
            sb.append(String.format(Locale.ROOT, "%04X%04X", (int)c, (int)low));
            i++; // skip low surrogate
            continue;
          }
        }
        sb.append(String.format(Locale.ROOT, "%04X", (int)c));
      }
      return sb.append('>').toString();
    } else { // Encode as standard ASCII literal string
      StringBuilder sb = new StringBuilder(s.length() + 10);
      sb.append('(');
      for (int i = 0; i < s.length(); i++) {
        char c = s.charAt(i);
        if (c == '(' || c == ')' || c == '\\')
          sb.append('\\').append(c);
        else if (c < 32)
          sb.append(String.format(Locale.ROOT, "\\%03o", (int)c));
        else
          sb.append(c);
      }
      return sb.append(')').toString();
    }
  }



  /**
   * Writes the barcode image in EPS format.
   *
   * @param out        the {@code OutputStream} to write the barcode image to
   * @param colorModel the {@code ImageColorModel} to use
   * @throws IOException if an I/O error occurs while writing the image
   */
  public void writeEPS(OutputStream out, ImageColorModel colorModel) throws IOException {
    if (myTiffRes == 0) {
      writePureEPS(out, colorModel);
      return;
    }

    ByteArrayOutputStream epsArray = new ByteArrayOutputStream(10_000);
    writePureEPS(epsArray, colorModel);

    ByteArrayOutputStream tiffArray = new ByteArrayOutputStream(10_000);
    BufferedImage img = createBufferedImage(myTiffRes, myTiffRes, null);
    Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("tiff");
    if (!writers.hasNext())
      throw new IOException("No TIFF ImageWriter found");
    ImageWriter imageWriter = writers.next();
    ImageWriteParam param = imageWriter.getDefaultWriteParam();
    param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
    param.setCompressionType("LZW");
    try (ImageOutputStream mc = new MemoryCacheImageOutputStream(tiffArray)) {
      imageWriter.setOutput(mc);
      imageWriter.write(null, new IIOImage(img, null, null), param);
    } finally {
      imageWriter.dispose();
    }

    DataOutputStream dos = new DataOutputStream(out);
    dos.writeInt(0xC5D0D3C6); // EPS binary file header magic number (4 bytes)
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



  // Creates an EPS file without an embedded TIFF preview
  private void writePureEPS(OutputStream out, ImageColorModel colorModel) throws IOException {
    final Point2D.Double size = getEffectiveSize();
    final Point2D.Double docSize = new Point2D.Double(size.x * MM_TO_POINTS, size.y * MM_TO_POINTS);
    final String br = "\n";
    final StringBuilder sb = new StringBuilder(10_000);

    apd(sb, "%!PS-Adobe-3.0 EPSF-3.0", br);
    if (myTitle != null)
      apd(sb, "%%Title: ", myTitle, br);
    if (myCreator != null)
      apd(sb, "%%Creator: ", myCreator, br);
    apd(sb, "%%HiResBoundingBox: 0 0 ", docSize.x, ' ', docSize.y, br, br);

    apd(sb, "/m {moveto} bind def", br);
    apd(sb, "/l {lineto} bind def", br);
    apd(sb, "/c {curveto} bind def", br);
    apd(sb, "/r {rectfill} bind def", br);      // Only command that differs from PDF ('re')
    apd(sb, "/h {closepath} bind def", br, br);

    if (myIsOpaque) {
      apd(sb, getColorCommand(myBackground, colorModel, ImageFormat.EPS), br);
      apd(sb, "0 0 ", docSize.x, ' ', docSize.y, " r", br, br);
    }

    apd(sb, getColorCommand(myForeground, colorModel, ImageFormat.EPS), br);
    AffineTransform at = new AffineTransform(MM_TO_POINTS, 0.0, 0.0, -MM_TO_POINTS, 0.0, docSize.y);
    at.concatenate(createTransform());
    for (Rectangle2D r : myGraphics2D.barRectangles) {
      r = at.createTransformedShape(r).getBounds2D();
      apd(sb, r.getX(), ' ', r.getY(), ' ', r.getWidth(), ' ', r.getHeight(), " r", br);
    }
    final double[] d = new double[6];
    final double[] lastPoint = new double[2];
    final double[] controlPoint = new double[4];
    for (Shape shape : myGraphics2D.textShapes) {
      PathIterator pathIterator = shape.getPathIterator(at);
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
            apd(sb, 'h', br);
        }
        pathIterator.next();
      }
    }
    apd(sb, "fill", br);

    out.write(sb.toString().getBytes(StandardCharsets.US_ASCII));
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
    final Point2D.Double size = getEffectiveSize();

    if (!myIsInlineSVG)
      apd(sb, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>", br);
    apd(sb, "<svg ", myIsInlineSVG ? "" : "xmlns=\"http://www.w3.org/2000/svg\" ",
        "width=\"", size.x, "mm\" height=\"", size.y,
        "mm\" viewBox=\"0 0 ", size.x, ' ', size.y, "\">", br);
    if (myTitle != null)
      apd(sb, "<title>", escapeXML(myTitle), "</title>", br);

    apd(sb, "<g>", br);
    apd(sb, "<rect ", (myIsOpaque ? "fill=\"" + getColorForSVG(myBackground) : "opacity=\"0"),
        "\" width=\"", size.x, "\" height=\"", size.y, "\"/>", br);

    apd(sb, "<path fill=\"", getColorForSVG(myForeground), "\" d=\"");
    final double[] d = new double[6];
    AffineTransform at = createTransform();
    ArrayList<Shape> allShapes = new ArrayList<>(myGraphics2D.textShapes);
    allShapes.addAll(myGraphics2D.barRectangles);
    for (Shape shape : allShapes) {
      PathIterator pathIterator = shape.getPathIterator(at);
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
    }
    apd(sb, "\"/>", br);

    apd(sb, "</g>", br);

    apd(sb, "</svg>", br);

    out.write(sb.toString().getBytes(StandardCharsets.UTF_8));
  }



  private static String getColorForSVG(Color c) {
    return String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
  }



  // Called by PDF and EPS write methods
  private String getColorCommand(CompoundColor c, ImageColorModel colorModel, ImageFormat format) {
    return colorModel == ImageColorModel.RGB ?
        myDecimalFormat.format(c.getRed()     / 255.0) + " " +
        myDecimalFormat.format(c.getGreen()   / 255.0) + " " +
        myDecimalFormat.format(c.getBlue()    / 255.0) + " " +
        (format == ImageFormat.PDF ? "rg" : "setrgbcolor") :
        myDecimalFormat.format(c.getCyan()    / 100.0) + " " +
        myDecimalFormat.format(c.getMagenta() / 100.0) + " " +
        myDecimalFormat.format(c.getYellow()  / 100.0) + " " +
        myDecimalFormat.format(c.getKey()     / 100.0) + " " +
        (format == ImageFormat.PDF ? "k" : "setcmykcolor");
  }



  // Called by the SVG write method
  private static String escapeXML(String s) {
    StringBuilder sb = new StringBuilder(s.length() + 10);
    for (int i=0; i<s.length(); i++) {
      char c = s.charAt(i);
      switch (c) {
        case '&'  :  sb.append("&amp;");   break;
        case '<'  :  sb.append("&lt;");    break;
        case '>'  :  sb.append("&gt;");    break;
        case '"'  :  sb.append("&quot;");  break;
        case '\'' :  sb.append("&apos;");  break;
        default   :  sb.append(c);
      }
    }
    return sb.toString();
  }



  private void apd(StringBuilder sb, Object... tokens) {
    for (Object t : tokens) {
      if (t instanceof Double)
        sb.append(myDecimalFormat.format(t));
      else
        sb.append(t);
    }
  }



  private AffineTransform createTransform() {
    final AffineTransform at = new AffineTransform();
    if (myTransform == ImageTransform.ROTATE_90) {
      at.rotate(Math.PI / -2.0);
      at.translate(-mySize.x, 0.0);
    } else if (myTransform == ImageTransform.ROTATE_180) {
      at.rotate(Math.PI);
      at.translate(-mySize.x, -mySize.y);
    } else if (myTransform == ImageTransform.ROTATE_270) {
      at.rotate(Math.PI / 2.0);
      at.translate(0.0, -mySize.y);
    } else if (myTransform == ImageTransform.ROTATE_0_FLIP) {
      at.scale(-1.0, 1.0);
      at.translate(-mySize.x, 0.0);
    } else if (myTransform == ImageTransform.ROTATE_90_FLIP) {
      at.scale(1.0, -1.0);
      at.rotate(Math.PI / -2.0);
    } else if (myTransform == ImageTransform.ROTATE_180_FLIP) {
      at.scale(-1.0, 1.0);
      at.rotate(-Math.PI);
      at.translate(0.0, -mySize.y);
    } else if (myTransform == ImageTransform.ROTATE_270_FLIP) {
      at.scale(1.0, -1.0);
      at.rotate(Math.PI / 2.0);
      at.translate(-mySize.x, -mySize.y);
    }
    return at;
  }



  private Point2D.Double getEffectiveSize() {
    return myTransform.isFlat() ? mySize : new Point2D.Double(mySize.y, mySize.x);
  }



  private BufferedImage createBufferedImage(int dpiResX, int dpiResY, ImageFormat format) {
    final double resolutionMmX = dpiResX / 25.4;
    final double resolutionMmY = dpiResY / 25.4;
    final Point2D.Double size = getEffectiveSize();
    final int pxlWidth = Math.round((float)(size.x * resolutionMmX + 0.5));
    final int pxlHeight = Math.round((float)(size.y * resolutionMmY + 0.5));

    // Create image with transparency? ('null' here means TIFF preview for EPS)
    boolean ensureTransparency = !myIsOpaque && (format == null || format.supportsTransparency());

    int imageType = BufferedImage.TYPE_3BYTE_BGR;
    if (ensureTransparency) {
      imageType = BufferedImage.TYPE_INT_ARGB;
    } else if (myForeground.getRed() == myForeground.getGreen() &&
               myForeground.getGreen() == myForeground.getBlue() &&
               myBackground.getRed() == myBackground.getGreen() &&
               myBackground.getGreen() == myBackground.getBlue()) {
      imageType = BufferedImage.TYPE_BYTE_GRAY;
    }
    BufferedImage bi = new BufferedImage(pxlWidth, pxlHeight, imageType);
    Graphics2D g2d = bi.createGraphics();

    if (!ensureTransparency) {
      g2d.setColor(myBackground);
      g2d.fillRect(0, 0, pxlWidth, pxlHeight);
    }

    AffineTransform at = AffineTransform.getScaleInstance(resolutionMmX, resolutionMmY);
    at.concatenate(createTransform());
    g2d.setTransform(at);

    g2d.setColor(myForeground);
    for (Shape s : myGraphics2D.barRectangles)
      g2d.fill(s);
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    for (Shape s : myGraphics2D.textShapes)
      g2d.fill(s);

    g2d.dispose();
    return bi;
  }



  private static void toPNG(BufferedImage img, OutputStream out, int dpiResX, int dpiResY)
      throws IOException {
    final String neededFormatName = "javax_imageio_png_1.0";
    ImageWriter imageWriter = null;
    IIOMetadata iiomd = null;
    Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("png");
    while (writers.hasNext()) {
      ImageWriter w = writers.next();
      iiomd = w.getDefaultImageMetadata(new ImageTypeSpecifier(img), w.getDefaultWriteParam());
      if (neededFormatName.equals(iiomd.getNativeMetadataFormatName())) {
        imageWriter = w;
        break;
      }
    }
    if (imageWriter == null)
      throw new IOException("No suitable PNG ImageWriter found");

    try (ImageOutputStream ios = new MemoryCacheImageOutputStream(out)) {
      try {
        IIOMetadataNode rootNode = (IIOMetadataNode)iiomd.getAsTree(neededFormatName);
        IIOMetadataNode pHYSNode = ensureChildNode(rootNode, "pHYs");
        pHYSNode.setAttribute("unitSpecifier", "meter");
        pHYSNode.setAttribute("pixelsPerUnitXAxis", Long.toString(Math.round(dpiResX / 0.0254)));
        pHYSNode.setAttribute("pixelsPerUnitYAxis", Long.toString(Math.round(dpiResY / 0.0254)));
        iiomd.setFromTree(neededFormatName, rootNode);
      } catch (Exception e) {
        throw new IOException("Failed to apply DPI metadata to PNG image", e);
      }
      imageWriter.setOutput(ios);
      imageWriter.write(new IIOImage(img, null, iiomd));
    } finally {
      imageWriter.dispose();
    }
  }



  private static void toBMP(BufferedImage img, OutputStream out, int dpiResX, int dpiResY)
      throws IOException {
    ImageWriter imageWriter = ImageIO.getImageWritersByFormatName("bmp").next();
    ImageWriteParam param = imageWriter.getDefaultWriteParam();
    IIOMetadata iiomd = imageWriter.getDefaultImageMetadata(new ImageTypeSpecifier(img), param);

    // Java's built-in ImageWriter does not permit manipulation of the BMP resolution, either
    // through "javax_imageio_1.0" or "javax_imageio_bmp_1.0". An attempt to do so results in a
    // "java.lang.IllegalStateException: Metadata is read-only". Hence, custom methods are employed.
    DataOutputStream dos = new DataOutputStream(out) {
      ByteArrayOutputStream baos = new ByteArrayOutputStream(50);
      public void write(byte b[], int off, int len) throws IOException {
        if (baos != null) {
          baos.write(b, off, len);
          if (baos.size() >= 38) {
            b = baos.toByteArray();
            super.write(b, 0, 38);
            super.writeInt(Integer.reverseBytes(Math.round(dpiResX / 0.0254F)));
            super.writeInt(Integer.reverseBytes(Math.round(dpiResY / 0.0254F)));
            if (b.length > 46)
              super.write(b, 46, b.length - 46);
            baos = null;
          }
        } else {
          super.write(b, off, len);
        }
      }
    };

    try (ImageOutputStream ios = new MemoryCacheImageOutputStream(dos)) {
      imageWriter.setOutput(ios);
      imageWriter.write(new IIOImage(img, null, iiomd));
    } finally {
      imageWriter.dispose();
    }
  }



  private static void toJPG(BufferedImage img, OutputStream out, int dpiResX, int dpiResY,
      float quality) throws IOException {
    final String neededFormatName = "javax_imageio_jpeg_image_1.0";
    ImageWriter imageWriter = null;
    IIOMetadata iiomd = null;
    Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
    while (writers.hasNext()) {
      ImageWriter w = writers.next();
      iiomd = w.getDefaultImageMetadata(new ImageTypeSpecifier(img), w.getDefaultWriteParam());
      if (neededFormatName.equals(iiomd.getNativeMetadataFormatName())) {
        imageWriter = w;
        break;
      }
    }
    if (imageWriter == null)
      throw new IOException("No suitable JPEG ImageWriter found");

    try (ImageOutputStream ios = new MemoryCacheImageOutputStream(out)) {
      try {
        ImageWriteParam param = imageWriter.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(quality);
        IIOMetadataNode root = (IIOMetadataNode)iiomd.getAsTree(neededFormatName);
        IIOMetadataNode n = ensureChildNode(ensureChildNode(root, "JPEGvariety"), "app0JFIF");
        n.setAttribute("resUnits", "1"); // 1 = "dpi"
        n.setAttribute("Xdensity", Integer.toString(dpiResX));
        n.setAttribute("Ydensity", Integer.toString(dpiResY));
        iiomd.setFromTree(neededFormatName, root);
      } catch (Exception e) {
        throw new IOException("Failed to apply parameters to JPEG image", e);
      }
      imageWriter.setOutput(ios);
      imageWriter.write(new IIOImage(img, null, iiomd));
    } finally {
      imageWriter.dispose();
    }
  }



  private static IIOMetadataNode ensureChildNode(IIOMetadataNode parentNode, String nodeName) {
    NodeList nodeList = parentNode.getElementsByTagName(nodeName);
    if (nodeList.getLength() == 0) {
      IIOMetadataNode node = new IIOMetadataNode(nodeName);
      parentNode.appendChild(node);
      return node;
    } else {
      return (IIOMetadataNode)nodeList.item(0);
    }
  }



  private static class BarcodeGraphics2D extends Graphics2D {
    Graphics2D fontG2D = new BufferedImage(1, 1, BufferedImage.TYPE_BYTE_GRAY).createGraphics();
    ArrayList<Rectangle2D> barRectangles = new ArrayList<>(40); // Decent default for 1D barcodes
    ArrayList<Shape> textShapes = new ArrayList<>(13); // Decent default for 1D barcodes


    BarcodeGraphics2D() {
      fontG2D.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                               RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    }


    // Only a subset of Graphics2D methods is required for this implementation
    public void fill(Shape shape) {
      barRectangles.add(shape.getBounds2D());
    }
    public void drawString(String text, float x, float y) {
      FontRenderContext rc = getFontRenderContext();
      char[] c = text.toCharArray();
      GlyphVector gv = getFont().layoutGlyphVector(rc, c, 0, c.length, Font.LAYOUT_LEFT_TO_RIGHT);
      textShapes.add(gv.getOutline(x, y));
    }
    public void setFont(Font font) { fontG2D.setFont(font); }
    public Font getFont() { return fontG2D.getFont(); }
    public FontMetrics getFontMetrics(Font f) { return fontG2D.getFontMetrics(); }
    public FontRenderContext getFontRenderContext() { return fontG2D.getFontRenderContext(); }
    public void dispose() { fontG2D.dispose(); }


    // Unused Graphics2D methods intentionally left unimplemented (UnsupportedOperationException)
    @SuppressWarnings("unchecked")
    private static <T> T u() {
      throw new UnsupportedOperationException("Method not supported by Graphics2D implementation");
    }
    public void addRenderingHints(Map<?,?> hints) { u(); }
    public void clearRect(int x, int y, int width, int height) { u(); }
    public void clip(Shape s) { u(); }
    public void clipRect(int x, int y, int width, int height) { u(); }
    public void copyArea(int x, int y, int width, int height, int dx, int dy) { u(); }
    public Graphics create() { return u(); }
    public void draw(Shape s) { u(); }
    public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle) { u(); }
    public void drawGlyphVector(GlyphVector g, float x, float y) { u(); }
    public boolean drawImage(Image i, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1,
                             int sx2, int sy2, Color bgcolor, ImageObserver o) { return u(); }
    public boolean drawImage(Image i, int x, int y, int width, int height, Color bgcolor,
                             ImageObserver observer) { return u(); }
    public boolean drawImage(Image i, int x, int y, Color bgrClr, ImageObserver o) { return u(); }
    public boolean drawImage(Image i, int x, int y, int w, int h, ImageObserver o) { return u(); }
    public boolean drawImage(Image i, int x, int y, ImageObserver observer) { return u(); }
    public boolean drawImage(Image i, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1,
                             int sx2, int sy2, ImageObserver observer) { return u(); }
    public void drawImage(BufferedImage i, BufferedImageOp op, int x, int y) { u(); }
    public boolean drawImage(Image i, AffineTransform at, ImageObserver obs) { return u(); }
    public void drawLine(int x1, int y1, int x2, int y2) { u(); }
    public void drawOval(int x, int y, int width, int height) { u(); }
    public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) { u(); }
    public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints) { u(); }
    public void drawRenderableImage(RenderableImage i, AffineTransform xform) { u(); }
    public void drawRenderedImage(RenderedImage i, AffineTransform xform) { u(); }
    public void drawRoundRect(int x, int y, int w, int h, int arcWidth, int arcHeight) { u(); }
    public void drawString(AttributedCharacterIterator iterator, float x, float y) { u(); }
    public void drawString(AttributedCharacterIterator iterator, int x, int y) { u(); }
    public void drawString(String str, int x, int y) { u(); }
    public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle) { u(); }
    public void fillOval(int x, int y, int width, int height) { u(); }
    public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) { u(); }
    public void fillRect(int x, int y, int width, int height) { u(); }
    public void fillRoundRect(int x, int y, int w, int h, int arcWidth, int arcHeight) { u(); }
    public Color getBackground() { return u(); }
    public GraphicsConfiguration getDeviceConfiguration() { return u(); }
    public Shape getClip() { return u(); }
    public Rectangle getClipBounds() { return u(); }
    public Color getColor() { return u(); }
    public Composite getComposite() { return u(); }
    public Paint getPaint() { return u(); }
    public Object getRenderingHint(RenderingHints.Key hintKey) { return u(); }
    public RenderingHints getRenderingHints() { return u(); }
    public Stroke getStroke() { return u(); }
    public AffineTransform getTransform() { return u(); }
    public boolean hit(Rectangle rect, Shape s, boolean onStroke) { return u(); }
    public void rotate(double theta, double x, double y) { u(); }
    public void rotate(double theta) { u(); }
    public void scale(double sx, double sy) { u(); }
    public void setBackground(Color c) { u(); }
    public void setClip(int x, int y, int width, int height) { u(); }
    public void setClip(Shape clip) { u(); }
    public void setColor(Color c) { u(); }
    public void setComposite(Composite comp) { u(); }
    public void setPaint(Paint paint) { u(); }
    public void setPaintMode() { u(); }
    public void setRenderingHint(RenderingHints.Key hintKey, Object hintValue) { u(); }
    public void setRenderingHints(Map<?,?> hints) { u(); }
    public void setStroke(Stroke s) { u(); }
    public void setTransform(AffineTransform Tx) { u(); }
    public void setXORMode(Color c1) { u(); }
    public void shear(double shx, double shy) { u(); }
    public void transform(AffineTransform Tx) { u(); }
    public void translate(int x, int y) { u(); }
    public void translate(double tx, double ty) { u(); }
  }

}
