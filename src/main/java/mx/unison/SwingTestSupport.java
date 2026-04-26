package mx.unison;

import junit.framework.TestCase;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public abstract class SwingTestSupport extends TestCase {

    protected JButton findButton(Container root, String text) {
        for (Component c : root.getComponents()) {
            if (c instanceof JButton) {
                JButton b = (JButton) c;
                if (text.equals(b.getText())) {
                    return b;
                }
            }
            if (c instanceof Container) {
                JButton found = findButton((Container) c, text);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    protected <T extends Component> List<T> findAll(Container root, Class<T> type) {
        List<T> out = new ArrayList<T>();
        collect(root, type, out);
        return out;
    }

    private <T extends Component> void collect(Container root, Class<T> type, List<T> out) {
        for (Component c : root.getComponents()) {
            if (type.isInstance(c)) {
                out.add(type.cast(c));
            }
            if (c instanceof Container) {
                collect((Container) c, type, out);
            }
        }
    }

    protected JDialog waitForDialog(String title, long timeoutMs) throws InterruptedException {
        long end = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < end) {
            for (Window w : Window.getWindows()) {
                if (w instanceof JDialog && w.isShowing()) {
                    JDialog d = (JDialog) w;
                    if (title == null || title.equals(d.getTitle())) {
                        return d;
                    }
                }
            }
            Thread.sleep(50L);
        }
        return null;
    }

    protected void runOnEdtAndWait(Runnable action) throws Exception {
        if (SwingUtilities.isEventDispatchThread()) {
            action.run();
        } else {
            SwingUtilities.invokeAndWait(action);
        }
    }

    protected void disposeDialog(final JDialog dialog) throws Exception {
        if (dialog != null) {
            runOnEdtAndWait(new Runnable() {
                public void run() {
                    dialog.dispose();
                }
            });
        }
    }
}