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
 * Implementation of Code 39 Extended ("Full ASCII").
 * <p>
 * Code 39 Extended is a variant of the {@link ImplCode39 Code 39} barcode format that supports
 * a broader range of characters, supporting all 128 ASCII characters.
 * <p>
 * <b>Barcode scanner configuration:</b> Please note that Code 39 Extended is not technically
 * different from standard Code 39, so a barcode scanner cannot automatically distinguish between
 * the two. To correctly read and interpret Code 39 Extended barcodes, the scanner must be
 * explicitly configured to interpret Code 39 as Extended.
 */
public class ImplCode39E extends ImplCode39 {

  private static final String[] CHARS_EXT = { "%U", "$A", "$B", "$C", "$D", "$E", "$F", "$G", "$H",
      "$I", "$J", "$K", "$L", "$M", "$N", "$O", "$P", "$Q", "$R", "$S", "$T", "$U", "$V", "$W",
      "$X", "$Y", "$Z", "%A", "%B", "%C", "%D", "%E", " ", "/A", "/B", "/C", "/D", "/E", "/F", "/G",
      "/H", "/I", "/J", "/K", "/L", "-", ".", "/O", "0", "1", "2", "3", "4", "5", "6", "7", "8",
      "9", "/Z", "%F", "%G", "%H", "%I", "%J", "%V", "A", "B", "C", "D", "E", "F", "G", "H", "I",
      "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "%K",
      "%L", "%M", "%N", "%O", "%W", "+A", "+B", "+C", "+D", "+E", "+F", "+G", "+H", "+I", "+J",
      "+K", "+L", "+M", "+N", "+O", "+P", "+Q", "+R", "+S", "+T", "+U", "+V", "+W", "+X", "+Y",
      "+Z", "%P", "%Q", "%R", "%S", "%T" };

  // Note: instance variables that exist only at runtime and store temporary data are marked as
  // 'transient' throughout the package; this may also be useful for a possible later serialization
  private transient String myContentASCII;



  ImplCode39E() {
    super("Code 39 Ext");
  }



  /** @hidden */
  @Override
  public boolean supportsAutoCompletion() {
    return false;
  }



  /**
   * Sets the content to be encoded in the barcode.
   *
   * @param content                the content to be encoded in the Code 39 Extended barcode
   * @param autoComplete           has no function in this method implementation
   * @param appendOptionalChecksum whether to append an optional checksum to the content
   * @throws BarcodeException      if the content is empty or contains non-ASCII characters
   */
  @Override
  public void setContent(String content, boolean autoComplete, boolean appendOptionalChecksum)
      throws BarcodeException {
    validateNotEmpty(content);
    validateASCII(content);

    final int len = content.length();
    StringBuilder sb = new StringBuilder(len << 1);
    for (int i=0; i<len; i++)
      sb.append(CHARS_EXT[content.charAt(i)]);

    myContent = sb.toString();
    myContentASCII = content;
    myOptionalChecksum = appendOptionalChecksum ? calculateOptionalChecksum(myContent) : null;

    updateHumanReadableText();
    invalidateDrawing(); // Reset cached bars to force recalculation on the next drawing
  }



  @Override
  void updateHumanReadableText() {
    myText = myContentASCII;
    if (myOptionalChecksum != null && myIsOptionalChecksumVisible)
      myText += myOptionalChecksum;
  }

}
