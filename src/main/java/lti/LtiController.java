package lti;

import java.net.URLEncoder;
import java.net.URL;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;
import java.util.Base64;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Controller
public class LtiController {

	@Value("${client_id}")
	private String clientId;

	@GetMapping("/login")
	public String login(final HttpServletRequest request) throws Exception {
		
		System.out.println("Login URL");
		final String state = UUID.randomUUID().toString();
		final String nonce = UUID.randomUUID().toString();
		final String developerUrl = "https://developer.blackboard.com/api/v1/gateway/oidcauth"
			+ "?scope=openid&response_type=id_token&response_mode=form_post&prompt=none"
			+ "&client_id=" + request.getParameter("client_id")
			+ "&redirect_uri=" + URLEncoder.encode(request.getParameter("target_link_uri"))
			+ "&state=" + state
			+ "&nonce=" + nonce
			+ "&login_hint=" + request.getParameter("login_hint")
			+ "&lti_message_hint=" + request.getParameter("lti_message_hint");
		
		System.out.println("Nonce: " + nonce);
		System.out.println("State: " + state);
		System.out.println("Developer URL: " + developerUrl);
		return "redirect:" + developerUrl;
	}
	
	@PostMapping("/launch")
	public String launch(final HttpServletRequest request, final Model model) throws Exception {
		
		System.out.println("Launch URL");
		final String token = request.getParameter("id_token");
		final String[] chunks = token.split("\\.");
		Base64.Decoder decoder = Base64.getUrlDecoder();
		final String header = new String(decoder.decode(chunks[0]));
		final String payload = new String(decoder.decode(chunks[1]));
		
		System.out.println("Header: " + header);
		System.out.println("Payload: " + payload);
		
		final String state = request.getParameter("state");
		//System.out.println("ID Token: " + token);
		System.out.println("State: " + state);
		
		final JsonObject jsonObject = new JsonParser().parse(payload).getAsJsonObject();
		System.out.println("Issuer: " + jsonObject.get("iss").getAsString());
		System.out.println("Name: " + jsonObject.get("name").getAsString());
		System.out.println("Email: " + jsonObject.get("email").getAsString());
		System.out.println("Nonce: " + jsonObject.get("nonce").getAsString());
		model.addAttribute("username", jsonObject.get("name").getAsString());
		
		final DecodedJWT jwt = JWT.decode(token);
		final String jwksUrl = "https://developer.blackboard.com/api/v1/management/applications/" + clientId + "/jwks.json";
		final JwkProvider provider = new UrlJwkProvider(new URL(jwksUrl));
		final Jwk jwk = provider.get(jwt.getKeyId());
		final Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey(), null);
		System.out.println("JWKS URL: " + jwksUrl);
		algorithm.verify(jwt);
		System.out.println("JWT signature verified");
		
		return "launch";
	}
	
}