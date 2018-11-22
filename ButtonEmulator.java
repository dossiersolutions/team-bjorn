import java.util.Hashtable;
import javax.swing.JFrame;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.event.*;

import java.io.*;
import java.net.*;


public class ButtonEmulator extends JFrame implements ChangeListener {

  JButton button = new JButton("The Button");
  JSlider potentiometer = new JSlider(JSlider.VERTICAL, 0, 1023, 0);
  JTextField buttonId = new JTextField(20);
  JTextField serverAddress = new JTextField(20);
  JLabel valueLabel = new JLabel("Ready...");

  int messageCounter = -1;

  public void stateChanged(ChangeEvent e) {
    int potentiometerValue = potentiometer.getValue();
    String buttonIdString = buttonId.getText();
    String serverAddresString = serverAddress.getText();
    boolean buttonIsPressed = button.getModel().isPressed();
    int quantizedPotentiometerValue = (potentiometerValue * 9) / 1024;

    String hostAndPort[] = serverAddresString.split(":");
    String host = hostAndPort[0];
    int port = Integer.parseInt(hostAndPort[1]);

    messageCounter++;
    
    valueLabel.setText(
        "msg: " + messageCounter + " pot: " + potentiometerValue + " " + (buttonIsPressed ? "Pressed." : "Not pressed."));

    try {
      Socket socket = new Socket(host, port);
      DataOutputStream output = new DataOutputStream(socket.getOutputStream());
      output.writeShort((short) Integer.parseInt(buttonIdString));
      output.writeShort((short) messageCounter);
      output.writeShort((short) (buttonIsPressed ? 1 : 0));
      output.writeShort((short) potentiometerValue);
      output.writeShort((short) quantizedPotentiometerValue);
      socket.close();
    } catch (Exception exception) {
      valueLabel.setText(valueLabel.getText() + " Unable to send.");
    }
  }

  public ButtonEmulator() {
    setTitle("Bjørn ButtonEmulator™");
    setSize(640, 480);
    setLocationRelativeTo(null);
    setDefaultCloseOperation(EXIT_ON_CLOSE);

    JPanel panel = new JPanel();
    getContentPane().add(panel);

    panel.setLayout(null);

    Hashtable labelTable = new Hashtable();
    labelTable.put(new Integer(0), new JLabel("0"));
    labelTable.put(new Integer(1023), new JLabel("1023"));
    potentiometer.setLabelTable(labelTable);
    potentiometer.setPaintLabels(true);

    buttonId.setText("1");
    serverAddress.setText("localhost:38911");

    potentiometer.setBounds(             0,   20,    50, 400);
    buttonId.setBounds(                100,   60,   200,  30);
    serverAddress.setBounds(           100,  120,   200,  30);
    button.setBounds(                  100,  180,   200,  60);
    valueLabel.setBounds(              100,  320,   600,  50);

    panel.add(potentiometer);
    panel.add(valueLabel);
    panel.add(buttonId);
    panel.add(serverAddress);
    panel.add(button);

    potentiometer.addChangeListener(this);
    button.addChangeListener(this);
  }

  public static void main(String[] args) {
    ButtonEmulator ex = new ButtonEmulator();
    ex.setVisible(true);
  }
}
