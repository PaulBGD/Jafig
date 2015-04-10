/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Burn Games LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package net.burngames.jafig.utils;

/**
 * Tidies up a JSON object,
 *
 * @author PaulBGD
 */
public class JSONTidier {

    private static final String tab = "    ";
    private static final String line = "\n";

    public static String tidyJSON(String json) {
        StringBuilder string = new StringBuilder();
        int tabCount = 0;
        boolean quotes = false;
        char[] charArray = json.toCharArray();
        for (int i = 0, charArrayLength = charArray.length; i < charArrayLength; i++) {
            char c = charArray[i];
            if (c == '"' && i != 0 && charArray[i - 1] != '\\') {
                quotes = !quotes;
            }
            if (quotes) {
                string.append(c);
                continue;
            }
            switch (c) {
                case '{':
                case '[':
                    string.append(c).append(line);
                    tabCount++;
                    for (int j = 0; j < tabCount; j++) {
                        string.append(tab);
                    }
                    break;
                case '}':
                case ']':
                    string.append(line);
                    tabCount--;
                    for (int j = 0; j < tabCount; j++) {
                        string.append(tab);
                    }
                    string.append(c);
                    break;
                case ',':
                    string.append(c);
                    if (i + 1 != charArrayLength && charArray[i + 1] != '{' && charArray[i + 1] != '[') {
                        string.append(line);
                        for (int j = 0; j < tabCount; j++) {
                            string.append(tab);
                        }
                    }
                    break;
                case ':':
                    string.append(c).append(" ");
                    break;
                default:
                    string.append(c);
                    break;
            }
        }
        string.append(line);
        return string.toString();
    }

}