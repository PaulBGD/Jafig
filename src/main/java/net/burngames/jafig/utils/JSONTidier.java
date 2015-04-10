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