package com.conti.global.auth;

import com.conti.domain.team.repository.TeamMemberRepository;
import com.conti.global.error.BusinessException;
import com.conti.global.error.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Arrays;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class TeamAuthInterceptor implements HandlerInterceptor {

    private final TeamMemberRepository teamMemberRepository;

    @Override
    @SuppressWarnings("unchecked")
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        TeamAuth teamAuth = handlerMethod.getMethodAnnotation(TeamAuth.class);
        if (teamAuth == null) {
            return true;
        }

        Map<String, String> pathVariables =
                (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        String teamIdStr = pathVariables.get("teamId");
        if (teamIdStr == null) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        Long teamId = Long.valueOf(teamIdStr);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        Long userId = (Long) authentication.getPrincipal();

        var teamMember = teamMemberRepository.findByUserIdAndTeamId(userId, teamId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FORBIDDEN));

        String[] allowedRoles = teamAuth.roles();
        if (allowedRoles.length > 0) {
            String memberRole = teamMember.getRole().name();
            boolean hasRole = Arrays.asList(allowedRoles).contains(memberRole);
            if (!hasRole) {
                throw new BusinessException(ErrorCode.FORBIDDEN);
            }
        }

        return true;
    }
}
