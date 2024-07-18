package Client.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.json.simple.JSONObject;

import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SkillCrud {
    private DataConnection dataConnection;

    public SkillCrud(DataConnection pDataConnection) {
        this.dataConnection = pDataConnection;
    }

    public boolean createSkill(int candidateId, String skill, String experience) {
        Statement state;
        try {
            state = dataConnection.getConnection().createStatement();
            String query = "INSERT INTO skill (client_id, skill, experience) VALUES (" +
                    candidateId + ",'" + skill + "','" + experience + "')";
            int result = state.executeUpdate(query);

            state.close();
            dataConnection.closeConnection();

            return result >= 1;
        } catch (SQLException e) {
            System.out.println(e.toString());
            return false;
        }
    }

    public Skill readSkills(int id) {
        Statement state;
        ResultSet result;

        try {
            state = dataConnection.getConnection().createStatement();
            String query = "SELECT * FROM skill WHERE client_id = '" + id + "'";
            result = state.executeQuery(query);

            if (result.next()) {
                Skill skill = new Skill (result.getInt("id"),
                        result.getString("skill"),
                        result.getString("experience"));
                return skill;
            }
            result.close();
            state.close();
            dataConnection.closeConnection();
        } catch (SQLException e) {
            System.out.println(e.toString());
        }
        return null;
    }

    public Skill readSkillsD(String name) {
        Statement state;
        ResultSet result;

        try {
            state = dataConnection.getConnection().createStatement();
            String query = "SELECT * FROM skill WHERE skill = '" + name + "'";
            result = state.executeQuery(query);

            if (result.next()) {
                Skill skill = new Skill (result.getInt("id"),
                        result.getString("skill"),
                        result.getString("experience"));
                return skill;
            }
            result.close();
            state.close();
            dataConnection.closeConnection();
        } catch (SQLException e) {
            System.out.println(e.toString());
        }
        return null;
    }



    public boolean updateSkill(int Id, String skillName, String experience) {
        Statement state;

        try {
            state = dataConnection.getConnection().createStatement();
            String query = "UPDATE skill SET skill = '" + skillName + "', experience = '" + experience + "' WHERE client_id = " + Id;

            int result = state.executeUpdate(query);

            state.close();
            dataConnection.closeConnection();

            return result >= 1;
        } catch (SQLException e) {
            System.out.println(e.toString());
            return false;
        }
    }

    public boolean deleteSkill(String skillName) {
        Statement state;

        try {
            state = dataConnection.getConnection().createStatement();
            String query = "DELETE FROM skill WHERE skill = '" + skillName + "'";

            int result = state.executeUpdate(query);

            if (result >= 1) {
                System.out.println("Skill deleted successfully!");
            } else {
                System.out.println("Failed to delete skill!");
            }

            state.close();
            dataConnection.closeConnection();
        } catch (SQLException e) {
            System.out.println(e.toString());
        }
        return false;
    }

    public Skill readSkillById(String skillId, int candidateId) {
        Statement state = null;
        ResultSet result = null;

        try {
            state = dataConnection.getConnection().createStatement();
            String query = "SELECT * FROM skill WHERE id = " + skillId + " AND client_id = " + candidateId;
            result = state.executeQuery(query);

            if (result.next()) {
                Skill skill = new Skill(result.getInt("id"),
                        result.getString("skill"),
                        result.getString("experience"));
                return skill;
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
                dataConnection.closeConnection();
            } catch (SQLException ex) {
                System.out.println(ex.toString());
            }
        }
        return null;
    }

    public Skill readSkillString(String skill, int candidateId) {
        Statement state = null;
        ResultSet result = null;

        try {
            state = dataConnection.getConnection().createStatement();
            String query = "SELECT * FROM skill WHERE skill = '" + skill + "' AND client_id = " + candidateId;
            result = state.executeQuery(query);

            if (result.next()) {
                Skill skill1 = new Skill(result.getInt("id"),
                        result.getString("skill"),
                        result.getString("experience"));
                return skill1;
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
                dataConnection.closeConnection();
            } catch (SQLException ex) {
                System.out.println(ex.toString());
            }
        }
        return null;
    }

    public List<Skill> readSkillset(int clientId) {
        List<Skill> skillset = new ArrayList<>();
        Statement state = null;
        ResultSet result = null;

        try {
            state = dataConnection.getConnection().createStatement();
            String query = "SELECT * FROM skill WHERE client_id = " + clientId;
            result = state.executeQuery(query);

            while (result.next()) {
                Skill skill = new Skill(
                        result.getInt("id"),
                        result.getString("skill"),
                        result.getString("experience")
                );
                skillset.add(skill);
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
                dataConnection.closeConnection();
            } catch (SQLException ex) {
                System.out.println(ex.toString());
            }
        }

        return skillset;
    }

    public void sendResponse(PrintWriter writer, String status, JSONObject data, String operation) {
        JSONObject response = new JSONObject();
        response.put("operation", operation);
        response.put("status", status);
        response.put("data", data);
        writer.println(response.toJSONString());

        System.out.println("Response: " + response.toJSONString());
    }


}

