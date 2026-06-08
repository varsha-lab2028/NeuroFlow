package com.neuroflow.ui.panels;

import com.neuroflow.model.PracticeActivity;
import com.neuroflow.model.Student;
import com.neuroflow.service.AnalyticsService;
import com.neuroflow.service.PracticeActivityService;
import com.neuroflow.service.StudentService;
import com.neuroflow.ui.MainFrame;
import com.neuroflow.ui.components.*;
import com.neuroflow.ui.theme.ThemeManager;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import java.util.Map;

public class EducatorPanel extends BasePanel {

    private JPanel tabBar;
    private JPanel tabContent;
    private String activeTab = "overview";

    private final StudentService          studentService  = StudentService.get();
    private final AnalyticsService        analyticsService = AnalyticsService.get();
    private final PracticeActivityService activityService  = PracticeActivityService.get();

    public EducatorPanel(MainFrame frame) {
        super(frame, "Educator Dashboard", true, false, null);
        refresh();
    }

    @Override
    public void refresh() {
        contentArea.removeAll();
        ThemeManager tm = ThemeManager.get();

        // Greeting
        addFull(label("Class Overview 🎓", 20, true, tm.tx()));
        addGap(4);
        addFull(label("Here's how your students are doing", 13, false, tm.sub()));
        addGap(14);

        // Tab bar
        tabBar = buildTabBar();
        tabBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        addFull(tabBar);
        addGap(14);

        // Tab content area
        tabContent = new JPanel();
        tabContent.setOpaque(false);
        tabContent.setLayout(new BoxLayout(tabContent, BoxLayout.Y_AXIS));
        tabContent.setAlignmentX(LEFT_ALIGNMENT);
        addFull(tabContent);

        showTab(activeTab);

        contentArea.revalidate();
        contentArea.repaint();
    }

    // ── Tab bar ───────────────────────────────────────────────────
    private JPanel buildTabBar() {
        JPanel bar = new JPanel(new GridLayout(1, 3, 4, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ThemeManager.get().alt());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
            }
        };
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(4, 4, 4, 4));
        bar.setAlignmentX(LEFT_ALIGNMENT);

        bar.add(buildTabBtn("Overview",   "overview"));
        bar.add(buildTabBtn("This Week",  "thisweek"));
        bar.add(buildTabBtn("Trends",     "trends"));
        return bar;
    }

    private JButton buildTabBtn(String text, String tabKey) {
        ThemeManager tm = ThemeManager.get();
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                boolean active = tabKey.equals(activeTab);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                if (active) {
                    g2.setColor(ThemeManager.get().sf());
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                }
                g2.setFont(getFont());
                g2.setColor(active
                        ? ThemeManager.get().tx()
                        : ThemeManager.get().sub());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                        (getWidth() - fm.stringWidth(getText())) / 2,
                        (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
                g2.dispose();
            }
        };
        btn.setFont(tabKey.equals(activeTab) ? tm.bold(13) : tm.regular(13));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            activeTab = tabKey;
            showTab(tabKey);
        });
        return btn;
    }

    // ── Tab switching ─────────────────────────────────────────────

    private void showTab(String tab) {
        tabContent.removeAll();
        switch (tab) {
            case "overview"  -> buildOverviewTab();
            case "thisweek"  -> buildThisWeekTab();
            case "trends"    -> buildTrendsTab();
        }
        // Rebuild tab bar so active highlight updates
        int idx = switch (tab) {
            case "overview" -> 0;
            case "thisweek" -> 1;
            default         -> 2;
        };
        if (tabBar != null) {
            Component[] btns = tabBar.getComponents();
            for (Component c : btns) c.repaint();
        }
        tabContent.revalidate();
        tabContent.repaint();
    }

    // ── Overview tab ──────────────────────────────────────────────

    private void buildOverviewTab() {
        ThemeManager tm = ThemeManager.get();

        // Summary stat cards
        int activeCount    = analyticsService.getActiveStudentCount();
        int practicedToday = analyticsService.getPracticedTodayCount();
        Map<String, Integer> errors = analyticsService.getAllTimeErrorTotals();
        String topMixup = errors.isEmpty() ? "b/d reversal"
                : errors.entrySet().iterator().next().getKey();

        JPanel statGrid = new JPanel(new GridLayout(1, 3, 10, 0));
        statGrid.setOpaque(false);
        statGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 88));
        statGrid.setAlignmentX(LEFT_ALIGNMENT);

        statGrid.add(buildStatCard(String.valueOf(activeCount), "Active students",  tm.ac()));
        statGrid.add(buildStatCard(String.valueOf(practicedToday), "Practised today", tm.ok()));
        statGrid.add(buildStatCard(topMixup, "Common mix-up", tm.warn()));

        tabContent.add(statGrid);
        tabContent.add(Box.createVerticalStrut(14));

        // Student progress list
        JLabel listTitle = label("Student Progress", 15, true, tm.tx());
        listTitle.setAlignmentX(LEFT_ALIGNMENT);
        tabContent.add(listTitle);
        tabContent.add(Box.createVerticalStrut(10));

        List<Student> students = studentService.getAllStudents();
        if (students.isEmpty()) {
            JLabel empty = label("No students found. Check seed data.", 13, false, tm.sub());
            empty.setAlignmentX(LEFT_ALIGNMENT);
            tabContent.add(empty);
        } else {
            for (Student s : students) {
                StudentRowPanel row = new StudentRowPanel(s);
                row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 88));
                row.setAlignmentX(LEFT_ALIGNMENT);
                tabContent.add(row);
                tabContent.add(Box.createVerticalStrut(8));
            }
        }
    }

    // ── This Week tab ─────────────────────────────────────────────

    private void buildThisWeekTab() {
        ThemeManager tm = ThemeManager.get();

        // Classroom focus section
        JLabel focusTitle = label("📌 Classroom Focus", 15, true, tm.tx());
        focusTitle.setAlignmentX(LEFT_ALIGNMENT);
        tabContent.add(focusTitle);
        tabContent.add(Box.createVerticalStrut(4));

        JLabel focusSub = label(
                "Gentle practice suggestions for this week — not homework",
                12, false, tm.sub());
        focusSub.setAlignmentX(LEFT_ALIGNMENT);
        tabContent.add(focusSub);
        tabContent.add(Box.createVerticalStrut(10));

        List<PracticeActivity> activities = activityService.getActivitiesForThisWeek();

        if (activities.isEmpty()) {
            // Show a placeholder card if no activities yet
            tabContent.add(buildEmptyActivityCard());
        } else {
            for (PracticeActivity a : activities) {
                tabContent.add(buildActivityCard(a));
                tabContent.add(Box.createVerticalStrut(10));
            }
        }

        tabContent.add(Box.createVerticalStrut(16));

        // Share with parents button
        RoundedButton shareBtn = new RoundedButton(
                "📤  Share focus with parents", RoundedButton.Style.PRIMARY);
        shareBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        shareBtn.setAlignmentX(LEFT_ALIGNMENT);
        shareBtn.addActionListener(e -> {
            List<PracticeActivity> all = activityService.getAll();
            for (PracticeActivity a : all) {
                activityService.shareWithParents(a.getId(), true);
            }
            JOptionPane.showMessageDialog(this,
                    "Classroom focus shared with parents ✓",
                    "Shared", JOptionPane.INFORMATION_MESSAGE);
        });
        tabContent.add(shareBtn);
        tabContent.add(Box.createVerticalStrut(14));

        // Optional ideas for parents
        JLabel ideasTitle = label("💡 Optional ideas for parents", 15, true, tm.tx());
        ideasTitle.setAlignmentX(LEFT_ALIGNMENT);
        tabContent.add(ideasTitle);
        tabContent.add(Box.createVerticalStrut(10));

        String[] ideas = {
                "Ask \"which way does the bump go?\" during reading time",
                "Point out b and d in storybooks — no pressure, just noticing",
                "Celebrate the effort of practising, not just correct answers",
                "Keep sessions short — 5 minutes of focus is plenty at this age"
        };

        for (String idea : ideas) {
            tabContent.add(buildIdeaRow(idea));
            tabContent.add(Box.createVerticalStrut(8));
        }
    }

    // ── Trends tab ────────────────────────────────────────────────

    private void buildTrendsTab() {
        ThemeManager tm = ThemeManager.get();

        // Error pattern counts
        JLabel errTitle = label("📊 Common error patterns", 15, true, tm.tx());
        errTitle.setAlignmentX(LEFT_ALIGNMENT);
        tabContent.add(errTitle);
        tabContent.add(Box.createVerticalStrut(10));

        Map<String, Integer> errors = analyticsService.getAllTimeErrorTotals();

        if (errors.isEmpty()) {
            JLabel none = label("No error data yet — practice sessions will populate this.",
                    13, false, tm.sub());
            none.setAlignmentX(LEFT_ALIGNMENT);
            tabContent.add(none);
        } else {
            int maxVal = errors.values().stream().mapToInt(i -> i).max().orElse(1);
            for (Map.Entry<String, Integer> entry : errors.entrySet()) {
                tabContent.add(buildErrorRow(entry.getKey(), entry.getValue(), maxVal));
                tabContent.add(Box.createVerticalStrut(10));
            }
        }

        tabContent.add(Box.createVerticalStrut(16));

        // Weekly practice chart
        JLabel chartTitle = label("📅 Practice days this week", 15, true, tm.tx());
        chartTitle.setAlignmentX(LEFT_ALIGNMENT);
        tabContent.add(chartTitle);
        tabContent.add(Box.createVerticalStrut(10));

        // Aggregate weekly days across all students
        List<Student> students = studentService.getAllStudents();
        int[] combined = new int[7];
        for (Student s : students) {
            int[] days = com.neuroflow.service.PracticeSessionService.get()
                    .weeklyPracticeDays(s.getStudentId());
            for (int i = 0; i < 7; i++) combined[i] += days[i];
        }

        BarChartPanel chart = new BarChartPanel();
        chart.setValues(combined);
        chart.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        chart.setAlignmentX(LEFT_ALIGNMENT);
        tabContent.add(chart);
        tabContent.add(Box.createVerticalStrut(16));

        // Insight message
        tabContent.add(buildInsightCard(errors));
    }

    // ── Card builders ─────────────────────────────────────────────

    private JPanel buildStatCard(String value, String sublabel, Color accent) {
        ThemeManager tm = ThemeManager.get();
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(tm.sf());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));
                g2.setColor(tm.bd());
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f,
                        getWidth() - 1, getHeight() - 1, 14, 14));
                // Accent top bar
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, getWidth(), 4, 4, 4);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(14, 10, 10, 10));

        JLabel valLbl = new JLabel(value, SwingConstants.CENTER);
        valLbl.setFont(tm.bold(value.length() > 6 ? 12 : 20));
        valLbl.setForeground(tm.tx());
        valLbl.setAlignmentX(CENTER_ALIGNMENT);

        JLabel subLbl = new JLabel(
                "<html><center>" + sublabel + "</center></html>",
                SwingConstants.CENTER);
        subLbl.setFont(tm.regular(10));
        subLbl.setForeground(tm.sub());
        subLbl.setAlignmentX(CENTER_ALIGNMENT);

        card.add(valLbl);
        card.add(Box.createVerticalStrut(4));
        card.add(subLbl);
        return card;
    }

    private JPanel buildActivityCard(PracticeActivity a) {
        ThemeManager tm = ThemeManager.get();
        RoundedPanel card = new RoundedPanel(14);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(12, 14, 12, 14));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        card.setAlignmentX(LEFT_ALIGNMENT);

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        JLabel titleLbl = label(a.getTitle(), 14, true, tm.tx());
        topRow.add(titleLbl, BorderLayout.WEST);

        BadgeLabel badge = new BadgeLabel(
                a.isCompleted() ? "Done" : "In progress",
                a.isCompleted()
                        ? BadgeLabel.BadgeType.OK
                        : BadgeLabel.BadgeType.ACCENT);
        topRow.add(badge, BorderLayout.EAST);
        card.add(topRow);
        card.add(Box.createVerticalStrut(6));

        if (a.getDescription() != null && !a.getDescription().isBlank()) {
            JLabel desc = label(a.getDescription(), 12, false, tm.sub());
            desc.setAlignmentX(LEFT_ALIGNMENT);
            card.add(desc);
            card.add(Box.createVerticalStrut(8));
        }

        // Mark complete toggle
        RoundedButton doneBtn = new RoundedButton(
                a.isCompleted() ? "✓ Completed" : "Mark complete",
                a.isCompleted()
                        ? RoundedButton.Style.MUTED
                        : RoundedButton.Style.PRIMARY);
        doneBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        doneBtn.setAlignmentX(LEFT_ALIGNMENT);
        doneBtn.addActionListener(e -> {
            activityService.markCompleted(a.getId(), !a.isCompleted());
            showTab("thisweek");
        });
        card.add(doneBtn);
        return card;
    }

    private JPanel buildEmptyActivityCard() {
        ThemeManager tm = ThemeManager.get();
        RoundedPanel card = new RoundedPanel(14);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(16, 16, 16, 16));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        card.setAlignmentX(LEFT_ALIGNMENT);

        JLabel title = label("📌 Letters b & d", 14, true, tm.tx());
        title.setAlignmentX(LEFT_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(6));

        JLabel desc = label(
                "Visual discrimination — bump direction awareness",
                12, false, tm.sub());
        desc.setAlignmentX(LEFT_ALIGNMENT);
        card.add(desc);
        card.add(Box.createVerticalStrut(6));

        JLabel hint = label("Add activities via seed data or the service layer.",
                11, false, tm.sub());
        hint.setAlignmentX(LEFT_ALIGNMENT);
        card.add(hint);
        return card;
    }

    private JPanel buildErrorRow(String errorType, int count, int maxVal) {
        ThemeManager tm = ThemeManager.get();
        JPanel row = new JPanel();
        row.setOpaque(false);
        row.setLayout(new BoxLayout(row, BoxLayout.Y_AXIS));
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        topRow.add(label(errorType, 13, true, tm.tx()), BorderLayout.WEST);
        topRow.add(label(count + " times", 11, false, tm.sub()), BorderLayout.EAST);
        row.add(topRow);
        row.add(Box.createVerticalStrut(5));

        CustomProgressBar bar = new CustomProgressBar();
        bar.setValue((int) (100.0 * count / maxVal));
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 8));
        bar.setAlignmentX(LEFT_ALIGNMENT);

        Color barColor = count == maxVal ? tm.er()
                : count > maxVal / 2    ? tm.warn()
                : tm.ac();
        bar.setFillColor(barColor);
        row.add(bar);
        return row;
    }

    private JPanel buildIdeaRow(String text) {
        ThemeManager tm = ThemeManager.get();
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        row.setOpaque(false);
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JLabel dot = new JLabel("•");
        dot.setFont(tm.bold(16));
        dot.setForeground(tm.ac());
        row.add(dot);

        JLabel txt = label(text, 13, false, tm.tx());
        txt.setPreferredSize(new Dimension(360, 32));
        row.add(txt);
        return row;
    }

    private JPanel buildInsightCard(Map<String, Integer> errors) {
        ThemeManager tm = ThemeManager.get();
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(tm.withAlpha(tm.ac(), 18));
                g2.fill(new RoundRectangle2D.Float(0, 0,
                        getWidth(), getHeight(), 14, 14));
                g2.setColor(tm.withAlpha(tm.ac(), 55));
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f,
                        getWidth() - 1, getHeight() - 1, 14, 14));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(14, 16, 14, 16));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        card.setAlignmentX(LEFT_ALIGNMENT);

        String insight;
        if (errors.isEmpty()) {
            insight = "No trends yet — practice sessions will reveal patterns here.";
        } else {
            String top = errors.entrySet().iterator().next().getKey();
            insight = "💡 Most students are struggling with " + top
                    + ". Consider a group activity focusing on bump direction this week.";
        }

        JLabel insightLbl = new JLabel(
                "<html><body style='width:340px'>" + insight + "</body></html>");
        insightLbl.setFont(tm.regular(13));
        insightLbl.setForeground(tm.tx());
        insightLbl.setAlignmentX(LEFT_ALIGNMENT);
        card.add(insightLbl);
        return card;
    }
}