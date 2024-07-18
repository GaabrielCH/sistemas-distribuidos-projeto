package Client.services;

import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class EchoClient2 {
    private static final String TOKEN_KEY = "DISTRIBUIDOS";
    private static final Algorithm alg = Algorithm.HMAC256(TOKEN_KEY);

    public static void main(String[] args) {
        // IP da máquina
        String token = null;
        String serverHostname = "192.168.1.11";

        Client loggedInClient = null;
        Recruiter loggedInRecruit = null;

        try (Socket echoSocket = new Socket(serverHostname, 24001);
             PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
             Scanner scanner = new Scanner(System.in)) {

            while (true) {
                System.out.println("\nChoose an operation:");
                System.out.println("1. Mostrar um candidato (LOOKUP_ACCOUNT_CANDIDATE)");
                System.out.println("2. Adicionar um candidato (SIGNUP_CANDIDATE)");
                System.out.println("3. Login (LOGIN_CANDIDATE)");
                System.out.println("4. Excluir um candidato (DELETE_ACCOUNT_CANDIDATE)");
                System.out.println("5. Atualizar um candidato (UPDATE_ACCOUNT_CANDIDATE)");
                System.out.println("6. Logout (LOGOUT_CANDIDATE)");
                System.out.println("7. Include Skill (INCLUDE_SKILL)");
                System.out.println("8. Exclude Skill (DELETE_SKILL)");
                System.out.println("9. Lookup Skill (LOOKUP_SKILL)");
                System.out.println("10. Lookup Skills (LOOKUP_SKILLSET)");
                System.out.println("11. Update Skill (UPDATE_SKILL)");
                System.out.println("12. Search Job (SEARCH_JOB)");

                System.out.println("--------------------------------\n");

                System.out.println("7. Mostrar um recrutador (LOOKUP_ACCOUNT_RECRUITER)");
                System.out.println("8. Adicionar um recrutador (SIGNUP_RECRUITER)");
                System.out.println("9. Login (LOGIN_RECRUITER)");
                System.out.println("10. Excluir um recrutador (DELETE_ACCOUNT_RECRUITER)");
                System.out.println("11. Atualizar um recrutador (UPDATE_ACCOUNT_RECRUITER)");
                System.out.println("12. Logout (LOGOUT_RECRUITER)");
                System.out.println("13. Include Job (INCLUDE_JOB)");
                System.out.println("14. Exclude Job (DELETE_JOB)");
                System.out.println("15. Lookup Job (LOOKUP_JOB)");
                System.out.println("16. Lookup Jobs (LOOKUP_JOBSET)");
                System.out.println("17. Update Job (UPDATE_JOB)");


                String choice = scanner.nextLine();

                JSONObject request = new JSONObject();
                request.put("operation", choice);
                JSONObject requestData = new JSONObject();

                Request request1;

                switch (choice) {
                    case "SIGNUP_CANDIDATE":
                        System.out.println("Enter client name:");
                        requestData.put("name", scanner.nextLine());
                        System.out.println("Enter client email:");
                        requestData.put("email", scanner.nextLine());
                        System.out.println("Enter client password:");
                        requestData.put("password", scanner.nextLine());

                        request1 = new Request("SIGNUP_CANDIDATE", requestData, null);

                        break;

                    case "LOGIN_CANDIDATE":
                        System.out.println("Enter email:");
                        String email = scanner.nextLine();
                        requestData.put("email", email);

                        System.out.println("Enter password:");
                        String password = scanner.nextLine();
                        requestData.put("password", password);

                        request1 = new Request("LOGIN_CANDIDATE", requestData, null);

                        break;

                    case "LOOKUP_ACCOUNT_CANDIDATE":
                        request1 = new Request("LOOKUP_ACCOUNT_CANDIDATE", requestData, token);
                        request.put("token", token);
                        break;

                    case "DELETE_ACCOUNT_CANDIDATE":
                        String tokenDeleteCandidate = generateToken(loggedInClient.getId(), "CANDIDATE");
                        request.put("token", tokenDeleteCandidate);
                        loggedInClient = null;
                        break;

                    case "UPDATE_ACCOUNT_CANDIDATE":
                        System.out.println("Enter updated name:");
                        requestData.put("name", scanner.nextLine());
                        System.out.println("Enter updated email:");
                        requestData.put("email", scanner.nextLine());
                        System.out.println("Enter updated password:");
                        requestData.put("password", scanner.nextLine());

                        String tokenUpdateCandidate = generateToken(loggedInClient.getId(), "CANDIDATE");
                        request.put("token", tokenUpdateCandidate);
                        break;

                    case "LOGOUT_CANDIDATE":
                        request1 = new Request("LOGOUT_CANDIDATE", requestData, token);
                        request.put("token", token);
                        loggedInClient = null;
                        break;

                    case "INCLUDE_SKILL":
                        System.out.println("Enter skill name:");
                        requestData.put("skill", scanner.nextLine());
                        System.out.println("Enter experience:");
                        requestData.put("experience", scanner.nextLine());

                        request1 = new Request("INCLUDE_SKILL", requestData, token);

                        request.put("token", token);
                        break;

                    case "DELETE_SKILL":
                        System.out.println("Enter skill name:");
                        requestData.put("skill", scanner.nextLine());

                        request1 = new Request("DELETE_SKILL", requestData, token);

                        request.put("token", token);
                        break;

                    case "LOOKUP_SKILL":
                        System.out.println("Enter skill name:");
                        requestData.put("skill", scanner.nextLine());

                        request1 = new Request("LOOKUP_SKILL", requestData, token);

                        request.put("token", token);
                        break;

                    case "LOOKUP_SKILLSET":
                        request1 = new Request("INCLUDE_SKILLSET", requestData, token);

                        request.put("token", token);
                        break;

                    case "UPDATE_SKILL":
                        System.out.println("Enter skill name:");
                        requestData.put("skill", scanner.nextLine());
                        System.out.println("Enter updated experience:");
                        requestData.put("experience", scanner.nextLine());

                        request1 = new Request("UPDATE_SKILL", requestData, token);

                        request.put("token", token);
                        break;

                    case "SEARCH_JOB":
                        // faça uma verificação se o usuário quer buscar por skill ou por job ou pelo dois
                        System.out.println("Enter skills ,:");
                        String skillsInput = scanner.nextLine();
                        List<String> skill = Arrays.asList(skillsInput.split(","));
                        requestData.put("skill", skill);
                        System.out.println("Enter experience:");
                        requestData.put("experience", scanner.nextLine());
                        System.out.println("Enter filter (AND/OR)");
                        requestData.put("filter", scanner.nextLine());

                            request1 = new Request("SEARCH_JOB", requestData, token);
                            request.put("token", token);


                        break;

                    case "SIGNUP_RECRUITER":
                        System.out.println("Enter recruiter name:");
                        requestData.put("name", scanner.nextLine());
                        System.out.println("Enter recruiter email:");
                        requestData.put("email", scanner.nextLine());
                        System.out.println("Enter recruiter password:");
                        requestData.put("password", scanner.nextLine());
                        System.out.println("Enter recruiter industry:");
                        requestData.put("industry", scanner.nextLine());
                        System.out.println("Enter recruiter description:");
                        requestData.put("description", scanner.nextLine());
                        break;

                    case "LOGIN_RECRUITER":
                        System.out.println("Enter email:");
                        String email2 = scanner.nextLine();
                        requestData.put("email", email2);

                        System.out.println("Enter password:");
                        String password2 = scanner.nextLine();
                        requestData.put("password", password2);

//                        RecruiterCrud recruiterCrud = new RecruiterCrud(new DataConnection());
//
//                        Recruiter recruiter = recruiterCrud.getRecruiterByEmail(email2);
//                        if (recruiter != null && recruiter.getPassword().equals(password2)) {
//                            loggedInRecruit = recruiter;
//                            System.out.println("Login successful");
//                        } else {
//                            System.out.println("Invalid email or password");
//                            continue;
//                        }
                        request1 = new Request("LOGIN_RECRUITER", requestData, null);
                        break;

                    case "LOOKUP_ACCOUNT_RECRUITER":
//                        if(loggedInRecruit == null){
//                            System.out.println("Do the login.");
//                            continue;
//                        }
                        String tokenLookupRecruiter = generateToken(loggedInRecruit.getId(), "RECRUITER");
                        request.put("token", tokenLookupRecruiter);

                        break;

                    case "DELETE_ACCOUNT_RECRUITER":
                        String tokenDeleteRecruiter = generateToken(loggedInRecruit.getId(), "RECRUITER");
                        request.put("token", tokenDeleteRecruiter);
                        loggedInRecruit = null;
                        break;

                    case "UPDATE_ACCOUNT_RECRUITER":
                        System.out.println("Enter updated name:");
                        requestData.put("name", scanner.nextLine());
                        System.out.println("Enter updated email:");
                        requestData.put("email", scanner.nextLine());
                        System.out.println("Enter updated password:");
                        requestData.put("password", scanner.nextLine());
                        System.out.println("Enter updated industry:");
                        requestData.put("industry", scanner.nextLine());
                        System.out.println("Enter updated description:");
                        requestData.put("description", scanner.nextLine());

                        String tokenUpdateRecruiter = generateToken(loggedInRecruit.getId(), "RECRUITER");
                        request.put("token", tokenUpdateRecruiter);
                        break;

                    case "LOGOUT_RECRUITER":
                        request1 = new Request("LOGOUT_RECRUITER", requestData, token);
                        request.put("token", token);
                        loggedInRecruit = null;
                        break;

                    case "INCLUDE_JOB":
                        System.out.println("Enter skill name:");
                        requestData.put("skill", scanner.nextLine());
                        System.out.println("Enter job experience:");
                        requestData.put("experience", scanner.nextLine());

                        request1 = new Request("INCLUDE_JOB", requestData, token);

                        request.put("token", token);
                        break;

                    case "DELETE_JOB":
                        System.out.println("Enter job id:");
                        requestData.put("id", scanner.nextLine());

//                        String tokenDeleteJob = generateToken(loggedInRecruit.getId(), "RECRUITER");
////
////                        request.put("token", tokenDeleteJob);

                        request1 = new Request("DELETE_JOB", requestData, token);

                        request.put("token", token);
                        break;

                    case "LOOKUP_JOB":
                        System.out.println("Enter job id:");
                        requestData.put("id", scanner.nextLine());
                        request1 = new Request("LOOKUP_JOB", requestData, token);

                        request.put("token", token);
                        break;

                    case "LOOKUP_JOBSET":

                        request1 = new Request("LOOKUP_JOBSET", requestData, token);

                        request.put("token", token);
                        break;

                    case "UPDATE_JOB":
                        System.out.println("Enter job id:");
                        requestData.put("id", scanner.nextLine());
                        System.out.println("Enter job skill:");
                        requestData.put("skill", scanner.nextLine());
                        System.out.println("Enter updated experience:");
                        requestData.put("experience", scanner.nextLine());

                        request1 = new Request("UPDATE_JOB", requestData, token);

                        request.put("token", token);
                        break;


                    default:
                        System.out.println("Invalid operation.");
                        continue;
                }

                request.put("data", requestData);
                out.println(request);
                out.flush();

                // Esperar pela resposta do servidor
                Object response = in.readLine();
                Response response1 = Response.fromJSONString((String) response);
                System.out.println(response);

                if ("LOGIN_CANDIDATE".equals(response1.getOperation()) && "SUCCESS".equals(response1.getStatus())) {
                    token = response1.getToken();
                }
                if("LOGIN_RECRUITER".equals(response1.getOperation()) && "SUCCESS".equals(response1.getStatus())){
                    token = response1.getToken();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private static String generateToken(int id, String role) {
        return JWT.create().withClaim("id", String.valueOf(id)).withClaim("role", role).sign(alg);
    }
}
