public class Simbolo {
    
    private String Nombre = "";
    private String Tipo = "";
    private String Valor = "";
    private int Alcance = 0;
    
    public Simbolo(String nombre, String tipo, int alcance, String valor) {
        Nombre = nombre;
        Tipo = tipo;
        Alcance = alcance;
        Valor = valor;
    }

    public String toString(){
        return Nombre + " " + Tipo + " " + Alcance + " " + Valor;
    }
    public String getNombre() {
        return Nombre;
    }
    public void setNombre(String nombre) {
        Nombre = nombre;
    }
    public String getTipo() {
        return Tipo;
    }
    public void setTipo(String tipo) {
        Tipo = tipo;
    }
    public int getAlcance() {
        return Alcance;
    }
    public void setAlcance(int alcance) {
        Alcance = alcance;
    }
    public String getValor() {
        return Valor;
    }
    public void setValor(String valor) {
        Valor = valor;
    }
}