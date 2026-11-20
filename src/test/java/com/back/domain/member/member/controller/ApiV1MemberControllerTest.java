package com.back.domain.member.member.controller;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ApiV1MemberControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private MemberService memberService;

    @Test
    @DisplayName("회원가입")
    void t1() throws Exception {
        ResultActions resultActions = mockMvc
                .perform(
                    post("/api/v1/members/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "username" : "myamazingusername",
                                    "password" : "myamazingpassword",
                                    "name" : "myamazingname"
                                }
                                """)
                )
                .andDo(print());

        Member member = memberService.findLatest().get();

        resultActions
                .andExpect(handler().handlerType(ApiV1MemberController.class))
                .andExpect(handler().methodName("join"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.resultCode").value("201-1"))
                .andExpect(jsonPath("$.msg").value("%s님 환영합니다. 회원가입이 완료되었습니다.".formatted(member.getName())));
    }

    @Test
    @DisplayName("회원가입, 중복 username")
    void t2() throws Exception {
        // 같은 username을 미리 가입시켜 둔다 (샘플 데이터에 의존하지 않기)
        memberService.join("myamazingusername", "myamazingpassword", "myamazingname");

        ResultActions resultActions = mockMvc
                .perform(
                        post("/api/v1/members/join")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                {
                                    "username" : "myamazingusername",
                                    "password" : "myamazingpassword",
                                    "name" : "myamazingname2"
                                }
                                """)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1MemberController.class))
                .andExpect(handler().methodName("join"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.resultCode").value("409-1"))
                .andExpect(jsonPath("$.msg").value("myamazingusername(은)는 이미 사용중인 username 입니다."));
    }

    @Test
    @DisplayName("내 정보")
    void t3() throws Exception {
        Member actor = memberService.findByUsername("user1").get();

        ResultActions resultActions = mockMvc
                .perform(
                        get("/api/v1/members/me?actorId=" + actor.getId())
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1MemberController.class))
                .andExpect(handler().methodName("me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(actor.getId()))
                .andExpect(jsonPath("$.createDate").value(Matchers.startsWith(actor.getCreateDate().toString().substring(0, 20))))
                .andExpect(jsonPath("$.modifyDate").value(Matchers.startsWith(actor.getModifyDate().toString().substring(0, 20))))
                .andExpect(jsonPath("$.username").value("user1"))
                .andExpect(jsonPath("$.name").value("유저1"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @DisplayName("내 정보, actorId 없음")
    void t4() throws Exception {
        ResultActions resultActions = mockMvc
                .perform(
                        get("/api/v1/members/me")
                )
                .andDo(print());

        resultActions
                .andExpect(handler().methodName("me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.resultCode").value("401-1"))
                .andExpect(jsonPath("$.msg").value("로그인 후 이용해주세요."));
    }

    @Test
    @DisplayName("내 정보, 존재하지 않는 actorId")
    void t5() throws Exception {
        ResultActions resultActions = mockMvc
                .perform(
                        get("/api/v1/members/me?actorId=" + Integer.MAX_VALUE)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().methodName("me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.resultCode").value("401-1"))
                .andExpect(jsonPath("$.msg").value("로그인 후 이용해주세요."));
    }
}
