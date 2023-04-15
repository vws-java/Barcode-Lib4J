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

package de.vwsoft.common.awt;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import javax.imageio.metadata.*;
import javax.imageio.stream.*;
import org.w3c.dom.*;


/**
Saves images with a specified resolution in different bitmap formats. Except for TIFF, the
resolution is always stored in the header of a file to preserve its dimensions in
millimeters/inches.
*/
public class BitmapImageIO {


  //----
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
      pHYSNode.setAttribute("pixelsPerUnitXAxis", String.valueOf(Math.round(dpiResX / .0254)));
      pHYSNode.setAttribute("pixelsPerUnitYAxis", String.valueOf(Math.round(dpiResY / .0254)));
      try {
        iiomd.setFromTree(formatName, rootNode);
      } catch (IIOInvalidTreeException e) {}
    }

    imageWriter.setOutput(new MemoryCacheImageOutputStream(out));
    imageWriter.write(new IIOImage(img, null, iiomd));
  }


  //----
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
      node.setAttribute("Xdensity", String.valueOf(dpiResX));
      node.setAttribute("Ydensity", String.valueOf(dpiResY));
      try {
        iiomd.setFromTree(formatName, rootNode);
      } catch (IIOInvalidTreeException e) {}
    }

    imageWriter.setOutput(new MemoryCacheImageOutputStream(out));
    imageWriter.write(new IIOImage(img, null, iiomd));
  }


  //----
  public static void writeBMP(RenderedImage img, OutputStream out, int dpiResX, int dpiResY)
      throws IOException {
    ImageWriter imageWriter = ImageIO.getImageWritersByFormatName("bmp").next();
    ImageWriteParam param = imageWriter.getDefaultWriteParam();
    IIOMetadata iiomd = imageWriter.getDefaultImageMetadata(new ImageTypeSpecifier(img), param);

    // java's own ImageWriter does not allow manipulation of the bmp resolution - neither via
    // "javax_imageio_1.0" nor via "javax_imageio_bmp_1.0". an attempt is answered with
    // "java.lang.IllegalStateException: Metadata is read-only". therefore we have to use our
    // own methods here
    final int dotsPerMeterX = Integer.reverseBytes((int)Math.round(dpiResX / .0254));
    final int dotsPerMeterY = Integer.reverseBytes((int)Math.round(dpiResY / .0254));
    DataOutputStream dos = new DataOutputStream(out) {
      ByteArrayOutputStream baos = new ByteArrayOutputStream(50);
      public void write(byte b[], int off, int len) throws IOException {
        if (baos != null) {
          baos.write(b, off, len);
          if (baos.size() >= 38) {
            b = baos.toByteArray();
            super.write(b, 0, 38);
            super.writeInt(dotsPerMeterX);
            super.writeInt(dotsPerMeterY);
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


  //----
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


  //----
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


  //----
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
