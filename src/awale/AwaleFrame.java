package awale;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

final class AwaleFrame extends JFrame {
    private static final String AUTHOR = "Kalema Daniel Jonathan";
    private static final Color PAPER = new Color(0xF7F2E8);
    private static final Color INK = new Color(0x20382D);
    private static final Color MUTED = new Color(0x4F6257);
    private static final Color CREAM = new Color(0xFFF1D2);
    private static final Color GOLD = new Color(0xFFD477);

    private AwaleGame game;
    private final Map<String, Integer> visits = new HashMap<>();
    private final PitButton[] pits = new PitButton[AwaleGame.PIT_COUNT];
    private final JLabel[] rowLabels = new JLabel[2];
    private final JLabel status = label("", INK, 19, Font.BOLD);
    private final JLabel move = label("", MUTED, 14, Font.PLAIN);
    private final JCheckBox computerMode = new JCheckBox("Contre l’ordinateur", true);
    private final JComboBox<AwaleComputer.Level> difficulty = new JComboBox<>(AwaleComputer.Level.values());
    private SwingWorker<Integer, Void> computerTurn;
    private String report;
    private int lastPlayed;

    AwaleFrame() {
        super("Demo_Java_Jeu_Awale");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setJMenuBar(createMenu());

        JPanel content = new JPanel(new BorderLayout(0, 14));
        content.setBackground(PAPER);
        content.setBorder(new EmptyBorder(20, 27, 22, 27));
        setContentPane(content);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = label("Awalé", INK, 46, Font.BOLD);
        title.setFont(new Font("Serif", Font.BOLD, 46));
        header.add(title, BorderLayout.WEST);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        actions.setOpaque(false);
        computerMode.setOpaque(false);
        computerMode.addActionListener(event -> updateMode());
        difficulty.getAccessibleContext().setAccessibleName("Niveau de l’ordinateur");
        difficulty.addActionListener(event -> updateMode());
        actions.add(computerMode);
        actions.add(difficulty);
        JButton restart = new JButton("Nouvelle partie");
        restart.addActionListener(event -> reset());
        actions.add(restart);
        header.add(actions, BorderLayout.EAST);
        content.add(header, BorderLayout.NORTH);

        JPanel board = new JPanel(new GridLayout(2, 1, 0, 14)) {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setPaint(new GradientPaint(0, 0, new Color(0x6A3E2E),
                        getWidth(), getHeight(), new Color(0x442820)));
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 32, 32);
                g.setColor(new Color(0x97634A));
                g.drawRoundRect(5, 5, getWidth() - 11, getHeight() - 11, 26, 26);
                g.dispose();
            }
        };
        board.setOpaque(false);
        board.setBorder(new EmptyBorder(14, 19, 14, 19));
        board.add(createSide(1));
        board.add(createSide(0));
        content.add(board, BorderLayout.CENTER);

        JPanel footer = new JPanel(new GridLayout(2, 1, 0, 3));
        footer.setOpaque(false);
        footer.add(status);
        footer.add(move);
        content.add(footer, BorderLayout.SOUTH);

        reset();
        setMinimumSize(new Dimension(710, 575));
        setSize(950, 700);
        setLocationRelativeTo(null);
    }

    private JMenuBar createMenu() {
        JMenuBar bar = new JMenuBar();
        JMenu gameMenu = new JMenu("Partie");
        JMenuItem restart = new JMenuItem("Nouvelle partie");
        restart.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK));
        restart.addActionListener(event -> reset());
        gameMenu.add(restart);
        bar.add(gameMenu);
        JMenu help = new JMenu("Aide");
        JMenuItem about = new JMenuItem("À propos");
        about.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
        about.addActionListener(event -> showAbout());
        help.add(about);
        bar.add(help);
        return bar;
    }

    private JPanel createSide(int player) {
        JPanel side = new JPanel(new BorderLayout(0, 5));
        side.setOpaque(false);
        rowLabels[player] = label("", CREAM, 15, Font.BOLD);
        rowLabels[player].setBorder(new EmptyBorder(0, 3, 0, 0));
        JPanel rows = new JPanel(new GridLayout(2, 1, 0, 5));
        rows.setOpaque(false);
        int firstRow = player == 0 ? 2 : 0;
        rows.add(createRow(firstRow));
        rows.add(createRow(firstRow + 1));
        side.add(rowLabels[player], BorderLayout.NORTH);
        side.add(rows, BorderLayout.CENTER);
        return side;
    }

    private JPanel createRow(int row) {
        JPanel buttons = new JPanel(new GridLayout(1, AwaleGame.COLUMNS, 7, 0));
        buttons.setOpaque(false);
        for (int column = 0; column < AwaleGame.COLUMNS; column++) {
            final int pit = AwaleGame.pitAt(row, column);
            PitButton button = new PitButton(pitName(pit));
            button.addActionListener(event -> {
                if (!isComputerTurn() && game.canPlay(pit)) {
                    play(pit);
                }
            });
            pits[pit] = button;
            buttons.add(button);
        }
        return buttons;
    }

    private void reset() {
        cancelComputer();
        game = new AwaleGame();
        visits.clear();
        visits.put(game.positionKey(), 1);
        lastPlayed = -1;
        report = "Choisissez une case dorée. Le joueur du bas commence.";
        refresh();
    }

    private void play(int pit) {
        String player = playerName(game.turn());
        game = game.play(pit);
        if (!game.finished() && visits.merge(game.positionKey(), 1, Integer::sum) == 3) {
            game = game.draw();
        }
        lastPlayed = pit;
        int captured = game.captured();
        String capture = captured == 0 ? "aucune prise"
                : quantity(captured) + (captured == 1 ? " prise" : " prises");
        report = player + " · Case " + pitName(pit) + " · " + capture;
        refresh();
        if (isComputerTurn()) {
            startComputer();
        } else {
            focusPlayablePit();
        }
    }

    private void updateMode() {
        cancelComputer();
        difficulty.setEnabled(computerMode.isSelected());
        refresh();
        if (isComputerTurn()) {
            startComputer();
        }
    }

    private void cancelComputer() {
        if (computerTurn != null) {
            computerTurn.cancel(true);
            computerTurn = null;
        }
    }

    private void startComputer() {
        AwaleGame position = game;
        AwaleComputer.Level level = (AwaleComputer.Level) difficulty.getSelectedItem();
        computerTurn = new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() throws InterruptedException {
                Thread.sleep(250);
                return AwaleComputer.choose(position, level);
            }

            @Override
            protected void done() {
                if (computerTurn != this || isCancelled()) {
                    return;
                }
                computerTurn = null;
                int pit;
                try {
                    pit = get();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    return;
                } catch (ExecutionException ex) {
                    JOptionPane.showMessageDialog(AwaleFrame.this,
                            "L’ordinateur n’a pas pu jouer. La partie continue à deux joueurs.",
                            "Coup interrompu", JOptionPane.WARNING_MESSAGE);
                    computerMode.setSelected(false);
                    updateMode();
                    return;
                }
                play(pit);
            }
        };
        computerTurn.execute();
    }

    private boolean isComputerTurn() {
        return computerMode.isSelected() && !game.finished() && game.turn() == AwaleGame.OPPONENT;
    }

    private String playerName(int player) {
        if (computerMode.isSelected()) {
            return player == AwaleGame.HUMAN ? "Vous" : "Ordinateur";
        }
        return "Joueur " + (player + 1);
    }

    private void focusPlayablePit() {
        for (PitButton button : pits) {
            if (button.isEnabled()) {
                button.requestFocusInWindow();
                break;
            }
        }
    }

    private void refresh() {
        for (int pit = 0; pit < pits.length; pit++) {
            pits[pit].update(game.seeds(pit), !isComputerTurn() && game.canPlay(pit), pit == lastPlayed);
        }
        for (int player = 0; player < rowLabels.length; player++) {
            boolean active = !game.finished() && game.turn() == player;
            String side = player == 0 ? "Bas" : "Haut";
            rowLabels[player].setText(side + "   /   " + playerName(player)
                    + "   ·   " + quantity(game.total(player)) + (active ? "   ·   À jouer" : ""));
            rowLabels[player].setForeground(active ? GOLD : CREAM);
        }
        if (game.finished()) {
            if (game.winner() == AwaleGame.DRAW) {
                status.setText("Match nul · Position répétée");
            } else if (computerMode.isSelected() && game.winner() == AwaleGame.HUMAN) {
                status.setText("Vous avez gagné");
            } else {
                status.setText(playerName(game.winner()) + " a gagné");
            }
        } else {
            status.setText(computerMode.isSelected()
                    ? (isComputerTurn() ? "L’ordinateur réfléchit…" : "À vous de jouer")
                    : "Au tour du joueur " + (game.turn() + 1));
        }
        move.setText(report);
    }

    private void showAbout() {
        String rules = "<html><body style='font-family:sans-serif; color:#20382d; margin:10px'>"
                + "<h1>Awalé</h1><p>Demo_Java_Jeu_Awale<br>"
                + "<b>Auteur&nbsp;: " + AUTHOR + "</b></p>"
                + "<h2>Règles du jeu à quatre rangées</h2>"
                + "<p><b>Départ.</b> Cette version à semis direct s’inspire de l’Omweso. "
                + "Le plateau comporte quatre rangées de huit cases, deux par joueur. "
                + "Chaque case d’une rangée extérieure reçoit quatre graines. Le bas joue en premier.</p>"
                + "<p><b>Semis.</b> Prenez les graines d’une de vos cases qui en contient au moins "
                + "deux. Distribuez-les une par une dans vos deux rangées, dans le sens inverse "
                + "des aiguilles d’une montre. Au départ du bas, le trajet suit A4 vers H4, "
                + "puis H3 vers A3. Si la dernière graine arrive dans une case vide, passez la main. "
                + "Si la case était occupée, reprenez ses graines et poursuivez depuis cette case.</p>"
                + "<p><b>Prise.</b> Si la dernière graine arrive dans une case intérieure déjà "
                + "occupée et que les deux cases adverses de la même colonne contiennent des graines, "
                + "prenez ces graines adverses. Semez-les à partir de la case d’origine du dernier "
                + "semis et poursuivez le tour.</p>"
                + "<p><b>Fin.</b> Un joueur gagne si l’adversaire ne peut jouer aucun coup à son tour. "
                + "Une position identique trois fois au début d’un tour, ou une boucle de semis, "
                + "met fin à la partie sur un match nul.</p>"
                + "<p><b>Adversaire.</b> Jouez à deux ou contre l’ordinateur. Facile joue au hasard, "
                + "Moyen anticipe la réponse et Difficile simule jusqu’à quatre coups successifs.</p>"
                + "</body></html>";
        JEditorPane text = new JEditorPane("text/html", rules);
        text.setEditable(false);
        text.setCaretPosition(0);
        text.setBackground(PAPER);
        text.getAccessibleContext().setAccessibleName("Règles de l’awalé et auteur");
        JScrollPane scroll = new JScrollPane(text);
        scroll.setPreferredSize(new Dimension(520, 440));
        scroll.setBorder(BorderFactory.createEmptyBorder());
        JOptionPane.showMessageDialog(this, scroll, "À propos", JOptionPane.PLAIN_MESSAGE);
    }

    private static JLabel label(String text, Color color, int size, int style) {
        JLabel label = new JLabel(text, SwingConstants.LEFT);
        label.setForeground(color);
        label.setFont(label.getFont().deriveFont(style, (float) size));
        return label;
    }

    private static String pitName(int pit) {
        for (int row = 0; row < 4; row++) {
            for (int column = 0; column < AwaleGame.COLUMNS; column++) {
                if (AwaleGame.pitAt(row, column) == pit) {
                    return "" + (char) ('A' + column) + (row + 1);
                }
            }
        }
        throw new IllegalArgumentException("Case inconnue.");
    }

    private static String quantity(int count) {
        return count + (count == 1 ? " graine" : " graines");
    }

    private static final class PitButton extends JButton {
        private final String name;
        private int count;
        private boolean played;

        PitButton(String name) {
            this.name = name;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setOpaque(false);
        }

        void update(int count, boolean playable, boolean played) {
            this.count = count;
            this.played = played;
            setEnabled(playable);
            String description = "Case " + name + " · " + quantity(count);
            setToolTipText(description);
            getAccessibleContext().setAccessibleName(description);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int width = getWidth();
            int height = getHeight();
            boolean compact = height < 70;
            Color surface = new Color(0x482D27);
            if (isEnabled()) {
                surface = getModel().isPressed() ? new Color(0x754B33) : new Color(0x644130);
            }
            g.setPaint(new GradientPaint(0, 2, new Color(0x33231F), 0, height, surface));
            g.fillRoundRect(2, 2, width - 4, height - 4, 27, 27);
            g.setColor(isEnabled() ? GOLD : new Color(0xA17157));
            g.drawRoundRect(2, 2, width - 5, height - 5, 27, 27);
            if (isFocusOwner()) {
                g.setColor(CREAM);
                g.drawRoundRect(5, 5, width - 11, height - 11, 23, 23);
            }

            g.setColor(isEnabled() ? GOLD : CREAM);
            g.setFont(getFont().deriveFont(Font.BOLD, 14f));
            g.drawString(name, compact ? 9 : 13, compact ? 18 : 22);
            if (played) {
                g.fillOval(compact ? width / 2 - 2 : width - 18, 13, 5, 5);
            }

            drawSeeds(g, width, height, compact);
            String amount = Integer.toString(count);
            g.setColor(CREAM);
            g.setFont(getFont().deriveFont(Font.BOLD, compact ? 14f : 17f));
            int textWidth = g.getFontMetrics().stringWidth(amount);
            int textX = compact ? width - 9 - textWidth : (width - textWidth) / 2;
            g.drawString(amount, textX, compact ? 18 : height - 11);
            g.dispose();
        }

        private void drawSeeds(Graphics2D g, int width, int height, boolean compact) {
            int visible = Math.min(count, 8);
            int radiusX = Math.min(22, width / 4);
            int radiusY = Math.min(12, Math.max(3, (height - 34) / 3));
            int diameter = Math.min(13, Math.max(8, radiusY + 2));
            for (int seed = 0; seed < visible; seed++) {
                double angle = 2 * Math.PI * seed / visible - Math.PI / 2;
                int x = (width - diameter) / 2;
                int y = (height - diameter) / 2 + (compact ? 7 : -1);
                if (visible > 1) {
                    x += (int) Math.round(radiusX * Math.cos(angle));
                    y += (int) Math.round(radiusY * Math.sin(angle));
                }
                g.setColor(new Color(0x32201C));
                g.fillOval(x, y + 2, diameter, diameter - 1);
                g.setPaint(new GradientPaint(x, y, new Color(0xF6DBA1),
                        x + diameter, y + diameter, new Color(0xB37C43)));
                g.fillOval(x, y, diameter, diameter - 1);
            }
        }
    }
}
