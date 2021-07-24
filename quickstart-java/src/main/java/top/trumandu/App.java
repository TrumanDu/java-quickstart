package top.trumandu;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hello world!
 */
public class App {
    private static Logger logger = LoggerFactory.getLogger(App.class);



    public static void main(String[] args) {
        User user = new User("a");
        App app = new App();
        app.show(user);
        System.out.println(user.getName());

    }

    public synchronized void show(User user) {
        user = new User("b");
    }

    static class User {
        private String name;

        public User(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}

