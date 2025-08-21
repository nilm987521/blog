package cc.nilm.blog.controller;

import cc.nilm.blog.dto.JwtResponse;
import cc.nilm.blog.entity.User;
import cc.nilm.blog.security.UserDetailsImpl;
import cc.nilm.blog.service.JwtService;
import cc.nilm.blog.service.OAuth2Service;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/auth/oauth2")
@RequiredArgsConstructor
public class OAuth2Controller {

    private final OAuth2Service oAuth2Service;
    private final JwtService jwtService;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    /**
     * 啟動 Google OAuth2 流程
     */
    @GetMapping("/google")
    public RedirectView startGoogleOAuth2(@RequestParam(value = "redirect_uri", required = false) String redirectUri) {
        log.info("OAuth2 啟動流程，重定向 URI: {}", redirectUri);
        String authorizationUrl = oAuth2Service.getGoogleAuthorizationUrl(redirectUri);
        log.info("OAuth2 授權 URL: {}", authorizationUrl);
        return new RedirectView(authorizationUrl);
    }

    /**
     * Google OAuth2 回調處理 - 舊方法
     */
    @GetMapping("/code/google")
    public void handleGoogleCallback(@RequestParam("code") String code,
                                     @RequestParam(value = "state", required = false) String state,
                                     HttpServletResponse response) throws IOException {
        try {
            // 處理 OAuth2 回調並獲取用戶信息
            User user = oAuth2Service.processGoogleCallback(code, state);
            
            // 創建認證對象
            UserDetailsImpl userDetails = UserDetailsImpl.build(user);
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // 獲取重定向 URL（如果有的話）
            String redirectUrl = oAuth2Service.getStoredRedirectUri(state);
            if (redirectUrl == null || redirectUrl.isEmpty()) {
                redirectUrl = frontendUrl; // 默認重定向到前端根路徑
            }
            
            // 將認證信息添加到重定向 URL - 簡化URL
            String loginSuccessUrl = redirectUrl;
            
            log.info("OAuth2 登入成功，重定向到: {}", loginSuccessUrl);
            response.sendRedirect(loginSuccessUrl);
        } catch (Exception e) {
            log.error("OAuth2 登入失敗", e);
            String errorUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/login")
                    .queryParam("error", "oauth2_error")
                    .queryParam("message", e.getMessage())
                    .build().toUriString();
            response.sendRedirect(errorUrl);
        }
    }
    
    /**
     * 處理前端發送的授權碼
     */
    @PostMapping("/google/callback")
    public ResponseEntity<?> handleGoogleToken(@RequestBody GoogleAuthCodeRequest request) {
        try {
            // 驗證和處理授權碼
            User user = oAuth2Service.processGoogleCallback(request.getCode(), null);
            
            // 創建認證對象
            UserDetailsImpl userDetails = UserDetailsImpl.build(user);
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // 生成 JWT
            String jwt = jwtService.generateToken(userDetails);
            
            // 準備角色列表
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority).toList();
            
            return ResponseEntity.ok(new JwtResponse(jwt, user.getId(), user.getUsername(), user.getEmail(), roles));
        } catch (Exception e) {
            log.error("處理 Google 授權碼失敗", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Data
    public static class GoogleAuthCodeRequest {
        private String code;
    }
}
