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

import static de.vwsoft.barcodelib4j.oned.Barcode.calculateModulo10;
import static de.vwsoft.barcodelib4j.oned.Barcode.findNonDigitPosition;
import static de.vwsoft.barcodelib4j.oned.Barcode.validateModulo10;
import static de.vwsoft.barcodelib4j.oned.Barcode.validateNotEmpty;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Map;
import java.util.regex.Pattern;


/**
 * Validates GS1 structured data and generates its human-readable representation.
 * <p>
 * This class is typically used when preparing input for GS1-128, GS1 DataMatrix, and GS1 QR Code.
 * <p>
 * <b>Recommended Input Format (Simple Way)</b>
 * <p>
 * For composing GS1 data, use this input format:
 * <ol>
 *   <li>No leading FNC1 character</li>
 *   <li>Each Application Identifier (AI) enclosed in parentheses</li>
 *   <li>FNC1 character after each AI-data pair as separator</li>
 * </ol>
 * Example:
 * <pre>
 *   String content = "(01)01234567890128" + fnc1 +
 *                    "(15)251231" + fnc1 +
 *                    "(10)ABC123" + fnc1;
 *   GS1Validator validator = new GS1Validator(content, fnc1);
 * </pre>
 * Key Points:
 * <ol>
 *   <li>For GS1 DataMatrix and GS1 QR Code use {@code (char)29} as FNC1. For GS1-128 use the
 *     {@link de.vwsoft.barcodelib4j.oned.ImplCode128#FNC1} constant.</li>
 *   <li>Unnecessary FNC1 characters will be removed automatically by the validator to ensure an
 *     optimal and space-saving structure of the encoded data.</li>
 *   <li>Parentheses around AIs are never encoded in the barcode but are displayed in the
 *     human-readable line.</li>
 *   <li>Tip: Place one of your variable-length AIs at the end of your data sequence to save
 *     one FNC1 character.</li>
 * </ol>
 * <p>
 * <b>Alternative Input Formats</b>
 * <p>
 * The validator also accepts pre-composed data (e.g., from user input, databases etc.):
 * <ul>
 *   <li>With parentheses: {@code (01)01234567890128(15)251231(10)ABC123}</li>
 *   <li>Without parentheses: {@code 01012345678901281525123110ABC123}</li>
 *   <li>With or without leading FNC1</li>
 *   <li>Important: AIs with variable-length data must be terminated with FNC1 (unless at end)</li>
 * </ul>
 * <p>
 * <b>SSCC/GTIN Check Digits (AI 00, 01, 02)</b>
 * <p>
 * Use <b>#</b> as placeholder for automatic check digit calculation:
 * <pre>
 *   "(01)0123456789012#"  // Check digit will be calculated
 *   "(01)01234567890128"  // Check digit will be verified
 * </pre>
 * The placeholder character is stored in {@link #CHECKSUM_PLACEHOLDER} (static, non-final) and can
 * be customized if needed.
 */
public class GS1Validator {

  /**
   * Placeholder character that can be used in SSCC/GTIN numbers (AI 00, 01, 02) in place of the
   * check digit to force its automatic calculation. As a static non-final variable it can be
   * changed from the default ('#') if needed.
   */
  public static char CHECKSUM_PLACEHOLDER = '#';

  private static final int L_FNC1 = -1;
  private static final int L_BRACKET = -2;

  private static final AI[] APP_IDS = {
    new AI("00", 18, "_d{18}"),
    new AI("01", 14, "_d{14}"),
    new AI("02", 14, "_d{14}"),
    new AI("10", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,20}"),
    new AI("11", 6), // date YYMMDD
    new AI("12", 6), // date YYMMDD
    new AI("13", 6), // date YYMMDD
    new AI("15", 6), // date YYMMDD
    new AI("16", 6), // date YYMMDD
    new AI("17", 6), // date YYMMDD
    new AI("20", 2, "_d{2}"),
    new AI("21", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,20}"),
    new AI("22", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,20}"),
    new AI("235", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,28}"),
    new AI("240", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,30}"),
    new AI("241", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,30}"),
    new AI("242", L_BRACKET, "_d{0,6}"),
    new AI("243", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,20}"),
    new AI("250", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,30}"),
    new AI("251", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,30}"),
    new AI("253", L_FNC1, "_d{13})([_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,17}"),
    new AI("254", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,20}"),
    new AI("255", L_BRACKET, "_d{13})(_d{0,12}"),
    new AI("30", L_BRACKET, "_d{0,8}"),
    new AI("310", 5, 6, "_d{6}"),
    new AI("311", 5, 6, "_d{6}"),
    new AI("312", 5, 6, "_d{6}"),
    new AI("313", 5, 6, "_d{6}"),
    new AI("314", 5, 6, "_d{6}"),
    new AI("315", 5, 6, "_d{6}"),
    new AI("316", 5, 6, "_d{6}"),
    new AI("320", 5, 6, "_d{6}"),
    new AI("321", 5, 6, "_d{6}"),
    new AI("322", 5, 6, "_d{6}"),
    new AI("323", 5, 6, "_d{6}"),
    new AI("324", 5, 6, "_d{6}"),
    new AI("325", 5, 6, "_d{6}"),
    new AI("326", 5, 6, "_d{6}"),
    new AI("327", 5, 6, "_d{6}"),
    new AI("328", 5, 6, "_d{6}"),
    new AI("329", 5, 6, "_d{6}"),
    new AI("330", 5, 6, "_d{6}"),
    new AI("331", 5, 6, "_d{6}"),
    new AI("332", 5, 6, "_d{6}"),
    new AI("333", 5, 6, "_d{6}"),
    new AI("334", 5, 6, "_d{6}"),
    new AI("335", 5, 6, "_d{6}"),
    new AI("336", 5, 6, "_d{6}"),
    new AI("337", 5, 6, "_d{6}"),
    new AI("340", 5, 6, "_d{6}"),
    new AI("341", 5, 6, "_d{6}"),
    new AI("342", 5, 6, "_d{6}"),
    new AI("343", 5, 6, "_d{6}"),
    new AI("344", 5, 6, "_d{6}"),
    new AI("345", 5, 6, "_d{6}"),
    new AI("346", 5, 6, "_d{6}"),
    new AI("347", 5, 6, "_d{6}"),
    new AI("348", 5, 6, "_d{6}"),
    new AI("349", 5, 6, "_d{6}"),
    new AI("350", 5, 6, "_d{6}"),
    new AI("351", 5, 6, "_d{6}"),
    new AI("352", 5, 6, "_d{6}"),
    new AI("353", 5, 6, "_d{6}"),
    new AI("354", 5, 6, "_d{6}"),
    new AI("355", 5, 6, "_d{6}"),
    new AI("356", 5, 6, "_d{6}"),
    new AI("357", 5, 6, "_d{6}"),
    new AI("360", 5, 6, "_d{6}"),
    new AI("361", 5, 6, "_d{6}"),
    new AI("362", 5, 6, "_d{6}"),
    new AI("363", 5, 6, "_d{6}"),
    new AI("364", 5, 6, "_d{6}"),
    new AI("365", 5, 6, "_d{6}"),
    new AI("366", 5, 6, "_d{6}"),
    new AI("367", 5, 6, "_d{6}"),
    new AI("368", 5, 6, "_d{6}"),
    new AI("369", 5, 6, "_d{6}"),
    new AI("37", L_BRACKET, "_d{0,8}"),
    new AI("390", 9, L_BRACKET, "_d{0,15}"),
    new AI("391", 9, L_BRACKET, "_d{3})(_d{0,15}"),
    new AI("392", 9, L_BRACKET, "_d{0,15}"),
    new AI("393", 9, L_BRACKET, "_d{3})(_d{0,15}"),
    new AI("394", 3, 4, "_d{4}"),
    new AI("400", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,30}"),
    new AI("401", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,30}"),
    new AI("402", 17, "_d{17}"),
    new AI("403", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,30}"),
    new AI("410", 13, "_d{13}"),
    new AI("411", 13, "_d{13}"),
    new AI("412", 13, "_d{13}"),
    new AI("413", 13, "_d{13}"),
    new AI("414", 13, "_d{13}"),
    new AI("415", 13, "_d{13}"),
    new AI("416", 13, "_d{13}"),
    new AI("417", 13, "_d{13}"),
    new AI("420", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,20}"),
    new AI("421", L_FNC1, "_d{3})([_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,9}"),
    new AI("422", 3, "_d{3}"),
    new AI("423", L_BRACKET, "_d{3})(_d{0,12}"),
    new AI("424", 3, "_d{3}"),
    new AI("425", L_BRACKET, "_d{3})(_d{0,12}"),
    new AI("426", 3, "_d{3}"),
    new AI("427", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,3}"),
    new AI("7001", 13, "_d{13}"),
    new AI("7002", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,30}"),
    new AI("7003", 10), // Expiration date and time (YYMMDDHHMM)
    new AI("7004", L_BRACKET, "_d{0,4}"),
    new AI("7005", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,12}"),
    new AI("7006", 6), // First freeze date (YYMMDD)
    new AI("7007", L_BRACKET, "_d{6,12}"),
    new AI("7008", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,3}"),
    new AI("7009", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,10}"),
    new AI("7010", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,2}"),
    new AI("7020", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,20}"),
    new AI("7021", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,20}"),
    new AI("7022", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,20}"),
    new AI("7023", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,30}"),
    new AI("703", 9, L_FNC1, "_d{3})([_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,27}"),
    new AI("7040", 4, "_d[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{3}"),
    new AI("710", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,20}"),
    new AI("711", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,20}"),
    new AI("712", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,20}"),
    new AI("713", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,20}"),
    new AI("714", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,20}"),
    new AI("723", 9, L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{2,30}"),
    new AI("7240", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,20}"),
    new AI("8001", 14, "_d{14}"),
    new AI("8002", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,20}"),
    new AI("8003", L_FNC1, "_d{14})([_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,16}"),
    new AI("8004", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,30}"),
    new AI("8005", 6, "_d{6}"),
    new AI("8006", L_BRACKET, "_d{14})(_d{2})(_d{2}"),
    new AI("8007", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,34}"),
    new AI("8008", L_BRACKET), // Date and time of production (YYMMDDHHMMSS)
    new AI("8009", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,50}"),
    new AI("8010", L_BRACKET, "[_x23_x2D_x2F_x30-_x39_x41-_x5A]{0,30}"),
    new AI("8011", L_BRACKET, "_d{0,12}"),
    new AI("8012", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,20}"),
    new AI("8013", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,30}"),
    new AI("8017", 18, "_d{18}"),
    new AI("8018", 18, "_d{18}"),
    new AI("8019", L_BRACKET, "_d{0,10}"),
    new AI("8020", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,25}"),
    new AI("8026", L_BRACKET, "_d{14})(_d{2})(_d{2}"),
    new AI("8110", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,70}"),
    new AI("8111", 4, "_d{4}"),
    new AI("8112", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,70}"),
    new AI("8200", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,70}"),
    new AI("90", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,30}"),
    new AI("91", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,90}"),
    new AI("92", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,90}"),
    new AI("93", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,90}"),
    new AI("94", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,90}"),
    new AI("95", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,90}"),
    new AI("96", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,90}"),
    new AI("97", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,90}"),
    new AI("98", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,90}"),
    new AI("99", L_FNC1, "[_x21-_x22_x25-_x2F_x30-_x39_x41-_x5A_x5F_x61-_x7A]{0,90}")
  };



  private final String myContent; // The validated data for encoding in a GS1 barcode
  private final String myText;    // The validated data in human-readable format



  /**
   * Constructs a new instance and validates the provided GS1 data.
   *
   * @param content   the GS1 data to validate (see class documentation for format options)
   * @param fnc1Char  the character used as FNC1 separator within the provided GS1 data
   * @throws BarcodeException if the content is empty or invalid according to GS1 standards
   */
  public GS1Validator(String content, char fnc1Char) throws BarcodeException {
    final int len = content.length();
    StringBuilder sbContent = new StringBuilder(len);
    StringBuilder sbText = new StringBuilder(len);
    int idx = 0;

    while (true) {
      while (idx != len && content.charAt(idx) == fnc1Char) // Skip FNC1 characters
        idx++;
      if (idx == len)
        break;

      // Step 1: Extract the next Application Identifier (AI)
      AI ai = null;
      boolean embraced = content.charAt(idx) == '(';
      if (embraced)
        idx++; // Skip opening bracket
      for (AI nextAI : APP_IDS) {
        String s = nextAI.appId;
        if (s.regionMatches(0, content, idx, s.length())) {
          idx += s.length();
          if (nextAI.maxY > 0) {
            int n = idx < len ? content.charAt(idx) - 48 : -1;
            if (n < 0 || n > nextAI.maxY)
              throw new BarcodeException(BarcodeException.CONTENT_INVALID,
                  "'n' in AI %sn at position %s must be between 0 and %s",
                  "'n' in AI %sn an Position %s muss zwischen 0 und %s liegen",
                  s, idx, nextAI.maxY);
            s += n;
            idx++; // Skip one more character
          }
          if (embraced) {
            if (idx == len || content.charAt(idx) != ')')
              throw new BarcodeException(BarcodeException.CONTENT_INVALID,
                  "Closing bracket is missing at position %s",
                  "Schlie\u00DFende Klammer fehlt an Position %s", idx);
            idx++; // Skip closing bracket
          }
          sbContent.append(s);
          sbText.append('(').append(s).append(')');
          ai = nextAI;
          break;
        }
      }
      if (ai == null)
        throw new BarcodeException(BarcodeException.CONTENT_INVALID,
            "No valid AI found at position %s",
            "Kein g\u00FCltiger AI an Position %s gefunden", idx);

      // Step 2: Extract the data associated with the identified AI
      String data;
      int k;
      if (ai.delimiter > 0) { // AI expects a fixed length of data
        k = Math.min(idx + ai.delimiter, len); // Determine the maximum length to validate
      } else {
        k = content.indexOf(fnc1Char, idx);
        if (ai.delimiter == L_BRACKET) {
          int n = content.indexOf('(', idx);
          if (n > 0)
            k = k > 0 ? Math.min(k, n) : n;
        }
        if (k < 0)
          k = len;
      }
      data = content.substring(idx, k);

      // Step 3: Validate the extracted data
      if (ai.delimiter > 0 && ai.delimiter != data.length())
        throw new BarcodeException(BarcodeException.CONTENT_LENGTH_INVALID,
            "Value of AI %s must consist of %s characters; Provided: %s",
            "Wert von AI %s muss aus %s Zeichen bestehen; Aktuell: %s",
            ai.appId, ai.delimiter, data.length());
      if (ai == APP_IDS[0] || ai == APP_IDS[1] || ai == APP_IDS[2]) {
        if (data.charAt(ai.delimiter - 1) == CHECKSUM_PLACEHOLDER) {
          data = data.substring(0, ai.delimiter - 1);
          validateDigits(data, ai);
          data += calculateModulo10(data);
        } else {
          validateDigits(data, ai);
          validateModulo10(data);
        }
      } else if (!ai.matches(data)) {
        if (ai.isYYMMDD())
          throw new BarcodeException(BarcodeException.CONTENT_INVALID,
              "Value of AI %s is not a real date in YYMMDD format",
              "Wert von AI %s ist kein reales Datum im Format JJMMTT", ai.appId);
        else
          throw new BarcodeException(BarcodeException.CONTENT_INVALID,
              "Value of AI %s does not conform to its schema",
              "Wert von AI %s entspricht nicht dessen Schema", ai.appId);
      }

      sbContent.append(data);
      sbText.append(data);
      if (k == len)
        break;
      idx = k;
      if (ai.delimiter < 0)
        sbContent.append(fnc1Char);
    }

    // Remove trailing FNC1 characters
    for (int i=sbContent.length()-1; i>=0; i--) {
      if (sbContent.charAt(i) != fnc1Char) {
        sbContent.setLength(i + 1);
        break;
      }
    }

    validateNotEmpty(sbContent); // Check this first after all possible FNC1 chars have been removed

    myContent = sbContent.toString();
    myText = sbText.toString();
  }



  private static void validateDigits(String value, AI ai) throws BarcodeException {
    if (findNonDigitPosition(value) >= 0)
      throw new BarcodeException(BarcodeException.CONTENT_INVALID,
          "Value of AI %s must consist of exactly %s digits",
          "Wert von AI %s muss aus genau %s Ziffern bestehen", ai.appId, ai.delimiter);
  }



  /**
   * {@return the validated data for encoding in a GS1 barcode}
   */
  public String getContent() {
    return myContent;
  }



  /**
   * {@return the validated data in human-readable format}
   * <p>
   * All Application Identifiers are enclosed in parentheses for readability. No FNC1 is included.
   * <p>
   * Example: {@code (01)01234567890128(15)251231(10)ABC123}
   */
  public String getText() {
    return myText;
  }



  private static class AI {

    private static Map<Integer,DateTimeFormatter> dateTimeFormatters = Map.of(

      // AI 11, 12, 13, 15, 16, 17, 7006
      6,    new DateTimeFormatterBuilder().parseStrict().appendPattern("uuMMdd")     // YYMMDD
                .toFormatter().withResolverStyle(ResolverStyle.STRICT),

      // AI 7003
      10,   new DateTimeFormatterBuilder().parseStrict().appendPattern("uuMMddHHmm") // YYMMDDHHMM
                .toFormatter().withResolverStyle(ResolverStyle.STRICT),

      // AI 8008
      8008, new DateTimeFormatterBuilder().parseStrict()
                .appendPattern("uuMMddHH")               // Date (YYMMDD) and Hour (HH)
                .optionalStart()
                    .appendPattern("mm")                 // Minutes (MM), if present
                    .optionalStart()
                        .appendPattern("ss")             // Seconds (SS), if present
                    .optionalEnd()
                .optionalEnd()
                .toFormatter().withResolverStyle(ResolverStyle.STRICT)
    );

    String appId;
    int maxY;
    int delimiter;
    Pattern pattern;

    AI(String appId, int maxY, int delimiter, String regex) {
      this.appId = appId;
      this.maxY = maxY;
      this.delimiter = delimiter;

      StringBuilder sb = new StringBuilder(regex.length() + 2)
          .append('(')    .append(regex)    .append(')');
      for (int i=regex.length(); i!=0; i--)
        if (sb.charAt(i) == '_')
          sb.setCharAt(i, '\\');
      this.pattern = Pattern.compile(sb.toString());
    }

    AI(String appId, int delimiter, String regex) {
      this(appId, 0, delimiter, regex);
    }

    AI(String appId, int delimiter) { // Constructor for date-time-AIs
      this.appId = appId;
      this.delimiter = delimiter;
    }

    boolean isYYMMDD() {
      return pattern == null && delimiter == 6;
    }

    boolean matches(String data) {
      if (pattern != null)
        return pattern.matcher(data).matches();
      try {
        dateTimeFormatters.get(delimiter > 0 ? delimiter : Integer.parseInt(appId)).parse(data);
        return true;
      } catch (DateTimeParseException e) {
        return false;
      }
    }
  }

}
