<h1 align="center">Barcode-Lib4J &nbsp;&ndash;&nbsp; Java API</h1>
<br>
<div align="center">
  <img width="500" height="110" src="https://github.com/user-attachments/assets/ec774370-63be-4db3-9cb4-e6561918a807" alt="Barcode-Lib4J Logo">

  [![GitHub release](https://img.shields.io/github/release/vws-java/Barcode-Lib4J.svg)](https://github.com/vws-java/Barcode-Lib4J/releases)
  [![Maven Central](https://img.shields.io/maven-central/v/de.vwsoft/barcodelib4j.svg)](https://central.sonatype.com/artifact/de.vwsoft/barcodelib4j)
  [![Java](https://img.shields.io/badge/Java-9%2B-blue.svg)](https://www.oracle.com/java/)
  [![Javadoc](https://img.shields.io/badge/Javadoc-API%20Docs-blue.svg)](https://www.vw-software.com/barcode-lib4j-docs/index.html)
  [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

</div>
<br>

<h2 align="center">Proven Barcode Library for Java &ndash; now also available on GitHub & Maven</h2>

**Barcode-Lib4J** is a mature and battle-tested Java library for drawing, printing and saving 1D and 2D barcodes as vector (PDF, EPS, SVG) and raster (PNG, BMP, JPG) images. Field-proven in our long-standing software products.

<br>
<div align="center">
  <img width="780" height="160" style="width:35.1rem;height:7.2rem" src="https://github.com/user-attachments/assets/83189a55-22ff-46d5-88b4-e7f2d6f13ee3" alt="Barcode-Lib4J - Core Features">
</div>
<br>
<br>

## Features
- Drawing and printing of 1D and 2D barcodes using Java 2D (java.awt.Graphics2D)
- Vector and raster image export with full support for format-specific features such as color models (CMYK, RGB), transparency, high coordinate precision, optional embedded preview (EPS), DPI handling, and PDF/X-compliant output for print-ready PDFs
- **Adjustment of bar widths to printer resolution (important for ensuring proper barcode quality at resolutions ≤ 600 dpi)**
- Bar width reduction/correction (useful to account for ink spread in inkjet printers)
- Customizable plain text line: font, size, spacing and editable content; Optional automatic font size adjustment; Additionally, the plain text line can be positioned at the top, bottom or completely hidden
- Advanced setting: Customizable size of the barcode bars ("module size")
- Rotation in 90-degree increments, taking into account the horizontal and vertical resolution of the target medium
- Configurable bar width ratio from 2:1 to 3:1 for barcode types that support this feature: Interleaved 2 of 5, Code 39 and others
- Add-Ons 2 or 5 for the barcode types EAN-13, EAN-8, UPC-A, UPC-E, ISBN-13 and ISMN; second plain text line for ISBN-13 and ISMN
- **Comprehensive support for GS1-128, GS1 DataMatrix and GS1 QR Code:** Verification of data integrity such as correctness of application identifiers, length and format of individual data elements, verification/calculation of the check digit in SSCC and GTIN and more
- Lightweight architecture: Compiles to a single small JAR file (~300KB); Has only one dependency (OpenPDF for PDF export), fully optional at compile and runtime
<br>

## Image Output Formats
| Format | Type | CMYK colors | RGB colors | Transparency support |
|--------|---------------|------|-----|---------------------------|
| **PDF** | Vector | ✓ | ✓ | ✓ |
| **EPS** | Vector | ✓ | ✓ | ✓ |
| **SVG** | Vector | - | ✓ | ✓ |
| **PNG** | Raster | - | ✓ | ✓ |
| **BMP** | Raster | - | ✓ | - |
| **JPG** | Raster | - | ✓ | - |
- **PDF**: Compliance with PDF/X-1a:2001 for color-accurate printing in CMYK as well as PDF/X-3:2002 for RGB colors
- **EPS**: Optional embedding of a TIFF preview for display in graphic applications without their own EPS interpreter
- **PDF, EPS, SVG**: Coordinates are stored with up to six decimal places for high precision
- **PNG, BMP, JPG**: DPI resolution is stored in the file header, thereby preserving both the target resolution and the original dimensions of the graphic for later printing; No aliasing, distortion or other unwanted artifacts
<br>

## Supported Barcode Formats
| 1D Barcodes | 2D Barcodes |
|-------------|-------------|
| Code 128, Code 128 A, Code 128 B, Code 128 C | QR Code |
| 2 of 5 Interleaved, Code 39, Code 39 Extended | GS1 QR Code |
| GS1-128 (EAN-128), GTIN-13 (EAN-13), GTIN-8 (EAN-8) | DataMatrix |
| UPC-A, UPC-E, ISBN-13, ISMN | GS1 DataMatrix |
| Code 93, Code 93 Extended, GTIN-14 (EAN-14) | PDF417 |
| SSCC-18 (NVE), PZN, PZN8, Codabar, Code 11 | Aztec |
<br>

## Required Java Version & Dependencies
- Minimum Java Version: 9
- Optional: OpenPDF version 1.3.0 to 2.4.0 is needed for PDF export - [GitHub](https://github.com/LibrePDF/OpenPDF) | [Maven Central](https://central.sonatype.com/artifact/com.github.librepdf/openpdf) - Please ensure the used version is compatible with your Java version! Also note that version 3.0.0 and higher is **not compatible** with Barcode-Lib4J.
<br>

## Installation
**Maven:**
```xml
<dependency>
    <groupId>de.vwsoft</groupId>
    <artifactId>barcodelib4j</artifactId>
    <version>2.0.2</version>
</dependency>
```
**Gradle:**
```gradle
implementation 'de.vwsoft:barcodelib4j:2.0.2'
```
**Alternative methods:**
- Add the dependency to other build tools and/or browse all versions: [Maven Central](https://central.sonatype.com/artifact/de.vwsoft/barcodelib4j)
- Directly download Java sources, Javadoc, and a precompiled JAR: [GitHub Releases](../../releases)

<br>

## Getting Started
### RECOMMENDED: Best Practices & Examples
Follow best practices for **reliable, scannable barcodes** and avoid common pitfalls &ndash; including proper DPI handling for printing and raster image generation, RGB vs. CMYK color choice, and validation of Application Identifiers and data elements for GS1 barcodes (GS1-128, GS1 DataMatrix, GS1 QR Code) &ndash; in our [Essential Tutorial & Examples](https://www.vw-software.com/java-barcode-library/#get-started)

### Quick Start Example &ndash; Generating a QR Code as SVG and EPS (CMYK)
```java
import java.awt.*;
import java.io.*;
import de.vwsoft.barcodelib4j.image.*;
import de.vwsoft.barcodelib4j.twod.*;


public class QRCodeExample {
  public static void main(String[] args) throws Exception {
    final double sizeMM = 40.0; // QR Code size: 40x40 millimeters

    TwoDCode tdc = new TwoDCode(); // Defaults to QR Code
    tdc.setContent("Barcode-Lib4J Demo");

    ImageCreator imageCreator = new ImageCreator(sizeMM, sizeMM);
    Graphics2D g2d = imageCreator.getGraphics2D();
    tdc.buildSymbol().draw(g2d, 0.0, 0.0, sizeMM, sizeMM);

    try (FileOutputStream svgOut = new FileOutputStream("sample-qrcode.svg");
         FileOutputStream epsOut = new FileOutputStream("sample-qrcode.eps")) {
      imageCreator.writeSVG(svgOut);
      imageCreator.writeEPS(epsOut, ImageCreator.COLORSPACE_CMYK);
    }
  }
}
```
<div align="center">
  <p><strong>Resulting QR Code:</strong></p>
  <img width="567" height="567" style="width:40mm;height:40mm" src="https://github.com/user-attachments/assets/1916ab09-4004-4ef2-bc27-135263d3cc87" alt="Generated QR Code Example">
</div>

<br>
<br>

## Credits
All 2D barcodes (QR Code, DataMatrix, PDF417, and Aztec) are provided by some slightly modified and seamlessly integrated Java classes from the [ZXing](https://github.com/zxing/zxing) barcode library. A factory class has been added to support the above features as well as GS1 versions of QR Code and DataMatrix.

## Contributing
Pull requests and suggestions are welcome. Development is handled locally, so submitted contributions will be reviewed and may be included in a future release.
