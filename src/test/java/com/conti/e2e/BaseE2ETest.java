package com.conti.e2e;

import com.conti.domain.team.entity.Team;
import com.conti.domain.team.entity.TeamMember;
import com.conti.domain.team.entity.TeamRole;
import com.conti.domain.team.repository.TeamMemberRepository;
import com.conti.domain.team.repository.TeamRepository;
import com.conti.domain.user.entity.Provider;
import com.conti.domain.user.entity.User;
import com.conti.domain.user.repository.UserRepository;
import com.conti.global.auth.jwt.JwtTokenProvider;
import com.conti.global.auth.jwt.TokenDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public abstract class BaseE2ETest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected JwtTokenProvider jwtTokenProvider;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected TeamRepository teamRepository;

    @Autowired
    protected TeamMemberRepository teamMemberRepository;

    @Autowired
    protected EntityManager entityManager;

    /**
     * 테스트 사용자를 생성하고 JWT 토큰을 반환한다.
     */
    protected String createUserAndGetToken(String email, String name) {
        User user = User.builder()
                .email(email)
                .name(name)
                .provider(Provider.KAKAO)
                .providerId("test-" + email)
                .build();
        user = userRepository.save(user);
        TokenDto tokens = jwtTokenProvider.generateTokens(user.getId());
        return tokens.accessToken();
    }

    /**
     * 테스트 사용자를 생성하고 User 엔티티를 반환한다.
     */
    protected User createUser(String email, String name) {
        User user = User.builder()
                .email(email)
                .name(name)
                .provider(Provider.KAKAO)
                .providerId("test-" + email)
                .build();
        return userRepository.save(user);
    }

    /**
     * 사용자의 JWT 토큰을 반환한다.
     */
    protected String getToken(Long userId) {
        TokenDto tokens = jwtTokenProvider.generateTokens(userId);
        return tokens.accessToken();
    }

    /**
     * 팀을 생성하고 지정한 사용자를 ADMIN으로 추가한다.
     */
    protected Team createTeamWithAdmin(Long userId) {
        Team team = Team.builder()
                .name("Test Team")
                .description("Test Description")
                .inviteCode(UUID.randomUUID().toString().substring(0, 8))
                .build();
        team = teamRepository.save(team);

        User user = userRepository.findById(userId).orElseThrow();
        TeamMember member = TeamMember.builder()
                .user(user)
                .team(team)
                .role(TeamRole.ADMIN)
                .build();
        teamMemberRepository.save(member);

        return team;
    }

    /**
     * 팀에 사용자를 특정 역할로 추가한다.
     */
    protected TeamMember addTeamMember(Long userId, Long teamId, TeamRole role) {
        User user = userRepository.findById(userId).orElseThrow();
        Team team = teamRepository.findById(teamId).orElseThrow();
        TeamMember member = TeamMember.builder()
                .user(user)
                .team(team)
                .role(role)
                .build();
        return teamMemberRepository.save(member);
    }

    /**
     * JPA 영속성 컨텍스트를 플러시하고 초기화한다.
     * @Transactional 테스트에서 엔티티 캐시 문제를 방지하기 위해 사용한다.
     */
    protected void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    // -- HTTP Helper Methods --

    protected ResultActions performGet(String url, String token) throws Exception {
        return mockMvc.perform(get(url)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON));
    }

    protected ResultActions performPost(String url, String token, Object body) throws Exception {
        return mockMvc.perform(post(url)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }

    protected ResultActions performPatch(String url, String token, Object body) throws Exception {
        return mockMvc.perform(patch(url)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }

    protected ResultActions performPut(String url, String token, Object body) throws Exception {
        return mockMvc.perform(put(url)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }

    protected ResultActions performDelete(String url, String token) throws Exception {
        return mockMvc.perform(delete(url)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON));
    }

    protected ResultActions performPostNoBody(String url, String token) throws Exception {
        return mockMvc.perform(post(url)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON));
    }
}
