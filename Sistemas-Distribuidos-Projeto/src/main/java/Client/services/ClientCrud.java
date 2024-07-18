package Client.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.json.simple.JSONObject;

import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ClientCrud {
    DataConnection dataConnection;

    Client loggedIn;

    public ClientCrud(DataConnection pDataConnection) {
        this.dataConnection = pDataConnection;
        this.loggedIn = null;
    }

    public Object getAllClients() {
        Statement state = null;
        ResultSet result = null;

        try {
            state = dataConnection.getConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            String query = "SELECT * FROM client";

            result = state.executeQuery(query);

            if (!result.isBeforeFirst()) {
                System.out.println("Data is empty!");
            } else {
                while (result.next()) {
                    Client client = new Client(result.getInt("id"),
                            result.getString("name"),
                            result.getString("email"),
                            result.getString("password"));

                    System.out.println(client.toString());
                }
            }
        } catch (SQLException e) {
            System.out.println(e.toString());
        } finally {
            try {
                if (result != null) {
                    result.close();
                }
                if (state != null) {
                    state.close();
                }
                // Note that we don't need to close the connection here
                // It's managed by the DataConnection class
            } catch (SQLException ex) {
                System.out.println(ex.toString());
            }
        }
        return null;
    }

    public Client getClient(String token) {
        DecodedJWT jwt = JWT.decode(token);

        if (jwt == null || jwt.getClaim("id") == null || jwt.getClaim("id").asString() == null) {
            // Token inválido ou ausente do ID do cliente
            return null;
        }

        String idStr = jwt.getClaim("id").asString();

        // Converta o ID do cliente para inteiro
        int id;
        try {
            id = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            // Formato inválido do ID
            return null;
        }

        // Busque o cliente pelo ID
        return readClientID(id);
    }
    public boolean addClient(Client client) {
        Statement state;

        try {
            state = dataConnection.getConnection().createStatement();
            String query = "INSERT INTO client (name, email, password) VALUES ('" +
                    client.getName() + "','" +
                    client.getEmail() + "','" +
                    client.getPassword() + "')";

            int result = state.executeUpdate(query);

            if (result >= 1) {
                System.out.println("Client added successfully!");
            } else {
                System.out.println("Failed to add client!");
            }


            state.close();
            dataConnection.closeConnection();
        } catch (SQLException e) {
            System.out.println(e.toString());
        }
        return false;
    }

    public Client readClient(String email) {
        Statement state;
        ResultSet result;

        try {
            state = dataConnection.getConnection().createStatement();
            String query = "SELECT * FROM client WHERE email = '" + email + "'";
            result = state.executeQuery(query);

            if (result.next()) {
                Client client = new Client(result.getInt("id"),
                        result.getString("name"),
                        result.getString("email"),
                        result.getString("password"));
                return client;
            }

            result.close();
            state.close();
            dataConnection.closeConnection();
        } catch (SQLException e) {
            System.out.println(e.toString());
        }
        return null;
    }

    public Client getClientByID(int id) {
        return readClientID(id);
    }

    public Client getClientByEmail(String email) {
        Statement state;
        ResultSet result;

        try {
            state = dataConnection.getConnection().createStatement();
            String query = "SELECT * FROM client WHERE email = '" + email + "'";
            result = state.executeQuery(query);

            if (result.next()) {
                Client client = new Client(result.getInt("id"),
                        result.getString("name"),
                        result.getString("email"),
                        result.getString("password"));
                return client;
            }

            result.close();
            state.close();
        } catch (SQLException e) {
            System.out.println(e.toString());
        }

        return null;
    }

    private void sendResponse(PrintWriter writer, String status, JSONObject data, String operation) {
        JSONObject response = new JSONObject();
        response.put("operation", operation);
        response.put("status", status);
        response.put("data", data);
        writer.println(response.toJSONString());

        System.out.println("Response: " + response.toJSONString());
    }


    public Client readClientID(int id) {
        Statement state;
        ResultSet result;

        try {
            state = dataConnection.getConnection().createStatement();
            String query = "SELECT * FROM client WHERE id = '" + id + "'";
            result = state.executeQuery(query);

            if (result.next()) {
                Client client = new Client(result.getInt("id"),
                        result.getString("name"),
                        result.getString("email"),
                        result.getString("password"));
                return client;
            }

            result.close();
            state.close();
            dataConnection.closeConnection();
        } catch (SQLException e) {
            System.out.println(e.toString());
        }
        return null;
    }

    public boolean login(String email, String password) {
        Statement state;
        ResultSet result;

        try {
            state = dataConnection.getConnection().createStatement();
            String query = "SELECT * FROM client WHERE email = '" + email + "' AND password = '" + password + "'";
            result = state.executeQuery(query);

            if (result.next()) {
                System.out.println("Login successful!");
                loggedIn = new Client(result.getInt("id"),
                        result.getString("name"),
                        result.getString("email"),
                        result.getString("password")); // Set loggedClient here
            } else {
                System.out.println("Login failed. Invalid email or password.");
                return false;
            }

            result.close();
            state.close();
            dataConnection.closeConnection();
        } catch (SQLException e) {
            System.out.println(e.toString());
        }
        return true;
    }

    public boolean delete(int id) {
        Statement state;

        try {
            state = dataConnection.getConnection().createStatement();
            String query = "DELETE FROM client WHERE id = " + id;

            int result = state.executeUpdate(query);

            if (result >= 1) {
                System.out.println("Client deleted successfully!");
            } else {
                System.out.println("Failed to delete client!");
            }

            state.close();
            dataConnection.closeConnection();
        } catch (SQLException e) {
            System.out.println(e.toString());
        }
        return false;
    }

    public boolean update(int id, String name, String email, String password) {
        Statement state;

        try {
            state = dataConnection.getConnection().createStatement();
            String query = "UPDATE client SET name = '" + name + "', email = '" + email + "', password = '" + password + "' WHERE id = " + id;

            int result = state.executeUpdate(query);

            if (result >= 1) {
                System.out.println("Client updated successfully!");
            } else {
                System.out.println("Failed to update client!");
            }

            state.close();
            dataConnection.closeConnection();
        } catch (SQLException e) {
            System.out.println(e.toString());
        }
        return false;
    }

    public void logout() {
        loggedIn = null;
        System.out.println("Logged out successfully!");
    }

    public Client getLoggedClient() {
        return loggedIn;
    }
}