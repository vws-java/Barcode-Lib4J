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
package de.vwsoft.barcodelib4j.twod;

import de.vwsoft.barcodelib4j.twod.aztec.AztecWriter;
import de.vwsoft.barcodelib4j.twod.datamatrix.DataMatrixWriter;
import de.vwsoft.barcodelib4j.twod.datamatrix.Dimension;
import de.vwsoft.barcodelib4j.twod.datamatrix.SymbolInfo;
import de.vwsoft.barcodelib4j.twod.datamatrix.SymbolShapeHint;
import de.vwsoft.barcodelib4j.twod.pdf417.Dimensions;
import de.vwsoft.barcodelib4j.twod.pdf417.PDF417Writer;
import de.vwsoft.barcodelib4j.twod.qrcode.ErrorCorrectionLevel;
import de.vwsoft.barcodelib4j.twod.qrcode.QRCodeWriter;
import de.vwsoft.barcodelib4j.twod.zxing.BarcodeFormat;
import de.vwsoft.barcodelib4j.twod.zxing.BitMatrix;
import de.vwsoft.barcodelib4j.twod.zxing.EncodeHintType;
import de.vwsoft.barcodelib4j.twod.zxing.Writer;

import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


/**
 * Main class of the package and a wrapper for the integrated ZXing classes.
 * <p>
 * The class provides methods and constants that facilitate the integration of 2D codes into an
 * existing application. It accepts settings in a type-safe form and validates them, catching any
 * errors beforehand. This reduces the number of possible exceptions that can be thrown during code
 * generation and helps to locate and handle them in a more differentiated way.
 * <p>
 * <b>Usage:</b>
 * <ol>
 * <li>Providing common settings such as code type (QR Code, DataMatrix, ...), the content,
 *   character set and quiet zone.</li>
 * <li>Providing type-specific settings for the 2D code type you want to create.</li>
 * <li>Check compatibility between the content and selected charset using {@link #canEncode()}.</li>
 * <li>Call {@link #buildSymbol()} to generate the drawable code symbol.</li>
 * </ol>
 * <b>Example:</b>
 * <pre>
 *     // Step 1: Common settings
 *     TwoDCode tdc = new TwoDCode();
 *     tdc.setType(TwoDType.QRCODE);
 *     tdc.setContent("Some content");
 *     tdc.setCharset(null); // include no ECI
 *     tdc.setQuietZoneSize(TwoDCode.ALL_QUIET_ZONES.get(TwoDType.QRCODE));
 *
 *     // Step 2: Type-specific settings
 *     tdc.setQRCodeVersion(TwoDCode.QRCODE_VERSION_AUTO);
 *     tdc.setQRCodeErrCorr(1);
 *
 *     // Step 3 and 4: Generate drawable symbol
 *     TwoDSymbol symbol = null;
 *     if (tdc.canEncode()) {
 *       try {
 *         symbol = tdc.buildSymbol();
 *       } catch (Exception ex) {
 *         // Handle the Exception
 *       }
 *     }
 * </pre>
 * When generating GS1 DataMatrix or GS1 QR Code, the {@link de.vwsoft.barcodelib4j.oned.GS1Validator}
 * class from the "oned" package can be used to perform advanced content validation. See the
 * {@link #setContent(String) setContent} method description for more information.
 */
public class TwoDCode implements Cloneable {

  // Common constants

  /** Map containing the minimum quiet zones for all supported 2D code types, according to
      their respective specifications, specified in modules. */
  public static final Map<TwoDType,Integer> ALL_QUIET_ZONES = new HashMap<>(6);
  static {
    ALL_QUIET_ZONES.put(TwoDType.QRCODE,          4);
    ALL_QUIET_ZONES.put(TwoDType.DATAMATRIX,      1);
    ALL_QUIET_ZONES.put(TwoDType.PDF417,          2);
    ALL_QUIET_ZONES.put(TwoDType.AZTEC,           0);
    ALL_QUIET_ZONES.put(TwoDType.GS1_QRCODE,      4);
    ALL_QUIET_ZONES.put(TwoDType.GS1_DATAMATRIX,  1);
  }



  // QR Code constants

  /** Minimum (smallest) version of QR Code {@code (21x21)} according to specification;
      value: 1. */
  public static final int QRCODE_VERSION_MIN  =  1;

  /** Maximum (largest) version of QR Code {@code (177x177)} according to specification;
      value: 40. */
  public static final int QRCODE_VERSION_MAX  = 40;

  /** Selects the smallest possible QR Code version capable of encoding the content. */
  public static final int QRCODE_VERSION_AUTO =  0;

  /** Lowest QR Code error correction level ("L"); value: 0. */
  public static final int QRCODE_ERR_CORR_MIN =  0;

  /** Highest QR Code error correction level ("H"); value: 3. */
  public static final int QRCODE_ERR_CORR_MAX =  3;

  /** Array of all QR Code sizes ranging from version 1 to 40, as defined in the specification.
      All QR Code symbol shapes are square, hence width equals height. */
  public static final TwoDSize[] QRCODE_SIZES = new TwoDSize[40];
  static {
    for (int i=39; i>=0; i--) {
      int size = 21 + 4 * i; // Formula for calculating QR Code size based on version
      QRCODE_SIZES[i] = new TwoDSize(size, size);
    }
  }



  // DataMatrix constants

  /** Smallest size of DataMatrix {@code (10x10)} according to ECC200 specification; value: 1. */
  public static final int DATAMATRIX_SIZE_MIN  =  1;

  /** Largest size of DataMatrix {@code (144x144)} according to ECC200 specification; value: 30. */
  public static final int DATAMATRIX_SIZE_MAX  = 30;

  /** Selects the smallest possible DataMatrix size capable of encoding the content. */
  public static final int DATAMATRIX_SIZE_AUTO =  0;

  /** When selected automatically, the smallest size with no priority for the shape is chosen. */
  public static final int DATAMATRIX_SHAPE_AUTO      = 0;

  /** When selected automatically, the smallest size with a SQUARE shape is chosen. */
  public static final int DATAMATRIX_SHAPE_SQUARE    = 1;

  /** When selected automatically, the smallest size with a RECTANGLE shape is chosen. */
  public static final int DATAMATRIX_SHAPE_RECTANGLE = 2;

  /** Array containing all 30 DataMatrix sizes, ranging from {@code 10x10} to {@code 144x144}.
      The sizes are sorted by width and height and include 24 square and 6 rectangular sizes. */
  public static final TwoDSize[] DATAMATRIX_SIZES = new TwoDSize[SymbolInfo.PROD_SYMBOLS.length];
  static {
    SymbolInfo[] si = SymbolInfo.PROD_SYMBOLS;
    for (int i=si.length-1; i>=0; i--)
      DATAMATRIX_SIZES[i] = new TwoDSize(si[i].getSymbolWidth(), si[i].getSymbolHeight());
    Arrays.sort(DATAMATRIX_SIZES);
  }



  // PDF417 constants

  /** Minimum number of rows in a valid PDF417 symbol; value: 3. */
  public static final int PDF417_ROWS_MIN  =  3;

  /** Maximum number of rows in a valid PDF417 symbol; value: 90. */
  public static final int PDF417_ROWS_MAX  = 90;

  /** Minimum number of columns (code words) in a valid PDF417 symbol; value: 1. */
  public static final int PDF417_COLUMNS_MIN  =  1;

  /** Maximum number of columns (code words) in a valid PDF417 symbol; value: 30. */
  public static final int PDF417_COLUMNS_MAX  = 30;

  /** Lowest PDF417 error correction level; value: 0. */
  public static final int PDF417_ERR_CORR_MIN =  0;

  /** Highest PDF417 error correction level; value: 8. */
  public static final int PDF417_ERR_CORR_MAX =  8;



  // Aztec constants

  /** Selects the smallest possible Aztec size capable of encoding the content. */
  public static final int AZTEC_SIZE_AUTO =  0;

  /** Minimum Aztec error correction level in percent; value: 5. */
  public static final int AZTEC_ERR_CORR_MIN =  5;

  /** Maximum Aztec error correction level in percent; value: 95. */
  public static final int AZTEC_ERR_CORR_MAX = 95;

  /** Array of all 4 Aztec compact sizes: {@code 15x15}, {@code 19x19}, {@code 23x23},
      {@code 27x27}. All Aztec symbol shapes are square, hence width equals height. */
  public static final TwoDSize[] AZTEC_SIZES_COMPACT = new TwoDSize[] {
      new TwoDSize(15, 15), new TwoDSize(19, 19), new TwoDSize(23, 23), new TwoDSize(27, 27) };

  /** Array of all 32 Aztec normal sizes, ranging from {@code 19x19} to {@code 151x151}.
      All Aztec symbol shapes are square, hence width equals height. */
  public static final TwoDSize[] AZTEC_SIZES_NORMAL = new TwoDSize[32];
  static {
    int k = 19;
    for (int i=0; i!=32; i++) {
      if (i == 4 || i == 11 || i == 19 || i == 26)
        k += 2;
      int size = k + i * 4;
      AZTEC_SIZES_NORMAL[i] = new TwoDSize(size, size);
    }
  }



  // Default settings

  // Common properties
  private TwoDType pType     = TwoDType.QRCODE;
  private String pContent    = "ABCD... 01234";
  private Charset pCharset   = null;             // null = "ISO 8859-1", no ECI
  private int pQuietZoneSize = 1;                // Size in modules

  // QR Code properties
  private int pQRVersion = QRCODE_VERSION_AUTO;  // Automatically selected (fit to content)
  private int pQRErrCorr = 1;                    // Error correction level set to "M"

  // DataMatrix properties
  private int pDMSize  = DATAMATRIX_SIZE_AUTO;   // Automatically selected (fit to content)
  private int pDMShape = DATAMATRIX_SHAPE_AUTO;  // Automatically selected

  // PDF417 properties
  private TwoDSize pPDSize = new TwoDSize(0, 0); // Automatically fit to content in both dimensions
  private int pPDErrCorr   = 2;                  // Error correction level set to 2

  // Aztec properties
  private int pAZSize    = AZTEC_SIZE_AUTO;      // Automatically selected (fit to content)
  private int pAZErrCorr = 23;                   // Recommended minimum error correction level in %



  /**
   * Creates a new instance with default settings.
   * <ul>
   * <li>Code type: {@link TwoDType#QRCODE}</li>
   * <li>Content: "ABCD... 01234"</li>
   * <li>Charset: {@code null} (ISO 8859-1)</li>
   * <li>Quiet zone size: 1</li>
   * </ul>
   * <p>
   * Settings by code type:
   * <ul>
   * <li>QR Code version: {@link #QRCODE_VERSION_AUTO}</li>
   * <li>QR Code error correction level: 1 (Medium or "M")</li>
   * <li>DataMatrix size: {@link #DATAMATRIX_SIZE_AUTO}</li>
   * <li>DataMatrix shape: {@link #DATAMATRIX_SHAPE_AUTO}</li>
   * <li>PDF417 size: {@link TwoDSize}(0, 0)</li>
   * <li>PDF417 error correction level: 2</li>
   * <li>Aztec size: {@link #AZTEC_SIZE_AUTO}</li>
   * <li>Aztec error correction level: 23</li>
   * </ul>
   */
  public TwoDCode() {
  }



  /**
   * Sets the type of the 2D code.
   *
   * @param codeType the type of the 2D code to set
   * @throws IllegalArgumentException if the provided codeType is {@code null}
   */
  public void setType(TwoDType codeType) {
    if (codeType == null)
      throw new IllegalArgumentException("Code type cannot be null");
    pType = codeType;
  }



  /**
   * Sets the content to be encoded in the 2D code.
   * <p>
   * <b>GS1 QR Code</b> + <b>GS1 DataMatrix</b>: For GS1 formatted data, ASCII 29 must be used as
   * FNC1 separator character where necessary. Note that the content should NOT include the leading
   * FNC1, which indicates the GS1 data structure. It is recommended to validate the content using
   * the {@link de.vwsoft.barcodelib4j.oned.GS1Validator} class before setting. Example:
   * <pre>
   *    import de.vwsoft.barcodelib4j.oned.BarcodeException;
   *    import de.vwsoft.barcodelib4j.oned.GS1Validator;
   *
   *    String gs1 = "(01)01234567890128(15)191231";
   *    String validated = null;
   *    try {
   *      validated = new GS1Validator(gs1, (char)29).getContent();
   *    } catch (BarcodeException ex) {
   *      // Validation failed
   *    }
   *
   *    if (validated != null)
   *      // ... use this method to set the validated value
   * </pre>
   *
   * @param content the content to be encoded
   * @throws IllegalArgumentException if the provided content is {@code null} or empty
   * @see de.vwsoft.barcodelib4j.oned.GS1Validator
   */
  public void setContent(String content) {
    if (content == null)
      throw new IllegalArgumentException("Content cannot be null");
    if (content.isEmpty())
      throw new IllegalArgumentException("Content cannot be empty");
    pContent = content;
  }



  /**
   * Specifies the character set used to encode the content in the 2D code.
   * <p>
   * If set to {@code null} (default), no character set information (ECI) is built into the code,
   * and the default ISO 8859-1 character set is used. This is the default character set for all
   * supported 2D types unless another character set is explicitly specified.
   * <p>
   * <b>GS1 QR Code</b> + <b>GS1 DataMatrix</b>: For GS1 code types, specifying a character set is
   * usually unnecessary since GS1 data typically consists only of ASCII characters, compatible with
   * ISO 8859-1.
   *
   * @param charset the character set to be used for encoding the content, or {@code null} to
   *                indicate the default ISO 8859-1 charset with no ECI
   */
  public void setCharset(Charset charset) {
    // The Writer#encode() methods can potentially throw various exceptions, so at this point, we
    // save at least one of them and move the handling of those exceptions thrown by
    // Charset#forName(String) outside of this class by having this method accept a pre-existing
    // Charset instance instead of a String. Also, some writers can perform various unwanted
    // fallbacks that lead to results that are not intended, if Charset#forName(String) fails.
    pCharset = charset;
  }



  /**
   * Sets the size of the quiet zone for the 2D code.
   * <p>
   * The quiet zone is the blank space around the 2D code that helps prevent interference or
   * misreading of the code by surrounding elements. The size of the quiet zone is specified in
   * modules. A module is the smallest single element in a 2D code, typically representing a single
   * dot within the code.
   * <p>
   * Standard quiet zones for all supported 2D codes are available in the {@link ALL_QUIET_ZONES}
   * array.
   * <p>
   * Note: For QR Codes, the specification defines a minimum quiet zone size of 4 modules. Although
   * this minimum value is often used, it is not strictly adhered to and can vary depending on the
   * application or implementation.
   *
   * @param sizeInModules the size of the quiet zone in modules
   * @throws IllegalArgumentException if the specified size is negative
   */
  public void setQuietZoneSize(int sizeInModules) {
    if (sizeInModules < 0)
      throw new IllegalArgumentException("Quiet zone size cannot be negative: " + sizeInModules);
    pQuietZoneSize = sizeInModules;
  }



  /**
   * Sets the version of the QR Code.
   * <p>
   * The version determines the size and data capacity of the QR Code. Valid values range from 1 to
   * 40 inclusive. A version of 1 means a {@code 21x21} matrix and each subsequent version increases
   * the matrix size by 4 modules. Version 40 represents a {@code 177x177} matrix. Use
   * {@link #QRCODE_VERSION_AUTO} instead of a version number to automatically select the smallest
   * version capable of encoding the content.
   *
   * @param version the version number of the QR Code (1 to 40) or {@link #QRCODE_VERSION_AUTO}
   * @throws IllegalArgumentException  if the specified version is not within the valid range
   *                                   and does not match {@link #QRCODE_VERSION_AUTO}
   * @see #QRCODE_SIZES
   */
  public void setQRCodeVersion(int version) {
    if ((version < QRCODE_VERSION_MIN || version > QRCODE_VERSION_MAX) &&
        version != QRCODE_VERSION_AUTO)
      throw new IllegalArgumentException("Invalid QR Code version: " + version);
    pQRVersion = version;
  }



  /**
   * Sets the error correction level for QR Code.
   * <p>
   * The error correction level specifies the amount of redundant data added to the symbol to
   * enable error recovery. QR Codes support four levels of error correction: L (Low - ~7%),
   * M (Medium - ~15%), Q (Quartile - ~25%), and H (High - ~30%). The higher the error correction
   * level, the more redundancy is added, resulting in a higher resistance to errors, but also in a
   * larger symbol size.
   * <p>
   * The error correction level can be set to any value between 0 and 3 inclusive: 0 for "L",
   * 1 for "M" (default), 2 for "Q", and 3 for "H".
   *
   * @param errorCorrectionLevel the error correction level to set for QR Code in the range 0-3
   * @throws IllegalArgumentException  if the specified error correction level is outside the valid
   *                                   range
   */
  public void setQRCodeErrCorr(int errorCorrectionLevel) {
    if (errorCorrectionLevel < QRCODE_ERR_CORR_MIN || errorCorrectionLevel > QRCODE_ERR_CORR_MAX)
      throw new IllegalArgumentException(
          "Invalid QR Code error correction level: " + errorCorrectionLevel);
    pQRErrCorr = errorCorrectionLevel;
  }



  /**
   * Sets the size of the DataMatrix symbol.
   * <p>
   * The size index [minus 1] represents the index of the DataMatrix symbol size in the
   * {@link #DATAMATRIX_SIZES} array. The value ranges from 1 to 30, inclusive, where 1 corresponds
   * to the smallest size {@code (10x10)} and 30 corresponds to the largest size {@code (144x144)}.
   * Use {@link #DATAMATRIX_SIZE_AUTO} instead of a size index to automatically select the smallest
   * size capable of encoding the content.
   * <p>
   * Note: The DataMatrix implementation allows the use of character sets other than ISO 8859-1 only
   * when the size is set to {@link #DATAMATRIX_SIZE_AUTO}.
   *
   * @param sizeIndex the index (1 to 30) specifying the size of the DataMatrix symbol or
   *                  {@link #DATAMATRIX_SIZE_AUTO}
   * @throws IllegalArgumentException  if the specified size index is not within the valid range
   *                                   and does not match {@link #DATAMATRIX_SIZE_AUTO}
   * @see #DATAMATRIX_SIZES
   */
  public void setDataMatrixSize(int sizeIndex) {
    if ((sizeIndex < DATAMATRIX_SIZE_MIN || sizeIndex > DATAMATRIX_SIZE_MAX) &&
        sizeIndex != DATAMATRIX_SIZE_AUTO)
      throw new IllegalArgumentException("Invalid DataMatrix size index: " + sizeIndex);
    pDMSize = sizeIndex;
  }



  /**
   * Sets the shape of the DataMatrix symbol.
   * <p>
   * Valid values are:
   * <ul>
   * <li>{@link #DATAMATRIX_SHAPE_SQUARE}: Square shape</li>
   * <li>{@link #DATAMATRIX_SHAPE_RECTANGLE}: Rectangular shape</li>
   * <li>{@link #DATAMATRIX_SHAPE_AUTO}: Automatically select the shape that best fits the
   *   content</li>
   * </ul>
   *
   * @param shape the shape of the DataMatrix symbol
   * @throws IllegalArgumentException if the provided shape is not one of the valid constants
   */
  public void setDataMatrixShape(int shape) {
    if (shape != DATAMATRIX_SHAPE_AUTO &&
        shape != DATAMATRIX_SHAPE_SQUARE &&
        shape != DATAMATRIX_SHAPE_RECTANGLE)
      throw new IllegalArgumentException("Invalid DataMatrix shape: " + shape);
    pDMShape = shape;
  }



  /**
   * Sets the size of the PDF417 symbol.
   * <p>
   * The PDF417 symbol size is specified in terms of the number of columns and rows. The number of
   * columns can range from 1 to 30, and the number of rows can range from 3 to 90. If the width or
   * height value is set to {@code 0}, the respective value is automatically selected based on the
   * content to be encoded.
   *
   * @param size the size of the PDF417 symbol represented by a {@link TwoDSize} object
   * @throws IllegalArgumentException  if the provided size is {@code null}, or if the specified
   *                                   column or row have invalid values
   */
  public void setPDF417Size(TwoDSize size) {
    if (size == null)
      throw new IllegalArgumentException("PDF417 size cannot be null.");

    if ((size.width < PDF417_COLUMNS_MIN || size.width > PDF417_COLUMNS_MAX) && size.width != 0)
      throw new IllegalArgumentException("Invalid PDF417 column count: " + size.width);

    if ((size.height < PDF417_ROWS_MIN || size.height > PDF417_ROWS_MAX) && size.height != 0)
      throw new IllegalArgumentException("Invalid PDF417 row count: " + size.height);

    pPDSize = size;
  }



  /**
   * Sets the error correction level for PDF417.
   * <p>
   * The parameter specifies the amount of redundant data added to the symbol to enable error
   * recovery. The higher the error correction level, the more redundancy is added, resulting in a
   * higher resistance to errors, but also in a larger symbol size. PDF417 supports error correction
   * levels from 0 to 8.
   *
   * @param errorCorrectionLevel the error correction level to set for PDF417 in the range 0-8
   * @throws IllegalArgumentException if the specified error correction level is outside the valid
   *                                  range
   */
  public void setPDF417ErrCorr(int errorCorrectionLevel) {
    if (errorCorrectionLevel < PDF417_ERR_CORR_MIN || errorCorrectionLevel > PDF417_ERR_CORR_MAX)
      throw new IllegalArgumentException(
          "Invalid PDF417 error correction level: " + errorCorrectionLevel);
    pPDErrCorr = errorCorrectionLevel;
  }



  /**
   * Sets the size of the Aztec code symbol. The parameter can have the following values:
   * <ul>
   * <li>{@link #AZTEC_SIZE_AUTO} indicates that the smallest possible size capable of encoding the
   *   content is selected automatically (default).</li>
   * <li>Negative numbers (-1, -2, -3, -4) represent compact Aztec codes.
   *   See: {@link #AZTEC_SIZES_COMPACT}.</li>
   * <li>Positive numbers (1, 2, ... 32) represent normal (non-compact) Aztec codes.
   *   See: {@link #AZTEC_SIZES_NORMAL}.</li>
   * </ul>
   * @param sizeIndex  the index specifying the size of the Aztec code or {@link #AZTEC_SIZE_AUTO}
   * @throws IllegalArgumentException  if the specified size index is not within the valid ranges
   *                                   and does not match {@link #AZTEC_SIZE_AUTO}
   */
  public void setAztecSize(int sizeIndex) {
    if ((sizeIndex < -4 || sizeIndex > 32) && sizeIndex != AZTEC_SIZE_AUTO)
      throw new IllegalArgumentException("Invalid Aztec size index: " + sizeIndex);
    pAZSize = sizeIndex;
  }



  /**
   * Sets the error correction level for Aztec, specified in percent.
   * <p>
   * The error correction level determines the amount of redundancy data added to the symbol for
   * error detection and correction. Aztec symbols support error correction levels ranging from
   * 5% to 95%. Higher error correction levels add more redundancy data, increasing the symbol's
   * ability to withstand errors but also increasing its size.
   *
   * @param errorCorrectionLevel  the error correction level for Aztec, specified in percent
   * @throws IllegalArgumentException  if the specified error correction level is outside the valid
   *                                   range
   */
  public void setAztecErrCorr(int errorCorrectionLevel) {
    if (errorCorrectionLevel < AZTEC_ERR_CORR_MIN || errorCorrectionLevel > AZTEC_ERR_CORR_MAX)
      throw new IllegalArgumentException(
          "Invalid Aztec error correction level: " + errorCorrectionLevel);
    pAZErrCorr = errorCorrectionLevel;
  }



  /**
   * Checks if the specified charset can encode the given content.
   * <p>
   * If no charset is specified (i.e., {@code null}), ISO 8859-1 is checked as this is the default
   * charset used by all supported 2D code implementations.
   * <p>
   * For several reasons, it is recommended to call this method before building the 2D code symbol:
   * <ul>
   * <li>Some 2D code implementations (QR Code, Aztec) will generate the symbol even if the content
   *   cannot be encoded correctly with the specified charset, resulting in a 2D code that no longer
   *   contains the original information.</li>
   * <li>In contrast, other implementations (DataMatrix, PDF417) will properly throw an exception if
   *   the content cannot be encoded. Such exceptions can be avoided by having this method perform
   *   the check in advance.</li>
   * </ul>
   *
   * @return {@code true} if the specified charset can encode the content, {@code false} otherwise.
   */
  public boolean canEncode() {
    Charset cs = pCharset != null ? pCharset : StandardCharsets.ISO_8859_1;
    try {
      cs.newEncoder().encode(CharBuffer.wrap(pContent));
      return true;
    } catch (CharacterCodingException ex) {
      return false;
    }
  }



  /**
   * Generates a drawable 2D symbol based on the provided properties.
   * <p>
   * Any exceptions thrown by this method are forwarded exceptions caused by the internal call to
   * ZXing's {@code Writer#encode()}. Since this method is implemented differently in the ZXing
   * classes depending on the code type, it can throw different exceptions, even though only
   * {@code WriterException} is declared by the {@code Writer#encode()} methods. Therefore, all
   * exceptions are forwarded as a generalized {@code java.lang.Exception}.
   * <p>
   * For example, the {@code DataMatrixWriter} and the {@code PDF417Writer} may throw different
   * exceptions, even for the same reason, such as when non-encodable characters are detected in the
   * content. While the first {@code Writer} throws the declared {@code WriterException}, the second
   * may throw an {@code IllegalArgumentException}.
   * <p>
   * The {@code Writer} implementations of the other two code types (QR Code and Aztec) do not throw
   * an exception at all when encountering non-encodable characters, but instead encode the content
   * with a loss of information due to incorrect character encoding. This underlines the importance
   * of checking this first by calling {@link #canEncode()}.
   *
   * @return a {@link TwoDSymbol} object representing the generated 2D symbol
   * @throws Exception if an error occurs during the symbol generation process
   */
  @SuppressWarnings("deprecation")
  public TwoDSymbol buildSymbol() throws Exception {

    // Step 1: Set ZXing's hints that are common to all code types.
    Map<EncodeHintType,Object> hints = new HashMap<>();
    // Set the MARGIN value explicitly to "0" here. This hint is not implemented by all code types
    // or may have a predefined value. We will add any quiet zones ourselves.
    hints.put(EncodeHintType.MARGIN, 0);
    hints.put(EncodeHintType.GS1_FORMAT, pType.isGS1());
    if (pCharset != null)
      hints.put(EncodeHintType.CHARACTER_SET, pCharset.name());

    // Step 2: - Assign matching ZXing's BarcodeFormat constant and a Writer implementation
    //         - Configure hints based on the code type
    BarcodeFormat barcodeFormat = null;
    Writer writer = null;
    switch (pType) {
      case GS1_QRCODE:
      case QRCODE:
        barcodeFormat = BarcodeFormat.QR_CODE;
        writer = new QRCodeWriter();
        if (pQRVersion != QRCODE_VERSION_AUTO)
          hints.put(EncodeHintType.QR_VERSION, pQRVersion);
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.values()[pQRErrCorr]);
        break;
      case GS1_DATAMATRIX:
      case DATAMATRIX:
        barcodeFormat = BarcodeFormat.DATA_MATRIX;
        writer = new DataMatrixWriter();
        if (pDMSize != DATAMATRIX_SIZE_AUTO) { // No charset setting/ECI is supported in this mode
          TwoDSize dmSize = DATAMATRIX_SIZES[pDMSize - 1];
          Dimension dim = new Dimension(dmSize.width, dmSize.height);
          hints.put(EncodeHintType.MIN_SIZE, dim);
          hints.put(EncodeHintType.MAX_SIZE, dim);
        } else {
          hints.put(EncodeHintType.DATA_MATRIX_SHAPE, SymbolShapeHint.values()[pDMShape]);
          if (pCharset != null) // Charset setting/ECI is only supported in compact mode
            hints.put(EncodeHintType.DATA_MATRIX_COMPACT, true);
        }
        break;
      case PDF417:
        barcodeFormat = BarcodeFormat.PDF_417;
        writer = new PDF417Writer();
        int minCols = pPDSize.width  != 0 ? pPDSize.width  : PDF417_COLUMNS_MIN;
        int maxCols = pPDSize.width  != 0 ? pPDSize.width  : PDF417_COLUMNS_MAX;
        int minRows = pPDSize.height != 0 ? pPDSize.height : PDF417_ROWS_MIN;
        int maxRows = pPDSize.height != 0 ? pPDSize.height : PDF417_ROWS_MAX;
        hints.put(EncodeHintType.PDF417_DIMENSIONS,
            new Dimensions(minCols, maxCols, minRows, maxRows));
        hints.put(EncodeHintType.ERROR_CORRECTION, pPDErrCorr);
        break;
      case AZTEC:
        barcodeFormat = BarcodeFormat.AZTEC;
        writer = new AztecWriter();
        hints.put(EncodeHintType.AZTEC_LAYERS, pAZSize);
        hints.put(EncodeHintType.ERROR_CORRECTION, pAZErrCorr);
    }

    // Step 3: We use the selected "BarcodeFormat", "Writer" and hints established in previous steps
    // to generate a "BitMatrix". During this process various exceptions may arise, depending on the
    // specific implementation of the "Writer", while "WriterException" is just one example. To
    // ensure comprehensive error handling, we catch and delegate any potential exceptions by using
    // "throw Exception" in this method's declaration.
    BitMatrix bitMatrix = writer.encode(pContent, barcodeFormat, 0, 0, hints);

    return new TwoDSymbol(bitMatrix, pQuietZoneSize);
  }



  /** {@return the type of the 2D code} */
  public TwoDType getType() {
    return pType;
  }

  /** {@return the content encoded in the 2D code} */
  public String getContent() {
    return pContent;
  }

  /** {@return the character set used for encoding the content} */
  public Charset getCharset() {
    return pCharset;
  }

  /** {@return the size of the quiet zone around the 2D code} */
  public int getQuietZoneSize() {
    return pQuietZoneSize;
  }

  /** {@return the version of the QR Code} */
  public int getQRCodeVersion() {
    return pQRVersion;
  }

  /** {@return the error correction level of the QR Code} */
  public int getQRCodeErrCorr() {
    return pQRErrCorr;
  }

  /** {@return the size of the DataMatrix symbol} */
  public int getDataMatrixSize() {
    return pDMSize;
  }

  /** {@return the shape of the DataMatrix symbol} */
  public int getDataMatrixShape() {
    return pDMShape;
  }

  /** {@return the size of the PDF417 symbol} */
  public TwoDSize getPDF417Size() {
    return pPDSize;
  }

  /** {@return the error correction level of the PDF417} */
  public int getPDF417ErrCorr() {
    return pPDErrCorr;
  }

  /** {@return the size index of the Aztec symbol} */
  public int getAztecSize() {
    return pAZSize;
  }

  /** {@return the error correction level of the Aztec} */
  public int getAztecErrCorr() {
    return pAZErrCorr;
  }



  /**
   * {@return a copy of this object} The returned copy can be considered and used as a "deep copy".
   */
  @Override
  public Object clone() {
    try { return super.clone(); } catch (Exception e) { return null; }
  }

}
