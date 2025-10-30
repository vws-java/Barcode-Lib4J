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
package de.vwsoft.barcodelib4j.oned;


/**
 * Enumeration of all supported 1D barcode types ordered alphabetically by type name.
 * <p>
 * Each barcode type has a unique integer ID which can be used for efficient storage in a file or
 * database. The IDs are small positive integers that can be safely cast to byte if needed.
 * See {@link #getID()} and {@link #valueOf(int id)}.
 */
public enum BarcodeType {

  /** Barcode type "Codabar" */                CODABAR  ("Codabar",                7),
  /** Barcode type "Code 11" */                CODE11   ("Code 11",               12),
  /** Barcode type "Code 128" */               CODE128  ("Code 128",               6),
  /** Barcode type "Code 128 A" */             CODE128A ("Code 128 A",            20),
  /** Barcode type "Code 128 B" */             CODE128B ("Code 128 B",             8),
  /** Barcode type "Code 128 C" */             CODE128C ("Code 128 C",             9),
  /** Barcode type "Code 39" */                CODE39   ("Code 39",                5),
  /** Barcode type "Code 39 Extended" */       CODE39E  ("Code 39 Extended",      13),
  /** Barcode type "Code 93" */                CODE93   ("Code 93",               10),
  /** Barcode type "Code 93 Extended" */       CODE93E  ("Code 93 Extended",      11),
  /** Barcode type "EAN-13 (GTIN-13)" */       EAN13    ("EAN-13 (GTIN-13)",       1),
  /** Barcode type "EAN-14 (GTIN-14)" */       EAN14    ("EAN-14 (GTIN-14)",      18),
  /** Barcode type "EAN-8 (GTIN-8)" */         EAN8     ("EAN-8 (GTIN-8)",         2),
  /** Barcode type "GS1-128 (UCC/EAN-128)" */  EAN128   ("GS1-128 (UCC/EAN-128)", 17),
  /** Barcode type "Interleaved 2 of 5" */     ITF      ("Interleaved 2 of 5",     4),
  /** Barcode type "ISBN-13" */                ISBN13   ("ISBN-13",               15),
  /** Barcode type "ISMN" */                   ISMN     ("ISMN",                  21),
  /** Barcode type "PZN" */                    PZN      ("PZN",                   14),
  /** Barcode type "PZN8" */                   PZN8     ("PZN8",                  16),
  /** Barcode type "SSCC-18 (NVE/EAN-18)" */   SSCC18   ("SSCC-18 (NVE/EAN-18)",  19),
  /** Barcode type "UPC-A" */                  UPCA     ("UPC-A",                  3),
  /** Barcode type "UPC-E" */                  UPCE     ("UPC-E",                 22);



  private final String myTypeName;
  private final int myID;



  private BarcodeType(String name, int id) {
    myTypeName = name;
    myID = id;
  }



  // Perform the creation of new instances within this enum class
  // to centralize and simplify the management of barcode types.
  Barcode newInstance() {
    Barcode bc = null;
    switch (this) {
      case CODABAR    : bc = new ImplCodabar();    break;
      case CODE11     : bc = new ImplCode11();     break;
      case CODE128    : bc = new ImplCode128();    break;
      case CODE128A   : bc = new ImplCode128A();   break;
      case CODE128B   : bc = new ImplCode128B();   break;
      case CODE128C   : bc = new ImplCode128C();   break;
      case CODE39     : bc = new ImplCode39();     break;
      case CODE39E    : bc = new ImplCode39E();    break;
      case CODE93     : bc = new ImplCode93();     break;
      case CODE93E    : bc = new ImplCode93E();    break;
      case EAN13      : bc = new ImplEAN13();      break;
      case EAN14      : bc = new ImplEAN14();      break;
      case EAN8       : bc = new ImplEAN8();       break;
      case EAN128     : bc = new ImplEAN128();     break;
      case ITF        : bc = new ImplITF();        break;
      case ISBN13     : bc = new ImplISBN13();     break;
      case ISMN       : bc = new ImplISMN();       break;
      case PZN        : bc = new ImplPZN();        break;
      case PZN8       : bc = new ImplPZN8();       break;
      case SSCC18     : bc = new ImplSSCC18();     break;
      case UPCA       : bc = new ImplUPCA();       break;
      case UPCE       : bc = new ImplUPCE();       break;
    }
    return bc;
  }



  // This static cache helps to avoid repeated array creation that occurs internally in 'values()'.
  // The constant is used only by the 'valueOf(int)' method and is therefore declared next to it.
  private static final BarcodeType[] cachedValues = values();
  /**
   * Returns the enum constant of this class associated with the specified integer ID.
   *
   * @param id the ID of the enum constant to be returned.
   * @return the enum constant associated with the specified ID
   * @throws IllegalArgumentException  if this enum class has no constant associated
   *                                   with the specified ID
   */
  public static BarcodeType valueOf(int id) {
    for (BarcodeType type : cachedValues)
      if (type.getID() == id)
        return type;
    throw new IllegalArgumentException("Invalid barcode type ID: " + id);
  }



  /**
   * {@return the integer ID associated with this barcode type}
   *
   * @see #valueOf(int id)
   */
  public int getID() {
    return myID;
  }



  /**
   * {@return the name of this barcode type}
   */
  public String getTypeName() {
    return myTypeName;
  }



  /**
   * {@return a shortened version of the name of this barcode type}
   * <p>
   * If there is a part enclosed in parentheses, it is truncated. For example, if the type name is
   * "EAN-13 (GTIN-13)", this method will return "EAN-13".
   */
  public String getTypeNameShort() {
    int pos = myTypeName.indexOf('(');
    return pos > 0 ? myTypeName.substring(0, pos - 1) : myTypeName;
  }



  /**
   * {@return the name of this barcode type}
   * <p>
   * Equivalent to {@link #getTypeName()}.
   */
  @Override
  public String toString() {
    return myTypeName;
  }



  private Barcode myPrototypeInstance;
  private Barcode getPrototype() {
    if (myPrototypeInstance == null)
      myPrototypeInstance = newInstance();
    return myPrototypeInstance;
  }
  /**
   * {@return whether this barcode type supports customization of the automatically generated
   * human readable text}
   *
   * @see Barcode#setCustomText(String)
   */
  public boolean supportsCustomText() { return getPrototype().supportsCustomText(); }
  /**
   * {@return whether this barcode type supports the addition of supplementary barcode symbols}
   * <p>
   * This feature is specific to barcode types in the UPC family, such as {@link ImplUPCA UPC-A},
   * {@link ImplUPCE UPC-E}, {@link ImplEAN13 EAN-13}, {@link ImplEAN8 EAN-8},
   * {@link ImplISBN13 ISBN-13} and {@link ImplISMN ISMN}.
   *
   * @see Barcode#setAddOn(String)
   */
  public boolean supportsAddOn() { return getPrototype().supportsAddOn(); }
  /**
   * {@return whether this barcode type supports placing the human readable text above the
   * barcode symbol}
   *
   * @see Barcode#setTextOnTop(boolean)
   */
  public boolean supportsTextOnTop() { return getPrototype().supportsTextOnTop(); }
  /**
   * {@return whether this barcode type supports setting the width ratio between
   * wide and narrow bars in the barcode symbol}
   * <p>
   * This feature is used in two-width barcode types such as
   * {@link ImplITF Interleaved 2 of 5 (ITF)}, {@link ImplCode39 Code 39},
   * {@link ImplCode11 Code 11} and {@link ImplCodabar Codabar}.
   *
   * @see Barcode#setRatio(float)
   */
  public boolean supportsRatio() { return getPrototype().supportsRatio(); }
  /**
   * {@return whether this barcode type supports auto-completion}
   * <p>
   * The returned value indicates whether the {@code autoComplete} parameter has any effect
   * when calling {@link Barcode#setContent(String, boolean, boolean) setContent} on instances
   * of this barcode type.
   */
  public boolean supportsAutoCompletion() { return getPrototype().supportsAutoCompletion(); }
  /**
   * {@return whether this barcode type supports an optional checksum}
   * <p>
   * The returned value indicates whether the {@code appendOptionalChecksum} parameter has any
   * effect when calling {@link Barcode#setContent(String, boolean, boolean) setContent} on
   * instances of this barcode type.
   */
  public boolean supportsOptionalChecksum() { return getPrototype().supportsOptionalChecksum(); }

}
