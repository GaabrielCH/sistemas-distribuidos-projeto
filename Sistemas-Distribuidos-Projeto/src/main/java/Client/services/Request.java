
package Client.services;

import org.json.simple.JSONObject;

public class Request {
    private String operation;
    private JSONObject data;
    private String token;

    public Request(String operation, JSONObject data, String token) {
        this.operation = operation;
        this.data = data;
        this.token = token;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
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

    public String toJSONString() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("operation", operation);
        jsonObject.put("data", data);
        jsonObject.put("token", token);
        return jsonObject.toJSONString();
    }
}
