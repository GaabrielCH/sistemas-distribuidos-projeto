
package Client.services;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Response {
    private String operation;
    private String status;
    private JSONObject data;
    private String token;

    public Response(String operation, String status, JSONObject data, String token) {
        this.operation = operation;
        this.status = status;
        this.data = data;
        this.token = token;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public JSONObject getData() {
        return data;
    }

    public void setData(JSONObject data) {
        this.data = data;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public static Response fromJSONString(String jsonString) throws ParseException {
        JSONObject jsonObject = (JSONObject) new JSONParser().parse(jsonString);
        String operation = (String) jsonObject.get("operation");
        String status = (String) jsonObject.get("status");
        JSONObject data = (JSONObject) jsonObject.get("data");
        String token = (String) jsonObject.get("token");
        return new Response(operation, status, data, token);
    }

    public String toJSONString() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("operation", operation);
        jsonObject.put("status", status);
        jsonObject.put("data", data);
        jsonObject.put("token", token);
        return jsonObject.toJSONString();
    }
}
