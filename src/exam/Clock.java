/* This class creates a clock object to be displayed in the client area of a window.
 * Its constructor accepts either true or false as parameter.
 * If you give the true paramenter, is gonna be a normal clock giving you the
 * current time. On the otherside, if you provide a false parameter, then is going to
 * be a countdown clock. Becareful! if you give false as the constructor's
 * parameter you have to provide the minutes with the function setMinutes.
 * Otherwise, is not going to countdown.
 *
 * Developed by         : Santiago De La Torre.
 * Last Modification    : Dic-12-2010.
 * Educational Center   : Bunker Hill Community College.
 *
 */
package exam;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Clock extends JPanel implements Runnable {

    JLabel tictoc = new JLabel();
    int iSecond = 1000, imin = 0, isec = 0;
    boolean isTime; //Time or contdown
    boolean bFlag = true;

    public Clock(boolean bT) {
        setBackground(Color.white);
        isTime = bT;
        add(tictoc);
    }

    public void run() {
        if (isTime) {
            while (true) {
                tictoc.setText(time("h:mm:ss"));
                try {
                    Thread.sleep(iSecond);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Clock.class.getName()).log(Level.SEVERE, null, ex);
                }
                repaint();
            }
        } else {
            while (true) {
                tictoc.setForeground(Color.black);
                if (isec == 0 && imin == 0) {
                    continue;
                }
                if (isec == 0) {
                    isec = 60;
                    if (imin > 0) {
                        imin--;
                    }
                }
                if (bFlag) {
                    isec--;
                }
                tictoc.setText(Integer.toString(imin) + " : " + Integer.toString(isec));
                if (imin < 2) {
                    tictoc.setForeground(Color.red);//time getti' over
                }
                repaint();
                try {
                    Thread.sleep(iSecond);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Clock.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public void setMinute(int minut) {
        imin = minut;
    }
    public void setSeconds(int sec) {
        isec = sec;
    }
    public String time(String format) {
        Calendar hour = Calendar.getInstance();
        SimpleDateFormat sdFormat = new SimpleDateFormat(format);
        return sdFormat.format(hour.getTime());
    }
}
