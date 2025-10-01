import java.util.ArrayList;
import javax.swing.JTextArea;

public class Parser {

    private final int IF = 0, PRINT = 1, INPUTINT = 2, INPUTFLOAT = 3, INPUTSTRING = 4, ELSE = 5, TYPEINT = 6, 
        TYPEFLOAT = 7, TYPESTRING = 8, ID = 9, FLOAT = 10, NUM = 11, COMP = 12, ASIG = 13, OPER = 14, 
        LIM = 15, PAROPEN = 16, PARCLOSE = 17, LLAVEOPEN = 18, LLAVECLOSE = 19, EOL = 20, CADENA = 21;
    public String[] Words = {"if", "print", "inputInt", "inputFloat", "inputString", "else", "\"Typeint\"", 
    "\"Typefloat\"", "\"TypeString\"", "ID", "FLOAT", "NUM", "comparator", "=", "operator", "\"$$\"", "(", 
    ")", "{", "}", ";", "CADENA"};
    Escaner Escaneado = new Escaner();
    int i = 0; // Apuntador de token
    int Tok;
    private boolean ParserError = false;
    byte llavebloque = 0; // Indica cuántas llaves se han abierto
    ArrayList<String> Calculos = new ArrayList<>();

    public Parser(Escaner escaneado, JTextArea jta) {
        Escaneado = escaneado;
    }

    public boolean P() {
        System.out.println("ESCANER:");
        System.out.println("Tokens: " + Escaneado.tokens);
        System.out.println("\nPARSER:");
        this.Tok = (int) Escaneado.tokens.get(i);
        eat(LIM); 
        DECLARACION(); 
        ESTATUTO(); 
        return !this.ParserError; // Devuelve true si no hay errores, false si hay errores
    }    
       
    public void DECLARACION() {
        switch (this.Tok) {
            case TYPEINT: eat(TYPEINT); eat(ID); 
                switch (this.Tok) {
                    case ASIG: 
                        eat(ASIG); 
                        switch (this.Tok) {
                            case NUM: eat(NUM); eat(EOL); DECLARACION(); break;
                            case INPUTINT: eat(INPUTINT); eat(EOL); DECLARACION(); break;
                        } break;
                    case EOL: eat(EOL); DECLARACION(); break;
                } break;
            case TYPEFLOAT: eat(TYPEFLOAT); eat(ID); 
                switch (this.Tok) {
                    case ASIG: eat(ASIG); 
                        switch (this.Tok) {
                            case FLOAT: eat(FLOAT); eat(EOL); DECLARACION(); break;
                            case INPUTFLOAT: eat(INPUTFLOAT); eat(EOL); DECLARACION(); break;
                        } break;
                    case EOL: eat(EOL); DECLARACION(); break;
                } break;
            case TYPESTRING: eat(TYPESTRING); eat(ID); 
                switch (this.Tok) {
                    case ASIG: eat(ASIG); 
                        switch (this.Tok) {
                            case CADENA: eat(CADENA); eat(EOL); DECLARACION(); break;
                            case INPUTSTRING: eat(INPUTSTRING); eat(EOL); DECLARACION(); break;
                            default:
                                Error();
                                break;
                        } break;
                    case EOL: eat(EOL); DECLARACION(); break;
                } break;
            default: 
                ESTATUTO();
                break;
        }
    }

    public void ESTATUTO() {
        switch (this.Tok) {
            case ID:
                eat(ID); 
                eat(ASIG); 
                CALCULO(); 
                eat(EOL); 
                ESTATUTO(); 
                break;
            case IF:
                eat(IF); 
                eat(PAROPEN);
                switch (this.Tok) {
                    case ID:
                    case FLOAT:
                    case NUM:
                        eat(this.Tok); 
                        eat(COMP);
                        switch (this.Tok) {
                            case ID:
                            case FLOAT:
                            case NUM:
                                eat(this.Tok);  
                                eat(PARCLOSE);
                                BLOQUE();
                                handleElse();
                                ESTATUTO();
                                break;
                            case PAROPEN:
                                eat(PAROPEN); 
                                CALCULO(); 
                                System.out.println("Despues de calculo en IF");
                                eat(PARCLOSE);
                                BLOQUE();
                                handleElse();
                                ESTATUTO();
                                break;
                            default:
                                break;
                        } 
                        break;
                    case PAROPEN:
                        eat(PAROPEN);
                        CALCULO(); //1
                        eat(PARCLOSE);
                        eat(COMP);
                        switch (this.Tok) {
                            case ID:
                            case FLOAT:
                            case NUM:
                                eat(this.Tok);  
                                eat(PARCLOSE);
                                BLOQUE();
                                handleElse();
                                ESTATUTO();
                                break;
                            case PAROPEN:
                                eat(PAROPEN); 
                                CALCULO(); //200+a)
                                eat(PARCLOSE);
                                eat(PARCLOSE);
                                BLOQUE();
                                handleElse();
                                ESTATUTO();
                                break;
                            default:
                                break;
                        } 
                        break;
                    default:
                        break;
                }       
                break;
            case PRINT:
                eat(PRINT); 
                eat(PAROPEN);
                switch (this.Tok) {
                    case ID:
                        eat(ID); 
                        eat(PARCLOSE); 
                        eat(EOL); 
                        ESTATUTO();
                        break;
                    case CADENA:
                        eat(CADENA); 
                        eat(PARCLOSE); 
                        eat(EOL); 
                        ESTATUTO();
                        break;
                    case PAROPEN:
                        CALCULO(); 
                        eat(PARCLOSE); 
                        eat(EOL); 
                        ESTATUTO();
                        break;
                    default:
                        break;
                }  
                break;
            case LLAVECLOSE:
                if (llavebloque == 0) {
                    break;
                } else {
                    llavebloque--;
                    eat(LLAVECLOSE);
                    return;
                }
            default:
                eat(LIM);
                break;    
        }
    }
    
    private void handleElse() {
        if (this.Tok == ELSE) {
            eat(ELSE);
            BLOQUE();
        }
    }
    
    
    public void BLOQUE() {
        eat(LLAVEOPEN);
        llavebloque++;
        DECLARACION();
    }

    public void CALCULO() {
        System.out.println("Entrando a CALCULO con token: " + this.Tok + " " + Words[this.Tok]);
        switch (this.Tok) {
            case ID:
            case FLOAT:
            case NUM:
                eat(this.Tok); 
                break;
            case PAROPEN:
                eat(PAROPEN); 
                CALCULO(); 
                eat(PARCLOSE); 
                if (this.Tok == OPER) {
                    eat(OPER);
                    if (this.Tok == PAROPEN) {
                        System.out.println("Entrando a CALCULO despues de parentesis");
                        eat(PAROPEN); 
                        CALCULO(); 
                        eat(PARCLOSE); 
                    } else if (this.Tok == ID || this.Tok == FLOAT || this.Tok == NUM) {
                        eat(this.Tok);
                    }
                }
                break;
            default:
                break;
        }
        while (this.Tok == OPER) {
            eat(OPER);
            switch (this.Tok) {
                case ID:
                case FLOAT:
                case NUM:
                    eat(this.Tok); 
                    break;
                case PAROPEN:
                    eat(PAROPEN); 
                    CALCULO(); //2
                    System.out.println("Saliendo de x+4");
                    eat(PARCLOSE); 
                    break;
                default:
                    return;
            }
        }
    }

    public void eat(int tok) {
        if (this.ParserError) return;
    
        if (i >= Escaneado.tokens.size() || i < 0) {
            return;
        }
        if (this.Tok == tok) {
            System.out.println("Token reconocido: " + this.Tok + " " + Words[this.Tok]);
            Avanzar();
        } else {
            Error();
        }
    }

    private void Avanzar() {
        i++;    
        if (i < Escaneado.tokens.size()) {
            this.Tok = (int) Escaneado.tokens.get(i);
        } else {
            System.out.println("Sin errores de Parser");
        }
    }
    
    private void Error() {
        setParserError(true);        
        System.out.println("Token inesperado: " + this.Tok + " " + Words[this.Tok]);
    }

    public boolean isParserError() {
        return ParserError;
    }

    public void setParserError(boolean parserError) {
        this.ParserError = parserError;
    }

    public ArrayList<String> getCalculos() {
        return Calculos;
    }

    public void setCalculos(ArrayList<String> calculos) {
        Calculos = calculos;
    }

    public void setCalcTok(int Tok) {
        Escaneado.GetTokens(null);
    }
}