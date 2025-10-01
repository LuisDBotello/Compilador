import java.util.HashMap;

public final class BinLine {

    public String Address = "";
    public StringBuilder Content = new StringBuilder();
    public String ASMline = "";
    public String Code = "";
    public static HashMap<String, String> Adds = new HashMap<>();
    public static int add = 0;
    public static boolean primerPasada;

    public BinLine(String ASMLine){
        this.ASMline = ASMLine;
        this.Address = toBytes(add, 16);
    }
    
    public void GenerarData(){
        //Transformar y limpiar la linea de ensamblador en un arreglo. Ej. NOMBRE DB 'LUIS', '$' = {NOMBRE, DB, LUIS, $}
        String[] tokens = this.ASMline.replace("\'", "").replace(",", " ").trim().split("\\s+|\n");
        //Iterador de Tokens
        int i = 0;
        //Guardar nombre de la variable
        String ID = tokens[i];
        //Ahora trataremos el tipo de dato
        i++;
        switch (tokens[i]) {
            //Dependiendo el tipo de dato se guardarán 8, 16 o 32 bits
            case "DB":      
                //Guardar en el Hashmap Adds la dirección de esta variable según su tamaño             
                if(primerPasada) Adds.put(ID, toBytes(add, 16));
                i++;   
                //Iterar hasta llegar al fin de la linea de ensamblador 
                while (i < tokens.length) { 
                    //Según el contenido del dato
                    if (isNum(tokens[i])) {
                        //Concatenar el contenido del dato de 8 bits a la línea de binario
                        Content.append(splitNibbles(toBytes(Integer.parseInt(tokens[i], 10),8))).append(" ");
                        //Aumentar el contador de direcciones y avanzar al siguiente token de la linea 
                        add++; i++;
                        continue;
                    }                        
                    //Si no es un número se trata de una cadena por lo que la dividimos en caracteres para guardar su código ASCII
                    char[] cadena = tokens[i].toCharArray();
                    for (char character : cadena) {
                        Content.append(splitNibbles(toBytes((int)character,8))).append(" ");
                        add++;
                    }
                    i++;                         
                }
                break; 
            case "DW": 
                //Se guarda la dirección con una longitud de 16 bits
                if (primerPasada) Adds.put(ID, toBytes(add, 16));
                i++;
                Content.append(splitNibbles(toBytes(Integer.parseInt(tokens[i], 10),16)));
                i++; add+=2;                    
                break;
            case "DD": i++;
            Content.append(Integer.toBinaryString(add)).append(" ");
            i++; add+=4; 
                break;
            default:
                break; 
        }
    }    
    void GenerarCode(){
        
        //Divide y limpia el código en tokens. Ej. MOV AX, BX = {MOV, AX, BX}
        String[] tokens = this.ASMline.trim().replaceAll("\\s+", " ").replace(",", "").split(" "); 
        //Clasificaremos las instrucciones por el total de tokens de la linea. Ej. ADD AX, 10 = 'triple; 'CALL LABEL1 = 'doble'; RET = 'unario'
        String Tipo = tokens.length == 3 ? "triple" : tokens.length == 2 ? "doble" : "unario";
        //Variable que va a formar la linea en binario
        String Codigo = "";        
        //Guardar la dirección de la instrucción
        this.setAddress(splitNibbles(toBytes(add, 16)));
        //Como los OpCodes son muchos, vamos a separarlos por triples, dobles y unarios y por modalidad(Registro -> Registro, Memoria -> Registro, etc)
        switch (Tipo) {                
            //Según la modalidad y el tamaño del registro escribiremos el codigo objeto de la linea
            case "triple":
                String Instruccion = tokens[0]; 
                String Destino = tokens[1]; 
                String Fuente = tokens[2]; 
                if (isRegister(Destino)){
                    if (isRegister(Fuente))  Codigo = getOpRR(Instruccion, Destino, Fuente);
                    if (isMemory(Fuente))    Codigo = getOpRM(Instruccion, Destino, Fuente); 
                    if (isImmediate(Fuente)) Codigo = getOpRI(Instruccion, Destino, Fuente); 
                }
                if (isMemory(Destino)){
                    if (isImmediate(Fuente)) Codigo = getOpMI(Instruccion, Destino, Fuente);
                    if (isRegister(Fuente))  Codigo = getOpMR(Instruccion, Destino, Fuente);
                }
                break;
            case "doble":
                String Inst = tokens[0]; 
                String Operando = tokens[1]; 
                if (isRegister(Operando))    Codigo = getOpDobleReg(Inst, Operando);
                if (isMemory(Operando))      Codigo = getOpDobleMem(Inst, Operando);
                if (isImmediate(Operando))   Codigo = getOpDobleImm(Inst, Operando);
                
                break;
            case "unario":
                switch (tokens[0]) {
                    case "RET" -> Codigo = "11000011";
                    case "NOP" -> Codigo = "10010000";
                    default -> {
                }
                }
            break;

        }
        //En el mapeo aquí se detecta si hay un salto relativo y suma 2 al contador de direcciones
        //De esta manera las direcciones de etiquetas se guardan correctamente
        if (Codigo == null || Codigo.isEmpty()) {
            add+=2;
            return;
        }        
        System.out.println(tokens[0] +" "+ add);
        //Según la longitud del codigo binario de la linea hasta ahorita se calculará cuantos bytes ocupó
        //Ej. Codigo = 01011010101011010101101001010110; length = 32; add sumará 4 (4 bytes)
        add += Codigo.length()/8;        
        //Para más comodidad de lectura se dividirá el byte en nibbles. Ej. 01011110 = 0101 1110
        Content.append(splitNibbles(Codigo));
    }
    //Todos los métodos GetOp... a partir de aquí siguen la misma lógica:
    //Generar la linea de Binario con el OpCode de la instrucción y los datos de los operandos.
    //Los operandos pueden ser Registros, Direcciones de Memoria o valores inmediatos 
    private String getOpDobleMem(String inst, String Operando){
        switch (inst) {
            //Ej. de instrucción: 
            //MUL RESULTADO ; DEC X ; CALL IMPRIMIR_NUMERO ; JMP LABEL1
            case "PUSH": return "1111111100110110" + Adds.get(Operando);
            case "POP" : return "1000111100000110" + Adds.get(Operando);
            case "IMUL": return Adds.get(Operando).length()==16 ?
                        "1111011100101110" + Adds.get(Operando):
                        "1111011000101110" + Adds.get(Operando);
            case "MUL" : return Adds.get(Operando).length()==16 ?
                        "1111011100101110" + Adds.get(Operando):
                        "1111011000101110" + Adds.get(Operando);
            case "INC" : return Adds.get(Operando).length()==16 ?
                        "1111111100000110" + Adds.get(Operando):
                        "1111111000000110" + Adds.get(Operando);
            case "DEC" : return Adds.get(Operando).length()==16 ?
                        "1111111100001110" + Adds.get(Operando):
                        "1111111000001110" + Adds.get(Operando);
            //Instrucciones con salto relativo
            case "CALL": return   "01110101" + RelJumpto(Operando);
            case "LOOP": return   "11100010" + RelJumpto(Operando);  
            case "JG"  : return   "01111111" + RelJumpto(Operando);
            case "JGE" : return   "01111101" + RelJumpto(Operando);
            case "JNE" : return   "01110101" + RelJumpto(Operando);
            case "JE"  : return   "01110100" + RelJumpto(Operando);
            case "JL"  : return   "01111100" + RelJumpto(Operando);
            case "JNZ" : return   "01110101" + RelJumpto(Operando);            
            case "JMP" : return   "11101011" + RelJumpto(Operando); 
        }
        return null;
    }
    private String getOpDobleReg(String inst, String operando){
        //Ej. de instrucción: POP AX ; INC DL
        switch (inst) {
            case "POP" : return OpCodePlusReg("01011000", Opcode(operando));
            case "PUSH": return OpCodePlusReg("01010000", Opcode(operando));
            case "IMUL": return is16bits(operando) ?
                        "1111011111101" + Opcode(operando):
                        "1111011011101" + Opcode(operando);
            case "IDIV": return is16bits(operando) ?
                        "1111011111111" + Opcode(operando):
                        "1111011011111" + Opcode(operando);
            case "DIV":  return is16bits(operando) ?
                        "1111011111111" + Opcode(operando):
                        "1111011011111" + Opcode(operando);
            case "INC":  return OpCodePlusReg("01000000", Opcode(operando));
            case "INT":  return OpCodePlusReg("01000000", Opcode(operando));
        }
        return null;
    }

    private String getOpDobleImm(String inst, String operando){
        //Ej. INT 21h ; PUSH 5
        switch (inst) {
            case "PUSH": return OpCodePlusReg("01010000", Opcode(operando));
            case "INT" : return "11001101" + String.format("%8s", Integer.
                        toBinaryString(toDecimal(operando))).replace(' ', '0');
        }
        return null;
    }

    private String getOpRR(String inst, String destino, String fuente) {
        //Ej. MOV AX, BX ; CMP CX, DX
        switch (inst) {
        case "MOV" -> {
            if (destino.equals("DS")) return "1000111011011" + Opcode(fuente);
            if (is16bits(fuente))  return "1000100111" + Opcode(destino) + Opcode(fuente);
            return "1000100011" + Opcode(destino) + Opcode(fuente);
            }
        case "ADD" -> {
            if (is16bits(fuente))  return "0000000111" + Opcode(destino) + Opcode(fuente);
            return "0000001011" + Opcode(destino) + Opcode(fuente);
            }
        case "CMP" -> {
            if (is16bits(fuente))  return "0011100111" + Opcode(destino) + Opcode(fuente);
            return "0011100011" + Opcode(destino) + Opcode(fuente);
            }
        case "TEST" -> { 
            if (is16bits(fuente))  return "1000010111" + Opcode(destino) + Opcode(fuente);
            return "1000010011" + Opcode(destino) + Opcode(fuente);
            }
       }
        return null;
    }
    private String getOpRI(String inst, String destino, String fuente) {
        //Ej: MOV AX, 250 ; MOV AH, 09h 
        int imm = toDecimal(fuente);
        switch (inst) {
            case "MOV":
                return is16bits(destino) ?  
                    "10101000" + LittleEndian(imm, 16):
                    OpCodePlusReg("10110000", Opcode(destino)) + LittleEndian(imm, 8);
            case "ADD":
                if (is16bits(destino))
                    return destino.equals("AX") ?
                        "00000101"      + LittleEndian(imm, 16) : 
                        "1000000111000" + Opcode(destino) + LittleEndian(imm, 8);
                return destino.equals("AL") ?
                        "00000100"      + LittleEndian(imm, 16) :
                        "1000000011000" + Opcode(destino) + LittleEndian(imm, 8);
            case "CMP":
                if (is16bits(destino))  
                    return destino.equals("AX") ? 
                        "01111101"      + LittleEndian(imm, 16) :
                        "1000000111111" + Opcode(destino) + LittleEndian(imm, 8);
                return destino.equals("AL") ? 
                        "00111100"      + LittleEndian(imm, 8) :
                        "1000000011111" + Opcode(destino) + LittleEndian(imm, 8);               
            case "TEST":
                if (is16bits(fuente))  
                    return destino.equals("AX") ?
                        "10101001"      + LittleEndian(imm, 16) :
                        "1111011111000" + Opcode(destino) + LittleEndian(imm, 16);
                return destino.equals("AL") ?
                        "10101000"      + LittleEndian(imm, 8) :
                        "1111011011000" + Opcode(destino) + LittleEndian(imm, 8);
           }    
        return null;
    }
    private String getOpMR(String inst, String destino, String fuente) {
        //Ej: ADD RESULTADO, AX 
        if (is16bits(fuente)) {
            switch (inst) {
                case "MOV": return fuente.equals("AX") ? 
                     "10100011"   + Adds.get(destino): 
                     "10001001"   + Adds.get(destino);
                case "SUB": return "0010100100" + Opcode(fuente) + "110" + Adds.get(destino);
                case "ADD": return "0000000100" + Opcode(fuente) + "110" + Adds.get(destino);
                case "CMP": return "0011100100" + Opcode(fuente) + "110" + Adds.get(destino);
                case "TEST":return "1000010100" + Opcode(fuente) + "110" + Adds.get(destino); 
           }
        } switch (inst) {
            case "MOV": return fuente.equals("AL") ? 
                 "10100000"   + Adds.get(destino) : 
                 "1000100000" + Opcode(fuente) + "110" + Adds.get(destino);
            case "ADD": return "0000000000" + Opcode(fuente) + "110" + Adds.get(destino);       
            case "CMP": return "0011100000" + Opcode(fuente) + "110" + Adds.get(destino);
            case "TEST":return "1000010000" + Opcode(fuente) + "110" + Adds.get(destino);
        }                    
        return null;
    }

    private String getOpRM(String inst, String reg, String var) {
        //Ej: ADD CL, LETRA ; LEA DL, NOMBRE
        switch (inst) {
            case "MOV":
                if (is16bits(reg)){
                    if (reg.equals("AX")) return var.equals("@data") ? 
                        "10111000XXXXXXXXXXXXXXXX" : 
                        "10100001"   + Adds.get(var);
                    return  "1000101100" + Opcode(reg) + "110" + Adds.get(var);
                }
                return reg.equals("AL") ?
                        "10100000"   + Adds.get(var) :
                        "1000101000" + Opcode(reg) + "110" + Adds.get(var);
            case "ADD":
                return is16bits(reg) ?
                    "0000001100" + Opcode(reg) + "110" + Adds.get(var):
                    "1000001000" + Opcode(reg) + "110" + Adds.get(var);
            case "CMP":
                return is16bits(reg) ?  
                    "0011101100" + Opcode(reg) + "110" + Adds.get(var):
                    "0011101000" + Opcode(reg) + "110" + Adds.get(var);
            case "LEA": return "1000110100" + Opcode(reg) + "110" + Adds.get(var);
        }
        return null;
    }
    
    private String getOpMI(String inst, String var, String fuente) {
        //Ej: CMP TOTAL, 70
        int imm = toDecimal(fuente);
        switch (inst) {
            case "MOV":
                return (Adds.get(var).length()==16) ?
                    "11000111" + Adds.get(var) + LittleEndian(imm, 16):
                    "11000110" + Adds.get(var) + LittleEndian(imm, 8);        
            case "ADD":
                return (Adds.get(var).length()==16) ?
                    "1000001100000110" + LittleEndian(imm, 16) : 
                    "1000000000000110" + LittleEndian(imm, 8);
            case "CMP":
                return (Adds.get(var).length()==16) ?
                    "1000000100111110" + Adds.get(var) + LittleEndian(imm, 16):
                    "1000000000111110" + Adds.get(var) + LittleEndian(imm, 8);               
            case "TEST":
                return (Adds.get(var).length()==16) ?
                    "1111011100000110" + Adds.get(var) + LittleEndian(imm, 16):
                    "1111011000000110" + Adds.get(var) + LittleEndian(imm, 8);
           }    
        return null;
    }
    //Métodos de utilería
    private boolean isMemory(String Tok){
        return Adds.containsKey(Tok) || Tok.equals("@data");
    }
    private boolean isImmediate(String Tok){
        String tok = Tok.toLowerCase();
        return tok.matches("\\d+") || tok.endsWith("h") || tok.endsWith("b");
    }
    private String splitNibbles(String binario) {
        while (binario.length() % 4 != 0) {
            binario = "0" + binario;  
        }
        StringBuilder nibbles = new StringBuilder();    
        for (int i = 0; i < binario.length(); i += 4) {
            nibbles.append(binario.substring(i, i + 4)).append(" ");
        }
        return nibbles.toString().trim();
    }
    private int HextoDecimal(String tok) {
        String hex = tok.substring(0, tok.length() - 1);    
        if (Character.isLetter(hex.charAt(0))) {
            hex = "0" + hex;
        }
        return Integer.parseInt(hex, 16);
    }
    private int toDecimal(String imm){
        return imm.endsWith("h")||imm.endsWith("H") ? HextoDecimal(imm) : Integer.parseInt(imm);
    }

    /*El cálculo del salto relativo es Dirección de destino - (Dirección actual + 2)
      El +2 son los bytes ocupados para la instrucción de salto relativo
      Es importante mencionar que también funciona para dar saltos hacia atrás en el código.
      Por ejemplo para regresarse 1 byte el salto sería 1111 1111, para regresarse 2 bytes sería 1111 1110
    */
    private String RelJumpto(String operando) {
        int origen = add;
        int destino = Integer.parseInt(Adds.get(operando), 2);
        int offset = destino - (origen + 2);         
        System.out.println("SALTO DE " + origen + " A " + destino + " Salto de " + offset);
        String binario = Integer.toBinaryString(offset & 0xFF);
    
        while (binario.length() < 8) {
            binario = "0" + binario;
        }
        return binario;
    }
    //Este método convierto los valores inmediatos de más de 8 bits a Little Endian, o sea, 
    //lo ordena con el byte menos significativo a la izquierda
    private String LittleEndian(int imm, int size) {
        String bin = toBytes(imm, 16); 
        String low = bin.substring(8, 16);  // bits 8 a 15 (últimos 8 bits)
        String high = bin.substring(0, 8);  // bits 0 a 7 (primeros 8 bits)
        return size == 16 ? low + high : low;
    }
    private boolean isRegister(String Tok){
        return Tok.matches("AX|BX|CX|DX|DS|AL|AH|BL|BH|CL|CH|DL|DH|SP|BP|SI|DI");
    }
    private boolean is16bits(String reg){
        return reg.matches("AX|BX|CX|DX|SI|DI|SP|BP");
    }
    private String Opcode(String reg){

        if (is16bits(reg)) {
            switch (reg) {
                case "AX": return "000";    
                case "CX": return "001";
                case "DX": return "010";
                case "BX": return "011";
                case "SP": return "100";
                case "BP": return "101";
                case "SI": return "110";
                case "DI": return "111";
            }
        }
        switch (reg) {
            case "AL": return "000";
            case "CL": return "001";
            case "DL": return "010";
            case "BL": return "011";
            case "AH": return "100";
            case "CH": return "101";
            case "DH": return "110";
            case "BH": return "111";
        }
        return "000";
    }
    //Este método transforma un numero decimal a un byte en binario acompletandolo con ceros a la izquierda
    String toBytes(int num, int totalLength) {
        String binary = Integer.toBinaryString(num);
    
        binary = String.format("%" + totalLength + "s", binary).replace(' ', '0');
        return binary;
    }
    //Algunos OpCode indican que se les sume el valor del registro al OpCode. Ej PUSH BX



    String OpCodePlusReg(String OpCode, String Reg) {
        if (OpCode == null || Reg == null) {
            System.err.println("Error: OpCode o Reg es null");
            return "00000000"; // o algún valor por defecto
        }
        int suma = Integer.parseInt(OpCode, 2) + Integer.parseInt(Reg, 2);
        String bin = Integer.toBinaryString(suma);
        while (bin.length() < 8) bin = "0" + bin;
        return bin;
    }
    //Instrucciones que no se traducen a binario como etiquetas o directivas como .DATA, .CODE, .MODEL, que son para el IDE
    boolean isOmitible(String ASMline){
        if (ASMline.contains("PROC")){ //Guardar dirección de procedimientos si está en mapeo
            if (primerPasada) 
                Adds.put(ASMline.replace("PROC", "").trim(), toBytes(add, 16));
            return true;
        }
        if (ASMline.trim().endsWith(":")) {
            if (primerPasada) 
                Adds.put(ASMline.replace(":", "").trim(), toBytes(add, 16));
            return true;
        }
        if (ASMline.equals(".CODE")) {
            //Para el caso específico de .CODE, el contador de direcciones tiene que reiniciarse
            add = 0;
            return true;
        }
        if (ASMline.startsWith(".")) return true;
        if (ASMline.endsWith("ENDP") || ASMline.equals(".MODEL")) return true;        
        if (ASMline.startsWith("END")) return true;
        if (ASMline.isBlank()) return true;

        return false;
    }
    boolean isNum(String tok){
        return tok.matches("\\d+");        
    }
    public void setAddress(String address) {
        Address = address;
    }
    public StringBuilder getContent() {
        return Content;
    }
    public void setContent(StringBuilder content) {
        Content = content;
    }
}
