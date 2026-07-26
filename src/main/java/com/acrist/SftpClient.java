package com.acrist;

import com.acrist.data.Address;
import com.acrist.managers.SftpManager;
import com.acrist.utils.JsonParser;
import com.acrist.utils.JsonSerializer;
import com.acrist.utils.ValidationUtils;
import com.jcraft.jsch.SftpException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

public class SftpClient {

    private static final String FILE_PATH = "upload/addresses.json";

    private static Scanner scanner;

    public static void main(String[] args) {
        scanner = new Scanner(System.in);

        System.out.println("----SFTP JSON CLIENT----");

        System.out.println("Введите хост: ");
        String host = scanner.nextLine();

        int port = 0;
        System.out.println("Введите порт: ");
        try {
            port = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.err.println("Порт - это число!");
            return;
        }

        System.out.println("Введите логин: ");
        String username = scanner.nextLine();

        System.out.println("Введите пароль: ");
        String password = scanner.nextLine();

        try (SftpManager sftpManager = new SftpManager(host, port, username, password)){
            sftpManager.connect();

            List<Address> addresses = loadData(sftpManager);
            System.out.println("Записей загружено: " + addresses.size() + "\n");

            boolean isRunning = true;
            while (isRunning){
                printMenu();

                int choice = 0;
                try {
                    choice = Integer.parseInt(scanner.nextLine().trim());
                } catch (NumberFormatException e) {
                    System.err.println("Введите число, пожалуйста");
                    continue;
                }

                System.out.println();

                switch (choice){
                    case 1:
                        printAllAddresses(addresses);
                        break;
                    case 2:
                        findIpByDomain(addresses);
                        break;
                    case 3:
                        findDomainByIp(addresses);
                        break;
                    case 4:
                        addNewAddress(addresses, sftpManager);
                        break;
                    case 5:
                        deleteAddress(addresses, sftpManager);
                        break;
                    case 6:
                        System.out.println("Завершение работы");
                        isRunning = false;
                        break;
                    default:
                        System.out.println("Введено число не от 1 до 6");
                }
            }

        } catch (SftpException e) {
            System.err.println("Ошибка работы с SFTP: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Непонятная ошибка: " + e.getMessage());
        }
        finally {
            scanner.close();
        }
    }

    private static void deleteAddress(List<Address> addresses, SftpManager sftpManager){
        System.out.println("----Удаление адреса----");

        System.out.println("Введите домен или ip для удаления адреса: ");
        String domainOrIp = scanner.nextLine();

        boolean removed;
        if (ValidationUtils.isValidIpv4(domainOrIp)){
            removed = addresses.removeIf(address -> address.getIp().equals(domainOrIp));
        }
        else{
            removed = addresses.removeIf(address -> address.getDomain().equals(domainOrIp));
        }

        try {
            saveData(sftpManager, addresses);
        } catch (SftpException e) {
            System.err.println("По логике такого не должно быть, но вот ошибка: " + e.getMessage());
        }

        if (removed) {
            System.out.println("Адрес удален");
        }
        else{
            System.out.println("Адреса с таким доменом или ip нет");
        }
    }

    private static void addNewAddress(List<Address> addresses, SftpManager sftpManager) {
        System.out.println("----Добавление нового адреса----");

        System.out.println("Введите домен: ");
        String domain = scanner.nextLine().trim();

        if (!ValidationUtils.isDomainUnique(addresses, domain)){
            System.out.println("Такой Домен уже есть в списке");
            return;
        }

        System.out.println("Введите ip: ");
        String ip = scanner.nextLine().trim();

        if (!ValidationUtils.isValidIpv4(ip)){
            System.out.println("Введенный ip невалиден, должен быть ipv4");
            return;
        }

        if (!ValidationUtils.isUniqueIp(addresses, ip)){
            System.out.println("такой ip уже есть в списке");
            return;
        }

        addresses.add(new Address(domain, ip));

        try {
            saveData(sftpManager, addresses);
        } catch (SftpException e) {
            System.err.println("Запись не добалена, ошибка работы с сервером: " + e.getMessage());
            addresses.remove(new Address(domain, ip));
            return;
        }

        System.out.println("Запись добавлена");
    }

    private static void saveData(SftpManager sftpManager, List<Address> addresses) throws SftpException {
        String json = JsonSerializer.toJSON(addresses);
        sftpManager.writeFile(FILE_PATH, json);
    }

    private static void findDomainByIp(List<Address> addresses) {
        System.out.println("Введите IP для поиска: ");
        String ip = scanner.nextLine().trim();

        addresses.stream()
                .filter(a -> a.getIp().equals(ip))
                .findFirst()
                .ifPresent(a -> System.out.println("Результат поиска: " + a.getDomain()));
    }

    private static void findIpByDomain(List<Address> addresses) {
        System.out.println("Введите домен для поиска: ");
        String domain = scanner.nextLine().trim();

        addresses.stream()
                .filter(a -> a.getDomain().equals(domain))
                .findFirst()
                .ifPresent(a -> System.out.println("Результат поиска: " + a.getIp()));
    }

    private static void printAllAddresses(List<Address> addresses) {
        System.out.println("----Вывод Списка адресов----");

        if (addresses.isEmpty()){
            System.out.println("Список пуст");
            return;
        }

        addresses.sort(Comparator.comparing(Address::getDomain));

        for (Address address : addresses){
            System.out.println(address.toString());
        }
        System.out.println();
    }

    private static void printMenu() {
        System.out.println("----МЕНЮ----");
        System.out.println("1. Получить список 'Домен-адрес'");
        System.out.println("2. Получить IP-адрес по доменному имени");
        System.out.println("3. Получить доменное имя по IP-адресу");
        System.out.println("4. Добавить новые 'Домен-адрес'");
        System.out.println("5. Удалить один 'Домен-адрес'");
        System.out.println("6. Завершить работу");
        System.out.println("Выберите пункт меню (1-6): ");
    }

    private static List<Address> loadData(SftpManager sftpManager) throws SftpException {
        try {
            String jsonContent = sftpManager.readFile(FILE_PATH);
            return JsonParser.parse(jsonContent);
        } catch (SftpException e) {
            if (e.id == com.jcraft.jsch.ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                    sftpManager.createFile(FILE_PATH);
                    return new ArrayList<>();
            }
            throw e;
        }
    }
}