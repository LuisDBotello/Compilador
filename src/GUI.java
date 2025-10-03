import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.AdjustmentListener;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.*;

public class GUI extends JFrame {

    Escaner escaner;
    Parser parser;
    Semantico semantico;
    protected boolean sinErrores;

    public GUI() {
        super("Compiler");
        setPreferredSize(new Dimension(1440, 720));
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        initComponents();
    }

    private void initComponents() {
        jPanel1 = new JPanel();
        jLabel1 = new JLabel();
        jButton1 = new JButton();
        jButton2 = new JButton();
        jButton3 = new JButton();
        jButton4 = new JButton();
        jButton5 = new JButton();
        jButton6 = new JButton();
        jLabel3 = new JLabel();
        jScrollPane3 = new JScrollPane();
        jTextArea1 = new JTextArea();
        jScrollPane1 = new JScrollPane();
        jTextArea2 = new JTextArea();
        jScrollPaneDual = new JScrollPane();
        jPanelDual = new JPanel();
        jTextArea3 = new JTextArea();
        jTextArea4 = new JTextArea();
        jTextArea5 = new JTextArea();
        jTextArea6 = new JTextArea();
        jLabel2 = new JLabel();
        jLabel4 = new JLabel();
        jLabel5 = new JLabel();
        jLabel6 = new JLabel();
        jLabel7 = new JLabel();
        jLabel8 = new JLabel();
        jLabel9 = new JLabel();
        jPanelEscaner = new JPanel();
        jPanelParser = new JPanel();
        jPanelSemantico = new JPanel();

        jPanel1.setBackground(new Color(18, 18, 18));

        jLabel1.setFont(new java.awt.Font("Segoe UI Semilight", 0, 14));
        jLabel1.setForeground(new Color(204, 204, 204));
        jLabel1.setText("Archivo:");

        jButton1.setText("Browse");
        jButton1.addActionListener(evt -> {
            Archivo arch = new Archivo();
            arch.browseFile();
            arch.WriteFile(jTextArea1, arch.Codigo, jLabel1);
        });

        jButton2.setText("Run Escaner");
        jButton2.addActionListener(evt -> {
            Escaner Esc = new Escaner();
            boolean sinErrores = Esc.WriteRun(jTextArea1, jTextArea2);
            actualizarIndicador(jPanelEscaner, sinErrores);
            jLabel2.setForeground(sinErrores ? Color.GREEN : Color.RED);
            jLabel2.setText(sinErrores ? "Sin errores" : "Error en escaner");
            escaner = Esc;
        });

        jButton3.setText("Run Parser");
        jButton3.addActionListener(evt -> {
            Parser Par = new Parser(escaner, jTextArea1);
            boolean sinErrores = Par.P();
            actualizarIndicador(jPanelParser, sinErrores);
            jLabel2.setForeground(sinErrores ? Color.GREEN : Color.RED);
            jLabel2.setText(sinErrores ? "Sin errores" : "Error en el Parser");
            parser = Par;
        });

        jButton4.setText("Run Semántico");
        jButton4.addActionListener(evt -> {
            Semantico Sem = new Semantico(parser, escaner);
            sinErrores = Sem.ValidarSemantico(jTextArea1);
            Sem.MostrarTablaDeSimbolos();
            actualizarIndicador(jPanelSemantico, sinErrores);
            jLabel2.setForeground(sinErrores ? Color.GREEN : Color.RED);
            jLabel2.setText(sinErrores ? "Sin errores" : "Error en el Semántico");
            semantico = Sem;
        });

        jButton5.setText("Generar Ensamblador");
        jButton5.addActionListener(evt -> {
            Assembly AE = new Assembly(semantico);
            Assembly.CodigoASMEnsamblador.setLength(0);
            AE.Start();
            jTextArea3.setText(Assembly.CodigoASMEnsamblador.toString());
            jLabel2.setForeground(sinErrores ? Color.GREEN : Color.RED);
        });

        jButton6.setText("Generar Objeto");
        jButton6.addActionListener(evt -> {
            ObjectCode OC = new ObjectCode(jTextArea3);            

            OC.Start(true);
            OC.Start(false);
            jTextArea4.setText(OC.getAddresses().toString());
            jTextArea5.setText(OC.Content.toString());
            jTextArea6.setText(OC.Hex.toString());
            jLabel2.setForeground(sinErrores ? Color.GREEN : Color.RED);
        });

        //CONSTANTES
        int JPANELSIZE =12;
        String FONT = java.awt.Font.MONOSPACED;

        jLabel3.setFont(new java.awt.Font(FONT, 1, 20));
        jLabel3.setForeground(new Color(204, 204, 204));
        jLabel3.setText("Seleccione el archivo .txt con el código");

        jTextArea1.setBackground(new Color(204, 204, 204));
        jTextArea1.setColumns(18);
        jTextArea1.setRows(0);
        jTextArea1.setFont(new java.awt.Font(FONT, 1, JPANELSIZE));
        jScrollPane3.setViewportView(jTextArea1);

        jTextArea2.setBackground(new Color(204, 204, 204));
        jTextArea2.setColumns(5);
        jTextArea2.setRows(5);
        jTextArea2.setEditable(false);
        jTextArea2.setFont(new java.awt.Font(FONT, 1, JPANELSIZE));
        jScrollPane1.setViewportView(jTextArea2);

        jTextArea3.setBackground(new Color(204, 204, 204));
        jTextArea3.setFont(new java.awt.Font(FONT, 1, JPANELSIZE));
        jTextArea3.setColumns(5);
        jTextArea3.setRows(5);

        jTextArea4.setBackground(new Color(204, 204, 204));
        jTextArea4.setFont(new java.awt.Font(FONT, 1, JPANELSIZE));
        jTextArea4.setColumns(1);
        jTextArea4.setRows(0);

        jTextArea5.setBackground(new Color(204, 204, 204));
        jTextArea5.setFont(new java.awt.Font(FONT, 1, JPANELSIZE));
        jTextArea5.setColumns(12);
        jTextArea5.setRows(5);

        jTextArea6.setBackground(new Color(204, 204, 204));
        jTextArea6.setFont(new java.awt.Font(FONT, 1, JPANELSIZE));
        jTextArea6.setColumns(12); 
        jTextArea6.setRows(5);

        jPanelDual.setLayout(new BoxLayout(jPanelDual, BoxLayout.X_AXIS));
        jPanelDual.add(new JScrollPane(jTextArea3));
        jPanelDual.add(new JScrollPane(jTextArea4));
        jPanelDual.add(new JScrollPane(jTextArea5));
        jPanelDual.add(new JScrollPane(jTextArea6));

        jScrollPaneDual.setViewportView(jPanelDual);

        // Sincronizar scroll
        JScrollBar scrollBar1 = ((JScrollPane) jPanelDual.getComponent(0)).getVerticalScrollBar();
        JScrollBar scrollBar2 = ((JScrollPane) jPanelDual.getComponent(1)).getVerticalScrollBar();
        JScrollBar scrollBar3 = ((JScrollPane) jPanelDual.getComponent(2)).getVerticalScrollBar();
        JScrollBar scrollBar4 = ((JScrollPane) jPanelDual.getComponent(3)).getVerticalScrollBar();

        AtomicBoolean adjusting = new AtomicBoolean(false);

        AdjustmentListener syncScroll = e -> {
            if (adjusting.get()) return;

            adjusting.set(true);
            int value = ((JScrollBar) e.getSource()).getValue();
            scrollBar1.setValue(value);
            scrollBar2.setValue(value);
            scrollBar3.setValue(value);
            scrollBar4.setValue(value);
            adjusting.set(false);
            
            
        };

        scrollBar1.addAdjustmentListener(syncScroll);
        scrollBar2.addAdjustmentListener(syncScroll);
        scrollBar3.addAdjustmentListener(syncScroll);
        scrollBar4.addAdjustmentListener(syncScroll);

        scrollBar1.setVisible(true);
        jTextArea3.revalidate();
        jTextArea3.repaint();

        scrollBar2.setVisible(false);
        scrollBar2.setPreferredSize(new Dimension(0, 0));
        jTextArea4.revalidate();
        jTextArea4.repaint();

        scrollBar3.setVisible(false);
        scrollBar3.setPreferredSize(new Dimension(0, 0));
        jTextArea5.revalidate();
        jTextArea5.repaint();

        scrollBar4.setVisible(false);
        scrollBar4.setPreferredSize(new Dimension(0, 0));
        jTextArea6.revalidate();
        jTextArea6.repaint();

        jLabel2.setFont(new java.awt.Font(FONT, 1, 14));
        jLabel2.setForeground(new Color(204, 204, 204));

        jLabel4.setFont(new java.awt.Font(FONT, 1, 14));
        jLabel4.setForeground(new Color(204, 204, 204));
        jLabel4.setText("SRC");

        jLabel5.setFont(new java.awt.Font(FONT, 1, 14));
        jLabel5.setForeground(new Color(204, 204, 204));
        jLabel5.setText("Tokens");

        jLabel6.setFont(new java.awt.Font(FONT, 1, 14));
        jLabel6.setForeground(new Color(204, 204, 204));
        jLabel6.setText("ASM");

        jLabel7.setFont(new java.awt.Font(FONT, 1, 14));
        jLabel7.setForeground(new Color(204, 204, 204));
        jLabel7.setText("Address");

        jLabel8.setFont(new java.awt.Font(FONT, 1, 14));
        jLabel8.setForeground(new Color(204, 204, 204));
        jLabel8.setText("Content");

        jLabel9.setFont(new java.awt.Font(FONT, 1, 14));
        jLabel9.setForeground(new Color(204, 204, 204));
        jLabel9.setText("Hex");

        
        jPanelEscaner.setBackground(Color.RED);
        jPanelEscaner.setPreferredSize(new Dimension(40, 20));
        jPanelEscaner.setMinimumSize(new Dimension(40, 20));
        jPanelEscaner.setMaximumSize(new Dimension(40, 20));

        jPanelParser.setBackground(Color.RED);
        jPanelParser.setPreferredSize(new Dimension(40, 20));
        jPanelParser.setMinimumSize(new Dimension(40, 20));
        jPanelParser.setMaximumSize(new Dimension(40, 20));

        jPanelSemantico.setBackground(Color.RED);
        jPanelSemantico.setPreferredSize(new Dimension(40, 20));
        jPanelSemantico.setMinimumSize(new Dimension(40, 20));
        jPanelSemantico.setMaximumSize(new Dimension(40, 20));

        // Layout principal
        GroupLayout jPanel1Layout = new GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(20)
                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addGap(18)
                        .addComponent(jButton1)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel1))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4)
                            .addComponent(jScrollPane3, GroupLayout.PREFERRED_SIZE, 200, GroupLayout.PREFERRED_SIZE))
                        .addGap(2)
                        .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel5)
                            .addComponent(jScrollPane1, GroupLayout.PREFERRED_SIZE, 120, GroupLayout.PREFERRED_SIZE))
                        .addGap(2)
                        .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel6)
                                .addGap(230)
                                .addComponent(jLabel7)
                                .addGap(130)
                                .addComponent(jLabel8)
                                .addGap(327)
                                .addComponent(jLabel9))
                            .addComponent(jScrollPaneDual, GroupLayout.PREFERRED_SIZE, 1000, GroupLayout.PREFERRED_SIZE)))  // Ajuste aquí
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jButton2)
                        .addGap(5)
                        .addComponent(jPanelEscaner)
                        .addGap(5)
                        .addComponent(jButton3)
                        .addGap(5)
                        .addComponent(jPanelParser)
                        .addGap(5)
                        .addComponent(jButton4)
                        .addGap(5)
                        .addComponent(jPanelSemantico)
                        .addGap(5)
                        .addComponent(jButton5)
                        .addGap(5)
                        .addComponent(jLabel2)
                        .addGap(20)
                        .addComponent(jButton6)))
                .addContainerGap(40, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(20)
                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jButton1)
                    .addComponent(jLabel1))
                .addGap(18)
                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5)
                    .addComponent(jLabel6)
                    .addComponent(jLabel7)
                    .addComponent(jLabel8)
                    .addComponent(jLabel9))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, GroupLayout.DEFAULT_SIZE, 520, Short.MAX_VALUE)
                    .addComponent(jScrollPane1)
                    .addComponent(jScrollPaneDual))
                .addGap(18)
                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(jButton2)
                    .addComponent(jPanelEscaner)
                    .addComponent(jButton3)
                    .addComponent(jPanelParser)
                    .addComponent(jButton4)
                    .addComponent(jPanelSemantico)
                    .addComponent(jButton5)
                    .addComponent(jLabel2)
                    .addComponent(jButton6))
                .addContainerGap(40, Short.MAX_VALUE))
        );

        getContentPane().add(jPanel1);
        pack();
    }

    private void actualizarIndicador(JPanel panel, boolean sinErrores) {
        panel.setBackground(sinErrores ? Color.GREEN : Color.RED);
        panel.repaint();
    }

    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        java.awt.EventQueue.invokeLater(() -> new GUI().setVisible(true));
    }

 
