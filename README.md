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
- **Zero Dependencies:** All functionality is included in a lightweight ~300 KB JAR with no external libraries required.
- **Vector & Raster Output:** Generates barcodes as PDF, EPS, SVG, PNG, BMP, and JPG — suitable for design, print, and web workflows.
- **RGB & CMYK Support:** Provides CMYK/RGB support in vector formats and RGB support in raster formats for professional color accuracy.
- **High-Precision Coordinates:** Stores vector coordinates with up to six decimal places to ensure crisp, scalable line rendering.
- **Resolution-Adaptive Bars:** Automatically adjusts bar sizes to the target resolution to ensure optimal quality at ≤ 600 dpi.
- **Ink-Spread Compensation:** Applies optional bar-width correction to counteract ink spread on inkjet printers.
- **Extensive Barcode Coverage:** Supports all popular 1D and 2D symbologies including Code 128, EAN/UPC, ITF, Code 39, QR Code, DataMatrix, PDF417, Aztec, and more.
- **GS1-Validated Encoding:** Includes comprehensive GS1 support (GS1-128, GS1 DataMatrix, GS1 QR Code) with AI validation, data-format checks, and check-digit verification.
- **Transparent Backgrounds:** Allows full transparency in EPS, PDF, SVG, and PNG images for seamless overlay.
- **Configurable Text Line:** Fully customizable human-readable text with adjustable font, size, spacing, top/bottom placement, or complete hiding.
- **Automatic Font Scaling:** Optionally adjusts text size automatically to fit the available barcode width.
- **Custom Module Size:** Provides direct control over the physical size of bars/modules for precise print output.
- **Rotation Support:** Supports rotation in 90-degree increments with correct handling of differing horizontal and vertical printer resolutions.
- **Bar Width Ratio Control:** Allows adjusting wide-to-narrow bar ratios (2.0:1 to 3.0:1) for symbologies that support variable ratios (ITF, Code 39, etc.).
- **Add-On Symbols:** Supports 2- and 5-digit add-ons for EAN-13, EAN-8, UPC-A, UPC-E, ISBN-13, and ISMN, including an optional second text line (at the top).
- **Accurate Raster Metadata:** Raster images store DPI information to preserve physical dimensions and ensure consistent printing.

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
- Minimum Java Version: 11
- **No dependencies!** All functionality included in a single, lightweight JAR (~300 KB)

<br>

## Get Started &ndash; Maven and Gradle, Examples, Best Practices
- 📦 **Installation Guide** - Maven and Gradle dependency snippets
- 🧑‍💻 **Quick Start Examples** - For 1D and 2D barcodes in RGB & CMYK, vector & raster
- 🏷️ **GS1 Examples** - For GS1-128, GS1 DataMatrix, and GS1 QR Code
- 💡 **Best Practices** - Produce reliable, scannable barcodes and avoid common pitfalls
- 📏 **Quality Guidelines** - DPI handling and resolution optimization for quality output
- 📖 **API Documentation** - Detailed Javadocs and method references

**→ Available in:** &nbsp;<b>[ENGLISH](https://www.vw-software.com/java-barcode-library/#get-started) &nbsp;|&nbsp; [GERMAN](https://www.vwsoft.de/barcode-library-for-java/#get-started)</b>

<br>
<br>

## Credits
All 2D barcodes (QR Code, DataMatrix, PDF417, and Aztec) are provided by some slightly modified and seamlessly integrated Java classes from the [ZXing](https://github.com/zxing/zxing) barcode library. A factory class has been added to support the above features as well as GS1 versions of QR Code and DataMatrix.

## Contributing
Pull requests are welcome and will be reviewed for inclusion in future releases. Since development is handled locally, contributions are applied during the release process rather than merged directly on GitHub.
