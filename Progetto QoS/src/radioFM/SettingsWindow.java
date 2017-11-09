package radioFM;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

public class SettingsWindow extends JFrame{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7088089716177834916L;
	
	private JPanel mappa, mh, ma, pr;
	
	public SettingsWindow() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		createFrame();
	}
	
	private void createFrame() {
		//Mappa
		mappa = new JPanel();
		String[] param = new String[] {"Raggio", "Larghezza", "Altezza", "Numero nodi di traffico"};
		createTab(mappa, param);
		
		//Mobile Host
		mh = new JPanel();
		param = new String[] {"Velocità", "Numero massimo di MH", "Numero di MH generati in 1 secondo"};
		createTab(mh, param);
		
		JTabbedPane tabs = new JTabbedPane();
		tabs.addTab("Mappa", mappa);
		tabs.add("Mobile Host", mh);
		add(tabs,BorderLayout.NORTH);
		Dimension schermo = Toolkit.getDefaultToolkit().getScreenSize();
		int width = (int) schermo.getWidth();
		int height = (int) schermo.getHeight();
		setSize(700,700);
		this.setLocation(schermo.width/2-this.getSize().width/2, schermo.height/2-this.getSize().height/2);
		pack();
		setVisible(true);
	}
	
	private void createTab(JPanel p, String[] param) {
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		String current = "";
		JLabel l;
		JTextField t;
		for(int i = 0; i < param.length; i++) {
			current = param[i];
			l = new JLabel(current);
			t = new JTextField();
			l.setLabelFor(t);
			p.add(l);
			p.add(t);
		}
	}
	
	public static void main(String [] args) {
		new SettingsWindow();
	}
}
