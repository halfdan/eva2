package eva2.gui;

import com.sun.management.OperatingSystemMXBean;

import javax.swing.*;
import java.awt.*;
import java.lang.management.ManagementFactory;
import java.util.LinkedList;

/**
 * Small Panel that shows the current OS and Process CPU usage.
 *
 * Sliding window with n time steps, where each time step has a
 * resolution of 500ms.
 */
public class CPUPanel extends JPanel {
    private LinkedList<Double> processLoadList = new LinkedList<>();
    private LinkedList<Double> osLoadList = new LinkedList<>();
    private int maxTimeSteps = 100;
    private OperatingSystemMXBean osBean;

    public CPUPanel(int timeSteps) {
        Timer timer = new Timer(500, e -> {
            this.updateLoad();
            this.repaint();
        });
        timer.start();
        maxTimeSteps = timeSteps;
        setMinimumSize(new Dimension(timeSteps, 1));
        setPreferredSize(new Dimension(timeSteps, 1));
        osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);

    }

    private void updateLoad() {
        // What % CPU load this current JVM is taking, from 0.0-1.0
        processLoadList.add(osBean.getProcessCpuLoad());

        // What % load the overall system is at, from 0.0-1.0
        osLoadList.add(osBean.getSystemCpuLoad());

        if (processLoadList.size() > maxTimeSteps) {
            processLoadList.removeFirst();
        }

        if (osLoadList.size() > maxTimeSteps) {
            osLoadList.removeFirst();
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Dimension panelSize = this.getSize();

        Graphics2D g2d = (Graphics2D)g;
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        g2d.setColor(Color.LIGHT_GRAY);
        int pos = maxTimeSteps - osLoadList.size();
        for(Double load : osLoadList) {
            g2d.drawLine(pos, panelSize.height - 1, pos, (int)(panelSize.height - 1 - (panelSize.height * load)));
            pos++;
        }

        g2d.setColor(Color.GREEN);
        pos = maxTimeSteps - processLoadList.size();
        for(Double load : processLoadList) {
            g2d.drawLine(pos, panelSize.height - 1, pos, (int)(panelSize.height - 1 - (panelSize.height * load)));
            pos++;
        }
    }
}
