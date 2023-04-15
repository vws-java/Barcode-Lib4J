## Features

- Draw and print via Java2D (java.awt.Graphics2D)
- Export to PDF: PDF/X-1a:2001 compliance for CMYK, PDF/X-3:2002  compliance for RGB
- Export to EPS: optional TIFF preview, RGB and CMYK
- Export to SVG: RGB color model only
- Export to PNG, BMP, JPG
- Barcode precision, coordinates with 6 decimal places within all supported vector formats
- Colored or transparent background
- **Adjustment of bars to printer resolution (essential for barcode quality when printing at <= 600 dpi)**
- Bar width correction
- Barcodes include minimum distances ("quiet zones")
- Barcode number optionally on top, at the bottom or invisible
- Customizable plain text line: font + font size, spacing, modifiable content
- Automatic adjustment of the font size, when requested
- Advanced setting: printing with a self-defined module width
- Rotation in 90° steps
- Configurable ratio from 2:1 to 3:1 for barcode types that support it
- Add-On 2/5 for barcodes like EAN-13, EAN-8, ISBN-13 and UPC-A, additional plain text line for ISBN and ISMN
- Full support for GS1 barcodes: Data integrity check, validitation of application identifiers, checking length and content of user data, verifying/calculation of check digit for SSCC and GTIN, etc.

## Supported Barcodes
**1D:** Code 128, Code 128 A, Code 128 B, Code 128 C, 2 of 5 Interleaved, Code 39, Code 39 Extended, GS1-128 (EAN-128), GTIN-13 (EAN-13), GTIN-8 (EAN-8), UPC-A, ISBN-13, ISMN, Code 93, Code 93 Extended, GTIN-14 (EAN-14), SSCC-18 (NVE), PZN, PZN8, Codabar.

**2D:** QR Code, GS1 QR Code, DataMatrix, GS1 DataMatrix, PDF417, Aztec.

## Requirements & Dependencies
- Minimum Java Version: 9
- [OpenPDF v1.3.30+](https://github.com/LibrePDF/OpenPDF) (A tiny version of the JAR ist included in everey [release](https://github.com/Barcode-Lib4J/Barcode-Lib4J/releases)
