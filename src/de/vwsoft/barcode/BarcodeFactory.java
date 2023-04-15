/*
 * Copyright 2023 by Viktor Wedel, https://www.vwsoft.de/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.vwsoft.barcode;


public class BarcodeFactory {

  // all supported barcode types
  public static final int CODETYPE_EAN13     =  0;
  public static final int CODETYPE_EAN8      =  1;
  public static final int CODETYPE_UPCA      =  2;
  public static final int CODETYPE_2OF5      =  3;
  public static final int CODETYPE_CODE39    =  4;
  public static final int CODETYPE_CODE128   =  5;
  public static final int CODETYPE_CODABAR   =  6;
  public static final int CODETYPE_CODE128B  =  7;
  public static final int CODETYPE_CODE128C  =  8;
  public static final int CODETYPE_CODE93    =  9;
  public static final int CODETYPE_CODE93E   = 10;
  public static final int CODETYPE_CODE11    = 11;
  public static final int CODETYPE_CODE39E   = 12;
  public static final int CODETYPE_PZN       = 13;
  public static final int CODETYPE_ISBN13    = 14;
  public static final int CODETYPE_PZN8      = 15;
  public static final int CODETYPE_EAN128    = 16;
  public static final int CODETYPE_EAN14     = 17;
  public static final int CODETYPE_SSCC18    = 18;
  public static final int CODETYPE_CODE128A  = 19;
  public static final int CODETYPE_ISMN      = 20;

  // all barcode types sorted alphabetically
  private static final BarcodeType[] BARCODES = {
    new BarcodeType("2 of 5 Interleaved", CODETYPE_2OF5),
    new BarcodeType("Codabar", CODETYPE_CODABAR),
    new BarcodeType("Code 11", CODETYPE_CODE11),
    new BarcodeType("Code 128", CODETYPE_CODE128),
    new BarcodeType("Code 128 A", CODETYPE_CODE128A),
    new BarcodeType("Code 128 B", CODETYPE_CODE128B),
    new BarcodeType("Code 128 C", CODETYPE_CODE128C),
    new BarcodeType("Code 39", CODETYPE_CODE39),
    new BarcodeType("Code 39 Extended", CODETYPE_CODE39E),
    new BarcodeType("Code 93", CODETYPE_CODE93),
    new BarcodeType("Code 93 Extended", CODETYPE_CODE93E),
    new BarcodeType("GTIN-13 (EAN-13)", CODETYPE_EAN13),
    new BarcodeType("GTIN-14 (EAN-14)", CODETYPE_EAN14),
    new BarcodeType("GTIN-8 (EAN-8)", CODETYPE_EAN8),
    new BarcodeType("GS1-128 (UCC/EAN-128)", CODETYPE_EAN128),
    new BarcodeType("ISBN-13", CODETYPE_ISBN13),
    new BarcodeType("ISMN", CODETYPE_ISMN),
    new BarcodeType("PZN", CODETYPE_PZN),
    new BarcodeType("PZN8", CODETYPE_PZN8),
    new BarcodeType("SSCC-18 (NVE/EAN-18)", CODETYPE_SSCC18),
    new BarcodeType("UPC-A", CODETYPE_UPCA)
  };


  //----
  private BarcodeFactory() {
  }


  //----
  public static Barcode createBarcode(int barcodeType) throws IllegalArgumentException {
    Barcode bc;
    switch(barcodeType) {
      case CODETYPE_2OF5:      bc = new Barcode2Of5();      break;
      case CODETYPE_CODE39:    bc = new BarcodeCode39();    break;
      case CODETYPE_EAN13:     bc = new BarcodeEAN13();     break;
      case CODETYPE_EAN8:      bc = new BarcodeEAN8();      break;
      case CODETYPE_UPCA:      bc = new BarcodeUPCA();      break;
      case CODETYPE_CODE128:   bc = new BarcodeCode128();   break;
      case CODETYPE_CODABAR:   bc = new BarcodeCodabar();   break;
      case CODETYPE_CODE128A:  bc = new BarcodeCode128A();  break;
      case CODETYPE_CODE128B:  bc = new BarcodeCode128B();  break;
      case CODETYPE_CODE128C:  bc = new BarcodeCode128C();  break;
      case CODETYPE_CODE93:    bc = new BarcodeCode93();    break;
      case CODETYPE_CODE93E:   bc = new BarcodeCode93E();   break;
      case CODETYPE_CODE11:    bc = new BarcodeCode11();    break;
      case CODETYPE_CODE39E:   bc = new BarcodeCode39E();   break;
      case CODETYPE_PZN:       bc = new BarcodePZN();       break;
      case CODETYPE_PZN8:      bc = new BarcodePZN8();      break;
      case CODETYPE_ISBN13:    bc = new BarcodeISBN13();    break;
      case CODETYPE_ISMN:      bc = new BarcodeISMN();      break;
      case CODETYPE_EAN128:    bc = new BarcodeEAN128();    break;
      case CODETYPE_EAN14:     bc = new BarcodeEAN14();     break;
      case CODETYPE_SSCC18:    bc = new BarcodeSSCC18();    break;
      default: throw new IllegalArgumentException("Wrong barcode type: " + barcodeType);
    }
    return bc;
  }


  //----
  public static Barcode createBarcode(String number, int barcodeType, boolean autoComplete,
      boolean addOptChecksum) throws IllegalArgumentException {
    Barcode bc = createBarcode(barcodeType);
    bc.setNumber(number, autoComplete, addOptChecksum);
    return bc;
  }


  //----
  public static int getType(int index) {
    return BARCODES[index].bcType;
  }


  //----
  public static int getIndex(int type) {
    for (int i=BARCODES.length-1; i>=0; i--)
      if (BARCODES[i].bcType == type)
        return i;
    return -1;
  }


  //----
  public static String getBarcodeName(int type) {
    return BARCODES[getIndex(type)].bcName;
  }


  //----
  public static String getBarcodeShortName(int type) {
    String s = BARCODES[getIndex(type)].bcName;
    int idx = s.indexOf('(');
    return idx > 0 ? s.substring(0, idx - 1) : s;
  }


  //----
  public static String[] getBarcodeNames() {
    String[] bcNames = new String[BARCODES.length];
    for (int i=BARCODES.length-1; i>=0; i--)
      bcNames[i] = BARCODES[i].bcName;
    return bcNames;
  }


  //----
  private static class BarcodeType {
    String bcName;
    int bcType;
    BarcodeType(String bcName, int bcType) {
      this.bcName = bcName;
      this.bcType = bcType;
    }
  }

}
