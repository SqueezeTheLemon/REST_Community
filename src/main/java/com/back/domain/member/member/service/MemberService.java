package com.back.domain.member.member.service;


import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.repository.MemberRepository;
import com.back.global.globalExceptionHandler.GlobalExceptionHandler.MemberDuplicateUsernameException;
import com.back.global.globalExceptionHandler.GlobalExceptionHandler.UnauthenticatedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;

    public Member join(String username, String password, String name) {
        // 저장 전에 중복 검사 → 있으면 409
        findByUsername(username).ifPresent(m -> {
            throw new MemberDuplicateUsernameException(username);
        });

        Member member = new Member(username, password, name);

        return memberRepository.save(member);
    }

    public Optional<Member> findByUsername(String username) {
        return memberRepository.findByUsername(username);
    }

    public Optional<Member> findById(int id) {
        return memberRepository.findById(id);
    }

    // actorId → 회원. 없거나 존재하지 않으면 401
    // actorId가 필요한 모든 API에서 이 메서드 하나만 쓴다
    public Member getActor(Integer actorId) {
        if (actorId == null) throw new UnauthenticatedException();

        return findById(actorId).orElseThrow(UnauthenticatedException::new);
    }

    public Optional<Member> findLatest(){
        return memberRepository.findFirstByOrderByIdDesc();
    }

    public long count() {
        return memberRepository.count();
    }
}
