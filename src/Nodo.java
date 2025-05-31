public class Nodo {
    String Caracter = "";
    Nodo Izq, Der;
    
    public Nodo(String caracter) {
        this.Caracter = caracter;
        this.Der = null;
        this.Izq = null;
    }

    public String getCaracter() {
        return Caracter;
    }
    public void setCaracter(String caracter) {
        Caracter = caracter;
    }
    public Nodo getIzq() {
        return Izq;
    }
    public void setIzq(Nodo izq) {
        Izq = izq;
    }
    public Nodo getDer() {
        return Der;
    }
    public void setDer(Nodo der) {
        Der = der;
    }
    

}
