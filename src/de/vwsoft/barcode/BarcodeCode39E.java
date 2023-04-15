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


public class BarcodeCode39E extends BarcodeCode39 {
  private static final String[] CHARS_EXT = { "%U", "$A", "$B", "$C", "$D", "$E", "$F", "$G", "$H",
      "$I", "$J", "$K", "$L", "$M", "$N", "$O", "$P", "$Q", "$R", "$S", "$T", "$U", "$V", "$W",
      "$X", "$Y", "$Z", "%A", "%B", "%C", "%D", "%E", " ", "/A", "/B", "/C", "/D", "/E", "/F", "/G",
      "/H", "/I", "/J", "/K", "/L", "-", ".", "/O", "0", "1", "2", "3", "4", "5", "6", "7", "8",
      "9", "/Z", "%F", "%G", "%H", "%I", "%J", "%V", "A", "B", "C", "D", "E", "F", "G", "H", "I",
      "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "%K",
      "%L", "%M", "%N", "%O", "%W", "+A", "+B", "+C", "+D", "+E", "+F", "+G", "+H", "+I", "+J",
      "+K", "+L", "+M", "+N", "+O", "+P", "+Q", "+R", "+S", "+T", "+U", "+V", "+W", "+X", "+Y",
      "+Z", "%P", "%Q", "%R", "%S", "%T" };

  private String myNumber7Bit;


  //----
  public BarcodeCode39E(String number, boolean addOptChecksum) throws IllegalArgumentException {
    super(number, false, addOptChecksum);
  }


  //----
  public BarcodeCode39E() throws IllegalArgumentException {
    super("Code 39 Ext", false, false);
  }


  //----
  public boolean isCompletionSupported() {
    return false;
  }


  //----
  public void setNumber(String number, boolean autoComplete, boolean addOptChecksum)
      throws IllegalArgumentException {
    checkEmpty(number);
    checkAscii(number);

    final int len = number.length();
    StringBuilder sb = new StringBuilder(len << 1);
    for (int i=0; i<len; i++)
      sb.append(CHARS_EXT[number.charAt(i)]);

    myIsOptionalChecksumUsed = addOptChecksum;
    myNumber = sb.toString();
    myNumber7Bit = number;
    if (myIsOptionalChecksumUsed)
      initOptionalChecksum();
    initHumanReadableNumber();
    reset();
  }


  //----
  protected void initHumanReadableNumber() {
    myHumanReadableNumber = myNumber7Bit;
    if (myIsOptionalChecksumUsed && myIsOptionalChecksumVisible)
      myHumanReadableNumber += myOptionalChecksum;
  }

}
