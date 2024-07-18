package Client.services;

import java.io.*;
import java.net.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import io.jsonwebtoken.*;

public class EchoServer2c extends Thread {
    protected static boolean serverContinue = true;
    protected Socket clientSocket;

    DataConnection dataConnection = new DataConnection();

    Connection Connection = dataConnection.getConnection();
    ClientCrud clientCrud = new ClientCrud(dataConnection);

    RecruiterCrud recruiterCrud = new RecruiterCrud(dataConnection);

    JobCrud jobCrud = new JobCrud(dataConnection);

    SkillCrud skillCrud = new SkillCrud(dataConnection);

    private static final String TOKEN_KEY = "DISTRIBUIDOS";
    private static final Algorithm alg = Algorithm.HMAC256(TOKEN_KEY);

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(24001)) {
            System.out.println("Connection Socket Created");
            while (serverContinue) {
                serverSocket.setSoTimeout(10000);
                System.out.println("Waiting for Connection");
                try {
                    Socket clientSocket = serverSocket.accept();
                    new EchoServer2c(clientSocket).start();
                } catch (SocketTimeoutException ste) {
                    System.out.println("Timeout Occurred");
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (IOException e) {
            System.err.println("Could not listen on port: 24002.");
            System.exit(1);
        }
    }

    public EchoServer2c(Socket clientSocket) throws SQLException {
        this.clientSocket = clientSocket;
    }

    public void run() {
        System.out.println("New Communication Thread Started");

        try (InputStream input = clientSocket.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(input));
             OutputStream output = clientSocket.getOutputStream();
             PrintWriter writer = new PrintWriter(output, true)) {

            JSONParser parser = new JSONParser();

            Client loggedInClient = null;

            Recruiter loggedInRecruiter = null;

            while (true) {

                /* ler json completo se necessario
                    StringBuilder jsonString = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null && !line.isEmpty()) {
                        jsonString.append(line);
                    }

                    JSONObject request = (JSONObject) parser.parse(jsonString.toString());
                 */

                String jsonString = reader.readLine();

                if (jsonString == null) {
                    break;
                }

                System.out.println("Request: " + jsonString);

                Request request = new Request(
                        (String) ((JSONObject) parser.parse(jsonString)).get("operation"),
                        (JSONObject) ((JSONObject) parser.parse(jsonString)).get("data"),
                        (String) ((JSONObject) parser.parse(jsonString)).get("token")
                );

                String token = request.getToken();
                String operation = request.getOperation();
                JSONObject requestData = request.getData();


                JSONObject request1 = (JSONObject) parser.parse(jsonString);

//                String token = (String) request.get("token");
//
//                String operation = (String) request.get("operation");
//
//                JSONObject requestData = (JSONObject) request.get("data");

//                if (!operation.equals("LOGIN_CANDIDATE") && !operation.equals("LOGIN_RECRUITER") && !operation.equals("SIGNUP_RECRUITER") && !operation.equals("SIGNUP_CANDIDATE") && !verifyToken(token)) {
//                    sendResponse(writer, "INVALID_TOKEN", null, operation);
//                    continue;
//                }

                //     verifyToken(token);

                switch (operation) {
                    case "SIGNUP_CANDIDATE":
                        // Adicionar um candidato
                        String name = (String) requestData.get("name");
                        String email = (String) requestData.get("email");
                        String password = (String) requestData.get("password");

                        JSONObject clientDataS = new JSONObject();
                        clientDataS.put("name", name);
                        clientDataS.put("email", email);
                        clientDataS.put("password", password);

                        if (name == null || email == null || password == null) {
                            sendResponse(writer, "INVALID_FIELD", clientDataS, "SIGNUP_CANDIDATE");
                            continue;
                        }

                        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                            sendResponse(writer, "INVALID_FIELD", clientDataS, "SIGNUP_CANDIDATE");
                            continue;
                        }

                        // Checar se ja existe o candidato
                        if (clientCrud.readClient(email) != null) {
                            sendResponse(writer, "USER_EXIST", clientDataS, "SIGNUP_CANDIDATE");
                        } else {
                            if (clientCrud.addClient(new Client(0, name, email, password))) {
                                sendResponse(writer, "SUCCESS", clientDataS, "SIGNUP_CANDIDATE");
                            } else {
                                sendResponse(writer, "SUCCESS", clientDataS, "SIGNUP_CANDIDATE");
                            }
                        }

                        break;
                    case "LOGIN_CANDIDATE":
                        // Login
                        String email4 = (String) requestData.get("email");
                        String password4 = (String) requestData.get("password");

                        if (loggedInClient != null) {
                            System.out.println("Already logged.");
                            continue;
                        }

                        JSONObject clientDataL = new JSONObject();
                        clientDataL.put("email", email4);
                        clientDataL.put("password", password4);

                        if (email4 == null || password4 == null) {
                            sendResponse(writer, "INVALID_FIELD", clientDataL, "LOGIN_CANDIDATE");
                            continue;
                        }

                        if(email4.isEmpty() || password4.isEmpty()){
                            sendResponse(writer, "INVALID_FIELD", clientDataL, "LOGIN_CANDIDATE");
                            continue;
                        }

                        Client client4 = clientCrud.getClientByEmail(email4);
                        loggedInClient = client4;
                        if (client4 != null && client4.getPassword().equals(password4)) {
                            String token2 = generateToken(client4.getId(), "CANDIDATE");
                            sendResponseLogin(writer, "SUCCESS", clientDataL, "LOGIN_CANDIDATE", token2);
                        } else {
                            sendResponse(writer, "INVALID_LOGIN", clientDataL, "LOGIN_CANDIDATE");
                        }
                        break;

                    case "LOOKUP_ACCOUNT_CANDIDATE":

                        if (loggedInClient == null) {
                            System.out.println("Login first.");
                            continue;
                        }else {
                            if (token == null) {
                                sendResponse(writer, "INVALID_TOKEN", null, "LOOKUP_ACCOUNT_CANDIDATE");
                                continue;
                            }

                            DecodedJWT jwt;
                            try {
                                jwt = JWT.decode(token);
                            } catch (JWTDecodeException e) {
                                sendResponse(writer, "INVALID_TOKEN", null, "LOOKUP_ACCOUNT_CANDIDATE");
                                continue;
                            }

                            int id;

                            String idStr = jwt.getClaim("id").asString();
                            String role = jwt.getClaim("role").asString();
                            id = Integer.parseInt(idStr);

                            Client client = clientCrud.getClientByID(id);
                            JSONObject clientData = new JSONObject();
                            //   clientData.put("id", client.getId());
                            clientData.put("name", client.getName());
                            clientData.put("email", client.getEmail());
                            clientData.put("password", client.getPassword());

                            // String token5 = (String) requestData.get("token");

                            // pode ser comentado
                            if (idStr == null || role == null || !role.equals("CANDIDATE")) {
                                sendResponse(writer, "INVALID_TOKEN", clientData, "LOOKUP_ACCOUNT_CANDIDATE");
                                return;
                            }

                            if (client == null) {
                                sendResponse(writer, "USER_NOT_FOUND", clientData, "LOOKUP_ACCOUNT_CANDIDATE");
                            } else {
                                sendResponse(writer, "SUCCESS", clientData, "LOOKUP_ACCOUNT_CANDIDATE");
                            }
                        }
                        break;

                    case "UPDATE_ACCOUNT_CANDIDATE":
                        // Update

                        if (loggedInClient == null) {
                            System.out.println("Login first.");
                            continue;
                        }

                        String nameC = (String) requestData.get("name");
                        String emailC = (String) requestData.get("email");
                        String passwordC = (String) requestData.get("password");

                        JSONObject clientDataUP = new JSONObject();
                        clientDataUP.put("name", nameC);
                        clientDataUP.put("email", emailC);
                        clientDataUP.put("password", passwordC);

                     //   String token4 = (String) request.get("token");
                        String token4 = request.getToken();
                        if (token4 == null) {
                            sendResponse(writer, "INVALID_TOKEN", clientDataUP, "UPDATE_ACCOUNT_CANDIDATE");
                            return;
                        }

                        DecodedJWT jwt3;
                        try {
                            jwt3 = JWT.decode(token4);
                        } catch (JWTDecodeException e) {
                            sendResponse(writer, "INVALID_TOKEN", clientDataUP, "UPDATE_ACCOUNT_CANDIDATE");
                            return;
                        }

                        String idStr3 = jwt3.getClaim("id").asString();
                        String role3 = jwt3.getClaim("role").asString();

                        if (idStr3 == null || role3 == null || !role3.equals("CANDIDATE")) {
                            sendResponse(writer, "INVALID_TOKEN", clientDataUP, "UPDATE_ACCOUNT_CANDIDATE");
                            return;
                        }

                        int id5;
                        try {
                            id5 = Integer.parseInt(idStr3);
                        } catch (NumberFormatException e) {
                            sendResponse(writer, "INVALID_ID_FORMAT", clientDataUP, "UPDATE_ACCOUNT_CANDIDATE");
                            return;
                        }


                        if (nameC == null || emailC == null || passwordC == null) {
                            sendResponse(writer, "INVALID_FIELD", clientDataUP, "UPDATE_ACCOUNT_CANDIDATE");
                            continue;
                        }

                        // Verificar se os campos não estão vazios
                        if (nameC.isEmpty() || emailC.isEmpty() || passwordC.isEmpty()) {
                            sendResponse(writer, "INVALID_FIELD", clientDataUP, "UPDATE_ACCOUNT_CANDIDATE");
                            continue;
                        }

                        Client existingClient = clientCrud.readClient(emailC);
                        if (existingClient != null && existingClient.getId() != id5) {
                            sendResponse(writer, "INVALID_EMAIL", clientDataUP, "UPDATE_ACCOUNT_CANDIDATE");
                        } else {
                            if (clientCrud.update(id5, nameC, emailC, passwordC)) {
                                sendResponse(writer, "SUCCESS", clientDataUP, "UPDATE_ACCOUNT_CANDIDATE");
                            } else {
                                sendResponse(writer, "SUCCESS", clientDataUP, "UPDATE_ACCOUNT_CANDIDATE");
                            }
                        }
                        break;


                    case "DELETE_ACCOUNT_CANDIDATE":
                        // Excluir um candidato
                        if(loggedInClient == null) {
                            System.out.println("Login first.");
                            return;
                        }

                        JSONObject clientDataDL = new JSONObject();
                        clientDataDL.get("name");
                        clientDataDL.get("email");
                        clientDataDL.get("password");

                       // String token3 = (String) request.get("token");
                        String token3 = request.getToken();
                        if (token3 == null) {
                            sendResponse(writer, "INVALID_TOKEN", clientDataDL, "DELETE_ACCOUNT_CANDIDATE");
                            return;
                        }

                        DecodedJWT jwt2;
                        try {
                            jwt2 = JWT.decode(token3);
                        } catch (JWTDecodeException e) {
                            sendResponse(writer, "INVALID_TOKEN", clientDataDL, "DELETE_ACCOUNT_CANDIDATE");
                            return;
                        }

                        String idStr2 = jwt2.getClaim("id").asString();
                        String role2 = jwt2.getClaim("role").asString();

                        if (idStr2 == null || role2 == null || !role2.equals("CANDIDATE")) {
                            sendResponse(writer, "INVALID_TOKEN", clientDataDL, "DELETE_ACCOUNT_CANDIDATE");
                            return;
                        }

                        int id4;
                        try {
                            id4 = Integer.parseInt(idStr2);
                        } catch (NumberFormatException e) {
                            sendResponse(writer, "INVALID_ID_FORMAT", clientDataDL, "DELETE_ACCOUNT_CANDIDATE");
                            return;
                        }

                        Client client2 = clientCrud.getClientByID(id4);
                        if (client2 == null) {
                            sendResponse(writer, "USER_NOT_FOUND", clientDataDL, "DELETE_ACCOUNT_CANDIDATE");
                        } else {
                            clientCrud.delete(id4);
                            sendResponse(writer, "SUCCESS", clientDataDL, "DELETE_ACCOUNT_CANDIDATE");
                        }
                        break;

                    case "LOGOUT_CANDIDATE":
                        // Logout

                        if(loggedInClient == null) {
                            System.out.println("Login first.");
                            continue;
                        }

                        String token1 = (String) request1.get("token");
                        //String token1 = request.getToken();

                        String nameLO = (String) requestData.get("name");
                        String emailLO = (String) requestData.get("email");
                        String passwordLO = (String) requestData.get("password");

                        JSONObject clientDataLO = new JSONObject();
                        clientDataLO.put("name", nameLO);
                        clientDataLO.put("email", emailLO);
                        clientDataLO.put("password", passwordLO);

                        if (token1 == null) {
                            sendResponse(writer, "INVALID_TOKEN", clientDataLO, "LOGOUT");
                            return;
                        }

                        DecodedJWT jwt1;
                        try {
                            jwt1 = JWT.decode(token1);
                        } catch (JWTDecodeException e) {
                            sendResponse(writer, "INVALID_TOKEN", clientDataLO, "LOGOUT");
                            return;
                        }

                        String idString = jwt1.getClaim("id").asString();
                        String role1 = jwt1.getClaim("role").asString();

                        if (idString == null || role1 == null || !role1.equals("CANDIDATE")) {
                            sendResponse(writer, "INVALID_TOKEN", clientDataLO, "LOGOUT");
                            return;
                        }

                        int id2;
                        try {
                            id2 = Integer.parseInt(idString);
                        } catch (NumberFormatException e) {
                            sendResponse(writer, "INVALID_ID_FORMAT", clientDataLO, "LOGOUT");
                            return;
                        }

                        Client client3 = clientCrud.getClientByID(id2);
                        if (client3 == null) {
                            sendResponse(writer, "USER_NOT_FOUND", clientDataLO, "LOGOUT");
                        } else {
                            sendResponse(writer, "SUCCESS", clientDataLO, "LOGOUT");
                            clientCrud.logout();
                            loggedInClient = null;
                        }
                        break;

                    case "INCLUDE_SKILL":
                        // skill e experience

                        if (loggedInClient == null) {
                            System.out.println("Login first.");
                            continue;
                        }

                        //String token = request.getToken();
                        DecodedJWT jwt4;
                        try {
                            jwt4 = JWT.decode(token);
                        } catch (JWTDecodeException e) {
                            sendResponse(writer, "INVALID_TOKEN", null, "LOGOUT");
                            return;
                        }

                        String idString2 = jwt4.getClaim("id").asString();
                        String role4 = jwt4.getClaim("role").asString();

                        if (idString2 == null || role4 == null || !role4.equals("CANDIDATE")) {
                            sendResponse(writer, "INVALID_TOKEN", null, "LOGOUT");
                            return;
                        }

                        int id6;
                        try {
                            id6 = Integer.parseInt(idString2);
                        } catch (NumberFormatException e) {
                            sendResponse(writer, "INVALID_ID_FORMAT", null, "LOGOUT");
                            return;
                        }


                        String skill = (String) requestData.get("skill");
                        String experience = (String) requestData.get("experience");

                        JSONObject skillData = new JSONObject();
                        skillData.put("skill", skill);
                        skillData.put("experience", experience);

                        if (skill == null || experience == null || skill.isEmpty() || experience.isEmpty()) {
                            sendResponse(writer, "INVALID_FIELD", skillData, "INCLUDE_SKILL");
                            return;
                        }
                        // verificar se ja existe uma skill com esse nome
                        Skill skill2 = skillCrud.readSkillsD(skill);
                        if (skill2 != null) {
                            sendResponse(writer, "SKILL_EXIST", skillData, "INCLUDE_SKILL");
                            return;
                        }

                        if (skillCrud.createSkill(id6, skill, experience)) {
                            sendResponse(writer, "SUCCESS", skillData, "INCLUDE_SKILL");
                        } else {
                            sendResponse(writer, "FAILURE", skillData, "INCLUDE_SKILL");
                        }

                        break;

                    case "LOOKUP_SKILL":

                        if (loggedInClient == null) {
                            System.out.println("Login first.");
                            return;
                        }

                        String token8 = (String) request1.get("token");
                        String skillId = (String) requestData.get("skill");
                        if (token8 == null) {
                            sendResponse(writer, "INVALID_TOKEN", null, "LOOKUP_SKILL");
                            return;
                        }

                        DecodedJWT jwt;
                        try {
                            jwt = JWT.decode(token8);
                        } catch (JWTDecodeException e) {
                            sendResponse(writer, "INVALID_TOKEN", null, "LOOKUP_SKILL");
                            return;
                        }

                        String idStr = jwt.getClaim("id").asString();
                        String role = jwt.getClaim("role").asString();

                        if (idStr == null || role == null || !role.equals("CANDIDATE")) {
                            sendResponse(writer, "INVALID_TOKEN", null, "LOOKUP_SKILL");
                            return;
                        }

                        int id;
                        try {
                            id = Integer.parseInt(idStr);
                        } catch (NumberFormatException e) {
                            sendResponse(writer, "INVALID_ID_FORMAT", null, "LOOKUP_SKILL");
                            return;
                        }

                        Skill skill3 = skillCrud.readSkillString(skillId, id);
                        if (skill3 == null) {
                            sendResponse(writer, "USER_NOT_FOUND", null, "LOOKUP_SKILL");
                        } else {
                            JSONObject skillData3 = new JSONObject();
                            skillData3.put("skill", skill3.getSkill());
                            skillData3.put("experience", skill3.getExperience());
                            sendResponse(writer, "SUCCESS", skillData3, "LOOKUP_SKILL");
                        }
                        break;

                    case "UPDATE_SKILL":
                        // skill and experience

                        if (loggedInClient == null) {
                            System.out.println("Login first.");
                            continue;
                        }

                        String skillUP = (String) requestData.get("skill");
                        String experienceUP = (String) requestData.get("experience");

                        JSONObject skillDataUP = new JSONObject();
                        skillDataUP.put("skill", skillUP);
                        skillDataUP.put("experience", experienceUP);

                        //String tokenSkill = (String) request.get("token");
                        String tokenSkill = request.getToken();
                        if (tokenSkill == null) {
                            sendResponse(writer, "INVALID_TOKEN", skillDataUP, "UPDATE_SKILL");
                            return;
                        }

                        DecodedJWT jwtSkill2;
                        try {
                            jwtSkill2 = JWT.decode(tokenSkill);
                        } catch (JWTDecodeException e) {
                            sendResponse(writer, "INVALID_TOKEN", skillDataUP, "UPDATE_SKILL");
                            return;
                        }

                        String idStrSkill2 = jwtSkill2.getClaim("id").asString();
                        String roleSkill2 = jwtSkill2.getClaim("role").asString();

                        if (idStrSkill2 == null || roleSkill2 == null || !roleSkill2.equals("CANDIDATE")) {
                            sendResponse(writer, "INVALID_TOKEN", skillDataUP, "UPDATE_SKILL");
                            return;
                        }

                        int idSkill2;
                        try {
                            idSkill2 = Integer.parseInt(idStrSkill2);
                        } catch (NumberFormatException e) {
                            sendResponse(writer, "INVALID_ID_FORMAT", skillDataUP, "UPDATE_SKILL");
                            return;
                        }

                        if (skillUP == null || experienceUP == null) {
                            sendResponse(writer, "INVALID_FIELD", skillDataUP, "UPDATE_SKILL");
                            continue;
                        }

                        // Verificar se os campos não estão vazios
                        if (skillUP.isEmpty() || experienceUP.isEmpty()) {
                            sendResponse(writer, "INVALID_FIELD", skillDataUP, "UPDATE_SKILL");
                            continue;
                        }
                        skillCrud.updateSkill(idSkill2, skillUP, experienceUP);
                        sendResponse(writer, "SUCCESS", skillDataUP, "UPDATE_SKILL");
                        break;

                    case "DELETE_SKILL":
                        if (loggedInClient == null) {
                            System.out.println("Login first.");
                            continue;
                        }

                        String skillIdToDelete;
                        try {
                            skillIdToDelete = ((String) requestData.get("skill"));
                        } catch (NumberFormatException | NullPointerException e) {
                            sendResponse(writer, "INVALID_SKILL_ID", new JSONObject(), "DELETE_SKILL");
                            return;
                        }

                        JSONObject skillDataToDelete = new JSONObject();
                        skillDataToDelete.put("id", skillIdToDelete);

                        String tokenDeleteSkill = (String) request1.get("token");
                        if (tokenDeleteSkill == null) {
                            sendResponse(writer, "INVALID_TOKEN", skillDataToDelete, "DELETE_SKILL");
                            return;
                        }

                        DecodedJWT jwtDeleteSkill;
                        try {
                            jwtDeleteSkill = JWT.decode(tokenDeleteSkill);
                        } catch (JWTDecodeException e) {
                            sendResponse(writer, "INVALID_TOKEN", skillDataToDelete, "DELETE_SKILL");
                            return;
                        }

                        String idStrDeleteSkill = jwtDeleteSkill.getClaim("id").asString();
                        String roleDeleteSkill = jwtDeleteSkill.getClaim("role").asString();

                        if (idStrDeleteSkill == null || roleDeleteSkill == null || !roleDeleteSkill.equals("CANDIDATE")) {
                            sendResponse(writer, "INVALID_TOKEN", skillDataToDelete, "DELETE_SKILL");
                            return;
                        }

                        int clientId;
                        try {
                            clientId = Integer.parseInt(idStrDeleteSkill);
                        } catch (NumberFormatException e) {
                            sendResponse(writer, "INVALID_ID_FORMAT", skillDataToDelete, "DELETE_SKILL");
                            return;
                        }

//                        // Verificar se a habilidade existe
//                        Skill skillToDelete = skillCrud.readSkillById(skillIdToDelete, clientId);
//                        if (skillToDelete == null) {
//                            sendResponse(writer, "SKILL_NOT_FOUND", new JSONObject(), "DELETE_SKILL");
//                            return;
//                        }

                        Skill skillDelete = skillCrud.readSkillsD(skillIdToDelete);
                        if(skillDelete == null){
                            sendResponse(writer, "SKILL_NOT_FOUND", new JSONObject(), "DELETE_SKILL");
                            return;
                        }


                        // Excluir a habilidade
                        boolean deleteSuccess = skillCrud.deleteSkill(skillIdToDelete);

                        if (deleteSuccess) {
                            sendResponse(writer, "SUCCESS", skillDataToDelete, "DELETE_SKILL");
                        } else {
                            sendResponse(writer, "SUCCESS", skillDataToDelete, "DELETE_SKILL");
                        }
                        break;

                    case "SEARCH_JOB":
                        String tokenJobset = (String) request1.get("token");
                        List<String> skillsJobset = (List<String>) requestData.get("skill");
                        String experienceJobset = (String) requestData.get("experience");
                        String filterJobset = (String) requestData.get("filter");

                        if (tokenJobset == null) {
                            sendResponse(writer, "INVALID_TOKEN", new JSONObject(), "SEARCH_JOB");
                            return;
                        }

                        DecodedJWT jwtJobset;
                        try {
                            jwtJobset = JWT.decode(tokenJobset);
                        } catch (JWTDecodeException e) {
                            sendResponse(writer, "INVALID_TOKEN", new JSONObject(), "SEARCH_JOB");
                            return;
                        }

                        String idStrJobset = jwtJobset.getClaim("id").asString();
                        String roleJobset = jwtJobset.getClaim("role").asString();

                        if (idStrJobset == null || roleJobset == null) {
                            sendResponse(writer, "INVALID_TOKEN", new JSONObject(), "SEARCH_JOB");
                            return;
                        }

                        int recruiterIdJobset;
                        try {
                            recruiterIdJobset = Integer.parseInt(idStrJobset);
                        } catch (NumberFormatException e) {
                            sendResponse(writer, "INVALID_ID_FORMAT", new JSONObject(), "SEARCH_JOB");
                            return;
                        }

                        // Verificação do filtro
                        if (!filterJobset.equalsIgnoreCase("AND") && !filterJobset.equalsIgnoreCase("OR")) {
                            sendResponse(writer, "INVALID_FILTER", new JSONObject(), "SEARCH_JOB");
                            return;
                        }

                        // Obter jobset
                        List<Job> jobset = jobCrud.searchJob(skillsJobset, experienceJobset, filterJobset);

                        // Construir resposta
                        JSONObject responseJobset = new JSONObject();
                        responseJobset.put("jobset_size", jobset.size());

                        JSONArray jobsArray = new JSONArray();
                        for (Job job : jobset) {
                            JSONObject jobObject = new JSONObject();
                            jobObject.put("skill", job.getSkill());
                            jobObject.put("experience", job.getExperience());
                            jobObject.put("id", job.getId());
                            jobsArray.add(jobObject);
                        }
                        responseJobset.put("jobset", jobsArray);

                        sendResponse(writer, "SUCCESS", responseJobset, "SEARCH_JOB");
                        break;

                    case "LOOKUP_SKILLSET":
                        if (loggedInClient == null) {
                            System.out.println("Login first.");
                            continue;
                        }

                        String tokenLookupSkillset = (String) request1.get("token");
                        if (tokenLookupSkillset == null) {
                            sendResponse(writer, "INVALID_TOKEN", new JSONObject(), "LOOKUP_SKILLSET");
                            return;
                        }

                        DecodedJWT jwtLookupSkillset;
                        try {
                            jwtLookupSkillset = JWT.decode(tokenLookupSkillset);
                        } catch (JWTDecodeException e) {
                            sendResponse(writer, "INVALID_TOKEN", new JSONObject(), "LOOKUP_SKILLSET");
                            return;
                        }

                        String idStrLookupSkillset = jwtLookupSkillset.getClaim("id").asString();
                        String roleLookupSkillset = jwtLookupSkillset.getClaim("role").asString();

                        if (idStrLookupSkillset == null || roleLookupSkillset == null || !roleLookupSkillset.equals("CANDIDATE")) {
                            sendResponse(writer, "INVALID_TOKEN", new JSONObject(), "LOOKUP_SKILLSET");
                            return;
                        }

                        int clientIdLookupSkillset;
                        try {
                            clientIdLookupSkillset = Integer.parseInt(idStrLookupSkillset);
                        } catch (NumberFormatException e) {
                            sendResponse(writer, "INVALID_ID_FORMAT", new JSONObject(), "LOOKUP_SKILLSET");
                            return;
                        }

                        // Obter skillset
                        List<Skill> skillset = skillCrud.readSkillset(clientIdLookupSkillset);

                        // Construir resposta
                        JSONObject responseSkillset = new JSONObject();
                        responseSkillset.put("skillset_size", ((List<?>) skillset).size());

                        JSONArray skillsArray = new JSONArray();
                        for (Skill skillSET : skillset) {
                            JSONObject skillObject = new JSONObject();
                            skillObject.put("skill", skillSET.getSkill());
                            skillObject.put("experience", skillSET.getExperience());
                            skillsArray.add(skillObject);
                        }
                        responseSkillset.put("skillset", skillsArray);

                        sendResponse(writer, "SUCCESS", responseSkillset, "LOOKUP_SKILLSET");
                        break;


                    case "SIGNUP_RECRUITER":
                        // Adicionar um recrutador
                        String nameR = (String) requestData.get("name");
                        String emailR = (String) requestData.get("email");
                        String passwordR = (String) requestData.get("password");
                        String industry = (String) requestData.get("industry");
                        String description = (String) requestData.get("description");

                        JSONObject recruiterDataS = new JSONObject();
                        recruiterDataS.put("name", nameR);
                        recruiterDataS.put("email", emailR);
                        recruiterDataS.put("password", passwordR);
                        recruiterDataS.put("industry", industry);
                        recruiterDataS.put("description", description);

                        if (nameR == null || emailR == null || passwordR == null || industry == null || description == null) {
                            sendResponse(writer, "INVALID_FIELD", recruiterDataS, "SIGNUP_RECRUITER");
                            continue;
                        }

                        if (nameR.isEmpty() || emailR.isEmpty() || passwordR.isEmpty() || industry.isEmpty() || description.isEmpty()) {
                            sendResponse(writer, "INVALID_FIELD", recruiterDataS, "SIGNUP_RECRUITER");
                            continue;
                        }

                        // Checar se ja existe o recrutador
                        if (recruiterCrud.readRecruiter(emailR) != null) {
                            sendResponse(writer, "USER_EXIST", recruiterDataS, "SIGNUP_RECRUITER");
                        } else {
                            if (recruiterCrud.addRecruiter(new Recruiter(0, nameR, emailR, passwordR, industry, description))) {
                                sendResponse(writer, "SUCCESS", recruiterDataS, "SIGNUP_RECRUITER");
                            } else {
                                sendResponse(writer, "SUCCESS", recruiterDataS, "SIGNUP_RECRUITER");
                            }
                        }

                        break;
                    case "LOGIN_RECRUITER":
                        // Login
                        String email5 = (String) requestData.get("email");
                        String password5 = (String) requestData.get("password");

                        if (loggedInRecruiter != null) {
                            System.out.println("Already logged.");
                            continue;
                        }

                        JSONObject recruiterDataL = new JSONObject();
                        recruiterDataL.put("email", email5);
                        recruiterDataL.put("password", password5);

                        if (email5 == null || password5 == null) {
                            sendResponse(writer, "INVALID_FIELD", recruiterDataL, "LOGIN_RECRUITER");
                            continue;
                        }

                        if(email5.isEmpty() || password5.isEmpty()){
                            sendResponse(writer, "INVALID_FIELD", recruiterDataL, "LOGIN_RECRUITER");
                            continue;
                        }

                        Recruiter recruiter = recruiterCrud.getRecruiterByEmail(email5);
                        // boolean recruiter2 = recruiterCrud.login(email5, password5);
                        loggedInRecruiter = recruiter;
                        recruiterCrud.login(email5, password5);
                        if (recruiter != null && recruiter.getPassword().equals(password5)) {
                            String token2 = generateToken(recruiter.getId(), "RECRUITER");
                            // sendResponseLogin(writer, "SUCCESS", recruiterDataL, "LOGIN_RECRUITER", token2);
                            //sendResponseLogin2(writer, "SUCCESS", token2, "LOGIN_RECRUITER");
                            sendResponseLogin(writer, "SUCCESS", recruiterDataL, "LOGIN_RECRUITER", token2);
                        } else {
                            sendResponse(writer, "INVALID_LOGIN", recruiterDataL, "LOGIN_RECRUITER");
                        }
                        break;

                    case "LOOKUP_ACCOUNT_RECRUITER":

                        if (loggedInRecruiter == null) {
                            System.out.println("Login first.");
                            continue;
                        }

                        if (token == null) {
                            sendResponse(writer, "INVALID_TOKEN", null, "LOOKUP_ACCOUNT_RECRUITER");
                            continue;
                        }

                        DecodedJWT jwt7;
                        try {
                            jwt7 = JWT.decode(token);
                        } catch (JWTDecodeException e) {
                            sendResponse(writer, "INVALID_TOKEN", null, "LOOKUP_ACCOUNT_RECRUITER");
                            continue;
                        }

                        String idStr4 = jwt7.getClaim("id").asString();
                        String role6 = jwt7.getClaim("role").asString();

                        if (idStr4 == null || role6 == null || !role6.equals("RECRUITER")) {
                            sendResponse(writer, "INVALID_TOKEN", null, "LOOKUP_ACCOUNT_RECRUITER");
                            return;
                        }

                        int id3;
                        try {
                            id3 = Integer.parseInt(idStr4);
                        } catch (NumberFormatException e) {
                            sendResponse(writer, "INVALID_ID_FORMAT", null, "LOOKUP_ACCOUNT_RECRUITER");
                            return;
                        }

                        Recruiter recruiter3 = recruiterCrud.getRecruiterByID(id3);
                        if (recruiter3 == null) {
                            sendResponse(writer, "USER_NOT_FOUND", null, "LOOKUP_ACCOUNT_RECRUITER");
                        } else {
                            JSONObject recruiterData = new JSONObject();
                            recruiterData.put("name", recruiter3.getName());
                            recruiterData.put("email", recruiter3.getEmail());
                            recruiterData.put("password", recruiter3.getPassword());
                            recruiterData.put("industry", recruiter3.getIndustry());
                            recruiterData.put("description", recruiter3.getDescription());

                            sendResponse(writer, "SUCCESS", recruiterData, "LOOKUP_ACCOUNT_RECRUITER");
                        }
                        break;

                    case "UPDATE_ACCOUNT_RECRUITER":
                        // Update

                        if (loggedInRecruiter == null) {
                            System.out.println("Login first.");
                            continue;
                        }

                        String nameR2 = (String) requestData.get("name");
                        String emailR2 = (String) requestData.get("email");
                        String passwordR2 = (String) requestData.get("password");
                        String industry2 = (String) requestData.get("industry");
                        String description2 = (String) requestData.get("description");

                        JSONObject recruiterDataUP = new JSONObject();
                        recruiterDataUP.put("name", nameR2);
                        recruiterDataUP.put("email", emailR2);
                        recruiterDataUP.put("password", passwordR2);
                        recruiterDataUP.put("industry", industry2);
                        recruiterDataUP.put("description", description2);

                        //String token5 = (String) request.get("token");
                        String token5 = request.getToken();
                        if (token5 == null) {
                            sendResponse(writer, "INVALID_TOKEN", recruiterDataUP, "UPDATE_ACCOUNT_RECRUITER");
                            return;
                        }

                        DecodedJWT jwt5;
                        try {
                            jwt5 = JWT.decode(token5);
                        } catch (JWTDecodeException e) {
                            sendResponse(writer, "INVALID_TOKEN", recruiterDataUP, "UPDATE_ACCOUNT_RECRUITER");
                            return;
                        }

                        String idStr5 = jwt5.getClaim("id").asString();
                        String role5 = jwt5.getClaim("role").asString();

                        if (idStr5 == null || role5 == null || !role5.equals("RECRUITER")) {
                            sendResponse(writer, "INVALID_TOKEN", recruiterDataUP, "UPDATE_ACCOUNT_RECRUITER");
                            return;
                        }

                        int idUP6;
                        try {
                            idUP6 = Integer.parseInt(idStr5);
                        } catch (NumberFormatException e) {
                            sendResponse(writer, "INVALID_ID_FORMAT", recruiterDataUP, "UPDATE_ACCOUNT_RECRUITER");
                            return;
                        }

                        if (nameR2 == null || emailR2 == null || passwordR2 == null || industry2 == null || description2 == null) {
                            sendResponse(writer, "INVALID_FIELD", recruiterDataUP, "UPDATE_ACCOUNT_RECRUITER");
                            continue;
                        }

                        // Verificar se os campos não estão vazios
                        if (nameR2.isEmpty() || emailR2.isEmpty() || passwordR2.isEmpty() || industry2.isEmpty()
                                || description2.isEmpty()) {
                            sendResponse(writer, "INVALID_FIELD", recruiterDataUP, "UPDATE_ACCOUNT_RECRUITER");
                            continue;
                        }

                        Recruiter existingRecruiter = recruiterCrud.readRecruiter(emailR2);
                        if (existingRecruiter != null && existingRecruiter.getId() != idUP6) {
                            sendResponse(writer, "INVALID_EMAIL", recruiterDataUP, "UPDATE_ACCOUNT_RECRUITER");
                        } else {
                            if (recruiterCrud.update(idUP6, nameR2, emailR2, passwordR2, industry2, description2)) {
                                sendResponse(writer, "SUCCESS", recruiterDataUP, "UPDATE_ACCOUNT_RECRUITER");
                            } else {
                                sendResponse(writer, "SUCCESS", recruiterDataUP, "UPDATE_ACCOUNT_RECRUITER");
                            }
                        }
                        break;

                    case "DELETE_ACCOUNT_RECRUITER":
                        // Excluir um recrutador
                        if(loggedInRecruiter == null) {
                            System.out.println("Login first.");
                            continue;
                        }


                      //  String token6 = (String) request.get("token");
                        String token6 = request.getToken();
                        if (token6 == null) {
                            sendResponse(writer, "INVALID_TOKEN", null, "DELETE_ACCOUNT_RECRUITER");
                            return;
                        }

                        DecodedJWT jwt6;
                        try {
                            jwt6 = JWT.decode(token6);
                        } catch (JWTDecodeException e) {
                            sendResponse(writer, "INVALID_TOKEN", null, "DELETE_ACCOUNT_RECRUITER");
                            return;
                        }

                        String idStr6 = jwt6.getClaim("id").asString();
                        String roleDL6 = jwt6.getClaim("role").asString();

                        if (idStr6 == null || roleDL6 == null || !roleDL6.equals("RECRUITER")) {
                            sendResponse(writer, "INVALID_TOKEN", null, "DELETE_ACCOUNT_RECRUITER");
                            return;
                        }

                        int id7;
                        try {
                            id7 = Integer.parseInt(idStr6);
                        } catch (NumberFormatException e) {
                            sendResponse(writer, "INVALID_ID_FORMAT", null, "DELETE_ACCOUNT_RECRUITER");
                            return;
                        }

                        Recruiter recruiter4 = recruiterCrud.getRecruiterByID(id7);
                        if (recruiter4 == null) {
                            sendResponse(writer, "USER_NOT_FOUND", null, "DELETE_ACCOUNT_RECRUITER");
                        } else {
                            recruiterCrud.delete(id7);
                            JSONObject recruiterDataDL = new JSONObject();
                            recruiterDataDL.put("name", recruiter4.getName());
                            recruiterDataDL.put("email", recruiter4.getEmail());
                            recruiterDataDL.put("password", recruiter4.getPassword());
                            recruiterDataDL.put("industry", recruiter4.getIndustry());
                            recruiterDataDL.put("description", recruiter4.getDescription());
                            sendResponse(writer, "SUCCESS", recruiterDataDL, "DELETE_ACCOUNT_RECRUITER");
                        }
                        break;

                    case "LOGOUT_RECRUITER":

                        if(loggedInRecruiter == null) {
                            System.out.println("Login first.");
                            continue;
                        }
                      // String token7 = (String) request.get("token");
                        String token7 = request.getToken();

                        String nameLO2 = (String) requestData.get("name");
                        String emailLO2 = (String) requestData.get("email");
                        String passwordLO2 = (String) requestData.get("password");

                        JSONObject recruiterDataLO = new JSONObject();
                        recruiterDataLO.put("name", nameLO2);
                        recruiterDataLO.put("email", emailLO2);
                        recruiterDataLO.put("password", passwordLO2);

                        if (token7 == null) {
                            sendResponse(writer, "INVALID_TOKEN", recruiterDataLO, "LOGOUT");
                            return;
                        }

                        DecodedJWT jwt8;
                        try {
                            jwt8 = JWT.decode(token7);
                        } catch (JWTDecodeException e) {
                            sendResponse(writer, "INVALID_TOKEN", recruiterDataLO, "LOGOUT");
                            return;
                        }

                        String idString3 = jwt8.getClaim("id").asString();
                        String role7 = jwt8.getClaim("role").asString();

                        if (idString3 == null || role7 == null || !role7.equals("RECRUITER")) {
                            sendResponse(writer, "INVALID_TOKEN", recruiterDataLO, "LOGOUT");
                            return;
                        }

                        int id8;
                        try {
                            id8 = Integer.parseInt(idString3);
                        } catch (NumberFormatException e) {
                            sendResponse(writer, "INVALID_ID_FORMAT", recruiterDataLO, "LOGOUT");
                            return;
                        }

                        Recruiter recruiter5 = recruiterCrud.getRecruiterByID(id8);
                        if (recruiter5 == null) {
                            sendResponse(writer, "USER_NOT_FOUND", recruiterDataLO, "LOGOUT");
                        } else {
                            sendResponse(writer, "SUCCESS", recruiterDataLO, "LOGOUT");
                            recruiterCrud.logout();
                            loggedInRecruiter = null;
                        }
                        break;

                    case "INCLUDE_JOB":
                        if (loggedInRecruiter == null) {
                            System.out.println("Login first.");
                            continue;
                        }

                        String tokenIncludeJob = (String) request1.get("token");
                        if (tokenIncludeJob == null) {
                            sendResponse(writer, "INVALID_TOKEN", null, "INCLUDE_JOB");
                            return;
                        }

                        DecodedJWT jwtIncludeJob;
                        try {
                            jwtIncludeJob = JWT.decode(tokenIncludeJob);
                        } catch (JWTDecodeException e) {
                            sendResponse(writer, "INVALID_TOKEN", null, "INCLUDE_JOB");
                            return;
                        }

                        String idStringIncludeJob = jwtIncludeJob.getClaim("id").asString();
                        String roleIncludeJob = jwtIncludeJob.getClaim("role").asString();

                        if (idStringIncludeJob == null || roleIncludeJob == null || !roleIncludeJob.equals("RECRUITER")) {
                            sendResponse(writer, "INVALID_TOKEN", null, "INCLUDE_JOB");
                            return;
                        }

                        int recruiterId;
                        try {
                            recruiterId = Integer.parseInt(idStringIncludeJob);
                        } catch (NumberFormatException e) {
                            sendResponse(writer, "INVALID_ID_FORMAT", null, "INCLUDE_JOB");
                            return;
                        }

                        String jobSkill = (String) requestData.get("skill");
                        String jobExperience = (String) requestData.get("experience");

                        JSONObject jobData = new JSONObject();
                        jobData.put("skill", jobSkill);
                        jobData.put("experience", jobExperience);

                        if (jobSkill == null || jobExperience == null || jobSkill.isEmpty() || jobExperience.isEmpty()) {
                            sendResponse(writer, "INVALID_FIELD", jobData, "INCLUDE_JOB");
                            return;
                        }

                        if (jobCrud.createJob(recruiterId, jobSkill, jobExperience)) {
                            sendResponse(writer, "SUCCESS", jobData, "INCLUDE_JOB");
                        } else {
                            sendResponse(writer, "FAILURE", jobData, "INCLUDE_JOB");
                        }
                        break;

                    case "DELETE_JOB":
                        if (loggedInRecruiter == null) {
                            System.out.println("Login first.");
                            continue;
                        }

                        String jobIdToDelete;
                        try {
                            jobIdToDelete = ((String) requestData.get("id"));
                        } catch (NumberFormatException | NullPointerException e) {
                            sendResponse(writer, "INVALID_JOB_ID", new JSONObject(), "DELETE_JOB");
                            return;
                        }

                        JSONObject jobDataToDelete = new JSONObject();
                        jobDataToDelete.put("id", jobIdToDelete);

                        String tokenDeleteJob = (String) request1.get("token");
                        if (tokenDeleteJob == null) {
                            sendResponse(writer, "INVALID_TOKEN", jobDataToDelete, "DELETE_JOB");
                            return;
                        }

                        DecodedJWT jwtDeleteJob;
                        try {
                            jwtDeleteJob = JWT.decode(tokenDeleteJob);
                        } catch (JWTDecodeException e) {
                            sendResponse(writer, "INVALID_TOKEN", jobDataToDelete, "DELETE_JOB");
                            return;
                        }

                        String idStrDeleteJob = jwtDeleteJob.getClaim("id").asString();
                        String roleDeleteJob = jwtDeleteJob.getClaim("role").asString();

                        if (idStrDeleteJob == null || roleDeleteJob == null || !roleDeleteJob.equals("RECRUITER")) {
                            sendResponse(writer, "INVALID_TOKEN", jobDataToDelete, "DELETE_JOB");
                            return;
                        }

                        int recruiterIdDeleteJob;
                        try {
                            recruiterIdDeleteJob = Integer.parseInt(idStrDeleteJob);
                        } catch (NumberFormatException e) {
                            sendResponse(writer, "INVALID_ID_FORMAT", jobDataToDelete, "DELETE_JOB");
                            return;
                        }

                        // Verificar se o job existe
                        Job jobToDelete = jobCrud.readJobById(jobIdToDelete, recruiterIdDeleteJob);
                        if (jobToDelete == null) {
                            sendResponse(writer, "JOB_NOT_FOUND", new JSONObject(), "DELETE_JOB");
                            return;
                        }

                        // Excluir o job
                        boolean deleteJobSuccess = jobCrud.deleteJob(jobIdToDelete);

                        if (deleteJobSuccess) {
                            sendResponse(writer, "SUCCESS", jobDataToDelete, "DELETE_JOB");
                        } else {
                            sendResponse(writer, "FAILURE", jobDataToDelete, "DELETE_JOB");
                        }
                        break;

                    case "LOOKUP_JOB":
                        if (loggedInRecruiter == null) {
                            System.out.println("Login first.");
                            continue;
                        }

                        String tokenLookupJob = (String) request1.get("token");
                        String jobId = (String) requestData.get("id");
                        if (tokenLookupJob == null) {
                            sendResponse(writer, "INVALID_TOKEN", null, "LOOKUP_JOB");
                            return;
                        }

                        DecodedJWT jwtLookupJob;
                        try {
                            jwtLookupJob = JWT.decode(tokenLookupJob);
                        } catch (JWTDecodeException e) {
                            sendResponse(writer, "INVALID_TOKEN", null, "LOOKUP_JOB");
                            return;
                        }

                        String idStrLookupJob = jwtLookupJob.getClaim("id").asString();
                        String roleLookupJob = jwtLookupJob.getClaim("role").asString();

                        if (idStrLookupJob == null || roleLookupJob == null || !roleLookupJob.equals("RECRUITER")) {
                            sendResponse(writer, "INVALID_TOKEN", null, "LOOKUP_JOB");
                            return;
                        }

                        int recruiterIdLookupJob;
                        try {
                            recruiterIdLookupJob = Integer.parseInt(idStrLookupJob);
                        } catch (NumberFormatException e) {
                            sendResponse(writer, "INVALID_ID_FORMAT", null, "LOOKUP_JOB");
                            return;
                        }

                        Job job2 = jobCrud.readJobById(jobId, recruiterIdLookupJob);
                        if (job2 == null) {
                            sendResponse(writer, "JOB_NOT_FOUND", null, "LOOKUP_JOB");
                        } else {
                            JSONObject jobDataLookup = new JSONObject();
                            jobDataLookup.put("skill", job2.getSkill());
                            jobDataLookup.put("experience", job2.getExperience());
                            sendResponse(writer, "SUCCESS", jobDataLookup, "LOOKUP_JOB");
                        }
                        break;

                    case "LOOKUP_JOBSET":
                        if (loggedInRecruiter == null) {
                            System.out.println("Login first.");
                            continue;
                        }

                        String tokenLookupJobset = (String) request1.get("token");
                        if (tokenLookupJobset == null) {
                            sendResponse(writer, "INVALID_TOKEN", new JSONObject(), "LOOKUP_JOBSET");
                            return;
                        }

                        DecodedJWT jwtLookupJobset;
                        try {
                            jwtLookupJobset = JWT.decode(tokenLookupJobset);
                        } catch (JWTDecodeException e) {
                            sendResponse(writer, "INVALID_TOKEN", new JSONObject(), "LOOKUP_JOBSET");
                            return;
                        }

                        String idStrLookupJobset = jwtLookupJobset.getClaim("id").asString();
                        String roleLookupJobset = jwtLookupJobset.getClaim("role").asString();

                        if (idStrLookupJobset == null || roleLookupJobset == null || !roleLookupJobset.equals("RECRUITER")) {
                            sendResponse(writer, "INVALID_TOKEN", new JSONObject(), "LOOKUP_JOBSET");
                            return;
                        }

                        int recruiterIdLookupJobset;
                        try {
                            recruiterIdLookupJobset = Integer.parseInt(idStrLookupJobset);
                        } catch (NumberFormatException e) {
                            sendResponse(writer, "INVALID_ID_FORMAT", new JSONObject(), "LOOKUP_JOBSET");
                            return;
                        }

                        // Obter jobset
                        List<Job> jobLookSet = jobCrud.readJobset(recruiterIdLookupJobset);

                        // Construir resposta
                        JSONObject responseLookJobset = new JSONObject();
                        responseLookJobset.put("jobset_size", jobLookSet.size());

                        JSONArray jobsLookArray = new JSONArray();
                        for (Job jobSET : jobLookSet) {
                            JSONObject jobObject = new JSONObject();
                            jobObject.put("skill", jobSET.getSkill());
                            jobObject.put("experience", jobSET.getExperience());
                            jobObject.put("id", jobSET.getId());
                            jobsLookArray.add(jobObject);
                        }
                        responseLookJobset.put("jobset", jobsLookArray);

                        sendResponse(writer, "SUCCESS", responseLookJobset, "LOOKUP_JOBSET");
                        break;

                    case "UPDATE_JOB":
                        if (loggedInRecruiter == null) {
                            System.out.println("Login first.");
                            continue;
                        }

                        String jobIdUpdate = (String) requestData.get("id");
                        String jobSkillUpdate = (String) requestData.get("skill");
                        String jobExperienceUpdate = (String) requestData.get("experience");

                        JSONObject jobDataUpdate = new JSONObject();
                        jobDataUpdate.put("id", jobIdUpdate);
                        jobDataUpdate.put("skill", jobSkillUpdate);
                        jobDataUpdate.put("experience", jobExperienceUpdate);

                        String tokenUpdateJob = (String) request1.get("token");
                        if (tokenUpdateJob == null) {
                            sendResponse(writer, "INVALID_TOKEN", jobDataUpdate, "UPDATE_JOB");
                            return;
                        }

                        DecodedJWT jwtUpdateJob;
                        try {
                            jwtUpdateJob = JWT.decode(tokenUpdateJob);
                        } catch (JWTDecodeException e) {
                            sendResponse(writer, "INVALID_TOKEN", jobDataUpdate, "UPDATE_JOB");
                            return;
                        }

                        String idStrUpdateJob = jwtUpdateJob.getClaim("id").asString();
                        String roleUpdateJob = jwtUpdateJob.getClaim("role").asString();

                        if (idStrUpdateJob == null || roleUpdateJob == null || !roleUpdateJob.equals("RECRUITER")) {
                            sendResponse(writer, "INVALID_TOKEN", jobDataUpdate, "UPDATE_JOB");
                            return;
                        }

                        int recruiterIdUpdateJob;
                        try {
                            recruiterIdUpdateJob = Integer.parseInt(idStrUpdateJob);
                        } catch (NumberFormatException e) {
                            sendResponse(writer, "INVALID_ID_FORMAT", jobDataUpdate, "UPDATE_JOB");
                            return;
                        }

                        if (jobSkillUpdate == null || jobExperienceUpdate == null) {
                            sendResponse(writer, "INVALID_FIELD", jobDataUpdate, "UPDATE_JOB");
                            continue;
                        }

                        if (jobSkillUpdate.isEmpty() || jobExperienceUpdate.isEmpty()) {
                            sendResponse(writer, "INVALID_FIELD", jobDataUpdate, "UPDATE_JOB");
                            continue;
                        }

                        if (jobCrud.updateJob(jobIdUpdate, recruiterIdUpdateJob, jobSkillUpdate, jobExperienceUpdate)) {
                            sendResponse(writer, "SUCCESS", jobDataUpdate, "UPDATE_JOB");
                        } else {
                            sendResponse(writer, "FAILURE", jobDataUpdate, "UPDATE_JOB");
                        }
                        break;

                    default:

                        JSONObject clientDataD = new JSONObject();
                        clientDataD.get("name");
                        clientDataD.get("email");
                        clientDataD.get("password");

                        sendResponse(writer, "INVALID_OPERATION", clientDataD, "NAO_EXISTE");
                        continue;
                }

                if (jsonString.equals("Bye.")) {
                    break;
                }

                if (jsonString.equals("End Server.")) {
                    serverContinue = false;
                }
            }
        } catch (IOException | ParseException e) {
            System.err.println("Problem with Communication Server");
            e.printStackTrace();
        }
    }

    // Enviar resposta
    private void sendResponse(PrintWriter writer, String status, JSONObject data, String operation) {
        JSONObject response = new JSONObject();
        response.put("operation", operation);
        response.put("status", status);
        response.put("data", data);
        writer.println(response.toJSONString());

        System.out.println("Response: " + response.toJSONString());
    }

//    private void sendResponseLogin(PrintWriter writer, String status, JSONObject data, String operation, String token) {
//        Response response = new Response(operation, status, data, token);
//        writer.println(response);
//        System.out.println("Response: " + response.toJSONString());
//    }

    // Enviar resposta com token de login
    private void sendResponseLogin(PrintWriter writer, String status, JSONObject data, String operation, String token) {
        JSONObject response = new JSONObject();
        response.put("operation", operation);
        response.put("status", status);
        response.put("data", data);
        response.put("token", token);
        writer.println(response.toJSONString());

        System.out.println("Response: " + response.toJSONString());
    }

    private void sendResponseLogin2(PrintWriter writer, String status, String tokenData, String operation) {
        JSONObject response = new JSONObject();
        response.put("operation", operation);
        response.put("status", status);
        // response.put("token", tokenData);
        // response.put("data", responsetoken);

        JSONObject responsetoken = new JSONObject();
        responsetoken.put("token", tokenData);

        response.put("data", responsetoken);
        //response.put("data", "token:");
        writer.println(response.toJSONString());

        System.out.println("Response: " + response.toJSONString());
    }

    private boolean isLogged(String token) {
        return verifyToken(token);
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

    private static String decodeToken(int id, String role){
        return String.valueOf(JWT.decode(String.valueOf(id)));
    }

}