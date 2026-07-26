package com.acrist.managers;

import com.jcraft.jsch.*;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class SftpManager implements AutoCloseable{

    private final String host;
    private final int port;
    private final String username;
    private final String password;

    private Session session;
    private ChannelSftp channel;
    private boolean isConnected = false;

    public SftpManager(String host, int port, String username, String password) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    public void connect() throws SftpException {
        try{
            JSch jsch = new JSch();
            session = jsch.getSession(username, host, port);
            session.setPassword(password);

            session.setConfig("StrictHostKeyChecking", "no");

            session.setTimeout(10000);
            session.connect();

            channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect();

            isConnected = true;
            System.out.println("Connected to " + host + ":" + port);

        } catch (JSchException e) {
            throw new SftpException(1, "Ошибка подключения к серверу: " + e.getMessage());
        }
    }

    public String readFile(String path) throws SftpException{
        checkConnection();

        try (InputStream inputStream = channel.get(path);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1){
                outputStream.write(buffer, 0, bytesRead);
            }

            return outputStream.toString(StandardCharsets.UTF_8.name());
        } catch (IOException e){
            throw new SftpException(13, "Ошибка io-инструментов чтения: " + e.getMessage());
        }
    }

    public void writeFile(String path, String content) throws SftpException{
        checkConnection();

        try (OutputStream outputStream = channel.put(path)) {
            outputStream.write(content.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
        } catch (SftpException e) {
            throw new SftpException(e.id, "Ошибка записи в файл: " + e.getMessage());
        } catch (IOException e) {
            throw new SftpException(13, "Ошибка io-инструметов записи: " + e.getMessage());
        }
    }

    @Override
    public void close() {
        if (channel != null && channel.isConnected()) {
            channel.disconnect();
        }
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
        isConnected = false;
        System.out.println("Соединение с SFTP-сервером закрыто.");
    }

    public void createFile(String path) throws SftpException {
        checkConnection();

        try (OutputStream outputStream = channel.put(path)) {
            outputStream.write("{\"addresses\": []}".getBytes(StandardCharsets.UTF_8));
            System.out.println("Файл не найден, создан по пути: "  + path);
        } catch (IOException e) {
            throw new SftpException(13, "Ошибка создания файла: " + e.getMessage());
        }
    }

    private void checkConnection() throws SftpException {
        if (!isConnected || session == null || channel == null || !channel.isConnected()){
            isConnected = false;
            throw new SftpException(1, "Нет подключения к серверу");
        }
    }

}
