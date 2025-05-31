import java.util.ArrayList;
import javax.swing.JTextArea;

public class Semantico {

    public static String regexNum = "^\\s*(int|float)\\s+[a-zA-Z_$][a-zA-Z0-9_$]*\\s*(=\\s*(-?\\d+(\\.\\d+)?)?)?\\s*$";
    public static String regexSt = "^\\s*String\\s+[a-zA-Z_$][a-zA-Z0-9_$]*\\s*(=\\s*\".*\"\\s*)?\\s*$";;
    Escaner Esc = null;
    private Parser Par;
    public boolean ErrorSemantico = false;
    public boolean VariableDuplicada = false;
    ArrayList<String> Instrucciones = new ArrayList<>(); 
    TablaDeSimbolos tds;
    
    public Semantico(Parser par, Escaner esc){
        this.Par = par;
        this.Esc = esc;
    }

    public ArrayList<String> GetDeclaraciones(Escaner Esc) {
        String[] Declaraciones = Esc.Scanned.toString().split("\n");
        String Codigo = "";
    
        for (int j = 0; j < Declaraciones.length; j++) {
            Codigo += Declaraciones[j] + " ";
        }
        Codigo = Codigo.replaceAll("\\s*\\$\\$\\s*", "").trim();

        Declaraciones = Codigo.split(";");
        
        ArrayList<String> Dec = new ArrayList<>();
        
        for (int i = 0; i < Declaraciones.length; i++) {
            if (Declaraciones[i].matches(regexNum) || Declaraciones[i].matches(regexSt)) {
                Declaraciones[i] += ";";
                Dec.add(Declaraciones[i]);
            }
        }
        return Dec;
    }

    public TablaDeSimbolos GetTablaDeSimbolos(ArrayList<String> declaraciones) {
    tds = new TablaDeSimbolos();
    
    for (String declaracion : declaraciones) {
        System.out.println("Procesando: " + declaracion); // 🔍 Verifica orden
        
        String[] partes = declaracion.trim().split("\\s+");
        if (partes.length < 2) continue; // Evita errores en líneas vacías
        
        String Tipo = partes[0];
        String Nombre = partes[1];
        String Alcance = "1";  
        String Valor = "0";  

        if (Tipo.equals("String")) Valor = "\'\'";
        if (Tipo.equals("float")) Valor = "0.0";

        if (partes.length > 3 && partes[2].equals("=")) {
            Valor = partes[3];
        }

        Simbolo S = new Simbolo(Nombre, Tipo, Integer.parseInt(Alcance), Valor.replaceAll("\"", ""));
        tds.InsertarSimbolo(S);
    }
    
    return tds;
}
public void MostrarTablaDeSimbolos() {
    if (tds == null || tds.getTabla().isEmpty()) {
        System.out.println("La tabla de símbolos está vacía.");
        return;
    }

    System.out.println("\n-- Tabla de Símbolos --");
    System.out.printf("%-15s %-10s %-10s %-10s\n", "Nombre", "Tipo", "Alcance", "Valor");
    System.out.println("--------------------------------------------");

    for (Simbolo s : tds.getTabla().values()) {
        System.out.printf("%-15s %-10s %-10d %-10s\n", 
            s.getNombre(), s.getTipo(), s.getAlcance(), s.getValor());
    }
}

    public void ValidarExpresiones(JTextArea JTA) {
        String[] Expresiones = JTA.getText().split("\n");
        String Codigo = "";
        for (int j = 0; j < Expresiones.length; j++) {
            Codigo += Expresiones[j] + " ";
        }

        String CodigoINST = Codigo;
        CodigoINST = CodigoINST.replaceAll("\\s*\\$\\$\\s*", "").trim().replace("\"\"", "\"");
        String[] EXP = CodigoINST.replace("{", "#{#").replace("}", "#}#").split(";|\n|#");
        for (String expr : EXP) {
            if (expr.matches(regexNum) || expr.matches(regexSt)) continue;
            if (expr.matches("\\s*")) continue;
            Instrucciones.add(expr.trim());
        }
        System.out.println(Instrucciones.toString());
        
    
        Codigo = Codigo.replaceAll("\\s*\\$\\$\\s*", "").trim().replace("\"\"", "\"");
        Codigo = Codigo.replaceAll("\\(\"[A-Za-z0-9]+\"\\)", "");
        
        String[] RemoveWords = {"if", "else", "print", "inputInt", "inputFloat", "inputString"};
        for (int i = 0; i < RemoveWords.length; i++) {
            Codigo = Codigo.replace(RemoveWords[i], "").trim();
        }        

        Expresiones = Codigo.split(";|\n|\\{|\\}");
        ArrayList<String> ExprArrL = new ArrayList<>();

        for (String expr : Expresiones) {
            if (expr.matches(regexNum) || expr.matches(regexSt)) continue;
            if (expr.matches("\\s*")) continue;
            ExprArrL.add(expr.trim());
        }
        
        String[] Expresion = ExprArrL.toArray(new String[0]);
        
        for (int i = 0; i < Expresion.length; i++) {
            Expresion[i] = GetTokExpresion(Expresion[i]);
        }

        TablaDeSimbolos tds = new TablaDeSimbolos();
        tds = GetTablaDeSimbolos(GetDeclaraciones(this.Esc));
        //tds.MostrarSimbolos(this);
        System.out.println("\n-EXPRESIONES MATEMATICAS-");
        for (String expr : Expresion) {
            System.out.println(expr);
        }

        // Si la variable no está en la tabla de simbolos se marcará error
        System.out.println("\n-ERRORES DE DECLARACIÓN-");
        for (int i = 0; i < Expresion.length; i++) {
            if (ErrorSemantico == true) break;
            String[] TokensExpr = Expresion[i].split(" ");
            for (String Tok : TokensExpr) {
                if (Tok.matches("^[a-zA-Z][a-zA-Z0-9]*$")) {
                    if (Tok.equals("String") || Tok.equals("float") || Tok.equals("int")) continue;
                    if (!tds.getTabla().containsKey(Tok)) {
                        System.out.println("La variable: (" + Tok + ") no está declarada"); 
                        ErrorSemantico = true;
                    }               
                }
            }
        }
        if (!ErrorSemantico) System.out.println("Sin errores de declaracion\n");
        if (!ErrorSemantico) System.out.println("-TYPE MISSMATCH?-");
        for (int i = 0; i < Expresion.length; i++) {
            if (ErrorSemantico) break;

            // Validar que no haya type missmatch
            ArrayList<String> ExprType = new ArrayList<>();
            String[] tok = Expresion[i].split(" ");
            for (String Tok : tok) { 
                if (!Tok.matches("^[a-zA-Z][a-zA-Z0-9]*$")) continue;

                if (Tok.equals("String") || Tok.equals("float") || Tok.equals("int")) {
                    ExprType.add(Tok);
                    continue;
                }
                ExprType.add(tds.getTabla().get(Tok).getTipo());
            }                
            boolean TypeMissmatch = false;
            String PrimerElemento = ExprType.get(0);
            for (String Elemento : ExprType) {
                if (!PrimerElemento.equals(Elemento)) {
                    TypeMissmatch = true;
                    ErrorSemantico = true;
                } 
            }
            if (TypeMissmatch) {
                String Mensaje = "Type Mismatch en: " + Expresion[i];
                System.out.printf("%-60s%-60s\n", Mensaje, ExprType);
            } 
        }               
        if (ErrorSemantico == false) System.out.println("Sin errores semánticos");
    }

    public String GetTokExpresion(String Expresion) {
        char[] chars = Expresion.toCharArray();
        String Palabra = "";
        String[] ops = {"(", ")", "=", "+", "-", "*", "/"};
        int i = 0;
        while (i < chars.length) {
            char c = chars[i];
            if (Character.isWhitespace(c)) { i++; continue; }

            for (int j = 0; j < ops.length; j++) {
                if (Character.toString(c).equals(ops[j])) {
                    i++;
                    Palabra += ops[j] + " ";
                }
            }
        
            if (Character.isLetter(c)) {
                while (i < chars.length && (Character.isLetter(chars[i]) || Character.isDigit(chars[i]))) {
                    Palabra += chars[i];
                    i++;
                } // Retorna el nombre de una variable
                Palabra += " ";
            }
            if (Character.isDigit(c)) {
                boolean esFloat = false;
            
                while (i < chars.length && (Character.isDigit(chars[i]) || chars[i] == '.')) {
                    if (chars[i] == '.') {
                        if (esFloat) { // Si ya había un punto, es un error
                            i++;
                            break;
                        }
                        esFloat = true;
                    }
                    i++;
                }
                if (esFloat) {
                    Palabra += "float ";
                } else {
                    Palabra += "int ";
                }
            }
            if (c == '=' || c == '!' || c == '<' || c == '>') { 
                Palabra += chars[i] + " ";
                i++;
                if (i < chars.length && chars[i] == '=') {
                    Palabra += chars[i] + " ";
                    i++;
                }
            }
        }
        return Palabra;
    }

    public boolean ValidarSemantico(JTextArea JTA) {
        if (this.Par.isParserError() == true) return false; // Si hay errores en el Parser, no se valida el Semántico
        ValidarExpresiones(JTA);
        return !this.ErrorSemantico; // Devuelve true si no hay errores, false si hay errores
    }

    public boolean isErrorSemantico() {
        return ErrorSemantico;
    }

    public void setErrorSemantico(boolean errorSemantico) {
        ErrorSemantico = errorSemantico;
    }  

    public boolean isVariableDuplicada() {
        return VariableDuplicada;
    }

    public void setVariableDuplicada(boolean variableDuplicada) {
        VariableDuplicada = variableDuplicada;
    } 
}