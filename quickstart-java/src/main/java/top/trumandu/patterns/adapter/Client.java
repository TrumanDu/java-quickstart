package top.trumandu.patterns.adapter;

/**
 * @author Truman.P.Du
 * @date 2022/08/12
 * @description 适配器不是在详细设计时添加的，而是解决正在服役的项目的问题。
 */
public class Client {
    public static void main(String[] args) {
        Electron110 electron110 = new Electron110();
        ChargerAdapter chargerAdapter = new ChargerAdapter(electron110);
        chargerAdapter.charge();
    }
}
