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
import de.vwsoft.barcodelib4j.twod.datamatrix.SymbolShapeHint;
import de.vwsoft.barcodelib4j.twod.pdf417.Dimensions;
import de.vwsoft.barcodelib4j.twod.pdf417.PDF417Writer;
import de.vwsoft.barcodelib4j.twod.qrcode.QRCodeWriter;
import de.vwsoft.barcodelib4j.twod.zxing.BarcodeFormat;
import de.vwsoft.barcodelib4j.twod.zxing.BitMatrix;
import de.vwsoft.barcodelib4j.twod.zxing.EncodeHintType;
import de.vwsoft.barcodelib4j.twod.zxing.Writer;

import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


/**
 * Main class of the package for creating and configuring 2D codes.
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
 *     TwoDCode tdc = new TwoDCode(TwoDType.QRCODE);
 *     tdc.setContent("Hello World!");
 *     tdc.setCharset(null); // null = ISO 8859-1, includes no ECI
 *     tdc.setQuietZone(TwoDType.QRCODE.getDefaultQuietZone());
 *
 *     // Step 2: Type-specific settings
 *     tdc.setQRCodeVersion(QRCodeVersion.AUTO);
 *     tdc.setQRCodeErrCorr(QRCodeErrorCorrection.M);
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
 */
public class TwoDCode implements Cloneable {

  // Common properties
  private TwoDType myType     = TwoDType.QRCODE;
  private String myContent    = "ABCD... 01234";
  private Charset myCharset   = null;             // null = ISO 8859-1, no ECI is included
  private int myQuietZoneSize = 1;                // Size in modules

  // QR Code properties
  private QRCodeVersion myQRVersion = QRCodeVersion.AUTO;
  private QRCodeErrorCorrection myQRErrCorr = QRCodeErrorCorrection.M;

  // DataMatrix properties
  private DataMatrixSize myDMSize = DataMatrixSize.AUTO;
  private DataMatrixShape myDMShape = DataMatrixShape.AUTO;

  // PDF417 properties
  private PDF417Size myPDSize = new PDF417Size(0, 0);
  private PDF417ErrorCorrection myPDErrCorr = PDF417ErrorCorrection.EC2;

  // Aztec properties
  private AztecSize myAZSize = AztecSize.AUTO;
  private int myAZErrCorr = 23;



  /**
   * Creates a new instance with default settings.
   * <ul>
   * <li>Code type: {@link TwoDType#QRCODE}</li>
   * <li>Content: "ABCD... 01234"</li>
   * <li>Charset: {@code null} (equivalent to ISO 8859-1 with no ECI block included)</li>
   * <li>Quiet zone size: 1</li>
   * </ul>
   * <p>
   * Settings by code type:
   * <ul>
   * <li>QR Code version: {@link QRCodeVersion#AUTO}</li>
   * <li>QR Code error correction level: {@link QRCodeErrorCorrection#M}</li>
   * <li>DataMatrix size: {@link DataMatrixSize#AUTO}</li>
   * <li>DataMatrix shape: {@link DataMatrixShape#AUTO}</li>
   * <li>PDF417 size: {@link PDF417Size}(0, 0) - fit to content in both dimensions</li>
   * <li>PDF417 error correction level: {@link PDF417ErrorCorrection#EC2}</li>
   * <li>Aztec size: {@link AztecSize#AUTO}</li>
   * <li>Aztec error correction level: 23</li>
   * </ul>
   */
  public TwoDCode() {
  }



  /**
   * Creates a new instance with the specified code type.
   * <p>
   * All other settings are the same as when using the {@link #TwoDCode() default constructor}.
   *
   * @param codeType the type of the 2D code to set
   * @throws NullPointerException if the provided code type is {@code null}
   */
  public TwoDCode(TwoDType codeType) {
    setType(codeType);
  }



  /**
   * Sets the type of the 2D code.
   *
   * @param codeType the type of the 2D code to set
   * @throws NullPointerException if the provided code type is {@code null}
   */
  public void setType(TwoDType codeType) {
    myType = Objects.requireNonNull(codeType, "Code type cannot be null");
  }



  /**
   * Sets the content to be encoded in the 2D code.
   * <p>
   * <b>GS1 DataMatrix</b> and <b>GS1 QR Code</b>: It is recommended to validate the content using
   * the {@link de.vwsoft.barcodelib4j.oned.GS1Validator} class before setting. Please refer to the
   * class documentation for the expected input format requirements. Example:
   * <pre>
   *    import de.vwsoft.barcodelib4j.oned.BarcodeException;
   *    import de.vwsoft.barcodelib4j.oned.GS1Validator;
   *
   *    GS1Validator validator = null;
   *    try {
   *      validator = new GS1Validator(gs1Data, fnc1Char);
   *    } catch (BarcodeException ex) {
   *      // Validation failed
   *    }
   *
   *    if (validator != null)
   *      twoDCode.setContent(validator.getContent());
   * </pre>
   *
   * @param content the content to be encoded
   * @throws NullPointerException if the provided content is {@code null}
   * @throws IllegalArgumentException if the provided content is empty
   */
  public void setContent(String content) {
    Objects.requireNonNull(content, "Content cannot be null");
    if (content.isEmpty())
      throw new IllegalArgumentException("Content cannot be empty");
    myContent = content;
  }



  /**
   * Specifies the character set used to encode the content in the 2D code.
   * <p>
   * If set to {@code null} (default), no character set information (ECI) is built into the code,
   * and the ISO 8859-1 character set is used. This is the default character set for all supported
   * 2D types unless another character set is explicitly specified.
   * <p>
   * <b>DataMatrix</b>: The current implementation allows the use of character sets other than
   * ISO 8859-1 only when its size is set to {@code AUTO}.
   * <p>
   * <b>GS1 DataMatrix</b> and <b>GS1 QR Code</b>: Specifying a character set is unnecessary since
   * GS1 data consists only of ASCII characters, which are fully compatible with ISO 8859-1.
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
    myCharset = charset;
  }



  /**
   * Sets the size of the quiet zone for the 2D code.
   * <p>
   * For an explanation of what quiet zones are and the default quiet zone sizes for each 2D code
   * type, see {@link TwoDType#getDefaultQuietZone()}.
   *
   * @param sizeInModules the size of the quiet zone in modules
   * @throws IllegalArgumentException if the specified size is negative
   */
  public void setQuietZone(int sizeInModules) {
    if (sizeInModules < 0)
      throw new IllegalArgumentException("Quiet zone size cannot be negative: " + sizeInModules);
    myQuietZoneSize = sizeInModules;
  }



  /**
   * Sets the version of the QR Code.
   * <p>
   * The version determines the size and data capacity of the QR Code. Valid versions range from
   * 1 (21x21 modules) to 40 (177x177 modules). Use {@link QRCodeVersion#AUTO} to automatically
   * select the smallest version capable of encoding the content.
   *
   * @param version the version of the QR Code or {@link QRCodeVersion#AUTO}
   * @throws NullPointerException if the specified version is {@code null}
   */
  public void setQRCodeVersion(QRCodeVersion version) {
    myQRVersion = Objects.requireNonNull(version, "QR Code version cannot be null");
  }



  /**
   * Sets the error correction level for QR Code.
   * <p>
   * The error correction level specifies the amount of redundant data added to the symbol to
   * enable error recovery. QR Codes support four levels of error correction: L (Low - ~7%),
   * M (Medium - ~15%), Q (Quartile - ~25%), and H (High - ~30%). The higher the error correction
   * level, the more redundancy is added, resulting in a higher resistance to errors, but also in a
   * larger symbol size.
   *
   * @param errorCorrectionLevel the error correction level to set for QR Code
   * @throws NullPointerException if the specified error correction level is {@code null}
   */
  public void setQRCodeErrCorr(QRCodeErrorCorrection errorCorrectionLevel) {
    myQRErrCorr = Objects.requireNonNull(errorCorrectionLevel,
        "QR Code error correction level cannot be null");
  }



  /**
   * Sets the size of the DataMatrix symbol.
   * <p>
   * The size determines the data capacity of the DataMatrix symbol. Valid sizes range from 10x10
   * modules to 144x144 modules. The 30 available sizes include 24 square and 6 rectangular
   * variants. Use {@link DataMatrixSize#AUTO} to automatically select the smallest size capable of
   * encoding the content.
   *
   * @param size the size of the DataMatrix symbol or {@link DataMatrixSize#AUTO}
   * @throws NullPointerException if the specified size is {@code null}
   */
  public void setDataMatrixSize(DataMatrixSize size) {
    myDMSize = Objects.requireNonNull(size, "DataMatrix size cannot be null");
  }



  /**
   * Sets the shape of the DataMatrix symbol.
   * <p>
   * Valid values are:
   * <ul>
   * <li>{@link DataMatrixShape#SQUARE} - Square shape</li>
   * <li>{@link DataMatrixShape#RECTANGLE} - Rectangular shape</li>
   * <li>{@link DataMatrixShape#AUTO} - Automatically select the shape that best fits the
   *   content</li>
   * </ul>
   * <p>
   * Note: This setting is ignored when a fixed size (not {@code AUTO}) is specified via
   * {@link #setDataMatrixSize(DataMatrixSize) setDataMatrixSize},
   * as the size already determines whether the symbol is square or rectangular.
   *
   * @param shape the shape of the DataMatrix symbol
   * @throws NullPointerException if the provided shape is {@code null}
   */
  public void setDataMatrixShape(DataMatrixShape shape) {
    myDMShape = Objects.requireNonNull(shape, "DataMatrix shape cannot be null");
  }



  /**
   * Sets the size of the PDF417 symbol.
   * <p>
   * The PDF417 symbol size is specified in terms of the number of columns and rows. The number of
   * columns can range from 1 to 30, and the number of rows can range from 3 to 90. If the columns
   * or rows value is set to {@code 0}, the respective value is automatically selected based on the
   * content to be encoded.
   *
   * @param size the size of the PDF417 symbol
   * @throws NullPointerException if the provided size is {@code null}
   */
  public void setPDF417Size(PDF417Size size) {
    myPDSize = Objects.requireNonNull(size, "PDF417 size cannot be null");
  }



  /**
   * Sets the error correction level for PDF417.
   * <p>
   * The parameter specifies the amount of redundant data added to the symbol to enable error
   * recovery. The higher the error correction level, the more redundancy is added, resulting in a
   * higher resistance to errors, but also in a larger symbol size. PDF417 supports error correction
   * levels from {@code 0} to {@code 8}.
   *
   * @param errorCorrectionLevel the error correction level to set for PDF417
   * @throws NullPointerException if the specified error correction level is {@code null}
   */
  public void setPDF417ErrCorr(PDF417ErrorCorrection errorCorrectionLevel) {
    myPDErrCorr = Objects.requireNonNull(errorCorrectionLevel,
        "PDF417 error correction level cannot be null");
  }



  /**
   * Sets the size of the Aztec code symbol.
   * <p>
   * Aztec symbols come in two types that differ in their internal structure. Compact symbols
   * (1 to 4 layers, 15x15 to 27x27 modules) have a smaller bullseye finder pattern and less
   * error correction overhead, making them efficient for small data amounts. Normal symbols
   * (1 to 32 layers, 19x19 to 151x151 modules) have a larger bullseye finder pattern and more
   * robust error correction, providing better readability and greater data capacity. Use
   * {@link AztecSize#AUTO} to automatically select the smallest size capable of encoding the
   * content, preferring compact sizes when possible.
   *
   * @param size the size of the Aztec symbol or {@link AztecSize#AUTO}
   * @throws NullPointerException if the specified size is {@code null}
   */
  public void setAztecSize(AztecSize size) {
    myAZSize = Objects.requireNonNull(size, "Aztec size cannot be null");
  }



  /**
   * Sets the error correction level for Aztec, specified in percent.
   * <p>
   * The error correction level determines the amount of redundancy data added to the symbol for
   * error detection and correction. Aztec symbols support error correction levels ranging from
   * 5% to 95%. Higher error correction levels add more redundancy data, increasing the symbol's
   * ability to withstand errors but also increasing its size.
   *
   * @param errorCorrectionLevel the error correction level for Aztec, specified in percent
   * @throws IllegalArgumentException if the specified error correction level is outside the valid
   *                                  range
   */
  public void setAztecErrCorr(int errorCorrectionLevel) {
    if (errorCorrectionLevel < 5 || errorCorrectionLevel > 95)
      throw new IllegalArgumentException(
          "Invalid Aztec error correction level: " + errorCorrectionLevel);
    myAZErrCorr = errorCorrectionLevel;
  }



  /**
   * Checks if the specified charset can encode the given content.
   * <p>
   * If no character set is specified ({@code null}), ISO 8859-1 is checked as this is the default
   * charset used by all supported 2D code implementations.
   * <p>
   * For several reasons, it is recommended to call this method before building the 2D code symbol
   * using the {@link #buildSymbol()} method:
   * <ul>
   * <li>Some 2D code implementations (QR Code, Aztec) will generate the symbol even if the content
   *   cannot be encoded correctly with the specified charset, resulting in a 2D code that no longer
   *   contains the original information.</li>
   * <li>In contrast, other implementations (DataMatrix, PDF417) will properly throw an exception if
   *   the content cannot be encoded. Such exceptions can be avoided by having this method perform
   *   the check in advance.</li>
   * </ul>
   *
   * @return {@code true} if the specified charset can encode the content, {@code false} otherwise
   */
  public boolean canEncode() {
    Charset cs = myCharset != null ? myCharset : StandardCharsets.ISO_8859_1;
    try {
      cs.newEncoder().encode(CharBuffer.wrap(myContent));
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
    hints.put(EncodeHintType.GS1_FORMAT, myType.isGS1());
    if (myCharset != null)
      hints.put(EncodeHintType.CHARACTER_SET, myCharset.name());

    // Step 2: - Assign matching ZXing's BarcodeFormat constant and a Writer implementation
    //         - Configure hints based on the code type
    BarcodeFormat barcodeFormat = null;
    Writer writer = null;
    switch (myType) {
      case GS1_QRCODE:
      case QRCODE:
        barcodeFormat = BarcodeFormat.QR_CODE;
        writer = new QRCodeWriter();
        if (myQRVersion != QRCodeVersion.AUTO)
          hints.put(EncodeHintType.QR_VERSION, myQRVersion.getNumber());
        hints.put(EncodeHintType.ERROR_CORRECTION, myQRErrCorr.name());
        break;
      case GS1_DATAMATRIX:
      case DATAMATRIX:
        barcodeFormat = BarcodeFormat.DATA_MATRIX;
        writer = new DataMatrixWriter();
        if (myDMSize != DataMatrixSize.AUTO) { // No charset setting/ECI is supported in this mode
          Dimension dim = new Dimension(myDMSize.getWidth(), myDMSize.getHeight());
          hints.put(EncodeHintType.MIN_SIZE, dim); // Deprecated but still allowed for DataMatrix
          hints.put(EncodeHintType.MAX_SIZE, dim); // Deprecated but still allowed for DataMatrix
        } else {
          hints.put(EncodeHintType.DATA_MATRIX_SHAPE, SymbolShapeHint.values()[myDMShape.getID()]);
          if (myCharset != null) // Charset setting/ECI is only supported in compact mode
            hints.put(EncodeHintType.DATA_MATRIX_COMPACT, true);
        }
        break;
      case PDF417:
        barcodeFormat = BarcodeFormat.PDF_417;
        writer = new PDF417Writer();
        int minCols = myPDSize.cols != 0 ? myPDSize.cols : PDF417Size.COLS_MIN;
        int maxCols = myPDSize.cols != 0 ? myPDSize.cols : PDF417Size.COLS_MAX;
        int minRows = myPDSize.rows != 0 ? myPDSize.rows : PDF417Size.ROWS_MIN;
        int maxRows = myPDSize.rows != 0 ? myPDSize.rows : PDF417Size.ROWS_MAX;
        hints.put(EncodeHintType.PDF417_DIMENSIONS,
            new Dimensions(minCols, maxCols, minRows, maxRows));
        hints.put(EncodeHintType.ERROR_CORRECTION, myPDErrCorr.getLevelNumber());
        break;
      case AZTEC:
        barcodeFormat = BarcodeFormat.AZTEC;
        writer = new AztecWriter();
        hints.put(EncodeHintType.AZTEC_LAYERS, myAZSize.getLayerCount());
        hints.put(EncodeHintType.ERROR_CORRECTION, myAZErrCorr);
    }

    // Step 3: We use the selected "BarcodeFormat", "Writer" and hints established in previous steps
    // to generate a "BitMatrix". During this process various exceptions may arise, depending on the
    // specific implementation of the "Writer", while "WriterException" is just one example. To
    // ensure comprehensive error handling, we catch and delegate any potential exceptions by using
    // "throw Exception" in this method's declaration.
    BitMatrix bitMatrix = writer.encode(myContent, barcodeFormat, 0, 0, hints);

    return new TwoDSymbol(bitMatrix, myQuietZoneSize);
  }



  /** {@return the type of the 2D code} */
  public TwoDType getType() {
    return myType;
  }

  /** {@return the content encoded in the 2D code} */
  public String getContent() {
    return myContent;
  }

  /** {@return the character set used for encoding the content} */
  public Charset getCharset() {
    return myCharset;
  }

  /** {@return the size of the quiet zone around the 2D code} */
  public int getQuietZone() {
    return myQuietZoneSize;
  }

  /** {@return the version of the QR Code} */
  public QRCodeVersion getQRCodeVersion() {
    return myQRVersion;
  }

  /** {@return the error correction level of the QR Code} */
  public QRCodeErrorCorrection getQRCodeErrCorr() {
    return myQRErrCorr;
  }

  /** {@return the size of the DataMatrix symbol} */
  public DataMatrixSize getDataMatrixSize() {
    return myDMSize;
  }

  /** {@return the shape of the DataMatrix symbol} */
  public DataMatrixShape getDataMatrixShape() {
    return myDMShape;
  }

  /** {@return the size of the PDF417 symbol} */
  public PDF417Size getPDF417Size() {
    return myPDSize;
  }

  /** {@return the error correction level of the PDF417} */
  public PDF417ErrorCorrection getPDF417ErrCorr() {
    return myPDErrCorr;
  }

  /** {@return the size of the Aztec symbol} */
  public AztecSize getAztecSize() {
    return myAZSize;
  }

  /** {@return the error correction level of the Aztec} */
  public int getAztecErrCorr() {
    return myAZErrCorr;
  }



  /**
   * {@return a copy of this object}
   * <p>
   * The returned copy is independent of the original and can be modified without affecting it.
   * All instance members are either primitive or immutable types, or will be automatically
   * rebuilt when any of the instance's properties change. Thus, the copy can be handled
   * as if it were a "deep copy".
   */
  @Override
  public TwoDCode clone() {
    try {
      return (TwoDCode)super.clone();
    } catch (CloneNotSupportedException e) {
      throw new AssertionError("Unexpected: Clone not supported", e);
    }
  }

}
