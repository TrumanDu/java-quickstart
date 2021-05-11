package top.trumandu.patterns.command;

/**
 * Created by td20 on 2018/6/12.
 */
public class Invoke {

    public void action(Command command) {
        command.execute();
    }
}
