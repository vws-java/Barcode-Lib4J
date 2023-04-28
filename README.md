<img align="right" width="120" height="120" src="https://user-images.githubusercontent.com/130756709/232242399-81d7d770-4c66-469c-b1fd-a2ad6531d212.png">

## Features

- Draws and prints 1D & 2D barcodes via Java2D (java.awt.Graphics2D)
- Export to PDF: PDF/X-1a:2001 compliant for CMYK, PDF/X-3:2002 compliant for RGB
- Export to EPS: optional TIFF preview, RGB and CMYK
- Export to SVG: RGB colors only
- Barcode precision, coordinates are stored with 6 decimal places within all supported vector formats
- Export to PNG, BMP, JPG: No unwanted effects such as aliasing, etc. Storage of DPI resolution in file header
- **Adjustment of bar widths to printer resolution (essential for proper barcode quality at <= 600 dpi)**
- Bar width reduction/correction (e.g. for dealing with ink spreading on inkjet printers)
- Barcodes include minimum distances ("quiet zones")
- Barcode number optionally on top, bottom or invisible
- Customizable plain text line: font + font size, spacing, modifiable content
- Automatic adjustment of the font size, if requested
- Pro setting: printing with a self-defined module width
- Optionally transparent background (Supported in EPS, PDF, SVG and PNG)
- Rotation in 90-degree increments
- Configurable ratio from 2:1 to 3:1 for barcode types that support it
- Add-On 2/5 for barcodes such as EAN-13, EAN-8, ISBN-13 and UPC-A, second plain text line for ISBN and ISMN
- Full support for GS1 barcodes: Data integrity check, validitation of application identifiers, checking length and content of user data, verifying/calculation of the check digit in SSCC and GTIN, etc.

## Supported 1D and 2D Barcodes
**1D:** Code 128, Code 128 A, Code 128 B, Code 128 C, 2 of 5 Interleaved, Code 39, Code 39 Extended, GS1-128 (EAN-128), GTIN-13 (EAN-13), GTIN-8 (EAN-8), UPC-A, ISBN-13, ISMN, Code 93, Code 93 Extended, GTIN-14 (EAN-14), SSCC-18 (NVE), PZN, PZN8, Codabar.

**2D:** QR Code, GS1 QR Code, DataMatrix, GS1 DataMatrix, PDF417, Aztec.

## Requirements & Dependencies
- Minimum Java Version: 9
- Optional: [OpenPDF](https://github.com/LibrePDF/OpenPDF) for PDF export

## Get started
- Download the source code (+dependency) and compile it on your own OR get the latest [JAR](https://github.com/Barcode-Lib4J/Barcode-Lib4J/releases)
- Read [Tutorial.java](src/Tutorial.java)

## Credits
2D barcodes are provided by some slightly modified classes from the [ZXing](https://github.com/zxing/zxing) project. A factory class has been added to it to support above features as well as GS1 versions of QR Code and DataMatrix.



