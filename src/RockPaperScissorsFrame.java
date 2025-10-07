import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

public class RockPaperScissorsFrame extends JFrame {
    private final JButton rockBtn = new JButton();
    private final JButton paperBtn = new JButton();
    private final JButton scissorsBtn = new JButton();
    private final JButton quitBtn = new JButton("Quit");

    private final JTextField playerWinsField = new JTextField("0", 5);
    private final JTextField compWinsField   = new JTextField("0", 5);
    private final JTextField tiesField       = new JTextField("0", 5);

    private final JTextArea logArea = new JTextArea(12, 40);

    private int playerWins = 0;
    private int compWins = 0;
    private int ties = 0;

    private final Random rand = new Random();

    private final Strategy cheat = new Cheat();
    private final Strategy randomStrat = new RandomStrategy();

    private final Strategy leastUsedInner = new LeastUsed();
    private final Strategy mostUsedInner = new MostUsed();
    private final Strategy lastUsedInner = new LastUsed();

    private int playerRCount = 0;
    private int playerPCount = 0;
    private int playerSCount = 0;
    private String lastPlayerMove = null;

    public RockPaperScissorsFrame() {
        setTitle("Rock Paper Scissors Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8,8));

        JPanel top = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Rock Paper Scissors Game", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        top.add(title, BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(8,8));
        JPanel buttonsPanel = new JPanel(new GridLayout(1,4,10,10));
        buttonsPanel.setBorder(new TitledBorder("Play"));

        setIconButton(rockBtn, "R", "/rock.png");
        setIconButton(paperBtn, "P", "/paper.png");
        setIconButton(scissorsBtn, "S", "/scissors.png");

        quitBtn.addActionListener(e -> System.exit(0));

        buttonsPanel.add(rockBtn);
        buttonsPanel.add(paperBtn);
        buttonsPanel.add(scissorsBtn);
        buttonsPanel.add(quitBtn);

        center.add(buttonsPanel, BorderLayout.NORTH);

        JPanel statsPanel = new JPanel(new GridLayout(3,2,6,6));
        statsPanel.setBorder(new TitledBorder("Stats"));
        playerWinsField.setEditable(false);
        compWinsField.setEditable(false);
        tiesField.setEditable(false);
        statsPanel.add(new JLabel("Player Wins:"));
        statsPanel.add(playerWinsField);
        statsPanel.add(new JLabel("Computer Wins:"));
        statsPanel.add(compWinsField);
        statsPanel.add(new JLabel("Ties:"));
        statsPanel.add(tiesField);

        center.add(statsPanel, BorderLayout.WEST);

        logArea.setEditable(false);
        JScrollPane sp = new JScrollPane(logArea);
        sp.setBorder(new TitledBorder("Game Log"));
        center.add(sp, BorderLayout.CENTER);

        add(center, BorderLayout.CENTER);

        RPSListener listener = new RPSListener();
        rockBtn.addActionListener(listener);
        paperBtn.addActionListener(listener);
        scissorsBtn.addActionListener(listener);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void setIconButton(JButton btn, String toolTip, String resource) {
        ImageIcon icon = null;
        try { icon = new ImageIcon(getClass().getResource(resource)); } catch (Exception ignored) {}
        if (icon != null) {
            Image img = icon.getImage().getScaledInstance(64,64, Image.SCALE_SMOOTH);
            btn.setIcon(new ImageIcon(img));
        } else {
            btn.setText(toolTip);
            btn.setFont(new Font("SansSerif", Font.BOLD, 18));
        }
        btn.setToolTipText(toolTip);
    }

    private class RPSListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent ae) {
            Object src = ae.getSource();
            String playerMove = "R";
            if (src == rockBtn) playerMove = "R";
            else if (src == paperBtn) playerMove = "P";
            else if (src == scissorsBtn) playerMove = "S";

            recordPlayerMove(playerMove);

            int pick = rand.nextInt(100) + 1;
            Strategy chosenStrategy;
            if (pick <= 10) chosenStrategy = cheat;
            else if (pick <= 30) chosenStrategy = leastUsedInner;
            else if (pick <= 50) chosenStrategy = mostUsedInner;
            else if (pick <= 70) chosenStrategy = lastUsedInner;
            else chosenStrategy = randomStrat;

            String compMove = chosenStrategy.getMove(playerMove);
            String result = resolveRound(playerMove, compMove);
            String desc = describeResult(playerMove, compMove, result);
            String line = desc + " (Computer: " + chosenStrategy.getName() + ")";
            logArea.append(line + "\n");
            updateStatsFields();
            lastPlayerMove = playerMove;
        }
    }

    private void recordPlayerMove(String playerMove) {
        switch (playerMove) {
            case "R": playerRCount++; break;
            case "P": playerPCount++; break;
            case "S": playerSCount++; break;
        }
    }

    private String resolveRound(String player, String comp) {
        if (player.equals(comp)) { ties++; return "Tie"; }
        if (player.equals("R") && comp.equals("S") ||
                player.equals("P") && comp.equals("R") ||
                player.equals("S") && comp.equals("P")) {
            playerWins++; return "Player";
        } else {
            compWins++; return "Computer";
        }
    }

    private String describeResult(String p, String c, String winner) {
        String moveDesc = "";
        if ((p.equals("R") && c.equals("S")) || (c.equals("R") && p.equals("S"))) moveDesc = "Rock breaks scissors";
        else if ((p.equals("P") && c.equals("R")) || (c.equals("P") && p.equals("R"))) moveDesc = "Paper covers rock";
        else if ((p.equals("S") && c.equals("P")) || (c.equals("S") && p.equals("P"))) moveDesc = "Scissors cut paper";
        String who = winner.equals("Tie") ? " (Tie)" : winner.equals("Player") ? " (Player wins)" : " (Computer wins)";
        String pm = expandMove(p);
        String cm = expandMove(c);
        return moveDesc + "Player: " + pm + " Computer: " + cm + who;
    }

    private String expandMove(String m) {
        switch (m) {
            case "R": return "Rock";
            case "P": return "Paper";
            default:  return "Scissors";
        }
    }

    private void updateStatsFields() {
        playerWinsField.setText(String.valueOf(playerWins));
        compWinsField.setText(String.valueOf(compWins));
        tiesField.setText(String.valueOf(ties));
    }

    private class LeastUsed implements Strategy {
        @Override
        public String getMove(String playerMove) {
            if (playerRCount <= playerPCount && playerRCount <= playerSCount) {
                return moveThatBeats("R");
            } else if (playerPCount <= playerRCount && playerPCount <= playerSCount) {
                return moveThatBeats("P");
            } else {
                return moveThatBeats("S");
            }
        }
        @Override
        public String getName() { return "LeastUsed"; }
    }

    private class MostUsed implements Strategy {
        @Override
        public String getMove(String playerMove) {
            if (playerRCount >= playerPCount && playerRCount >= playerSCount) {
                return moveThatBeats("R");
            } else if (playerPCount >= playerRCount && playerPCount >= playerSCount) {
                return moveThatBeats("P");
            } else {
                return moveThatBeats("S");
            }
        }
        @Override
        public String getName() { return "MostUsed"; }
    }

    private class LastUsed implements Strategy {
        @Override
        public String getMove(String playerMove) {
            if (lastPlayerMove == null) {
                return randomStrat.getMove(playerMove);
            }
            return moveThatBeats(lastPlayerMove);
        }
        @Override
        public String getName() { return "LastUsed"; }
    }

    private String moveThatBeats(String symbol) {
        switch (symbol) {
            case "R": return "P";
            case "P": return "S";
            case "S": return "R";
            default:  return randomStrat.getMove("R");
        }
    }
}
