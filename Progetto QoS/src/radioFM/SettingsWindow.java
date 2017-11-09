package radioFM;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

public class SettingsWindow extends JFrame implements ActionListener{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7088089716177834916L;
	
	public double radius, speedMax, speedMin, maxTimeHandoff, minTimeHandoff, routeUpdateTime, pagingUpdateTime;
	public int numNodi, numMH, rate, avgRate, pSize,capacitaGMA, capacitaMA, pPaging, width, height;
	private JTextField raggio, larghezza, altezza, nodiTraffico, v_max, v_min, pg, cg, ru, pu, m, ra, c, t_max,t_min, avg, ps;

	public SettingsWindow() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		createFrame();
	}
	
	private void createFrame() {
		
		int width = 400;
		//Mappa
		JPanel mappa = new JPanel(new SpringLayout());
		
		JLabel r = new JLabel("Raggio", JLabel.TRAILING);
		raggio = new JTextField();
		raggio.setMaximumSize(new Dimension(width, 20));
		r.setLabelFor(raggio);
		mappa.add(r);
		mappa.add(raggio);
		
		JLabel l = new JLabel("Larghezza", JLabel.TRAILING);
		larghezza = new JTextField();
		larghezza.setMaximumSize(new Dimension(width, 20));
		l.setLabelFor(larghezza);
		mappa.add(l);
		mappa.add(larghezza);
		
		JLabel a = new JLabel("Altezza", JLabel.TRAILING);
		altezza = new JTextField();
		altezza.setMaximumSize(new Dimension(width, 20));
		a.setLabelFor(altezza);
		mappa.add(a);
		mappa.add(altezza);
		
		JLabel n = new JLabel("Numero nodi di traffico", JLabel.TRAILING);
		nodiTraffico = new JTextField();
		nodiTraffico.setMaximumSize(new Dimension(width, 20));
		n.setLabelFor(nodiTraffico);
		mappa.add(n);
		mappa.add(nodiTraffico);
		
		SpringUtilities.makeCompactGrid(mappa,
                4, 2, //rows, cols
                6, 6,        //initX, initY
                6, 6);
		
		//Mobile Host
		JPanel mh = new JPanel(new SpringLayout());
		
		JLabel velocita_max = new JLabel("Velocità massima", JLabel.TRAILING);
		v_max = new JTextField();
		v_max.setMaximumSize(new Dimension(width, 20));
		velocita_max.setLabelFor(v_max);
		mh.add(velocita_max);
		mh.add(v_max);
		
		JLabel velocita_min = new JLabel("Velocità minima", JLabel.TRAILING);
		v_min = new JTextField();
		v_min.setMaximumSize(new Dimension(width, 20));
		velocita_min.setLabelFor(v_min);
		mh.add(velocita_min);
		mh.add(v_min);
		
		JLabel numMh = new JLabel("Numero massimo di MH", JLabel.TRAILING);
		m = new JTextField();
		m.setMaximumSize(new Dimension(width, 20));
		numMh.setLabelFor(m);
		mh.add(numMh);
		mh.add(m);
		
		JLabel rate = new JLabel("Numero di MH generati in 1 secondo", JLabel.TRAILING);
		ra = new JTextField();
		ra.setMaximumSize(new Dimension(width, 20));
		rate.setLabelFor(ra);
		mh.add(rate);
		mh.add(ra);
		
		SpringUtilities.makeCompactGrid(mh,
                4, 2, //rows, cols
                6, 6,        //initX, initY
                6, 6);
		
		//Mobility Agent
		JPanel ma = new JPanel(new SpringLayout());
		
		JLabel capacitaG = new JLabel("Capacità GMA", JLabel.TRAILING);
		cg = new JTextField();
		cg.setMaximumSize(new Dimension(width, 20));
		capacitaG.setLabelFor(cg);
		ma.add(capacitaG);
		ma.add(cg);
		
		JLabel capacita = new JLabel("Capacità MA", JLabel.TRAILING);
		c = new JTextField();
		c.setMaximumSize(new Dimension(width, 20));
		capacita.setLabelFor(c);
		ma.add(capacita);
		ma.add(c);
		
		JLabel perPaging = new JLabel("Percentuali capacità di paging", JLabel.TRAILING);
		pg = new JTextField();
		pg.setMaximumSize(new Dimension(width, 20));
		perPaging.setLabelFor(pg);
		ma.add(perPaging);
		ma.add(pg);
		
		SpringUtilities.makeCompactGrid(ma,
                3, 2, //rows, cols
                6, 6,        //initX, initY
                6, 6);
		
		//Protocollo
		JPanel pr = new JPanel(new SpringLayout());
		
		JLabel timeHandoff_max = new JLabel("Latenza Handoff massima", JLabel.TRAILING);
		t_max = new JTextField();
		t_max.setMaximumSize(new Dimension(width, 20));
		timeHandoff_max.setLabelFor(t_max);
		pr.add(timeHandoff_max);
		pr.add(t_max);
		
		JLabel timeHandoff_min = new JLabel("Latenza Handoff minima", JLabel.TRAILING);
		t_min = new JTextField();
		t_min.setMaximumSize(new Dimension(width, 20));
		timeHandoff_min.setLabelFor(t_min);
		pr.add(timeHandoff_min);
		pr.add(t_min);
		
		JLabel avgRate = new JLabel("Velocità di trasmissione per ogni MH", JLabel.TRAILING);
		avg = new JTextField();
		avg.setMaximumSize(new Dimension(width, 20));
		avgRate.setLabelFor(avg);
		pr.add(avgRate);
		pr.add(avg);
		
		JLabel packet = new JLabel("Dimensione del pacchetto", JLabel.TRAILING);
		ps = new JTextField();
		ps.setMaximumSize(new Dimension(width, 20));
		packet.setLabelFor(ps);
		pr.add(packet);
		pr.add(ps);
		
		JLabel routeUpdate = new JLabel("Tempo di aggiornamento del percorso attivo", JLabel.TRAILING);
		ru = new JTextField();
		ru.setMaximumSize(new Dimension(width, 20));
		routeUpdate.setLabelFor(ru);
		pr.add(routeUpdate);
		pr.add(ru);
		
		JLabel pagingUpdate = new JLabel("Tempo di aggiornamento del percorso passivo", JLabel.TRAILING);
		pu = new JTextField();
		pu.setMaximumSize(new Dimension(width, 20));
		pagingUpdate.setLabelFor(pu);
		pr.add(pagingUpdate);
		pr.add(pu);
		
		SpringUtilities.makeCompactGrid(pr,
                6, 2, //rows, cols
                6, 6,        //initX, initY
                6, 6);
		
		//aggiunge le schede
		JTabbedPane tabs = new JTabbedPane();
		tabs.addTab("Mappa", mappa);
		tabs.addTab("Mobile Host", mh);
		tabs.addTab("Mobility Agent", ma);
		tabs.addTab("Protocollo", pr);
		add(tabs,BorderLayout.NORTH);
		
		JButton start = new JButton("Avvia");
		start.addActionListener(this);
		add(start, BorderLayout.SOUTH);
		
		setSize(width,350);
		setResizable(false);
		//centra la finestra
		Dimension schermo = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(schermo.width/2-this.getSize().width/2, schermo.height/2-this.getSize().height/2);
		setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		LinkedList<Integer> li = new LinkedList<>();
		LinkedList<Double> ld = new LinkedList<>();
		try {
			this.avgRate = Integer.parseInt(avg.getText());
			this.radius = Double.parseDouble(raggio.getText());
			this.width = Integer.parseInt(larghezza.getText());
			this.height = Integer.parseInt(altezza.getText());
			this.numNodi = Integer.parseInt(nodiTraffico.getText());
			this.numMH = Integer.parseInt(m.getText());
			this.speedMax = Double.parseDouble(v_max.getText());
			this.speedMin = Double.parseDouble(v_min.getText());
			this.rate = Integer.parseInt(ra.getText());
			this.capacitaGMA = Integer.parseInt(cg.getText());
			this.capacitaMA = Integer.parseInt(c.getText());
			this.pPaging = Integer.parseInt(pg.getText());
			this.maxTimeHandoff = Double.parseDouble(t_max.getText());
			this.minTimeHandoff = Double.parseDouble(t_min.getText());
			this.pSize = Integer.parseInt(ps.getText());
			this.routeUpdateTime = Double.parseDouble(ru.getText());
			this.pagingUpdateTime = Double.parseDouble(pu.getText());
		}catch(NumberFormatException nbe) {
			JOptionPane.showMessageDialog(this, "Ci sono dei campi vuoti!");
			return;
		}
		
		li.add(avgRate);
		li.add(width);
		li.add(height);
		li.add(numNodi);
		li.add(numMH);
		li.add(rate);
		li.add(capacitaGMA);
		li.add(capacitaMA);
		li.add(pPaging);
		li.add(pSize);
		
		ld.add(radius);
		ld.add(speedMax);
		ld.add(speedMin);
		ld.add(maxTimeHandoff);
		ld.add(minTimeHandoff);
		ld.add(routeUpdateTime);
		ld.add(pagingUpdateTime);
		
		for(int i : li) {
			if(i < 0) {
				JOptionPane.showMessageDialog(this, "Input errato, riprova!");
				return;
			}
		}
		for(double d : ld) {
			if(d < 0.0) {
				JOptionPane.showMessageDialog(this, "Input errato, riprova!");
				return;
			}
		}
		
		  /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Main_app();
            }
        });
        dispose();
	}
}
