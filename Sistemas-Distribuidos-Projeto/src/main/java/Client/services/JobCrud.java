package Client.services;

import org.json.simple.JSONObject;

import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JobCrud {
    private DataConnection dataConnection;

    public JobCrud(DataConnection pDataConnection) {
        this.dataConnection = pDataConnection;
    }

    public boolean createJob(int recruiterId, String skill, String experience) {
        Statement state;
        try {
            state = dataConnection.getConnection().createStatement();
            String query = "INSERT INTO job (recruiter_id, skill, experience) VALUES (" +
                    recruiterId + ",'" + skill + "','" + experience + "')";
            int result = state.executeUpdate(query);

            state.close();
            dataConnection.closeConnection();

            return result >= 1;
        } catch (SQLException e) {
            System.out.println(e.toString());
            return false;
        }
    }

    public Job readJobById(String jobId, int recruiterId) {
        Statement state = null;
        ResultSet result = null;

        try {
            state = dataConnection.getConnection().createStatement();
            String query = "SELECT * FROM job WHERE id = " + jobId + " AND recruiter_id = " + recruiterId;
            result = state.executeQuery(query);

            if (result.next()) {
                Job job = new Job(result.getInt("id"),
                        result.getString("skill"),
                        result.getString("experience"));
                return job;
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

    public List<Job> readJobset(int recruiterId) {
        List<Job> jobset = new ArrayList<>();
        Statement state = null;
        ResultSet result = null;

        try {
            state = dataConnection.getConnection().createStatement();
            String query = "SELECT * FROM job WHERE recruiter_id = " + recruiterId;
            result = state.executeQuery(query);

            while (result.next()) {
                Job job = new Job(
                        result.getInt("id"),
                        result.getString("skill"),
                        result.getString("experience")
                );
                jobset.add(job);
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

        return jobset;
    }


    public boolean updateJob(String jobId, int recruiterId, String skill, String experience) {
        Statement state;
        try {
            state = dataConnection.getConnection().createStatement();
            String query = "UPDATE job SET skill = '" + skill + "', experience = '" + experience + "' WHERE id = " + jobId + " AND recruiter_id = " + recruiterId;
            int result = state.executeUpdate(query);

            state.close();
            dataConnection.closeConnection();

            return result >= 1;
        } catch (SQLException e) {
            System.out.println(e.toString());
            return false;
        }
    }

    public boolean deleteJob(String jobId) {
        Statement state;

        try {
            state = dataConnection.getConnection().createStatement();
            String query = "DELETE FROM job WHERE id = '" + jobId + "'";

            int result = state.executeUpdate(query);

            if (result >= 1) {
                System.out.println("Job deleted successfully!");
                return true;
            } else {
                System.out.println("Failed to delete job!");
            }

            state.close();
            dataConnection.closeConnection();
        } catch (SQLException e) {
            System.out.println(e.toString());
        }
        return false;
    }

//    public List<Job> searchJob(List<String> skills, String experience, String filter) {
//        List<Job> jobset = new ArrayList<>();
//        Statement state = null;
//        ResultSet result = null;
//
//        try {
//            state = dataConnection.getConnection().createStatement();
//            StringBuilder queryBuilder = new StringBuilder("SELECT * FROM job WHERE experience <= ");
//            queryBuilder.append(experience).append(" AND (");
//
//            for (int i = 0; i < skills.size(); i++) {
//                queryBuilder.append("skill = '").append(skills.get(i)).append("'");
//                if (i < skills.size() - 1) {
//                    queryBuilder.append(" ").append(filter).append(" ");
//                }
//            }
//            queryBuilder.append(")");
//
//            String query = queryBuilder.toString();
//            result = state.executeQuery(query);
//
//            while (result.next()) {
//                Job job = new Job(
//                        result.getInt("id"),
//                        result.getString("skill"),
//                        result.getString("experience")
//                );
//                jobset.add(job);
//            }
//        } catch (SQLException e) {
//            System.out.println(e.toString());
//        } finally {
//            try {
//                if (result != null) {
//                    result.close();
//                }
//                if (state != null) {
//                    state.close();
//                }
//                dataConnection.closeConnection();
//            } catch (SQLException ex) {
//                System.out.println(ex.toString());
//            }
//        }
//
//        return jobset;
//    }

    public List<Job> searchJob(List<String> skills, String experience, String filter) {
        List<Job> jobset = new ArrayList<>();
        Statement state = null;
        ResultSet result = null;

        try {
            state = dataConnection.getConnection().createStatement();
            StringBuilder queryBuilder = new StringBuilder("SELECT * FROM job WHERE experience <= ");
            queryBuilder.append(experience).append(" AND (");

            for (int i = 0; i < skills.size(); i++) {
                queryBuilder.append("skill = '").append(skills.get(i)).append("'");
                if (i < skills.size() - 1) {
                    queryBuilder.append(" OR "); // Alterado de filter para OR
                }
            }
            queryBuilder.append(")");

            String query = queryBuilder.toString();
            result = state.executeQuery(query);

            while (result.next()) {
                Job job = new Job(
                        result.getInt("id"),
                        result.getString("skill"),
                        result.getString("experience")
                );
                jobset.add(job);
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

        return jobset;
    }

    public void sendResponse(PrintWriter writer, String status, JSONObject data, String operation) {
        JSONObject response = new JSONObject();
        response.put("operation", operation);
        response.put("status", status);
        response.put("data", data);
        writer.println(response.toJSONString());

        System.out.println("Response: " + response.toJSONString());
    }

    public List<Job> readJobExperienceOR(String experience, String skill) {
        List<Job> jobset = new ArrayList<>();
        Statement state = null;
        ResultSet result = null;

        try {
            state = dataConnection.getConnection().createStatement();
            String query = "SELECT * FROM job WHERE experience = '" + experience + "' OR skill = '" + skill + "'";
            result = state.executeQuery(query);

            while (result.next()) {
                Job job = new Job(
                        result.getInt("id"),
                        result.getString("skill"),
                        result.getString("experience")
                );
                jobset.add(job);
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

        return jobset; // Isso sempre retornará uma lista, que pode estar vazia, mas nunca será null.
    }

    public List<Job> readJobExperienceAND(String experience, String skill) {
        List<Job> jobset = new ArrayList<>();
        Statement state = null;
        ResultSet result = null;

        try {
            state = dataConnection.getConnection().createStatement();
            String query = "SELECT * FROM job WHERE experience = '" + experience + "' AND skill = '" + skill + "'";
            result = state.executeQuery(query);

            while (result.next()) {
                Job job = new Job(
                        result.getInt("id"),
                        result.getString("skill"),
                        result.getString("experience")
                );
                jobset.add(job);
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

        return jobset;
    }

}
