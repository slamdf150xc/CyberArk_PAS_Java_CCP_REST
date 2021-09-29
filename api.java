import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.*;

public class api {
	// Provide your base URL for your PVWA/CCP
	public static final String baseURL="https://pvwa.randa.com/";
	
	public static void main(String[] args) {
		// Provide your AppID, Safe Name and ObjectID of the account you want to log into the REST API with
		String rawCred = getCredential("Ansible", "Service Accounts", "Application-RandA_CyberArk_Local-192.168.1.34-administrator");
		
		Gson gson = new Gson();
	    
	    CredentialObject user = gson.fromJson(rawCred, CredentialObject.class);
	    		
		String token = getToken(user.getUserName(), user.getContent(), "CyberArk");
		
		System.out.println(sysSum(token));
		
		logoff(token);
	}
	
	public static String getCredential(String AppID, String Safe, String ObjectID) {
		String ccpURL = baseURL + "AIMWebService/api/Accounts?AppID=" + URLEncoder.encode(AppID, StandardCharsets.UTF_8) + "&Safe=" + URLEncoder.encode(Safe, StandardCharsets.UTF_8) + "&Object=" + URLEncoder.encode(ObjectID, StandardCharsets.UTF_8);
		String pwObject = "";
		
		try {
			URL obj = new URL(ccpURL);
			HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
			conn.setDoOutput(true);
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestMethod("GET");
			conn.setDoOutput(true);
			conn.connect();
			
			int code = conn.getResponseCode();
			System.out.println("GetCred: " + code);
			
			BufferedReader reader = new BufferedReader (new InputStreamReader(conn.getInputStream()));
			for (String line; (line = reader.readLine()) != null;) {
				pwObject += line;
			}			
		}
		catch(Exception e) {
			System.out.println(e.getMessage());
		}
		
		return pwObject;
	}
	
	public static String getToken(String UserName, String Password, String AuthType) {
		String tokenGenURL = baseURL + "PasswordVault/API/auth/" + AuthType + "/Logon";
		String token = null;
		String userName = UserName;
		String password = Password;
		
		try {
			URL obj = new URL(tokenGenURL);
			HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
			conn.setDoOutput(true);
			
			Map map = new HashMap();
			map.put("username", userName);
			map.put("password", password);
			
			Gson gson = new Gson();
			String env = gson.toJson(map).toString();
			
			byte[] b = env.getBytes();
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			conn.connect();
			
			OutputStream out = conn.getOutputStream();
			out.write(b);
			out.close();
			
			BufferedReader reader = new BufferedReader (new InputStreamReader(
					conn.getInputStream()));
			for (String line; (line = reader.readLine()) != null;) {
				token = line;
			}
			
			int code = conn.getResponseCode();
			System.out.println("Login: " + code);
		}
		catch(Exception e) {
			System.out.println(e.getMessage());
		}
		
		token = token.replace("\"", "");
		return token;
	}
	
	public static String sysSum(String token) {
		String sysSumURL =  baseURL + "PasswordVault/API/ComponentsMonitoringSummary";
		String ret = "";
		
		try {
			URL obj = new URL(sysSumURL);
			HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
			conn.setDoOutput(true);
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("Authorization", token);
			conn.setRequestMethod("GET");
			conn.setDoOutput(true);
			
			BufferedReader reader = new BufferedReader (new InputStreamReader(
					conn.getInputStream()));
			for (String line; (line = reader.readLine()) != null;) {
				ret += line;
			}			
		}
		catch(Exception e) {
			System.out.println(e.getMessage());
		}
		return ret;		
	}
	
	public static void logoff(String token) {
		String logoffURL =  baseURL + "PasswordVault/API/Auth/Logoff";
		
		try {
			URL obj = new URL(logoffURL);
			HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
			conn.setDoOutput(true);
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("Authorization", token);
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);			
		}
		catch(Exception e) {
			System.out.println(e.getMessage());
		}
	}
}