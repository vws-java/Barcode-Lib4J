import de.vwsoft.barcode.*;
import de.vwsoft.common.awt.*;
import twodcode.*;
import java.awt.*;
import java.awt.geom.*;
import java.io.*;

/*
    Welcome! There are only a few classes you need to interact with directly:

        de.vwsoft.barcode.Barcode – Abstract super class of all 1D barcodes
        de.vwsoft.barcode.BarcodeFactory – Factory class for creating 1D barcode instances
        twodcode.TwoDCode – Instantiable class for creating 2D barcode instances

    And for creation of image files:

        de.vwsoft.barcode.ImageExporter – Provides a Graphics2D instance, generates images
        de.vwsoft.common.awt.CompoundColor – Stores a color in both RGB and CMYK spaces at once

    Following example demonstrates how 1D and 2D barcodes can be generated and saved as a bitmap.

    The process of creating images is quite similar to that of printing, so both cases will be
    covered in this tutorial.
*/

public class Tutorial {

  public static void main(String args[]) {

    // For bitmap images, a resolution must be specified. The resolution should correspond to that
    // of the printer on which the image will be printed later. For vector formats, specifying a
    // resolution is optional, but still recommended if the vector image is to be printed on a low
    // resolution printer (<= 600 dpi).

    // A typical (low) resolution of an average thermal transfer label printer
    final int horResolutionDPI = 300;
    final int verResolutionDPI = 300;

    // Barcode dimensions in millimeters - for a better overview we use the same ones for 1D and 2D
    final double widthMM = 40.0, heightMM = 30.0;

    // Note: Image formats that optionally support transparent background: PDF, EPS, SVG, PNG.
    final String exportFile1D = "EAN-13.jpg";
    // Note: Although JPEGs are generated with a quality factor of 1.0 and appearances of JPEG
    // artifacts are unlikely due to the high contrast, PNG and BMP should still be preferred as
    // bitmap formats, since they use lossless / no compression and do not perform internal
    // color conversion (compared to JPEG: RGB -> YCbCr).
    final String exportFile2D = "QR-Code.png";


    // 1D Barcode ----------------------------------------------------------------------------------


    // The "BarcodeFactory" class provides everything to easily integrate support for 1D barcodes
    // into your own application. For 2D barcodes a similar class is provided, see next section.
    Barcode bc1D = BarcodeFactory.createBarcode(BarcodeFactory.CODETYPE_EAN13);

    // One of the key methods! The first parameter is self-explanatory. The second parameter
    // (boolean autoComplete) has barcode type-specific effects, such as prepending a '0' for
    // barcodes that expect numbers with an even number of digits (2 of 5 Interleaved, Code 128 C).
    // For barcode types with a fixed number of digits, such as EAN-13, setting "autoComplete=true"
    // will automatically calculate the check digit if it is missing. Setting it to "false" will
    // force validation of an existing check digit and throw an Exception if it is missing or wrong.
    //
    // The third parameter (boolean addOptChecksum) adds an optional check digit for those barcode
    // types that can optionally have a check digit by definition, but do not have to
    // (e.g. 2 of 5 Interleaved, Code 39).
    //
    // For other barcode types, one or even both parameters may have no function. See source code of
    // the respective barcode type.
    bc1D.setNumber("123456789012", true, false); // We let the lib calculate the check digit for us

    // The SIZE of the font is ignored and must be set explicitly using "setFixedFontSize" if
    // required. If not set, the font size will be automatically adjusted by default.
    bc1D.setFont(new Font("Calibri", Font.PLAIN, 1));

    ImageExporter ie = new ImageExporter(widthMM, heightMM);

    // For 1D barcodes, only one of the two resolutions is relevant here. When printing at
    // 90° or 270°, the VERTICAL resolution should be passed instead.
    double sizeOfADotMM = 25.4 / horResolutionDPI;
    bc1D.draw(ie.getGraphics2D(), 0.0, 0.0, widthMM, heightMM, sizeOfADotMM, 0.0, 0.0);

    try (FileOutputStream fos = new FileOutputStream(exportFile1D)) {
      ie.writeJPG(fos, horResolutionDPI, verResolutionDPI);
    } catch (IOException e) {
      e.printStackTrace();
    }


    // 2D Code -------------------------------------------------------------------------------------


    // "TwoDCode" is a kind of proxy class for creating and drawing all supported 2D code types.
    // Properties are assigned via public variables.
    TwoDCode bc2D = new TwoDCode();
    bc2D.pType = TwoDCode.QRCODE;
    bc2D.pContent = "The longer the input, the larger the 2D code";

    // Once all needed properties are set, a call to "update()" must follow to rebuild the barcode.
    bc2D.update();

    // An instance of "ImageExporter" can not be reused, so we have to create a new one
    ImageExporter ie2 = new ImageExporter(widthMM, heightMM);

    // Note: If colors other than black code on a white background are intended, the new colors
    // should be set at "ImageExporter", not at "TwoDCode". Same with transparency. Example:
    ie2.setForeground(new CompoundColor(30, 20, 220));
    ie2.setOpaque(false); // Transparent background

    // Note: For 2D codes, the smaller resolution should be used (regardless of the angle at
    // which it is printed):
    double dotSizeMM = 25.4 / Math.min(horResolutionDPI, verResolutionDPI);
    bc2D.draw(ie2.getGraphics2D(), new  Rectangle2D.Double(0.0, 0.0, widthMM, heightMM), dotSizeMM);

    try (FileOutputStream fos = new FileOutputStream(exportFile2D)) {
      ie2.writePNG(fos, horResolutionDPI, verResolutionDPI);
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

}
