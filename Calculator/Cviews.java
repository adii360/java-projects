import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

public class Cviews extends JFrame{
    JLabel a,b,result;
    JTextField txta,txtb,txtResult;
    JButton add ,sub ,mul ,div;

    Cviews(){
        setTitle("Calculator");
        setSize(400, 400);
        setLayout(null);

        a=new JLabel("Enter A:");
a.setBounds(30, 50, 100, 30);
add(a);

txta=new JTextField();
txta.setBounds(150, 50, 150, 30);
add(txta);

b=new JLabel("Enter B:");
b.setBounds(30, 100, 100, 30);
add(b);

txtb=new JTextField();
txtb.setBounds(150, 100, 150, 30);
add(txtb);

add = new JButton("+");
add.setBounds(50, 150, 50, 30);
add(add);

sub = new JButton("-");
sub.setBounds(110, 150, 50, 30);
add(sub);

mul = new JButton("*");
mul.setBounds(170, 150, 50, 30);    
add(mul);

div = new JButton("/");
div.setBounds(230, 150, 50, 30);    
add(div);

result = new JLabel("Result:");
result.setBounds(50, 200, 100, 30);
add(result);

txtResult = new JTextField();
txtResult.setBounds(150, 200, 150, 30);

txtResult.setEditable(false);
add(txtResult);

setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
setVisible(true);
    }
}