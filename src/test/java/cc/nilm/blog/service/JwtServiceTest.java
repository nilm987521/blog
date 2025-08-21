package cc.nilm.blog.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @Spy
    @InjectMocks
    private JwtService jwtService;

    @Mock
    private UserDetails userDetails;

    @Mock
    private HttpServletRequest request;

    private final long testExpirationMs = 60000; // 1 minute
    private final String testUsername = "testUser";

    @BeforeEach
    void setUp() {
        // 使用ReflectionTestUtils設置私有屬性的值，以便測試
        String testSecret = "testSecretKeyMustBeAtLeast64BytesLongForHS512AlgorithmSoWeNeedToMakeItLonger";
        ReflectionTestUtils.setField(jwtService, "jwtSecret", testSecret);
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", testExpirationMs);
    }

    @Test
    void generateToken_ShouldReturnValidJWT() {
        // 執行
        String token = jwtService.generateToken(userDetails);

        // 驗證
        assertNotNull(token);
        assertThat(token).isNotEmpty();
    }

    @Test
    void validateToken_WithValidToken_ShouldReturnTrue() {
        // 準備
        when(userDetails.getUsername()).thenReturn(testUsername);
        String token = jwtService.generateToken(userDetails);

        // 執行
        boolean isValid = jwtService.validateToken(token, userDetails);

        // 驗證
        assertTrue(isValid);
    }

    @Test
    void validateToken_WithExpiredToken_ShouldReturnFalse() {
        // 準備
        // 設置一個非常短的過期時間
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", 1); // 1毫秒
        String token = jwtService.generateToken(userDetails);

        // 確保token過期
        await().atMost(10, TimeUnit.MICROSECONDS);

        // 執行
        assertThrows(ExpiredJwtException.class, () -> jwtService.validateToken(token, userDetails));
    }

    @Test
    void validateToken_WithWrongUsername_ShouldReturnFalse() {
        // 準備
        when(userDetails.getUsername()).thenReturn("mockUser");
        String token = jwtService.generateToken(userDetails);
        UserDetails wrongUser = mock(UserDetails.class);
        when(wrongUser.getUsername()).thenReturn("wrongUser");

        // 執行
        boolean isValid = jwtService.validateToken(token, wrongUser);

        // 驗證
        assertFalse(isValid);
    }

    @Test
    void extractUsername_ShouldReturnCorrectUsername() {
        // 準備
        when(userDetails.getUsername()).thenReturn("testUser");
        String token = jwtService.generateToken(userDetails);

        // 執行
        String extractedUsername = jwtService.extractUsername(token);

        // 驗證
        assertEquals(testUsername, extractedUsername);
    }

    @Test
    void extractExpiration_ShouldReturnCorrectExpirationDate() {
        // 準備
        long currentTimeMillis = System.currentTimeMillis();
        String token = jwtService.generateToken(userDetails);

        // 執行
        Date expirationDate = jwtService.extractExpiration(token);

        // 驗證
        assertTrue(expirationDate.getTime() >= currentTimeMillis + testExpirationMs - 1000); // 允許1秒的誤差
        assertTrue(expirationDate.getTime() <= currentTimeMillis + testExpirationMs + 1000);
    }

    @Test
    void extractClaim_ShouldExtractSpecificClaim() {
        // 準備
        when(userDetails.getUsername()).thenReturn(testUsername);
        String token = jwtService.generateToken(userDetails);

        // 執行
        String subject = jwtService.extractClaim(token, Claims::getSubject);
        Date issuedAt = jwtService.extractClaim(token, Claims::getIssuedAt);

        // 驗證
        assertEquals(testUsername, subject);
        assertNotNull(issuedAt);
        assertTrue(issuedAt.getTime() <= System.currentTimeMillis());
    }

    @Test
    void extractAllClaims_WithInvalidToken_ShouldThrowException() {
        // 準備
        String invalidToken = "invalid.token.structure";

        // 執行與驗證
        assertThrows(MalformedJwtException.class, () -> {
            jwtService.extractClaim(invalidToken, Claims::getSubject);
        });
    }

    @Test
    void extractAllClaims_WithTamperedToken_ShouldThrowException() {
        // 準備
        String token = jwtService.generateToken(userDetails);
        // 修改token的最後一個字符以模擬篡改
        String tamperedToken = token.substring(0, token.length() - 1) + (token.charAt(token.length() - 1) == 'A' ? 'B' : 'A');

        // 執行與驗證
        assertThrows(SignatureException.class, () -> {
            jwtService.extractClaim(tamperedToken, Claims::getSubject);
        });
    }

    @Test
    void parseJwt_WithValidAuthorizationHeader_ShouldReturnToken() {
        // 準備
        String token = "valid.token.string";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        // 執行
        String parsedToken = jwtService.parseJwt(request);

        // 驗證
        assertEquals(token, parsedToken);
    }

    @ParameterizedTest
    @ValueSource(strings = {"null", "Token valid.token.string"})
    void parseJwt(String token) {
        // 準備
        if (!token.equals("null")) {
            when(request.getHeader("Authorization")).thenReturn(token);
        } else {
            when(request.getHeader("Authorization")).thenReturn(null);
        }

        // 執行
        String parsedToken = jwtService.parseJwt(request);

        // 驗證
        assertNull(parsedToken);
    }

    @Test
    void parseJwt_WithNullHeader_ShouldReturnNull() {
        // 準備
        when(request.getHeader("Authorization")).thenReturn(null);

        // 執行
        String parsedToken = jwtService.parseJwt(request);

        // 驗證
        assertNull(parsedToken);
    }

    @Test
    void expiredToken_ShouldThrowExpiredJwtException() {
        // 準備
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", 1); // 1毫秒
        String token = jwtService.generateToken(userDetails);

        // 確保token過期
        await().atMost(10, TimeUnit.MICROSECONDS);

        // 執行與驗證
        assertThrows(ExpiredJwtException.class, () -> {
            jwtService.extractUsername(token);
        });
    }
}