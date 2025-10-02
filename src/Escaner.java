import java.util.ArrayList;
import javax.swing.JTextArea;

public class Escaner {
    private final String[] PR = {"if", "print", "inputInt", "inputFloat", "inputString", "else"};
    private final String[] TIPO = {"int", "float", "String"};
    private final String FOR = "for"; //22 (2025-02-10)
    private final String INC = "++", DEC = "--"; //INC 23, DEC 24
    StringBuilder Scanned = new StringBuilder();
    boolean Error = false;    
    String TokensString;

    @SuppressWarnings("rawtypes")
    ArrayList tokens = new ArrayList<Integer>();

    @SuppressWarnings({ "StringConcatenationInsideStringBufferAppend", "unchecked" })
    public String GetTokens(JTextArea texto) {
        char[] chars = texto.getText().toCharArray();
        StringBuilder Token = new StringBuilder();
        int i = 0;

        while (i < chars.length) {
            char c = chars[i];
    
            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }
    
            if (Character.isLetter(c)) {
                Token.setLength(0);
                while (i < chars.length && (Character.isLetter(chars[i]) || Character.isDigit(chars[i]))) {
                    Token.append(chars[i]);
                    i++;
                }
                String palabra = Token.toString();
    
                boolean esReservada = false;
                for (int k=0; k< PR.length; k++) {
                    if (palabra.equals(PR[k])) {
                        esReservada = true;
                        Scanned.append(palabra + "\n");
                        tokens.add(k);
                        break;
                    }
                }
                if (palabra.equals(FOR)){
                    esReservada = true;
                    Scanned.append(palabra + "\n");
                    tokens.add(22);
                }
                if (esReservada) continue;
    
                boolean esTipo = false;
                for (int k = 0; k < TIPO.length; k++) {
                    if (palabra.equals(TIPO[k])) {
                        esTipo = true;
                        Scanned.append(palabra + "\n");
                        tokens.add(6+k);
                        break;
                    }
                }
                if (esTipo) continue;
    
                Scanned.append(palabra + "\n");
                tokens.add(9);
                continue;
            }
    
            if (Character.isDigit(c)) {
                Token.setLength(0);
                boolean esFloat = false;
            
                while (i < chars.length && (Character.isDigit(chars[i]) || chars[i] == '.')) {
                    if (chars[i] == '.') {
                        if (esFloat) {
                            Scanned.append("ERROR: Número inválido (" + Token + ".)\n");
                            i++;
                            break;
                        }
                        esFloat = true;
                    }
                    Token.append(chars[i]);
                    i++;
                }
            
                if (Token.toString().endsWith(".")) {
                    Scanned.append("ERROR: Número inválido (" + Token + ")\n");
                } else {
                    if (esFloat) {
                        tokens.add(10);
                        Scanned.append(Token + "\n");
                    } else {
                        tokens.add(11);
                        Scanned.append(Token + "\n");
                    }
                }
                continue;
            }

            if (c == '=') {
                Token.setLength(0); 
                Token.append(c); 
                i++; 
                if (i < chars.length && chars[i] == '=') { 
                    Token.append('='); 
                    tokens.add(12); 
                    Scanned.append(Token + "\n");
                    i++; 
                } else {
                    tokens.add(13); 
                    Scanned.append("=\n");
                }
                continue;
            }
            
            if (c == '!' || c == '<' || c == '>') {
                Token.setLength(0); 
                Token.append(c); 
                i++;
                if (i < chars.length && chars[i] == '=') { 
                    Token.append('='); 
                    i++; 
                }
                tokens.add(12); 
                Scanned.append(Token + "\n");
                continue;
            }
            if (c == '+' && chars[i+1] == '+') {
                tokens.add(23); 
                i+=2;
                Scanned.append(INC + "\n");
            }
            if (c == '-' && chars[i+1] == '-') {
                tokens.add(24); 
                i+=2;
                Scanned.append(DEC + "\n");
            }            
            if (c == '+' || c == '-' || c == '*' || c == '/') {
                i++;
                tokens.add(14);
                Scanned.append(c + "\n");
                continue;
            }

            if (c == '$' && i + 1 < chars.length && chars[i + 1] == '$') {
                i += 2;
                tokens.add(15);
                Scanned.append("$$\n");
                continue;
            }
    
            if (c == '(') {
                i++;
                tokens.add(16);
                Scanned.append(c + "\n");
                continue;
            }
            if (c == ')') {
                i++;
                tokens.add(17);
                Scanned.append(c + "\n");
                continue;
            }
    
            if (c == '{') {
                i++;
                tokens.add(18);
                Scanned.append(c + "\n");
                continue;
            }
            if (c == '}') {
                i++;
                tokens.add(19);
                Scanned.append(c + "\n");
                continue;
            }
            if (c == ';') {
                i++;
                tokens.add(20);
                Scanned.append(";\n");
                continue;
            }
    
            if (c == '"') {
                Token.setLength(0);
                Token.append(c);
                i++;
                while (i < chars.length && chars[i] != '"') {
                    Token.append(chars[i]);
                    i++;
                }
                if (i < chars.length && chars[i] == '"') {
                    Token.append(chars[i]);
                    i++;
                    tokens.add(21);
                    Scanned.append("\"" + Token + "\"\n");
                }
                continue;
            }
    
            // Si no es ningún token anterior
            i++;
            Error = true;
            tokens.add(-1);
            Scanned.append("ERROR: Token inválido (" + c + ")\n");
        }
        return Scanned.toString();
    }
    
    public boolean WriteRun(JTextArea jtextA, JTextArea jtextB) {
        setTokensString(GetTokens(jtextA));
        jtextB.setText(this.TokensString);
        return !this.Error; // Devuelve true si no hay errores, false si hay errores
    }

    public String getTokensString() {
        return TokensString;
    }

    public void setTokensString(String tokensString) {
        TokensString = tokensString;
    }
}