package com.back.domain.member.member.service;


import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.repository.MemberRepository;
import com.back.global.globalExceptionHandler.GlobalExceptionHandler.MemberDuplicateUsernameException;
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

    public Optional<Member> findLatest(){
        return memberRepository.findFirstByOrderByIdDesc();
    }

    public long count() {
        return memberRepository.count();
    }
}
