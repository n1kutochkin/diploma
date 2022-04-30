package org.drinkless.tdlib.example;

import javax.swing.*;

public class TgForm {

    private JTextArea textArea1;
    private JTextField textField1;
    private JRadioButton radioButton1;
    private JComboBox comboBox1;
    private JButton button1;
    private JList list1;
    private JPanel panel;

    public static void main(String[] args) {
        JFrame frame = new JFrame("TgForm");
        frame.setContentPane(new TgForm().panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
