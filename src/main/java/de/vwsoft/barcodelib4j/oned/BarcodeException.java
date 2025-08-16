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
import java.util.Locale;


/**
 * Thrown when a barcode object is assigned an invalid content that cannot be encoded by the given
 * barcode type.
 * <p>
 * Returns an exception ID from one of the three categories: Content, Checksum and Add-On.
 * Some IDs are for general use, while others are reserved for specific barcode types.
 * <p>
 * Content related exceptions:
 * <ul>
 * <li>{@link #CONTENT_EMPTY} - Indicates empty content.</li>
 * <li>{@link #CONTENT_INVALID} - States a general invalidity of the content.</li>
 * <li>{@link #CONTENT_NOT_DIGITS} - Indicates that the content contains non-digit characters.</li>
 * <li>{@link #CONTENT_NOT_ASCII} - Indicates that the content contains non-ASCII characters.</li>
 * <li>{@link #CONTENT_LENGTH_INVALID} - Indicates an invalid content length.</li>
 * <li>{@link #CONTENT_LENGTH_NOT_EVEN} - Indicates that the content length is not even.</li>
 * </ul>
 * Checksum related exceptions:
 * <ul>
 * <li>{@link #CHECKSUM_INVALID} - Indicates an invalid checksum.</li>
 * </ul>
 * Add-On related exceptions:
 * <ul>
 * <li>{@link #ADDON_EMPTY} - Indicates empty add-on content.</li>
 * <li>{@link #ADDON_LENGTH_INVALID} - Indicates an invalid add-on length.</li>
 * <li>{@link #ADDON_NOT_DIGITS} - Indicates that the add-on content contains non-digit
 *     characters.</li>
 * </ul>
 * <p>
 * If you prefer to determine which of the three categories an exception belongs to, rather than
 * handle each exception ID separately, you can use the three corresponding methods
 * {@link #isContentRelated()}, {@link #isChecksumRelated()} and {@link #isAddOnRelated()}
 * provided in this class.
 */
public class BarcodeException extends Exception {

  /** Exception ID indicating empty content. */
  public static final int CONTENT_EMPTY = 105;

  /** Exception ID indicating general invalidity of the content. */
  public static final int CONTENT_INVALID = 110;

  /** Exception ID indicating non-digit characters in the content. */
  public static final int CONTENT_NOT_DIGITS = 115;

  /** Exception ID indicating non-ASCII characters in the content. */
  public static final int CONTENT_NOT_ASCII = 120;

  /** Exception ID indicating an invalid content length. */
  public static final int CONTENT_LENGTH_INVALID = 125;

  /** Exception ID indicating an uneven content length. */
  public static final int CONTENT_LENGTH_NOT_EVEN = 130;


  /** Exception ID indicating an invalid checksum. */
  public static final int CHECKSUM_INVALID = 205;


  /** Exception ID indicating empty add-on content. */
  public static final int ADDON_EMPTY = 305;

  /** Exception ID indicating an invalid add-on length. */
  public static final int ADDON_LENGTH_INVALID = 310;

  /** Exception ID indicating non-digit characters in the add-on content. */
  public static final int ADDON_NOT_DIGITS = 315;



  /** The exception ID associated with this exception. */
  private final int myID;
  /** The localized message in German for this exception. */
  private final String myMessageDE;



  /**
   * Constructs a {@code BarcodeException} with the specified ID, message, and optional inserts.
   * <p>
   * The optional {@code inserts} can be used to dynamically insert values into the message text.
   * Use "%s" as a placeholder in the message where the insert should be placed. The order of the
   * inserts should correspond to the order of the placeholders in the message. For example, if the
   * message is "Invalid value at position %s: %s", you can provide the position and the invalid
   * value as inserts.
   *
   * @param id        the exception ID that categorizes the exception
   * @param message   the detailed message explaining the exception
   * @param messageDE the German translation of the message
   * @param inserts   optional parameters to be inserted into the message
   */
  public BarcodeException(int id, String message, String messageDE, Object... inserts) {
    super(fillInserts(message, inserts));
    myMessageDE = fillInserts(messageDE, inserts);
    myID = id;
  }



  private static String fillInserts(String message, Object[] inserts) {
    for (Object o : inserts)
      message = message.replaceFirst("%s", o.toString());
    return message;
  }



  /**
   * {@return the exception ID associated with this exception}
   */
  public int getID() {
    return myID;
  }



  /**
   * {@return the localized message for this exception} If the default locale is German, it returns
   * the German message. Otherwise, it returns the standard message.
   */
  @Override
  public String getLocalizedMessage() {
    return "de".equals(Locale.getDefault().getLanguage()) ? myMessageDE : getMessage();
  }



  /**
   * {@return whether the exception is content related}
   */
  public boolean isContentRelated() {
    return myID >= 100 && myID < 200;
  }



  /**
   * {@return whether the exception is checksum related}
   */
  public boolean isChecksumRelated() {
    return myID >= 200 && myID < 300;
  }



  /**
   * {@return whether the exception is add-on related}
   */
  public boolean isAddOnRelated() {
    return myID >= 300 && myID < 400;
  }

}
