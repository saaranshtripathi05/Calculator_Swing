import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class SwingCalculator extends JFrame implements ActionListener {
    private final JTextField display;
    private double firstOperand = 0;
    private String operator = "";
    private boolean startNewNumber = true; 

    public SwingCalculator() {
        setTitle("Calculator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(320, 420);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(6, 6));

        display = new JTextField("0");
        display.setEditable(false);
        display.setHorizontalAlignment(SwingConstants.RIGHT);
        display.setFont(new Font("SansSerif", Font.BOLD, 28));
        display.setBackground(Color.WHITE);
        display.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(display, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(5, 4, 6, 6));
        String[] buttons = {
            "C", "←", "%", "/",
            "7", "8", "9", "*",
            "4", "5", "6", "-",
            "1", "2", "3", "+",
            "±", "0", ".", "="
        };

        for (String text : buttons) {
            JButton btn = new JButton(text);
            btn.setFont(new Font("SansSerif", Font.PLAIN, 20));
            btn.addActionListener(this);
            buttonPanel.add(btn);
        }

        add(buttonPanel, BorderLayout.CENTER);

        // keyboard support
        addKeyBindings(buttonPanel);

        setVisible(true);

        this.setFocusable(true);
        this.requestFocusInWindow();
    }

    private void addKeyBindings(JComponent comp) {
        InputMap im = comp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = comp.getActionMap();

        String keys = "0123456789.+-*/=\n\b\u007F"; 
        for (char k : keys.toCharArray()) {
            final String key = String.valueOf(k);
            im.put(KeyStroke.getKeyStroke(k), "key_" + key);
            am.put("key_" + key, new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    handleInput(key);
                }
            });
        }
        // enter
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "key_=");
        am.put("key_=", new AbstractAction() { public void actionPerformed(ActionEvent e) { handleInput("="); } });
        // escape -> clear
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "key_C");
        am.put("key_C", new AbstractAction() { public void actionPerformed(ActionEvent e) { handleInput("C"); } });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String cmd = ((JButton)e.getSource()).getText();
        handleInput(cmd);
    }

    private void handleInput(String cmd) {
        switch (cmd) {
            case "C":
                display.setText("0");
                firstOperand = 0;
                operator = "";
                startNewNumber = true;
                break;
            case "←":
                backspace();
                break;
            case "%":
                applyPercent();
                break;
            case "±":
                toggleSign();
                break;
            case "+": case "-": case "*": case "/":
                setOperator(cmd);
                break;
            case "=": case "\n":
                calculateResult();
                break;
            case ".":
                appendDecimalPoint();
                break;
            default:
                if (cmd.matches("[0-9]")) appendDigit(cmd);
                else if (cmd.equals("\b") || cmd.equals("\u007F")) backspace(); 
                break;
        }
    }

    private void appendDigit(String d) {
        if (startNewNumber) {
            display.setText(d);
            startNewNumber = false;
        } else {
            if (display.getText().equals("0")) display.setText(d);
            else display.setText(display.getText() + d);
        }
    }

    private void appendDecimalPoint() {
        if (startNewNumber) {
            display.setText("0.");
            startNewNumber = false;
        } else if (!display.getText().contains(".")) {
            display.setText(display.getText() + ".");
        }
    }

    private void backspace() {
        if (startNewNumber) return;
        String txt = display.getText();
        if (txt.length() <= 1) {
            display.setText("0");
            startNewNumber = true;
        } else {
            display.setText(txt.substring(0, txt.length() - 1));
        }
    }

    private void toggleSign() {
        String txt = display.getText();
        if (txt.equals("0")) return;
        if (txt.startsWith("-")) display.setText(txt.substring(1));
        else display.setText("-" + txt);
    }

    private void applyPercent() {
        try {
            double val = Double.parseDouble(display.getText());
            val = val / 100.0;
            display.setText(stripTrailingZeros(val));
            startNewNumber = true;
        } catch (NumberFormatException ex) {
            display.setText("Error");
            startNewNumber = true;
        }
    }

    private void setOperator(String op) {
        try {
            if (!operator.isEmpty() && !startNewNumber) {
                // chain calculations
                calculateResult();
            }
            firstOperand = Double.parseDouble(display.getText());
            operator = op;
            startNewNumber = true;
        } catch (NumberFormatException ex) {
            display.setText("Error");
            startNewNumber = true;
        }
    }

    private void calculateResult() {
        if (operator.isEmpty()) return;
        try {
            double second = Double.parseDouble(display.getText());
            double result = 0;
            switch (operator) {
                case "+": result = firstOperand + second; break;
                case "-": result = firstOperand - second; break;
                case "*": result = firstOperand * second; break;
                case "/":
                    if (second == 0) { display.setText("Error: Division by 0"); operator = ""; startNewNumber = true; return; }
                    result = firstOperand / second; break;
            }
            display.setText(stripTrailingZeros(result));
            operator = "";
            startNewNumber = true;
        } catch (NumberFormatException ex) {
            display.setText("Error");
            startNewNumber = true;
        }
    }

    private String stripTrailingZeros(double val) {
        if (Double.isInfinite(val) || Double.isNaN(val)) return "Error";
        String s = Double.toString(val);
        if (s.contains("E")) return s;
        if (s.indexOf('.') >= 0) {
            while (s.endsWith("0")) s = s.substring(0, s.length() - 1);
            if (s.endsWith(".")) s = s.substring(0, s.length() - 1);
        }
        return s;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SwingCalculator());
    }
}
