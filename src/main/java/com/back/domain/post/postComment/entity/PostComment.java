package com.back.domain.post.postComment.entity;

import com.back.domain.member.member.entity.Member;
import com.back.domain.post.post.entity.Post;
import com.back.global.globalExceptionHandler.GlobalExceptionHandler.AccessDeniedException;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Getter
@NoArgsConstructor
public class PostComment extends BaseEntity {
    @ManyToOne(fetch = LAZY)
    private Member author;
    @ManyToOne(fetch = LAZY)
    private Post post;
    private String content;

    public PostComment(Member author, Post post, String content) {
        this.author = author;
        this.post = post;
        this.content = content;
    }

    // 댓글 권한은 댓글 작성자 기준 (글 작성자 아님)
    public void checkActorCanModify(Member actor) {
        if (author.getId() != actor.getId()) throw new AccessDeniedException();
    }

    public void checkActorCanDelete(Member actor) {
        checkActorCanModify(actor);
    }

    public void modify(String content) {
        this.content = content;
    }
}