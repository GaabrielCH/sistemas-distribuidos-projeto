package Client.services;

import java.io.*;
import java.net.*;
import java.util.Scanner;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import io.jsonwebtoken.*;

public class EchoServer2c extends Thread {
    protected static boolean serverContinue = true;
    protected Socket clientSocket;

    DataConnection dataConnection = new DataConnection();
    ClientCrud clientCrud = new ClientCrud(dataConnection);

    private static final String TOKEN_KEY = "DISTRIBUIDOS";
    private static final Algorithm alg = Algorithm.HMAC256(TOKEN_KEY);

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(24002)) {
            System.out.println("Connection Socket Created");
            while (serverContinue) {
                serverSocket.setSoTimeout(10000);
                System.out.println("Waiting for Connection");
                try {
                    Socket clientSocket = serverSocket.accept();
                    new EchoServer2c(clientSocket).start();
                } catch (SocketTimeoutException ste) {
                    System.out.println("Timeout Occurred");
                }
            }
        } catch (IOException e) {
            System.err.println("Could not listen on port: 24002.");
            System.exit(1);
        }
    }

    public EchoServer2c(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public void run() {
        System.out.println("New Communication Thread Started");

        try (ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
             ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream())) {

            JSONParser parser = new JSONParser();

            while (true) {
                String jsonString = (String) in.readObject();

                // Mostrar o JSON recebido
                System.out.println("Request: " + jsonString);

                System.out.println("Response: " + out.toString());

                JSONObject request = (JSONObject) parser.parse(jsonString);

                String token = (String) request.get("token");

                String operation = (String) request.get("operation");
                JSONObject requestData = (JSONObject) request.get("data");

                if (!verifyToken(token)) {
                    sendResponse(out, "INVALID_TOKEN", null, operation);
                    continue;
                }

                switch (operation) {
                    case "SIGNUP_CANDIDATE":
                        // Adicionar um candidato
                        String name = (String) requestData.get("name");
                        String email = (String) requestData.get("email");
                        String password = (String) requestData.get("password");

                        // Check if the user already exists
                        if (clientCrud.readClient(email) != null) {
                            sendResponse(out, "USER_EXIST", null , "SIGNUP_CANDIDATE");
                        } else {
                            if (clientCrud.addClient(new Client(0, name, email, password))) {
                                sendResponse(out, "SUCCESS", null, "SIGNUP_CANDIDATE");
                            }else{
                                sendResponse(out, "SUCCESS", null, "SIGNUP_CANDIDATE");
                            }

                        }

                        break;
                    case "LOGIN_CANDIDATE":
                        // Login
                        email = (String) requestData.get("email");
                        password = (String) requestData.get("password");
                        if (clientCrud.login(email, password)) {
                            sendResponseLogin(out, "SUCCESS", null, "LOGIN_CANDIDATE", generateToken(clientCrud.getLoggedClient().getId(), "candidate"));
                        } else {
                            sendResponse(out, "INVALID_LOGIN", null, "LOGIN_CANDIDATE");
                        }
                        break;
                    case "LOOKUP_ACCOUNT_CANDIDATE":
                        // Obter todos os clientes
                        JSONObject clientsData = (JSONObject) clientCrud.getAllClients();
                        sendResponse(out, "SUCCESS", clientsData, "LOOKUP_ACCOUNT_CANDIDATE");
                        break;
                    case "UPDATE_ACCOUNT_CANDIDATE":
                        // Atualizar um cliente
                        Object idObj = requestData.get("id");
                        if (idObj instanceof Long) {
                            long idLong = (Long) idObj;
                            int id = (int) idLong;
                            name = (String) requestData.get("name");
                            email = (String) requestData.get("email");
                            password = (String) requestData.get("password");

                            Client existingClient = clientCrud.readClient(email);
                            if (existingClient != null && existingClient.getId() != id) {
                                sendResponse(out, "INVALID_EMAIL", null, "UPDATE_ACCOUNT_CANDIDATE");
                            } else {
                                if (clientCrud.update(id, name, email, password)) {
                                    sendResponse(out, "SUCCESS", null, "UPDATE_ACCOUNT_CANDIDATE");
                                } else {
                                    sendResponse(out, "SUCCESS", null, "UPDATE_ACCOUNT_CANDIDATE");
                                }
                            }
                        }
                        break;
                    case "DELETE_ACCOUNT_CANDIDATE":
                        // Excluir um cliente
                        String idStr = String.valueOf(requestData.get("id"));
                        int id = Integer.parseInt(idStr);
                        Client client = clientCrud.readClientID(id);
                        clientCrud.readClientID(id);
                        clientCrud.delete(id);
                        if(client == null) {
                            sendResponse(out, "INVALID_FIELD", null, "DELETE_ACCOUNT_CANDIDATE");
                        }else{
                            sendResponse(out, "SUCCESS", null, "DELETE_ACCOUNT_CANDIDATE");
                        }
                        //sendResponse(out, "SUCCESS", null, "DELETE_ACCOUNT_CANDIDATE");
                        break;
                    case "LOGOUT":
                        // Logout
                        if (clientCrud.getLoggedClient() == null) {
                            sendResponse(out, "INVALID_FIELD", null, "LOGOUT");
                        } else {
                            clientCrud.logout();
                            sendResponse(out, "SUCCESS", null, "LOGOUT");
                        }
                        break;
                    default:
                        sendResponse(out, "INVALID_OPERATION", null, "NAO_EXISTE");
                        continue;
                }

                if (jsonString.equals("Bye.")) {
                    break;
                }

                if (jsonString.equals("End Server.")) {
                    serverContinue = false;
                }
            }
        } catch (IOException | ClassNotFoundException | ParseException e) {
            System.err.println("Problem with Communication Server");
            e.printStackTrace();
        }
    }

    private void sendResponse(ObjectOutputStream out, String status, JSONObject data, String operation) throws IOException {
        JSONObject response = new JSONObject();
        response.put("operation", operation);
        response.put("status", status);
        response.put("data", data);
        //response.put("data", data != null ? data : "");
        out.writeObject(response.toJSONString());
        out.flush();

        System.out.println("Response: " + response.toJSONString());
    }
    private void sendResponseLogin(ObjectOutputStream out, String status, JSONObject data, String operation, String token) throws IOException {
        JSONObject response = new JSONObject();
        response.put("operation", operation);
        response.put("status", status);
        response.put("data", data);
        //response.put("data", data != null ? data : "");
        response.put("token", token);
        out.writeObject(response.toJSONString());
        out.flush();

        System.out.println("Response: " + response.toJSONString());
    }

    private boolean verifyToken(String token) {
        try {
            JWT.require(alg).build().verify(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static String generateToken(int id, String role) {
        return JWT.create().withClaim("id", String.valueOf(id)).withClaim("role", role).sign(alg);
    }
}
