<div align="center">
  <img width="500" height="110" src="https://www.vw-software.com/img/pgmlogo7big.webp">
</div>

## Features
- Drawing and printing of 1D and 2D barcodes using Java2D (java.awt.Graphics2D)
- Barcodes as **PDF**: Compliance with PDF/X-1a:2001 for color-accurate printing in CMYK as well as PDF/X-3:2002 for RGB colors
- Barcodes as **EPS**: Supports both RGB and CMYK color models; Optional embedding of a TIFF preview for display in graphic applications without their own EPS interpreter
- Barcodes as **SVG**: This vector format inherently only supports the RGB color model
- Barcodes as **PNG, BMP, JPG**: DPI resolution is stored in the file header, thereby preserving both the target resolution and the original dimensions of the graphic for later printing; No unwanted visual effects, such as aliasing, distortion or other artifacts
- High precision: Coordinates are stored with up to six decimal places in all supported vector formats
- **Adjustment of bar widths to printer resolution (important for ensuring proper barcode quality at resolutions ≤ 600 dpi)**
- Bar width reduction/correction (useful to account for ink spread in inkjet printers)
- Barcodes include required minimum distances ("quiet zones") according to the respective specification
- Customizable plain text line: font, size, spacing and editable content; Optional automatic font size adjustment; Additionally, the plain text line can be positioned at the top, bottom or completely hidden
- Advanced setting: Customizable size of the barcode bars ("module size")
- Option for transparent background (Supported in EPS, PDF, SVG and PNG)
- Rotation in 90-degree increments
- Configurable bar width ratio from 2:1 to 3:1 for barcode types that support this feature: Interleaved 2 of 5, Code 39 and others
- Add-Ons 2 or 5 for the barcode types EAN-13, EAN-8, UPC-A, UPC-E, ISBN-13 and ISMN; second plain text line for ISBN-13 and ISMN
- **Comprehensive support for GS1-128, GS1 DataMatrix and GS1 QR Code:** Verification of data integrity such as correctness of application identifiers, length and format of individual data elements, verification/calculation of the check digit in SSCC and GTIN and more

## Supported 1D and 2D Barcodes
**1D:** Code 128, Code 128 A, Code 128 B, Code 128 C, 2 of 5 Interleaved, Code 39, Code 39 Extended, GS1-128 (EAN-128), GTIN-13 (EAN-13), GTIN-8 (EAN-8), UPC-A, ISBN-13, ISMN, Code 93, Code 93 Extended, GTIN-14 (EAN-14), SSCC-18 (NVE), PZN, PZN8, Codabar.

**2D:** QR Code, GS1 QR Code, DataMatrix, GS1 DataMatrix, PDF417, Aztec.

## Requirements & Dependencies
- Minimum Java Version: 9
- Optional: [OpenPDF](https://github.com/LibrePDF/OpenPDF) for PDF export

## Getting started
- Download the source code (+dependency) and compile it on your own **OR** download the latest ZIP file with the source code, precompiled JAR and JavaDocs from the [Barcode-Lib4J Official Product Website](https://www.vw-software.com/java-barcode-library/)
- See tutorial with several examples for different use cases (same Link as above)

## Credits
2D barcodes are provided by some slightly modified classes from the [ZXing](https://github.com/zxing/zxing) project. A factory class has been added to it to support above features as well as GS1 versions of QR Code and DataMatrix.



