package UI.controlador;

import javafx.fxml.FXML;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import jfx.incubator.scene.control.richtext.CodeArea;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.fxmisc.richtext.*;
import org.fxmisc.richtext.LineNumberFactory;


public class EditorController {

    @FXML
    private TreeView<String> treeArchivos;

    @FXML
    private VBox panelIzquierdo;
    private CodeArea codeArea;


    @FXML
    private VBox vboxCodigo; // contenedor donde estaba el TextArea

    private File carpetaBase = new File("C:/Users/luisd/Documents/Compilador"); // ruta inicial

    @FXML
    private void initialize() {
        codeArea = new CodeArea();
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea)); // agrega números de línea
        codeArea.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 14px;");

        // Agregar a VBox
        vboxCodigo.getChildren().add(codeArea);
        VBox.setVgrow(codeArea, Priority.ALWAYS);
    }

    // Carga el TreeView con archivos y carpetas
    private void cargarExplorador(File carpeta) {
        
        TreeItem<String> rootItem = crearNodo(carpeta);
        treeArchivos.setRoot(rootItem);
        treeArchivos.setShowRoot(true);
        rootItem.setExpanded(false);
    }

    // Crea nodos recursivos para el TreeView
    private TreeItem<String> crearNodo(File file) {
        TreeItem<String> item = new TreeItem<>(file.getName());
        item.setExpanded(true);
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                item.getChildren().add(crearNodo(child));
            }
        }
        return item;
    }

    // Permite hacer clic en un archivo y cargarlo en el TextArea
    private void configurarClickArchivo() {
        treeArchivos.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                String path = obtenerRutaCompleta(newSel);
                File f = new File(path);
                if (f.isFile()) {
                    try {
                        String contenido = new String(Files.readAllBytes(Paths.get(path)));
                        txtAreaCodigo.setText(contenido);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    // Construye la ruta completa del archivo seleccionado
    private String obtenerRutaCompleta(TreeItem<String> item) {
        StringBuilder sb = new StringBuilder(item.getValue());
        TreeItem<String> parent = item.getParent();
        while (parent != null && parent.getParent() != null) { // ignoramos root
            sb.insert(0, parent.getValue() + File.separator);
            parent = parent.getParent();
        }
        return carpetaBase.getAbsolutePath() + File.separator + sb.toString();
    }

    @FXML
    private void refrescarExplorador() {
        cargarExplorador(carpetaBase);
    }

    @FXML
    private void compilarCodigo() {
        System.out.println("Compilando código:\n" + txtAreaCodigo.getText());
        // Aquí puedes invocar tu compilador
    }

    @FXML
    private void guardarCodigo() {
        TreeItem<String> seleccionado = treeArchivos.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            String path = obtenerRutaCompleta(seleccionado);
            File f = new File(path);
            if (f.isFile()) {
                try {
                    Files.write(Paths.get(path), txtAreaCodigo.getText().getBytes());
                    System.out.println("Archivo guardado: " + path);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
