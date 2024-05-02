package Client.services;

import java.io.*;
import java.net.*;
import java.util.Scanner;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.json.simple.JSONObject;

public class EchoClient2 {
    private static final String TOKEN_KEY = "DISTRIBUIDOS";
    private static final Algorithm alg = Algorithm.HMAC256(TOKEN_KEY);

    public static void main(String[] args) {
        String serverHostname = "192.168.1.17";

        int operationID = 0;

        try (Socket echoSocket = new Socket(serverHostname, 24002);
             ObjectOutputStream outputStream = new ObjectOutputStream(echoSocket.getOutputStream());
             ObjectInputStream inputStream = new ObjectInputStream(echoSocket.getInputStream());
             Scanner scanner = new Scanner(System.in)) {

            while (true) {
                System.out.println("\nChoose an operation:");
                System.out.println("1. Get all clients (LOOKUP_ACCOUNT_CANDIDATE)");
                System.out.println("2. Add a client (SIGNUP_CANDIDATE)");
                System.out.println("3. Login (LOGIN_CANDIDATE)");
                System.out.println("4. Delete a client (DELETE_ACCOUNT_CANDIDATE)");
                System.out.println("5. Update a client (UPDATE_ACCOUNT_CANDIDATE)");
                System.out.println("6. Logout (LOGOUT)");

                String choice = scanner.nextLine();

                JSONObject request = new JSONObject();
                request.put("operation", choice);
                JSONObject requestData = new JSONObject();

                switch (choice) {
                    case "SIGNUP_CANDIDATE":
                        operationID++;
                        System.out.println("Enter client name:");
                        requestData.put("name", scanner.nextLine());
                        System.out.println("Enter client email:");
                        requestData.put("email", scanner.nextLine());
                        System.out.println("Enter client password:");
                        requestData.put("password", scanner.nextLine());
                        break;

//                    case "SIGNUP_ACCOUNT_CANDIDATE":
//                        operationID++;
//                        System.out.println("Enter client name:");
//                        String name = scanner.nextLine();
//                        System.out.println("Enter client email:");
//                        String email = scanner.nextLine();
//                        System.out.println("Enter client password:");
//                        String password = scanner.nextLine();
//
//                        requestData.put("name", name);
//                        requestData.put("email", email);
//                        requestData.put("password", password);
//
//                        String token = generateToken(operationID, "CANDIDATE");
//
//                        request.put("operation", "SIGNUP_ACCOUNT_CANDIDATE");
//                        request.put("data", requestData);
//                        request.put("token", token);
//
//                        outputStream.writeObject(request.toJSONString());
//                        outputStream.flush();
//                        break;
                    case "LOGIN_CANDIDATE":
                        operationID++;
                        System.out.println("Enter email:");
                        requestData.put("email", scanner.nextLine());
                        System.out.println("Enter password:");
                        requestData.put("password", scanner.nextLine());
                        break;
                    case "LOOKUP_ACCOUNT_CANDIDATE":
                        operationID++;
                        break;
                    case "LOGOUT":
                        operationID++;
                        break;
                    case "DELETE_ACCOUNT_CANDIDATE":
                        operationID++;
                        System.out.println("Enter client ID:");
                        requestData.put("id", scanner.nextInt());
                        break;
                    case "UPDATE_ACCOUNT_CANDIDATE":
                        operationID++;
                        System.out.println("Enter client ID:");
                        requestData.put("id", scanner.nextInt());
                        scanner.nextLine(); // Consume newline character
                        System.out.println("Enter updated name:");
                        requestData.put("name", scanner.nextLine());
                        System.out.println("Enter updated email:");
                        requestData.put("email", scanner.nextLine());
                        System.out.println("Enter updated password:");
                        requestData.put("password", scanner.nextLine());
                        break;
                    default:
                        operationID++;
                        request.put("operation", "INVALID_CHOICE");
                        request.put("data", requestData);
                        outputStream.writeObject(request.toJSONString());
                        outputStream.flush();

                        Object response = inputStream.readObject();
                        System.out.println(response);
                        continue;
                }

               // String token = generateToken((Integer) request.get("id"), "CANDIDATE");
                String token = generateToken(operationID, "CANDIDATE"); // Supondo que o ID e o papel do usuário sejam conhecidos
                request.put("token", token);
                request.put("data", requestData);

                outputStream.writeObject(request.toJSONString());
                outputStream.flush();

                // Esperar pela resposta do servidor
                Object response = inputStream.readObject();
                System.out.println(response);
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static String generateToken(int id, String role) {
        return JWT.create().withClaim("id", String.valueOf(id)).withClaim("role", role).sign(alg);
    }
}
