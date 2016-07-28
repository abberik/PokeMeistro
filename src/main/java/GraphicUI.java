import javax.swing.*;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;

/**
 * Created by root on 7/28/16.
 */
class GraphicUI extends JFrame {

    private JScrollPane pane;
    private JTextArea textArea;
    private JButton exitButton;

    public GraphicUI(String title,AbstractAction exitAction) {

        this.setTitle(title);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(640,480);
        this.setLocationRelativeTo(null);
        this.setLayout(new BorderLayout());
        this.setBackground(Color.black);

        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setForeground(Color.GREEN);
        textArea.setBackground(Color.BLACK);

        pane = new JScrollPane(textArea);
        pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        this.add(pane,BorderLayout.CENTER);

        exitButton = new JButton("exit");
        exitButton.addActionListener(exitAction);
        this.add(exitButton,BorderLayout.SOUTH);

        this.setVisible(true);

    }

    public void log(String text){
        textArea.append("["+ Instant.now().toString()+"]" +text + "\n");
    }

    public void setTitleText(String text){
        this.setTitle(text);
    }

    public void save(){

        try {

            BufferedWriter bw = new BufferedWriter(new FileWriter("log " + Instant.now().toString()));
            bw.write(textArea.getText());
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
