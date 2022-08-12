package top.trumandu.patterns.adapter;

/**
 * @author Truman.P.Du
 * @date 2022/08/12
 * @description
 */
public class ChargerAdapter implements Charger {
    private Electron110 electron110;

    public ChargerAdapter(Electron110 electron110) {
        this.electron110 = electron110;
    }

    @Override
    public void charge() {
        System.out.println("转换电压");
        electron110.action();
    }
}
