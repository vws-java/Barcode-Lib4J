<div align="center">
  <img width="500" height="110" src="https://github.com/user-attachments/assets/ec774370-63be-4db3-9cb4-e6561918a807" alt="Barcode-Lib4J Logo">

  [![GitHub release](https://img.shields.io/github/release/vws-java/Barcode-Lib4J.svg)](https://github.com/vws-java/Barcode-Lib4J/releases)
  [![Maven Central](https://img.shields.io/maven-central/v/de.vwsoft/barcodelib4j.svg)](https://central.sonatype.com/artifact/de.vwsoft/barcodelib4j)
  [![Java](https://img.shields.io/badge/Java-9%2B-blue.svg)](https://www.oracle.com/java/)
  [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

</div>
<br>
<p align="center">
  <strong><em>Barcode-Lib4J &ndash; Java library for drawing, printing and storing 1D and 2D barcodes as vector (PDF, EPS, SVG) and raster (PNG, BMP, JPG) images. Proven in practice through use in our commercial software products.</em></strong>
</p>
<br>
<div align="center">
  <img width="868" height="160" style="width:39.06rem;height:7.2rem" src="https://github.com/user-attachments/assets/23e4475f-3c24-4bf4-83c3-8b923ecbab07" alt="Barcode-Lib4J - Key Features">
</div>
<br>
<br>

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
<br>

## Supported Barcode Formats
| 1D Barcodes | 2D Barcodes
|-------------|-------------
| Code 128, Code 128 A, Code 128 B, Code 128 C | QR Code
| 2 of 5 Interleaved, Code 39, Code 39 Extended | GS1 QR Code
| GS1-128 (EAN-128), GTIN-13 (EAN-13), GTIN-8 (EAN-8) | DataMatrix
| UPC-A, ISBN-13, ISMN | GS1 DataMatrix
| Code 93, Code 93 Extended, GTIN-14 (EAN-14) | PDF417
| SSCC-18 (NVE), PZN, PZN8, Codabar | Aztec
<br>

## Image Output Formats
| Format | Type | CMYK colors | RGB colors | Transparency support
|--------|---------------|------|-----|------------------------
| **PDF** | Vector | ✓ | ✓ | ✓
| **EPS** | Vector | ✓ | ✓ | ✓
| **SVG** | Vector | - | ✓ | ✓
| **PNG** | Raster | - | ✓ | ✓
| **BMP** | Raster | - | ✓ | -
| **JPG** | Raster | - | ✓ | -
<br>

## Requirements & Dependencies
- Minimum Java Version: 9
- Optional: OpenPDF version 1.3.0 to 2.4.0 is needed for PDF export - [GitHub](https://github.com/LibrePDF/OpenPDF) | [Maven Central](https://central.sonatype.com/artifact/com.github.librepdf/openpdf) - Please ensure the used version is compatible with your Java version!
<br>

## Getting Started
### &#9656;&#9656; Method 1: Maven Repository
Add the dependency via [Maven Central](https://central.sonatype.com/artifact/de.vwsoft/barcodelib4j) - copy the snippet for Maven, Gradle, or your preferred build tool

### &#9656;&#9656; Method 2: Manual Download
Download the latest ZIP file with source code, precompiled JAR and JavaDocs from the [Official Product Website](https://www.vw-software.com/java-barcode-library/)

### &#9656;&#9656; RECOMMENDED: Tutorial & Examples
Tutorial and examples: [Official Product Website](https://www.vw-software.com/java-barcode-library/)  (scroll down)

<br>

## Credits
All 2D barcodes (QR Code, DataMatrix, PDF417 and Aztec) are provided by some slightly modified and seamlessly integrated classes from the [ZXing](https://github.com/zxing/zxing) project. A factory class has been added to it to support above features as well as GS1 versions of QR Code and DataMatrix.



