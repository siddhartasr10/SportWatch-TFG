package com.reactive.SportWatch.services;

import com.reactive.SportWatch.models.ExtUserDetails;
import com.reactive.SportWatch.repositories.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.ReactiveUserDetailsPasswordService;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;


@Service
public class UserService implements ReactiveUserDetailsService, ReactiveUserDetailsPasswordService {

    private final UserRepository users;

    @Autowired
    UserService(UserRepository UserRepository) {
        this.users = UserRepository;
    }

    public Mono<UserDetails> findByUsername(String username) {
        return Mono.just(users.findByUsername(username));
    }

    public Mono<ExtUserDetails> extFindByUsername(String username) {
        return Mono.just(users.extFindByUsername(username));
    }

    public Mono<UserDetails> updatePassword(UserDetails user, String password) {
        return Mono.just(users.updatePassword(user, password));
    }

    public Mono<ExtUserDetails> updatePassword(ExtUserDetails user, String password) {
        return Mono.just(users.updatePassword(user, password));
    }

}
