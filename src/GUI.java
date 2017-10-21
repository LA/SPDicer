package scripts.SPDicer.src;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Created by Adar on 6/24/17.
 */
public class GUI extends JFrame {

    private JPanel contentPane;
    private JTextField minBet;
    private JTextField maxBet;
    private JTextField odds;
    private JTextField effects;
    private JTextField spamTimer;

    public GUI(final ScriptVars vars) {
        setTitle("SPDicer");
        setAlwaysOnTop(true);
        setResizable(false);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 400, 400);

        contentPane = new JPanel();
        contentPane.setBackground(SystemColor.inactiveCaptionText);
        contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        JLabel lblMinBet = new JLabel("Min Bet (Ex: 250k):");
        lblMinBet.setForeground(Color.WHITE);
        lblMinBet.setBounds(15, 85, 180, 14);
        contentPane.add(lblMinBet);

        minBet = new JTextField();
        minBet.setText("");
        minBet.setBounds(195, 85, 180, 20);
        contentPane.add(minBet);
        minBet.setColumns(10);

        JLabel lblMaxBet = new JLabel("Max Bet (Ex: 10m):");
        lblMaxBet.setForeground(Color.WHITE);
        lblMaxBet.setBounds(15, 125, 180, 14);
        contentPane.add(lblMaxBet);

        maxBet = new JTextField();
        maxBet.setText("");
        maxBet.setBounds(195, 125, 180, 20);
        contentPane.add(maxBet);
        maxBet.setColumns(10);

        JLabel lblOdds = new JLabel("Odds (Ex: 55)");
        lblOdds.setForeground(Color.WHITE);
        lblOdds.setBounds(15, 165, 180, 14);
        contentPane.add(lblOdds);

        odds = new JTextField();
        odds.setText("");
        odds.setBounds(195, 165, 180, 20);
        contentPane.add(odds);
        odds.setColumns(10);

        JLabel lblEffects = new JLabel("Spamming Text Effects");
        lblEffects.setForeground(Color.WHITE);
        lblEffects.setBounds(15, 205, 180, 14);
        contentPane.add(lblEffects);

        JLabel lblEffects2 = new JLabel("(Ex: flash2:wave2:)");
        lblEffects2.setForeground(Color.WHITE);
        lblEffects2.setBounds(15, 225, 180, 14);
        contentPane.add(lblEffects2);

        effects = new JTextField();
        effects.setText("");
        effects.setBounds(195, 205, 180, 20);
        contentPane.add(effects);
        effects.setColumns(10);

        JLabel lblSpam = new JLabel("Spam Timer MS (ex: 2500)");
        lblSpam.setForeground(Color.WHITE);
        lblSpam.setBounds(15, 245, 180, 14);
        contentPane.add(lblSpam);

        spamTimer = new JTextField();
        spamTimer.setText("");
        spamTimer.setBounds(195, 245, 180, 20);
        contentPane.add(spamTimer);
        spamTimer.setColumns(10);


        JButton startBtn = new JButton("Get Started");
        startBtn.addActionListener((ActionEvent e) -> {
            vars.started = true;
            vars.minBet = Integer.parseInt(minBet.getText().replace(",", "").replace("m", "000000").replace("k", "000"));
            vars.maxBet = Integer.parseInt(maxBet.getText().replace(",", "").replace("m", "000000").replace("k", "000"));
            vars.odds = Integer.parseInt(odds.getText());
            vars.spamTimerMS = Integer.parseInt(spamTimer.getText());
            vars.effects = effects.getText();
            System.out.println("Min: " + minBet + " Max: " + maxBet);
            dispose();
        });
        startBtn.setBounds(0, 325, 400, 45);
        contentPane.add(startBtn);
    }
}
