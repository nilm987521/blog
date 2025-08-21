package cc.nilm.blog.service;

import cc.nilm.blog.entity.Role;
import cc.nilm.blog.entity.User;
import cc.nilm.blog.repository.RoleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuth2ServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private UserService userService;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private OAuth2Service oAuth2Service;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    private final String testGoogleClientId = "test-client-id";
    private final String testGoogleRedirectUri = "http://localhost:8080/api/auth/oauth2/google/callback";
    private final String testAccessToken = "test-access-token";
    private final String testState = UUID.randomUUID().toString();
    private final String testEmail = "test@example.com";
    private final String testPicture = "http://example.com/picture.jpg";

    @BeforeEach
    void setUp() {
        // 使用ReflectionTestUtils設置OAuth2Service的私有屬性
        ReflectionTestUtils.setField(oAuth2Service, "googleClientId", testGoogleClientId);
        String testGoogleClientSecret = "test-client-secret";
        ReflectionTestUtils.setField(oAuth2Service, "googleClientSecret", testGoogleClientSecret);
        ReflectionTestUtils.setField(oAuth2Service, "googleRedirectUri", testGoogleRedirectUri);
    }

    @Test
    void getGoogleAuthorizationUrl_ShouldCreateCorrectUrl() {
        // 執行
        String authUrl = oAuth2Service.getGoogleAuthorizationUrl(null);

        // 驗證
        assertThat(authUrl).contains(
                "https://accounts.google.com/o/oauth2/v2/auth",
                "client_id=" + testGoogleClientId,
                "response_type=code",
                "scope=email",
                "redirect_uri=" + testGoogleRedirectUri,
                "state=",
                "prompt=select_account"
        );
    }

    @Test
    void getGoogleAuthorizationUrl_WithCustomRedirectUri_ShouldUseProvidedUri() {
        // 準備
        String customRedirectUri = "http://example.com/callback";

        // 執行
        String authUrl = oAuth2Service.getGoogleAuthorizationUrl(customRedirectUri);

        // 驗證
        assertThat(authUrl).contains("redirect_uri=" + customRedirectUri);

        // 驗證state和redirectUri的關係被正確保存
        String state = authUrl.split("state=")[1].split("&")[0];
        String storedUri = oAuth2Service.getStoredRedirectUri(state);
        assertEquals(customRedirectUri, storedUri);
    }

    @Test
    void getStoredRedirectUri_ShouldReturnAndRemoveUri() {
        // 準備 - 先產生一個包含state的URL
        String customRedirectUri = "http://example.com/callback";
        String authUrl = oAuth2Service.getGoogleAuthorizationUrl(customRedirectUri);
        String state = authUrl.split("state=")[1].split("&")[0];

        // 驗證URI被儲存
        String storedUri = oAuth2Service.getStoredRedirectUri(state);
        assertEquals(customRedirectUri, storedUri);

        // 驗證獲取後URI被刪除
        String secondAttempt = oAuth2Service.getStoredRedirectUri(state);
        assertNull(secondAttempt);
    }

    @Test
    void processGoogleCallback_ShouldExchangeCodeForToken() {
        // 準備
        mockTokenExchange();
        mockUserInfoFetch(false); // 模擬新用戶

        Role userRole = new Role();
        userRole.setName(Role.ERole.ROLE_USER);
        when(roleRepository.findByName(Role.ERole.ROLE_USER)).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode(any())).thenReturn("encoded-password");
        when(userService.findByEmail(testEmail)).thenReturn(Optional.empty());

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setEmail(testEmail);
        when(userService.save(any(User.class))).thenReturn(savedUser);

        // 執行
        String testAuthCode = "test-auth-code";
        User user = oAuth2Service.processGoogleCallback(testAuthCode, testState);

        // 驗證
        assertNotNull(user);
        assertEquals(testEmail, user.getEmail());

        // 驗證調用了restTemplate.exchange獲取token
        verify(restTemplate, times(2)).exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                eq(String.class)
        );
    }

    @Test
    void processGoogleToken_WithNewUser_ShouldCreateNewUser() {
        // 準備
        mockUserInfoFetch(false); // 模擬新用戶

        Role userRole = new Role();
        userRole.setName(Role.ERole.ROLE_USER);
        when(roleRepository.findByName(Role.ERole.ROLE_USER)).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode(any())).thenReturn("encoded-password");
        when(userService.findByEmail(testEmail)).thenReturn(Optional.empty());

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setEmail(testEmail);
        when(userService.save(any(User.class))).thenReturn(savedUser);

        // 執行
        User user = oAuth2Service.processGoogleToken(testAccessToken);

        // 驗證
        assertNotNull(user);
        assertEquals(testEmail, user.getEmail());

        // 捕獲保存的User對象
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userService).save(userCaptor.capture());

        User capturedUser = userCaptor.getValue();
        assertEquals(testEmail, capturedUser.getEmail());
        assertTrue(capturedUser.getUsername().startsWith(testEmail.split("@")[0]));
        assertEquals("test", capturedUser.getFullName());
        assertEquals(null, capturedUser.getProfileImage());
        assertTrue(capturedUser.isActive());
        assertNotNull(capturedUser.getRoles());
        assertEquals(1, capturedUser.getRoles().size());
    }

    @Test
    void processGoogleToken_WithExistingUser_ShouldUpdateUser() {
        // 準備
        mockUserInfoFetch(true); // 模擬現有用戶

        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setEmail(testEmail);
        existingUser.setUsername("existing_user");
        existingUser.setFullName("Existing User");
        existingUser.setProfileImage("old-picture-url");

        when(userService.findByEmail(testEmail)).thenReturn(Optional.of(existingUser));
        when(userService.save(any(User.class))).thenReturn(existingUser);

        // 執行
        User user = oAuth2Service.processGoogleToken(testAccessToken);

        // 驗證
        assertNotNull(user);
        assertEquals(testEmail, user.getEmail());
        assertEquals("existing_user", user.getUsername()); // 用戶名不變
        assertEquals("Existing User", user.getFullName()); // 全名不變
        assertEquals(testPicture, user.getProfileImage()); // 圖片被更新
    }

    // 模擬獲取訪問令牌的響應
    private void mockTokenExchange() {
        ObjectNode tokenResponse = objectMapper.createObjectNode();
        tokenResponse.put("access_token", testAccessToken);
        tokenResponse.put("token_type", "Bearer");
        tokenResponse.put("expires_in", 3600);

        when(restTemplate.exchange(
                eq("https://oauth2.googleapis.com/token"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(ResponseEntity.ok(tokenResponse.toString()));
    }

    // 模擬獲取用戶信息的響應
    private void mockUserInfoFetch(boolean includeAllFields) {
        ObjectNode userInfo = objectMapper.createObjectNode();
        String testSub = "123456789012345678901";
        userInfo.put("sub", testSub);
        userInfo.put("email", testEmail);

        if (includeAllFields) {
            String testName = "Test User";
            userInfo.put("name", testName);
            userInfo.put("picture", testPicture);
        }

        when(restTemplate.exchange(
                eq("https://www.googleapis.com/oauth2/v3/userinfo"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(ResponseEntity.ok(userInfo.toString()));
    }
}