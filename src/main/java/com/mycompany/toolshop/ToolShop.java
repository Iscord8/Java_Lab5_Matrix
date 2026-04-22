package com.mycompany.toolshop;

import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;

public class ToolShop implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name = "Master Tool (Grad & Green Garden)";
    private List<Product> inventory = new ArrayList<>();

    public static void logAction(String className, String message, String type) {
        Properties props = new Properties();
        try (InputStream input = new FileInputStream("lab4.properties")) {
            props.load(input);
            if (!"on".equals(props.getProperty("logging"))) return;
        } catch (IOException e) {
            return; 
        }

        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String user = System.getProperty("user.name");
        String logEntry = String.format("%s %s %s %s", timestamp, user, message, type);

        try (FileWriter fw = new FileWriter(className + ".log", true);
             PrintWriter out = new PrintWriter(fw)) {
            out.println(logEntry);
        } catch (IOException e) {
            System.err.println("Помилка запису логу: " + e.getMessage());
        }
    }

    public static void saveSystemInfo() {
        try (FileWriter fw = new FileWriter("system_info.txt", true);
             PrintWriter out = new PrintWriter(fw)) {
            out.println(new Date() + " | OS: " + System.getProperty("os.name") + 
                        " | Java: " + System.getProperty("java.version") + 
                        " | User: " + System.getProperty("user.name"));
        } catch (IOException e) {
            logAction("ToolShop", "Помилка запису системної інфо: " + e.getMessage(), "exception");
        }
    }

    private static void showLocalizedHelp() {
        try {
            ResourceBundle bundle = ResourceBundle.getBundle("i18n.Bundle");
            System.out.println(bundle.getString("hello"));
            System.out.println(bundle.getString("help_msg"));
            logAction("ToolShop", "Допомога виведена мовою: " + Locale.getDefault(), "message");
        } catch (MissingResourceException e) {
            logAction("ToolShop", "Файл локалізації не знайдено: " + e.getMessage(), "exception");
            System.out.println("Використання: java ToolShop [matrix|systems_info|list|printlog|serialize|help]");
        }
    }

    // ВАРІАНТ 2: ЗАВДАННЯ 1 (МАТРИЦЯ)
    private static void processMatrixTask(int n) {
        int[][] matrix = new int[n][n];
        File inputFile = new File("matrix_input.txt");

        System.out.println("--- Робота з матрицею (" + n + "x" + n + ") ---");
        
        try (Scanner sc = new Scanner(inputFile)) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (sc.hasNextInt()) {
                        matrix[i][j] = sc.nextInt();
                    } else {
                        matrix[i][j] = 0;
                    }
                }
            }

            System.out.println("Початкова матриця:");
            printMatrix(matrix);

            long[] products = new long[n];
            for (int j = 0; j < n; j++) {
                long prod = 1;
                for (int i = 0; i < n; i++) {
                    prod *= matrix[i][j];
                }
                products[j] = prod;
            }

            int minIdx = 0;
            int maxIdx = 0;
            for (int i = 1; i < n; i++) {
                if (products[i] < products[minIdx]) minIdx = i;
                if (products[i] > products[maxIdx]) maxIdx = i;
            }

            System.out.println("Добутки стовпців: " + Arrays.toString(products));
            System.out.println("Міняємо місцями стовпець " + minIdx + " та " + maxIdx);

            for (int i = 0; i < n; i++) {
                int temp = matrix[i][minIdx];
                matrix[i][minIdx] = matrix[i][maxIdx];
                matrix[i][maxIdx] = temp;
            }

            System.out.println("Результат після перестановки:");
            printMatrix(matrix);
            
            logAction("ToolShop", "Оброблено матрицю. Змінено стовпці " + minIdx + " та " + maxIdx, "message");

        } catch (FileNotFoundException e) {
            System.out.println("Помилка: Файл matrix_input.txt не знайдено!");
            logAction("ToolShop", "Файл матриці не знайдено", "exception");
        }
    }

    private static void printMatrix(int[][] m) {
        for (int[] row : m) {
            for (int val : row) {
                System.out.printf("%4d ", val);
            }
            System.out.println();
        }
    }

    public void addProduct(Product p) { inventory.add(p); }

    public static void main(String[] args) {
        saveSystemInfo();
        
        if (args.length == 0) {
            System.out.println("Аргументи командного рядка відсутні.");
            return;
        }

        switch (args[0]) {
            case "matrix":
                processMatrixTask(3);
                break;
            case "help":
                showLocalizedHelp();
                break;
            case "systems_info":
                printFile("system_info.txt");
                break;
            case "list":
                listLogs();
                break;
            case "serialize":
                performSerialization();
                break;
            case "printlog":
                if (args.length >= 3) {
                    displayLog(args[1], args[2]);
                } else {
                    System.out.println("Формат: printlog [НазваКласу] [0|1]");
                }
                break;
            default:
                System.out.println("Невідома команда. Спробуйте 'matrix' або 'help'.");
        }
    }

    private static void performSerialization() {
        ToolShop shop = new ToolShop();
        shop.addProduct(new Product("Обприскувач Grad"));
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("shop_state.ser"))) {
            oos.writeObject(shop);
            System.out.println("Об'єкт серіалізовано успішно.");
            logAction("ToolShop", "Виконано серіалізацію", "message");
        } catch (IOException e) {
            logAction("ToolShop", "Помилка серіалізації: " + e.getMessage(), "exception");
        }
    }

    private static void listLogs() {
        File dir = new File(".");
        File[] logs = dir.listFiles((d, name) -> name.endsWith(".log"));
        if (logs != null) {
            System.out.println("Знайдені лог-файли:");
            for (File f : logs) System.out.println("- " + f.getName());
        }
    }

    private static void printFile(String fileName) {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) System.out.println(line);
        } catch (IOException e) {
            System.out.println("Файл " + fileName + " не знайдено.");
        }
    }

    private static void displayLog(String className, String reverse) {
        File file = new File(className + ".log");
        if (!file.exists()) {
            System.out.println("Лог для класу " + className + " не знайдено.");
            return;
        }
        try {
            List<String> lines = java.nio.file.Files.readAllLines(file.toPath());
            if ("1".equals(reverse)) Collections.reverse(lines);
            lines.forEach(System.out::println);
        } catch (IOException e) {
            logAction("ToolShop", "Помилка читання логу: " + e.getMessage(), "exception");
        }
    }
}

class Product implements Serializable {
    private String brand;
    public Product(String brand) { 
        this.brand = brand; 
        ToolShop.logAction("Product", "Додано товар: " + brand, "message");
    }
}