package cc.nilm.blog.service;

import cc.nilm.blog.entity.Role;
import cc.nilm.blog.entity.User;
import cc.nilm.blog.repository.RoleRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2Service {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final UserService userService;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    // Google OAuth2 配置
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String googleClientSecret;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String googleRedirectUri;

    // 存儲狀態與重定向URI的映射
    private final Map<String, String> stateToRedirectUri = new ConcurrentHashMap<>();

    /**
     * 獲取 Google 授權 URL
     */
    public String getGoogleAuthorizationUrl(String redirectUri) {
        String state = UUID.randomUUID().toString();
        
        // 使用提供的重定向 URI 或默認的回調 URI
        String actualRedirectUri = redirectUri != null && !redirectUri.isEmpty() 
                ? redirectUri 
                : googleRedirectUri;
        
        log.info("使用重定向 URI: {}", actualRedirectUri);
        
        // 如果提供了重定向 URI，則存儲它
        if (redirectUri != null && !redirectUri.isEmpty()) {
            stateToRedirectUri.put(state, redirectUri);
        }
        
        String authorizationUri = UriComponentsBuilder
                .fromUriString("https://accounts.google.com/o/oauth2/v2/auth")
                .queryParam("client_id", googleClientId)
                .queryParam("response_type", "code")
                .queryParam("scope", "email")
                .queryParam("redirect_uri", actualRedirectUri)
                .queryParam("state", state)
                .queryParam("prompt", "select_account")
                .build()
                .toUriString();
                
        log.info("生成 Google 授權 URL: {}", authorizationUri);
        return authorizationUri;
    }

    /**
     * 獲取存儲的重定向 URI
     */
    public String getStoredRedirectUri(String state) {
        return state != null ? stateToRedirectUri.remove(state) : null;
    }

    /**
     * 處理 Google OAuth2 回調
     */
    public User processGoogleCallback(String code, String state) {
        try {
            // 決定使用哪個重定向URI
            String redirectUri = state != null && stateToRedirectUri.containsKey(state) 
                    ? stateToRedirectUri.get(state) 
                    : googleRedirectUri;
            
            log.info("處理授權碼: {}, 狀態: {}, 重定向URI: {}", code, state, redirectUri);
            
            // 兌換授權碼獲取訪問令牌
            String tokenEndpoint = "https://oauth2.googleapis.com/token";
            
            MultiValueMap<String, String> tokenRequest = new LinkedMultiValueMap<>();
            tokenRequest.add("code", code);
            tokenRequest.add("client_id", googleClientId);
            tokenRequest.add("client_secret", googleClientSecret);
            tokenRequest.add("redirect_uri", redirectUri);
            tokenRequest.add("grant_type", "authorization_code");
            
            log.info("發送兑換請求到Google: {} 使用重定向URI: {}", tokenEndpoint, redirectUri);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            ResponseEntity<String> tokenResponse;
            try {
                tokenResponse = restTemplate.exchange(
                        tokenEndpoint,
                        HttpMethod.POST,
                        new HttpEntity<>(tokenRequest, headers),
                        String.class
                );
                log.info("得到令牌回應: {}", tokenResponse.getStatusCode());
            } catch (Exception e) {
                log.error("兑換授權碼失敗: {}", e.getMessage());
                throw new RuntimeException("兑換授權碼失敗: " + e.getMessage(), e);
            }
            
            JsonNode tokenResponseJson = objectMapper.readTree(tokenResponse.getBody());
            String accessToken = tokenResponseJson.get("access_token").asText();
            
            return processGoogleToken(accessToken);
        } catch (Exception e) {
            log.error("處理 Google 回調失敗", e);
            throw new RuntimeException("處理 Google 回調失敗: " + e.getMessage());
        }
    }

    /**
     * 處理 Google 訪問令牌
     */
    public User processGoogleToken(String accessToken) {
        try {
            // 使用訪問令牌獲取用戶信息
            String userInfoEndpoint = "https://www.googleapis.com/oauth2/v3/userinfo";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            
            ResponseEntity<String> userInfoResponse = restTemplate.exchange(
                    userInfoEndpoint,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    String.class
            );
            
            JsonNode userInfo = objectMapper.readTree(userInfoResponse.getBody());
            
            String email = userInfo.get("email").asText();
            String sub = userInfo.get("sub").asText(); // Google的唯一ID
            String name = userInfo.has("name") ? userInfo.get("name").asText() : email.split("@")[0];
            String picture = userInfo.has("picture") ? userInfo.get("picture").asText() : null;
            
            // 檢查用戶是否已存在
            Optional<User> userOptional = userService.findByEmail(email);
            
            if (userOptional.isPresent()) {
                // 更新現有用戶信息
                User existingUser = userOptional.get();
                existingUser.setProfileImage(picture);
                return userService.save(existingUser);
            } else {
                // 創建新用戶
                User newUser = new User();
                newUser.setUsername(email.split("@")[0] + "_" + sub.substring(0, 8));
                newUser.setEmail(email);
                newUser.setFullName(name);
                newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString())); // 隨機密碼
                newUser.setActive(true);
                newUser.setProfileImage(picture);
                
                // 設置角色
                Set<Role> roles = new HashSet<>();
                Role userRole = roleRepository.findByName(Role.ERole.ROLE_USER)
                        .orElseThrow(() -> new RuntimeException("用戶角色未找到"));
                roles.add(userRole);
                newUser.setRoles(roles);
                
                return userService.save(newUser);
            }
        } catch (Exception e) {
            log.error("處理 Google token 失敗", e);
            throw new RuntimeException("處理 Google token 失敗: " + e.getMessage());
        }
    }
}
