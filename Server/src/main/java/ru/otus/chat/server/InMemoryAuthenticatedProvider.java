package ru.otus.chat.server;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class InMemoryAuthenticatedProvider implements AuthenticatedProvider {
    private class User {
        private String login;
        private String password;
        private String username;
        private final String role;

        public User(String login, String password, String username, String role) {
            this.login = login;
            this.password = password;
            this.username = username;
            this.role = role;
        }
    }

    private List<User> users;
    private Server server;

    public InMemoryAuthenticatedProvider(Server server) {
        this.server = server;
        this.users = new CopyOnWriteArrayList<>();
        this.users.add(new User("admin", "admin", "admin1", "admin"));
        this.users.add(new User("qwe", "qwe", "qwe1", "user"));
        this.users.add(new User("asd", "asd", "asd1", "user"));
        this.users.add(new User("zxc", "zxc", "zxc1", "user"));
    }

    @Override
    public void initialize() {
        System.out.println("Сервис аунтентификации запущен: InMemory режим");
    }

    private String getUsernameByLoginAndPassword(String login, String password) {
        for (User user : users) {
            if (user.login.equals(login.toLowerCase()) && user.password.equals(password)) {
                return user.username;
            }
        }
        return null;
    }

    private boolean isLoginAlreadyExists(String login) {
        for (User user : users) {
            if (user.login.equals(login.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private boolean isUsernameAlreadyExists(String username) {
        for (User user : users) {
            if (user.username.equalsIgnoreCase(username)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean authenticate(ClientHandler clientHandler, String login, String password) {
        String authUsername = getUsernameByLoginAndPassword(login, password);
        if (authUsername == null) {
            clientHandler.sendMsg("Некорректный логин/пароль");
            return false;
        }
        if (server.isUsernameBusy(authUsername)) {
            clientHandler.sendMsg("Указанная учетная запись уже занята");
            return false;
        }
        clientHandler.setUsername(authUsername);
        server.subscribe(clientHandler);
        clientHandler.sendMsg("/authok " + authUsername);
        return true;
    }

    @Override
    public boolean registration(ClientHandler clientHandler, String login, String password, String username, String role) {
        if (login.length() < 3) {
            clientHandler.sendMsg("Логин должен быть 3+ символа");
            return false;
        }
        if (username.length() < 3) {
            clientHandler.sendMsg("Имя пользователя должна быть 3+ символа");
            return false;
        }
        if (password.length() < 3) {
            clientHandler.sendMsg("Пароль должен быть 3+ символа");
            return false;
        }
        if (isLoginAlreadyExists(login)) {
            clientHandler.sendMsg("Такой логин уже занят");
            return false;
        }
        if (isUsernameAlreadyExists(username)) {
            clientHandler.sendMsg("Такое имя пользователя уже занято");
            return false;
        }
        users.add(new User(login, password, username, role));
        clientHandler.setUsername(username);
        server.subscribe(clientHandler);
        clientHandler.sendMsg("/regok " + username);
        return true;
    }
    public String getUserRole(String username) {
        for (User user : users) {
            if (user.username.equalsIgnoreCase(username)) {
                return user.role;
            }
        } return null;
    }

}