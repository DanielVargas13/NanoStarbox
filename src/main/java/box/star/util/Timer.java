package box.star.util;

import java.util.Date;
import java.util.TimerTask;

public class Timer {

    private java.util.Timer timer = new java.util.Timer();

    public TimerTask createTimeout(int time, ITimerCallback<Object> callback, Object... parameter) {
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                callback.onTimer(parameter);
            }
        };
        timer.schedule(timerTask, time);
        return timerTask;
    }

    public TimerTask createInterval(int time, ITimerCallback<Object> callback, Object... parameter) {
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                callback.onTimer(parameter);
            }
        };
        timer.schedule(timerTask, time, time);
        return timerTask;
    }

    public TimerTask createAlarm(Date time, ITimerCallback<Object> callback, Object... parameter) {
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                callback.onTimer(parameter);
            }
        };
        timer.schedule(timerTask, time);
        return timerTask;
    }

    public void cancelTimers(){
        timer.cancel();
    }

}
