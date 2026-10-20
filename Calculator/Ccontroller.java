import java.awt.event.*;

public class Ccontroller implements ActionListener {

    Cviews view;
    Cmodels model;

    public Ccontroller(Cviews view) {
        this.view = view;
        model = new Cmodels();

        view.add.addActionListener(this);
        view.sub.addActionListener(this);
        view.mul.addActionListener(this);
        view.div.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        try{

        double a = Double.parseDouble(view.txta.getText());
        double b = Double.parseDouble(view.txtb.getText());
        double ans = 0;

        if (e.getSource() == view.add)
            ans = model.add(a, b);

        else if (e.getSource() == view.sub)
            ans = model.sub(a, b);

        else if (e.getSource() == view.mul)
            ans = model.mul(a, b);

        else if (e.getSource() == view.div)
            ans = model.div(a, b);
        view.txtResult.setText(String.valueOf(ans));

          } catch (NumberFormatException ex) {
        view.txtResult.setText("Invalid Input");
    }
}
    
}