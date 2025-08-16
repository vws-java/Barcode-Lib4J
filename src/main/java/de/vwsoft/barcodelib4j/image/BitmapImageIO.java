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
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import javax.imageio.metadata.*;
import javax.imageio.stream.*;
import org.w3c.dom.*;


/**
 * Provides static utility methods for writing raster graphics with a specified resolution.
 * <p>
 * The specified resolution is typically written to the header of an image file, allowing graphics
 * software to determine the image's dimensions, such as in millimeters or inches. The formats
 * currently supported are PNG, JPEG, and BMP.
 */
public class BitmapImageIO {

  // This class only contains static utility methods and should not be instantiated.
  private BitmapImageIO() {
  }



  /**
   * Writes a rendered image in PNG format with the specified resolution.
   *
   * @param img the {@code RenderedImage} object to write
   * @param out the {@code OutputStream} to write the image data to
   * @param dpiResX the horizontal resolution in dots per inch (DPI)
   * @param dpiResY the vertical resolution in dots per inch (DPI)
   * @throws IOException if an I/O error occurs during writing
   */
  public static void writePNG(RenderedImage img, OutputStream out, int dpiResX, int dpiResY)
      throws IOException {
    ImageWriter imageWriter = ImageIO.getImageWritersByFormatName("png").next();
    IIOMetadata iiomd = imageWriter.getDefaultImageMetadata(new ImageTypeSpecifier(img),
        imageWriter.getDefaultWriteParam());

    if (dpiResX > 0 && dpiResY > 0) {
      String formatName = "javax_imageio_png_1.0";
      IIOMetadataNode rootNode = (IIOMetadataNode)iiomd.getAsTree(formatName);
      IIOMetadataNode pHYSNode = ensureChildNode(rootNode, "pHYs");
      pHYSNode.setAttribute("unitSpecifier", "meter");
      pHYSNode.setAttribute("pixelsPerUnitXAxis", Long.toString(Math.round(dpiResX / .0254)));
      pHYSNode.setAttribute("pixelsPerUnitYAxis", Long.toString(Math.round(dpiResY / .0254)));
      try {
        iiomd.setFromTree(formatName, rootNode);
      } catch (IIOInvalidTreeException e) {}
    }

    imageWriter.setOutput(new MemoryCacheImageOutputStream(out));
    imageWriter.write(new IIOImage(img, null, iiomd));
  }



  /**
   * Writes a rendered image in JPEG format with the specified resolution and quality.
   *
   * @param img the {@code RenderedImage} object to write
   * @param out the {@code OutputStream} to write the image data to
   * @param dpiResX the horizontal resolution in dots per inch (DPI)
   * @param dpiResY the vertical resolution in dots per inch (DPI)
   * @param quality the quality of the JPEG image (0.0f to 1.0f)
   * @throws IOException if an I/O error occurs during writing
   */
  public static void writeJPG(RenderedImage img, OutputStream out, int dpiResX, int dpiResY,
      float quality) throws IOException {
    ImageWriter imageWriter = ImageIO.getImageWritersByFormatName("jpg").next();
    ImageWriteParam param = imageWriter.getDefaultWriteParam();
    param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
    param.setCompressionQuality(quality);
    IIOMetadata iiomd = imageWriter.getDefaultImageMetadata(new ImageTypeSpecifier(img), param);

    if (dpiResX > 0 && dpiResX <= 65535 && dpiResY > 0 && dpiResY <= 65535) {
      String formatName = "javax_imageio_jpeg_image_1.0";
      IIOMetadataNode rootNode = (IIOMetadataNode)iiomd.getAsTree(formatName);
      IIOMetadataNode node = ensureChildNode(ensureChildNode(rootNode, "JPEGvariety"), "app0JFIF");
      node.setAttribute("resUnits", "1"); // "dpi"
      node.setAttribute("Xdensity", Integer.toString(dpiResX));
      node.setAttribute("Ydensity", Integer.toString(dpiResY));
      try {
        iiomd.setFromTree(formatName, rootNode);
      } catch (IIOInvalidTreeException e) {}
    }

    imageWriter.setOutput(new MemoryCacheImageOutputStream(out));
    imageWriter.write(new IIOImage(img, null, iiomd));
  }



  /**
   * Writes a rendered image in BMP format with the specified resolution.
   *
   * @param img the {@code RenderedImage} object to write
   * @param out the {@code OutputStream} to write the image data to
   * @param dpiResX the horizontal resolution in dots per inch (DPI)
   * @param dpiResY the vertical resolution in dots per inch (DPI)
   * @throws IOException if an I/O error occurs during writing
   */
  public static void writeBMP(RenderedImage img, OutputStream out, int dpiResX, int dpiResY)
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
            super.writeInt(Integer.reverseBytes(Math.round(dpiResX / .0254F)));
            super.writeInt(Integer.reverseBytes(Math.round(dpiResY / .0254F)));
            if (b.length > 46)
              super.write(b, 46, b.length - 46);
            baos = null;
          }
        } else {
          super.write(b, off, len);
        }
      }
    };

    imageWriter.setOutput(new MemoryCacheImageOutputStream(dos));
    imageWriter.write(new IIOImage(img, null, iiomd));
  }



  /**
   * Writes a rendered image in the standard TIFF format with LZW compression.
   *
   * @param img the {@code RenderedImage} object to write
   * @param out the {@code OutputStream} to write the image data to
   * @throws IOException if an I/O error occurs during writing
   */
  public static void writeTIFF(RenderedImage img, OutputStream out) throws IOException {
    ImageWriter imageWriter = ImageIO.getImageWritersByFormatName("tiff").next();
    ImageWriteParam param = imageWriter.getDefaultWriteParam();
    param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
    param.setCompressionType("LZW");
    MemoryCacheImageOutputStream mc = new MemoryCacheImageOutputStream(out);
    imageWriter.setOutput(mc);
    imageWriter.write(null, new IIOImage(img, null, null), param);
    mc.flush();
  }



  /**
   * Retreives the resolution of an image.
   * <p>
   * The formats currently supported are PNG, JPEG, and BMP.
   *
   * @param imgReader the {@code ImageReader} object used to read the image
   * @return a {@code Point} object representing the resolution (in DPI) of the image
   * @throws IOException if an I/O error occurs during reading
   */
  public static Point getResolution(ImageReader imgReader) throws IOException {
    String format = imgReader.getFormatName().toLowerCase();
    IIOMetadata iiomd = imgReader.getImageMetadata(imgReader.getMinIndex());

    if ("jpeg".equals(format) || "png".equals(format)) {

      IIOMetadataNode resNode =
          ensureChildNode((IIOMetadataNode)iiomd.getAsTree("javax_imageio_1.0"), "Dimension");
      String x = ensureChildNode(resNode, "HorizontalPixelSize").getAttribute("value");
      String y = ensureChildNode(resNode, "VerticalPixelSize").getAttribute("value");
      try {
        return new Point((int)Math.round(25.4 / Double.parseDouble(x)),  // in fact "float"
                         (int)Math.round(25.4 / Double.parseDouble(y))); // in fact "float"
      } catch (Exception e) { /* throws on division by zero as well */ }

    } else if ("bmp".equals(format)) { // "javax_imageio_1.0" doesn't work on bmp's

      IIOMetadataNode ppmNode = ensureChildNode((IIOMetadataNode)iiomd
          .getAsTree("javax_imageio_bmp_1.0"), "PixelsPerMeter");
      String x = ensureChildNode(ppmNode, "X").getNodeValue();
      String y = ensureChildNode(ppmNode, "Y").getNodeValue();
      try {
        return new Point((int)Math.round(Long.parseLong(x) * 2.54 / 100.0),  // in fact "uint"
                         (int)Math.round(Long.parseLong(y) * 2.54 / 100.0)); // in fact "uint"
      } catch (Exception e) { /* throws on division by zero as well */ }

    }

    return new Point();
  }



  // This method ensures the existence of a child node with the specified name under the provided
  // parent node. If the child node does not exist, it creates a new node with the given name and
  // appends it to the parent node. If the child node already exists, it returns the first
  // occurrence of the node with the specified name found under the parent node.
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

}
