package Client.services;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class RecruiterCrud {
    private static DataConnection dataConnection;
    private Recruiter loggedIn;

    public RecruiterCrud(DataConnection pDataConnection) {
        dataConnection = new DataConnection();
    }

    public boolean addRecruiter(Recruiter recruiter) {
        Statement state;

        try {
            state = dataConnection.getConnection().createStatement();
            String query = "INSERT INTO recruiter (name, email, password, industry, description) VALUES ('" +
                    recruiter.getName() + "','" +
                    recruiter.getEmail() + "','" +
                    recruiter.getPassword() + "','" +
                    recruiter.getIndustry() + "','" +
                    recruiter.getDescription() + "')";

            int result = state.executeUpdate(query);

            if (result >= 1) {
                System.out.println("Recruiter added successfully!");
            } else {
                System.out.println("Failed to add recruiter!");
            }

            state.close();
            dataConnection.closeConnection();
        } catch (SQLException e) {
            System.out.println(e.toString());
        }
        return false;
    }

    public Recruiter readRecruiter(String email) {
        Statement state;
        ResultSet result;

        try {
            state = dataConnection.getConnection().createStatement();
            String query = "SELECT * FROM recruiter WHERE email = '" + email + "'";
            result = state.executeQuery(query);

            if (result.next()) {
                Recruiter recruiter = new Recruiter(result.getInt("id"),
                        result.getString("name"),
                        result.getString("email"),
                        result.getString("password"),
                        result.getString("industry"),
                        result.getString("description"));
                return recruiter;
            }

            result.close();
            state.close();
            dataConnection.closeConnection();
        } catch (SQLException e) {
            System.out.println(e.toString());
        }
        return null;
    }

    public Recruiter getRecruiterByID(int id) {
        return readRecruiterID(id);
    }

    public static Recruiter getRecruiterByEmail(String email) {
        Statement state;
        ResultSet result;

        try {
            state = dataConnection.getConnection().createStatement();
            String query = "SELECT * FROM recruiter WHERE email = '" + email + "'";
            result = state.executeQuery(query);

            if (result.next()) {
                Recruiter recruiter = new Recruiter(result.getInt("id"),
                        result.getString("name"),
                        result.getString("email"),
                        result.getString("password"),
                        result.getString("industry"),
                        result.getString("description"));
                return recruiter;
            }

            result.close();
            state.close();
        } catch (SQLException e) {
            System.out.println(e.toString());
        }

        return null;
    }

    public static Object getRecruiterById(){
        Statement state;
        ResultSet result;

        try {
            state = dataConnection.getConnection().createStatement();
            String query = "SELECT * FROM recruiter WHERE id = '" + "'";
            result = state.executeQuery(query);

            if (result.next()) {
                Recruiter recruiter = new Recruiter(result.getInt("id"),
                        result.getString("name"),
                        result.getString("email"),
                        result.getString("password"),
                        result.getString("industry"),
                        result.getString("description"));
                return recruiter;
            }

            result.close();
            state.close();
            dataConnection.closeConnection();
        } catch (SQLException e) {
            System.out.println(e.toString());
        }
        return null;
    }


    public Recruiter readRecruiterID(int id) {
        Statement state;
        ResultSet result;

        try {
            state = dataConnection.getConnection().createStatement();
            String query = "SELECT * FROM recruiter WHERE id = '" + id + "'";
            result = state.executeQuery(query);

            if (result.next()) {
                Recruiter recruiter = new Recruiter(result.getInt("id"),
                        result.getString("name"),
                        result.getString("email"),
                        result.getString("password"),
                        result.getString("industry"),
                        result.getString("description"));
                return recruiter;
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
            String query = "SELECT * FROM recruiter WHERE email = '" + email + "' AND password = '" + password + "'";
            result = state.executeQuery(query);

            if (result.next()) {
                System.out.println("Login successful!");
                loggedIn = new Recruiter(result.getInt("id"),
                        result.getString("name"),
                        result.getString("email"),
                        result.getString("password"),
                        result.getString("industry"),
                        result.getString("description")); // Set loggedRecruiter here
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
            String query = "DELETE FROM recruiter WHERE id = " + id;

            int result = state.executeUpdate(query);

            if (result >= 1) {
                System.out.println("Recruiter deleted successfully!");
            } else {
                System.out.println("Failed to delete recruiter!");
            }

            state.close();
            dataConnection.closeConnection();
        } catch (SQLException e) {
            System.out.println(e.toString());
        }
        return false;
    }

    public boolean update(int id, String name, String email, String password, String industry, String description) {
        Statement state;

        try {
            state = dataConnection.getConnection().createStatement();
            String query = "UPDATE recruiter SET name = '" + name + "', email = '" + email + "', password = '" + password + "', industry = '" + industry + "', description = '" + description + "' WHERE id = " + id;

            int result = state.executeUpdate(query);

            if (result >= 1) {
                System.out.println("Recruiter updated successfully!");
            } else {
                System.out.println("Failed to update recruiter!");
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

    public Recruiter getLoggedRecruiter() {
        return loggedIn;
    }
}