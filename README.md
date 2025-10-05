<h1 align="center">Barcode-Lib4J &nbsp;&ndash;&nbsp; Java API</h1>
<br>
<div align="center">
  <img width="500" height="110" src="https://github.com/user-attachments/assets/ec774370-63be-4db3-9cb4-e6561918a807" alt="Barcode-Lib4J Logo">

  [![GitHub release](https://img.shields.io/github/release/vws-java/Barcode-Lib4J.svg)](https://github.com/vws-java/Barcode-Lib4J/releases)
  [![Maven Central](https://img.shields.io/maven-central/v/de.vwsoft/barcodelib4j.svg)](https://central.sonatype.com/artifact/de.vwsoft/barcodelib4j)
  [![Java](https://img.shields.io/badge/Java-11%2B-blue.svg)](https://www.oracle.com/java/)
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

## Features
- Drawing and printing of 1D and 2D barcodes using Java 2D (java.awt.Graphics2D)
- Vector and raster image export with full support for format-specific features such as color models (CMYK, RGB), transparency, high coordinate precision, optional embedded preview (EPS), DPI handling, and PDF/X-compliant output for print-ready PDFs
- **Adjustment of bar widths to printer resolution (important for ensuring proper barcode quality at resolutions ≤ 600 dpi)**
- Bar width reduction/correction (useful to account for ink spread in inkjet printers)
- Customizable plain text line: font, size, spacing and editable content; Optional automatic font size adjustment; Additionally, the plain text line can be positioned at the top, bottom or completely hidden
- Advanced setting: Customizable size of the barcode bars ("module size")
- Rotation in 90-degree increments, taking into account the horizontal and vertical resolution of the target medium (e.g. when outputting to low-resolution printers)
- Configurable bar width ratio from 2:1 to 3:1 for barcode types that support this feature: Interleaved 2 of 5, Code 39 and others
- Add-Ons 2 or 5 for the barcode types EAN-13, EAN-8, UPC-A, UPC-E, ISBN-13 and ISMN; second plain text line for ISBN-13 and ISMN
- **Comprehensive support for GS1-128, GS1 DataMatrix and GS1 QR Code:** Verification of data integrity such as correctness of application identifiers, length and format of individual data elements, verification/calculation of the check digit in SSCC and GTIN and more

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
- Minimum Java Version: 11
- Optional: [OpenPDF](https://github.com/LibrePDF/OpenPDF) version 1.3.0 to 3.0.0 is needed for PDF export - Please ensure the used version is compatible with your Java version!
<br>

## Get Started
Find this on the official Barcode-Lib4J project site: &nbsp; <b>[ENGLISH](https://www.vw-software.com/java-barcode-library/#get-started) &nbsp;|&nbsp; [GERMAN](https://www.vwsoft.de/barcode-library-for-java/#get-started)</b>

- 📦 **Installation Guide** - Maven and Gradle dependency snippets
- 🧑‍💻 **Quick Start Examples** - For 1D and 2D barcodes in RGB & CMYK, vector & raster
- 🏷️ **GS1 Examples** - For GS1-128, GS1 DataMatrix, and GS1 QR Code
- 💡 **Best Practices** - Produce reliable, scannable barcodes and avoid common pitfalls
- 📏 **Quality Guidelines** - DPI handling and resolution optimization for quality output
- 📖 **API Documentation** - Detailed Javadocs and method references

<br>
<br>

## Credits
All 2D barcodes (QR Code, DataMatrix, PDF417, and Aztec) are provided by some slightly modified and seamlessly integrated Java classes from the [ZXing](https://github.com/zxing/zxing) barcode library. A factory class has been added to support the above features as well as GS1 versions of QR Code and DataMatrix.

## Contributing
Pull requests are welcome and will be reviewed for inclusion in future releases. Since development is handled locally, contributions are applied during the release process rather than merged directly on GitHub.
