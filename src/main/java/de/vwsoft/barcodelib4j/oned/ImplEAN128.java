/*
 * Copyright (c) 2026 Viktor Wedel
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
 * Implementation of GS1-128 (also known as EAN-128 or UCC-128).
 */
public class ImplEAN128 extends ImplCode128 {


  ImplEAN128() {
    super("(01)01234567890128", CODESET_ALL);
  }



  ImplEAN128(String content) {
    super(content, CODESET_ALL);
  }



  /**
   * Sets the GS1 data to be encoded in the barcode.
   * <p>
   * For guidance on how to properly provide GS1 structured data, refer to the {@link GS1Validator}
   * class documentation.
   * <p>
   * Note: This method performs validation automatically using an internal {@code GS1Validator}
   * instance. Therefore, there is no need to create your own {@code GS1Validator} instance or to
   * manually validate the provided content beforehand.
   *
   * @param content                the GS1 data to be encoded in the barcode
   * @param autoComplete           has no function in this method implementation
   * @param appendOptionalChecksum has no function in this method implementation
   * @throws BarcodeException      if the provided content is empty, does not comply with the GS1
   *                               standard, or exceeds 48 data characters (including Application
   *                               Identifiers and FNC1 separators)
   */
  @Override
  public void setContent(String content, boolean autoComplete, boolean appendOptionalChecksum)
      throws BarcodeException {

    GS1Validator gs1 = new GS1Validator(content, FNC1);
    content = gs1.getContent();

    if (content.length() > 48)
      throw new BarcodeException(BarcodeException.CONTENT_LENGTH_INVALID,
          "GS1-128 is limited to 48 data characters; Provided: %s",
          "GS1-128 ist auf 48 Datenzeichen begrenzt; Aktuell: %s", content.length());

    myContent = FNC1 + content;
    myText = gs1.getText();

    invalidateDrawing(); // Reset cached bars to force recalculation on the next drawing
  }

}
